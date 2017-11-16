
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

import com.sun.media.jsdt.JSDTException;
import com.sun.media.jsdt.Session;
import com.sun.media.jsdt.URLString;
import com.sun.media.jsdt.impl.*;
import java.io.*;
import java.net.*;
import java.util.*;

/**
 * JSDT Session Server Thread class.
 *
 * @version     2.3 - 6th November 2017
 * @author      Rich Burridge
 */

final class
SessionServerThread extends TCPSocketThread {

    // The Server-side sessions (and their names) for this socket.
    private Hashtable<Short, Session> sessions = null;

    // The Session server message associated with this server thread.
    private SessionServerMessage ssm = null;


/**
 * <A NAME="SD_SESSIONSERVERTHREAD"></A>
 * <EM>SessionServerThread</EM>
 *
 * @param socket
 * @param sessions
 */

    public
    SessionServerThread(Socket socket, Hashtable<Short, Session> sessions)
                throws SocketException, UnknownHostException {
        super(socket);

        if (SessionServerThread_Debug) {
            debug("SessionServerThread: constructor:" +
                  " socket: "   + socket +
                  " sessions: " + sessions);
        }

        this.sessions = sessions;
        ssm = new SessionServerMessage();
    }


/**
 * <A NAME="SD_CLEANUPCONNECTION"></A>
 * <EM>cleanupConnection</EM>
 */

    public void
    cleanupConnection() {
        if (SessionServerThread_Debug) {
            debug("SessionServerThread: cleanupConnection.");
        }

        super.cleanupConnection();
        synchronized (sessions) {
            for (Enumeration e = sessions.elements(); e.hasMoreElements();) {
                SessionImpl   session = (SessionImpl) e.nextElement();
                SessionServer ss      = (SessionServer) session.so.getServer();

                ss.removeThread(this);
            }
        }
    }


/**
 * <A NAME="SD_SETUPSESSIONSERVER"></A>
 * <EM>setupSessionServer</EM>
 *
 * @param message
 *
 * @return the exception value to return to the proxy (or 0 if successful).
 */

    private int
    setupSessionServer(Message message) {
        socketSession   session;
        URLString       url;
        String          urlString;
        DataInputStream in        = message.thread.dataIn;
        int             retval    = 0;
        String          className = "com.sun.media.jsdt.socket.socketSession";

        if (SessionServerThread_Debug) {
            debug("SessionServerThread: setupSessionServer:" +
                  " message: " + message);
        }

        try {
            session = (socketSession)
                                Util.getClassForName(className).newInstance();

            urlString = in.readUTF();
            url = new URLString(urlString);
            session._createServer(url.getObjectName(), message.sessionNo,
                                  url.getConnectionType(), urlString,
                                  url.getPort());
            sessions.put(message.sessionNo, session);
        } catch (Exception e) {
            retval = JSDTException.NO_SUCH_SESSION;
        }

        return(retval);
    }


/**
 * <A NAME="SD_HANDLEMESSAGE"></A>
 * <EM>handleMessage</EM>
 *
 * @param message
 */

    public void
    handleMessage(Message message) {
        SessionImpl session = (SessionImpl) sessions.get(message.sessionNo);

        if (SessionServerThread_Debug) {
            debug("SessionServerThread: handleMessage:" +
                  " message: " + message);
        }

        if (session == null) {
            int retval;

            if (message.type == SessionImpl.M_Session &&
                message.action == T_CreateSession) {
                retval = setupSessionServer(message);
            } else {
                retval = JSDTException.NO_SUCH_SESSION;
            }

            try {
                message.thread.writeMessageHeader(message.thread.dataOut,
                                  message.sessionNo, message.id,
                                  message.type, message.action, false, true);
                message.thread.dataOut.writeInt(retval);
                message.thread.flush();
                message.thread.finishMessage();
            } catch (IOException ioe) {
                error("SessionServerThread: handleMessage: ", ioe);
            }
        } else {
            ssm.setSession(session);
            ssm.handleMessage(message);
        }
    }
}
