
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

import com.sun.media.jsdt.impl.Message;
import java.net.*;
import java.io.IOException;

/**
 * JSDT Registry Server Thread class.
 *
 * @version     2.3 - 7th November 2017
 * @author      Rich Burridge
 */

final class
RegistryServerThread extends HttpThread {

    /** Handle to server-side registry object. */
    private Registry registry;


/**
 * <A NAME="SD_REGISTRYSERVERTHREAD"></A>
 * <EM>RegistryServerThread</EM>
 *
 * @param socket
 * @param registry
 */

    public
    RegistryServerThread(Socket socket, Registry registry)
                throws SocketException, UnknownHostException {
        super(socket);

        if (RegistryServerThread_Debug) {
            debug("RegistryServerThread: constructor:" +
                  " socket: "   + socket +
                  " registry: " + registry);
        }

        this.registry = registry;
        needConnection = false;
    }


/**
 * <A NAME="SD_SENDASYNCHMESSAGE"></A>
 * <EM>sendAsynchMessage</EM>
 *
 * @param id
 * @param byteArray the outgoing data message.
 */

    protected synchronized void
    sendAsynchMessage(int id, byte[] byteArray) {
        if (RegistryServerThread_Debug) {
            debug("RegistryServerThread: sendAsynchMessage:" +
                  " id #: "       + id +
                  " byte array: " + byteArray);
        }

        registry.sendAsynchMessage(id, byteArray);
    }


/**
 * <A NAME="SD_FLUSH"></A>
 * <EM>flush</EM> flush the message written to this connection.
 *
 * @exception IOException if an IO exception has occured.
 */

    public synchronized void
    flush() throws IOException {
        if (RegistryServerThread_Debug) {
            debug("RegistryServerThread: flush.");
        }

        if (sendNow) {
            super.flush();
        } else {
            byte[] bytes = byteOut.toByteArray();

            sendAsynchMessage(currentId, bytes);
            byteOut = null;
        }
    }


/**
 * <A NAME="SD_HANDLEMESSAGE"></A>
 * <EM>handleMessage</EM>
 *
 * @param message
 */

    public void
    handleMessage(Message message) {
        RegistryServerMessage rsm = new RegistryServerMessage(registry, this);

        if (RegistryServerThread_Debug) {
            debug("RegistryServerThread: handleMessage:" +
                  " message: " + message);
        }

        rsm.handleMessage(message);
    }


/**
 * <A NAME="SD_RUN"></A>
 * <EM>run</EM>
 */

    public void
    run() {
        if (RegistryServerThread_Debug) {
            debug("RegistryServerThread: run.");
        }

        try {
            if (getSocketMessage()) {
                handleMessage(message);
            }
        } catch (IOException ioe) {
            cleanupConnection();
        } catch (Exception e) {
            error("RegistryServerThread: run: ", e);
        }
    }
}
