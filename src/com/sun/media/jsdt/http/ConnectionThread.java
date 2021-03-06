
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
import com.sun.media.jsdt.impl.*;
import com.sun.media.jsdt.event.ConnectionEvent;
import com.sun.media.jsdt.event.ConnectionListener;
import java.io.*;
import java.util.*;

/**
 * JSDT Check connections thread class.
 *
 * @version     2.3 - 19th December 2017
 * @author      Rich Burridge
 */

final class
ConnectionThread extends JSDTObject implements Runnable, httpDebugFlags {

    // The sessions currently bound.
    private final Hashtable sessions;

    // The listeners (and event masks), observing connection failures.
    private final Hashtable<ConnectionListener, Integer>
                      listeners = new Hashtable<>();

    // The thread running this ConnectionThread object.
    private final Thread thread;

    // The time of the last successful reply to a ping message.
    private long lastPingTime = System.currentTimeMillis();


/**
 * <A NAME="SD_CONNECTIONTHREAD"></A>
 * <EM>ConnectionThread</EM> is a constructor for the ConnectionThread class.
 * This thread will continually check all this applications connections to
 * its server(s), and notify any listeners if there is a connection failure.
 *
 * @param sessions the sessions currently bound.
 */

    public
    ConnectionThread(Hashtable sessions) {
        if (ConnectionThread_Debug) {
            debug("ConnectionThread: constructor:" +
                  " sessions: " + sessions);
        }

        this.sessions = sessions;
        thread        = Util.startThread(this, "ConnectionThread", true);

    }


/**
 * <A NAME="SD_ADDCONNECTIONLISTENER"></A>
 * add the specified connection listener to provide asynchronous notification
 * of a connection failure. Without such a connection listener, the application
 * would only be able to determine failure when it next attempted to perform
 * an operation that required the use of its underlying connection(s), which
 * would throw a ConnectionException.
 *
 * <P>A single connection listener can handle multiple underlying connections.
 * Multiple connection listeners can be added by an application.
 *
 * @param listener the connection listener to add.
 */

    void
    addConnectionListener(ConnectionListener listener) {
        if (ConnectionThread_Debug) {
            debug("ConnectionThread: addConnectionListener:" +
                  " listener: " + listener);
        }

        sendMessage(T_AddConnection);

/* By default, a listener will listen for every type of event, so set all
 * bits in the event mask. Note that there is currently no API calls to
 * adjust the mask for a Connection listener, but we might as well add in the
 * hooks now.
 */

        synchronized (listeners) {
            listeners.put(listener, 0XFFFFFFFF);
        }

        synchronized (this) {
            notifyAll();
        }
    }


/**
 * <A NAME="SD_REMOVECONNECTIONLISTENER"></A>
 * remove the specified connection listener. This connection listener will
 * no longer be notified of any failures on the underlying connection(s).
 *
 * @param listener the connection listener to remove.
 *
 * @exception NoSuchListenerException if this connection listener doesn't
 * exist.
 */

    void
    removeConnectionListener(ConnectionListener listener)
                throws NoSuchListenerException {
        if (ConnectionThread_Debug) {
            debug("ConnectionThread: removeConnectionListener:" +
                  " listener: " + listener);
        }

        sendMessage(T_RemoveConnection);

        synchronized (listeners) {
            if (listener == null || listeners.remove(listener) == null) {
                throw new NoSuchListenerException();
            }
        }

        synchronized (this) {
            notifyAll();
        }
    }


/**
 * <A NAME="SD_SENDMESSAGE"></A>
 * send a message to all the known Sessions.
 *
 * @param action the message action.
 */

    private void
    sendMessage(char action) {
        httpSession     session;
        SessionProxy    sp;
        String          host;
        int             port;
        HttpThread      t;
        short           sessionNo;
        int             id;
        Message         message;
        DataInputStream in;

        if (ConnectionThread_Debug) {
            debug("ConnectionThread: sendMessage:" +
                  " action: " + action);
        }

        synchronized (sessions) {
            long period = Util.getLongProperty("cleanupPeriod", cleanupPeriod);
            long currentTime;

            for (Enumeration e = sessions.elements(); e.hasMoreElements();) {
                session   = (httpSession) e.nextElement();
                sp        = (SessionProxy) session.po;
                host      = sp.proxyThread.getAddress();
                port      = sp.proxyThread.getPort();
                t         = sp.proxyThread;
                sessionNo = sp.getSessionNo();
                id        = t.getId();

                try {
                    t.writeMessageHeader(t.dataOut, sessionNo, id,
                                    SessionImpl.M_Session, action, true, true);
                    t.flush();
                    message = t.waitForReply();
                    in      = message.thread.dataIn;
                    in.readInt();             // return value.
                    t.finishReply();
                    lastPingTime = System.currentTimeMillis();
                } catch (IOException ioe) {
                    t.finishReply();
                    informListeners(host, port,
                                    ConnectionEvent.CONNECTION_FAILED);
                } catch (TimedOutException toe) {
                    t.finishReply();
                    currentTime = System.currentTimeMillis();
                    if (currentTime - lastPingTime > period) {
                        informListeners(host, port,
                                        ConnectionEvent.CONNECTION_FAILED);
                        lastPingTime = System.currentTimeMillis();
                    }
                }
            }
        }
    }


/**
 * <A NAME="SD_STOP"></A>
 * <EM>stop</EM> will terminate this thread.
 */

    void
    stop() {
        if (ConnectionThread_Debug) {
            debug("ConnectionThread: stop.");
        }

        thread.stop();
    }


/**
 * <A NAME="SD_INFORMLISTENERS"></A>
 * <EM>informListeners</EM>
 *
 * @param host
 * @param port
 * @param type
 */

    private synchronized void
    informListeners(String host, int port, int type) {
        if (ConnectionThread_Debug) {
            debug("ConnectionThread: informListeners:" +
                  " host: " + host +
                  " port: " + port +
                  " type: " + type);
        }

        for (Enumeration e = listeners.elements(), k = listeners.keys();
             e.hasMoreElements();) {
            ConnectionListener listener = (ConnectionListener) k.nextElement();
            int                mask = (Integer) e.nextElement();

            if ((mask & type) != 0) {
                ConnectionEvent event = new ConnectionEvent(host, port, type);

                switch (type) {
                    case ConnectionEvent.CONNECTION_FAILED:
                          ConnectionMessage cm = new ConnectionMessage(listener,
                                                                       event);

                        Util.startThread(cm, "ConnectionMessageThread", true);

/* Remove this listener, so that continual connection failures aren't given. */

                        listeners.remove(listener);
                        break;

                    default: debug("ConnectionThread: informListeners:" +
                                   " unexpected type: " + type);
                }
            }
        }
    }


/**
 * <A NAME="SD_RUN"></A>
 * <EM>run</EM>
 */

    public void
    run() {
        long alivePeriod = Util.getLongProperty("keepAlivePeriod",
                                                keepAlivePeriod);

        if (ConnectionThread_Debug) {
            debug("ConnectionThread: run.");
        }

        while (true) {
            synchronized (this) {
                while (listeners.size() == 0) {
                    try {
                        wait();
                    } catch (InterruptedException ie) {
                    }
                }
            }

            sendMessage(T_IsAlive);

            try {
                Thread.sleep(alivePeriod);
            } catch (InterruptedException ie) {
            }
        }
    }
}
