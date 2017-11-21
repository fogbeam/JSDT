
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

import com.sun.media.jsdt.Session;
import com.sun.media.jsdt.impl.*;
import java.util.EventObject;

/**
 * The Session event. Session events are created for the following actions:
 * <PRE>
 * - when a ByteArray has been created.
 * - when a ByteArray has been destroyed.
 * - when a Channel has been created.
 * - when a Channel has been destroyed.
 * - when a Token has been created.
 * - when a Token has been destroyed.
 * - when a Client has joined a Session.
 * - when a Client has left a Session.
 * - when a Client has been invited to join a Session.
 * - when a Client has been expelled from a Session.
 * - when a Session has been destroyed.
 * </PRE>
 *
 * @version     2.3 - 20th November 2018
 * @author      Rich Burridge
 * @since       JSDT 1.0
 */

public class
SessionEvent extends EventObject implements JSDTDebugFlags {

    /** The Session ByteArray created event type. */
    public static final int BYTEARRAY_CREATED   = 1 << 0;

    /** The Session ByteArray destroyed event type. */
    public static final int BYTEARRAY_DESTROYED = 1 << 1;

    /** The Session Channel created event type. */
    public static final int CHANNEL_CREATED     = 1 << 2;

    /** The Session Channel destroyed event type. */
    public static final int CHANNEL_DESTROYED   = 1 << 3;

    /** The Session Token created event type. */
    public static final int TOKEN_CREATED       = 1 << 4;

    /** The Session Token destroyed event type. */
    public static final int TOKEN_DESTROYED     = 1 << 5;

    /** The Session joined event type. */
    public static final int JOINED              = 1 << 6;

    /** The Session left event type. */
    public static final int LEFT                = 1 << 7;

    /** The Session invited event type. */
    public static final int INVITED             = 1 << 8;

    /** The Session expelled event type. */
    public static final int EXPELLED            = 1 << 9;

    /** The Session destroyed event type. */
    public static final int DESTROYED           = 1 << 10;


    /** The session associated with this Session event.
     *
     *  @serial
     */
    private final Session session;

    /** The name of the client causing this event.
     *
     *  @serial
     */
    private final String clientName;

    /** The name of the resource within the session that the event affects.
     *
     *  @serial
     */
    private final String resourceName;

    /** The type of this Session event.
     *
     *  @serial
     */
    private final int type;


/**
 * <A NAME="SD_SESSIONEVENT"></A>
 * constructor for the SessionEvent class. A new session event is generated
 * for a client action within the given session.
 *
 * @param session the session in question.
 * @param clientName the name of the client.
 * @param resourceName the name of the resource within the session that the
 * event affects.
 * @param type the type of event.
 */

    public
    SessionEvent(Session session, String clientName,
                 String resourceName, int type) {
        super(session);

        if (SessionEvent_Debug) {
            JSDTObject.Debug("SessionEvent: constructor:" +
                             " session name: "  + session.getName() +
                             " client name: "   + clientName +
                             " resource name: " + resourceName +
                             " type: "          + type);
        }

        this.session      = session;
        this.clientName   = clientName;
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
        if (SessionEvent_Debug) {
            JSDTObject.Debug("SessionEvent: getSession.");
        }

        return(session);
    }


/**
 * <A NAME="SD_GETCLIENTNAME"></A>
 * get the name of the Client that generated this event.
 *
 * @return the name of the Client that generated this event.
 */

    public String
    getClientName() {
        if (SessionEvent_Debug) {
            JSDTObject.Debug("SessionEvent: getClientName.");
        }

        return(clientName);
    }


/**
 * <A NAME="SD_GETRESOURCENAME"></A>
 * get the name of the resource for this event. The resource will be the
 * ByteArray, Channel or Token that the been created or destroyed, or the
 * Session that the Client has just joined, left, been invited to join or
 * expelled from.
 *
 * @return the name of the resource for this event.
 */

    public String
    getResourceName() {
        if (SessionEvent_Debug) {
            JSDTObject.Debug("SessionEvent: getResourceName.");
        }

        return(resourceName);
    }


/**
 * <A NAME="SD_TOSTRING"></A>
 * <EM>toString</EM> print a short description of this Session event.
 *
 * @return a String containing a description of this Session event.
 *
 * @since        JSDT 1.3
 */

    public String
    toString() {
        String typeAsString = null;

        switch (type) {
            case BYTEARRAY_CREATED:   typeAsString = "bytearray created";
                                      break;
            case BYTEARRAY_DESTROYED: typeAsString = "bytearray destroyed";
                                      break;
            case CHANNEL_CREATED:     typeAsString = "channel created";
                                      break;
            case CHANNEL_DESTROYED:   typeAsString = "channel destroyed";
                                      break;
            case TOKEN_CREATED:       typeAsString = "token created";
                                      break;
            case TOKEN_DESTROYED:     typeAsString = "token destroyed";
                                      break;
            case JOINED:              typeAsString = "joined";
                                      break;
            case LEFT:                typeAsString = "left";
                                      break;
            case INVITED:             typeAsString = "invited";
                                      break;
            case EXPELLED:            typeAsString = "expelled";
                                      break;
            case DESTROYED:           typeAsString = "destroyed";
        }

        return("Session event:" + "\n" +
                " session name: "  + session.getName() + "\n" +
                " client name: "   + clientName + "\n" +
                " resource name: " + resourceName + "\n" +
                " type: "          + typeAsString + "\n");
    }
}
