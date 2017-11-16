
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

import com.sun.media.jsdt.*;
import com.sun.media.jsdt.event.*;
import com.sun.media.jsdt.impl.ClientImpl;

/**
 * JSDT Socket Client class.
 *
 * @version     2.3 - 6th November 2017
 * @author      Rich Burridge
 */

public class
socketClient extends ClientImpl implements ClientListener, socketDebugFlags {

    // The name of this socket client.
    String name;

    // Handle to server-side object for this client.
    ClientServer cs;

    // Handle to client-side proxy for this client.
    ClientProxy cc;


/**
 * <A NAME="SD_SOCKETCLIENT"></A>
 * <EM>socketClient</EM> the default constructor for the socketClient class.
 */

    public
    socketClient() {
        if (socketClient_Debug) {
            debug("socketClient: constructor.");
        }
    }


    public final Object
    authenticate(AuthenticationInfo info) {
        return(null);
    }


    public final String
    getName() {
        return(name);
    }


    public final void
    byteArrayInvited(ClientEvent event) {
        ClientListener listener = getListener();

        if (socketClient_Debug) {
            debug("socketClient: byteArrayInvited:" +
                  " event: " + event);
        }

        listener.byteArrayInvited(event);
    }


    public final void
    byteArrayExpelled(ClientEvent event) {
        ClientListener listener = getListener();

        if (socketClient_Debug) {
            debug("socketClient: byteArrayExpelled:" +
                  " event: " + event);
        }

        listener.byteArrayExpelled(event);
    }


    public final void
    channelInvited(ClientEvent event) {
        ClientListener listener = getListener();

        if (socketClient_Debug) {
            debug("socketClient: channelInvited:" +
                  " event: " + event);
        }

        listener.channelInvited(event);
    }


    public final void
    channelExpelled(ClientEvent event) {
        ClientListener listener = getListener();

        if (socketClient_Debug) {
            debug("socketClient: channelExpelled:" +
                  " event: " + event);
        }

        listener.channelExpelled(event);
    }


    public final void
    sessionInvited(ClientEvent event) {
        ClientListener listener = getListener();

        if (socketClient_Debug) {
            debug("socketClient: sessionInvited:" +
                  " event: " + event);
        }

        listener.sessionInvited(event);
    }


    public final void
    sessionExpelled(ClientEvent event) {
        ClientListener listener = getListener();

        if (socketClient_Debug) {
            debug("socketClient: sessionExpelled:" +
                  " event: " + event);
        }

        listener.sessionExpelled(event);
    }


    public final void
    tokenInvited(ClientEvent event) {
        ClientListener listener = getListener();

        if (socketClient_Debug) {
            debug("socketClient: tokenInvited:" +
                  " event: " + event);
        }

        listener.tokenInvited(event);
    }


    public final void
    tokenExpelled(ClientEvent event) {
        ClientListener listener = getListener();

        if (socketClient_Debug) {
            debug("socketClient: tokenExpelled:" +
                  " event: " + event);
        }

        listener.tokenExpelled(event);
    }


    public final void
    tokenGiven(ClientEvent event) {
        ClientListener listener = getListener();

        if (socketClient_Debug) {
            debug("socketClient: tokenGiven:" +
                  " event: " + event);
        }

        listener.tokenGiven(event);
    }


/**
 * <A NAME="SD__CREATEPROXY"></A>
 * <EM>_createProxy</EM> create a proxy-side connection for this socket
 * Client.
 *
 * @param namingProxy the naming proxy that created this client.
 * @param url the JSDT URL string associated with this client.
 * @param name the name of the socket client being constructed.
 * @param host the host of the server-side connection.
 * @param port the port number to use for the connection.
 *
 * @exception NoSuchHostException if the host given doesn't exist.
 */

    final synchronized void
    _createProxy(NamingProxy namingProxy, String url,
                 String name, String host, int port)
                throws NoSuchHostException {
        if (socketClient_Debug) {
            debug("socketClient: _createProxy:" +
                  " naming proxy: " + namingProxy +
                  " url string: "   + url +
                  " object name: "  + name +
                  " host: "         + host +
                  " port: "         + port);
        }

        this.name = name;
        cc = new ClientProxy(this, url, name, host, port);
    }


/**
 * <A NAME="SD__CREATESERVER"></A>
 * <EM>_createServer</EM> create a server-side connection for this socket
 * Client.
 *
 * @param name the name of the socket client being constructed.
 * @param url the url associated with this socket Client.
 * @param port the port number to use for the connection.
 *
 * @exception PortInUseException if this port is being used by another
 * application.
 */

    final synchronized void
    _createServer(String name, String url, int port)
                throws PortInUseException {
        if (socketClient_Debug) {
            debug("socketClient: _createServer:" +
                  " object name: " + name +
                  " url: "         + url +
                  " port: "        + port);
        }

        this.name = name;
        cs = new ClientServer(this, name, port);
    }
}
