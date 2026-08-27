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

    public static List<Class<?>> getClassesWithAnnotation(String packagePrefix,
                                                          Class<? extends Annotation> annotation)
            throws ClassNotFoundException, IOException {
        if (!onAndroid())
            return Utils.getClassesWithAnnotation(packagePrefix, annotation);
        return invoke("getClassesWithAnnotation",
                new Class<?>[] { String.class, Class.class }, packagePrefix, annotation);
    }

    public static List<Class<?>> getClasses(String packagePrefix)
            throws ClassNotFoundException, IOException {
        if (!onAndroid())
            return Utils.getClasses(packagePrefix);
        return invoke("getClasses", new Class<?>[] { String.class }, packagePrefix);
    }

    public static List<Class<?>> getSubClasses(String packagePrefix, Class<?> superType)
            throws ClassNotFoundException, IOException {
        if (!onAndroid())
            return Utils.getSubClasses(packagePrefix, superType);
        return invoke("getSubClasses",
                new Class<?>[] { String.class, Class.class }, packagePrefix, superType);
    }

    public static List<Method> getMethodsWithAnnotation(String packagePrefix,
                                                        Class<? extends Annotation> annotation) {
        if (!onAndroid())
            return Utils.getMethodsWithAnnotation(packagePrefix, annotation);
        try {
            return invoke("getMethodsWithAnnotation",
                    new Class<?>[] { String.class, Class.class }, packagePrefix, annotation);
        } catch (ClassNotFoundException | IOException e) {
            // The Android scanner does not declare these on this method.
            throw new IllegalStateException(e);
        }
    }

    private static boolean onAndroid() {
        return Boolean.getBoolean("darkan.android");
    }

    @SuppressWarnings("unchecked")
    private static <T> List<T> invoke(String name, Class<?>[] signature, Object... args)
            throws ClassNotFoundException, IOException {
        try {
            Method method = Class.forName(ANDROID_SCANNER).getMethod(name, signature);
            return (List<T>) method.invoke(null, args);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof ClassNotFoundException notFound) throw notFound;
            if (cause instanceof IOException io) throw io;
            if (cause instanceof RuntimeException runtime) throw runtime;
            if (cause instanceof Error error) throw error;
            throw new IllegalStateException(cause);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("The Android class scanner is unavailable.", e);
        }
    }
}
