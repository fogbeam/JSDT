
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

package com.sun.media.jsdt.impl;

import java.net.*;
import java.io.IOException;

/**
 * JSDT socket factory interface. This interface needs to be implemented by
 * any class wishing to provide a socket implementation.
 *
 * @version     2.3 - 28th October 2017
 * @author      Rich Burridge
 */

public interface
JSDTSocketFactory {

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
 * @see java.net.Socket
 *
 * @return a socket connected to a ServerSocket on the named host, at the
 * given port.
 */

    Socket
    createSocket(String host, int port) throws IOException;


/**
 * <A NAME="SD_CREATESERVERSOCKET"></A>
 * <EM>createServerSocket</EM> returns a server socket which uses all network
 * interfaces on the host, and is bound to the specified port.
 * 
 * @param port the port to listen to
 *
 * @exception IOException for networking errors
 *
 * @see java.net.ServerSocket
 *
 * @return a ServerSocket on the given port.
 */

    ServerSocket
    createServerSocket(int port) throws IOException;


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

    DatagramSocket
    createDatagramSocket() throws SocketException;


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

    DatagramSocket
    createDatagramSocket(int port) throws SocketException;
}
