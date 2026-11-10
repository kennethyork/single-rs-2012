package com.rs.jagex;

import jaggl.OpenGL;

/**
 * Hands the scene's depth buffer, projection, camera and sun direction to the
 * post-processing composite pass (FilterBloom.composite.glsl), which uses them
 * for screen-space ambient occlusion and screen-space sun shadows. OpenGL HD
 * renderer only.
 *
 * <p>The composite shader's {@code aoStrength} and {@code shadowStrength}
 * uniforms are 0 unless this class sets them, so both effects switch
 * themselves off whenever the depth texture is unavailable. Disable them with
 * -Ddarkan.ambientOcclusion=false and -Ddarkan.shadows=false.
 */
final class PostFx {

	static final boolean AMBIENT_OCCLUSION = !"false".equals(System.getProperty("darkan.ambientOcclusion"));
	static final float AO_STRENGTH = 1.0F;
	static final boolean SHADOWS = !"false".equals(System.getProperty("darkan.shadows"));
	static final float SHADOW_STRENGTH = 1.0F;

	/** True when either effect needs the scene depth texture. */
	static boolean wantsDepth() {
		return AMBIENT_OCCLUSION || SHADOWS;
	}

	/** A texture unit the 2012 renderer never touches. */
	private static final int DEPTH_UNIT = 10;
	private static final int GL_TEXTURE0 = 0x84C0;
	private static final int GL_TEXTURE_2D = 0x0DE1;
	private static final int GL_ACTIVE_TEXTURE = 0x84E0;
	private static final int GL_TEXTURE_BINDING_2D = 0x8069;
	private static final int GL_CURRENT_PROGRAM = 0x8B8D;

	private static final float[] sceneProjection = new float[16];
	private static final float[] sceneView = new float[16];
	private static boolean haveProjection;
	private static final int[] scratch = new int[1];
	private static int previousUnit;
	private static int previousTexture;
	private static boolean bound;

	private PostFx() {
	}

	/**
	 * Called whenever the renderer sets a projection or camera. A perspective
	 * projection (buf[11] != 0) means 3D scene rendering, so the pair current at
	 * that moment is kept; whichever of the two is set second takes the snapshot.
	 */
	static void noteProjection(Matrix44 projection, Matrix44 view) {
		if (Math.abs(projection.buf[11]) > 0.5F) {
			System.arraycopy(projection.buf, 0, sceneProjection, 0, 16);
			System.arraycopy(view.buf, 0, sceneView, 0, 16);
			haveProjection = true;
		}
	}

	static void noteView(Matrix44 view, Matrix44 projection) {
		noteProjection(projection, view);
	}

	/**
	 * Binds the depth texture for the composite draw that is about to happen.
	 * Must be followed by {@link #unbind()} once the draw has been issued.
	 */
	static void bind(SceneDepthTexture depth, HardwareRenderer renderer) {
		bound = false;
		if (depth == null || !haveProjection || !wantsDepth())
			return;
		OpenGL.glGetIntegerv(GL_CURRENT_PROGRAM, scratch, 0);
		int program = scratch[0];
		if (program == 0)
			return;
		int depthLocation = OpenGL.glGetUniformLocation(program, "depthTex");
		int aoLocation = OpenGL.glGetUniformLocation(program, "aoStrength");
		int shadowLocation = OpenGL.glGetUniformLocation(program, "shadowStrength");
		int sunLocation = OpenGL.glGetUniformLocation(program, "sunView");
		if (depthLocation == -1 || (aoLocation == -1 && shadowLocation == -1))
			return;

		OpenGL.glGetIntegerv(GL_ACTIVE_TEXTURE, scratch, 0);
		previousUnit = scratch[0];
		OpenGL.glActiveTexture(GL_TEXTURE0 + DEPTH_UNIT);
		OpenGL.glGetIntegerv(GL_TEXTURE_BINDING_2D, scratch, 0);
		previousTexture = scratch[0];
		OpenGL.glBindTexture(GL_TEXTURE_2D, depth.texture);
		OpenGL.glActiveTexture(previousUnit);

		OpenGL.glUniform1i(depthLocation, DEPTH_UNIT);
		if (aoLocation != -1)
			OpenGL.glUniform1f(aoLocation, AMBIENT_OCCLUSION ? AO_STRENGTH : 0.0F);
		if (shadowLocation != -1 && sunLocation != -1) {
			// Sun direction (towards the sun) from world space into camera space.
			float[] sun = renderer.aFloatArray8747;
			float[] v = sceneView;
			float x = v[0] * sun[0] + v[4] * sun[1] + v[8] * sun[2];
			float y = v[1] * sun[0] + v[5] * sun[1] + v[9] * sun[2];
			float z = v[2] * sun[0] + v[6] * sun[1] + v[10] * sun[2];
			float length = (float) Math.sqrt(x * x + y * y + z * z);
			if (length > 1e-6F) {
				OpenGL.glUniform3f(sunLocation, x / length, y / length, z / length);
				OpenGL.glUniform1f(shadowLocation, SHADOWS ? SHADOW_STRENGTH : 0.0F);
			}
		}
		// The scene is drawn into the post-processing framebuffer upside down
		// (the composite pass flips it back), so the projection that produced
		// the depth buffer is the captured one with its y row negated.
		float[] p = sceneProjection.clone();
		p[1] = -p[1];
		p[5] = -p[5];
		p[9] = -p[9];
		p[13] = -p[13];
		float[] inverse = invert(p);
		if (inverse != null) {
			int inv = OpenGL.glGetUniformLocation(program, "invProj");
			int proj = OpenGL.glGetUniformLocation(program, "proj");
			if (inv != -1)
				OpenGL.glUniform4fv(inv, 4, inverse, 0);
			if (proj != -1)
				OpenGL.glUniform4fv(proj, 4, p, 0);
		}
		bound = true;
	}

