
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
import java.io.*;
import java.net.*;

/**
 * JSDT Naming proxy permanent thread class (HTTP implementation).
 *
 * @version     2.3 - 16th November 2017
 * @author      Rich Burridge
 */

final class
NamingPermThread extends HttpThread {

    /** The Naming proxy message associated with this permanent thread. */
    private NamingProxyMessage npm = null;


/**
 * <A NAME="SD_NAMINGPERMTHREAD"></A>
 * <EM>NamingPermThread</EM>
 *
 * @param namingProxy
 * @param host
 * @param port
 * @param id
 */

    public
    NamingPermThread(NamingProxy namingProxy, String host, int port, int id)
                throws SocketException, UnknownHostException {
        super(host, port, true);

        if (NamingPermThread_Debug) {
            debug("NamingPermThread: constructor:" +
                  " naming proxy: " + namingProxy +
                  " host: "         + host +
                  " port: "         + port +
                  " id: "           + id);
        }

        npm = new NamingProxyMessage(namingProxy);
        sendRegistryPermMessage(this, id);
    }


/**
 * <A NAME="SD_GETSOCKETMESSAGE"></A>
 * <EM>getSocketMessage</EM> gets the next message off the connection.
 *
 * @return true if there is a valid message to be processed.
 */

    public synchronized boolean
    getSocketMessage() throws IOException {
        if (NamingPermThread_Debug) {
            debug("NamingPermThread: getSocketMessage.");
        }

        if (in == null) {
            try {
                in      = socket.getInputStream();
                dataIn  = new DataInputStream(new BufferedInputStream(in));
            } catch (IOException ioe) {
                error("NamingPermThread: getSocketMessage: ", ioe);
            }
        }

        message.getMessageHeader(this);
        return(message.validMessageHeader());
    }


    private void
    sendRegistryPermMessage(HttpThread thread, int id) {
        if (NamingPermThread_Debug) {
            debug("NamingPermThread: sendRegistryPermMessage:" +
                  " thread: "  + thread +
                  " id: "      + id);
        }

        try {
            thread.writeMessageHeader(thread.dataOut, (short) 1, id,
                                      T_Registry, T_Permanent,
                                      true, true);
            thread.dataOut.flush();
        } catch (IOException ioe) {
            thread.finishMessage();
        } catch (Exception e) {
            error("NamingPermThread: sendRegistryPermMessage: ", e);
        }
    }


/**
 * <A NAME="SD_HANDLEMESSAGE"></A>
 * <EM>handleMessage</EM> process the next message.
 *
 * @param message the next message to be processed.
 */

    public void
    handleMessage(Message message) {
        if (NamingPermThread_Debug) {
            debug("NamingPermThread: handleMessage:" +
                  " message: " + message);
        }

        npm.handleMessage(message);
    }


/**
 * <A NAME="SD_RUN"></A>
 * <EM>run</EM>
 */

    public void
    run() {
        if (NamingPermThread_Debug) {
            debug("NamingPermThread: run.");
        }

        try {
            while (true) {
                if (getSocketMessage()) {
                    handleMessage(message);
                }
            }
        } catch (IOException ioe) {
            cleanupConnection();
        } catch (Exception e) {
            error("NamingPermThread: run: ", e);
        }
    }
}
