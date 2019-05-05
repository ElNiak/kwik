/*
 * Copyright © 2019 Peter Doornbosch
 *
 * This file is part of Kwik, a QUIC client Java library
 *
 * Kwik is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * Kwik is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package net.luminis.quic;

import net.luminis.tls.ByteUtils;

import java.nio.ByteBuffer;

public class NewTokenFrame extends QuicFrame {

    private byte[] newToken;

    @Override
    byte[] getBytes() {
        return new byte[0];
    }

    public NewTokenFrame parse(ByteBuffer buffer, Logger log) {
        buffer.get();

        int tokenLength = VariableLengthInteger.parse(buffer);
        newToken = new byte[tokenLength];
        buffer.get(newToken);

        log.debug("Got New Token: ", newToken);

        return this;
    }

    @Override
    public String toString() {
        return "NewTokenFrame[" + ByteUtils.bytesToHex(newToken) + "]";
    }
}
