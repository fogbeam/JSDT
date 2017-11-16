
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

import com.sun.media.jsdt.event.RegistryListener;
import com.sun.media.jsdt.impl.*;
import java.net.*;

/**
 * JSDT Naming proxy thread class (http implementation).
 *
 * @version     2.3 - 28th October 2017
 * @author      Rich Burridge
 */

final class
NamingProxyThread extends HttpThread {

    /** The Naming proxy message associated with this proxy thread. */
    private NamingProxyMessage npm = null;

    /** The thread that will be used to ping the Registry for any
     *  asynchronous messages.
     */ 
    private RegistryPingThread registryPingThread = null;

    /** The actual thread. */
    private Thread thread = null;


/**
 * <A NAME="SD_NAMINGPROXYTHREAD"></A>
 * <EM>NamingProxyThread</EM>
 *
 * @param namingProxy
 * @param host
 * @param port
 */

    public
    NamingProxyThread(NamingProxy namingProxy, String host, int port)
                throws SocketException, UnknownHostException {
        super(host, port, false);

        if (NamingProxyThread_Debug) {
            debug("NamingProxyThread: constructor:" +
                  " naming proxy: " + namingProxy +
                  " host: "         + host +
                  " port: "         + port);
        }

        npm = new NamingProxyMessage(namingProxy);
    }


/**
 * <A NAME="SD_HANDLEMESSAGE"></A>
 * <EM>handleMessage</EM> process the next message.
 *
 * @param message the next message to be processed.
 */

    public void
    handleMessage(Message message) {
        if (NamingProxyThread_Debug) {
            debug("NamingProxyThread: handleMessage:" +
                  " message: " + message);
        }

        npm.handleMessage(message);
    }


    protected void
    startRegistryPingThread(RegistryListener listener) {
        if (NamingProxyThread_Debug) {
            debug("NamingProxyThread: startRegistryPingThread:" +
                  " listener: " + listener);
        }

        if (registryPingThread == null) {
            try {
                registryPingThread = new RegistryPingThread(getAddress(),
                                                            getPort(), this);
            } catch (UnknownHostException uhe) {
            }

            thread = Util.startThread(registryPingThread,
                                      "RegistryPingThread: " + address, true);
        }

        registryPingThread.addEntry(listener);
    }


    void
    stopRegistryPingThread(RegistryListener listener) {
        if (NamingProxyThread_Debug) {
            debug("NamingProxyThread: stopRegistryPingThread:" +
                  " listener: " + listener);
        }

        registryPingThread.removeEntry(listener);
        if (!registryPingThread.hasListeners()) {
            thread.stop();
            registryPingThread = null;
        }
    }
}
