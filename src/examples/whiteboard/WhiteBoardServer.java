
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

package examples.whiteboard;

import com.sun.media.jsdt.*;

/**
 * This is just a simple server/client based proof-of-concept
 * implementation to make sure that the Shared Data classes have
 * all the required API in them.
 *
 * It's based on WhiteBoard; a test applet for the game server from
 * daniel@vpro.nl. It's been rewritten to use JSDT and to make it
 * easier to modify and maintain.
 *
 * @version     2.3 - 31st October 2017
 * @author      daniel@vpro.nl
 * @author      Rich Burridge
 */

public class
WhiteBoardServer implements WhiteBoardDebugFlags {

    public static void
    main(String args[]) {
        String sessionName = "WBSession";
        WhiteBoardClient client;
        Session          wbSession;
        URLString        url;
        String           sessionType;
        String           hostname;
        int              hostport;

        if (WhiteBoardServer_Debug) {
            System.err.println("WhiteBoardServer: main.");
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

// Create a session, [re]bind it and create a channel.

            client = new WhiteBoardClient("Server");
            wbSession = SessionFactory.createSession(client, url, true);
            wbSession.createChannel(client, "WBChannel", true, true, false);
            System.err.println("Setup and bound WhiteBoard server.");
        } catch (Exception e) {
            System.err.print("WhiteBoardServer: main: ");
            System.err.println("shared data exception: " + e);
            e.printStackTrace();
        }
    }


    private static String
    getHost(String args[]) {
        String defHost = "localhost";  // Default host name for connections.
        int length = args.length;

        if (WhiteBoardServer_Debug) {
            System.err.println("WhiteBoardServer: getHost.");
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
        int defPort = 4466;   // Default port number for connections.
        int length = args.length;

        if (WhiteBoardServer_Debug) {
            System.err.println("WhiteBoardServer: getPort.");
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

        if (WhiteBoardServer_Debug) {
            System.err.println("WhiteBoardServer: getType.");
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
