package com.rs.lib.util;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;

/**
 * Android replacement for rs.darkan:core's com.rs.lib.util.RecordTypeAdapterFactory.
 *
 * The published version calls Class.isRecord() and java.lang.reflect.RecordComponent,
 * both of which Android only gained in API 33. Below that they do not exist at
 * all, so the very first Gson call died with
 * "NoSuchMethodError: No virtual method isRecord()Z in class Ljava/lang/Class;"
 * while the server was still loading worldConfig.json.
 *
 * Records need no special handling here anyway: below minSdk 33 D8 desugars
 * record declarations into ordinary final classes, so Gson's own reflective
 * adapter reads and writes their fields normally. Returning null simply defers
 * to it.
 *
 * The original is removed from the Android copy of the jar by
 * downgradeDarkanCore. See android/PORT.md.
 */
public class RecordTypeAdapterFactory implements TypeAdapterFactory {

    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        // If records ever stop being desugared -- minSdk raised to 33 or above --
        // Gson's reflective adapter can no longer construct them, and silently
        // producing empty objects would be far worse than failing here.
        if (isRecord(type.getRawType()))
            throw new IllegalStateException("Gson cannot deserialize the record "
                    + type.getRawType().getName() + " without a record-aware adapter. "
                    + "Records are no longer being desugared, so this Android "
                    + "replacement for RecordTypeAdapterFactory needs reimplementing.");
        return null;
    }

    /** Class.isRecord() via reflection: it does not exist below API 33. */
    private static boolean isRecord(Class<?> type) {
        try {
            return (Boolean) Class.class.getMethod("isRecord").invoke(type);
        } catch (ReflectiveOperationException notAvailable) {
            return false;
        }
    }
}
