
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

import com.sun.media.jsdt.*;
import com.sun.media.jsdt.event.ClientListener;
import java.util.*;

/**
 * JSDT Client (implementation) class.
 *
 * @version     2.3 - 30th October 2017
 * @author      Rich Burridge
 */

public class
ClientImpl extends JSDTObject implements Client {

    /** Object to use as a check this Client is not being spoofed.
     *
     *  @serial
     */
    private Object check;

    /** To signify an object of type Client. */
    public static final char M_Client = '\u00F5';

    /** Byte Arrays that this client is currently joined to.
     *
     *  @serial
     */
    private Vector<ByteArray> byteArrays = null;

    /** Channels that this client is currently joined to.
     *
     *  @serial
     */
    private Vector<Channel> channels = null;

    /** Tokens that this client is currently joined to.
     *
     *  @serial
     */
    private Vector<Token> tokens = null;

    /** Channel consumers associated with this client.
     *
     *  @serial
     */
    private Hashtable<String, ChannelConsumer> consumers = null;

    /** The name of this client.
     *
     *  @serial
     */
    private String name;

    /** For invitable clients, the client to use for authentication.
     *
     *  @serial
     */
    private Client client = null;

    /** For invitable clients, the listener to send client events too.
     *
     *  @serial
     */
    private ClientListener listener = null;


/**
 * <A NAME="SD_CLIENTIMPL"></A>
 * <EM>ClientImpl</EM> the default constructor for the ClientImpl class.
 */

    public
    ClientImpl() {
        if (ClientImpl_Debug) {
            debug("ClientImpl: constructor.");
        }
    }


/**
 * <A NAME="SD_CLIENTIMPL"></A>
 * <EM>ClientImpl</EM> the constructor for the ClientImpl class.
 *
 * @param name the name of the client being constructed.
 * @param check object to use as check that this Client is not being spoofed.
 * This is implementation specific.
 */

    public
    ClientImpl(String name, Object check) {
        if (ClientImpl_Debug) {
            debug("ClientImpl: constructor:" +
                  " name: "  + name +
                  " check: " + check);
        }

        this.name  = name;
        this.check = check;

        byteArrays = new Vector<>();
        channels   = new Vector<>();
        consumers  = new Hashtable<>();
        tokens     = new Vector<>();
    }


/**
 * <A NAME="SD_AUTHENTICATE"></A>
 * used to authenticate a Client for potentially joining a managed object,
 * or creating or destroying a ByteArray, Channel or Token within a managed
 * Session. The ByteArray, Channel, Session or Token manager will be doing
 * this Client validation.
 *
 * <P>The manager sends the Client an authentication request. Within this
 * request is a challenge. The Client replies with a response.
 * This response is validated by the manager and determines if the
 * Client will be allowed to join the ByteArray/Channel/Session/Token
 * or create/destroy the ByteArray/Channel/Token.
 *
 * <P>The challenge given by the manager and the response provided by the
 * Client are both Java objects. There must be some agreed policy between
 * the manager and the Client with regards to these objects. In other words
 * the Client needs to know what to do with the challenge and how to respond
 * to it, and the manager needs to know how to handle that response.
 *
 * @param info the authentication information. This object needs to be
 * serializable.
 *
 * @return the response by the Client to the managers challenge.
 */

    public Object
    authenticate(AuthenticationInfo info) {
        if (ClientImpl_Debug) {
            debug("ClientImpl: authenticate:" +
                  " info: " + info);
        }

        if (client != null) {
            return(client.authenticate(info));
        } else {
            return(null);
        }
    }


/**
 * <A NAME="SD_GETNAME"></A>
 * get the name of this Client.
 *
 * @return the name of the Client.
 */

    public String
    getName() {
        if (ClientImpl_Debug) {
            debug("ClientImpl: getName.");
        }

        if (client != null) {
            return(client.getName());
        } else {
            return(name);
        }
    }


/**
 * <A NAME="SD_GETCHECK"></A>
 * <EM>getCheck</EM> gets the object to use as a check this Client is not
 * being spoofed. This is implementation specific.
 *
 * @return the object to use as a check this Client is not being spoofed.
 * joined to.
 */

    public Object
    getCheck() {
        if (ClientImpl_Debug) {
            debug("ClientImpl: getCheck.");
        }

        return(check);
    }


/**
 * <A NAME="SD_GETBYTEARRAYS"></A>
 * <EM>getByteArrays</EM> get a vector of the byte arrays that this client is
 * currently joined to.
 *
 * @return a Vector containing the byte arrays that this client is currently
 * joined to.
 */

    public Vector<ByteArray>
    getByteArrays() {
        if (ClientImpl_Debug) {
            debug("ClientImpl: getByteArrays.");
        }

        return(byteArrays);
    }


/**
 * <A NAME="SD_GETCHANNELS"></A>
 * <EM>getChannels</EM> get a vector of the channels that this client is
 * currently joined to.
 *
 * @return a Vector containing the channels that this client is currently
 * joined to.
 */

    public Vector<Channel>
    getChannels() {
        if (ClientImpl_Debug) {
            debug("ClientImpl: getChannels.");
        }

        return(channels);
    }


/**
 * <A NAME="SD_GETCONSUMERS"></A>
 * <EM>getConsumers</EM> get a hashtable of the channel consumers associated
 * with this client.
 *
 * @return a Hashtable containing the channel consumers for this client.
 */

    public Hashtable<String, ChannelConsumer>
    getConsumers() {
        if (ClientImpl_Debug) {
            debug("ClientImpl: getConsumers.");
        }

        return(consumers);
    }


/**
 * <A NAME="SD_GETTOKENS"></A>
 * <EM>getTokens</EM> get a vector of the tokens that this client is
 * currently joined to.
 *
 * @return a Vector containing the tokens that this client is currently
 * joined to.
 */

    public Vector<Token>
    getTokens() {
        if (ClientImpl_Debug) {
            debug("ClientImpl: getTokens.");
        }

        return(tokens);
    }


/**
 * <A NAME="SD_SETCLIENTANDLISTENER"></A>
 * <EM>setClientAndListener</EM> set the Client and Listener associated with
 * this invitable Client.
 *
 * @param client the client to use for authentication purposes.
 * @param listener the listener to send ClientEvents too.
 */

    public void
    setClientAndListener(Client client, ClientListener listener) {
        if (ClientImpl_Debug) {
            debug("ClientImpl: setClientAndListener:" +
                  " client: "   + client +
                  " listener: " + listener);
        }

        this.client   = client;
        this.listener = listener;
    }


/**
 * <A NAME="SD_GETLISTENER"></A>
 * <EM>getListener</EM> returns the client listener associated with this
 * invitable client.
 *
 * @return the client listener associated with this invitable client, or
 * null if there isn't one assigned.
 */

    public ClientListener
    getListener() {
        if (ClientImpl_Debug) {
            debug("ClientImpl: getListener.");
        }

        return(listener);
    }
}
