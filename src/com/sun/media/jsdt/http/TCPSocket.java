
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

import com.sun.media.jsdt.impl.JSDTObject;
import java.io.*;
import java.net.*;

/**
 * The TCP socket class.
 *
 * @version     2.3 - 7th November 2017
 * @author      Rich Burridge
 */

class
TCPSocket extends Socket implements JSDTSocketInfo, httpDebugFlags {

    // The host to connect to.
    protected String host;

    // The port to connect to.
    protected int port;

    // The underlying concrete socket.
    protected Socket socket;

    // The input stream to return for socket.
    protected InputStream in = null;

    // The output stream to return for socket.
    protected OutputStream out = null;

    // Indicates whether this socket should keep a permanent connection.
    private boolean reusable = false;


/**
 * Create a stream socket and connect it to the specified port on
 * the specified host.
 *
 * @param host the host
 * @param port the port
 */

    public
    TCPSocket(String host, int port) throws IOException {
        if (TCPSocket_Debug) {
            JSDTObject.Debug("TCPSocket: constructor:" +
                             " host: " + host +
                             " port: " + port);
        }

        this.socket = new Socket(host, port);
        this.host   = host;
        this.port   = port;
    }


/**
 * Indicate that this socket is not reusable.
 */

    public boolean
    isReusable() {
        return(reusable);
    }


/**
 * Set whether this socket should be a permanent connection.
 */

    public void
    setReusable(boolean reusable) {
        this.reusable = reusable;
    }


/**
 * Get the address to which the socket is connected.
 */

    public InetAddress
    getInetAddress() {
        if (TCPSocket_Debug) {
            JSDTObject.Debug("TCPSocket: getInetAddress.");
        }

        return(socket.getInetAddress());
    }


/**
 * Get the local address to which the socket is bound.
 */

    public InetAddress
    getLocalAddress() {
        if (TCPSocket_Debug) {
            JSDTObject.Debug("TCPSocket: getLocalAddress.");
        }

        return(socket.getLocalAddress());
    }


/**
 * Get the remote port to which the socket is connected.
 */

    public int
    getPort() {
        if (TCPSocket_Debug) {
            JSDTObject.Debug("TCPSocket: getPort.");
        }

        return(socket.getPort());
    }


/**
 * Get the local port to which the socket is connected.
 */

    public int
    getLocalPort() {
        if (TCPSocket_Debug) {
            JSDTObject.Debug("TCPSocket: getLocalPort.");
        }

        return(socket.getLocalPort());
    }


/**
 * Get an InputStream for this socket.
 */

    public InputStream
    getInputStream() throws IOException {
        if (TCPSocket_Debug) {
            JSDTObject.Debug("TCPSocket: getInputStream.");
        }

        if (in == null) {
            in = socket.getInputStream();
        }
        return(in);
    }


/**
 * Get an OutputStream for this socket.
 */

    public OutputStream
    getOutputStream() throws IOException {
        if (TCPSocket_Debug) {
            JSDTObject.Debug("TCPSocket: getOutputStream.");
        }

        if (out == null) {
            out = socket.getOutputStream();
        }
        return(out);
    }


/**
 * Enable/disable TCP_NODELAY.
 */

    public void
    setTcpNoDelay(boolean on) throws SocketException {
        if (TCPSocket_Debug) {
            JSDTObject.Debug("TCPSocket: setTcpNoDelay:" +
                             " on: " + on);
        }

        socket.setTcpNoDelay(on);
    }


/**
 * Retrieve whether TCP_NODELAY is enabled.
 */

    public boolean
    getTcpNoDelay() throws SocketException {
        if (TCPSocket_Debug) {
            JSDTObject.Debug("TCPSocket: getTcpNoDelay.");
        }

        return(socket.getTcpNoDelay());
    }


/**
 * Enable/disable SO_LINGER with the specified linger time. 
 */

    public void
    setSoLinger(boolean on, int val) throws SocketException {
        if (TCPSocket_Debug) {
            JSDTObject.Debug("TCPSocket: setSoLinger:" +
                             " on: " + on +
                             " val: " + val);
        }

        socket.setSoLinger(on, val);
    }


/**
 * Retrive setting for SO_LINGER.
 */

    public int
    getSoLinger() throws SocketException {
        if (TCPSocket_Debug) {
            JSDTObject.Debug("TCPSocket: getSoLinger.");
        }

        return(socket.getSoLinger());
    }


/**
 * Enable/disable SO_TIMEOUT with the specified timeout
 */

    public synchronized void
    setSoTimeout(int timeout) throws SocketException {
        if (TCPSocket_Debug) {
            JSDTObject.Debug("TCPSocket: setSoTimeout:" +
                             " timeout: " + timeout);
        }

        socket.setSoTimeout(timeout);
    }


/**
 * Retrive setting for SO_TIMEOUT.
 */

    public synchronized int
    getSoTimeout() throws SocketException {
        if (TCPSocket_Debug) {
            JSDTObject.Debug("TCPSocket: getSoTimeout.");
        }

        return(socket.getSoTimeout());
    }


/**
 * Close the socket.
 */

    public synchronized void
    close() throws IOException {
        if (TCPSocket_Debug) {
            JSDTObject.Debug("TCPSocket: close.");
        }

        socket.close();
    }


/**
 * Return string representation of the socket.
 */

    public String
    toString() {
        return("Wrapped" + socket.toString());
    }
}
