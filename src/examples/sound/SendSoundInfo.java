
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

import com.sun.media.jsdt.*;
import java.io.*;

/**
 * This is just a simple server/client based proof-of-concept
 * implementation to test UDP channels.
 *
 * The server continuously sends a sound file over a UDP channel
 * to each client joined to the channel. These in turn, play the sound on
 * the workstation speaker.
 *
 * @version     2.3 - 21st November 2017
 * @author      Rich Burridge
 */

public class
SendSoundInfo extends Thread implements SoundDebugFlags {

    // The channel to send the audio file information over.
    private final Channel channel;

    // The client sending the audio file info.
    private final Client client;

    // The name of the client to receive the audio file info.
    private final String receivingClientName;

    // The audio information.
    private final String audioFileName;
    private final int    sampleRate;
    private final int    encoding;
    private final int    channels;


    public
    SendSoundInfo(Channel channel, Client client, String receivingClientName,
                  String audioFileName, int sampleRate,
                  int encoding, int channels) {
        if (SendSoundInfo_Debug) {
            System.err.println("SendSoundInfo: constructor: " +
                                " channel: "          + channel +
                                " client: "           + client +
                                " receiving client: " + receivingClientName +
                                " audio file name: "  + audioFileName +
                                " sample rate: "      + sampleRate +
                                " encoding: "         + encoding +
                                " channels: "         + channels);
        }

        this.channel             = channel;
        this.client              = client;
        this.receivingClientName = receivingClientName;
        this.audioFileName       = audioFileName;
        this.sampleRate          = sampleRate;
        this.encoding            = encoding;
        this.channels            = channels;
    }


    public void
    run() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream      dos  = new DataOutputStream(baos);

        if (SendSoundInfo_Debug) {
            System.err.println("SendSoundInfo: run.");
        }

        try {
            dos.writeUTF(audioFileName);
            dos.writeInt(sampleRate);
            dos.writeInt(encoding);
            dos.writeInt(channels);
            dos.flush();

            channel.sendToClient(client, receivingClientName,
                                 new Data(baos.toByteArray()));
        } catch (Exception e) {
            System.err.println("SendSoundInfo: run: exception " + e);
            if (SendSoundInfo_Debug) {
                e.printStackTrace();
            }
        }
    }
}
