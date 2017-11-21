
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

package examples.synth;

import com.sun.media.jsdt.*;

/**
 * JSDT "server" for Midi synthesiser jam session.
 *
 * @version     2.3 - 1st November 2017
 * @author      Rich Burridge
 */

public class
MidiServer implements Client, SynthDebugFlags {

    public
    MidiServer(String args[]) {
        Session   session;
        URLString url;
        String    sessionName = "MidiSession";
        String    sessionType = getType(args);
        String    hostname    = getHost(args);
        int       hostport    = getPort(args);

        if (MidiServer_Debug) {
            System.err.println("MidiServer: constructor:");
            for (int i = 0; i < args.length ; i++) {
                System.err.println("args[" + i + "]: " + args[i]);
            }
        }

        url = URLString.createSessionURL(hostname, hostport,
                                         sessionType, sessionName);

        try {
            if (!RegistryFactory.registryExists(sessionType)) {
                RegistryFactory.startRegistry(sessionType);
            }

            session = SessionFactory.createSession(this, url, false);
            session.createChannel(this, "MidiChannel", true, true, false);
            System.err.println("Setup and bound Midi server.");
        } catch (JSDTException e) {
            System.err.println("MidiServer: main: exception: " + e);
            e.printStackTrace();
        }
    }


    public Object
    authenticate(AuthenticationInfo info) {
        if (MidiServer_Debug) {
            System.err.println("MidiServer: authenticate:" +
                                " authentication info: " + info);
        }

        return(null);
    }


    public String
    getName() {
        if (MidiServer_Debug) {
            System.err.println("MidiServer: getName.");
        }

        return("ServerClient");
    }


    private String
    getHost(String args[]) {
        String defHost = "localhost";  /* Default host name for connections. */
        int length = args.length;

        if (MidiServer_Debug) {
            System.err.println("MidiServer: getHost.");
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


    private int
    getPort(String args[]) {
        int defPort = 4470;   /* Default port number for connections. */
        int length = args.length;

        if (MidiServer_Debug) {
            System.err.println("MidiServer: getPort.");
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


    private String
    getType(String args[]) {
        String defType = "socket";   /* Default Session type. */
        int length = args.length;

        if (MidiServer_Debug) {
            System.err.println("MidiServer: getType.");
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


    public static void
    main(String args[]) {
        if (MidiServer_Debug) {
            System.err.println("MidiServer: main:");
            for (int i = 0; i < args.length ; i++) {
                System.err.println("args[" + i + "]: " + args[i]);
            }
        }

        new MidiServer(args);
    }
}
