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
package com.rs.engine.thread;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.time.Instant;

/**
 * Reflective wrapper over jdk.jfr, used by {@link WorldThread} to attach a
 * Flight Recorder dump to slow-tick reports.
 *
 * The Android build compiles the same server sources but runs on ART, which has
 * no jdk.jfr module, so the package cannot be imported directly. Everything goes
 * through reflection instead: on a JDK this behaves exactly as a direct call
 * would, and on Android {@link #isSupported()} reports false and
 * {@link WorldThread} skips recording entirely.
 */
public final class TickRecorder {

	private static final Class<?> CONFIGURATION_CLASS = findClass("jdk.jfr.Configuration");
	private static final Class<?> RECORDING_CLASS = findClass("jdk.jfr.Recording");

	private static Class<?> findClass(String name) {
		try {
			return Class.forName(name);
		} catch (ClassNotFoundException | LinkageError e) {
			return null;
		}
	}

	/** Whether jdk.jfr is present on this runtime (true on a JDK, false on Android). */
	public static boolean isSupported() {
		return CONFIGURATION_CLASS != null && RECORDING_CLASS != null;
	}

	/**
	 * Loads a .jfc recorder template.
	 *
	 * @throws IOException           if the template cannot be read or parsed
	 * @throws IllegalStateException if jdk.jfr is unavailable
	 */
	public static TickRecorder load(Path template) throws IOException {
		requireSupported();
		Object configuration = invokeStatic(CONFIGURATION_CLASS, "create", Path.class, template);
		if (configuration == null)
			throw new IOException("Unable to load flight recorder template: " + template);
		return new TickRecorder(configuration);
	}

	private final Object configuration;

	private TickRecorder(Object configuration) {
		this.configuration = configuration;
	}

	/** Creates a new, not-yet-started recording from this template. */
	public Recording newRecording() {
		try {
			Constructor<?> constructor = RECORDING_CLASS.getConstructor(CONFIGURATION_CLASS);
			return new Recording(constructor.newInstance(configuration));
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException("Failed to create a JFR recording.", e);
		}
	}

	/** A single jdk.jfr.Recording. */
	public static final class Recording {

		private final Object recording;

		private Recording(Object recording) {
			this.recording = recording;
		}

		public void start() {
			invoke(recording, "start");
		}

		public void stop() {
			invoke(recording, "stop");
		}

		/** The recorded events, in .jfr format. */
		public InputStream getStream() throws IOException {
			try {
				Method method = RECORDING_CLASS.getMethod("getStream", Instant.class, Instant.class);
				return (InputStream) method.invoke(recording, null, null);
			} catch (InvocationTargetException e) {
				throw asIOException(e);
			} catch (ReflectiveOperationException e) {
				throw new IllegalStateException("Failed to read the JFR recording.", e);
			}
		}

		private static void invoke(Object target, String name) {
			try {
				RECORDING_CLASS.getMethod(name).invoke(target);
			} catch (InvocationTargetException e) {
				throw asUnchecked(e);
			} catch (ReflectiveOperationException e) {
				throw new IllegalStateException("Failed to " + name + " the JFR recording.", e);
			}
		}
	}

	private static void requireSupported() {
		if (!isSupported())
			throw new IllegalStateException("jdk.jfr is not available on this runtime.");
	}

	private static Object invokeStatic(Class<?> owner, String name, Class<?> parameterType, Object argument)
			throws IOException {
		try {
			return owner.getMethod(name, parameterType).invoke(null, argument);
		} catch (InvocationTargetException e) {
			throw asIOException(e);
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException("Failed to invoke " + owner.getName() + "." + name, e);
		}
	}

	private static IOException asIOException(InvocationTargetException e) {
		Throwable cause = e.getCause();
		if (cause instanceof IOException io)
			return io;
		// jdk.jfr.Configuration#create also throws ParseException, which is not
		// an IOException; surface it as one rather than leaking the wrapper.
		if (cause instanceof Exception)
			return new IOException(cause);
		throw asUnchecked(e);
	}

	private static RuntimeException asUnchecked(InvocationTargetException e) {
		Throwable cause = e.getCause();
		if (cause instanceof RuntimeException runtime)
			return runtime;
		if (cause instanceof Error error)
			throw error;
		return new IllegalStateException(cause);
	}
}
