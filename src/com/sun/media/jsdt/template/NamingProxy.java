
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

package com.sun.media.jsdt.template;

import com.sun.media.jsdt.*;
import com.sun.media.jsdt.event.ConnectionListener;
import com.sun.media.jsdt.event.RegistryListener;
import com.sun.media.jsdt.impl.*;
import java.util.Hashtable;

/**
 * JSDT Naming proxy class.
 *
 * @version     2.3 - 28th October 2017
 * @author      Rich Burridge
 */

public final class
NamingProxy extends JSDTObject
            implements AbstractNamingProxy, templateDebugFlags {

/**
 * <A NAME="SD_INITPROXY"></A>
 * <EM>initProxy</EM> initialise the Registry proxy.
 *
 * @param connections the hash table of connections (keyed by host).
 * @param host the host the Registry is running on.
 * @param port the port number the Registry is running on.
 *
 * @exception NoRegistryException if no Registry process is running.
 * @exception NoSuchHostException if the host given does not exist.
 */

    public void
    initProxy(Hashtable connections, String host, int port)
        throws NoRegistryException, NoSuchHostException {
        if (NamingProxy_Debug) {
            debug("NamingProxy: initProxy:" +
                  " connections: " + connections +
                  " host: "        + host +
                  " port: "        + port);
        }
    }


/**
 * <A NAME="SD_GETPROXY"></A>
 * <EM>getProxy</EM> get a handle to the proxy for the Registry.
 *
 * @return a handle to the proxy for this Registry.
 */

    public Object
    getProxy() {
        if (ManageableProxy_Debug) {
            debug("NamingProxy: getProxy.");
        }

        return(this);
    }


/**
 * <A NAME="SD_BIND"></A>
 * <EM>bind</EM> binds the name to the specified JSDT object.
 *
 * The name should be of the form:
 *     jsdt://<machine>[:<port>]/<impl type>/<objecttype>/<objectname>
 *
 * where valid impl types are "multicast", "http" and "socket".
 * where valid object types are "Session" and "Client".
 *
 * So for example:
 *     bind("jsdt://stard:3355/socket/Session/chatSession", chatSession);
 *
 *     bind("jsdt://stard:4386/socket/Client/fredClient", fredClient);
 *
 * @param urlString the URLString object for the given name.
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
 * @exception PermissionDeniedException if this Client doesn't have
 * permission for this operation.
 * @exception PortInUseException if this port is being used by another
 * application.
 * @exception TimedOutException if no reply was received for this operation
 * in the given timeout period.
 */

    public void
    bind(URLString urlString, Object object, Client client)
        throws AlreadyBoundException, ConnectionException,
               InvalidClientException, InvalidURLException,
               NoRegistryException, PermissionDeniedException,
               PortInUseException, TimedOutException {
        if (NamingProxy_Debug) {
            debug("NamingProxy: bind:" +
                  " URL string: " + urlString +
                  " object: "     + object +
                  " client: "     + client);
        }
    }


/**
 * <A NAME="SD_UNBIND"></A>
 * <EM>unbind</EM> unbinds the object associated with this name.
 *
 * @param urlString the URLString object for the given name.
 * @param object the object to unbind.
 * @param client the client that is trying to unbind this object.
 *
 * @exception ConnectionException if a connection error occured.
 * @exception InvalidClientException if the Client is invalid is some way (ie.
 * its getName() method returns null).
 * @exception InvalidURLException if the url string given is invalid.
 * @exception NoRegistryException if no Registry process is running.
 * @exception NotBoundException if no object bound to this url string.
 * @exception PermissionDeniedException if this Client doesn't have
 * permission for this operation.
 * @exception TimedOutException if no reply was received for this operation
 * in the given timeout period.
 */

    public void
    unbind(URLString urlString, Object object, Client client)
        throws ConnectionException, InvalidClientException,
               InvalidURLException, NoRegistryException,
               NotBoundException, PermissionDeniedException,
               TimedOutException {
        if (NamingProxy_Debug) {
            debug("NamingProxy: unbind:" +
                  " url string: " + urlString +
                  " object: "     + object +
                  " client: "     + client);
        }
    }


/**
 * <A NAME="SD_LOOKUP"></A>
 * <EM>lookup</EM> returns the JSDT object for the given name.
 *
 * The name should be of the form:
 *     jsdt://<machine>[:<port>]/<connectType>/<objectType>/<objectName>
 *
 * where valid object types are "Session" and "Client".
 *
 * So for example:
 *   Session session = lookup("jsdt://stard:3355/socket/Session/chatSession");
 *
 *   Client  client  = lookup("jsdt://stard:4386/socket/Client/fredClient");
 *
 * @param urlString the URLString object for the given name.
 *
 * @exception ConnectionException if a connection error occured.
 * @exception NoRegistryException if no Registry process is running.
 * @exception InvalidURLException if the url string given is invalid.
 * @exception NotBoundException if no object bound to this url string.
 * @exception TimedOutException if no reply was received for this operation
 * in the given timeout period.
 *
 * @return the resolved object.
 */

    public Object
    lookup(URLString urlString)
        throws ConnectionException,
               NoRegistryException, InvalidURLException,
               NotBoundException, TimedOutException {
        Object object = null;

        if (NamingProxy_Debug) {
            debug("NamingProxy: lookup:" +
                  " url string: " + urlString);
        }

        return(object);
    }


/**
 * <A NAME="SD_ADDREGISTRYLISTENER"></A>
 * add the specified Registry listener to receive Registry events from the
 * Registry of the specified type running on the given host.
 *
 * @param listener the Registry listener to add.
 *
 * @exception ConnectionException if a connection error occured.
 * @exception NoRegistryException if there is no Registry running of the
 * given type on the given host.
 * @exception NoSuchHostException if the host doesn't exist.
 * @exception TimedOutException if no reply was received for this operation
 * in the given timeout period.
 */

    public void
    addRegistryListener(RegistryListener listener)
        throws ConnectionException, NoRegistryException,
               NoSuchHostException, TimedOutException {
        if (NamingProxy_Debug) {
            debug("NamingProxy: addRegistryListener:" +
                  " listener: " + listener);
        }
    }


/**
 * <A NAME="SD_REMOVEREGISTRYLISTENER"></A>
 * removes the specified Registry listener so that it no longer receives
 * Registry events from the Registry of the specified type running on the
 * given host.
 *
 * @param listener the Registry listener to remove.
 *
 * @exception ConnectionException if a connection error occured.
 * @exception NoRegistryException if there is no Registry running of the
 * given type on the given host.
 * @exception NoSuchHostException if the host doesn't exist.
 * @exception NoSuchListenerException if this SessionListener doesn't exist.
 * @exception TimedOutException if no reply was received for this operation
 * in the given timeout period.
 */

    public void
    removeRegistryListener(RegistryListener listener)
        throws ConnectionException, NoRegistryException,
               NoSuchHostException, NoSuchListenerException,
               TimedOutException {
        if (NamingProxy_Debug) {
            debug("NamingProxy: removeRegistryListener:" +
                  " listener: " + listener);
        }
    }


/**
 * <A NAME="SD_LIST"></A>
 * <EM>list</EM> lists all the names of the known bound JSDT objects.
 *
 * @exception ConnectionException if a connection error occured.
 * @exception NoRegistryException if no Registry process is running.
 * @exception TimedOutException if no reply was received for this operation
 * in the given timeout period.
 *
 * @return an array of URL Strings of the names of all the known bound objects.
 */

    public URLString[]
    list()
        throws ConnectionException, NoRegistryException, TimedOutException {
        URLString[] names = null;

        if (NamingProxy_Debug) {
            debug("NamingProxy: list.");
        }

        return(names);
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
 * @param listener the connection listener to add.
 */

    public void
    addConnectionListener(ConnectionListener listener) {
        if (NamingProxy_Debug) {
            debug("NamingProxy: addConnectionListener:" +
                  " listener: " + listener);
        }
    }


/**
 * <A NAME="SD_REMOVECONNECTIONLISTENER"></A>
 * remove the specified connection listener. This connection listener will
 * no longer be notified of any failures on the underlying connection(s).
 *
 * @param listener the connection listener to remove.
 *
 * @exception NoSuchListenerException if this connection listener doesn't
 * exist.
 */

    public void
    removeConnectionListener(ConnectionListener listener)
        throws NoSuchListenerException {
        if (NamingProxy_Debug) {
            debug("NamingProxy: removeConnectionListener:" +
                  " listener: " + listener);
        }
    }
}
