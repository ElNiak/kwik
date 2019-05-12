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

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;


// https://tools.ietf.org/html/draft-ietf-quic-transport-20#section-16
public class VariableLengthInteger {

    public static int parse(ByteBuffer buffer) {
        long value = parseLong(buffer);
        if (value <= Integer.MAX_VALUE) {
            return (int) value;
        }
        else {
            throw new RuntimeException("value to large for Java int");
        }
    }

    public static long parseLong(ByteBuffer buffer) {
        long value;
        byte firstLengthByte = buffer.get();
        switch ((firstLengthByte & 0xc0) >> 6) {
            case 0:
                value = firstLengthByte;
                break;
            case 1:
                buffer.position(buffer.position() - 1);
                value = buffer.getShort() & 0x3fff;
                break;
            case 2:
                buffer.position(buffer.position() - 1);
                value = buffer.getInt() & 0x3fffffff;
                break;
            case 3:
                buffer.position(buffer.position() - 1);
                value = buffer.getLong() & 0x3fffffffffffffffL;
                break;
            default:
                // Impossible, just to satisfy the compiler
                throw new RuntimeException();
        }
        return value;
    }

    public static int parse(InputStream inputStream) throws IOException {
        long value = parseLong(inputStream);
        if (value <= Integer.MAX_VALUE) {
            return (int) value;
        }
        else {
            throw new RuntimeException("value to large for Java int");
        }
    }

    public static long parseLong(InputStream inputStream) throws IOException {
        long value;
        int firstLengthByte = inputStream.read();
        if (firstLengthByte == -1) {
            throw new EOFException();
        }
        switch ((firstLengthByte & 0xc0) >> 6) {
            case 0:
                value = firstLengthByte;
                break;
            case 1:
                int nextByte = inputStream.read();
                if (nextByte == -1) {
                    throw new EOFException();
                }
                value = ((firstLengthByte & 0x3f) << 8) | (nextByte & 0xff);
                break;
            case 2:
                int byte2 = inputStream.read();
                int byte3 = inputStream.read();
                int byte4 = inputStream.read();
                if (byte2 == -1 || byte3 == -1 || byte4 == -1) {
                    throw new EOFException();
                }
                value = ((firstLengthByte & 0x3f) << 24) | ((byte2 & 0xff) << 16) | ((byte3 & 0xff) << 8) | (byte4 & 0xff);
                break;
            case 3:
                byte[] rawBytes = new byte[8];
                rawBytes[0] = (byte) (firstLengthByte & 0x3f);
                int bytesRead = 0;
                while (bytesRead != 7) {
                    int read = inputStream.read(rawBytes, 1 + bytesRead, 7 - bytesRead);
                    if (read > 0) {
                        bytesRead += read;
                    }
                    else {
                        throw new EOFException();
                    }
                }
                value = ByteBuffer.wrap(rawBytes).getLong();
                break;
            default:
                // Impossible, just to satisfy the compiler
                throw new RuntimeException();
        }
        return value;
    }

    public static int encode(int value, ByteBuffer buffer) {
        // https://tools.ietf.org/html/draft-ietf-quic-transport-20#section-16
        // | 2Bit | Length | Usable Bits | Range                 |
        // +------+--------+-------------+-----------------------+
        // | 00   | 1      | 6           | 0-63                  |
        // | 01   | 2      | 14          | 0-16383               |
        // | 10   | 4      | 30          | 0-1073741823          |
        if (value <= 63) {
            buffer.put((byte) value);
            return 1;
        }
        else if (value <= 16383) {
            buffer.put((byte) ((value / 256) | 0x40));
            buffer.put((byte) (value % 256));
            return 2;
        }
        else if (value <= 1073741823) {
            int initialPosition = buffer.position();
            buffer.putInt(value);
            buffer.put(initialPosition, (byte) (buffer.get(initialPosition) | (byte) 0x80));
            return 4;
        }
        else {
            int initialPosition = buffer.position();
            buffer.putLong(value);
            buffer.put(initialPosition, (byte) (buffer.get(initialPosition) | (byte) 0xc0));
            return 8;
        }
    }

    public static int encode(long value, ByteBuffer buffer) {
        if (value <= Integer.MAX_VALUE) {
            return encode((int) value, buffer);
        }
        // https://tools.ietf.org/html/draft-ietf-quic-transport-20#section-16
        // | 2Bit | Length | Usable Bits | Range                 |
        // +------+--------+-------------+-----------------------+
        // | 11   | 8      | 62          | 0-4611686018427387903 |
        else if (value <= 4611686018427387903L) {
            int initialPosition = buffer.position();
            buffer.putLong(value);
            buffer.put(initialPosition, (byte) (buffer.get(initialPosition) | (byte) 0xc0));
            return 8;
        }
        else {
            throw new IllegalArgumentException("value cannot be encoded in variable-length integer");
        }
    }

}