
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
import com.sun.media.jsdt.impl.JSDTSecurity;
import java.io.*;
import java.net.*;

/**
 * The HttpSendSocket class extends the java.net.Socket class
 * by enclosing the data output stream in, then extracting the input
 * stream from, an HTTP protocol transmission.
 *
 * NOTES:
 *
 * Since the length of the output request must be known before the
 * HTTP header can be completed, all of the output is buffered by
 * an HttpOutputStream object until either an attempt is made to
 * read from this socket, or the socket is explicitly closed.
 *
 * On the first read attempt to read from this socket, the buffered
 * output is sent to the destination as the body of an HTTP POST
 * request.  All reads will then acquire data from the body of
 * the response.  A subsequent attempt to write to this socket will
 * throw an IOException.
 *
 * Based on the sun.rmi.transport.proxy.HttpSendSocket class.
 *
 * @version     2.3 - 6th November 2017
 * @author      Rich Burridge
 */

class
HttpSendSocket extends Socket implements JSDTSocketInfo, httpDebugFlags {

    // The host to connect to.
    protected String host;

    // The port to connect to.
    protected int port;

    // The URL to forward through.
    protected URL url;

    // The object managing this connection through the URL.
    private URLConnection conn = null;

    // Internal input stream for this socket.
    protected InputStream in = null;

    // Internal output stream for this socket.
    protected OutputStream out = null;

    // The notifying input stream returned to users.
    private HttpSendInputStream inNotifier;

    // The notifying output stream returned to users.
    private HttpSendOutputStream outNotifier;

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
    HttpSendSocket(String host, int port, URL url) throws IOException {
        super((SocketImpl) null);  // No underlying SocketImpl for this object.

        if (HttpSendSocket_Debug) {
            JSDTObject.Debug("HttpSendSocket: constructor:" +
                             " host: " + host +
                             " port: " + port +
                             " url: "  + url);
        }

        this.host = host;
        this.port = port;
        this.url = url;

        inNotifier  = new HttpSendInputStream(null, this);
        outNotifier = new HttpSendOutputStream(writeNotify(), this);
    }


/**
 * Create a stream socket and connect it to the specified port on
 * the specified host.
 *
 * @param host the host
 * @param port the port
 */

    public
    HttpSendSocket(String host, int port) throws IOException {
        this(host, port, new URL("http", host, port, "/"));
    }


/**
 * Create a stream socket and connect it to the specified address on
 * the specified port.
 *
 * @param address the address
 * @param port the port
 */

    public
    HttpSendSocket(InetAddress address, int port) throws IOException {
        this(address.getHostName(), port);
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
 * Create a new socket connection to host (or proxy), and prepare to
 * send HTTP transmission.
 */

    public synchronized OutputStream
    writeNotify() throws IOException {
        if (HttpSendSocket_Debug) {
            JSDTObject.Debug("HttpSendSocket: writeNotify.");
        }

        if (conn != null) {
            throw new IOException("attempt to write on HttpSendSocket after " +
                                  "request has been sent");
        }

        conn = url.openConnection();
        conn.setDoOutput(true);
        conn.setUseCaches(false);
        conn.setRequestProperty("Content-type", "application/octet-stream");

        inNotifier.deactivate();
        in = null;

        return(out = conn.getOutputStream());
    }


/**
 * Send HTTP output transmission and prepare to receive response.
 */

    public synchronized InputStream
    readNotify() throws IOException {
        if (HttpSendSocket_Debug) {
            JSDTObject.Debug("HttpSendSocket: readNotify.");
        }

        try {
            JSDTSecurity.enablePrivilege.invoke(JSDTSecurity.privilegeManager,
                                                JSDTSecurity.connectArgs);
        } catch (Exception e) {
        }

        outNotifier.deactivate();
        if (out != null) {
            out.close();
            out = null;
        }

        try {
            in = conn.getInputStream();
        } catch (IOException e) {
            if (HttpSendSocket_Debug) {
                JSDTObject.Debug("HttpSendSocket: readNotify:" +
                              " failed to get input stream: exception: " + e);
            }   

            throw new IOException("HTTP request failed");
        }

/*
 * If an HTTP error response is returned, sometimes an IOException
 * is thrown, which is handled above, and other times it isn't, and
 * the error response body will be available for reading.
 * As a safety net to catch any such unexpected HTTP behavior, we
 * verify that the content type of the response is what the
 * HttpOutputStream generates: "application/octet-stream".
 * (Servers' error responses will generally be "text/html".)
 * Any error response body is printed to the log.
 */

        String contentType = conn.getContentType();

        if (contentType == null ||
            !conn.getContentType().equals("application/octet-stream")) {
            if (HttpSendSocket_Debug) {
                if (contentType == null) {
                    JSDTObject.Debug("HttpSendSocket: readNotify:" +
                                     " missing content type in response");
                } else {
                    JSDTObject.Debug("HttpSendSocket: readNotify:" +
                                     " invalid content type in response: " +
                                     contentType);
                }
            }

            throw new IOException("HTTP request failed");
        }

        return(in);
    }


/**
 * Get the address to which the socket is connected.
 */

    public InetAddress
    getInetAddress() {
        if (HttpSendSocket_Debug) {
            JSDTObject.Debug("HttpSendSocket: getInetAddress.");
        }

        try {
            return(InetAddress.getByName(host));
        } catch (UnknownHostException e) {
            return(null);        // Null if couldn't resolve destination host.
        }
    }


/**
 * Get the local address to which the socket is bound.
 */

    public InetAddress
    getLocalAddress() {
        if (HttpSendSocket_Debug) {
            JSDTObject.Debug("HttpSendSocket: getLocalAddress.");
        }

        try {
            return(InetAddress.getLocalHost());
        } catch (UnknownHostException e) {
            return(null);        // null if couldn't determine local host
        }
    }


/**
 * Get the remote port to which the socket is connected.
 */

    public int
    getPort() {
        if (HttpSendSocket_Debug) {
            JSDTObject.Debug("HttpSendSocket: getPort.");
        }

        return(port);
    }


/**
 * Get the local port to which the socket is connected.
 */

    public int
    getLocalPort() {
        if (HttpSendSocket_Debug) {
            JSDTObject.Debug("HttpSendSocket: getLocalPort.");
        }

        return(-1);        // Request not applicable to this socket type.
    }


/**
 * Get an InputStream for this socket.
 */

    public InputStream
    getInputStream() throws IOException {
        if (HttpSendSocket_Debug) {
            JSDTObject.Debug("HttpSendSocket: getInputStream.");
        }

        return(inNotifier);
    }


/**
 * Get an OutputStream for this socket.
 */

    public OutputStream
    getOutputStream() throws IOException {
        if (HttpSendSocket_Debug) {
            JSDTObject.Debug("HttpSendSocket: getOutputStream.");
        }

        return(outNotifier);
    }


/**
 * Enable/disable TCP_NODELAY.
 * This operation has no effect for an HttpSendSocket.
 */

    public void
    setTcpNoDelay(boolean on) throws SocketException {
        if (HttpSendSocket_Debug) {
            JSDTObject.Debug("HttpSendSocket: setTcpNoDelay:" +
                             " on: " + on);
        }
    }


/**
 * Retrieve whether TCP_NODELAY is enabled.
 */

    public boolean
    getTcpNoDelay() throws SocketException {
        if (HttpSendSocket_Debug) {
            JSDTObject.Debug("HttpSendSocket: getTcpNoDelay.");
        }

        return(false);                // Imply option is disabled.
    }


/**
 * Enable/disable SO_LINGER with the specified linger time. 
 * This operation has no effect for an HttpSendSocket.
 */

    public void
    setSoLinger(boolean on, int val) throws SocketException {
        if (HttpSendSocket_Debug) {
            JSDTObject.Debug("HttpSendSocket: setSoLinger:" +
                             " on: "  + on +
                             " val: " + val); 
        }
    }


/**
 * Retrive setting for SO_LINGER.
 */

    public int
    getSoLinger() throws SocketException {
        if (HttpSendSocket_Debug) {
            JSDTObject.Debug("HttpSendSocket: getSoLinger.");
        }

        return(-1);               // Imply option is disabled.
    }


/**
 * Enable/disable SO_TIMEOUT with the specified timeout
 * This operation has no effect for an HttpSendSocket.
 */

    public synchronized void
    setSoTimeout(int timeout) throws SocketException {
        if (HttpSendSocket_Debug) {
            JSDTObject.Debug("HttpSendSocket: setSoTimeout:" +
                             " timeout: " + timeout); 
        }
    }


/**
 * Retrive setting for SO_TIMEOUT.
 */

    public synchronized int
    getSoTimeout() throws SocketException {
        if (HttpSendSocket_Debug) {
            JSDTObject.Debug("HttpSendSocket: getSoTimeout.");
        }

        return(0);               // Imply option is disabled.
    }


/**
 * Close the socket.
 */

    public synchronized void
    close() throws IOException {
        if (HttpSendSocket_Debug) {
            JSDTObject.Debug("HttpSendSocket: close.");
        }

        if (out != null) {     // Push out transmission if not done.
            out.close();
        }
    }


/**
 * Return string representation of this pseudo-socket.
 */

    public String
    toString() {
        return("HttpSendSocket[host=" + host +
                             ",port=" + port +
                             ",url=" + url + "]");
    }
}
