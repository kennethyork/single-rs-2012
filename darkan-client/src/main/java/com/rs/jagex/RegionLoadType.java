package com.rs.jagex;

import com.rs.jagex.AreadSound.SoundType;

public enum RegionLoadType {

	LOAD_MAP_SCENE_BACKGROUND(false, false),
	aRegionLoadType_3153(true, false),
	LOAD_MAP_SCENE_NORMAL(false, false),
	aRegionLoadType_3155(true, false),
	aRegionLoadType_3156(true, false),
	aRegionLoadType_3157(true, true),
	aRegionLoadType_3161(true, true),
	aRegionLoadType_3152(false, false);

	public static void playMusic(SongReference class116_0, int volume) {
		volume = volume * Class393.preferences.musicVolume.method12714() >> 8;
		if (class116_0 == null)
			VarBitDefinitions.method3805();
		else {
			HostNameIdentifier.method487(class116_0, volume);
			VarBitIndexLoader.method3912();
		}
	}
	public static AreadSound playSoundVorbis(int soundId, int repeat, int delay, int volume, int sourceTile, boolean isVoice, int sampleRate) {
		if ((isVoice ? Class393.preferences.voiceOverVolume.method12714() : Class393.preferences.soundEffectVolume.method12714()) != 0 && repeat != 0 && Class260.SOUNDS_SIZE < 50 && soundId != -1) {
			AreadSound class268_8 = new AreadSound(isVoice ? SoundType.VOICE : SoundType.VORBIS_EFFECT, soundId, repeat, delay, volume, sourceTile, sampleRate, null);
			Class260.SOUNDS[++Class260.SOUNDS_SIZE - 1] = class268_8;
			return class268_8;
		}
		return null;
	}

	public static void method4415(int i_0, int i_1, int i_2, ObjectDefinition objectdefinitions_3) {
		for (Node_Sub48 class282_sub48_5 = (Node_Sub48) Node_Sub48.aClass482_8073.head(); class282_sub48_5 != null; class282_sub48_5 = (Node_Sub48) Node_Sub48.aClass482_8073.next())
			if (i_0 == class282_sub48_5.anInt8076 && class282_sub48_5.anInt8107 == i_1 << 9 && i_2 << 9 == class282_sub48_5.anInt8078 && class282_sub48_5.aClass478_8104.id == objectdefinitions_3.id) {
				if (class282_sub48_5.aNode_Sub15_Sub5_8099 != null) {
					Class79.aNode_Sub15_Sub4_783.method15276(class282_sub48_5.aNode_Sub15_Sub5_8099);
					class282_sub48_5.aNode_Sub15_Sub5_8099 = null;
				}
				if (class282_sub48_5.aNode_Sub15_Sub5_8096 != null) {
					Class79.aNode_Sub15_Sub4_783.method15276(class282_sub48_5.aNode_Sub15_Sub5_8096);
					class282_sub48_5.aNode_Sub15_Sub5_8096 = null;
				}
				class282_sub48_5.unlink();
				break;
			}
	}

	static void method4416() {
		Class187.anInt2363 = 0;
		Class187.MINIMAP_FLAG_X = -1;
		Class187.MINIMAP_FLAG_Y = -1;
	}

	boolean allowDynamicMapScene;

	boolean aBool3159;

	RegionLoadType(boolean allowDynamicMapScene, boolean bool_2) {
		this.allowDynamicMapScene = allowDynamicMapScene;
		aBool3159 = bool_2;
	}

	boolean allowDynamicMapScene() {
		return allowDynamicMapScene;
	}

	public boolean method4401() {
		return aBool3159;
	}
}
