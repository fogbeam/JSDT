
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

import com.sun.media.jsdt.impl.*;
import java.io.*;
import java.net.*;

/**
 * JSDT Session proxy permanent thread class (HTTP implementation).
 *
 * @version     2.3 - 28th October 2017
 * @author      Rich Burridge
 */

final class
SessionPermThread extends HttpThread {

    /** The Session proxy associated with this permanent proxy thread. */
    private SessionProxy sp;

    /** The Session proxy message associated with this permanent thread. */
    private SessionProxyMessage spm = null;


/**
 * <A NAME="SD_SESSIONPERMTHREAD"></A>
 * <EM>SessionPermThread</EM>
 *
 * @param session
 * @param sessionProxy
 * @param host
 * @param port
 * @param id
 */

    public
    SessionPermThread(SessionImpl session, SessionProxy sessionProxy,
                      String host, int port, int id)
                throws SocketException, UnknownHostException {
        super(host, port, true);

        if (SessionPermThread_Debug) {
            debug("SessionPermThread: constructor:" +
                  " session: "       + session +
                  " session proxy: " + sessionProxy +
                  " host: "          + host +
                  " port: "          + port +
                  " id: "            + id);
        }

        sp  = sessionProxy;
        spm = new SessionProxyMessage(session, sessionProxy);
        sendPermMessage(session, this, id);
    }


/**
 * <A NAME="SD_GETSOCKETMESSAGE"></A>
 * <EM>getSocketMessage</EM> gets the next message off the connection.
 *
 * @return true if there is a valid message to be processed.
 */

    public synchronized boolean
    getSocketMessage() throws IOException {
        if (SessionPermThread_Debug) {
            debug("SessionPermThread: getSocketMessage.");
        }

        if (in == null) {
            try {
                in      = socket.getInputStream();
                dataIn  = new DataInputStream(new BufferedInputStream(in));
            } catch (IOException ioe) {
                error("SessionPermThread: getSocketMessage: ", ioe);
            }
        }

        message.getMessageHeader(this);
        return(message.validMessageHeader());
    }


/**
 * <A NAME="SD_HANDLEMESSAGE"></A>
 * <EM>handleMessage</EM> process the next message.
 *
 * @param message the next message to be processed.
 */

    public void
    handleMessage(Message message) {
        if (SessionPermThread_Debug) {
            debug("SessionPermThread: handleMessage:" +
                  " message: " + message);
        }

        spm.handleMessage(message);
    }


/**
 * <A NAME="SD_RUN"></A>
 * <EM>run</EM>
 */

    public void
    run() {
        if (SessionPermThread_Debug) {
            debug("SessionPermThread: run.");
        }

        try {
            while (true) {
                if (getSocketMessage()) {
                    synchronized (this) {
                        while (message.type == ClientImpl.M_Client &&
                               message.action == T_Challenge       &&
                               sp.proxyThread.getAuthenticateWaitStatus()) {
                                try {
                                    Thread.sleep(500);
                                } catch (InterruptedException ie) {
                                }
                        }
                    }

                    handleMessage(message);
                }
            }
        } catch (IOException ioe) {
            cleanupConnection();
        } catch (Exception e) {
            error("SessionPermThread: run: ", e);
        }
    }
}
