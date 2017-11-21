
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
import com.sun.media.jsdt.ByteArray;
import com.sun.media.jsdt.impl.*;
import java.util.EventObject;

/**
 * The ByteArray event. ByteArray events are created for the following actions:
 * <PRE>
 * - when a Client has joined a ByteArray.
 * - when a Client has left a ByteArray.
 * - when the value of a ByteArray changes.
 * - when a Client has been invited to join a ByteArray.
 * - when a Client has been expelled from a ByteArray.
 * </PRE>
 *
 * @version     2.3 - 20th November 2017
 * @author      Rich Burridge
 * @since       JSDT 1.0
 */

public class
ByteArrayEvent extends EventObject implements JSDTDebugFlags {

    /** The ByteArray joined event type. */
    public static final int JOINED        = 1 << 0;

    /** The ByteArray left event type. */
    public static final int LEFT          = 1 << 1;

    /** The ByteArray value changed event type. */
    public static final int VALUE_CHANGED = 1 << 2;

    /** The ByteArray invited event type. */
    public static final int INVITED       = 1 << 3;

    /** The ByteArray expelled event type. */
    public static final int EXPELLED      = 1 << 4;

    /** The session associated with this ByteArray event.
     *
     *  @serial
     */
    private final Session session;

    /** The byte array associated with this ByteArray event.
     *
     *  @serial
     */
    private final ByteArray byteArray;

    /** The name of the client causing this event.
     *
     *  @serial
     */
    private final String clientName;

    /** The type of this ByteArray event.
     *
     *  @serial
     */
    private final int type;


/**
 * <A NAME="SD_BYTEARRAYEVENT"></A>
 * constructor for the ByteArrayEvent class. A new byte array event is
 * generated for a client action associated with the given byte array
 * within the given session.
 *
 * @param session the session associated with this event.
 * @param clientName the name of the client.
 * @param byteArray the byte array associated with this event.
 * @param type the type of event.
 */

    public
    ByteArrayEvent(Session session, String clientName,
                   ByteArray byteArray, int type) {
        super(byteArray);

        if (ByteArrayEvent_Debug) {
            JSDTObject.Debug("ByteArrayEvent: constructor:" +
                             " session name: " + session.getName() +
                             " client name: "  + clientName +
                             " byte array: "   + byteArray +
                             " type: "         + type);
        }

        this.session    = session;
        this.clientName = clientName;
        this.byteArray  = byteArray;
        this.type       = type;
    }


/**
 * <A NAME="SD_GETSESSION"></A>
 * get the Session associated with this event.
 *
 * @return the Session associated with this event.
 */

    public Session
    getSession() {
        if (ByteArrayEvent_Debug) {
            JSDTObject.Debug("ByteArrayEvent: getSession.");
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
        if (ByteArrayEvent_Debug) {
            JSDTObject.Debug("ByteArrayEvent: getClientName.");
        }

        return(clientName);
    }


/**
 * <A NAME="SD_GETBYTEARRAY"></A>
 * get the ByteArray associated with this event.
 *
 * @return the ByteArray associated with this event.
 */

    public ByteArray
    getByteArray() {
        if (ByteArrayEvent_Debug) {
            JSDTObject.Debug("ByteArrayEvent: getByteArray.");
        }

        return(byteArray);
    }


/**
 * <A NAME="SD_TOSTRING"></A>
 * <EM>toString</EM> print a short description of this ByteArray event.
 *
 * @return a String containing a description of this ByteArray event.
 *
 * @since        JSDT 1.3
 */

    public String
    toString() {
        String typeAsString = null;

        switch (type) {
            case JOINED:        typeAsString = "joined";
                                break;
            case LEFT:          typeAsString = "left";
                                break;
            case VALUE_CHANGED: typeAsString = "value changed";
                                break;
            case INVITED:       typeAsString = "invited";
                                break;
            case EXPELLED:      typeAsString = "expelled";
        }

        return("ByteArray event:" + "\n" +
                " session name: "   + session.getName() + "\n" +
                " client name: "    + clientName + "\n" +
                " bytearray name: " + byteArray.getName() + "\n" +
                " type: "           + typeAsString + "\n");
    }
}
