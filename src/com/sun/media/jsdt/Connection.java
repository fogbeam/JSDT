
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
import com.sun.media.jsdt.event.ConnectionListener;
import java.util.Properties;

/**
 * The Connection class..
 *
 * @version     2.3 - 26th October 2017
 * @author      Rich Burridge
 * @since       JSDT 2.0
 */

public class
Connection extends JSDTObject {

  /** The JSDT property list. */
  protected static Properties properties = new Properties();


/**
 * <A NAME="SD_GETPROPERTIES"></A>
 * Get the current JSDT property list. These property values override
 * the default values in <CODE>com.sun.media.jsdt.impl.JSDTObject</CODE>
 * and are used by each JSDT implementation. Initially this property set
 * is empty.
 *
 * Information of the specific properties used by each implementation can
 * be found in the Implementations chapter in the JSDT User Guide.
 */

    public static Properties
    getProperties() {
        if (Connection_Debug) {
            Debug("Connection: getProperties.");
        }

        return(properties);
    }


/**
 * <A NAME="SD_GETPROPERTY"></A>
 * Searches for the implementation property with the specified key in the
 * JSDT property list. If the key is not found in this property list, then
 * the default value (from <CODE>com.sun.media.jsdt.impl.JSDTObject</CODE>
 * is used by the implementation.
 *
 * Information of the specific properties used by each implementation can
 * be found in the Implementations chapter in the JSDT User Guide.
 *
 * @param key the property key.
 *
 * @return the value in this property list with the specified key value or
 * <CODE>null</CODE> if the property is not found.
 */

    public static String
    getProperty(String key) {
        return(properties.getProperty(key));
    }


/**
 * <A NAME="SD_SETPROPERTIES"></A>
 * Replaces the current JSDT property list with the given set of properties.
 * These property values will be used by the implementation instead of the
 * default values in <CODE>com.sun.media.jsdt.impl.JSDTObject</CODE>.
 *
 * Information of the specific properties used by each implementation can
 * be found in the Implementations chapter in the JSDT User Guide.
 *
 * @param properties the new JSDT property set to use.
 */

    public static void
    setProperties(Properties properties) {
        if (Connection_Debug) {
            Debug("Connection: setProperties:" +
                  " properties: " + properties);
        }

        Connection.properties = properties;
    }


/**
 * <A NAME="SD_SETPROPERTY"></A>
 * Sets an implementation property with the specified key in the JSDT
 * property list. This property value will override it's equivalent value
 * in <CODE>com.sun.media.jsdt.impl.JSDTObject</CODE> and be used by the
 * implementation.
 *
 * Information of the specific properties used by each implementation can
 * be found in the Implementations chapter in the JSDT User Guide.
 *
 * @param key the property key.
 * @param value the value to associate with this key.
 *
 * @exception NullPointerException if the key or value is <code>null</code>.
 *
 * @return the previous value of the specified key in this hashtable,
 * or <CODE>null</CODE> if it did not have one.
 */

    @SuppressWarnings("UnusedReturnValue")
    public static Object
    setProperty(String key, String value) {
        if (Connection_Debug) {
            Debug("Connection: setProperty:" +
                  " key: "   + key +
                  " value: " + value);
        }

        return(properties.put(key, value));
    }


/**
 * <A NAME="SD_REMOVEPROPERTY"></A>
 * Removes an implementation property with the specified key from the JSDT
 * property list. Unless this property is reset into the property list, the
 * default value in <CODE>com.sun.media.jsdt.impl.JSDTObject</CODE> will be
 * used by the implementation.
 *
 * Information of the specific properties used by each implementation can
 * be found in the Implementations chapter in the JSDT User Guide.
 *
 * @param key the property key.
 *
 * @return the value to which the key had been mapped in this hashtable,
 * or <CODE>null</CODE> if the key did not have a mapping.
 */

    public static Object
    removeProperty(String key) {
        if (Connection_Debug) {
            Debug("Connection: removeProperty:" +
                  " key: " + key);
        }

        return(properties.remove(key));
    }

/**
 * <A NAME="SD_ADDCONNECTIONLISTENER"></A>
 * add the specified connection listener to provide asynchronous notification
 * of a connection failure. Without such a connection listener, the application
 * would only be able to determine failure when it next attempted to perform
 * an operation that required the use of its underlying connection(s), which
 * would throw a ConnectionException.
 *
 * <P>A single connection listener can handle multiple underlying connections.
 * Multiple connection listeners can be added by an application.
 *
 * <P>If a connection failure is reported, then that connection listener is
 * automatically removed to prevent continual failure notifications.
 *
 * @param host the machine to the check the connection to.
 * @param connectionType the type of connection ("socket", "http" ...)
 * @param listener the connection listener to add.
 *
 * @exception NoRegistryException if there is no Registry running of the
 * given type on the given host.
 * @exception NoSuchHostException if the host doesn't exist.
 */

    public static void
    addConnectionListener(String host, String connectionType,
                          ConnectionListener listener)
        throws NoRegistryException, NoSuchHostException {
        if (Connection_Debug) {
            Debug("Connection: addConnectionListener:" +
                  " host: "            + host +
                  " connection type: " + connectionType +
                  " listener: "        + listener);
        }

        Naming.addConnectionListener(host, connectionType, listener);
    }


/**
 * <A NAME="SD_REMOVECONNECTIONLISTENER"></A>
 * remove the specified connection listener. This connection listener will
 * no longer be notified of any failures on the underlying connection(s).
 *
 * <P>If a connection failure is reported, then that connection listener is
 * automatically removed to prevent continual failure notifications.
 *
 * @param host the machine to the check the connection to.
 * @param connectionType the type of connection ("socket", "http" ...)
 * @param listener the connection listener to remove.
 *
 * @exception NoRegistryException if there is no Registry running of the
 * given type on the given host.
 * @exception NoSuchHostException if the host doesn't exist.
 * @exception NoSuchListenerException if this connection listener doesn't
 * exist.
 */

    public static void
    removeConnectionListener(String host, String connectionType,
                             ConnectionListener listener)
        throws NoRegistryException, NoSuchHostException,
                       NoSuchListenerException {
        if (Connection_Debug) {
            Debug("Connection: removeConnectionListener:" +
                  " host: "            + host +
                  " connection type: " + connectionType +
                  " listener: "        + listener);
        }

        Naming.removeConnectionListener(host, connectionType, listener);
    }
}
