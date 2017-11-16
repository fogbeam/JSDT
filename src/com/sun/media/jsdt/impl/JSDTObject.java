
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

import com.sun.media.jsdt.Connection;
import java.io.*;
import java.net.InetAddress;

/**
 * JSDT root class.
 *
 * @version     2.3 - 6th November 2017
 * @author      Rich Burridge
 */

public class
JSDTObject implements JSDTDebugFlags, Serializable {

    /** The socket factory that was used to create the sockets. */
    protected static JSDTSocketFactory socketFactory;

    /** The time that this JSDT app was started (use for debugging purposes. */
    private static long startTime = System.currentTimeMillis();

    /** The maximum size for UDP datagram packets. */
    protected static final int UDP_BUF_SIZE = 8192;

    /** Indicator to signify a message has been sent in fragments. */
    public static final char T_Fragmented         = '\uFFFF';

    /** Size of the initial fragmentation header (in bytes). */
    public static final int  FRAGMENT_HDRSIZE     = 14;


    /** The type of this implemention (socket, multicast, http ...).
     *
     *  @serial
     */
    private String connectType;

    /** The version string for this JSDT release. */
    public static String versionString = "JSDT 2.3 (EA)";

/** Variables for all implementations. */

    /** Use for debugging purposes. Set to true to print the time in
     *  milliseconds since the start of this application/applet.
     */
    private static boolean showTime = false;

    /** Use for debugging purposes. Set to true to print a message when
     *  an unexpected exception occurs.
     */
    public static boolean showMessage = false;

    /** Use for debugging purposes. Set to true to print a stack trace when
     *  an unexpected exception occurs.
     */
    public static boolean showStack = false;

    /** The time to wait (in milliseconds) for Token give() operations. */
    protected static long giveTime = 15000;

    /** The number of milliseconds to wait before pinging the server to see
     *  if it's still alive.
     */
    protected static long keepAlivePeriod = 5000;

    /** The number of milliseconds to wait for a reply from the server. */
    public static long timeoutPeriod = 15000;

    /** The number of seconds to attempt to try to start the Registry. */
    protected static int registryTime = 60;

    /** The maximum size of the message queue. */
    protected static int maxQueueSize = 15;

    /** The maximum thread pool size. */
    protected static int maxThreadPoolSize = 5;

    /** The port number the Registry should run on. */
    protected static int registryPort = 4561;

    /** The debug print stream. */
    private static PrintStream debugStream = System.err;

    /** Indicates whether we should cleanup pinging Clients, if we haven't
     *  heard from them, in cleanupPeriod milliseconds.
     */
    protected static boolean cleanupPingingClients = true;

    /** The number of milliseconds to wait before cleaning up a pinging
     *  client, that hasn't pinged.
     */
    protected static long cleanupPeriod = 30000;


/** "socket" implementation specific variables. */

    /** The factory class for creating sockets for the "socket" implementation
     *  of JSDT.
     *
     *  For TCP sockets, this should be set to:
     *  "com.sun.media.jsdt.socket.TCPSocketFactory"
     *
     *  For SSL sockets, this should be set to:
     *  "com.sun.media.jsdt.socket.SSLSocketFactory"
     */
    protected static String socketFactoryClass =
                                "com.sun.media.jsdt.socket.TCPSocketFactory";

    /** The location of the SSL keystore file. */
    protected static String SSLKeyStore = "keystore";


/** "http" implementation specific variables. */

    /** The factory class for creating sockets for the "http" implementation
     *  of JSDT.
     *
     *  To automatically first try TCP sockets, then direct HTTP sockets,
     *  then tunneling HTTP sockets, this should be set to:
     *  "com.sun.media.jsdt.http.JSDTMasterSocketFactory"
     *
     *  To just use TCP sockets, this can be set to:
     *  "com.sun.media.jsdt.http.TCPSocketFactory"
     *
     *  To just use direct HTTP sockets, ths can be set to:
     *  "com.sun.media.jsdt.http.HttpToPortSocketFactory"
     *
     *  To just use tunneling HTTP sockets, this can be set to:
     *  "com.sun.media.jsdt.http.HttpToCGISocketFactory"
     */
    protected static String httpFactoryClass =
                            "com.sun.media.jsdt.http.JSDTMasterSocketFactory";

    /** The port number of the web server used by the "http" implementation
     *  which is running a CGI script or Java Servlet, that will "tunnel"
     *  JSDT messages from proxies to the server, and back.
     */
    protected static int httpTunnelPort = 80;

    /** Indicates whether we should always ping for asynchronous messages
     *  irrespective of the socket factory class being used.
     */

    protected static boolean alwaysPing = false;

    /** The number of milliseconds to wait before pinging the server to see
     *  if there are any asynchronous messages buffered there which need to
     *  be retrieved.
     */
    protected static long pingPeriod = 500;

    /** The number of milliseconds to wait before pinging the Registry to see
     *  if there are any asynchronous messages buffered there which need to
     *  be retrieved.
     */
    protected static long registryPingPeriod = 1000;

    /** The relative URI used with the HttpToPortSocketFactory class. */
    protected static String httpURIName = "/";


/** "multicast" implementation specific variables. */

    /** The implementation class for creating the reliable multicast
     *  framework for the "multicast" implementation of JSDT.
     * 
     *  This defaults to JGroups (http://www.jgroups.org):
     *  "com.sun.media.jsdt.multicast.JGroupsImpl"
     *
     *  To use LRMP, this should be set to:
     *  "com.sun.media.jsdt.multicast.LRMPImpl"
     *
     *  To use RMF/RAMP, this should be set to:
     *  "com.sun.media.jsdt.multicast.RMFImpl"
     */
    protected static String multicastImplClass =
                                "com.sun.media.jsdt.multicast.JGroupsImpl";

    /** The registry address to use with the multicast implementation. */
    protected static String registryAddress = "224.1.2.3";

    /** The time-to-live for multicast packets. */
    protected static int TTL = 15;

    /** The expected minimum data rate, in kbits/sec. */
    protected static int minRate = 8;

    /** The expected maximum data rate, in kbits/sec. */
    protected static int maxRate = 64;


    /** The version number for this release. */

    protected static final char version = '\u0007';

    /* Initial message tokens. */

    protected static final char T_Session_No         = '\u00A0';
    protected static final char T_Version            = '\u00A1';

    /* The types of objects that can receive messages. Note that the tokens
     * for ByteArray, Channel, Client, Session and Token are obtained from
     * the implementation of those interfaces.
     */

    protected static final char T_Manager            = '\u00A2';
    protected static final char T_Registry           = '\u00A3';

    /* The various message types. */

    protected static final char T_AddConnection      = '\u00D3';
    protected static final char T_AddConsumer        = '\u00A4';
    protected static final char T_AddListener        = '\u00A5';
    protected static final char T_Authenticate       = '\u00A6';
    protected static final char T_ByteArrayExists    = '\u00A7';
    protected static final char T_ByteArraysJoined   = '\u00A8';
    protected static final char T_Challenge          = '\u00A9';
    protected static final char T_ChangeManagerMask  = '\u00D0';
    protected static final char T_ChannelExists      = '\u00AA';
    protected static final char T_ChannelsJoined     = '\u00AB';
    protected static final char T_Close              = '\u00CB';
    protected static final char T_CreateByteArray    = '\u00AC';
    protected static final char T_CreateChannel      = '\u00AD';
    protected static final char T_CreateSession      = '\u00D5';
    protected static final char T_CreateToken        = '\u00AE';
    protected static final char T_DataReceived       = '\u00AF';
    protected static final char T_DestroyByteArray   = '\u00B2';
    protected static final char T_DestroyChannel     = '\u00B3';
    protected static final char T_DestroyClient      = '\u00D1';
    protected static final char T_DestroySession     = '\u00B4';
    protected static final char T_DestroyToken       = '\u00B5';
    protected static final char T_Expel              = '\u00B6';
    protected static final char T_GetMessage         = '\u00B7';
    protected static final char T_GetSessionNo       = '\u00D6';
    protected static final char T_Give               = '\u00B8';
    protected static final char T_Grab               = '\u00B9';
    protected static final char T_InformListener     = '\u00BA';
    protected static final char T_Invite             = '\u00BB';
    protected static final char T_IsAlive            = '\u00D2';
    protected static final char T_IsManaged          = '\u00CE';
    protected static final char T_Join               = '\u00BC';
    protected static final char T_Leave              = '\u00BD';
    protected static final char T_ListByteArrayNames = '\u00BE';
    protected static final char T_ListChannelNames   = '\u00BF';
    protected static final char T_ListClientNames    = '\u00C0';
    protected static final char T_ListConsumerNames  = '\u00CC';
    protected static final char T_ListHolderNames    = '\u00CD';
    protected static final char T_ListTokenNames     = '\u00C1';
    protected static final char T_Permanent          = '\u00EF';
    protected static final char T_Release            = '\u00C2';
    protected static final char T_RemoveConnection   = '\u00D4';
    protected static final char T_RemoveConsumer     = '\u00B0';
    protected static final char T_RemoveListener     = '\u00B1';
    protected static final char T_Request            = '\u00C3';
    protected static final char T_Send               = '\u00C4';
    protected static final char T_SetValue           = '\u00C5';
    protected static final char T_Test               = '\u00C6';
    protected static final char T_TokenExists        = '\u00C7';
    protected static final char T_TokenGiven         = '\u00CF';
    protected static final char T_TokensJoined       = '\u00C8';
    protected static final char T_ValueChanged       = '\u00C9';
    protected static final char T__Manager           = '\u00CA';

    /* The possible registry messages. */

    protected static final char T_Bind               = '\u00D7';
    protected static final char T_Unbind             = '\u00D9';
    protected static final char T_Lookup             = '\u00DA';
    protected static final char T_List               = '\u00DB';
    protected static final char T_Exists             = '\u00DC';
    protected static final char T_Stop               = '\u00DD';


    public final String
    actionToString(char action) {
        if (JSDTObject_Debug) {
            debug("JSDTObject: actionToString:" +
                  " action: " + action);
        }

        switch (action) {
            case T_AddConnection:      return("AddConnection");
            case T_AddConsumer:        return("AddConsumer");
            case T_AddListener:        return("AddListener");
            case T_Authenticate:       return("Authenticate");
            case T_ByteArrayExists:    return("ByteArrayExists");
            case T_ByteArraysJoined:   return("ByteArraysJoined");
            case T_Challenge:          return("Challenge");
            case T_ChangeManagerMask:  return("ChangeManagerMask");
            case T_ChannelExists:      return("ChannelExists");
            case T_ChannelsJoined:     return("ChannelsJoined");
            case T_Close:              return("Close");
            case T_CreateByteArray:    return("CreateByteArray");
            case T_CreateChannel:      return("CreateChannel");
            case T_CreateSession:      return("CreateSession");
            case T_CreateToken:        return("CreateToken");
            case T_DataReceived:       return("DataReceived");
            case T_DestroyByteArray:   return("DestroyByteArray");
            case T_DestroyChannel:     return("DestroyChannel");
            case T_DestroyClient:      return("DestroyClient");
            case T_DestroySession:     return("DestroySession");
            case T_DestroyToken:       return("DestroyToken");
            case T_Expel:              return("Expel");
            case T_GetMessage:         return("GetMessage");
            case T_GetSessionNo:       return("GetSessionNo");
            case T_Give:               return("Give");
            case T_Grab:               return("Grab");
            case T_InformListener:     return("InformListener");
            case T_Invite:             return("Invite");
            case T_IsAlive:            return("IsAlive");
            case T_IsManaged:          return("IsManaged");
            case T_Join:               return("Join");
            case T_Leave:              return("Leave");
            case T_ListByteArrayNames: return("ListByteArrayNames");
            case T_ListChannelNames:   return("ListChannelNames");
            case T_ListClientNames:    return("ListClientNames");
            case T_ListConsumerNames:  return("ListConsumerNames");
            case T_ListHolderNames:    return("ListHolderNames");
            case T_ListTokenNames:     return("ListTokenNames");
            case T_Permanent:          return("Permanent");
            case T_Release:            return("Release");
            case T_RemoveConnection:   return("REmoveConnection");
            case T_RemoveConsumer:     return("RemoveConsumer");
            case T_RemoveListener:     return("RemoveListener");
            case T_Request:            return("Request");
            case T_Send:               return("Send");
            case T_SetValue:           return("SetValue");
            case T_Test:               return("Test");
            case T_TokenExists:        return("TokenExists");
            case T_TokenGiven:         return("TokenGiven");
            case T_TokensJoined:       return("TokensJoined");
            case T_ValueChanged:       return("ValueChanged");
            case T__Manager:           return("_Manager");

            case T_Bind:               return("Bind");
            case T_Unbind:             return("Unbind");
            case T_Lookup:             return("Lookup");
            case T_List:               return("List");
            case T_Exists:             return("Exists");
            case T_Stop:               return("Stop");
        }
        if (displayMessage()) {
            debug("JSDTObject: actionToString:" +
                  JSDTI18N.getResource("impl.unknown.action") + action);
        }
        printStack();

        return("Unknown");
    }


/**
 * <A NAME="SD_DEBUG"></A>
 * <EM>Debug</EM> print this debug message to the debug print stream (which
 * is System.err by default).
 *
 * @param output the debug message to display.
 */

    public static void
    Debug(String output) {
        boolean     displayTime = Util.getBooleanProperty("showTime", showTime);
        PrintStream stream = getDebugStream();

        if (displayTime) {
            stream.print((System.currentTimeMillis() - startTime) + ": ");
        }
        stream.println(output);
    }



/**
 * <A NAME="SD_DEBUG"></A>
 * <EM>debug</EM> print this debug message to the debug print stream (which
 * is System.err by default).
 *
 * @param output the debug message to display.
 */

    public final void
    debug(String output) {
        Debug(output);
    }


/**
 * <A NAME="SD_ERROR"></A>
 * <EM>error</EM> print out details of the exception that has occured.
 *
 * @param s the class and method generating this error.
 * @param e the exception that has occured.
 */

    public final void
    error(String s, Exception e) {
        if (displayMessage()) {
            debug(s + e.getMessage());
        }
        if (displayStack()) {
            e.printStackTrace(getDebugStream());
        }
    }


    public final void
    error(String s, String message) {
        if (displayMessage()) {
            debug(s + JSDTI18N.getResource(message));
        }
        printStack();
    }


    public final void
    error(String s, String message, Object o) {
        if (displayMessage()) {
            debug(s + JSDTI18N.getResource(message) + o);
        }
        printStack();
    }


    public final void
    error(String s, String message, int val) {
        if (displayMessage()) {
            debug(s + JSDTI18N.getResource(message) + val);
        }
        printStack();
    }


/**
 * <A NAME="SD_ERROR"></A>
 * <EM>Error</EM> print out details of the exception that has occured.
 *
 * @param s the class and method generating this error.
 * @param e the exception that has occured.
 */

    public static void
    Error(String s, Exception e) {
        if (displayMessage()) {
            Debug(s + e.getMessage());
        }
        if (displayStack()) {
            e.printStackTrace(getDebugStream());
        }
    }


/**
 * <A NAME="SD_ERROR"></A>
 * <EM>Error</EM> print out details of the error that has occured.
 *
 * @param s the class and method generating this error.
 * @param message the error message to display.
 * @param o more information on the error.
 */

    public static void
    Error(String s, String message, Object o) {
        if (displayMessage()) {
            Debug(s + JSDTI18N.getResource(message) + o);
        }
        printStack();
    }


    protected final void
    waited(String s, char action) {
        if (displayMessage()) {
            debug(s + JSDTI18N.getResource("impl.action") +
                  actionToString(action) +
                  JSDTI18N.getResource("impl.should.have.waited"));
        }
        printStack();
    }


/**
 * <A NAME="SD_PRINTSTACK"></A>
 * <EM>printStack</EM> print the current threads stack trace to the JSDT
 * debug stream.
 */

    private static void
    printStack() {
        if (displayStack()) {
            new Exception("Stack trace").printStackTrace(getDebugStream());
        }
    }


    private static boolean
    displayMessage() {
        return(Util.getBooleanProperty("showMessage", showMessage));
    }


    private static boolean
    displayStack() {
        return(Util.getBooleanProperty("showStack", showStack));
    }


    protected static PrintStream
    getDebugStream() {
        String      value;
        PrintStream stream = debugStream;

        if ((value = Connection.getProperty("debugStream")) != null) {
            try {
                stream = (PrintStream)
                                Util.getClassForName(value).newInstance();
            } catch (Exception e) {
            }
        }

        return(stream);
    }


    public final String
    typeToString(char type) {
        if (JSDTObject_Debug) {
            debug("JSDTObject: typeToString:" +
                  " type: " + type);
        }

        switch (type) {
            case ByteArrayImpl.M_ByteArray: return("ByteArray");
            case SessionImpl.M_Session:     return("Session");
            case ChannelImpl.M_Channel:     return("Channel");
            case TokenImpl.M_Token:         return("Token");
            case ClientImpl.M_Client:       return("Client");
            case T_Registry:                return("Registry");
            case T_Manager:                 return("Manager");
        }
        if (displayMessage()) {
            debug("JSDTObject: typeToString:" +
                  JSDTI18N.getResource("impl.unknown.type") + type);
        }
        printStack();

        return("Unknown");
    }


/**
 * <A NAME="SD_GETCONNECTIONTYPE"></A>
 * <EM>getConnectionType</EM> get the connection type of this JSDT object.
 *
 * @return the connection type of this JSDT object.
 */

    public String
    getConnectionType() {
        if (JSDTObject_Debug) {
            debug("JSDTObject: getConnectionType.");
        }

        return(connectType);
    }


/**
 * <A NAME="SD_SETCONNECTIONTYPE"></A>
 * <EM>setConnectionType</EM> set the connection type of this JSDT object.
 *
 * @param connectType the connection type of this JSDT object.
 */

    public void
    setConnectionType(String connectType) {
        if (JSDTObject_Debug) {
            debug("JSDTObject: setConnectionType:" +
                  " connect type: " + connectType);
        }

        this.connectType = connectType;
    }


/**
 * <A NAME="SD_UNIQUENAME"></A>
 * <EM>uniqueName</EM> generates a unique name for object based on its name,
 * it's hashcode and the host it's running on.
 *
 * @param object the object in question.
 * @param name the name of this object.
 *
 * @return a String which is a unique name based on the name/object for this
 * host
 */

    public final String
    uniqueName(Object object, String name) {
        String newName = null;

        if (JSDTObject_Debug) {
            debug("JSDTObject: uniqueName:" +
                  " object: " + object +
                  " name: "   + name);
        }

        try {
            newName = name + '\t' + InetAddress.getLocalHost() + '\t' +
                      object.hashCode();
        } catch (Exception e) {
            error("JSDTObject: uniqueName: ", e);
        }

        return(newName);
    }
}
