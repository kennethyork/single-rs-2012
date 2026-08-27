package com.rs.android;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.net.Uri;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Platform bridge between the 2012 client, the AWT shim, and the Android host.
 * The desktop client reads the game cache from disk and hosts rendering in an
 * AWT Frame; on Android the cache is served from assets and the render target
 * is the GameSurfaceView.
 */
public final class AndroidPlatform {

    private static Context appContext;
    private static String internalDir;
    private static String cacheDir;
    private static String dataDir;
    private static String saveDir;

    private AndroidPlatform() {}

    public static void init(Context ctx) {
        appContext = ctx.getApplicationContext();
        internalDir = appContext.getFilesDir().getAbsolutePath() + "/";
        cacheDir = internalDir + "cache/";
        dataDir = internalDir + "data/";
        saveDir = internalDir + "saves/";
    }

    public static Context getContext() {
        return appContext;
    }

    public static String getWritableDir() {
        return internalDir;
    }

    /**
     * Directory holding the extracted game cache. The 2012 cache must be
     * readable/writable (the client opens it with RandomAccessFile), so it is
     * copied out of assets into internal storage on first boot.
     */
    public static String getCacheDir() {
        return cacheDir;
    }

    /**
     * Directory holding the server's data files (drop tables, spawns, shops,
     * XTEA keys, worldConfig.json), unpacked from assets/data.zip.
     *
     * The server reads these through relative paths, which cannot work here: an
     * Android process starts with "/" as its working directory and there is no
     * chdir in android.system.Os. Settings.getDataPath() is pointed at this
     * directory instead via the darkan.data.path property.
     */
    public static String getDataDir() {
        return dataDir;
    }

    /** Directory the world server writes player saves into. */
    public static String getSaveDir() {
        return saveDir;
    }

