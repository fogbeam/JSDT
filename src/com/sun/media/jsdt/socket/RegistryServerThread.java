
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

package com.sun.media.jsdt.socket;

import com.sun.media.jsdt.impl.Message;
import java.net.*;
import java.io.IOException;
import java.util.*;

/**
 * JSDT Registry Server Thread class.
 *
 * @version     2.3 - 6th November 2017
 * @author      Rich Burridge
 */

final class
RegistryServerThread extends TCPSocketThread {

    // Handle to server-side registry object.
    private Registry registry;

    // A boolean to signal if we should shutdown the thread.
    private boolean shutdown = false;


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
    }


/**
 * <A NAME="SD_CLEANUPCONNECTION"></A>
 * <EM>cleanupConnection</EM>
 */

    public void
    cleanupConnection() {
        Hashtable bindings = registry.getBindings();

        if (RegistryServerThread_Debug) {
            debug("RegistryServerThread: cleanupConnection.");
        }

        super.cleanupConnection();
        synchronized (bindings) {
            Enumeration e, k;

            for (e = bindings.elements(), k = bindings.keys();
                 e.hasMoreElements();) {
                SocketThread thread = (SocketThread) e.nextElement();
                String       name   = (String)       k.nextElement();

                if (thread == this) {
                    if (RegistryServerThread_Debug) {
                        debug("RegistryServerThread: cleanupConnection:" +
                              " Removing binding for: " + name);
                    }

                    bindings.remove(name);
                }
            }
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
            message = new Message();
            while (!shutdown) {
                if (getSocketMessage()) {
                    handleMessage(message);
                } else {
                    break;
                }
            }
        } catch (IOException ioe) {
            cleanupConnection();
        } catch (Exception e) {
            error("RegistryServerThread: run: ", e);
        }
    }


    /**
     * Sets if we should shutdown the thread. <p>
     */

    void setShutdown(boolean shutdown) {
        this.shutdown = shutdown;
    }
}
