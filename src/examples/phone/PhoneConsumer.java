
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

package examples.phone;

import com.sun.media.jsdt.*;
import javax.sound.sampled.*;

/**
 * Simple Internet Phone.
 *
 * @version     2.3 - 20th November 2017
 * @author      Rich Burridge
 */

class
PhoneConsumer implements ChannelConsumer, PhoneDebugFlags {

    // The name of this channel consumer.
    protected final String name;

    // The output channel for sound data.
    private final SourceDataLine outputChannel;

    // Indicates whether this is a bigEndian machine.
    private final boolean isBig;


    public
    PhoneConsumer(String name, SourceDataLine outputChannel, boolean isBig) {
        if (PhoneConsumer_Debug) {
            System.err.println("PhoneConsumer: constructor:" +
                                " name: "           + name +
                                " output channel: " + outputChannel +
                                " is big endian: "  + isBig);
        }

        this.name          = name;
        this.outputChannel = outputChannel;
        this.isBig         = isBig;
    }


    private void
    reformat(int length, byte[] data) {
        if (PhoneConsumer_Debug) {
            System.err.println("PhoneConsumer: reformat " +
                               " length: "   + length +
                               " data: " + data);
        }

        for (int i = 0; i < length; i += 2 ) {
            byte temp;

            temp = data[i];
            data[i] = data[i+1];
            data[i+1] = temp;
        }
    }


    public synchronized void
    dataReceived(Data data) {
        if (PhoneConsumer_Debug) {
            System.err.println("PhoneConsumer: dataReceived " +
                               " Data: " + data);
        }

        try {
            SoundPacket soundPacket    = (SoundPacket) data.getDataAsObject();
            boolean     packetEndian   = soundPacket.getEndian();
            int         length         = soundPacket.getLength();
            byte[]      outputData     = soundPacket.getData();

            if (outputChannel != null) {

// Currently assuming everything is pcm_signed_big_endian or
// pcm_signed_little_endian.

                if (!isBig == packetEndian) {
                    reformat(length, outputData);
                }

                outputChannel.write(outputData, 0, outputData.length);
            }
        } catch (Exception e) {
            System.err.println("PhoneConsumer: dataReceived: exception: " + e);
            e.printStackTrace();
        }
    }
}
