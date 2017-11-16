
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
 * JSDT UDP socket thread class.
 *
 * @version     2.3 - 6th November 2017
 * @author      Rich Burridge
 */

class
UDPSocketThread extends SocketThread {

    // The datagram socket used to send and receive packets.
    DatagramSocket dsock = null;

    // The Internet address of the server.
    private InetAddress inetAddress = null;

    // The datagram packet being processed.
    protected DatagramPacket packet = null;

    // The byte array input stream associated with this thread.
    JSDTByteArrayInputStream in = null;

    // The byte array output stream associated with this thread.
    private ByteArrayOutputStream out = null;


/**
 * <A NAME="SD_UDPSOCKETTHREAD"></A>
 * <EM>UDPSocketThread</EM>
 *
 * @param address
 * @param port
 */


    public
    UDPSocketThread(String address, int port) {
        String factoryClass = Util.getStringProperty("socketFactoryClass",
                                                     socketFactoryClass);

        if (UDPSocketThread_Debug) {
            debug("UDPSocketThread: constructor:" +
                  " address: " + address +
                  " port: "    + port);
        }

        try {
            JSDTSocketFactory factory = (JSDTSocketFactory)
                  Util.getClassForName(factoryClass).newInstance();

            dsock       = factory.createDatagramSocket();
            inetAddress = InetAddress.getByName(address);
        } catch (Exception e) {
            error("UDPSocketThread: constructor: ", e);
        }

        this.address = address;
        this.port    = port;

        in      = new JSDTByteArrayInputStream();
        dataIn  = new DataInputStream(in);
        out     = new ByteArrayOutputStream();
        dataOut = new DataOutputStream(new BufferedOutputStream(out));
    }


/**
 * <A NAME="SD_UDPSOCKETTHREAD"></A>
 * <EM>UDPSocketThread</EM>
 *
 * @param port
 */


    public
    UDPSocketThread(int port) {
        String factoryClass = Util.getStringProperty("socketFactoryClass",
                                                     socketFactoryClass);

        if (UDPSocketThread_Debug) {
            debug("UDPSocketThread: constructor:" +
                  " port: " + port);
        }

        try {
            JSDTSocketFactory factory = (JSDTSocketFactory)
                  Util.getClassForName(factoryClass).newInstance();

            dsock       = factory.createDatagramSocket(port);
            inetAddress = InetAddress.getLocalHost();
        } catch (Exception e) {
            error("UDPSocketThread: constructor: ", e);
        }

        this.address = inetAddress.getHostName();
        this.port    = port;

        in      = new JSDTByteArrayInputStream();
        dataIn  = new DataInputStream(in);
        out     = new ByteArrayOutputStream();
        dataOut = new DataOutputStream(new BufferedOutputStream(out));
    }


/**
 * <A NAME="SD_CLEANUPCONNECTION"></A>
 * <EM>cleanupConnection</EM>
 */

    public void
    cleanupConnection() {
        if (UDPSocketThread_Debug) {
            debug("UDPSocketThread: cleanupConnection.");
        }

        dsock.close();
    }


/**
 * <A NAME="SD_FLUSH"></A>
 * <EM>flush</EM> flush the message written to this socket.
 *
 */

    public void
    flush() {
        if (UDPSocketThread_Debug) {
            debug("UDPSocketThread: flush.");
        }

        try {
            DatagramPacket dp;

            dataOut.flush();
            dp = new DatagramPacket(out.toByteArray(), out.size(),
                                    inetAddress, port);
            dsock.send(dp);

            out.reset();
        } catch (IOException e) {
            error("UDPSocketThread: flush: ", e);
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
        int size = UDP_BUF_SIZE;

        if (UDPSocketThread_Debug) {
            debug("UDPSocketThread: getSocketMessage.");
        }

        try {
            packet = new DatagramPacket(new byte[size], size);

            dsock.receive(packet);

            in.setByteArray(packet.getData(), 0, packet.getLength());

/* XXX: need to be able to handle data packets > UDP_BUF_SIZE. */

            message.getMessageHeader(this);
        } catch (IOException ioe) {
            throw ioe;
        } catch (Exception e) {
            error("UDPSocketThread: getSocketMessage: ", e);
        }

        return(message.validMessageHeader());
    }


/**
 * <A NAME="SD_RUN"></A>
 * <EM>run</EM>
 */

    public void
    run() {
        if (UDPSocketThread_Debug) {
            debug("UDPSocketThread: run.");
        }

        while (true) {
            int size = UDP_BUF_SIZE;

            packet = new DatagramPacket(new byte[size], size);

            try {
                if (!getSocketMessage()) {
                    break;
                }

                if (UDPSocketThread_Debug) {
                    debug("UDPSocketThread: run:" +
                          " got a message: " + message);
                }

                handleMessage(message);
            } catch (IOException ioe) {
                cleanupConnection();
            } catch (Exception e) {
                error("UDPSocketThread: run: ", e);
            }
        }
    }
}
