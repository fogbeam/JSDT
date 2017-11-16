
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
import com.sun.media.jsdt.event.ConnectionListener;
import com.sun.media.jsdt.event.RegistryListener;
import java.util.*;

/**
 * The Naming class.
 *
 * This class provides simple URL based naming for JSDT objects.
 *
 * Based on the RMI Naming class.
 *
 * @version     2.3 - 26th October 2017
 * @author      Rich Burridge
 * @since       JSDT 1.0
 */

final class
Naming extends JSDTObject {

    /** The Naming class instances for the various hosts. */
    private static Hashtable<String, Naming> connections = null;

    /** Handle to Naming class proxy.
     *
     *  @serial
     */
    public AbstractNamingProxy po;


/* Disallow anyone from creating one of these. */

    private
    Naming(String host, int port, String connectType)
                throws InvalidURLException, NoRegistryException,
                       NoSuchHostException {
        if (Naming_Debug) {
            debug("Naming: constructor:" +
                  " host: "            + host +
                  " port: "            + port +
                  " connection type: " + connectType);
        }

        try {
            String className = "com.sun.media.jsdt." + connectType +
                               ".NamingProxy";

            po = (AbstractNamingProxy) Class.forName(className).newInstance();
            po.initProxy(connections, host, port);
        } catch (NoRegistryException nre) {
            throw nre;
        } catch (NoSuchHostException nsh) {
            throw nsh;
        } catch (ClassNotFoundException cnfe) {
            throw new InvalidURLException();
        } catch (Exception e) {
            error("Naming: constructor: ", e);
        }
    }


/**
 * <A NAME="SD_BIND"></A>
 * binds the url string to the specified JSDT object.
 *
 * <PRE>
 * The name should be of the form:
 *     jsdt://<host>:<port>/<connection type>/<object type>/<object name>
 *
 * where <connection type> is the connection (or implementation) type
 * (eg. "socket", "http" or "multicast"),
 * and where valid object types are "Session" and "Client".
 *
 * So for example:
 *     bind("jsdt://stard:3355/socket/Session/chatSession", chatSession);
 *
 *     bind("jsdt://stard:4386/socket/Client/fredClient", fredClient);
 * </PRE>
 *
 * @param urlString the URLString associated with this object.
 * @param object the object to bind.
 * @param client the client that is trying to bind this object.
 *
 * @exception AlreadyBoundException if this url string is already bound to
 * an object.
 * @exception ConnectionException if a connection error occured.
 * @exception InvalidClientException if the Client is invalid is some way (ie.
 * its getName() method returns null).
 * @exception InvalidURLException if the url string given is invalid.
 * @exception NoRegistryException if no Registry process is running.
 * @exception NoSuchHostException if the host name in the url string doesn't
 * exist.
 * @exception PermissionDeniedException if this Client doesn't have
 * permission for this operation.
 * @exception PortInUseException if this port is being used by another
 * application.
 * @exception TimedOutException if no reply was received for this operation
 * in the given timeout period.
 *
 * @since       JSDT 1.5
 */

    static void
    bind(URLString urlString, Object object, Client client)
                throws AlreadyBoundException, ConnectionException,
                       InvalidClientException, InvalidURLException,
                       NoRegistryException, NoSuchHostException,
                       PermissionDeniedException, PortInUseException,
                       TimedOutException {
        if (Naming_Debug) {
            Debug("Naming: bind:" +
                  " url: "    + urlString +
                  " object: " + object +
                  " client: " + client);
        }

        if (urlString.isValid() &&
            urlString.getProtocol().equals("jsdt:")) {
            String host        = urlString.getHostName();
            String connectType = urlString.getConnectionType();
            Naming naming      = getRegistryConnection(host, connectType);

            naming.po.bind(urlString, object, client);
        } else {
            throw new InvalidURLException();
        }
    }


/**
 * <A NAME="SD_UNBIND"></A>
 * unbinds the object associated with this url string.
 *
 * @param urlString the URLString of the object to unbind.
 * @param object the object to unbind.
 * @param client the client that is trying to unbind this object.
 *
 * @exception ConnectionException if a connection error occured.
 * @exception InvalidClientException if the Client is invalid is some way (ie.
 * its getName() method returns null).
 * @exception InvalidURLException if the url string given is invalid.
 * @exception NoRegistryException if no Registry process is running.
 * @exception NoSuchHostException if the host name in the url string doesn't
 * exist.
 * @exception NotBoundException if no object bound to this url string.
 * @exception PermissionDeniedException if this Client doesn't have
 * permission for this operation.
 * @exception TimedOutException if no reply was received for this operation
 * in the given timeout period.
 *
 * @since       JSDT 1.5
 */

    static void
    unbind(URLString urlString, Object object, Client client)
                throws ConnectionException, InvalidClientException,
                       InvalidURLException, NoRegistryException,
                       NoSuchHostException, NotBoundException,
                       PermissionDeniedException, TimedOutException {
        if (Naming_Debug) {
            Debug("Naming: unbind:" +
                  " url: "    + urlString +
                  " object: " + object +
                  " client: " + client);
        }

        if (urlString.isValid() &&
            urlString.getProtocol().equals("jsdt:")) {
            String host        = urlString.getHostName();
            String connectType = urlString.getConnectionType();
            Naming naming      = getRegistryConnection(host, connectType);

            naming.po.unbind(urlString, object, client);
        } else {
            throw new InvalidURLException();
        }
    }


/**
 * <A NAME="SD_LOOKUP"></A>
 * returns the JSDT object for the given url string.
 *
 * <PRE>
 * The name should be of the form:
 *     jsdt://<host>:<port>/<connection type>/<object type>/<object name>
 *
 * where <connection type> is the connection (or implementation) type
 * (eg. "socket", "http" or "multicast"),
 * and where valid object types are "Session" and "Client".
 *
 * So for example:
 *   Session session = lookup("jsdt://stard:3355/socket/Session/chatSession");
 *
 *   Client  client  = lookup("jsdt://stard:4386/socket/Client/fredClient");
 * </PRE>
 *
 * @param urlString the URLString of the object to find.
 *
 * @exception ConnectionException if a connection error occured.
 * @exception NoRegistryException if no Registry process is running.
 * @exception NoSuchHostException if the host name in the url string doesn't
 * exist.
 * @exception InvalidURLException if the url string given is invalid.
 * @exception NotBoundException if no object bound to this url string.
 * @exception TimedOutException if no reply was received for this operation
 * in the given timeout period.
 *
 * @return the resolved object.
 *
 * @since       JSDT 1.5
 */

    static Object
    lookup(URLString urlString)
                throws ConnectionException, NoRegistryException,
                       NoSuchHostException, InvalidURLException,
                       NotBoundException, TimedOutException {
        Object object;

        if (Naming_Debug) {
            Debug("Naming: lookup:" +
                  " url: " + urlString);
        }

        if (urlString.isValid() &&
            urlString.getProtocol().equals("jsdt:")) {
            String host        = urlString.getHostName();
            String connectType = urlString.getConnectionType();
            Naming naming      = getRegistryConnection(host, connectType);

            object = naming.po.lookup(urlString);
        } else {
            throw new InvalidURLException();
        }

        return(object);
    }


/**
 * <A NAME="SD_LIST"></A>
 * lists all the url strings of the known bound JSDT objects.
 *
 * @exception ConnectionException if a connection error occured.
 * @exception NoRegistryException if no Registry process is running.
 * @exception TimedOutException if no reply was received for this operation
 * in the given timeout period.
 *
 * @return an array of Strings of the names of all the known bound JSDT
 * objects, or a zero length array if there are no bound objects.
 *
 * @since       JSDT 2.0
 */

    static URLString[]
    list()
        throws ConnectionException, NoRegistryException,
               TimedOutException {
        int               total    = 0;
        Vector<URLString> allNames = new Vector<>();
        URLString[]       names;

        if (Naming_Debug) {
            Debug("Naming: list.");
        }

        if (connections == null) {
            names = new URLString[0];
        } else {
            for (Enumeration e = connections.elements(); e.hasMoreElements();) {
                Naming      naming   = (Naming) e.nextElement();
                URLString[] newNames = naming.po.list();

                if (newNames.length != 0) {
                    for (int i = 0; i < newNames.length; i++) {
                        allNames.addElement(newNames[i]);
                    }

                    total += newNames.length;
                }
            }

            names = new URLString[total];
            allNames.copyInto(names);
        }

        return(names);
    }


/**
 * <A NAME="SD_LISTONHOST"></A>
 * lists all the url strings of the known bound JSDT objects on the given host.
 *
 * @param host host name of the machine to look for bound JSDT objects on.
 * @param connectionType implementation connection type (eg. "socket").
 *
 * @exception ConnectionException if a connection error occured.
 * @exception NoSuchHostException if the host doesn't exist.
 * @exception NoRegistryException if no Registry process is running.
 * @exception TimedOutException if no reply was received for this operation
 * in the given timeout period.
 *
 * @return an array of Strings of the names of all the known bound JSDT
 * objects on the given host, or a zero length array if there are no bound
 * objects.
 *
 * @since       JSDT 2.0
 */

    static URLString[]
    list(String host, String connectionType)
        throws ConnectionException, NoSuchHostException,
               NoRegistryException, TimedOutException {
        Naming naming;

        if (Naming_Debug) {
            Debug("Naming: list:" +
                  " host: "            + host +
                  " connection type: " + connectionType);
        }

        try {
            naming = getRegistryConnection(host, connectionType);
        } catch (InvalidURLException iue) {
            throw new NoRegistryException();
        }

        return(naming.po.list());
    }


/**
 * <A NAME="SD_ADDREGISTRYLISTENER"></A>
 * add the specified Registry listener to receive Registry events from the
 * Registry of the specified type running on the given host.
 *
 * @param host the machine the Registry is running on.
 * @param type the type of Registry ("socket", "http" ...).
 * @param listener the Registry listener to add.
 *
 * @exception ConnectionException if a connection error occured.
 * @exception NoRegistryException if there is no Registry running of the
 * given type on the given host.
 * @exception NoSuchHostException if the host doesn't exist.
 * @exception TimedOutException if no reply was received for this operation
 * in the given timeout period.
 *
 * @since       JSDT 2.0
 */

    static void
    addRegistryListener(String host, String type,
                        RegistryListener listener)
                throws ConnectionException, NoRegistryException,
                       NoSuchHostException, TimedOutException {
        if (Naming_Debug) {
            Debug("Naming: addRegistryListener:" +
                  " host: "     + host +
                  " type: "     + type +
                  " listener: " + listener);
        }

        try {
            Naming naming = getRegistryConnection(host, type);

            naming.po.addRegistryListener(listener);
        } catch (InvalidURLException iue) {
            throw new NoRegistryException();
        }
    }


/**
 * <A NAME="SD_REMOVEREGISTRYLISTENER"></A>
 * removes the specified Registry listener so that it no longer receives
 * Registry events from the Registry of the specified type running on the
 * given host.
 *
 * @param host the machine the Registry is running on.
 * @param type the type of Registry ("socket", "http" ...).
 * @param listener the Registry listener to remove.
 *
 * @exception ConnectionException if a connection error occured.
 * @exception NoRegistryException if there is no Registry running of the
 * given type on the given host.
 * @exception NoSuchHostException if the host doesn't exist.
 * @exception NoSuchListenerException if this SessionListener doesn't exist.
 * @exception TimedOutException if no reply was received for this operation
 * in the given timeout period.
 *
 * @since       JSDT 2.0
 */

    static void
    removeRegistryListener(String host, String type,
                           RegistryListener listener)
                throws ConnectionException, NoRegistryException,
                       NoSuchHostException, NoSuchListenerException,
                       TimedOutException {
        if (Naming_Debug) {
            Debug("Naming: removeRegistryListener:" +
                  " host: "     + host +
                  " type: "     + type +
                  " listener: " + listener);
        }

        try {
            Naming naming = getRegistryConnection(host, type);

            naming.po.removeRegistryListener(listener);
        } catch (InvalidURLException iue) {
            throw new NoRegistryException();
        }
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
        if (Naming_Debug) {
            Debug("Naming: addConnectionListener:" +
                  " host: "            + host +
                  " connection type: " + connectionType +
                  " listener: "        + listener);
        }

        try {
            Naming naming = getRegistryConnection(host, connectionType);

            naming.po.addConnectionListener(listener);
        } catch (InvalidURLException iue) {
            throw new NoRegistryException();
        }
    }


/**
 * <A NAME="SD_REMOVECONNECTIONLISTENER"></A>
 * remove the specified connection listener. This connection listener will
 * no longer be notified of any failures on the underlying connection(s).
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
        if (Naming_Debug) {
            Debug("Naming: removeConnectionListener:" +
                  " host: "            + host +
                  " connection type: " + connectionType +
                  " listener: "        + listener);
        }

        try {
            Naming naming = getRegistryConnection(host, connectionType);

            naming.po.removeConnectionListener(listener);
        } catch (InvalidURLException iue) {
            throw new NoRegistryException();
        }
    }


/**
 * <A NAME="SD_GETREGISTRYCONNECTION"></A>
 * get a connection to the Registry running on the given host.
 *
 * @param host host name of the machine running the registry.
 * @param connectType implementation connection type (eg. "socket").
 *
 * @exception InvalidURLException if the url string given is invalid.
 * @exception NoSuchHostException if the host doesn't exist.
 * @exception NoRegistryException if no Registry process is running.
 *
 * @return a connection to the Registry running on the given host.
 */

    static Naming
    getRegistryConnection(String host, String connectType)
                throws InvalidURLException,  NoSuchHostException,
                       NoRegistryException {
        Naming naming;

        if (Naming_Debug) {
            Debug("Naming: getRegistryConnection:" +
                  " host: "            + host +
                  " connection type: " + connectType);
        }

        if (connections == null) {
            connections = new Hashtable<>();
        }

        if ((naming = (Naming) connections.get(host)) == null) {
            int port = Util.getIntProperty("registryPort", registryPort);
            naming = new Naming(host, port, connectType);
            synchronized (connections) {
                connections.put(host, naming);
            }
        }
        return(naming);
    }
}
