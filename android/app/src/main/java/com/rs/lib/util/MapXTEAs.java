package com.rs.lib.util;

import com.google.gson.JsonIOException;
import com.google.gson.reflect.TypeToken;
import com.rs.Settings;
import com.rs.lib.file.JsonFileManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

/**
 * Android replacement for rs.darkan:core's com.rs.lib.util.MapXTEAs.
 *
 * The published class hardcodes "./data/map/xteaKeys.json" as a compile-time
 * constant. An Android process starts with "/" as its working directory and
 * cannot change it -- there is no chdir in android.system.Os -- so that path
 * resolves to /data/map/xteaKeys.json, which no app can read. Without the keys
 * every encrypted map region fails to decode, so this is fatal rather than
 * cosmetic.
 *
 * The core jar is already rewritten for Android (see downgradeDarkanCore, which
 * downgrades its Java 24 bytecode); that task also drops the original of this
 * class so this one takes its place. Behaviour is otherwise identical to the
 * published version -- only the path is resolved through Settings.
 */
public final class MapXTEAs {

    private static Map<Integer, int[]> KEYS;

    /**
     * @return the region's keys, or null if absent or all-zero (an all-zero
     *         entry means "not actually encrypted", same as upstream).
     */
    public static final int[] getMapKeys(int regionId) {
        int[] keys = KEYS == null ? null : KEYS.get(regionId);
        if (keys == null || (keys[0] == 0 && keys[1] == 0 && keys[2] == 0 && keys[3] == 0))
            return null;
        return keys;
    }

    @SuppressWarnings("unchecked")
    public static void loadKeys() throws JsonIOException, IOException {
        Logger.info(MapXTEAs.class, "loadKeys", "Loading map XTEAs...");
        File file = Settings.dataFile("map/xteaKeys.json");
        if (!file.exists())
            throw new FileNotFoundException("No map keys file found!");
        KEYS = (Map<Integer, int[]>) JsonFileManager.loadJsonFile(
                file, new TypeToken<Map<Integer, int[]>>() {}.getType());
    }
}
