
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

import java.io.*;
import sun.audio.*;

/**
 * Play an audio stream to Java's Audio Player.
 *
 * Create an AudioStream and handles AudioClick objects.
 *
 * By subclassing InputStream and over-riding the read() method, the
 * Speaker can pass itself to the AudioPlayer and play whatever data
 * is given to it.
 *
 * @version     2.3 - 20th November 2017
 * @author      Behfar Razavi
 */

public class
Speaker extends InputStream implements SoundDebugFlags {

    // The sound consumer to get the audio data from.
    private AudioConsumer audioConsumer;

    // Audio Stream characteristics.

    private final int sampleRate;
    private int totalRead;                     // Total bytes read so far.
    private boolean done;
    private AudioClick prevClick;
    private AudioStream audioStream;           // To be given to Java.

    final int channels;

    public
    Speaker(int sampleRate, int encoding, int channels) {
        int SUN_MAGIC = 0x2e736e64;        // Au file magic number.
        int HDR_SIZE = 24;                 // Minimum au header file size.
        int FILE_LENGTH = 0;               // File length (optional).

        if (Speaker_Debug) {
            System.err.println("Speaker: constructor:" +
                                " sample rate: " + sampleRate +
                                " encoding: "    + encoding +
                                " channels: "    + channels);
        }

        this.sampleRate = sampleRate;
        this.channels   = channels;
        totalRead       = 0;
        done            = false;

/* Create the .au header file and fake it as an incoming packet. The audio
 * stream is purely audio data and doesn't have the header info, but that
 * is needed for Java's audio player which expects to read an au file.
 */

        ByteArrayOutputStream tempOut = new ByteArrayOutputStream();
        DataOutputStream tempData = new DataOutputStream(tempOut);
        try {
            tempData.writeInt(SUN_MAGIC);
            tempData.writeInt(HDR_SIZE);
            tempData.writeInt(FILE_LENGTH);
            tempData.writeInt(encoding);
            tempData.writeInt(sampleRate);
            tempData.writeInt(channels);

            byte[] buf  = tempOut.toByteArray();
            prevClick   = new AudioClick(buf, 0, buf.length);
            audioStream = new AudioStream(this);
        } catch (Exception e) {
            System.err.println("Speaker: constructor: exception " + e);
            if (Speaker_Debug) {
                e.printStackTrace();
            }
        }
    }


    public void
    setConsumer(AudioConsumer audioConsumer) {
        if (Speaker_Debug) {
            System.err.println("Speaker: constructor:" +
                                " audio consumer: " + audioConsumer);
        }

        this.audioConsumer = audioConsumer;
    }


    public int
    read() throws IOException {
        if (Speaker_Debug) {
            System.err.println("Speaker: read.");
        }

        byte buf[] = new byte[1];
        read(buf, 0, 1);
        return((int) buf[0]);
    }


/* Get Audio Clicks from the AudioConsumer and stuff them into the audio
 * stream. If the AudioClick happens to contain more data than is needed,
 * keep the AudioClick around until the next call.
 */

    public synchronized int
    read(byte buf[], int pos, int len) throws IOException {
        AudioClick click;
        int count;

        if (Speaker_Debug) {
            System.err.println("Speaker: read:" +
                               " pos: " + pos +
                               " len: " + len);
        }

        if (len <= 0) {
            return(0);
        }

        if (done) {
            return(-1);
        }

/* If nothing left from last time, get a new AudioClick. Otherwise use up
 *  what you got from last time.
 */

        if (prevClick == null) {
            click = audioConsumer.get();
            if (click == null) {
                return(0);
            }
        } else {
            click = prevClick;
        }

        if (click.length == 0) {    // End of Audio Stream.
            done = true;
            return(-1);
        }

/* If more data is needed than is in the click just return what's here and
 * handle the rest of the call in subsequent reads.
 */

        if (len >= click.length) {
            count = click.length;
            System.arraycopy(click.buffer, click.offset, buf, pos, count);
            prevClick = null;
        } else {
            count = len;
            System.arraycopy(click.buffer, click.offset, buf, pos, count);
            click.consume(count);
            prevClick = click;
        }

        totalRead += count;
        return(count);
    }


    public void
    play() {
        if (Speaker_Debug) {
            System.err.println("Speaker: play.");
        }

        AudioPlayer.player.start(audioStream);
    }


    public void
    stop() {
        if (Speaker_Debug) {
            System.err.println("Speaker: stop.");
        }

        AudioPlayer.player.stop(audioStream);
    }


    public synchronized void
    abort() {
        if (Speaker_Debug) {
            System.err.println("Speaker: abort.");
        }

        done = true;
//        AudioPlayer.player.stop(audioStream);
    }


    public void
    setPosition(int where) {
        if (Speaker_Debug) {
            System.err.println("Speaker: setPosition.");
        }

        totalRead = where * sampleRate;
    }


    public int
    available() {
        return(0);
    }
}
