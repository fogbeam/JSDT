
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

import com.sun.media.jsdt.JSDTException;
import com.sun.media.jsdt.impl.*;
import java.io.*;
import java.util.*;

/**
 * JSDT Same VM Session Server Thread class.
 *
 * @version     2.3 - 7th November 2017
 * @author      Rich Burridge
 */

final class
SameVMSessionServerThread extends SameVMThread {

    // The Server-side sessions (and their names) for this connection.
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
        needConnection = false;
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

                ss.removeId(this.getId());
            }
        }
    }


/**
 * <A NAME="SD_SENDASYNCHMESSAGE"></A>
 * <EM>sendAsynchMessage</EM>
 *
 * @param sessionNo
 * @param id
 * @param byteArray the outgoing data message.
 */

    protected synchronized void
    sendAsynchMessage(short sessionNo, int id, byte[] byteArray) {
        SessionImpl   session = (SessionImpl)
                                 sessions.get(message.sessionNo);
        SessionServer ss      = (SessionServer) session.so.getServer();
        HttpThread    thread  = ss.permThreads.get(id);

        if (SameVMSessionServerThread_Debug) {
            debug("SameVMSessionServerThread: sendAsynchMessage:" +
                  " session #: "  + sessionNo +
                  " id #: "       + id +
                  " byte array: " + byteArray);
        }

        try {
            if (thread == null) {    /* No permanent connection. */
                putBufferedMessage(sessionNo, id, byteArray);
            } else if (thread instanceof SameVMThread) {
                ((SameVMThread) thread).putMessage(byteArray);
            } else {
                OutputStream os = thread.socket.getOutputStream();

                os.write(byteArray, 0, byteArray.length);
                if (thread.socket instanceof HttpReceiveSocket) {
                    thread.socket.getOutputStream().close();
                } else {
                    os.flush();
                }
            }
        } catch (IOException ioe) {
            error("SameVMSessionServerThread: sendAsynchMessage: ", ioe);
        }
    }


/**
 * <A NAME="SD_PUTBUFFEREDMESSAGE"></A>
 * <EM>putBufferedMessage</EM> put a message in the vector of messages to
 * be sent, when the proxy-side pings for them.
 *
 * @param byteArray the outgoing data message.
 */

    protected synchronized void
    putBufferedMessage(short sessionNo, int id, byte[] byteArray) {
        SessionImpl   session = (SessionImpl) sessions.get(message.sessionNo);
        SessionServer ss      = (SessionServer) session.so.getServer();

        if (SameVMSessionServerThread_Debug) {
            debug("SameVMSessionServerThread: putBufferedMessage:" +
                  " session #: "  + sessionNo +
                  " id #: "       + id +
                  " byte array: " + byteArray);
        }

        ss.putBufferedMessage(id, byteArray);
    }


    protected final void
    setReplyThread(short sessionNo, int id, SameVMThread replyThread) {
        SessionImpl   session = (SessionImpl) sessions.get(sessionNo);
        SessionServer ss      = (SessionServer) session.so.getServer();

        if (SameVMSessionServerThread_Debug) {
            debug("SameVMSessionServerThread: setReplyThread:" +
                  " session #: "    + sessionNo +
                  " id: "           + id +
                  " reply thread: " + replyThread);
        }

        super.setReplyThread(sessionNo, id, replyThread);
        ss.addPermanentThread(id, replyThread);
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


/**
 * <A NAME="SD_RUN"></A>
 * <EM>run</EM>
 */

    public void
    run() {
        if (SameVMSessionServerThread_Debug) {
            debug("SameVMSessionServerThread: run.");
        }

        try {
            while (running) {
                getSocketMessage();
                handleMessage(message);
            }
        } catch (Exception e) {
            error("SameVMSessionServerThread: run: ", e);
        }
    }
}
