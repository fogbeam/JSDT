
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

/**
 * HttpToPortSocketFactory creates a socket connection to the
 * specified host that is communicated within an HTTP request,
 * forwarded through the default firewall proxy, directly to the
 * specified port.
 *
 * Based on the sun.rmi.transport.proxy.RMIHttpToPortSocketFactory class.
 *
 * @version     2.3 - 6th November 2017
 * @author      Rich Burridge
 */

class
HttpToPortSocketFactory extends JSDTObject
                        implements JSDTSocketFactory, httpDebugFlags {

    public Socket
    createSocket(String host, int port) throws IOException {
        String URIName = Util.getStringProperty("httpURIName", httpURIName);

        if (HttpToPortSocketFactory_Debug) {
            debug("HttpToPortSocketFactory: createSocket:" +
                  " host: " + host +
                  " port: " + port);
        }

        socketFactory = this;
        return(new HttpSendSocket(host, port,
                                  new URL("http", host, port, URIName)));
    }


    public ServerSocket
    createServerSocket(int port) throws IOException {
        if (HttpToPortSocketFactory_Debug) {
            debug("HttpToPortSocketFactory: createServerSocket:" +
                  " port: " + port);
        }

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
        if (HttpToPortSocketFactory_Debug) {
            debug("HttpToPortSocketFactory: createDatagramSocket.");
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
        if (HttpToPortSocketFactory_Debug) {
            debug("HttpToPortSocketFactory: createDatagramSocket:" +
                  " port: " + port);
        }

        throw new SocketException();
    }
}
