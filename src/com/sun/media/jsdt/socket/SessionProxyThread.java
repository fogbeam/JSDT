
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

import com.sun.media.jsdt.event.ConnectionEvent;
import com.sun.media.jsdt.impl.Message;
import com.sun.media.jsdt.impl.SessionImpl;
import java.net.*;

/**
 * JSDT Session proxy thread class (socket implementation).
 *
 * @version     2.3 - 6th November 2017
 * @author      Rich Burridge
 */

final class
SessionProxyThread extends TCPSocketThread {

    // The session associated with this session proxy thread.
    private SessionImpl session;

    // The session proxy associated with this session proxy thread.
    private SessionProxy sessionProxy;

    // The Session proxy message associated with this proxy thread.
    private SessionProxyMessage spm = null;


/**
 * <A NAME="SD_SESSIONPROXYTHREAD"></A>
 * <EM>SessionProxyThread</EM>
 *
 * @param session
 * @param sessionProxy
 * @param host
 * @param port
 */

    public
    SessionProxyThread(SessionImpl session, SessionProxy sessionProxy,
                       String host, int port)
        throws SocketException, UnknownHostException {
        super(host, port);

        if (SessionProxyThread_Debug) {
            debug("SessionProxyThread: constructor:" +
                  " session: "       + session +
                  " session proxy: " + sessionProxy +
                  " host: "          + host +
                  " port: "          + port);
        }

        this.session      = session;
        this.sessionProxy = sessionProxy;

        spm = new SessionProxyMessage(session, sessionProxy);
    }


/**
 * <A NAME="SD_CLEANUPCONNECTION"></A>
 * <EM>cleanupConnection</EM>
 */

    public void
    cleanupConnection() {
        NamingProxy namingProxy = ((socketSession) session).namingProxy;

        if (SessionProxyThread_Debug) {
            debug("SessionProxyThread: cleanupConnection.");
        }

        super.cleanupConnection();

        namingProxy.connectionThread.informListeners(getAddress(), getPort(),
                                       ConnectionEvent.CONNECTION_FAILED);

        sessionProxy.removeSession(session.getName());
    }


/**
 * <A NAME="SD_HANDLEMESSAGE"></A>
 * <EM>handleMessage</EM> process the next message.
 *
 * @param message the next message to be processed.
 */

    public void
    handleMessage(Message message) {
        if (SessionProxyThread_Debug) {
            debug("SessionProxyThread: handleMessage:" +
                  " message: " + message);
        }

        spm.handleMessage(message);
    }
}
