
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

import com.sun.media.jsdt.*;
import java.io.*;
import java.util.Hashtable;


public class
CommandConsumer implements ChannelConsumer, SoundDebugFlags {

    // The name of this channel consumer.
    protected final String name;

    // Hashtable of audio file names / sample rates.
    private final Hashtable<String, Integer> sampleRates;

    // Hashtable of audio file names / number of channels.
    private final Hashtable<String, Integer> channels;

    // Hashtable of audio file names / audio file encodings.
    private final Hashtable<String, Integer> encodings;


    public
    CommandConsumer(String name) {
        if (CommandConsumer_Debug) {
            System.err.println("CommandConsumer: constructor:" +
                               " name: " + name);
        }

        this.name = name;
        sampleRates = new Hashtable<>();
        channels    = new Hashtable<>();
        encodings   = new Hashtable<>();
    }


    public synchronized void
    dataReceived(Data data) {
        ByteArrayInputStream bais;
        DataInputStream      dis;

        if (CommandConsumer_Debug) {
            System.err.println("CommandConsumer: dataReceived.");
        }

        bais = new ByteArrayInputStream(data.getDataAsBytes(),
                                        0, data.getLength());
        dis  = new DataInputStream(bais);
        try {
            String audioFileName = dis.readUTF();
            int    sampleRate    = dis.readInt();
            int    encoding      = dis.readInt();
            int    noChannels    = dis.readInt();

            if (CommandConsumer_Debug) {
                System.err.println("dataReceived:" +
                                   " audio file name: " + audioFileName +
                                   " sample rate: "     + sampleRate +
                                   " encoding: "        + encoding +
                                   " no of channels: "  + noChannels);
            }

            sampleRates.put(audioFileName, sampleRate);
            encodings.put(audioFileName,   encoding);
            channels.put(audioFileName,    noChannels);
        } catch (IOException e) {
            if (CommandConsumer_Debug) {
                System.err.println("CommandConsumer: dataReceived:" +
                                   " exception " + e);
                e.printStackTrace();
            }
        }
    }


    public int
    getSampleRate(String audioFileName) {
        Integer sampleRate;

        if (CommandConsumer_Debug) {
            System.err.println("CommandConsumer: getSampleRate:" +
                                " audio file name: " + audioFileName);
        }

        sampleRate = (Integer) sampleRates.get(audioFileName);
        return((sampleRate != null) ? sampleRate.intValue() : 0);
    }


    public int
    getChannels(String audioFileName) {
        Integer noChannels;

        if (CommandConsumer_Debug) {
            System.err.println("CommandConsumer: getChannels:" +
                                " audio file name: " + audioFileName);
        }

        noChannels = (Integer) channels.get(audioFileName);
        return((noChannels != null) ? noChannels.intValue() : 0);
    }


    public int
    getEncoding(String audioFileName) {
        Integer encoding;

        if (CommandConsumer_Debug) {
            System.err.println("CommandConsumer: getEncoding:" +
                                " audio file name: " + audioFileName);
        }

        encoding = (Integer) encodings.get(audioFileName);
        return((encoding != null) ? encoding.intValue() : 0);
    }



    public boolean
    isSoundInfo(String audioFileName) {
        if (CommandConsumer_Debug) {
            System.err.println("CommandConsumer: isSoundInfo:" +
                                " audio file name: " + audioFileName);
        }

        return(sampleRates.containsKey(audioFileName) &&
               channels.containsKey(audioFileName)    &&
               encodings.containsKey(audioFileName));
    }
}
