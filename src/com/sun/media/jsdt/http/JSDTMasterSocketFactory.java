
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
import java.util.*;

/**
 * JSDTMasterSocketFactory attempts to create a socket connection to the
 * specified host using successively less efficient mechanisms
 * until one succeeds.  If the host is successfully connected to,
 * the factory for the successful mechanism is stored in an internal
 * hash table keyed by the host name, so that future attempts to
 * connect to the same host will automatically use the same
 * mechanism.
 *
 * Based on the sun.rmi.transport.proxy.RMIMasterSocketFactory class.
 *
 * @version     2.3 - 6th November 2017
 * @author      Rich Burridge
 */

public class
JSDTMasterSocketFactory extends JSDTObject
                        implements JSDTSocketFactory, httpDebugFlags {

    // Table of hosts successfully connected to and the factory used.
    private Hashtable<String, JSDTSocketFactory>
                                       successTable = new Hashtable<>();

    // Maximum number of hosts to remember successful connection to.
    private static final int MaxRememberedHosts = 64;

    // List of the hosts in successTable in initial connection order.
    private Vector<String> hostList = new Vector<>(MaxRememberedHosts);

    // Default factory to initally use for direct socket connection.
    private JSDTSocketFactory initialFactory = new TCPSocketFactory();

    /* Ordered list of factories to try as alternate connection
     * mechanisms if a direct socket connections fails.
     */
    private Vector<JSDTObject> altFactoryList = new Vector<>(2);


/**
 * Create a JSDTMasterSocketFactory object. Establish order of connection
 * mechanisms to attempt on createSocket, if a direct socket connection fails.
 */

    public
    JSDTMasterSocketFactory() {
        if (JSDTMasterSocketFactory_Debug) {
            debug("JSDTMasterSocketFactory: constructor.");
        }

        altFactoryList.addElement(new HttpToPortSocketFactory());
        altFactoryList.addElement(new HttpToCGISocketFactory());
    }


/**
 * Create a new client socket. If we remember connecting to this host
 * successfully before, then use the same factory again.  Otherwise, try
 * using a direct socket connection and then the alternate factories in
 * the order specified in altFactoryList.
 */

    public Socket
    createSocket(String host, int port) throws IOException {
        JSDTSocketFactory factory;

        if (JSDTMasterSocketFactory_Debug) {
            debug("JSDTMasterSocketFactory: createSocket:" +
                  " host: " + host +
                  " port: " + port);
        }

// If we successfully connected to this host before, use the same factory.

        factory = successTable.get(host);
        if (factory != null) {
            if (JSDTMasterSocketFactory_Debug) {
                debug("JSDTMasterSocketFactory: createSocket:" +
                      " previously successful factory found: " + factory);
            }
            socketFactory = factory;
            return(factory.createSocket(host, port));
        }

/* Next, try a direct socket connection.  Open socket in another thread and
 * only wait for specified timeout, in case the socket would otherwise spend
 * minutes trying an unreachable host.
 */

        Socket         initialSocket;
        Socket         fallbackSocket = null;
        AsyncConnector connector = new AsyncConnector(this, initialFactory,
                                                      host, port);
        IOException    initialFailure = null;

        try {
            long timeoutValue = Util.getLongProperty("timeoutPeriod",
                                                     timeoutPeriod);

            synchronized (connector) {
                Util.startThread(connector,
                                 "AsyncConnector:" + host + ":" + port, true);

                try {
                    connector.wait(timeoutValue);
                } catch (InterruptedException e) {
                }
                initialSocket = checkConnector(connector);
            }

// Assume no route to host (for now) if no connection yet.

            if (initialSocket == null) {
                throw new NoRouteToHostException("connect timed out: " + host);
            }

            if (JSDTMasterSocketFactory_Debug) {
                debug("JSDTMasterSocketFactory: createSocket:" +
                      " direct socket connection successful");
            }

            socketFactory = initialFactory;
            return(initialSocket);
        } catch (UnknownHostException | NoRouteToHostException e) {
            initialFailure = e;
        } finally {
            if (initialFailure != null) {

                if (JSDTMasterSocketFactory_Debug) {
                    debug("JSDTMasterSocketFactory: createSocket:" +
                          " direct socket connection failed: " +
                          initialFailure);
                }

// Finally, try any alternate connection mechanisms.

                for (int i = 0; i < altFactoryList.size(); ++ i) {
                    factory = (JSDTSocketFactory) altFactoryList.elementAt(i);
                    try {
                        if (JSDTMasterSocketFactory_Debug) {
                            debug("JSDTMasterSocketFactory: createSocket:" +
                                  " trying with factory: " + factory);
                        }

/* For HTTP connections, the output (POST request) must be sent before we
 * verify a successful connection. So, sacrifice a socket for the sake of
 * testing... The following sequence should verify a successful HTTP
 * connection if no IOException is thrown.
 */

                        Socket      ts = factory.createSocket(host, port);
                        InputStream in = ts.getInputStream();
                        in.read();            // Probably -1 for EOF...

                        ts.close();
                    } catch (IOException ex) {
                        if (JSDTMasterSocketFactory_Debug) {
                            debug("JSDTMasterSocketFactory: createSocket:" +
                                  " factory failed: " + ex);
                        }
                        continue;
                    }
                    if (JSDTMasterSocketFactory_Debug) {
                        debug("JSDTMasterSocketFactory: createSocket:" +
                              " factory succeeded");
                    }

// Factory succeeded, open new socket for caller's use.

                    try {
                        fallbackSocket = factory.createSocket(host, port);
                    } catch (IOException ex) {        // If it fails 2nd time,
                    }                                 // just give up.
                    break;
                }
            }
        }

        synchronized (successTable) {
            try {

// Check once again to see if direct connection succeeded.

                synchronized (connector) {
                    initialSocket = checkConnector(connector);
                }
                if (initialSocket != null) {

// If we had made another one as well, clean it up...

                    if (fallbackSocket != null) {
                        fallbackSocket.close();
                    }
                    return(initialSocket);
                }

// If connector ever does get socket, it won't be used.

                connector.notUsed();
            } catch (UnknownHostException | NoRouteToHostException e) {
                initialFailure = e;
            }

// If we had found an alternate mechanism, go and use it.

            if (fallbackSocket != null) {

// Remember this successful host/factory pair.

                rememberFactory(host, factory);
                socketFactory = factory;
                return(fallbackSocket);
            }
            throw initialFailure;
        }
    }


/*
 * Remember a successful factory for connecting to host.
 * Currently, excess hosts are removed from the remembered list
 * using a Least Recently Created strategy.
 */

    void
    rememberFactory(String host, JSDTSocketFactory factory) {
        synchronized (successTable) {
            while (hostList.size() >= MaxRememberedHosts) {
                successTable.remove(hostList.elementAt(0));
                hostList.removeElementAt(0);
            }
            hostList.addElement(host);
            successTable.put(host, factory);
        }
    }


/*
 * Check if an AsyncConnector succeeded.  If not, return socket
 * given to fall back to.
 */

    private Socket
    checkConnector(AsyncConnector connector) throws IOException {
        IOException e = connector.getException();

        if (e != null) {
            e.fillInStackTrace();
            throw e;
        }
        return(connector.getSocket());
    }


// Create a new server socket.

    public ServerSocket
    createServerSocket(int port) throws IOException {
        return(new HttpAwareServerSocket(port));
    }


/**
 * <A NAME="SD_CREATEDATAGRAMSOCKET"></A>
 * <EM>createDatagramSocket</EM> returns a socket for sending and receiving
 * datagram packets, bound to any available port on the local host machine.
 * This socket is configured using the socket options established for this
 * factory.
 *
 * @exception SocketException if the socket could not be opened, or the socket
 * could not bind to the specified local port.
 *
 * @see java.net.DatagramSocket
 *
 * @return a DatagramSocket on any available local port.
 */

    public DatagramSocket
    createDatagramSocket() throws SocketException {
        if (JSDTMasterSocketFactory_Debug) {
            debug("JSDTMasterSocketFactory: createDatagramSocket.");
        }

        throw new SocketException();
    }


/**
 * <A NAME="SD_CREATEDATAGRAMSOCKET"></A>
 * <EM>createDatagramSocket</EM> returns a socket for sending and receiving
 * datagram packets, at the given local port. This socket is configured using
 * the socket options established for this factory.
 *
 * @param port the local port to use
 *
 * @exception SocketException if the socket could not be opened, or the socket
 * could not bind to the specified local port.
 *
 * @see java.net.DatagramSocket
 *
 * @return a DatagramSocket on the given local port.
 */

    public DatagramSocket
    createDatagramSocket(int port) throws SocketException {
        if (JSDTMasterSocketFactory_Debug) {
            debug("JSDTMasterSocketFactory: createDatagramSocket:" +
                  " port: " + port);
        }

        throw new SocketException();
    }
}


