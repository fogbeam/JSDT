
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
import java.net.InetAddress;
import java.util.StringTokenizer;

/**
 * JSDT URL String parsing class. A JSDT URL is of the form:
 *
 * <PRE>
 *     jsdt://&#60host&#62:&#60port&#62/&#60connection type&#62/&#60object type&#62/&#60object name&#62
 *
 * where <connection type> is the connection (or implementation) type
 * (eg. "socket", "multicast" or "http"),
 * and where valid object types are "Session" and "Client".
 *
 * So for example:
 *     bind("jsdt://stard:3355/socket/Session/chatSession", chatSession);
 *
 *     bind("jsdt://stard:4386/socket/Client/fredClient", fredClient);
 * </PRE>
 *
 * Static convenience methods are provided to create Session and Client
 * URLStrings
 *
 * @version     2.3 - 27th October 2017
 * @author      Rich Burridge
 * @since       JSDT 1.5
 */

public class
URLString extends JSDTObject {

    /** The URL protocol (jsdt:).
     *
     *  @serial
     */
    private String protocol;

    /** The URL host name.
     *
     *  @serial
     */
    private String hostName;

    /** The URL host IP address.
     *
     *  @serial
     */
    private String hostAddress;

    /** The URL port number.
     *
     *  @serial
     */
    private int port;

    /** The connection type for this Session/Client.
     *
     *  @serial
     */
    private String connectType;

    /** The URL object type (Session or Client).
     *
     *  @serial
     */
    private String objectType;

    /** The URL object name.
     *
     *  @serial
     */
    private String objectName ;

    /** The validity of the URLString. True if successfully broken down.
     *
     *  @serial
     */
    private boolean valid = false;


/**
 * <A NAME="SD_URLSTRING"></A>
 * <EM>URLString</EM> is a constructor for the URLString class. The URLString
 * constructor takes a JSDT URL String and divides it up into its component
 * parts.
 *
 * @param url the JSDT URL string to be broken down.
 */

    public
    URLString(String url) {
        StringTokenizer tok = new StringTokenizer(url, "/");

        if (URLString_Debug) {
            debug("URLString: constructor:" +
                  " url: " + url);
        }

        try {
            JSDTSecurity.enablePrivilege.invoke(JSDTSecurity.privilegeManager,
                                                JSDTSecurity.connectArgs);
        } catch (Exception e) {
        }

        try {
            InetAddress     address;
            String          hostport;
            StringTokenizer st;

            protocol    = tok.nextToken();                        /* "jsdt". */

            hostport    = tok.nextToken();              /* Server host:port. */
            st = new StringTokenizer(hostport, ":");
            hostName    = st.nextToken();                    /* Server host. */
            address     = InetAddress.getByName(hostName);
            hostAddress = address.getHostAddress();    /* Server IP address. */
            port        = Integer.parseInt(st.nextToken());         /* Port. */

            connectType = tok.nextToken();               /* Connection type. */
            objectType  = tok.nextToken();                   /* Object type. */
            objectName  = tok.nextToken();                   /* Object name. */
            valid       = true;

            if (URLString_Debug) {
                debug("URLString: constructor:" +
                      " protocol: "        + protocol +
                      " host name: "       + hostName +
                      " host adddress: "   + hostAddress +
                      " port number: "     + port +
                      " connection type: " + connectType +
                      " object type: "     + objectType +
                      " object name: "     + objectName);
            }
        } catch (Exception e) {
            valid = false;
        }
    }


/**
 * <A NAME="SD_CREATESESSIONURL"></A>
 * <EM>createSessionURL</EM> is a class method that will create a JSDT Session
 * URL given it's component parts. This URL can then be used in conjunction
 * with the <CODE>createSession</CODE> methods in the SessionFactory class.
 *
 * @param hostName the host name for the server for this Session.
 * @param port the port number the server is running on.
 * @param connectionType the connection (implementation) type of this Session.
 * @param sessionName the name of this Session.
 *
 * @since       JSDT 1.5
 */

    public static URLString
    createSessionURL(String hostName, int port,
                     String connectionType, String sessionName) {
        String url = "jsdt://" + hostName + ":" + port + "/" +
                     connectionType + "/Session/" + sessionName;

        if (URLString_Debug) {
            Debug("URLString: createSessionURL:" +
                  " host name: "       + hostName +
                  " port number: "     + port +
                  " connection type: " + connectionType +
                  " session name: "    + sessionName);
        }

        return(new URLString(url));
    }


/**
 * <A NAME="SD_CREATECLIENTURL"></A>
 * <EM>createClientURL</EM> is a class method that will create a JSDT Client
 * URL given it's component parts. This URL can then be used in conjunction
 * with the <CODE>createClient</CODE> method in the ClientFactory class.
 *
 * @param hostName the host name for the server for this Client.
 * In other words, the name of the machine that is running the Registry
 * where this special JSDT Client will be bound.
 * @param port the port number the Client is running on. This is a port on
 * the local machine; not the port number of the Registry.
 * @param connectionType the connection (implementation) type of this Client.
 * @param clientName the name of this Client.
 *
 * @since       JSDT 1.5
 */

    public static URLString
    createClientURL(String hostName, int port,
                    String connectionType, String clientName) {
        String url = "jsdt://" + hostName + ":" + port + "/" +
                     connectionType + "/Client/" + clientName;

        if (URLString_Debug) {
            Debug("URLString: createClientURL:" +
                  " host name: "       + hostName +
                  " port number: "     + port +
                  " connection type: " + connectionType +
                  " client name: "     + clientName);
        }

        return(new URLString(url));
    }


/**
 * <A NAME="SD_GETHOSTADDRESS"></A>
 * <EM>getHostAddress</EM> get the host IP address for the server for
 * this object.
 *
 * @return the host IP address of the server for this object.
 */

    public String
    getHostAddress() {
        if (URLString_Debug) {
            debug("URLString: getHostAddress.");
        }

        return(hostAddress);
    }


/**
 * <A NAME="SD_GETHOSTNAME"></A>
 * <EM>getHostName</EM> get the host name for the server for this object.
 *
 * @return the host name of the server for this object.
 */

    public String
    getHostName() {
        if (URLString_Debug) {
            debug("URLString: getHostName.");
        }

        return(hostName);
    }


/**
 * <A NAME="SD_GETPROTOCOL"></A>
 * <EM>getProtocol</EM> get the protocol portion of the given URL String.
 *
 * @return the port portion of the given URL String.
 */

    public String
    getProtocol() {
        if (URLString_Debug) {
            debug("URLString: getProtocol.");
        }

        return(protocol);
    }


/**
 * <A NAME="SD_GETPORT"></A>
 * <EM>getPort</EM> get the port number being used by the server for this
 * object.
 *
 * @return the port number being used by the server for this object.
 */

    public int
    getPort() {
        if (URLString_Debug) {
            debug("URLString: getPort.");
        }

        return(port);
    }


/**
 * <A NAME="SD_GETCONNECTIONTYPE"></A>
 * <EM>getConnectionType</EM> get the connection type of this Session/Client.
 *
 * @return the connection type of this Session/Client.
 */

    public String
    getConnectionType() {
        if (URLString_Debug) {
            debug("URLString: getConnectionType.");
        }

        return(connectType);
    }


/**
 * <A NAME="SD_GETOBJECTTYPE"></A>
 * <EM>getObjectType</EM> get the type of this object (Session or Client).
 *
 * @return the type of this object (Session or client).
 */

    public String
    getObjectType() {
        if (URLString_Debug) {
            debug("URLString: getObjectType.");
        }

        return(objectType);
    }


/**
 * <A NAME="SD_GETOBJECTNAME"></A>
 * <EM>getObjectName</EM> get the name of this object.
 *
 * @return the name of this object.
 */

    public String
    getObjectName() {
        if (URLString_Debug) {
            debug("URLString: getObjectName.");
        }

        return(objectName);
    }


/**
 * <A NAME="SD_ISVALID"></A>
 * <EM>isValid</EM> get the validity of this URLString.
 *
 * @return an indication of whether this URLString is valid.
 */

    public boolean
    isValid() {
        if (URLString_Debug) {
            debug("URLString: isValid.");
        }

        return(valid);
    }


/**
 * <A NAME="SD_TOSTRING"></A>
 * <EM>toString</EM> print this URLString object as a JSDT String URL.
 *
 * @return this URLString object as a JSDT String URL.
 *
 * @since       JSDT 1.5
 */

    public String
    toString() {
        return("" + protocol + "//" + hostName + ":" + port + "/" +
               connectType + "/" + objectType + "/" + objectName);
    }


/**
 * <A NAME="SD_EQUALS"></A>
 * Compares this URLString to the specified object.
 * The result is <CODE>true</CODE> if and only if the argument is not
 * <CODE>null</CODE> and is a <CODE>URLString</CODE> object that represents
 * the same JSDT URL as this object.
 *  
 * @param anObject the object to compare this <CODE>URLString</CODE> against.
 *
 * @return <CODE>true</CODE>if the<CODE>URLString</CODE> are equal;
 *         <CODE>false</CODE> otherwise.
 *
 * @since      JSDT 2.0
 */ 

    public boolean
    equals(Object anObject) {
        boolean reply = false;

        if (this == anObject) {
            reply = true;
        } else if ((anObject != null) && (anObject instanceof URLString)) {
            URLString u2 = (URLString) anObject;

            try {
                String IPString1 = Util.getIPAddress(getHostName());
                String IPString2 = Util.getIPAddress(u2.getHostName());

                String u1String = "jsdt://" + IPString1 + ":" +
                                   getPort() + "/" +
                                   getConnectionType() + "/" +
                                   getObjectType() + "/" + getObjectName();
                String u2String = "jsdt://" + IPString2 + ":" +
                                   u2.getPort() + "/" +
                                   u2.getConnectionType() + "/" +
                                   u2.getObjectType() + "/" + u2.getObjectName();

                reply = (u1String.equals(u2String));
            } catch (InvalidURLException e) {
            }
        }

        return(reply);
    }
}
