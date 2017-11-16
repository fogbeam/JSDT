
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

import com.sun.media.jsdt.impl.*;
import java.io.Serializable;

/**
 * <CODE>JSDTException</CODE> is the super class of all JSDT exceptions.
 * It is provided as a means for catching all JSDT exceptions without
 * having to catch them individually. It provides convenience methods to
 * determine what type of exception has been thrown.
 *
 * @version     2.3 - 26th October 2017
 * @author      Rich Burridge
 * @since       JSDT 1.0
 */

public class
JSDTException extends Exception implements JSDTDebugFlags, Serializable {

    /** The unknown exception type. */
    public static final int UNKNOWN             = 1000;

    /** The no such ByteArray exception type. */
    public static final int NO_SUCH_BYTEARRAY   = 1001;

    /** The no such Channel exception type. */
    public static final int NO_SUCH_CHANNEL     = 1002;

    /** The no such Client exception type. */
    public static final int NO_SUCH_CLIENT      = 1003;

    /** The no such Consumer exception type. */
    public static final int NO_SUCH_CONSUMER    = 1004;

    /** The no such Host exception type. */
    public static final int NO_SUCH_HOST        = 1005;

    /** The no such Listener exception type. */
    public static final int NO_SUCH_LISTENER    = 1006;

    /** The no such Session exception type. */
    public static final int NO_SUCH_SESSION     = 1007;

    /** The no such Token exception type. */
    public static final int NO_SUCH_TOKEN       = 1008;

    /** The manager exists exception type. */
    public static final int MANAGER_EXISTS      = 1009;

    /** The permission denied exception type. */
    public static final int PERMISSION_DENIED   = 1010;

    /** The Client not grabbing exception type. */
    public static final int CLIENT_NOT_GRABBING = 1011;

    /** The Client not released exception type. */
    public static final int CLIENT_NOT_RELEASED = 1012;

    /** The invalid URL exception type. */
    public static final int INVALID_URL         = 1013;

    /** The no Registry exception type. */
    public static final int NO_REGISTRY         = 1014;

    /** The already bound exception type. */
    public static final int ALREADY_BOUND       = 1015;

    /** The not bound exception type. */
    public static final int NOT_BOUND           = 1016;

    /** The name in use exception type. */
    public static final int NAME_IN_USE         = 1017;

    /** The Registry exists exception type. */
    public static final int REGISTRY_EXISTS     = 1018;

    /** The Connection error exception type. */
    public static final int CONNECTION_ERROR    = 1019;

    /** The port in use exception type. */
    public static final int PORT_IN_USE         = 1020;

    /** The timed out exception type. */
    public static final int TIMED_OUT           = 1021;

    /** The no such Manager exception type. */
    public static final int NO_SUCH_MANAGER     = 1022;

    /** The invalid Client exception type. */
    public static final int INVALID_CLIENT      = 1023;


    /** The type of this JSDT exception.
     *
     *  @serial
     */
    int type;


/**
 * <A NAME="SD_EXCEPTION"></A>
 * thrown if there is any kind of error in processing a JSDT request.
 */

    public
    JSDTException() {
        super();

        if (JSDTException_Debug) {
            JSDTObject.Debug("JSDTException: constructor.");
        }
        type = UNKNOWN;
    }


/**
 * <A NAME="SD_EXCEPTIONBYNAME"></A>
 * thrown if there is any kind of error in processing a JSDT request.
 *
 * @param s a string describing the exception thrown.
 */

    public
    JSDTException(String s) {
        super(s);

        if (JSDTException_Debug) {
            JSDTObject.Debug("JSDTException: constructor:" +
                             " string: " + s);
        }

        type = UNKNOWN;
    }


/**
 * <A NAME="SD_EXCEPTIONBYTYPE"></A>
 * thrown if there is any kind of error in processing a JSDT request.
 *
 * @param type the type of this JSDT exception.
 */

    public
    JSDTException(int type) {
        super();

        if (JSDTException_Debug) {
            JSDTObject.Debug("JSDTException: constructor:" +
                             " type: " + type);
        }

        this.type = type;
    }


/**
 * <A NAME="SD_GETTYPE"></A>
 * the type of this JSDT exception.
 *
 * @return the type of this JSDT exception.
 */

    public int
    getType() {
        if (JSDTException_Debug) {
            JSDTObject.Debug("JSDTException: getType:" +
                             " type: " + type);
        }

        return(type);
    }


/**
 * <A NAME="SD_TOSTRING"></A>
 * converts this JSDT exception into a string describing it.
 *
 * @return a String describing this JSDT exception.
 */

    public String
    toString() {
        if (JSDTException_Debug) {
            JSDTObject.Debug("JSDTException: toString.");
        }

        return(typeToString(type));
    }


/**
 * <A NAME="SD_TYPETOSTRING"></A>
 * <EM>typeToString</EM> converts an exception type into a String
 *  describing it.
 *
 * @param type the exception type.
 *
 * @return a String describing this type of exception.
 */

    public String
    typeToString(int type) {
        if (JSDTException_Debug) {
            JSDTObject.Debug("JSDTException: typeToString:" +
                             " type: " + type);
        }

        switch (type) {
            case NO_SUCH_BYTEARRAY:
                return(JSDTI18N.getResource("exception.no.such.byte.array"));
            case NO_SUCH_CHANNEL:
                return(JSDTI18N.getResource("exception.no.such.channel"));
            case NO_SUCH_CLIENT:
                return(JSDTI18N.getResource("exception.no.such.client"));
            case NO_SUCH_CONSUMER:
                return(JSDTI18N.getResource("exception.no.such.consumer"));
            case NO_SUCH_HOST:
                return(JSDTI18N.getResource("exception.no.such.host"));
            case NO_SUCH_LISTENER:
                return(JSDTI18N.getResource("exception.no.such.listener"));
            case NO_SUCH_SESSION:
                return(JSDTI18N.getResource("exception.no.such.session"));
            case NO_SUCH_TOKEN:
                return(JSDTI18N.getResource("exception.no.such.token"));
            case MANAGER_EXISTS:
                return(JSDTI18N.getResource("exception.manager.exists"));
            case PERMISSION_DENIED:
                return(JSDTI18N.getResource("exception.permission.denied"));
            case CLIENT_NOT_GRABBING:
                return(JSDTI18N.getResource("exception.client.not.grabbing"));
            case CLIENT_NOT_RELEASED:
                return(JSDTI18N.getResource("exception.client.not.released"));
            case INVALID_URL:
                return(JSDTI18N.getResource("exception.invalid.url"));
            case NO_REGISTRY:
                return(JSDTI18N.getResource("exception.no.registry"));
            case ALREADY_BOUND:
                return(JSDTI18N.getResource("exception.already.bound"));
            case NOT_BOUND:
                return(JSDTI18N.getResource("exception.not.bound"));
            case NAME_IN_USE:
                return(JSDTI18N.getResource("exception.name.in.use"));
            case CONNECTION_ERROR:
                return(JSDTI18N.getResource("exception.connection.error"));
            case PORT_IN_USE:
                return(JSDTI18N.getResource("exception.port.in.use"));
            case TIMED_OUT:
                return(JSDTI18N.getResource("exception.timed.out"));
            case NO_SUCH_MANAGER:
                return(JSDTI18N.getResource("exception.no.such.manager"));
            case INVALID_CLIENT:
                return(JSDTI18N.getResource("exception.invalid.client"));
        }
        return(JSDTI18N.getResource("exception.unknown"));
    }
}
