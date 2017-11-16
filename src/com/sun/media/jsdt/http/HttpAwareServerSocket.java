
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
import java.net.*;
import java.io.*;

/**
 * The HttpAwareServerSocket class extends the java.net.ServerSocket
 * class.  It behaves like a ServerSocket, except that if
 * the first four bytes of an accepted socket are the letters "POST",
 * then it returns an HttpReceiveSocket instead of a java.net.Socket.
 * This means that the accept method blocks until four bytes have been
 * read from the new socket's input stream.
 *
 * Based on the sun.rmi.transport.proxy.HttpAwareServerSocket class.
 *
 * @version     2.3 - 28th October 2017
 * @author      Rich Burridge
 */

class
HttpAwareServerSocket extends ServerSocket implements httpDebugFlags {

/**
 * Create a server socket on a specified port.
 *
 * @param port the port
 *
 * @exception IOException IO error when opening the socket.
 */

    public
    HttpAwareServerSocket(int port) throws IOException {
        super(port);

        if (HttpAwareServerSocket_Debug) {
            JSDTObject.Debug("HttpAwareServerSocket: constructor:" +
                             " port: " + port);
        }
    }


/**
 * Create a server socket, bind it to the specified local port
 * and listen to it.  You can connect to an annonymous port by
 * specifying the port number to be 0.  <i>backlog<\i> specifies
 * how many connection requests the system will queue up while waiting
 * for the ServerSocket to execute accept().
 *
 * @param port the specified port
 * @param backlog the number of queued connect requests pending accept
 *
 * @exception IOException IO error when opening the socket.
 */

    public
    HttpAwareServerSocket(int port, int backlog) throws IOException {
        super(port, backlog);

        if (HttpAwareServerSocket_Debug) {
            JSDTObject.Debug("HttpAwareServerSocket: constructor:" +
                             " port: "    + port +
                             " backlog: " + backlog);
        }
    }

/**
 * Accept a connection. This method will block until the connection
 * is made and four bytes can be read from the input stream.
 * If the first four bytes are "POST", then an HttpReceiveSocket is
 * returned, which will handle the HTTP protocol wrapping.
 * Otherwise, a WrappedSocket is returned.  The input stream will be
 * reset to the beginning of the transmission.
 * In either case, a BufferedInputStream will already be on top of
 * the underlying socket's input stream.
 *
 * @exception IOException IO error when waiting for the connection.
 */

    public Socket
    accept() throws IOException {
        Socket socket = super.accept();
        BufferedInputStream in =
            new BufferedInputStream(socket.getInputStream());

        if (HttpAwareServerSocket_Debug) {
            JSDTObject.Debug("HttpAwareServerSocket: accept:" +
                             " socket accepted (checking for POST)");
        }

        in.mark(4);
        int c1 = in.read();
        int c2 = in.read();
        int c3 = in.read();
        int c4 = in.read();
        boolean isHttp = ((c1 == 'G') && (c2 == 'E') &&    /* "GET " */
                          (c3 == 'T') && (c4 == ' ')) ||
                         ((c1 == 'P') && (c2 == 'O') &&    /* "POST" */
                          (c3 == 'S') && (c4 == 'T'));
        in.reset();

        if (HttpAwareServerSocket_Debug) {
            System.err.println("HttpAwareServerSocket: accept:" +
                     (isHttp ? "GET or POST found, HTTP socket returned" :
                        "GET or POST not found, direct socket returned"));
        }

        if (isHttp) {
            return(new HttpReceiveSocket(socket, in, null));
        } else {
            return(new WrappedSocket(socket, in, null));
        }
    }


/**
 * Return the implementation address and implementation port of
 * the HttpAwareServerSocket as a String.
 */

    public String
    toString() {
        return("HttpAware" + super.toString());
    }
}
