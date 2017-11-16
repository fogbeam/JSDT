
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

package com.sun.media.jsdt.event;

import com.sun.media.jsdt.Client;
import com.sun.media.jsdt.Session;
import com.sun.media.jsdt.impl.*;
import java.util.EventObject;

/**
 * The Client event. Client events are created for the following actions:
 * <PRE>
 * - when a Client has been invited to join a ByteArray.
 * - when a Client has been expelled from a ByteArray.
 * - when a Client has been invited to join a Channel.
 * - when a Client has been expelled from a Channel.
 * - when a Client has been invited to join a Session.
 * - when a Client has been expelled from a Session.
 * - when a Client has been invited to join a Token.
 * - when a Client has been expelled from a Token.
 * - when a Client has been given a Token.
 * </PRE>
 *
 * @version     2.3 - 27th October 2017
 * @author      Rich Burridge
 * @since       JSDT 1.0
 */

public class ClientEvent extends EventObject implements JSDTDebugFlags {

    /** The Client ByteArray invited event type. */
    public static final int BYTEARRAY_INVITED  = 1 << 0;

    /** The Client ByteArray expelled event type. */
    public static final int BYTEARRAY_EXPELLED = 1 << 1;

    /** The Client Channel invited event type. */
    public static final int CHANNEL_INVITED    = 1 << 2;

    /** The Client Channel expelled event type. */
    public static final int CHANNEL_EXPELLED   = 1 << 3;

    /** The Client Session invited event type. */
    public static final int SESSION_INVITED    = 1 << 4;

    /** The Client Session expelled event type. */
    public static final int SESSION_EXPELLED   = 1 << 5;

    /** The Client Token invited event type. */
    public static final int TOKEN_INVITED      = 1 << 6;

    /** The Client Token expelled event type. */
    public static final int TOKEN_EXPELLED     = 1 << 7;

    /** The Client Token given event type. */
    public static final int TOKEN_GIVEN        = 1 << 8;

    /** The session associated with this Client event.
     *
     *  @serial
     */
    private Session session;

    /** The client associated with this Client event.
     *
     *  @serial
     */
    private Client client;

    /** The name of the resource that this event occured on.
     *
     *  @serial
     */
    protected String resourceName;

    /** The type of this Client event.
     *
     *  @serial
     */
    private int type;


/**
 * <A NAME="SD_CLIENTEVENT"></A>
 * constructor for the ClientEvent class.  A new client event is generated
 * for a specific client when there has been a change in the clients status
 * (when it's been invited to join, or expelled from a ByteArray, Channel,
 * Session or Token).
 *
 * @param client the Client associated with this Client event.
 * @param resourceName the name of the ByteArray/Channel/Session/Token.
 * @param type the type of event.
 */

    public
    ClientEvent(Session session, Client client, String resourceName, int type) {
        super(client);

        if (ClientEvent_Debug) {
            JSDTObject.Debug("ClientEvent: constructor:" +
                             " session: "       + session +
                             " client: "        + client +
                             " resource name: " + resourceName +
                             " type: "          + type);
        }

        this.session      = session;
        this.client       = client;
        this.resourceName = resourceName;
        this.type         = type;
    }


/**
 * <A NAME="SD_GETSESSION"></A>
 * get the Session associated with this event.
 *
 * @return the Session associated with this event.
 */

    public Session
    getSession() {
        if (ClientEvent_Debug) {
            JSDTObject.Debug("ClientEvent: getSession.");
        }

        return(session);
    }


/**
 * <A NAME="SD_GETRESOURCENAME"></A>
 * get the name of the resource for this event. The resource will be the
 * ByteArray, Channel, Session or Token that the Client has been invited
 * to join or has been expelled from.
 *
 * @return the name of the resource for this event.
 */

    public String
    getResourceName() {
        if (ClientEvent_Debug) {
            JSDTObject.Debug("ClientEvent: getResourceName.");
        }

        return(resourceName);
    }


/**
 * <A NAME="SD_TOSTRING"></A>
 * <EM>toString</EM> print a short description of this Client event.
 *
 * @return a String containing a description of this Client event.
 *
 * @since        JSDT 1.3
 */

    public String
    toString() {
        String typeAsString = null;

        switch (type) {
            case BYTEARRAY_INVITED:  typeAsString = "bytearray invited";
                                     break;
            case BYTEARRAY_EXPELLED: typeAsString = "bytearray expelled";
                                     break;
            case CHANNEL_INVITED:    typeAsString = "channel invited";
                                     break;
            case CHANNEL_EXPELLED:   typeAsString = "channel expelled";
                                     break;
            case SESSION_INVITED:    typeAsString = "session invited";
                                     break;
            case SESSION_EXPELLED:   typeAsString = "session expelled";
                                     break;
            case TOKEN_INVITED:      typeAsString = "token invited";
                                     break;
            case TOKEN_EXPELLED:     typeAsString = "token expelled";
                                     break;
            case TOKEN_GIVEN:        typeAsString = "token given";
        }

        return("Client event:" + "\n" +
                " session name: "  + session.getName() + "\n" +
                " client name: "   + client.getName() + "\n" +
                " resource name: " + resourceName + "\n" +
                " type: "          + typeAsString + "\n");
    }
}
