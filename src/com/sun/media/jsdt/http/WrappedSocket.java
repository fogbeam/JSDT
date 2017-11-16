
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
 * The WrappedSocket class provides a general wrapper for providing an
 * extended implementation of java.net.Socket that can be attached to
 * a pre-existing Socket object.  WrappedSocket itself provides a
 * constructor for specifying alternate input or output streams to be
 * returned than those of the underlying Socket.
 *
 * Based on the sun.rmi.transport.proxy.WrappedSocket class.
 *
 * @version     2.3 - 7th November 2017
 * @author      Rich Burridge
 */

class
WrappedSocket extends Socket implements JSDTSocketInfo, httpDebugFlags {

    // The underlying concrete socket.
    protected Socket socket;

    // The input stream to return for socket.
    protected InputStream in = null;

    // The output stream to return for socket.
    protected OutputStream out = null;

    // Indicates whether this socket should keep a permanent connection.
    private boolean reusable = false;


/**
 * Layer on top of a pre-existing Socket object, and use specified
 * input and output streams.  This allows the creator of the
 * underlying socket to peek at the beginning of the input with a
 * BufferedInputStream and determine which kind of socket
 * to create, without consuming the input.
 *
 * @param socket the pre-existing socket to use
 * @param in the InputStream to return to users (can be null)
 * @param out the OutputStream to return to users (can be null)
 */

    public
    WrappedSocket(Socket socket,
                  InputStream in, OutputStream out) throws IOException {
        super((SocketImpl) null);  // No underlying SocketImpl for this object.

        if (WrappedSocket_Debug) {
            JSDTObject.Debug("WrappedSocket: constructor:" +
                             " socket: " + socket +
                             " in: "     + in +
                             " out: "    + out);
        }

        this.socket = socket;
        this.in = in;
        this.out = out;
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
        if (WrappedSocket_Debug) {
            JSDTObject.Debug("WrappedSocket: getInetAddress.");
        }

        return(socket.getInetAddress());
    }


/**
 * Get the local address to which the socket is bound.
 */

    public InetAddress
    getLocalAddress() {
        if (WrappedSocket_Debug) {
            JSDTObject.Debug("WrappedSocket: getLocalAddress.");
        }

        return(socket.getLocalAddress());
    }


/**
 * Get the remote port to which the socket is connected.
 */

    public int
    getPort() {
        if (WrappedSocket_Debug) {
            JSDTObject.Debug("WrappedSocket: getPort.");
        }

        return(socket.getPort());
    }


/**
 * Get the local port to which the socket is connected.
 */

    public int
    getLocalPort() {
        if (WrappedSocket_Debug) {
            JSDTObject.Debug("WrappedSocket: getLocalPort.");
        }

        return(socket.getLocalPort());
    }


/**
 * Get an InputStream for this socket.
 */

    public InputStream
    getInputStream() throws IOException {
        if (WrappedSocket_Debug) {
            JSDTObject.Debug("WrappedSocket: getInputStream.");
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
        if (WrappedSocket_Debug) {
            JSDTObject.Debug("WrappedSocket: getOutputStream.");
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
        if (WrappedSocket_Debug) {
            JSDTObject.Debug("WrappedSocket: setTcpNoDelay:" +
                             " on: " + on);
        }

        socket.setTcpNoDelay(on);
    }


/**
 * Retrieve whether TCP_NODELAY is enabled.
 */

    public boolean
    getTcpNoDelay() throws SocketException {
        if (WrappedSocket_Debug) {
            JSDTObject.Debug("WrappedSocket: getTcpNoDelay.");
        }

        return(socket.getTcpNoDelay());
    }


/**
 * Enable/disable SO_LINGER with the specified linger time. 
 */

    public void
    setSoLinger(boolean on, int val) throws SocketException {
        if (WrappedSocket_Debug) {
            JSDTObject.Debug("WrappedSocket: setSoLinger:" +
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
        if (WrappedSocket_Debug) {
            JSDTObject.Debug("WrappedSocket: getSoLinger.");
        }

        return(socket.getSoLinger());
    }


/**
 * Enable/disable SO_TIMEOUT with the specified timeout
 */

    public synchronized void
    setSoTimeout(int timeout) throws SocketException {
        if (WrappedSocket_Debug) {
            JSDTObject.Debug("WrappedSocket: setSoTimeout:" +
                             " timeout: " + timeout);
        }

        socket.setSoTimeout(timeout);
    }


/**
 * Retrive setting for SO_TIMEOUT.
 */

    public synchronized int
    getSoTimeout() throws SocketException {
        if (WrappedSocket_Debug) {
            JSDTObject.Debug("WrappedSocket: getSoTimeout.");
        }

        return(socket.getSoTimeout());
    }


/**
 * Close the socket.
 */

    public synchronized void
    close() throws IOException {
        if (WrappedSocket_Debug) {
            JSDTObject.Debug("WrappedSocket: close.");
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
