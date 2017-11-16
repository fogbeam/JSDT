
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

import com.sun.media.jsdt.*;
import com.sun.media.jsdt.impl.*;
import java.io.IOException;
import java.net.*;
import java.util.*;

/**
 * JSDT TCP Socket Server class.
 *
 * @version     2.3 - 6th November 2017
 * @author      Rich Burridge
 */

final class
TCPSocketServer extends JSDTObject implements Runnable, socketDebugFlags {

    // The TCP socket servers keyed by port number.
    static Hashtable<Integer, TCPSocketServer> socketServers = null;

    // The server-side sessions for this port keyed by session name.
    private Hashtable<Short, Session> sessions = null;

    // Hashtable of ServerThreads associated with this socket server.
    Hashtable<JSDTThread, Thread> serverThreads = null;

    // The server-side client for this port, or null for a session server.
    private Client client = null;

    // The server socket associated with this thread.
    private ServerSocket ssock = null;

    // The port number used by this socket server.
    protected int port;

    // This TCP socket server thread.
    private Thread thread = null;

    // A boolean to signal if we should shutdown the thread.
    private boolean shutdown = false;


/**
 * <A NAME="SD_TCPSOCKETSERVER"></A>
 * <EM>TCPSocketServer</EM>
 *
 * @param port
 *
 * @exception PortInUseException if this port is being used by another
 * application.
 */

    public
    TCPSocketServer(int port) throws PortInUseException {
        String factoryClass = Util.getStringProperty("socketFactoryClass",
                                                     socketFactoryClass);

        if (TCPSocketServer_Debug) {
            debug("TCPSocketServer: constructor:" +
                  " port " + port);
        }

        try {
            JSDTSocketFactory factory = (JSDTSocketFactory)
                  Util.getClassForName(factoryClass).newInstance();

            ssock = factory.createServerSocket(port);
        } catch (BindException be) {
            throw new PortInUseException();
        } catch (Exception e) {
            error("TCPSocketServer: constructor: ", e);
        }

        this.port     = port;
        sessions      = new Hashtable<>();
        serverThreads = new Hashtable<>();
    }


    void
    addSession(SessionImpl session, short sessionNo) {
        Short key = sessionNo;

        if (TCPSocketServer_Debug) {
            debug("TCPSocketServer: addSession:" +
                  " session: "    + session +
                  " session no: " + sessionNo);
        }

        if (sessions.get(key) == null) {
            sessions.put(key, session);
        }
    }


    void
    addClient(Client client) {
        if (TCPSocketServer_Debug) {
            debug("TCPSocketServer: addClient:" +
                  " client: " + client);
        }

        this.client = client;
    }


    void
    setThread(Thread thread) {
        if (TCPSocketServer_Debug) {
            debug("TCPSocketServer: setThread:" +
                  " thread: " + thread);
        }

        this.thread = thread;
    }


    void
    close() {
        if (TCPSocketServer_Debug) {
            debug("TCPSocketServer: close.");
        }

        try {
            ssock.close();
        } catch (IOException ioe) {
            error("TCPSocketServer: close: ", ioe);
        }
    }


    SameVMSessionServerThread
    createSameVMSessionServerThread(String host, int port) {
        SameVMSessionServerThread sameVMServerThread =
                new SameVMSessionServerThread(host, port, sessions);

        if (TCPSocketServer_Debug) {
            debug("TCPSocketServer: createSameVMSessionServerThread:" +
                  " host: " + host +
                  " port: " + port);
        }

        Util.startThread(sameVMServerThread,
                         "SameVMSessionServerThread:" + port, true);
        return(sameVMServerThread);
    }


/**
 * <A NAME="SD_GETPORT"></A>
 * <EM>getPort</EM> return the port number being used by this socket server.
 *
 * @return server socket port number
 */

    int
    getPort() {
        if (TCPSocketServer_Debug) {
            debug("TCPSocketServer: getPort.");
        }

        return(port);
    }


/**
 * <A NAME="SD_REMOVESESSION"></A>
 * <EM>removeSession</EM> remove the Session associated with this session
 * number from the hashtable of Sessions known to the server.
 *
 * @param sessionNo the session number of the session to remove.
 */

    void
    removeSession(short sessionNo) {
        if (TCPSocketServer_Debug) {
            debug("TCPSocketServer: removeSession:" +
                  " session no: " + sessionNo);
        }

        sessions.remove(sessionNo);

        if (sessions.size() == 0) {
            Enumeration e, k;

            for (e = serverThreads.elements(), k = serverThreads.keys();
                                               e.hasMoreElements();) {
                SocketThread socketThread = (SocketThread) k.nextElement();
                Thread       nextThread   = (Thread) e.nextElement();

                if (nextThread != null) {
                    socketThread.terminate();
                    nextThread.interrupt();
                    ((TCPSocketThread) socketThread).closeSocket();
                }
            }

            serverThreads.clear();
            socketServers.remove(port);

            if (thread != null) {
                setShutdown(true);
            }

            close();
        }
    }


/**
 * <A NAME="SD_RUN"></A>
 * <EM>run</EM>
 */

    public void
    run() {
        if (TCPSocketServer_Debug) {
            debug("TCPSocketServer: run.");
        }

        try {
            JSDTSecurity.enablePrivilege.invoke(JSDTSecurity.privilegeManager,
                                                JSDTSecurity.connectArgs);
        } catch (Exception e) {
        }

        try {
            while (!shutdown) {
                Socket       sock;
                SocketThread socketThread;
                String       threadName;
                Thread       thread;

                if (TCPSocketServer_Debug) {
                    debug("TCPSocketServer: run:" +
                          " Waiting...");
                }
                sock = ssock.accept();
                if (TCPSocketServer_Debug) {
                    debug("TCPSocketServer: run:" +
                          " Got a connection.");
                }

                if (client == null) {
                    socketThread = new SessionServerThread(sock, sessions);
                    threadName = "SessionServerThread:" + sock;
                } else {
                    socketThread = new ClientServerThread(sock, client);
                    threadName = "ClientServerThread:" + client.getName();
                }

                thread = Util.startThread(socketThread, threadName, false);
                if (client == null) {
                    serverThreads.put(socketThread, thread);
                }
            }
        } catch (SocketException se) {

/* It's possible that the server socket could be closed if this client is
 * destroyed. We just quietly catch the exception that's thrown in this case.
 */

        } catch (Exception e) {
            error("TCPSocketServer: run: ", e);
        }
    }

    /**
     * Sets if we should shutdown the thread. <p>
     */
    private void setShutdown(boolean shutdown) {
        this.shutdown = shutdown;
    }
}
