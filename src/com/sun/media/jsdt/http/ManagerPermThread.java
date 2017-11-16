
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

import com.sun.media.jsdt.*;
import com.sun.media.jsdt.impl.Message;
import com.sun.media.jsdt.impl.SessionImpl;
import java.io.*;
import java.net.*;

/**
 * JSDT Manager proxy permanent thread class (HTTP implementation).
 *
 * @version     2.3 - 28th October 2017
 * @author      Rich Burridge
 */

final class
ManagerPermThread extends HttpThread {

    /** The Manager proxy message associated with this permanent thread. */
    private ManagerProxyMessage mpm = null;


/**
 * <A NAME="SD_MANAGERPERMTHREAD"></A>
 * <EM>ManagerPermThread</EM>
 *
 * @param session
 * @param manageable
 * @param manager
 * @param host
 * @param port
 * @param id
 */

    public
    ManagerPermThread(SessionImpl session, Manageable manageable,
                       JSDTManager manager, String host, int port, int id)
                throws SocketException, UnknownHostException {
        super(host, port, true);

        if (ManagerPermThread_Debug) {
            debug("ManagerPermThread: constructor:" +
                  " manageable: " + manageable +
                  " manager: "    + manager +
                  " host: "       + host +
                  " port: "       + port +
                  " id: "         + id);
        }

        mpm = new ManagerProxyMessage(session, manager, manageable);
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
        if (ManagerPermThread_Debug) {
            debug("ManagerPermThread: getSocketMessage.");
        }

        if (in == null) {
            try {
                in      = socket.getInputStream();
                dataIn  = new DataInputStream(new BufferedInputStream(in));
            } catch (IOException ioe) {
                error("ManagerPermThread: getSocketMessage: ", ioe);
            }
        }

        message.getMessageHeader(this);
        return(message.validMessageHeader());
    }


/**
 * <A NAME="SD_HANDLEMESSAGE"></A>
 * <EM>handleMessage</EM>
 *
 * @param message
 */

    public void
    handleMessage(Message message) {
        if (ManagerPermThread_Debug) {
            debug("ManagerPermThread: handleMessage:" +
                  " message: " + message);
        }

        mpm.handleMessage(message);
    }


/**
 * <A NAME="SD_RUN"></A>
 * <EM>run</EM>
 */

    public void
    run() {
        if (ManagerPermThread_Debug) {
            debug("ManagerPermThread: run.");
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
            error("ManagerPermThread: run: ", e);
        }
    }
}
