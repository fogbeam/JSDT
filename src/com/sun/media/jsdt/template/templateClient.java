
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

package com.sun.media.jsdt.template;

import com.sun.media.jsdt.*;
import com.sun.media.jsdt.impl.ClientImpl;
import com.sun.media.jsdt.event.*;

/**
 * JSDT implementation specific Client class.
 *
 * @version     2.3 - 28th October 2017
 * @author      Rich Burridge
 */

public class
templateClient extends ClientImpl
               implements ClientListener, templateDebugFlags {

    /** The name of this Client.
     *
     *  @serial
     */
    String name;


/**
 * <A NAME="SD_TEMPLATECLIENT"></A>
 * <EM>templateClient</EM> the default constructor for the templateClient
 * class. Note that the Client name is supplied by _createProxy or
 * _createServer.
 */

    public
    templateClient() {
        if (templateClient_Debug) {
            debug("templateClient: constructor.");
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

        if (templateClient_Debug) {
            debug("templateClient: byteArrayInvited:" +
                  " event: " + event);
        }

        listener.byteArrayInvited(event);
    }


    public final void
    byteArrayExpelled(ClientEvent event) {
        ClientListener listener = getListener();

        if (templateClient_Debug) {
            debug("templateClient: byteArrayExpelled:" +
                  " event: " + event);
        }

        listener.byteArrayExpelled(event);
    }


    public final void
    channelInvited(ClientEvent event) {
        ClientListener listener = getListener();

        if (templateClient_Debug) {
            debug("templateClient: channelInvited:" +
                  " event: " + event);
        }

        listener.channelInvited(event);
    }


    public final void
    channelExpelled(ClientEvent event) {
        ClientListener listener = getListener();

        if (templateClient_Debug) {
            debug("templateClient: channelExpelled:" +
                  " event: " + event);
        }

        listener.channelExpelled(event);
    }


    public final void
    sessionInvited(ClientEvent event) {
        ClientListener listener = getListener();

        if (templateClient_Debug) {
            debug("templateClient: sessionInvited:" +
                  " event: " + event);
        }

        listener.sessionInvited(event);
    }


    public final void
    sessionExpelled(ClientEvent event) {
        ClientListener listener = getListener();

        if (templateClient_Debug) {
            debug("templateClient: sessionExpelled:" +
                  " event: " + event);
        }

        listener.sessionExpelled(event);
    }


    public final void
    tokenInvited(ClientEvent event) {
        ClientListener listener = getListener();

        if (templateClient_Debug) {
            debug("templateClient: tokenInvited:" +
                  " event: " + event);
        }

        listener.tokenInvited(event);
    }


    public final void
    tokenExpelled(ClientEvent event) {
        ClientListener listener = getListener();

        if (templateClient_Debug) {
            debug("templateClient: tokenExpelled:" +
                  " event: " + event);
        }

        listener.tokenExpelled(event);
    }


/**
 * <A NAME="SD_TOKENGIVEN"></A>
 * invoked when a Client has been given a Token.
 *
 * @param event the Client event containing more information.
 */

    public final void
    tokenGiven(ClientEvent event) {
        ClientListener listener = getListener();

        if (templateClient_Debug) {
            debug("templateClient: tokenGiven:" +
                  " event: " + event);
        }

        listener.tokenGiven(event);
    }


/**
 * <A NAME="SD__CREATEPROXY"></A>
 * <EM>_createProxy</EM> create a proxy-side connection for this Client.
 *
 * @param namingProxy the naming proxy that created this client.
 * @param name the name of the Client being constructed.
 * @param host the host of the server-side connection.
 * @param port the port number to use for the connection.
 *
 * @exception NoSuchHostException if the host given doesn't exist.
 */

    public final synchronized void
    _createProxy(NamingProxy namingProxy, String name, String host, int port)
                throws NoSuchHostException {
        if (templateClient_Debug) {
            debug("templateClient: _createProxy:" +
                  " naming proxy: " + namingProxy +
                  " object name: "  + name +
                  " host: "         + host +
                  " port: "         + port);
        }
    }


/**
 * <A NAME="SD__CREATESERVER"></A>
 * <EM>_createServer</EM> create a server-side connection for this Client.
 *
 * @param name the name of the Client being constructed.
 * @param url the url associated with this Client.
 * @param port the port number to use for the connection.
 *
 * @exception PortInUseException if this port is being used by another
 * application.
 */

    public final synchronized void
    _createServer(String name, String url, int port)
                throws PortInUseException {
        if (templateClient_Debug) {
            debug("templateClient: _createServer:" +
                  " object name: " + name +
                  " url: "         + url +
                  " port: "        + port);
        }
    }
}
