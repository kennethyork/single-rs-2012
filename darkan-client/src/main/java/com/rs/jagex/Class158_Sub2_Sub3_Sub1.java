package com.rs.jagex;

import java.awt.*;
import java.awt.image.*;
import java.util.Hashtable;

public class Class158_Sub2_Sub3_Sub1 extends Class158_Sub2_Sub3 {

	Image anImage10450;

	Class158_Sub2_Sub3_Sub1(JavaRenderer hardwarerenderer_1, Canvas canvas_2, int i_3, int i_4) {
		super(hardwarerenderer_1, canvas_2, i_3, i_4);
		new Rectangle();
		method15632();
	}

	@Override
	public int method14344() {
		return method14353(0, 0);
	}

	@Override
	public int method14345(int i_1, int i_2) {
		Graphics graphics_3 = canvas.getGraphics();
		graphics_3.drawImage(anImage10450, i_1, i_2, canvas);
		return 0;
	}

	@Override
	public int method14346() {
		return method14353(0, 0);
	}

	@Override
	public int method14349() {
		return method14353(0, 0);
	}

	@Override
	public int method14350() {
		return method14353(0, 0);
	}

	@Override
	public int method14351(int i_1, int i_2) {
		Graphics graphics_3 = canvas.getGraphics();
		graphics_3.drawImage(anImage10450, i_1, i_2, canvas);
		return 0;
	}

	@Override
	public int method14352(int i_1, int i_2) {
		Graphics graphics_3 = canvas.getGraphics();
		graphics_3.drawImage(anImage10450, i_1, i_2, canvas);
		return 0;
	}

	@Override
	public int method14353(int i_1, int i_2) {
		Graphics graphics_3 = canvas.getGraphics();
		if (graphics_3 != null)
			graphics_3.drawImage(anImage10450, i_1, i_2, canvas);
		presentToAndroid();
		return 0;
	}

	/**
	 * Forwards the rendered frame to the Android surface when present. Uses
	 * reflection so the shared client source also builds for the desktop target,
	 * where com.rs.android does not exist.
	 */
	private void presentToAndroid() {
		try {
			Class<?> loader = Class.forName("com.rs.android.AndroidLoader");
			loader.getMethod("presentFrame", int[].class, int.class, int.class)
					.invoke(null, anIntArray10240, width, height);
		} catch (Throwable ignored) {
		}
	}

	@Override
	void method15632() {
		super.method15632();
		DataBufferInt databufferint_2 = new DataBufferInt(anIntArray10240, anIntArray10240.length);
		DirectColorModel directcolormodel_3 = new DirectColorModel(32, 16711680, 65280, 255);
		WritableRaster writableraster_4 = Raster.createWritableRaster(directcolormodel_3.createCompatibleSampleModel(width, height), databufferint_2, null);
		anImage10450 = new BufferedImage(directcolormodel_3, writableraster_4, false, new Hashtable());
	}

	@Override
	public void method186() {
	}

	@Override
	public void method212() {
	}

}