    /**
     * Unpacks assets/data.zip into internal storage unless the same archive is
     * already there. ~4100 small files, so they ship as one archive and are
     * unpacked in a single pass rather than copied asset by asset.
     *
     * @throws IOException if the data cannot be unpacked; the server cannot
     *                     start without it.
     */
    public static void extractDataIfNeeded() throws IOException {
        if (appContext == null || dataDir == null)
            throw new IOException("AndroidPlatform.init() has not been called.");

        AssetManager assets = appContext.getAssets();
        String stamp = dataArchiveStamp(assets);
        File target = new File(dataDir);
        File marker = new File(target, COMPLETE_MARKER);
        if (stamp != null && marker.isFile() && stamp.equals(readFile(marker)))
            return;

        if (marker.isFile() && !marker.delete())
            throw new IOException("Cannot clear the previous data marker: " + marker);
        if (!target.isDirectory() && !target.mkdirs())
            throw new IOException("Cannot create the data directory: " + target);

        byte[] buffer = new byte[1 << 16];
        try (ZipInputStream zip = new ZipInputStream(assets.open(DATA_ASSET))) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                // Entries are data/<...>; they unpack alongside, not inside, the
                // data directory itself.
                String name = entry.getName();
                if (name.startsWith("data/")) name = name.substring("data/".length());
                if (name.isEmpty()) continue;
                File out = new File(target, name);
                // Refuse entries that would escape the target directory.
                if (!out.getCanonicalPath().startsWith(target.getCanonicalPath() + File.separator))
                    throw new IOException("Refusing to unpack outside the data directory: " + entry.getName());
                if (entry.isDirectory()) {
                    if (!out.isDirectory() && !out.mkdirs())
                        throw new IOException("Cannot create " + out);
                    continue;
                }
                File parent = out.getParentFile();
                if (parent != null && !parent.isDirectory() && !parent.mkdirs())
                    throw new IOException("Cannot create " + parent);
                try (OutputStream os = new FileOutputStream(out)) {
                    int read;
                    while ((read = zip.read(buffer)) != -1) os.write(buffer, 0, read);
                }
            }
        }
        if (stamp != null) writeFile(marker, stamp);
    }

    /**
     * Identity of the bundled archive, used to re-unpack when the APK ships a
     * newer one. Null if it cannot be determined, in which case the data is
     * unpacked every launch rather than risking a stale copy.
     */
    private static String dataArchiveStamp(AssetManager assets) throws IOException {
        try {
            return "data.zip:" + assets.openFd(DATA_ASSET).getLength();
        } catch (IOException compressedOrMissing) {
            // openFd only works for uncompressed assets. Confirm it is at least
            // present, so a missing archive is a clear error rather than a
            // confusing failure part-way through unpacking.
            assets.open(DATA_ASSET).close();
            return null;
        }
    }

    /** Reports extraction progress so the UI can show something during the first boot. */
    public interface ProgressListener {
        void onCacheProgress(long bytesDone, long bytesTotal);
    }

    /** One entry of assets/cache/manifest.txt: a file name and its exact size. */
    private static final class CacheEntry {
        final String name;
        final long size;

        CacheEntry(String name, long size) {
            this.name = name;
            this.size = size;
        }
    }

    private static final String CACHE_ASSET_DIR = "cache";
    private static final String DATA_ASSET = "data.zip";
    private static final String MANIFEST = "manifest.txt";
    /** Holds the manifest the current extraction was produced from. */
    private static final String COMPLETE_MARKER = ".cache-complete";

    /**
     * Copies the cache bundled in assets/cache/ into internal storage, unless an
     * identical copy is already there.
     *
     * The cache has to live on a real filesystem: both the client and the world
     * server's rs.darkan:core open it with RandomAccessFile, and the server's
     * copy is a prebuilt jar that cannot be taught to read from an AssetManager.
     * So the ~840 MB is paid for twice on device -- once in the APK, once
     * extracted -- and the first launch spends a minute or two on this.
     *
     * Extraction is driven by the manifest that the Gradle stageCacheAssets task
     * writes. Each file is written to a .part alongside its target and renamed
     * only once its size matches the manifest, and the marker recording a
     * successful run stores the manifest itself -- so a kill mid-extract, or a
     * newer bundled cache, both re-extract rather than leaving a truncated cache
     * that looks complete.
     *
     * @throws IOException if the cache cannot be extracted; the game cannot run
     *                     without it, so this is not swallowed.
     */
    public static void extractCacheIfNeeded(ProgressListener listener) throws IOException {
        if (appContext == null || cacheDir == null)
            throw new IOException("AndroidPlatform.init() has not been called.");

        AssetManager assets = appContext.getAssets();
        String manifest = readManifest(assets);
        if (manifest == null)
            throw new IOException("No game cache is bundled in this APK (assets/" + CACHE_ASSET_DIR + "/"
                    + MANIFEST + " is missing). It is built in by the stageCacheAssets Gradle task.");

        File target = new File(cacheDir);
        File marker = new File(target, COMPLETE_MARKER);
        if (marker.isFile() && manifest.equals(readFile(marker)))
            return; // already extracted, and from this same cache

        List<CacheEntry> entries = parseManifest(manifest);
        long total = 0L;
        for (CacheEntry entry : entries) total += entry.size;

        if (!target.isDirectory() && !target.mkdirs())
            throw new IOException("Cannot create the cache directory: " + target);
        // A previous run may have left a stale marker for a different cache.
        if (marker.isFile() && !marker.delete())
            throw new IOException("Cannot clear the previous cache marker: " + marker);

        long done = 0L;
        byte[] buffer = new byte[1 << 16];
        for (CacheEntry entry : entries) {
            File out = new File(target, entry.name);
            if (out.isFile() && out.length() == entry.size) {
                // Left intact by an interrupted run; no need to copy it again.
                done += entry.size;
                if (listener != null) listener.onCacheProgress(done, total);
                continue;
            }
            File part = new File(target, entry.name + ".part");
            try (InputStream in = assets.open(CACHE_ASSET_DIR + "/" + entry.name);
                 OutputStream os = new FileOutputStream(part)) {
                int read;
                while ((read = in.read(buffer)) != -1) {
                    os.write(buffer, 0, read);
                    done += read;
                    if (listener != null) listener.onCacheProgress(done, total);
                }
            }
            if (part.length() != entry.size) {
                part.delete();
                throw new IOException("Extracted " + entry.name + " is " + part.length()
                        + " bytes, expected " + entry.size + ".");
            }
            if (out.isFile() && !out.delete())
                throw new IOException("Cannot replace the existing cache file: " + out);
            if (!part.renameTo(out))
                throw new IOException("Cannot move the extracted cache file into place: " + out);
        }

        writeFile(marker, manifest);
    }

    /** Extracts the cache without progress reporting. */
    public static void extractCacheIfNeeded() throws IOException {
        extractCacheIfNeeded(null);
    }

    private static String readManifest(AssetManager assets) {
        try (InputStream in = assets.open(CACHE_ASSET_DIR + "/" + MANIFEST)) {
            return readStream(in);
        } catch (IOException notBundled) {
            return null;
        }
    }

    private static List<CacheEntry> parseManifest(String manifest) throws IOException {
        List<CacheEntry> entries = new ArrayList<>();
        for (String line : manifest.split("\n")) {
            line = line.trim();
            if (line.isEmpty()) continue;
            int tab = line.lastIndexOf('\t');
            if (tab < 0)
                throw new IOException("Malformed cache manifest line: " + line);
            try {
                entries.add(new CacheEntry(line.substring(0, tab), Long.parseLong(line.substring(tab + 1))));
            } catch (NumberFormatException e) {
                throw new IOException("Malformed cache manifest line: " + line, e);
            }
        }
        if (entries.isEmpty())
            throw new IOException("The bundled cache manifest is empty.");
        return entries;
    }

    private static String readStream(InputStream in) throws IOException {
        StringBuilder text = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            char[] buffer = new char[4096];
            int read;
            while ((read = reader.read(buffer)) != -1) text.append(buffer, 0, read);
        }
        return text.toString();
    }

    private static String readFile(File file) {
        try (InputStream in = new java.io.FileInputStream(file)) {
            return readStream(in);
        } catch (IOException e) {
            return null;
        }
    }

    private static void writeFile(File file, String text) throws IOException {
        try (OutputStream out = new FileOutputStream(file)) {
            out.write(text.getBytes(StandardCharsets.UTF_8));
        }
    }

    public static void openUri(String uri) {
        if (appContext == null) return;
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            appContext.startActivity(intent);
        } catch (Exception ignored) {
        }
    }

    /** Forwards cursor moves (Robot) into the game surface. */
    public static void onMouseMoved(int x, int y) {
        GameSurfaceView v = GameBridge.surfaceView;
        if (v != null) {
            v.gameMouseX = x;
            v.gameMouseY = y;
        }
    }
}
