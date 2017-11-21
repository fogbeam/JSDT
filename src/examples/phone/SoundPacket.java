
/*
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU Library General Public License as
 *  published by the Free Software Foundation; either version 2, or (at
 *  your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  General Public License for more details.
 *
 *  You should have received a copy of the GNU Library General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 *  02111-1307, USA.
 */

package examples.phone;

import java.io.Serializable;
import java.util.zip.*;

/**
 * The sound packet (encoding, length and data).
 *
 * @version     2.3 - 20th November 2017
 * @author      Rich Burridge
 * @author      Pete Boysen
 */

class
SoundPacket implements PhoneDebugFlags, Serializable {

// Phone optimization - (Pete Boysen - 7th December 1999).

    private static final Deflater zipOut =
                            new Deflater(Deflater.BEST_COMPRESSION, true);

// End of phone optimization.

    private final boolean isBig; // Set if sound packet is in big endian format.
    private final int length;    // The length of the audio data.
    private final byte[] data;   // The audio data bytes.


    public
    SoundPacket(boolean isBig, int length, byte[] data) {
        if (SoundPacket_Debug) {
            System.err.println("SoundPacket: constructor:" +
                                " big endian: " + isBig +
                                " length: "   + length +
                                " data: "     + data);
        }

        this.isBig    = isBig;
        this.length   = length;

/* Phone optimization - (Pete Boysen - 7th December 1999):
 *
 * Compress the data and store it in a new buffer.  Note that this will
 * overwrite the original data in the buffer.
 */

        zipOut.reset();
        zipOut.setInput(data, 0, length);
        zipOut.finish();

        int n = zipOut.deflate(data);

        System.out.println("bytes:" + n);
        this.data = new byte[n];
        System.arraycopy(data, 0, this.data, 0, n);

// End of phone optimization.

    }


    public boolean
    getEndian() {
        if (SoundPacket_Debug) {
            System.err.println("SoundPacket: getEndian.");
        }

        return(isBig);
    }


    public int
    getLength() {
        if (SoundPacket_Debug) {
            System.err.println("SoundPacket: getLength.");
        }

        return(length);
    }


    public byte[]
    getData() {
        if (SoundPacket_Debug) {
            System.err.println("SoundPacket: getData.");
        }

        return(data);
    }
}
