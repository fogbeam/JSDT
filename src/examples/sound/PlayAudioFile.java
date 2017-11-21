
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

/**
 * This is just a simple server/client based proof-of-concept
 * implementation to test UDP channels.
 *
 * The server continuously sends a sound file over a UDP channel
 * to each client joined to the channel. These in turn, play the sound on
 * the workstation speaker.
 *
 * Code to implement this is based upon the audio jukebox work of Befhar
 * Razavi.
 *
 * @version     2.3 - 20th November 2017
 * @author      Rich Burridge
 */

public class
PlayAudioFile extends Thread implements SoundDebugFlags {

    // The channel to send the audio file over.
    private Channel audioChannel = null;

    // The server-side client already joined to this channel.
    private final Client client;

    // The audio file reader.
    private AudioFileReader afr;

    // Indication of whether this thread should be suspended or not.
    private boolean suspend = false;


    public
    PlayAudioFile(Channel audioChannel, Client client, String audioFileName) {
        if (PlayAudioFile_Debug) {
            System.err.println("PlayAudioFile: constructor:" +
                               " audio channel: "   + audioChannel +
                               " client: "          + client +
                               " audio file name: " + audioFileName);
        }

        this.audioChannel  = audioChannel;
        this.client        = client;
        try {
            afr = new AudioFileReader(audioFileName, 50);
        } catch (Exception e) {
            System.err.println("PlayAudioFile: constructor: exception " + e);
            if (PlayAudioFile_Debug) {
                e.printStackTrace();
            }
        }
    }


    public AudioFileReader
    getReader() {
        if (PlayAudioFile_Debug) {
            System.err.println("PlayAudioFile: getReader.");
        }

        return(afr);
    }


    public void
    suspendThread(boolean suspend) {
        if (PlayAudioFile_Debug) {
            System.err.println("PlayAudioFile: suspendThread:" +
                               " suspend: " + suspend);
        }

        this.suspend = suspend;
        if (!suspend) {
            synchronized (this) {
                notifyAll();
            }
        }
    }


/**
 * Grab a chunk of audio, send it, prefetch another chunk. If there is time
 * left until the next chunk is to be sent, go to sleep for that many
 * milliseconds. The audio can have variable size chunks.
  */

    public void
    run() {
        Data data;
        AudioClick click;
        int MSMARGIN = 5;
        long delay;

        if (PlayAudioFile_Debug) {
            System.err.println("PlayAudioFile: run.");
        }

        long tm = System.currentTimeMillis();
        click = afr.get();
        while (true) {
            if (click == null || click.length == 0) {

/* Reached end of file. Reset and server again. */

/* XXX: need to reset to beginning of the audio file correctly. */

                System.err.println("End of file reached.");
                break;
            }

            synchronized(this) {
                while (suspend) {
                    try {
                        wait();
                    } catch (InterruptedException ie) {
                    }
                }
            }

            try {
                if (PlayAudioFile_Debug) {
                    System.err.println("PlayAudioFile: sending data:" +
                                       " length: " + click.length);
                }
                data = new Data(click.buffer, click.length);
                audioChannel.sendToAll(client, data);
            } catch (Exception e) {
                System.err.println("PlayAudioFile: run: exception " + e);
                if (PlayAudioFile_Debug) {
                    e.printStackTrace();
                }
            }

/* Remember how much data was sent and delay accordingly. */

            tm += click.length * 1000 / afr.getSampleRate();

/* Use the delay to prefetch the next audio click. */

            click = afr.get();
            delay = tm - System.currentTimeMillis();

/* If the time to be delayed is less than a margin it is not worth the
 * overhead of sleeping.
 */

            if (delay > MSMARGIN) {
                try {
                    Thread.sleep(delay);
                } catch (Exception e) {
                    System.err.println("PlayAudioFile: run: exception " + e);
                    if (PlayAudioFile_Debug) {
                        e.printStackTrace();
                    }
                }
            } else {
                tm = System.currentTimeMillis();
            }
        }
    }
}
