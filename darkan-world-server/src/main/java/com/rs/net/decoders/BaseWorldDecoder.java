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
package com.rs.net.decoders;

import com.rs.lib.Constants;
import com.rs.lib.io.InputStream;
import com.rs.lib.net.Decoder;
import com.rs.lib.net.Encoder;
import com.rs.lib.net.Session;
import com.rs.lib.net.decoders.GrabDecoder;
import com.rs.lib.net.encoders.GrabEncoder;
import com.rs.lib.util.Logger;

public final class BaseWorldDecoder extends Decoder {

	public BaseWorldDecoder() {

	}

	public BaseWorldDecoder(Session connection) {
		super(connection);
	}

	@Override
	public int decode(InputStream stream) {
		if (stream.getRemaining() < 1)
			return 0;
		int packetId = stream.readUnsignedByte();
		switch (packetId) {
		case 14:
			session.setDecoder(null);
			return decodeLogin(stream);
		case 15:
			return decodeGrab(stream);
		default:
			session.setDecoder(null);
			Logger.debug(BaseWorldDecoder.class, "decode", "Connection type: " + packetId + " Remaining: " + stream.getRemaining());
			StringBuilder hex = new StringBuilder();
			for (byte i : stream.getBuffer())
				hex.append(String.format("%02X", i));
			Logger.debug(BaseWorldDecoder.class, "decode", hex.toString());
			return -1;
		}
	}

	private int decodeGrab(InputStream stream) {
		if (stream.getRemaining() < 1)
			return 0;
		int packetSize = stream.readUnsignedByte();
		if (stream.getRemaining() < packetSize)
			return 0;

		int clientVersion = stream.readInt();
		int clientBuild = stream.readInt();
		int customClientBuild = stream.readInt();
		String token = stream.readString();

		GrabEncoder encoder = new GrabEncoder(session);
		session.setEncoder(encoder);
		if (clientVersion != Constants.CLIENT_VERSION || clientBuild != Constants.CLIENT_BUILD ||
				customClientBuild != Constants.CUSTOM_CLIENT_BUILD || !Constants.GRAB_SERVER_TOKEN.equals(token)) {
			encoder.sendOutdatedClientPacket();
			return -1;
		}

		session.setDecoder(new GrabDecoder(session));
		session.sendGrabStartup();
		return stream.getOffset();
	}

	private int decodeLogin(InputStream stream) {
		if (stream.getRemaining() != 0) {
			session.getChannel().close();
			return -1;
		}
		session.setDecoder(new WorldLoginDecoder(session));
		session.setEncoder(new Encoder(session));
		session.sendLoginStartup();
		return stream.getOffset();
	}
}
