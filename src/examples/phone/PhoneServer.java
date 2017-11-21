
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

/**
 * Simple Internet Phone server.
 *
 * @version     2.3 - 3rd November 2017
 * @author      Rich Burridge
 */

public class
PhoneServer implements PhoneDebugFlags {

    public static void
    main(String args[]) {
        String sessionName = "PhoneSession";
        PhoneClient client;
        Session    phoneSession;
        URLString  url;
        String     sessionType;
        String     hostname;
        int        hostport;

        if (PhoneServer_Debug) {
            System.err.println("PhoneServer: main.");
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

            client       = new PhoneClient("Server");
            phoneSession = SessionFactory.createSession(client, url, false);
            phoneSession.createChannel(client, "PhoneChannel",
                                       false, true, false);
            System.err.println("Setup and bound Phone server.");
        } catch (JSDTException e) {
            System.err.println("PhoneServer: main: exception: " + e);
            if (PhoneServer_Debug) {
                e.printStackTrace();
            }
        }
    }


    private static String
    getHost(String args[]) {
        String defHost = "localhost";  // Default host name for connections.
        int length = args.length;

        if (PhoneServer_Debug) {
            System.err.println("PhoneServer: getHost.");
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
        int defPort = 4469;   // Default port number for connections.
        int length = args.length;

        if (PhoneServer_Debug) {
            System.err.println("PhoneServer: getPort.");
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

        if (PhoneServer_Debug) {
            System.err.println("PhoneServer: getType.");
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
