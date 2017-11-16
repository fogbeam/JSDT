
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

import com.sun.media.jsdt.impl.*;
import java.util.EventObject;

/**
 * The Connection event. Connection events are created for the following
 * actions:
 * <PRE>
 * - when a Connection failure has occured.
 * </PRE>
 *
 * @version     2.3 - 27th October 2017
 * @author      Rich Burridge
 * @since       JSDT 2.0
 */

public class
ConnectionEvent extends EventObject implements JSDTDebugFlags {

    /** The Connection connection failed event type. */
    public static final int CONNECTION_FAILED = 1 << 0;

    /** The host address that caused the connection event.
     *
     *  @serial
     */
    private String address;

    /** The port number that caused the connection event.
     *
     *  @serial
     */
    private int port;

    /** The type of this Connection event.
     *
     *  @serial
     */
    private int type;


/**
 * <A NAME="SD_CONNECTIONEVENT"></A>
 * constructor for the ConnectionEvent class. A new connection event is
 * generated for a failure on (one of) the underlying connection(s).
 *
 * @param address the host address of the connection failure.
 * @param port the port number that the connection failure occured on.
 * @param type the type of event.
 */

    public
    ConnectionEvent(String address, int port, int type) {
        super(address);

        if (ConnectionEvent_Debug) {
            JSDTObject.Debug("ConnectionEvent: constructor:" +
                             " address: " + address +
                             " port: "    + port +
                             " type: "    + type);
        }

        this.address = address;
        this.port    = port;
        this.type    = type;
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
        if (ConnectionEvent_Debug) {
            JSDTObject.Debug("ConnectionEvent: getAddress.");
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
        if (ConnectionEvent_Debug) {
            JSDTObject.Debug("ConnectionEvent: getPort.");
        }

        return(port);
    }


/**
 * <A NAME="SD_TOSTRING"></A>
 * <EM>toString</EM> print a short description of this Connection event.
 *
 * @return a String containing a description of this Connection event.
 */

    public String
    toString() {
        String typeAsString = null;

        switch (type) {
            case CONNECTION_FAILED: typeAsString = "connection failed";
        }

        return("Connection event:" + "\n" +
                " address: " + address + "\n" +
                " port: "    + port + "\n" +
                " type: "    + typeAsString + "\n");
    }
}
