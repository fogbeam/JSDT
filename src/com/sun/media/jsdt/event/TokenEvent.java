
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
import com.sun.media.jsdt.Token;
import com.sun.media.jsdt.impl.*;
import java.util.EventObject;

/**
 * The Token event. Token events are created for the following actions:
 * <PRE>
 * - when a Token has been given from one Client to another.
 * - when a Client has grabbed a Token.
 * - when a Client has inhibited a Token.
 * - when a Client has joined a Token.
 * - when a Client has left a Token.
 * - when a Client has released itself from a Token.
 * - when a Client has requested a Token.
 * - when a Client has been invited to join a Token.
 * - when a Client has been expelled from a Token.
 * </PRE>
 *
 * @version     2.3 - 20th November 2017
 * @author      Rich Burridge
 * @since       JSDT 1.0
 */

public class
TokenEvent extends EventObject implements JSDTDebugFlags {

    /** The Token given event type. */
    public static final int GIVEN     = 1 << 0;

    /** The Token grabbed event type. */
    public static final int GRABBED   = 1 << 1;

    /** The Token inhibited event type. */
    public static final int INHIBITED = 1 << 2;

    /** The Token joined event type. */
    public static final int JOINED    = 1 << 3;

    /** The Token left event type. */
    public static final int LEFT      = 1 << 4;

    /** The Token released event type. */
    public static final int RELEASED  = 1 << 5;

    /** The Token requested event type. */
    public static final int REQUESTED = 1 << 6;

    /** The Token invited event type. */
    public static final int INVITED   = 1 << 7;

    /** The Token expelled event type. */
    public static final int EXPELLED  = 1 << 8;

    /** The session associated with this Token event.
     *
     *  @serial
     */
    private final Session session;

    /** The token this event occured on.
     *
     *  @serial
     */
    protected final Token token;

    /** The name of the client causing this event.
     *
     *  @serial
     */
    private final String clientName;

    /** The type of this Token event.
     *
     *  @serial
     */
    private final int type;


/**
 * <A NAME="SD_TOKENEVENT"></A>
 * constructor for the TokenEvent class. A new token event is generated for
 * a client action for a specific token.
 *
 * @param session the session this channel belongs to.
 * @param clientName the name of the client.
 * @param token the token.
 * @param type the type of event.
 */

    public
    TokenEvent(Session session, String clientName, Token token, int type) {
        super(token);

        if (TokenEvent_Debug) {
            JSDTObject.Debug("TokenEvent: constructor:" +
                             " session name: " + session.getName() +
                             " client name: "  + clientName +
                             " token name: "   + token.getName() +
                             " type: "         + type);
        }

        this.session    = session;
        this.clientName = clientName;
        this.token      = token;
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
        if (SessionEvent_Debug) {
            JSDTObject.Debug("TokenEvent: getSession.");
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
            JSDTObject.Debug("TokenEvent: getClientName.");
        }

        return(clientName);
    }


/**
 * <A NAME="SD_GETTOKEN"></A>
 * get the name of the token for this event.
 *
 * @return the name of the token for this event.
 */

    public Token
    getToken() {
        if (TokenEvent_Debug) {
            JSDTObject.Debug("TokenEvent: getToken.");
        }

        return(token);
    }


/**
 * <A NAME="SD_TOSTRING"></A>
 * <EM>toString</EM> print a short description of this Token event.
 *
 * @return a String containing a description of this Token event.
 *
 * @since        JSDT 1.3
 */

    public String
    toString() {
        String typeAsString = null;

        switch (type) {
            case GIVEN:     typeAsString = "given";
                            break;
            case GRABBED:   typeAsString = "grabbed";
                            break;
            case INHIBITED: typeAsString = "inhibited";
                            break;
            case JOINED:    typeAsString = "joined";
                            break;
            case LEFT:      typeAsString = "left";
                            break;
            case RELEASED:  typeAsString = "released";
                            break;
            case REQUESTED: typeAsString = "requested";
                            break;
            case INVITED:   typeAsString = "invited";
                            break;
            case EXPELLED:  typeAsString = "expelled";
        }

        return("Token event:" + "\n" +
                " session name: " + session.getName() + "\n" +
                " client name: "  + clientName + "\n" +
                " token name: "   + token.getName() + "\n" +
                " type: "         + typeAsString + "\n");
    }
}
