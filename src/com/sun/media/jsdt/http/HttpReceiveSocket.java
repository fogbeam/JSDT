
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
 * The HttpReceiveSocket class extends the WrappedSocket class
 * by removing the HTTP protocol packaging from the input stream and
 * formatting the output stream as an HTTP response.
 *
 * NOTES:
 *
 * The output stream must be explicitly closed for the output to be
 * sent, since the HttpResponseOutputStream needs to buffer the entire
 * transmission to be able to fill in the content-length field of
 * the HTTP header.  Closing this socket will do this.
 *
 * The constructor blocks until the HTTP protocol header
 * is received.  This could be fixed, but I don't think it should be a
 * problem because this object would not be created unless the
 * HttpAwareServerSocket has detected the beginning of the header
 * anyway, so the rest should be there.
 *
 * This socket can only be used to process one POST and reply to it.
 * Another message would be received on a newly accepted socket anyway.
 *
 * Based on the sun.rmi.transport.proxy.HttpReceiveSocket class.
 *
 * @version     2.3 - 6th November 2017
 * @author      Rich Burridge
 */

class
HttpReceiveSocket extends WrappedSocket
                  implements JSDTSocketInfo, httpDebugFlags {

    // True if the HTTP header has pushed through the output stream yet.
    private boolean headerSent = false;

    // Indicates whether this socket should keep a permanent connection.
    private boolean reusable = false;


/**
 * Layer on top of a pre-existing Socket object, and use specified
 * input and output streams.
 *
 * @param socket the pre-existing socket to use
 * @param in the InputStream to use for this socket (can be null)
 * @param out the OutputStream to use for this socket (can be null)
 */

    public
    HttpReceiveSocket(Socket socket,
                      InputStream in, OutputStream out) throws IOException {
        super(socket, in, out);

        if (HttpReceiveSocket_Debug) {
            JSDTObject.Debug("HttpReceiveSocket: constructor:" +
                             " socket: " + socket +
                             " in: "     + in +
                             " out: "    + out);
        }

        this.in = new HttpInputStream(in != null ? in :
                                                   socket.getInputStream());
        this.out = (out != null ? out : socket.getOutputStream());
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
 * Get the address to which the socket is connected.  "null" is always
 * returned because since the HTTP request may have gone through a
 * proxy server, the originating host's IP address cannot be reliably
 * determined.
 */

    public InetAddress
    getInetAddress() {
        if (HttpReceiveSocket_Debug) {
            JSDTObject.Debug("HttpReceiveSocket: getInetAddress.");
        }

        return(null);
    }


/**
 * Get the remote port to which the socket is connected.  Since the
 * HTTP request probably went through a proxy server, then this will
 * be the port on the gateway machine, which is probably not very
 * useful information.
 */

    public int
    getPort() {
        if (HttpReceiveSocket_Debug) {
            JSDTObject.Debug("HttpReceiveSocket: getPort.");
        }

        return(socket.getPort());
    }


/**
 * Get an OutputStream for this socket.
 */

    public OutputStream
    getOutputStream() throws IOException {
        if (HttpReceiveSocket_Debug) {
            JSDTObject.Debug("HttpReceiveSocket: getOutputStream.");
        }

        if (!headerSent) {     // Could this be done in constructor?
            DataOutputStream dos = new DataOutputStream(out);

            dos.writeBytes("HTTP/1.0 200 OK\r\n");
            dos.flush();
            headerSent = true;
            out = new HttpOutputStream(out, this);
        }
        return(out);
    }

/**
 * Close the socket.
 */

    public synchronized void
    close() throws IOException {
        if (HttpReceiveSocket_Debug) {
            JSDTObject.Debug("HttpReceiveSocket: close.");
        }

        getOutputStream().close();    // Make sure response is sent.
        socket.close();
    }


/**
 * Return string representation of the socket.
 */

    public String
    toString() {
        return("HttpReceive" + socket.toString());
    }
}
