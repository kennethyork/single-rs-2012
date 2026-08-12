package com.rs.game.model.entity;

import com.rs.lib.io.OutputStream;

public class ModelRotator {
	public static final ModelRotator RESET = new ModelRotator().addRotator(new Rotation().enableAll());
	
	private final int[] targetData = new int[15];
	private final int[] slotHashes = new int[15];
	private int curr = 0;
	
	public ModelRotator() {
		
	}
	
	public ModelRotator addRotator(Rotation rotator) {
		targetData[curr] = rotator.getData();
		slotHashes[curr++] = rotator.getMask();
		return this;
	}
	
	public void encodeNPC(OutputStream stream) {
		stream.writeByteSubtract(curr);
		for (int i = 0;i < curr;i++) {
			if ((targetData[i] & 0xc000) == 0xc000) {
				stream.writeShortAddLittle(targetData[i] >> 16);
				stream.writeShort(targetData[i] & 0xFFFF);
			} else
				stream.writeShortAddLittle(targetData[i]);
			stream.writeShortAddLittle(slotHashes[i]);
		}		
	}
	
	public void encodePlayer(OutputStream stream) {
		stream.writeByteSubtract(curr);
		for (int i = 0;i < curr;i++) {
			if ((targetData[i] & 0xc000) == 0xc000) {
				stream.writeShortAdd(targetData[i] >> 16);
				stream.writeShortAdd(targetData[i] & 0xFFFF);
			} else
				stream.writeShortAdd(targetData[i]);
			stream.writeShort(slotHashes[i]);
		}		
	}
}
