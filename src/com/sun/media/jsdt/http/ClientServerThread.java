
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

package com.sun.media.jsdt.http;

import com.sun.media.jsdt.Client;
import com.sun.media.jsdt.impl.*;
import java.io.IOException;
import java.net.*;

/**
 * JSDT Client Server Thread class.
 *
 * @version     2.3 - 28th October 2017
 * @author      Rich Burridge
 */

final class
ClientServerThread extends HttpThread {

    /** The Server-side client associated with this thread. */
    private Client client;


/**
 * <A NAME="SD_CLIENTSERVERTHREAD"></A>
 * <EM>ClientServerThread</EM>
 *
 * @param socket
 * @param client
 */

    public
    ClientServerThread(Socket socket, Client client)
                throws SocketException, UnknownHostException {
        super(socket);

        if (ClientServerThread_Debug) {
            debug("ClientServerThread: constructor:" +
                  " socket: " + socket +
                  " client: " + client);
        }

        this.client = client;
        needConnection = false;
    }


/**
 * <A NAME="SD_HANDLEMESSAGE"></A>
 * <EM>handleMessage</EM>
 *
 * @param message
 */

    public void
    handleMessage(Message message) {
        ClientServerMessage csm = new ClientServerMessage(client);

        if (ClientServerThread_Debug) {
            debug("ClientServerThread: handleMessage:" +
                  " message: " + message);
        }

        csm.handleMessage(message);
    }


/**
 * <A NAME="SD_RUN"></A>
 * <EM>run</EM>
 */

    public void
    run() {
        if (ClientServerThread_Debug) {
            debug("ClientServerThread: run.");
        }

        try {
            if (getSocketMessage()) {
                handleMessage(message);
            }
        } catch (IOException ioe) {
            cleanupConnection();
        } catch (Exception e) {
            error("ClientServerThread: run: ", e);
        }
    }
}
