
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

package com.sun.media.jsdt;

import com.sun.media.jsdt.impl.JSDTObject;
import com.sun.media.jsdt.impl.JSDTDebugFlags;

/**
 * JSDT Authentication Information class.
 *
 * This class contains all the information needed by a Client to determine
 * what they are being asked to authenticate.
 *
 * If a JSDT Manageable object (ByteArray, Channel, Session or Token)
 * has a Manager attached to it, any Client that tries to join() that object
 * will be authenticated, and asked to provide a response. If a Client is
 * joined to a managed Session, and wishes to create or destroy a ByteArray,
 * Channel or Token, the same authentication process takes place.
 *
 * @version     2.3 - 25th October 2017
 * @author      Rich Burridge
 * @since       JSDT 1.0
 */

public class
AuthenticationInfo extends JSDTObject implements JSDTDebugFlags {

    /** The ByteArray authentication type. */
    public static final char BYTEARRAY = 1;

    /** The Channel authentication type. */
    public static final char CHANNEL = 2;

    /** The Session authentication type. */
    public static final char SESSION = 3;

    /** The Token authentication type. */
    public static final char TOKEN = 4;

    /** The Registry authentication type. */
    public static final char REGISTRY = 5;


    /** The create ByteArray authentication action. */
    public static final int CREATE_BYTEARRAY  = 1 << 0;

    /** The destroy ByteArray authentication action. */
    public static final int DESTROY_BYTEARRAY = 1 << 1;

    /** The create Channel authentication action. */
    public static final int CREATE_CHANNEL    = 1 << 2;

    /** The destroy Channel authentication action. */
    public static final int DESTROY_CHANNEL   = 1 << 3;

    /** The create Token authentication action. */
    public static final int CREATE_TOKEN      = 1 << 4;

    /** The destroy Token authentication action. */
    public static final int DESTROY_TOKEN     = 1 << 5;

    /** The join authentication action. */
    public static final int JOIN              = 1 << 6;

    /** The create Session authentication action. */
    public static final int CREATE_SESSION    = 1 << 7;

    /** The destroy Session authentication action. */
    public static final int DESTROY_SESSION   = 1 << 8;

    /** The create Client authentication action. */
    public static final int CREATE_CLIENT     = 1 << 9;

    /** The destroy Client authentication action. */
    public static final int DESTROY_CLIENT    = 1 << 10;


    /** The Session associated with this authentication operation.
     *
     *  @serial
     */
    private Session session;

    /** The type of manageable object.
     *
     *  @serial
     */
    private char type = 0;

    /** The name of the ByteArray, Channel or Token being created, or
     *  the name of the ByteArray, Channel, Session or Token being destroyed,
     *  or the name of the manageable object the Client is trying to join.
     *
     *  @serial
     */
    private String name = null;

    /** The authentication action (CREATE*, DESTROY* or JOIN).
     *
     *  @serial
     */
    private int action = 0;

    /** The challenge given by the manager.
     *
     *  @serial
     */
    private Object challenge = null;


/**
 * <A NAME="SD_AUTHENTICATIONINFO"></A>
 * the constructor for the AuthenticationInfo class. A new authentication
 * info object is generated every time a Client tries to perform a
 * priviledged action on a managed object.
 *
 * @param session the Session associated with this authentication operation.
 * @param action the authentication action.
 * @param name the name of the manageable object.
 * @param type the type of the manageable object.
 */

    public
    AuthenticationInfo(Session session, int action, String name, char type) {
        if (AuthenticationInfo_Debug) {
            debug("AuthenticationInfo: constructor:" +
                  " session: " + session +
                  " action: "  + action +
                  " name: "    + name +
                  " type: "    + type);
        }

        this.session = session;
        this.action  = action;
        this.name    = name;
        this.type    = type;
    }


/**
 * <A NAME="SD_GETACTION"></A>
 * get the authentication action (CREATE, DESTROY or JOIN).
 *
 * @return the authentication action.
 */

    public int
    getAction() {
        if (AuthenticationInfo_Debug) {
            debug("AuthenticationInfo: getAction:" +
                  " action: " + action);
        }

        return(action);
    }


/**
 * <A NAME="SD_GETCHALLENGE"></A>
 * get the challenge given by the manager.
 *
 * @return the challenge given by the manager.
 */

    public Object
    getChallenge() {
        if (AuthenticationInfo_Debug) {
            debug("AuthenticationInfo: getChallenge:" +
                  " challenge: " + challenge);
        }

        return(challenge);
    }


/**
 * <A NAME="SD_GETNAME"></A>
 * Get the name of the object associated with this authentication operation.
 * This will be the name of the ByteArray, Channel or Token being created,
 * or the name of the ByteArray, Channel, Session or Token being destroyed,
 * or the name of the manageable object the Client is trying to join.
 *
 * @return the name of the object associated with this authentication operation.
 */

    public String
    getName() {
        if (AuthenticationInfo_Debug) {
            debug("AuthenticationInfo: getName:" +
                  " name: " + name);
        }

        return(name);
    }


/**
 * <A NAME="SD_GETSESSION"></A>
 * get the Session associated with this authentication operation.
 *
 * @return the Session associated with this authentication operation.
 *
 * @since        JSDT 1.2
 */

    public Session
    getSession() {
        if (AuthenticationInfo_Debug) {
            debug("AuthenticationInfo: getSession:" +
                  " session: " + session);
        }

        return(session);
    }


/**
 * <A NAME="SD_GETTYPE"></A>
 * get the type of this manageable object (ByteArray, Channel, Session 
 * or Token).
 *
 * @return the type of this manageable object.
 */

    public char
    getType() {
        if (AuthenticationInfo_Debug) {
            debug("AuthenticationInfo: getType:" +
                  "type: " + type);
        }

        return(type);
    }


/**
 * <A NAME="SD_SETCHALLENGE"></A>
 * set the challenge for this authentication.  This is an operation that
 * is performed by the manager just before it passes the authentication
 * information onto the client.
 *
 * @param challenge the challenge. This object needs to be
 * serializable.
 */

    public void
    setChallenge(Object challenge) {
        if (AuthenticationInfo_Debug) {
            debug("AuthenticationInfo: setChallenge:" +
                  "challenge: " + challenge);
        }

        this.challenge = challenge;
    }


/**
 * <A NAME="SD_TOSTRING"></A>
 * toString print a short description of this ByteArray event.
 *
 * @return a String containing a description of this ByteArray event.
 *
 * @since        JSDT 1.3
 */

    public String
    toString() {
        String actionAsString = null;
        String typeAsString   = null;

        switch (action) {
            case CREATE_BYTEARRAY:  actionAsString = "create bytearray";
                                    break;
            case DESTROY_BYTEARRAY: actionAsString = "destroy bytearray";
                                    break;
            case CREATE_CHANNEL:    actionAsString = "create channel";
                                    break;
            case DESTROY_CHANNEL:   actionAsString = "destroy channel";
                                    break;
            case CREATE_TOKEN:      actionAsString = "create token";
                                    break;
            case DESTROY_TOKEN:     actionAsString = "destroy token";
                                    break;
            case JOIN:              actionAsString = "join";
                                    break;
            case CREATE_SESSION:    actionAsString = "create session";
                                    break;
            case DESTROY_SESSION:   actionAsString = "destroy session";
                                    break;
            case CREATE_CLIENT:     actionAsString = "create client";
                                    break;
            case DESTROY_CLIENT:    actionAsString = "destroy create";
                                    break;
        }

        switch (type) {
            case BYTEARRAY: typeAsString = "bytearray";
                            break;
            case CHANNEL:   typeAsString = "channel";
                            break;
            case SESSION:   typeAsString = "session";
                            break;
            case TOKEN:     typeAsString = "token";
                            break;
            case REGISTRY:  typeAsString = "registry";
        }

        return("AuthenticationInfo:" + "\n" +
               " session name: " +
                 (session != null ? session.getName() : "null") + "\n" +
               " object name: "  + name + "\n" +
               " action: "       + actionAsString + "\n" +
               " type: "         + typeAsString + "\n" +
               " challenge: "    + challenge + "\n");
    }
}
