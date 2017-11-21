
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
import com.sun.media.jsdt.event.*;

/**
 * This is just a simple server/client based proof-of-concept
 * implementation to test UDP channels.
 *
 * The server continuously sends a sound file over a UDP channel
 * to each client joined to the channel. These in turn, play the sound to
 * the computer audio system.
 *
 * @version     2.3 - 20th November 2017
 * @author      Rich Burridge
 */

public class
SoundListener extends ChannelAdaptor implements SoundDebugFlags {

    // The name of the audio file to continuously play on this channel.
    private String audioFileName = null;

    // The server-side client already joined to this channel.
    private final Client client;

    // The command channel to send sound file information over.
    private final Channel commandChannel;

    // The thread playing the audio file.
    private PlayAudioFile paf = null;

    // The audio file reader (for sample rate, encoding and channels).
    private AudioFileReader afr;

    // The number of clients currently joined to this channel.
    private int noClients = 0;


    public
    SoundListener(String audioFileName, Client client,
                  Channel audioChannel, Channel commandChannel) {
        if (SoundListener_Debug) {
            System.err.println("SoundListener: constructor: " +
                                " audio file name: " + audioFileName +
                                " client: "          + client +
                                " audio channel: "   + audioChannel +
                                " command channel: " + commandChannel);
        }

        this.audioFileName  = audioFileName;
        this.client         = client;
        this.commandChannel = commandChannel;
    }


    public void
    channelJoined(ChannelEvent event) {
        Channel audioChannel = event.getChannel();
        SendSoundInfo ssfi;

        if (SoundListener_Debug) {
            System.err.println("SoundListener: channelJoined:" +
                               " event: " + event);
        }

        noClients++;

/* If this is the first client joining the channel, fire off the thread
 * to play the audio file.
 */
        if (noClients == 1) {
            if (paf == null) {
                paf = new PlayAudioFile(audioChannel,
                                        client, audioFileName);
                afr = paf.getReader();
                System.err.println("Starting to play the audio file.");
                paf.start();
            }
        }

// Send the sound file information to the newly joined client.

        ssfi = new SendSoundInfo(commandChannel, client,
                                 event.getClientName(),
                                 audioFileName,
                                 afr.getSampleRate(), afr.getEncoding(),
                                 afr.getChannels());
        ssfi.start();

        if (noClients == 1) {
            System.err.println("Resuming the audio file.");
            paf.suspendThread(false);
        }
    }


    public void
    channelLeft(ChannelEvent event) {
        if (SoundListener_Debug) {
            System.err.println("SoundListener: channelLeft:" +
                               " event: " + event);
        }

        noClients--;

/* If there are no clients currently joined to this channel, then turn off
 * the audio file.
 */
        if (noClients == 0) {
            System.err.println("Suspending the audio file.");

            paf.suspendThread(true);
        }
    }
}
