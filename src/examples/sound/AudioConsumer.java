
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
import java.util.Vector;

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
AudioConsumer implements ChannelConsumer, SoundDebugFlags {

    // The name of this channel consumer.
    protected final String name;

    // The buffered audio data.
    private final Vector<AudioClick> audioBuffer;


    public
    AudioConsumer(String name) {
        if (AudioConsumer_Debug) {
            System.err.println("AudioConsumer: constructor:" +
                               " name: " + name);
        }

        this.name   = name;
        audioBuffer = new Vector<>(200, 50);
    }


    public synchronized void
    dataReceived(Data data) {
        int        length   = data.getLength();
        byte[]     theBytes = new byte[length];
        AudioClick click;

        if (AudioConsumer_Debug) {
            System.err.println("AudioConsumer: dataReceived: length:" + length);
        }

        System.arraycopy(data.getDataAsBytes(), 0, theBytes, 0, length);
        click = new AudioClick(theBytes, 0, length);
        audioBuffer.addElement(click);
    }


/** Return the audio click at the head of the queue. If the queue is empty,
 *  wait a little bit to allow the buffer to fill up some before resuming.
 */

    public AudioClick
    get() {
        int jitterBuffer = 2;
        AudioClick click;

        if (AudioConsumer_Debug) {
            System.err.println("AudioConsumer: get.");
        }

        if (audioBuffer.size() == 0) {
            try {
                Thread.sleep(50 * jitterBuffer);
            } catch (Exception e) {
                System.err.println("AudioConsumer: get: exception " + e);
                if (AudioConsumer_Debug) {
                    e.printStackTrace();
                }
            }
        }

        if (audioBuffer.size() == 0) {
            return(null);
        }

        try {
            click = audioBuffer.firstElement();
            audioBuffer.removeElementAt(0);
        } catch (Exception e) {
            System.err.println("AudioConsumer: get: exception " + e);
            if (AudioConsumer_Debug) {
                e.printStackTrace();
            }
            return(null);
        }

        return(click);
    }
}