	static void unbind() {
		if (!bound)
			return;
		OpenGL.glActiveTexture(GL_TEXTURE0 + DEPTH_UNIT);
		OpenGL.glBindTexture(GL_TEXTURE_2D, previousTexture);
		OpenGL.glActiveTexture(previousUnit);
		bound = false;
	}

	/** Inverse of a column-major 4x4 matrix, or null if it is singular. */
	static float[] invert(float[] m) {
		float[] inv = new float[16];
		inv[0] = m[5] * m[10] * m[15] - m[5] * m[11] * m[14] - m[9] * m[6] * m[15] + m[9] * m[7] * m[14] + m[13] * m[6] * m[11] - m[13] * m[7] * m[10];
		inv[4] = -m[4] * m[10] * m[15] + m[4] * m[11] * m[14] + m[8] * m[6] * m[15] - m[8] * m[7] * m[14] - m[12] * m[6] * m[11] + m[12] * m[7] * m[10];
		inv[8] = m[4] * m[9] * m[15] - m[4] * m[11] * m[13] - m[8] * m[5] * m[15] + m[8] * m[7] * m[13] + m[12] * m[5] * m[11] - m[12] * m[7] * m[9];
		inv[12] = -m[4] * m[9] * m[14] + m[4] * m[10] * m[13] + m[8] * m[5] * m[14] - m[8] * m[6] * m[13] - m[12] * m[5] * m[10] + m[12] * m[6] * m[9];
		inv[1] = -m[1] * m[10] * m[15] + m[1] * m[11] * m[14] + m[9] * m[2] * m[15] - m[9] * m[3] * m[14] - m[13] * m[2] * m[11] + m[13] * m[3] * m[10];
		inv[5] = m[0] * m[10] * m[15] - m[0] * m[11] * m[14] - m[8] * m[2] * m[15] + m[8] * m[3] * m[14] + m[12] * m[2] * m[11] - m[12] * m[3] * m[10];
		inv[9] = -m[0] * m[9] * m[15] + m[0] * m[11] * m[13] + m[8] * m[1] * m[15] - m[8] * m[3] * m[13] - m[12] * m[1] * m[11] + m[12] * m[3] * m[9];
		inv[13] = m[0] * m[9] * m[14] - m[0] * m[10] * m[13] - m[8] * m[1] * m[14] + m[8] * m[2] * m[13] + m[12] * m[1] * m[10] - m[12] * m[2] * m[9];
		inv[2] = m[1] * m[6] * m[15] - m[1] * m[7] * m[14] - m[5] * m[2] * m[15] + m[5] * m[3] * m[14] + m[13] * m[2] * m[7] - m[13] * m[3] * m[6];
		inv[6] = -m[0] * m[6] * m[15] + m[0] * m[7] * m[14] + m[4] * m[2] * m[15] - m[4] * m[3] * m[14] - m[12] * m[2] * m[7] + m[12] * m[3] * m[6];
		inv[10] = m[0] * m[5] * m[15] - m[0] * m[7] * m[13] - m[4] * m[1] * m[15] + m[4] * m[3] * m[13] + m[12] * m[1] * m[7] - m[12] * m[3] * m[5];
		inv[14] = -m[0] * m[5] * m[14] + m[0] * m[6] * m[13] + m[4] * m[1] * m[14] - m[4] * m[2] * m[13] - m[12] * m[1] * m[6] + m[12] * m[2] * m[5];
		inv[3] = -m[1] * m[6] * m[11] + m[1] * m[7] * m[10] + m[5] * m[2] * m[11] - m[5] * m[3] * m[10] - m[9] * m[2] * m[7] + m[9] * m[3] * m[6];
		inv[7] = m[0] * m[6] * m[11] - m[0] * m[7] * m[10] - m[4] * m[2] * m[11] + m[4] * m[3] * m[10] + m[8] * m[2] * m[7] - m[8] * m[3] * m[6];
		inv[11] = -m[0] * m[5] * m[11] + m[0] * m[7] * m[9] + m[4] * m[1] * m[11] - m[4] * m[3] * m[9] - m[8] * m[1] * m[7] + m[8] * m[3] * m[5];
		inv[15] = m[0] * m[5] * m[10] - m[0] * m[6] * m[9] - m[4] * m[1] * m[10] + m[4] * m[2] * m[9] + m[8] * m[1] * m[6] - m[8] * m[2] * m[5];
		float det = m[0] * inv[0] + m[1] * inv[4] + m[2] * inv[8] + m[3] * inv[12];
		if (Math.abs(det) < 1e-12F)
			return null;
		for (int i = 0; i < 16; i++)
			inv[i] /= det;
		return inv;
	}
}
