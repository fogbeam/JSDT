
/*
 *  Copyright (c) 1996-2005 Sun Microsystems, Inc.
 *  All Rights Reserved.
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

package examples.sound;

/**
 * A clip of audio data
 *
 * To allow memory optimizations, an audio click object can be used
 * as a temporary buffer using its offset and length variables.
 *
 * @version     2.3 - 20th November 2017
 * @author      Behfar Razavi
 */

public class
AudioClick implements SoundDebugFlags {

    // The data.

    final byte buffer[];
    int offset;
    int length;


// Initialize buffer, length and offset variables.

    public
    AudioClick(byte buffer[], int offset, int length) {
        if (AudioClick_Debug) {
            System.err.println("AudioClick: constructor: " +
                               " offset: " + offset +
                               " length: " + length);
        }

        this.buffer = buffer;
        this.length = length;
        this.offset = offset;
    }


/* Update the length and offset variables to reflect the amount of audio
 * left in the buffer.
 */

    public void
    consume(int n) {
        if (AudioClick_Debug) {
            System.err.println("AudioClick: consume: " + n);
        }

        if (n > length) {
            length = 0;
        } else {
            length -= n;
            offset += n;
        }
    }
}
