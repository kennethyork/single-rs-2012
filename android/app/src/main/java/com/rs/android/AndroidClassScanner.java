package com.rs.android;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Classpath scanning for Android, standing in for rs.darkan:core's ClassGraph-backed
 * Utils.getClassesWithAnnotation / getClasses / getSubClasses / getMethodsWithAnnotation.
 *
 * The server finds its packet decoders, plugin handlers, quests and miniquests by
 * scanning the classpath for annotations. ClassGraph does that by reading .class
 * files out of jars and directories -- neither of which exists on Android, where
 * everything is compiled into classes*.dex inside the APK. So the scan is done by
 * reading the class names straight out of the APK's dex tables and resolving them.
 *
 * Signatures match the methods they stand in for exactly: the core jar's calls are
 * repointed here by downgradeDarkanCore (see android/PORT.md), which only changes
 * the owner of the call, not its descriptor.
 */
public final class AndroidClassScanner {

    private static volatile List<String> classNames;

    private AndroidClassScanner() {}

    public static List<Class<?>> getClassesWithAnnotation(String packagePrefix,
                                                          Class<? extends Annotation> annotation)
            throws ClassNotFoundException, IOException {
        List<Class<?>> found = new ArrayList<>();
        for (Class<?> type : getClasses(packagePrefix))
            if (type.isAnnotationPresent(annotation))
                found.add(type);
        return found;
    }

    public static List<Class<?>> getSubClasses(String packagePrefix, Class<?> superType)
            throws ClassNotFoundException, IOException {
        List<Class<?>> found = new ArrayList<>();
        for (Class<?> type : getClasses(packagePrefix))
            if (superType.isAssignableFrom(type) && !superType.equals(type))
                found.add(type);
        return found;
    }

    public static List<Method> getMethodsWithAnnotation(String packagePrefix,
                                                        Class<? extends Annotation> annotation) {
        List<Method> found = new ArrayList<>();
        try {
            for (Class<?> type : getClasses(packagePrefix)) {
                try {
                    for (Method method : type.getDeclaredMethods())
                        if (method.isAnnotationPresent(annotation))
                            found.add(method);
                } catch (Throwable unresolvable) {
                    // getDeclaredMethods resolves parameter and return types; a
                    // class referring to something absent on Android is skipped
                    // rather than failing the whole scan.
                }
            }
        } catch (ClassNotFoundException | IOException e) {
            throw new IllegalStateException("Could not scan " + packagePrefix, e);
        }
        return found;
    }

    public static List<Class<?>> getClasses(String packagePrefix)
            throws ClassNotFoundException, IOException {
        String prefix = packagePrefix.endsWith(".") ? packagePrefix : packagePrefix + ".";
        ClassLoader loader = AndroidClassScanner.class.getClassLoader();
        List<Class<?>> found = new ArrayList<>();
        for (String name : classNames()) {
            if (!name.startsWith(prefix))
                continue;
            try {
                // initialize=false: this only needs the class, not its static
                // initialisers, which would run half the server during a scan.
                found.add(Class.forName(name, false, loader));
            } catch (Throwable unresolvable) {
                // Classes referring to something Android does not have (the
                // MongoDB managers, say) simply are not scannable.
            }
        }
        return found;
    }

    /** Every class name in the APK, read once from its dex tables. */
    private static List<String> classNames() throws IOException {
        List<String> cached = classNames;
        if (cached != null)
            return cached;
        synchronized (AndroidClassScanner.class) {
            if (classNames == null)
                classNames = readClassNames();
            return classNames;
        }
    }

    private static List<String> readClassNames() throws IOException {
        List<String> names = new ArrayList<>();
        String apk = AndroidPlatform.getContext().getApplicationInfo().sourceDir;
        try (ZipFile zip = new ZipFile(apk)) {
            for (Enumeration<? extends ZipEntry> entries = zip.entries(); entries.hasMoreElements(); ) {
                ZipEntry entry = entries.nextElement();
                if (!entry.getName().endsWith(".dex"))
                    continue;
                byte[] dex = new byte[(int) entry.getSize()];
                try (InputStream in = zip.getInputStream(entry)) {
                    int read = 0;
                    while (read < dex.length) {
                        int n = in.read(dex, read, dex.length - read);
                        if (n < 0) break;
                        read += n;
                    }
                }
                readDexClassNames(dex, names);
            }
        }
        return names;
    }

    /**
     * Pulls class names out of a dex file's tables.
     *
     * Each class_def points at a type, each type at a string, and that string is
     * the descriptor, e.g. "Lcom/rs/Foo;". Only three of the header's tables are
     * needed, so the rest of the format can be ignored.
     */
    private static void readDexClassNames(byte[] dex, List<String> into) {
        ByteBuffer buffer = ByteBuffer.wrap(dex).order(ByteOrder.LITTLE_ENDIAN);
        int stringIdsSize = buffer.getInt(56);
        int stringIdsOff = buffer.getInt(60);
        int typeIdsOff = buffer.getInt(68);
        int classDefsSize = buffer.getInt(96);
        int classDefsOff = buffer.getInt(100);

        int[] stringOffsets = new int[stringIdsSize];
        for (int i = 0; i < stringIdsSize; i++)
            stringOffsets[i] = buffer.getInt(stringIdsOff + i * 4);

        for (int i = 0; i < classDefsSize; i++) {
            int typeIdx = buffer.getInt(classDefsOff + i * 32);
            int stringIdx = buffer.getInt(typeIdsOff + typeIdx * 4);
            String descriptor = readMutf8(dex, stringOffsets[stringIdx]);
            if (descriptor.length() > 2 && descriptor.charAt(0) == 'L'
                    && descriptor.charAt(descriptor.length() - 1) == ';')
                into.add(descriptor.substring(1, descriptor.length() - 1).replace('/', '.'));
        }
    }

    /** A dex string: a uleb128 length, then null-terminated MUTF-8. */
    private static String readMutf8(byte[] dex, int offset) {
        int at = offset;
        while ((dex[at] & 0x80) != 0)     // skip the uleb128 character count
            at++;
        at++;
        int end = at;
        while (dex[end] != 0)
            end++;
        return new String(dex, at, end - at, java.nio.charset.StandardCharsets.UTF_8);
    }
}
