package com.rs.jagex;

import java.io.File;

/**
 * Android build of Class191. The desktop version opens a javax.swing.JFileChooser
 * (unavailable on Android). On Android this is a no-op holder: file selection is
 * not currently wired to the platform picker (see android/PORT.md), so
 * method3154() reports no file was chosen.
 */
public class Class191 implements Runnable {

    public static void method3166() {
        ParticleProducerDefinition.aClass229_533.method3859();
    }

    File aFile2385;
    boolean aBool2387;
    String aString2384;
    boolean aBool2386;

    public Class191(String string_1) {
        aString2384 = string_1;
        aBool2386 = true;
    }

    public boolean method3154() {
        return aBool2387;
    }

    public File method3161() {
        return aFile2385;
    }

    @Override
    public void run() {
        aBool2387 = true;
    }
}
