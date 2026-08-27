package com.rs.jagex;

public class GraphicsToolkitPreference extends Preference {

	public boolean aBool7914;
	boolean aBool7913 = true;

	public GraphicsToolkitPreference(GamePreferences class282_sub54_1) {
		super(class282_sub54_1);
	}

	public GraphicsToolkitPreference(int i_1, GamePreferences class282_sub54_2) {
		super(i_1, class282_sub54_2);
	}

	@Override
	public int checkValid(int i_1) {
		// Android has no OpenGL/DirectX native libraries; only the safe-mode
		// JavaRenderer (toolkit 0) is usable there.
		if (Boolean.getBoolean("darkan.android"))
			return 0;
		return i_1 == 3 && !LibraryLoader.getLoader().hasDxLibrary() ? 3 : 2;
	}

	@Override
	int getDefaultValue() {
		aBool7914 = true;
		if (Boolean.getBoolean("darkan.android"))
			return 0;
		return 2;
	}

	public int getValue() {
		return value;
	}

	public void method12773() {
		if (value < 0 || value > 5)
			value = getDefaultValue();
		if (Boolean.getBoolean("darkan.android"))
			value = 0;
	}

	public boolean method12774() {
		return true;
	}

	boolean method12777() {
		return aBool7913;
	}

	public void method12783(boolean bool_1) {
		aBool7913 = bool_1;
	}

	void method7780(int i_1) {
		aBool7914 = false;
		value = Boolean.getBoolean("darkan.android") ? 0 : i_1 * -754033619 * -859024475;
	}

	int method7786() {
		aBool7914 = true;
		return Boolean.getBoolean("darkan.android") ? 0 : 2;
	}

	int method7787() {
		aBool7914 = true;
		return Boolean.getBoolean("darkan.android") ? 0 : 2;
	}

	@Override
	void setValue(int i_1) {
		aBool7914 = false;
		value = Boolean.getBoolean("darkan.android") ? 0 : i_1;
	}

}
