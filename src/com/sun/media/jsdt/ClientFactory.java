
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
import com.sun.media.jsdt.event.ClientListener;
import java.util.Vector;

/**
 * The ClientFactory class.
 *
 * This is a factory class that is used to create a special Client that
 * can be invited to join a Session, ByteArray, Channel or Token. If you
 * do not need this functionality, then you should just implement the
 * Client interface.
 *
 * See the JSDT User Guide for examples of using both a normal Client and
 * the ClientFactory.
 *
 * @version     2.3 - 26th October 2017
 * @author      Rich Burridge
 * @since       JSDT 1.3
 */

public class
ClientFactory extends JSDTObject {

/**
 * <A NAME="SD_CREATECLIENT"></A>
 * <EM>createClient</EM> is a class method that will create a new special
 * Client of the appropriate type. This Client will be capable of being
 * invited to join a JSDT Session. If a Client with this URL already exists
 * in the registry, then an AlreadyBoundException will be thrown.
 *
 * <P>An indication is delivered to each listener for the Registry where
 * this Client is stored, that it has been created.
 *
 * @param client the Client to use for authentication purposes.
 * This client will also be used for authentication purposes if the Registry
 * running on the server machine where this Client URL will be stored, is
 * managed.
 * @param urlString the URLString for this client.
 * @param listener the ClientListener to send invite/expel events to.
 *
 * @exception AlreadyBoundException if this url string is already bound to
 * an object.
 * @exception ConnectionException if a connection error occured.
 * @exception InvalidClientException if the Client is invalid is some way (ie.
 * its getName() method returns null, or returns a name that is not the same
 * as the object name portion of the URLString parameter).
 * @exception InvalidURLException if the url string given is invalid.
 * @exception NoRegistryException if no Registry process running.
 * @exception NoSuchHostException if the host name in the url string doesn't
 * exist.
 * @exception NoSuchClientException if a Client of this type doesn't exist.
 * @exception PermissionDeniedException if this Client doesn't have
 * permission for this operation.
 * @exception PortInUseException if this port is being used by another
 * application.
 * @exception TimedOutException if no reply was received for this operation
 * in the given timeout period.
 *
 * @see com.sun.media.jsdt.Client
 * @see com.sun.media.jsdt.event.ClientListener
 *
 * @since       JSDT 2.0
 */

    public static void
    createClient(Client client, URLString urlString, ClientListener listener)
        throws AlreadyBoundException, ConnectionException,
               InvalidClientException, InvalidURLException,
               NoRegistryException, NoSuchHostException,
               NoSuchClientException, PermissionDeniedException,
               PortInUseException, TimedOutException {
        ClientImpl invitableClient;
        String     clientName      = urlString.getObjectName();
        String     connectType     = urlString.getConnectionType();
        String     className       = "com.sun.media.jsdt." + connectType +
                                           "." + connectType + "Client";

        if (ClientFactory_Debug) {
            System.err.println("ClientFactory: createSession:" +
                                " url: "      + urlString +
                                " client: "   + client +
                                " listener: " + listener);
        }

        if (client.getName() == null || !clientName.equals(client.getName())) {
            throw new InvalidClientException();
        }

        try {
            invitableClient = (ClientImpl)
                                 Util.getClassForName(className).newInstance();
        } catch (Exception e) {
            throw new NoSuchClientException();
        }

        invitableClient.setClientAndListener(client, listener);
        Naming.bind(urlString, invitableClient, client);
    }


/**
 * <A NAME="SD_LOOKUPCLIENT"></A>
 * <EM>lookupClient</EM> is a class method that will return a reference to
 * the special Client bound by the given URL String. This Client can then
 * be invited to to join a JSDT Session.
 *
 * @param urlString the URLString for this client.
 *
 * @exception ConnectionException if a connection error occured.
 * @exception InvalidURLException if the url string given is invalid.
 * @exception NoRegistryException if no Registry process running.
 * @exception NoSuchHostException if the host name in the url string doesn't
 * exist.
 * @exception NotBoundException if no object bound to this url string.
 * @exception TimedOutException if no reply was received for this operation
 * in the given timeout period.
 *
 * @return a reference to the Client associated with the given URL String.
 *
 * @since       JSDT 2.0
 */

    public static Client
    lookupClient(URLString urlString)
        throws ConnectionException, InvalidURLException,
               NoRegistryException, NoSuchHostException,
               NotBoundException, TimedOutException {
        if (ClientFactory_Debug) {
            Debug("ClientFactory: lookupClient:" +
                  " url: " + urlString);
        }

        return((Client) Naming.lookup(urlString));
    }


/**
 * <A NAME="SD_DESTROYCLIENT"></A>
 * <EM>destroyClient</EM> is a class method that will destroy the Client
 * referenced by the given URL string.
 *
 * <P>An indication is delivered to each listener for the Registry where
 * this Client is stored, that it has been destroyed. If the Registry
 * where this Client is stored is managed, then the Client parameter is
 * used for authentication purposes to determine if this operation is
 * permitted.
 *
 * @param client the Client wishing to destroy this special Client.
 * @param urlString the URLString for this Client.
 *
 * @exception ConnectionException if a connection error occured.
 * @exception InvalidClientException if the Client is invalid is some way
 * (ie. its getName() method returns null).
 * @exception InvalidURLException if the url string given is invalid.
 * @exception NoRegistryException if no Registry process running.
 * @exception NoSuchClientException if this Client doesn't exist.
 * @exception NoSuchHostException if the host name in the url string doesn't
 * exist.
 * @exception NotBoundException if no object bound to this url string.
 * @exception PermissionDeniedException if this Client doesn't have
 * ermission for this operation.
 * @exception TimedOutException if no reply was received for this operation
 * in the given timeout period.
 *
 * @since       JSDT 2.0
 */

    public static void
    destroyClient(Client client, URLString urlString)
        throws ConnectionException, InvalidClientException,
               InvalidURLException, NoRegistryException,
               NoSuchClientException, NoSuchHostException,
               NotBoundException, PermissionDeniedException,
               TimedOutException {
        if (ClientFactory_Debug) {
            Debug("ClientFactory: destroyClient:" +
                  " client: " + client +
                  " url: "    + urlString);
        }

        Client object = ClientFactory.lookupClient(urlString);

        Naming.unbind(urlString, object, client);
    }


/**
 * <A NAME="SD_CLIENTEXISTS"></A>
 * <EMclientExists</EM> is a class method that checks if a Client with
 * the given url string already exists.
 *
 * @param urlString the Client URLString to check.
 *
 * @return true if the Client already exists; false if it doesn't.
 *
 * @exception ConnectionException if a connection error occured.
 * @exception NoSuchHostException if if the host name in the url string
 * doesn't exist.
 * @exception NoRegistryException if no Registry process running.
 * @exception InvalidURLException if the url string given is invalid.
 * @exception TimedOutException if no reply was received for this operation
 * in the given timeout period.
 *
 * @since       JSDT 1.5
 */

    public static boolean
    clientExists(URLString urlString)
        throws ConnectionException, NoSuchHostException,
               NoRegistryException, InvalidURLException,
               TimedOutException {
        URLString[] names;

        if (ClientFactory_Debug) {
            Debug("ClientFactory: clientExists:" +
                  " url: " + urlString);
        }

        if (urlString.isValid() &&
            urlString.getProtocol().equals("jsdt:")) {
            String host        = urlString.getHostName();
            String connectType = urlString.getConnectionType();

            names = Naming.list(host, connectType);
            if (names.length != 0) {
                String oldURL    = urlString.toString();
                String IPAddress = Util.getIPAddress(urlString.getHostName());
                String newURL    = Util.adjustURLString(oldURL, IPAddress);

                for (int i = 0; i < names.length; i++) {
                    String currentURL = names[i].toString();

                    if (currentURL.equals(newURL)) {
                        return(true);
                    }
                }
            }
        } else {
            throw new InvalidURLException();
        }

        return(false);
    }


/**
 * <A NAME="SD_LISTCLIENTS"></A>
 * lists all the url strings of the known bound JSDT Clients.
 *
 * @exception ConnectionException if a connection error occured.
 * @exception NoRegistryException if no Registry process is running.
 * @exception TimedOutException if no reply was received for this operation
 * in the given timeout period.
 *
 * @return an array of Strings of the names of all the known bound JSDT
 * Clients, or a zero length array if there are no bound objects.
 *
 * @since       JSDT 2.0
 */

    public static URLString[]
    listClients()
        throws ConnectionException, NoRegistryException,
               TimedOutException {
        URLString[]          all      = Naming.list();
        Vector<URLString>    allNames = new Vector<>();
        URLString[]          clients;

        if (ClientFactory_Debug) {
            Debug("ClientFactory: listClients.");
        }

        for (int i = 0; i < all.length; i++) {
            if (Util.isClient(all[i])) {
                allNames.addElement(all[i]);
            }
        }

        clients = new URLString[allNames.size()];
        allNames.copyInto(clients);

        return(clients);
    }


/**
 * <A NAME="SD_LISTCLIENTSONHOST"></A>
 * lists all the url strings of the known bound JSDT Clients on the given
 * host with the given connection type.
 *
 * @param host host name of the machine to look for bound JSDT clients on.
 * @param connectionType the type of connection ("socket", "http" ...).
 *
 * @exception ConnectionException if a connection error occured.
 * @exception NoSuchHostException if the host doesn't exist.
 * @exception NoRegistryException if no Registry process is running.
 * @exception TimedOutException if no reply was received for this operation
 * in the given timeout period.
 *
 * @return an array of Strings of the names of all the known bound JSDT
 * Clients on the given host, or null if there are no bound objects.
 *
 * @since       JSDT 2.0
 */

    public static URLString[]
    listClients(String host, String connectionType)
        throws ConnectionException, NoSuchHostException,
               NoRegistryException, TimedOutException {
        URLString[] all      = Naming.list(host, connectionType);
        Vector<URLString>    allNames = new Vector <>();
        URLString[] clients;

        if (ClientFactory_Debug) {
            Debug("ClientFactory: listClients:" +
                  " host: "            + host +
                  " connection type: " + connectionType);
        }

        for (int i = 0; i < all.length; i++) {
            if (Util.isClient(all[i])) {
                allNames.addElement(all[i]);
            }
        }

        clients = new URLString[allNames.size()];
        allNames.copyInto(clients);

        return(clients);
    }
}
