
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

import com.sun.media.jsdt.impl.*;
import java.net.*;
import java.io.*;

/**
 * JSDT TCP socket thread class.
 *
 * @version     2.3 - 6th November 2017
 * @author      Rich Burridge
 */

class
TCPSocketThread extends SocketThread {

    // The socket for this thread.
    private Socket socket;

    // The input stream associated with this socket.
    private InputStream in;

    // The output stream associated with this socket.
    protected OutputStream out;


/**
 * <A NAME="SD_TCPSOCKETTHREAD"></A>
 * <EM>TCPSocketThread</EM>
 *
 * @param socket
 */

    public
    TCPSocketThread(Socket socket)
                throws SocketException, UnknownHostException {
        if (TCPSocketThread_Debug) {
            debug("TCPSocketThread: constructor:" +
                  " socket: " + socket);
        }

        try {
            in      = socket.getInputStream();
            out     = socket.getOutputStream();
            dataIn  = new DataInputStream(new BufferedInputStream(in));
            dataOut = new DataOutputStream(new BufferedOutputStream(out));
        } catch (SocketException | UnknownHostException se) {
            throw se;
        } catch (Exception e) {
            error("TCPSocketThread: constructor: ", e);
        }

        this.socket = socket;
    }


/**
 * <A NAME="SD_TCPSOCKETTHREAD"></A>
 * <EM>TCPSocketThread</EM> creates
 *
 * @param address
 * @param port
 */

    public
    TCPSocketThread(String address, int port)
                throws SocketException, UnknownHostException {
        String factoryClass = Util.getStringProperty("socketFactoryClass",
                                                     socketFactoryClass);

        if (TCPSocketThread_Debug) {
            debug("TCPSocketThread: constructor:" +
                  " address: " + address +
                  " port: "    + port);
        }

        try {
            this.address = address;
            this.port    = port;

            JSDTSocketFactory factory = (JSDTSocketFactory)
                  Util.getClassForName(factoryClass).newInstance();

            socket  = factory.createSocket(address, port);
            in      = socket.getInputStream();
            out     = socket.getOutputStream();
            dataIn  = new DataInputStream(new BufferedInputStream(in));
            dataOut = new DataOutputStream(new BufferedOutputStream(out));
        } catch (SocketException | UnknownHostException se) {
            throw se;
        } catch (Exception e) {
            error("TCPSocketThread: constructor: ", e);
        }
    }


/**
 * <A NAME="SD_CLEANUPCONNECTION"></A>
 * <EM>cleanupConnection</EM>
 */

    public void
    cleanupConnection() {
        if (TCPSocketThread_Debug) {
            debug("TCPSocketThread: cleanupConnection.");
        }

        closeSocket();
    }


/**
 * <A NAME="SD_CLOSESOCKET"></A>
 * <EM>closeSocket</EM>
 */

    void
    closeSocket() {
        if (TCPSocketThread_Debug) {
            debug("TCPSocketThread: closeSocket.");
        }

        try {
            socket.close();
        } catch (IOException e) {
            error("TCPSocketThread: closeSocket: ", e);
        }
    }


/**
 * <A NAME="SD_GETSOCKETMESSAGE"></A>
 * <EM>getSocketMessage</EM> gets the next message off the socket.
 *
 * @return true if there is a valid message to be processed.
 */

    public boolean
    getSocketMessage() throws IOException {
        if (TCPSocketThread_Debug) {
            debug("TCPSocketThread: getSocketMessage.");
        }

        message.getMessageHeader(this);
        return(message.validMessageHeader());
    }


    public void
    handleMessage(Message message) {
        if (TCPSocketThread_Debug) {
            debug("TCPSocketThread: handleMessage:" +
                  " message: " + message);
        }
    }
}
