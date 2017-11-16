
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
import com.sun.media.jsdt.impl.*;
import java.io.IOException;
import java.util.*;

/**
 * JSDT Same VM Session Server Thread class.
 *
 * @version     2.3 - 6th November 2017
 * @author      Rich Burridge
 */

final class
SameVMSessionServerThread extends SameVMThread {

    // The Server-side sessions (and their names) for this socket.
    private Hashtable sessions = null;

    // The Session server message for this same VM server thread.
    private SessionServerMessage ssm = null;

/**
 * <A NAME="SD_SAMEVMSESSIONSERVERTHREAD"></A>
 * <EM>SameVMSessionServerThread</EM>
 *
 * @param host
 * @param port
 * @param sessions
 */

    public
    SameVMSessionServerThread(String host, int port, Hashtable sessions) {
        super(host, port);

        if (SameVMSessionServerThread_Debug) {
            debug("SameVMSessionServerThread: constructor:" +
                  " host: "     + host +
                  " port: "     + port +
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
        if (SameVMSessionServerThread_Debug) {
            debug("SameVMSessionServerThread: cleanupConnection.");
        }

        synchronized (sessions) {
            for (Enumeration e = sessions.elements(); e.hasMoreElements();) {
                SessionImpl   session = (SessionImpl) e.nextElement();
                SessionServer ss      = (SessionServer) session.so.getServer();

                ss.removeThread(this);
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
        SessionImpl session = (SessionImpl) sessions.get(message.sessionNo);

        if (SameVMSessionServerThread_Debug) {
            debug("SameVMSessionServerThread: handleMessage:" +
                  " message: " + message);
        }

        if (session == null) {
            try {
                message.thread.writeMessageHeader(message.thread.dataOut,
                                   message.sessionNo, message.id,
                                   message.type, message.action, false, true);
                message.thread.dataOut.writeInt(JSDTException.NO_SUCH_SESSION);
                message.thread.flush();
                message.thread.finishMessage();
            } catch (IOException ioe) {
                error("SameVMSessionServerThread: handleMessage: ", ioe);
            }
        } else {
            ssm.setSession(session);
            ssm.handleMessage(message);
        }
    }
}
