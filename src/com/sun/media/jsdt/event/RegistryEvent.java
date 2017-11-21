
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

import com.sun.media.jsdt.URLString;
import com.sun.media.jsdt.impl.*;
import java.util.EventObject;

/**
 * The Registry event. Registry events are created for the following actions:
 * <PRE>
 * - when a Session has been created.
 * - when a Session has been destroyed.
 * - when a Client has been created.
 * - when a Client has been destroyed.
 * - when the connection to the registry fails.
 * </PRE>
 *
 * @version     2.3 - 20th November 2017
 * @author      Rich Burridge
 * @since       JSDT 2.0
 */

public class
RegistryEvent extends EventObject implements JSDTDebugFlags {

    /** The Registry connection failed event type. */
    public static final int CONNECTION_FAILED = 1 << 0;

    /** The Registry Session created event type. */
    public static final int SESSION_CREATED   = 1 << 1;

    /** The Registry Session destroyed event type. */
    public static final int SESSION_DESTROYED = 1 << 2;

    /** The Registry Client created event type. */
    public static final int CLIENT_CREATED    = 1 << 3;

    /** The Registry Client destroyed event type. */
    public static final int CLIENT_DESTROYED  = 1 << 4;

    /** The name of the client causing this event.
     *
     *  @serial
     */
    private final String clientName;

    /** The URL string for the resource within the registry that the event
     *  affects.
     *
     *  @serial
     */
    private final URLString resourceName;

    /** The type of this Registry event.
     *
     *  @serial
     */
    private final int type;

    /** The host address that caused the registry event.
     *
     *  @serial
     */
    private final String address;

    /** The port number that caused the registry event.
     *
     *  @serial
     */
    private final int port;


/**
 * <A NAME="SD_REGISTRYEVENT"></A>
 * constructor for the RegistryEvent class. A new registry event is
 * generated for a client action within the given registry.
 *
 * @param clientName the name of the client.
 * @param resourceName the URL String for the resource within the registry
 * that the event affects.
 * @param address the host address of the registry.
 * @param port the port number that the registry uses.
 * @param type the type of event.
 */

    public
    RegistryEvent(String clientName, URLString resourceName,
                  String address, int port, int type) {
        super(address);

        if (RegistryEvent_Debug) {
            JSDTObject.Debug("RegistryEvent: constructor:" +
                             " client name: "   + clientName +
                             " resource name: " + resourceName +
                             " address: "       + address +
                             " port: "          + port +
                             " type: "          + type);
        }

        this.clientName   = clientName;
        this.resourceName = resourceName;
        this.address      = address;
        this.port         = port;
        this.type         = type;
    }


/**
 * <A NAME="SD_GETCLIENTNAME"></A>
 * get the name of the Client that generated this event.
 *
 * @return the name of the Client that generated this event.
 */

    public String
    getClientName() {
        if (RegistryEvent_Debug) {
            JSDTObject.Debug("RegistryEvent: getClientName.");
        }

        return(clientName);
    }


/**
 * <A NAME="SD_GETRESOURCENAME"></A>
 * get the URL String forthe resource for this event. The resource will
 * be Session or Client that has either been created or destroyed.
 *
 * @return the URL String for the resource for this event.
 */

    public URLString
    getResourceName() {
        if (RegistryEvent_Debug) {
            JSDTObject.Debug("RegistryEvent: getResourceName.");
        }

        return(resourceName);
    }


/**
 * <A NAME="SD_GETADDRESS"></A>
 * get the host address that generated this event. This will be an IP
 * address (either unicast or multicast depending upon the implementation
 * type ("socket", "http", "multicast" ...) being used.
 *
 * @return the host address that generated this event.
 */

    public String
    getAddress() {
        if (RegistryEvent_Debug) {
            JSDTObject.Debug("RegistryEvent: getAddress.");
        }

        return(address);
    }


/**
 * <A NAME="SD_GETPORT"></A>
 * get the port number that generated this event.
 *
 * @return the port number that generated this event.
 */

    public int
    getPort() {
        if (RegistryEvent_Debug) {
            JSDTObject.Debug("RegistryEvent: getPort.");
        }

        return(port);
    }


/**
 * <A NAME="SD_TOSTRING"></A>
 * <EM>toString</EM> print a short description of this Registry event.
 *
 * @return a String containing a description of this Registry event.
 */

    public String
    toString() {
        String typeAsString = null;

        switch (type) {
            case CONNECTION_FAILED: typeAsString = "connection failed";
                                    break;
            case SESSION_CREATED:   typeAsString = "session created";
                                    break;
            case SESSION_DESTROYED: typeAsString = "session destroyed";
                                    break;
            case CLIENT_CREATED:    typeAsString = "client created";
                                    break;
            case CLIENT_DESTROYED:  typeAsString = "client destroyed";
        }

        return("Registry event:" + "\n" +
                " client name: "   + clientName   + "\n" +
                " resource name: " + resourceName + "\n" +
                " address: "       + address      + "\n" +
                " port: "          + port         + "\n" +
                " type: "          + typeAsString + "\n");
    }
}
