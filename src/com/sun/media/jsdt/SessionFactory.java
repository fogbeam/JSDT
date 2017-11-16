
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
import java.util.Vector;

/**
 * The SessionFactory class.
 *
 * <P>This is a factory class that is the basis for creating new sessions.
 * These would be of the appropriate type ("socket", "http", "multicast" ...).
 * The session would then be published by the naming service, and tied
 * to a specific URL.
 *
 * @version     2.3 - 16th November 2017
 * @author      Rich Burridge
 * @since       JSDT 1.0
 */

public class
SessionFactory extends JSDTObject {


/**
 * <A NAME="SD_CREATESESSION"></A>
 * <EM>createSession</EM> is a class method that will create a new session.
 * If the Session already exists, a handle is returned to that Session.
 *
 * @param client a client that will potentially be joined to
 * this session. This client will also be used for authentication
 * purposes if the Registry running on the server machine where this
 * Session URL will be stored, is managed.
 * @param urlString the URLString for this session.
 * @param autoJoin if true, automatically join the session when it's created.
 *
 * @exception ConnectionException if a connection error occured.
 * @exception InvalidClientException if the Client is invalid is some way (ie.
 * its getName() method returns null).
 * @exception InvalidURLException if the url string given is invalid.
 * @exception NameInUseException if a Client with this name is already
 * joined to this Session.
 * @exception NoRegistryException if no Registry process running.
 * @exception NoSuchClientException if this Client doesn't exist.
 * @exception NoSuchHostException if the host name in the url string doesn't
 * exist.
 * @exception NoSuchSessionException if a session of this type could not be
 * returned.
 * @exception PermissionDeniedException if this Client doesn't have
 * permission for this operation.
 * @exception PortInUseException if this port is being used by another
 * application.
 * @exception TimedOutException if no reply was received for this operation
 * in the given timeout period.
 *
 * @return an appropriate Session.
 *
 * @since        JSDT 1.5
 */

    public static Session
    createSession(Client client, URLString urlString, boolean autoJoin)
        throws ConnectionException, InvalidClientException,
               InvalidURLException, NameInUseException,
               NoRegistryException, NoSuchClientException,
               NoSuchHostException, NoSuchSessionException,
               PermissionDeniedException, PortInUseException,
               TimedOutException {
        SessionImpl session;

        if (SessionFactory_Debug) {
            Debug("SessionFactory: createSession:" +
                  " client: "    + client +
                  " url: "       + urlString +
                  " auto join? " + autoJoin);
        }

        try {
            session = (SessionImpl) Naming.lookup(urlString);
        } catch (NotBoundException nbe) {
            String sessionName = urlString.getObjectName();
            String connectType = urlString.getConnectionType();
            String className   = "com.sun.media.jsdt." + connectType +
                                       "." + connectType + "Session";

            try {
                session = (SessionImpl)
                                Util.getClassForName(className).newInstance();
            } catch (Exception e) {
                throw new NoSuchSessionException();
            }

            session.setName(sessionName);
            try {
                Naming.bind(urlString, session, client);

/*  The second call to Naming.lookup() is needed in order to return a
 *  client-side handle to the Session.
 */
                session = (SessionImpl) Naming.lookup(urlString);
            } catch (AlreadyBoundException abe) {
            } catch (NotBoundException nbex) {
            }
        }

        try {
            if (autoJoin) {
                session.join(client);
            }
        } catch (NoSuchByteArrayException nsbe) {
        } catch (NoSuchChannelException nsce) {
        } catch (NoSuchTokenException nste) {
        }

        return(session);
    }


/**
 * <A NAME="SD_CREATESESSIONWITHMANAGER"></A>
 * <EM>createSession</EM> is a class method that will create a new session,
 * and associate a session manager with that session.
 * If the Session already exists, a handle is returned to that Session.
 *
 * @param client a client that will potentially be joined to this session.
 * This client will also be used for authentication purposes if the Registry
 * running on the server machine where this Session URL will be stored, is
 * managed.
 * @param urlString the URLString for this session.
 * @param autoJoin if true, automatically join the session when it's created.
 * @param sessionManager the session manager to associate with this session.
 *
 * @exception ConnectionException if a connection error occured.
 * exist.
 * @exception InvalidClientException if the Client is invalid is some way (ie.
 * its getName() method returns null).
 * @exception InvalidURLException if the url string given is invalid.
 * @exception NameInUseException if a Client with this name is already
 * joined to this Session.
 * @exception NoSuchClientException if this Client doesn't exist.
 * @exception NoRegistryException if no Registry process running.
 * @exception NoSuchHostException if the host name in the url string doesn't
 * exist.
 * @exception NoSuchSessionException if a session of this type could not be
 * returned.
 * @exception ManagerExistsException if a manager already exists for this
 * session.
 * @exception PermissionDeniedException if this Session was previously
 * created without a manager attached. You should not be able to add a
 * manager afterwards.
 * @exception PortInUseException if this port is being used by another
 * application.
 * @exception TimedOutException if no reply was received for this operation
 * in the given timeout period.
 *
 * @return an appropriate Session.
 *
 * @since       JSDT 2.0
 */

    public static Session
    createSession(Client client, URLString urlString,
                  boolean autoJoin, SessionManager sessionManager)
        throws ConnectionException, InvalidClientException,
               InvalidURLException, NameInUseException,
               NoRegistryException, NoSuchClientException,
               NoSuchHostException, NoSuchSessionException,
               ManagerExistsException, PermissionDeniedException,
               PortInUseException, TimedOutException {
        boolean     alreadyExists = false;
        SessionImpl session;

        if (SessionFactory_Debug) {
            Debug("SessionFactory: createSession:" +
                  " client: "    + client +
                  " url: "       + urlString +
                  " auto join? " + autoJoin +
                  " manager: "   + sessionManager);
        }

        try {
            session       = (SessionImpl) Naming.lookup(urlString);
            alreadyExists = true;
        } catch (NotBoundException nbe) {
            String sessionName = urlString.getObjectName();
            String connectType = urlString.getConnectionType();
            String className   = "com.sun.media.jsdt." + connectType +
                                       "." + connectType + "Session";

            try {
                session = (SessionImpl)
                                Util.getClassForName(className).newInstance();
            } catch (Exception e) {
                throw new NoSuchSessionException();
            }

            session.setName(sessionName);
            try {
                Naming.bind(urlString, session, client);

/*  The second call to Naming.lookup() is needed in order to return a
 *  client-side handle to the Session.
 */
                session = (SessionImpl) Naming.lookup(urlString);
            } catch (AlreadyBoundException abe) {
            } catch (NotBoundException nbex) {
            }
        }

        if (alreadyExists) {
            try {
                if (session.isManaged()) {
                    throw new ManagerExistsException();
                } else {
                    throw new PermissionDeniedException();
                }
            } catch (NoSuchByteArrayException nsbe) {
            } catch (NoSuchChannelException nsce) {
            } catch (NoSuchTokenException nste) {
            }
        }

        if (sessionManager != null) {
            session.po.attachSessionManager(sessionManager, session);
        }

        try {
            if (autoJoin) {
                session.join(client);
            }
        } catch (NoSuchByteArrayException nsbe) {
        } catch (NoSuchChannelException nsce) {
        } catch (NoSuchTokenException nste) {
        }

        return(session);
    }


/**
 * <A NAME="SD_DESTROYSESSION"></A>
 * <EM>destroySession</EM> is a class method that will destroy the Session
 * referenced by the given URL string.
 *
 * <P>An indication is delivered to each listener for the Registry where
 * this Session is stored, that it has been destroyed. If the Registry
 * where this Session is stored is managed, then the Client is authenticated
 * to determine if it is permitted to do this operation.
 *
 * @param client the Client wishing to destroy this Session..
 * @param urlString the URLString for this session.
 *
 * @exception ConnectionException if a connection error occured.
 * @exception InvalidClientException if the Client is invalid is some way (ie.   * its getName() method returns null).
 * @exception InvalidURLException if the url string given is invalid.
 * @exception NoRegistryException if no Registry process running.
 * @exception NoSuchHostException if the host name in the url string doesn't
 * exist.
 * @exception NoSuchSessionException if this Session doesn't exist.
 * @exception NoSuchClientException if this Client doesn't exist.
 * @exception NotBoundException if no object bound to this url string.
 * @exception PermissionDeniedException if this Client doesn't have
 * permission for this operation.
 * @exception TimedOutException if no reply was received for this operation
 * in the given timeout period.
 *
 * @since       JSDT 2.0
 */

    public static void
    destroySession(Client client, URLString urlString)
        throws ConnectionException, InvalidClientException,
               InvalidURLException, NoRegistryException,
               NoSuchClientException, NoSuchHostException,
               NoSuchSessionException, NotBoundException,
               PermissionDeniedException, TimedOutException {
        if (SessionFactory_Debug) {
            Debug("SessionFactory: destroySession:" +
                  " client: " + client +
                  " url: "    + urlString);
        }

        if (SessionFactory.sessionExists(urlString)) {
            try {
                Session session = SessionFactory.createSession(client,
                                                     urlString, false);

                session.destroy(client);
            } catch (NoSuchByteArrayException nbe) {
            } catch (NoSuchChannelException nce) {
            } catch (NoSuchTokenException nte) {
            } catch (PortInUseException piue) {
            } catch (NameInUseException niue) {
            }
        } else {
            throw new NotBoundException();
        }
    }


/**
 * <A NAME="SD_SESSIONEXISTS"></A>
 * <EM>sessionExists</EM> is a class method that checks if a Session with
 * the given url string, already exists.
 *
 * @param urlString the Session URLString to check.
 *
 * @return true if the Session already exists; false if it doesn't.
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
    sessionExists(URLString urlString)
        throws ConnectionException, NoSuchHostException,
               NoRegistryException, InvalidURLException,
               TimedOutException {
        URLString[] names;

        if (SessionFactory_Debug) {
            Debug("SessionFactory: sessionExists:" +
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
 * <A NAME="SD_SESSIONMANAGED"></A>
 * <EM>sessionManaged</EM> is a class method that checks if the Session with
 * the given url is managed.
 *
 * @param urlString the Session URLString to check.
 *
 * @return true if the Session is managed; false if it isn't.
 *
 * @exception ConnectionException if a connection error occured.
 * @exception NoSuchHostException if if the host name in the url string
 * doesn't exist.
 * @exception NoRegistryException if no Registry process running.
 * @exception NoSuchSessionException if the session name in the url string
 * doesn't exist.
 * @exception InvalidURLException if url string given is invalid.
 * @exception TimedOutException if no reply was received for this operation
 * in the given timeout period.
 *
 * @since       JSDT 1.5
 */

    public static boolean
    sessionManaged(URLString urlString)
        throws ConnectionException, NoSuchHostException,
               NoRegistryException, NoSuchSessionException,
               InvalidURLException, TimedOutException {
        SessionImpl session;
        boolean     managed = false;

        if (SessionFactory_Debug) {
            Debug("SessionFactory: sessionManaged:" +
                  " url: " + urlString);
        }

        try {
            session = (SessionImpl) Naming.lookup(urlString);
        } catch (NotBoundException nbe) {
            throw new NoSuchSessionException();
        }

        try {
            managed = session.isManaged();
        } catch (NoSuchByteArrayException nsbe) {
        } catch (NoSuchChannelException nsce) {
        } catch (NoSuchTokenException nste) {
        }

        return(managed);
    }


/**
 * <A NAME="SD_LISTSESSIONS"></A>
 * lists all the url strings of the known bound JSDT Sessions.
 *
 * @exception ConnectionException if a connection error occured.
 * @exception NoRegistryException if no Registry process is running.
 * @exception TimedOutException if no reply was received for this operation
 * in the given timeout period.
 *
 * @return an array of Strings of the names of all the known bound JSDT
 * Sessions, or a zero length array if there are no bound objects.
 *
 * @since       JSDT 2.0
 */

    public static URLString[]
    listSessions()
        throws ConnectionException, NoRegistryException,
               TimedOutException {
        URLString[]       all      = Naming.list();
        Vector<URLString> allNames = new Vector<>();
        URLString[]       sessions;

        if (SessionFactory_Debug) {
            Debug("SessionFactory: listSessions.");
        }

        for (int i = 0; i < all.length; i++) {
            if (Util.isSession(all[i])) {
                allNames.addElement(all[i]);
            }
        }

        sessions = new URLString[allNames.size()];
        allNames.copyInto(sessions);

        return(sessions);
    }


/**
 * <A NAME="SD_LISTSESSIONSONHOST"></A>
 * lists all the url strings of the known bound JSDT Sessions on the given
 * host with the given connection type.
 *
 * @param host host name of the machine to look for bound JSDT sessions on.
 * @param connectionType the type of connection ("socket", "http" ...).
 *
 * @exception ConnectionException if a connection error occured.
 * @exception NoSuchHostException if the host doesn't exist.
 * @exception NoRegistryException if no Registry process is running.
 * @exception TimedOutException if no reply was received for this operation
 * in the given timeout period.
 *
 * @return an array of Strings of the names of all the known bound JSDT
 * Sessions on the given host, or null if there are no bound objects.
 *
 * @since       JSDT 2.0
 */

    public static URLString[]
    listSessions(String host, String connectionType)
        throws ConnectionException, NoSuchHostException,
               NoRegistryException, TimedOutException {
        URLString[]       all      = Naming.list(host, connectionType);
        Vector<URLString> allNames = new Vector<>();
        URLString[]       sessions;

        if (SessionFactory_Debug) {
            Debug("SessionFactory: listSessions:" +
                  " host: "            + host +
                  " connection type: " + connectionType);
        }

        for (int i = 0; i < all.length; i++) {
            if (Util.isSession(all[i])) {
                allNames.addElement(all[i]);
            }
        }

        sessions = new URLString[allNames.size()];
        allNames.copyInto(sessions);

        return(sessions);
    }
}