/**
 * AsyncConnector is used by JSDTMasterSocketFactory to attempt socket
 * connections on a separate thread. This allows JSDTMasterSocketFactory
 * to control how long it will wait for the connection to succeed.
 */

class
AsyncConnector implements Runnable {

    /** The JSDTMasterSocketFactory that created this. */
    private JSDTMasterSocketFactory master;

    /** What factory to use to attempt connection. */
    protected JSDTSocketFactory factory;

    /** The host to connect to. */
    protected String host;

    /** The port to connect to. */
    protected int port;

    /** IOException that occurred during connection, if any. */
    protected IOException exception = null;

    /** The connected socket, if successful. */
    protected Socket socket = null;

    /** Socket should be closed after created, if ever. */
    private boolean cleanUp = false;


// Create a new asynchronous connector object.

    AsyncConnector(JSDTMasterSocketFactory master, JSDTSocketFactory factory,
                   String host, int port) {
        this.master  = master;
        this.factory = factory;
        this.host    = host;
        this.port    = port;
    }


/*
 * Attempt socket connection in separate thread.  If successful,
 * notify master waiting,
 */

    public void
    run() {
        try {
            Socket temp = factory.createSocket(host, port);

            synchronized (this) {
                socket = temp;
                notify();
            }
            master.rememberFactory(host, factory);
            synchronized (this) {
                if (cleanUp) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                    }
                }
            }
        } catch (IOException e) {
            synchronized (this) {
                exception = e;
                notify();
            }
        }
    }


// Get exception that occurred during connection attempt, if any.

    synchronized IOException
    getException() {
        return(exception);
    }


// Get successful socket, if any.

    synchronized Socket
    getSocket() {
        return(socket);
    }


/*
 * Note that this connector's socket, if ever successfully created,
 * will not be used, so it should be cleaned up quickly
 */

    synchronized void
    notUsed() {
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
            }
        }
        cleanUp = true;
    }
}
