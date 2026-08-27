// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program.  If not, see <http://www.gnu.org/licenses/>.
//
//  Copyright (C) 2021 Trenton Kress
//  This file is part of project: Darkan
//
package com.rs.utils;

import com.rs.lib.util.Utils;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Finds annotated classes, for the plugin, quest and miniquest registries.
 *
 * Delegates to rs.darkan:core's ClassGraph-backed Utils, except on Android:
 * ClassGraph reads .class files from jars and directories, and an Android app
 * has neither -- everything is compiled into classes*.dex inside the APK. There
 * the scan goes to com.rs.android.AndroidClassScanner, reached reflectively so
 * these shared sources still compile for the desktop build.
 */
public final class ClassScanner {

    private static final String ANDROID_SCANNER = "com.rs.android.AndroidClassScanner";

    private ClassScanner() {}

    @SuppressWarnings("unchecked")
    public static List<Class<?>> getClassesWithAnnotation(String packagePrefix,
                                                          Class<? extends Annotation> annotation)
            throws ClassNotFoundException, IOException {
        if (!Boolean.getBoolean("darkan.android"))
            return Utils.getClassesWithAnnotation(packagePrefix, annotation);
        try {
            Method method = Class.forName(ANDROID_SCANNER)
                    .getMethod("getClassesWithAnnotation", String.class, Class.class);
            return (List<Class<?>>) method.invoke(null, packagePrefix, annotation);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof ClassNotFoundException notFound) throw notFound;
            if (cause instanceof IOException io) throw io;
            if (cause instanceof RuntimeException runtime) throw runtime;
            throw new IllegalStateException(cause);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("The Android class scanner is unavailable.", e);
        }
    }
}
