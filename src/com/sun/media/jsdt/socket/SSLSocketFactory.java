
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

import javax.net.ssl.*;
import com.sun.media.jsdt.impl.*;
import java.net.*;
import java.security.KeyStore;
import java.io.*;

/**
 * This class provides an example of how to create SSL sockets, using
 * classes from the javax.net.ssl package.
 *
 * Code taken from the examples given in the
 * Java Secure Socket Extension (JSSE) Reference Guide
 * for the Java 2 SDK, Standard Edition, v 1.4.2
 *
 * @version     2.3 - 27th October 2004
 * @author      Rich Burridge
 */

class
SSLSocketFactory extends JSDTObject
                 implements JSDTSocketFactory, socketDebugFlags {

    // Factory for creating SSL sockets.
    private javax.net.ssl.SSLSocketFactory sf = null;

    // Factory for creating SSL server sockets.
    private SSLServerSocketFactory ssf = null;


/**
 * <A NAME="SD_SSLSOCKETFACTORY"></A>
 * <EM>SSLSocketFactory</EM> instantiates an instance of this class.
 */

    public
    SSLSocketFactory() {
        // Password used for checking the integrity of the keystore data.
        char[] passphrase;

        // Location of the file containing the KeyStore key.
        String keyStoreFile;

        // SSLContext object that implements the TLS secure socket protocol.
        SSLContext ctx;

        /* KeyManagerFactory object that implements the SUNX509 key
         * management algorithm.
         */
        KeyManagerFactory kmf;

        /* TrustManagerFactory object that implements the SUNX509 trust
         * management algorithm.
         */
        TrustManagerFactory tmf;

        // KeyStore object of type "JKS".
        KeyStore ks;

        if (SSLSocketFactory_Debug) {
            debug("SSLSocketFactory: constructor.");
        }

        keyStoreFile = Util.getStringProperty("SSLKeyStore", SSLKeyStore);
        passphrase = "passphrase".toCharArray();

        try {
            /* Set up key manager to do server authentication. */
            ctx = SSLContext.getInstance("TLS");
            kmf = KeyManagerFactory.getInstance("SunX509");
            tmf = TrustManagerFactory.getInstance("SunX509");
            ks = KeyStore.getInstance("JKS");

            ks.load(new FileInputStream(keyStoreFile), passphrase);
            kmf.init(ks, passphrase);
            tmf.init(ks);
            ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

            sf  = ctx.getSocketFactory();
            ssf = ctx.getServerSocketFactory();
        } catch (Exception e) {
            error("SSLSocketFactory: constructor: ", e);
        }
    }


/**
 * <A NAME="SD_CREATESOCKET"></A>
 * <EM>createSocket</EM> returns a socket connected to a ServerSocket on
 * the named host, at the given port.  This socket is configured using
 * the socket options established for this factory.
 *
 * @param host the server host
 * @param port the server port
 *
 * @exception IOException if the connection can't be established
 *
 * @see javax.net.ssl.SSLSocketFactory
 * @see javax.net.ssl.SSLSocket
 *
 * @return a socket connected to a ServerSocket on the named host, at the
 * given port.
 */

    public Socket
    createSocket(String host, int port) throws IOException {
        SSLSocket socket;

        if (SSLSocketFactory_Debug) {
            debug("SSLSocketFactory: createSocket:" +
                  " host: " + host +
                  " port: " + port);
        }

        socket = (SSLSocket) sf.createSocket(host, port);
        socket.startHandshake();

        return(socket);
    }


/**
 * <A NAME="SD_CREATESERVERSOCKET"></A>
 * <EM>createServerSocket</EM> returns a server socket which uses all network
 * interfaces on the host, and is bound to the specified port.
 *
 * @param port the port to listen to
 *
 * @exception IOException for networking errors
 *
 * @see javax.net.ssl.SSLServerSocketFactory
 * @see javax.net.ssl.SSLServerSocket
 *
 * @return a ServerSocket on the given port.
 */

    public ServerSocket
    createServerSocket(int port) throws IOException {
        if (SSLSocketFactory_Debug) {
            debug("SSLSocketFactory: createServerSocket:" +
                  " port: " + port);
        }

        return(ssf.createServerSocket(port));
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
        if (SSLSocketFactory_Debug) {
            debug("SSLSocketFactory: createDatagramSocket.");
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
        if (SSLSocketFactory_Debug) {
            debug("SSLSocketFactory: createDatagramSocket:" +
                  " port: " + port);
        }

        throw new SocketException();
    }
}
