package com.rs.jagex;

import jaggl.OpenGL;

/**
 * Depth attachment for the post-processing framebuffer that, unlike the 2012
 * renderbuffer it replaces, can also be sampled as a texture. The composite
 * pass reads it for ambient occlusion (see {@link PostFx}).
 */
final class SceneDepthTexture implements Interface28 {

	private static final int GL_TEXTURE_2D = 0x0DE1;
	private static final int GL_FRAMEBUFFER = 0x8D40;
	private static final int GL_DEPTH_COMPONENT = 0x1902;
	private static final int GL_DEPTH_COMPONENT24 = 0x81A6;
	private static final int GL_UNSIGNED_INT = 0x1405;
	private static final int GL_NEAREST = 0x2600;
	private static final int GL_REPEAT = 0x2901;
	private static final int GL_TEXTURE_MIN_FILTER = 0x2801;
	private static final int GL_TEXTURE_MAG_FILTER = 0x2800;
	private static final int GL_TEXTURE_WRAP_S = 0x2802;
	private static final int GL_TEXTURE_WRAP_T = 0x2803;
	private static final int GL_TEXTURE_COMPARE_MODE = 0x884C;
	private static final int GL_NONE = 0;
	private static final int GL_TEXTURE_BINDING_2D = 0x8069;

	final int width;
	final int height;
	int texture;

	SceneDepthTexture(int width, int height) {
		this.width = width;
		this.height = height;
		int[] scratch = new int[1];
		OpenGL.glGetIntegerv(GL_TEXTURE_BINDING_2D, scratch, 0);
		int previous = scratch[0];
		OpenGL.glGenTextures(1, scratch, 0);
		texture = scratch[0];
		OpenGL.glBindTexture(GL_TEXTURE_2D, texture);
		OpenGL.glTexImage2Di(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT24, width, height, 0, GL_DEPTH_COMPONENT, GL_UNSIGNED_INT, null, 0);
		OpenGL.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		OpenGL.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		// Repeat, like the scene colour texture: the composite pass addresses
		// both with coordinates that run from 0 to -1 vertically.
		OpenGL.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
		OpenGL.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
		OpenGL.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_COMPARE_MODE, GL_NONE);
		OpenGL.glBindTexture(GL_TEXTURE_2D, previous);
	}

	@Override
	public void method99(int attachment) {
		OpenGL.glFramebufferTexture2DEXT(GL_FRAMEBUFFER, attachment, GL_TEXTURE_2D, texture, 0);
	}

	@Override
	public void method104(int attachment) {
		method99(attachment);
	}

	@Override
	public void method26() {
		delete();
	}

	@Override
	public void method32() {
		delete();
	}

	private void delete() {
		if (texture != 0) {
			OpenGL.glDeleteTextures(1, new int[] { texture }, 0);
			texture = 0;
		}
	}

	@Override
	public int method1() {
		return width;
	}

	@Override
	public int method39() {
		return height;
	}

	@Override
	public int method73() {
		return height;
	}

	@Override
	public int method74() {
		return height;
	}

	@Override
	public int method75() {
		return width;
	}

	@Override
	public int method76() {
		return width;
	}

	@Override
	public int method77() {
		return height;
	}
}
