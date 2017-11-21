
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
 * @version     2.3 - 20th November 2017
 * @author      Rich Burridge
 */

public class
SoundServer implements SoundDebugFlags {

    private static final String tracks[] = {
      "Keep Your Ears Open;images/keepopen.jpg;sounds/keepopen.au",
      "See Everything;images/ckeepno.jpg;sounds/ckeepno.au",
      "Java Here;images/javahere.jpg;sounds/javahere.au"
    };


    public static void
    main(String args[]) {
        String        sessionName        = "SoundSession";
        String        commandChannelName = "CommandChannel";

        Channel       audioChannels[];
        Channel       commandChannel;
        SoundClient   client;
        SoundListener listeners[];
        Session       soundSession;
        URLString     url;
        String        sessionType;
        String        hostname;
        int           hostport;

        int noTracks   = tracks.length;
        String soundBase  = SoundServer.class.getResource("").toString();

        soundBase = soundBase.substring(5) + "../../";

        if (SoundServer_Debug) {
            System.err.println("SoundServer: main: " +
                               "soundBase: " + soundBase);
        }

        hostname    = getHost(args);
        hostport    = getPort(args);
        sessionType = getType(args);
        url         = URLString.createSessionURL(hostname, hostport,
                                                 sessionType, sessionName);
        try {

// Registry running?  Start it if it isn't.

            if (!RegistryFactory.registryExists(sessionType)) {
                RegistryFactory.startRegistry(sessionType);
            }

// Create a session, bind it, join it and create two channels.

            client       = new SoundClient("Server");
            soundSession = SessionFactory.createSession(client, url, true);

            commandChannel = soundSession.createChannel(client,
                                   commandChannelName, true, true, true);

            audioChannels = new Channel[noTracks];
            listeners     = new SoundListener[noTracks];

            for (int i = 0; i < noTracks; i++) {
                String audioFileName = soundBase +
                              tracks[i].substring(tracks[i].lastIndexOf(';')+1);
                if (SoundServer_Debug) {
                    System.err.println("audioFileName: " + audioFileName);
                }

                audioChannels[i] = soundSession.createChannel(client,
                                       tracks[i], false, false, true);
                listeners[i] = new SoundListener(audioFileName, client,
                                             audioChannels[i], commandChannel);
                audioChannels[i].addChannelListener(listeners[i]);
            }

            System.err.println("Setup and bound Sound server.");
        } catch (JSDTException e) {
            System.err.println("SoundServer: main: " +
                               "shared data exception: " + e);
            if (SoundServer_Debug) {
                e.printStackTrace();
            }
        }
    }


    private static String
    getHost(String args[]) {
        String defHost = "localhost";  // Default host name for connections.
        int length = args.length;

        if (SoundServer_Debug) {
            System.err.println("SoundServer: getHost.");
        }

        for (int i = 0; i < length; i++) {
            if (args[i].equals("-server")) {
                if (++i < length) {
                    return(args[i]);
                }
            }
        }
        return(defHost);
    }


    private static int
    getPort(String args[]) {
        int defPort = 4463;           // Default port number for connections.
        int length = args.length;

        if (SoundServer_Debug) {
            System.err.println("SoundServer: getPort.");
        }

        for (int i = 0; i < length; i++) {
            if (args[i].equals("-port")) {
                if (++i < length) {
                    return(Integer.parseInt(args[i]));
                }
            }
        }
        return(defPort);
    }


    private static String
    getType(String args[]) {
        String defType = "socket";   // Default Session type.
        int length = args.length;

        if (SoundServer_Debug) {
            System.err.println("SoundServer: getType.");
        }

        for (int i = 0; i < length; i++) {
            if (args[i].equals("-type")) {
                if (++i < length) {
                    return(args[i]);
                }
            }
        }
        return(defType);
    }
}
