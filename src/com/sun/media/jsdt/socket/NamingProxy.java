
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

package com.sun.media.jsdt.socket;

import com.sun.media.jsdt.*;
import com.sun.media.jsdt.impl.*;
import com.sun.media.jsdt.event.*;
import java.net.*;
import java.io.*;
import java.util.*;

/**
 * JSDT Naming proxy class (socket implementation).
 *
 * This class provides simple URL based naming for JSDT objects.
 *
 * Based on the RMI Naming class.
 *
 * @version     2.3 - 20th December 2017
 * @author      Rich Burridge
 */

public final class
NamingProxy extends JSDTObject
            implements AbstractNamingProxy, Runnable, socketDebugFlags {

    // The host the Registry is running on.
    private String host = null;

    // The port the Registry is running on.
    private int port;

    // Hash table of connections (keyed by host).
    private Hashtable connections = null;

    // The sessions currently bound.
    private Hashtable<String, Session> sessions = null;

    // The clients currently bound.
    private Hashtable<String, Client> clients = null;

    // The client-side thread for contacting the registry.
    private SocketThread proxyThread = null;

    // The listeners (and event masks), observing changes for the Registry.
    Hashtable<RegistryListener, Integer> listeners = null;

    // The client-side thread for detecting connection failure.
    ConnectionThread connectionThread = null;

    // The thread running this NamingProxy object.
    private Thread thread;

    // Set true if we haven't received a ping reply and have timed out.
    private boolean maybeFailure = false;

    // The time of the last successful reply to a ping message.
    private long lastPingTime = System.currentTimeMillis();

    private boolean shutdown = false;

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

        this.connections = connections;
        this.host        = host;
        this.port        = port;

        sessions  = new Hashtable<>();
        clients   = new Hashtable<>();
        listeners = new Hashtable<>();

        try {
            proxyThread      = new NamingProxyThread(this, host, port);
            connectionThread = new ConnectionThread(sessions);
        } catch (SocketException e) {
            throw new NoRegistryException();
        } catch (UnknownHostException uhe) {
            throw new NoSuchHostException();
        }

        thread = Util.startThread(this, "NamingProxy", true);
        Util.startThread(proxyThread,
                         "NamingProxyThread:" + host + ":" + port, true);
    }


/**
 * <A NAME="SD_GETPROXY"></A>
 * <EM>getProxy</EM>
 *
 * @return a handle to the proxy for this object.
 */

    public Object
    getProxy() {
        return(this);
    }


/**
 * <A NAME="SD_CLEANUPCLIENT"></A>
 * <EM>cleanupClient</EM>
 *
 * @param name the name of the Client to cleanup.
 * @param client a proxy-side reference to that client.
 * @param id the proxy thread id to use in the message to the client.
 *
 * @exception ConnectionException if a connection error occured.
 */

    private void
    cleanupClient(String name, Client client, int id)
        throws ConnectionException {
        char type   = ClientImpl.M_Client;
        char action = T_DestroyClient;

        if (NamingProxy_Debug) {
            debug("NamingProxy: cleanupClient:" +
                  " client name: " + name +
                  " client: "      + client +
                  " id: "          + id);
        }

        if (client instanceof socketClient) {
            socketClient sc = (socketClient) client;

            if (sc.cc != null) {
                SocketThread thread = sc.cc.proxyThread;

                if (thread != null) {
                    try {
                        thread.writeMessageHeader(thread.dataOut, (short) 1,
                                              id, type, action, false, true);
                        thread.flush();
                        thread.finishMessage();
                    } catch (IOException e) {
                        thread.finishMessage();
                        throw new ConnectionException();
                    }
                }
            }
        } else {
            error("NamingProxy: cleanupClient: ",
                  "impl.cannot.destroy.client", client);
        }

        synchronized (clients) {
            clients.remove(name);

            if (sessions.size()  == 0 &&
                clients.size()   == 0 &&
                listeners.size() == 0) {
                cleanupConnection();
            }
        }
    }


/**
 * <A NAME="SD_CLEANUPSESSION"></A>
 * <EM>cleanupSession</EM>
 *
 * @param name the name of the Session to cleanup.
 */

    void
    cleanupSession(String name) {
        if (NamingProxy_Debug) {
            debug("NamingProxy: cleanupSession:" +
                  " session name: " + name);
        }

        synchronized (sessions) {
            sessions.remove(name);

            if (sessions.size() == 0) {
                if (ChannelProxy.drq != null) {
                    ChannelProxy.drq.terminate();
                    ChannelProxy.drq = null;
                }

                if (clients.size() == 0 && listeners.size() == 0) {
                    cleanupConnection();
                }
            }
        }
    }


/**
 * <A NAME="SD_HASSESSIONS"></A>
 * <EM>hasSessions</EM> returns an indication of whether it still has
 * references to any Sessions on the given host:port.
 *
 * @param sessionHost the host name of the server machine.
 * @param sessionPort the port number on the server machine.
 *
 * @return an indication of whether there are any more Sessions associated
 * with this naming proxy, for the given host:port.
 */

    boolean
    hasSessions(String sessionHost, int sessionPort) {
        boolean reply = false;

        if (NamingProxy_Debug) {
            debug("NamingProxy: hasSessions:" +
                  " host: " + sessionHost +
                  " port: " + sessionPort);
        }

        synchronized (sessions) {
            for (Enumeration e = sessions.elements(); e.hasMoreElements();) {
                socketSession session = (socketSession) e.nextElement();
                SessionProxy  sp      = (SessionProxy) session.po;
                String        host    = sp.proxyThread.getAddress();
                int           port    = sp.proxyThread.getPort();

                if (sessionHost.equals(host) && sessionPort == port) {
                    reply = true;
                    break;
                }
            }
        }

        return(reply);
    }


/**
 * <A NAME="SD_CLEANUPCONNECTION"></A>
 * <EM>cleanupConnection</EM>
 */

    void
    cleanupConnection() {
        if (NamingProxy_Debug) {
            debug("NamingProxy: cleanupConnection.");
        }

        synchronized (connections) {
            connections.remove(host);
        }

        proxyThread.cleanupConnection();
        connectionThread.stop();
        informListeners(null, null, RegistryEvent.CONNECTION_FAILED);

        setShutdown(true);
        thread.interrupt();
    }


/**
 * <A NAME="SD_BIND"></A>
 * <EM>bind</EM> binds the name to the specified JSDT object.
 *
 * The name should be of the form:
 *     jsdt://&lt;machine&gt;[:&lt;port&gt;]/&lt;impl type&gt;/&lt;objecttype&gt;/&lt;objectname&gt;
 *
 * where valid impl types are "multicast", "http" and "socket".
 * where valid object types are "Session" and "Client".
 *
 * For example:
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
        DataInputStream in;
        Message         message;
        int             retval;
        String          name       = urlString.toString();
        String          host       = urlString.getHostName();
        int             id         = proxyThread.getId();
        char            type       = T_Registry;
        short           no         = 0;
        int             authAction = 0;
        boolean         isServer   = false;

        if (NamingProxy_Debug) {
            debug("NamingProxy: bind:" +
                  " URL string: " + urlString +
                  " object: "     + object +
                  " client: "     + client);
        }

        if (Util.isSession(urlString)) {
            authAction = AuthenticationInfo.CREATE_SESSION;
            isServer = checkForServer(urlString.getHostAddress(),
                                      urlString.getPort());
        } else if (Util.isClient(urlString)) {
            authAction = AuthenticationInfo.CREATE_CLIENT;
        }

        if (isManaged()) {
            if (!authenticateClient(urlString, authAction, client)) {
                throw new PermissionDeniedException();
            }
        }

        try {
            JSDTSecurity.enablePrivilege.invoke(JSDTSecurity.privilegeManager,
                                                JSDTSecurity.connectArgs);
        } catch (Exception e) {
        }

        try {
            proxyThread.writeMessageHeader(proxyThread.dataOut, (short) 1,
                                   id, type, T_GetSessionNo, true, true);
            proxyThread.flush();
            message = proxyThread.waitForReply();

            in     = message.thread.dataIn;
            retval = in.readInt();
            if (retval == 0) {
                no = in.readShort();
            }
            proxyThread.finishReply();

            if (retval != 0) {
                if (retval == JSDTException.NO_REGISTRY) {
                    throw new NoRegistryException();
                } else {
                    error("NamingProxy: bind: ",
                          "impl.unknown.exception.type", retval);
                }
            }
        } catch (IOException e) {
            proxyThread.finishReply();
            throw new ConnectionException();
        }

        try {
            int    port        = urlString.getPort();
            String connectType = urlString.getConnectionType();
            String objectType  = urlString.getObjectType();
            String objectName  = urlString.getObjectName();
            String className   = "com.sun.media.jsdt." + connectType + "." +
                                  connectType + objectType;
            Class  c           = object.getClass();

            while (c != null) {
                if (c == Util.getClassForName(className)) {
                    if (Util.isSession(urlString)) {
                        socketSession session = (socketSession) object;

                        if (isServer) {
                            setupSessionProxy(session, urlString, objectName,
                                              no, connectType, host, port);
                        } else {
                            session._createServer(objectName, no,
                                                  connectType, name, port);
                        }
                    } else if (Util.isClient(urlString)) {
                        ((socketClient) object)._createServer(objectName,
                                                              name, port);
                    } else {
                        throw new InvalidURLException();
                    }

                    try {
                        proxyThread.writeMessageHeader(proxyThread.dataOut,
                                    (short) 1, id, type, T_Bind, true, true);
                        proxyThread.dataOut.writeShort(no);
                        proxyThread.dataOut.writeUTF(name);
                        proxyThread.dataOut.writeUTF(Util.getIPAddress(host));
                        proxyThread.dataOut.writeUTF(Util.getClientName(client));
                        proxyThread.dataOut.writeInt(authAction);
                        proxyThread.flush();
                        message = proxyThread.waitForReply();

                        in     = message.thread.dataIn;
                        retval = in.readInt();
                        proxyThread.finishReply();

                        if (retval != 0) {
                            if (retval == JSDTException.NO_REGISTRY) {
                                throw new NoRegistryException();
                            } else if (retval == JSDTException.INVALID_URL) {
                                throw new InvalidURLException();
                            } else if (retval == JSDTException.ALREADY_BOUND) {
                                throw new AlreadyBoundException();
                            } else {
                                error("NamingProxy: bind: ",
                                      "impl.unknown.exception.type", retval);
                            }
                        }
                    } catch (IOException e) {
                        proxyThread.finishReply();
                        throw new ConnectionException();
                    }

                    return;
                } else {
                    c = c.getSuperclass();
                }
            }

            throw new InvalidURLException();
        } catch (InvalidURLException iue) {
            throw iue;
        } catch (ClassNotFoundException cnfe) {
            throw new InvalidURLException();
        } catch (PortInUseException piue) {
            try {
                unbind(urlString, object, client);
            } catch (JSDTException je) {
                error("NamingProxy: bind: ", je);
            }
            throw piue;
        } catch (Exception e) {
            error("NamingProxy: bind: ", e);
        }
    }


/**
 * <A NAME="SD_CHECKFORSERVER"></A>
 * <EM>checkForServer</EM> returns an indication of whether there is already
 * a server running on the specified address/port in a different VM.
 *
 * @param address the address of the potential server.
 * @param port the port number of the potential server.
 *
 * @exception ConnectionException if a connection error occured.
 * @exception NoRegistryException if no Registry process is running.
 * @exception TimedOutException if no reply was received for this operation
 * in the given timeout period.
 *
 * @return true if there is already a server running on the specified
 * host/port.
 */

    private boolean
    checkForServer(String address, int port)
        throws ConnectionException, NoRegistryException, TimedOutException {
        URLString[] names = list();
        boolean     reply = false;

        if (NamingProxy_Debug) {
            debug("NamingProxy: checkForServer:" +
                  " address: " + address +
                  " port: "    + port);
        }

        for (int i = 0; i < names.length; i++) {
            if (names[i].getHostAddress().equals(address) &&
                names[i].getPort() == port &&
                Util.isSession(names[i])) {
                if (!sessions.containsKey(names[i].toString())) {
                    reply = true;
                    break;
                }
            }
        }

        return(reply);
    }


/**
 * <A NAME="SD_SETUPSESSIONPROXY"></A>
 * <EM>setupSessionProxy</EM> creates a session proxy to an existing server,
 * and sends a message asking it to create another Session on the same port.
 *
 * @param session the socket session object.
 * @param urlString the JSDT URL string associated with this session.
 * @param name the name of the session being constructed.
 * @param sessionNo the unique session number for this session name.
 * @param connectionType the type of this connection.
 * @param host the host of the server-side connection.
 * @param port the port number to use for the connection.
 *
 * @exception ConnectionException if a connection error occured.
 * @exception InvalidURLException if the session couldn't be created on the
 * server.
 * @exception NoRegistryException if no Registry process is running.
 * @exception TimedOutException if no reply was received for this operation
 * in the given timeout period.
 *
 * @return true if there is already a server running on the specified
 * host/port.
 */

    private void
    setupSessionProxy(socketSession session, URLString urlString, String name,
                      short sessionNo, String connectionType,
                      String host, int port)
        throws ConnectionException, InvalidURLException,
               NoSuchHostException, NoRegistryException,
               TimedOutException {
        SessionProxy    sp;
        SocketThread    t;
        DataInputStream in;
        Message         message;
        int             retval;
        String          url = urlString.toString();

        if (NamingProxy_Debug) {
            debug("NamingProxy: setupSessionProxy:" +
                  " session: "         + session +
                  " url string: "      + url +
                  " object name: "     + name +
                  " session #: "       + sessionNo +
                  " connection type: " + connectionType +
                  " host: "            + host +
                  " port: "            + port);
        }

        session._createProxy(this, url, name, sessionNo,
                             connectionType, host, port);
        sessions.put(Util.adjustURLString(url, urlString.getHostAddress()),
                     session);

        try {
            sp = (SessionProxy) session.po.getProxy();
            t  = sp.proxyThread;

            t.writeMessageHeader(t.dataOut, sessionNo, t.getId(),
                           SessionImpl.M_Session, T_CreateSession, true, true);
            t.dataOut.writeUTF(url);
            t.flush();
            message = t.waitForReply();

            in     = message.thread.dataIn;
            retval = in.readInt();
            message.thread.finishReply();

            if (retval != 0) {
                switch (retval) {
                    case JSDTException.NO_SUCH_SESSION:
                        throw new InvalidURLException();
                    default:
                        error("NamingProxy: setupSessionProxy: ",
                              "impl.unknown.exception.type", retval);
                }
            }
        } catch (IOException e) {
            proxyThread.finishReply();
            throw new ConnectionException();
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
        DataInputStream  in;
        Message          message;
        int              retval;
        String           name       = urlString.toString();
        String           host       = urlString.getHostName();
        int              id         = proxyThread.getId();
        String           objectName = urlString.getObjectName();
        int              authAction = 0;

        if (NamingProxy_Debug) {
            debug("NamingProxy: unbind:" +
                  " url string: " + urlString +
                  " object: "     + object +
                  " client: "     + client);
        }

        if (Util.isSession(urlString)) {
            authAction = AuthenticationInfo.DESTROY_SESSION;
        } else if (Util.isClient(urlString)) {
            authAction = AuthenticationInfo.DESTROY_CLIENT;
        }

        if (isManaged()) {
            if (!authenticateClient(urlString, authAction, client)) {
                throw new PermissionDeniedException();
            }
        }

        try {
            JSDTSecurity.enablePrivilege.invoke(JSDTSecurity.privilegeManager,
                                                JSDTSecurity.connectArgs);
        } catch (Exception e) {
        }

        try {
            proxyThread.writeMessageHeader(proxyThread.dataOut, (short) 1,
                                         id, T_Registry, T_Unbind, true, true);
            proxyThread.dataOut.writeUTF(name);
            proxyThread.dataOut.writeUTF(Util.getIPAddress(host));
            proxyThread.dataOut.writeUTF(Util.getClientName(client));
            proxyThread.dataOut.writeInt(authAction);
            proxyThread.flush();
            message = proxyThread.waitForReply();

            in     = message.thread.dataIn;
            retval = in.readInt();
            proxyThread.finishReply();

            if (retval != 0) {
                if (retval == JSDTException.NO_REGISTRY) {
                    throw new NoRegistryException();
                } else if (retval == JSDTException.INVALID_URL) {
                    throw new InvalidURLException();
                } else if (retval == JSDTException.NOT_BOUND) {
                    throw new NotBoundException();
                } else {
                    error("NamingProxy: unbind: ",
                          "impl.unknown.exception.type", retval);
                }
            }

            if (Util.isSession(urlString)) {
                cleanupSession(Util.adjustURLString(objectName,
                               urlString.getHostAddress()));
            } else if (Util.isClient(urlString)) {
                cleanupClient(name, (Client) object, id);
            } else {
                throw new InvalidURLException();
            }
        } catch (IOException e) {
            proxyThread.finishReply();
            throw new ConnectionException();
        }
    }


/**
 * <A NAME="SD_LOOKUP"></A>
 * <EM>lookup</EM> returns the JSDT object for the given name.
 *
 * The name should be of the form:
 *     jsdt://&lt;machine&gt;[:&lt;port&gt;]/&lt;connectType&gt;/&lt;objectType&gt;/&lt;objectName&gt;
 *
 * where valid object types are "Session" and "Client".
 *
 * For example:
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
        throws ConnectionException, NoRegistryException,
               InvalidURLException, NotBoundException,
               TimedOutException {
        DataInputStream in;
        Message         message;
        int             retval;
        String          name        = urlString.toString();
        int             id          = proxyThread.getId();
        String          connectType = urlString.getConnectionType();
        String          objectName  = urlString.getObjectName();
        String          host        = urlString.getHostName();
        int             port        = urlString.getPort();
        short           no          = 0;

        if (NamingProxy_Debug) {
            debug("NamingProxy: lookup:" +
                  " url string: " + urlString);
        }

        try {
            JSDTSecurity.enablePrivilege.invoke(JSDTSecurity.privilegeManager,
                                                JSDTSecurity.connectArgs);
        } catch (Exception e) {
        }

        try {
            proxyThread.writeMessageHeader(proxyThread.dataOut, (short) 1,
                                         id, T_Registry, T_Lookup, true, true);
            proxyThread.dataOut.writeUTF(name);
            proxyThread.dataOut.writeUTF(Util.getIPAddress(host));
            proxyThread.flush();
            message = proxyThread.waitForReply();

            in     = message.thread.dataIn;
            retval = in.readInt();
            if (retval == 0) {
                no = in.readShort();
            }
            proxyThread.finishReply();

            if (retval != 0) {
                if (retval == JSDTException.NO_REGISTRY) {
                    throw new NoRegistryException();
                } else if (retval == JSDTException.INVALID_URL) {
                    throw new InvalidURLException();
                } else if (retval == JSDTException.NOT_BOUND) {
                    throw new NotBoundException();
                } else {
                    error("NamingProxy: lookup: ",
                          "impl.unknown.exception.type", retval);
                }
            }
        } catch (IOException e) {
            proxyThread.finishReply();
            throw new ConnectionException();
        }

        try {
            if (Util.isSession(urlString)) {
                socketSession session;

                synchronized (sessions) {
                    String url = Util.adjustURLString(name,
                                                urlString.getHostAddress());

                    session = (socketSession) sessions.get(url);
                    if (session == null) {
                        String className = "com.sun.media.jsdt." + connectType +
                                           "." + connectType + "Session";

                        session = (socketSession)
                                Util.getClassForName(className).newInstance();

                        sessions.put(url, session);
                        session._createProxy(this, name, objectName,
                                             no, connectType, host, port);
                    }
                }
                return(session);
            } else if (Util.isClient(urlString)) {
                socketClient client;

                synchronized (clients) {
                    client = (socketClient) clients.get(name);
                    if (client == null) {
                        String className = "com.sun.media.jsdt." + connectType +
                                           "." + connectType + "Client";

                        client = (socketClient)
                                Util.getClassForName(className).newInstance();

                        clients.put(name, client);
                        client._createProxy(this, name, objectName, host, port);
                    }
                }
                return(client);
            } else {
                throw new InvalidURLException();
            }
        } catch (InvalidURLException iue) {
            throw iue;
        } catch (Exception e) {
            error("NamingProxy: lookup: ", e);
        }

        return(null);
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
 * given type on the given host:port.
 * @exception TimedOutException if no reply was received for this operation
 * in the given timeout period.
 */

    public void
    addRegistryListener(RegistryListener listener)
        throws ConnectionException, NoRegistryException, TimedOutException {
        if (NamingProxy_Debug) {
            debug("NamingProxy: addRegistryListener:" +
                  " listener: " + listener);
        }

        try {
            sendListenerMessage(listener, T_AddListener);
        } catch (NoSuchListenerException e) {
        }

/* By default, a listener will listen for every type of event, so set all
 * bits in the event mask. Note that there is currently no API calls to
 * adjust the mask for a Registry listener, but we might as well add in the
 * hooks now.
 */

        synchronized (listeners) {
            listeners.put(listener, 0XFFFFFFFF);
        }

        synchronized (this) {
            notifyAll();
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
 * given type on the given host:port.
 * @exception NoSuchListenerException if this SessionListener doesn't exist.
 * @exception TimedOutException if no reply was received for this operation
 * in the given timeout period.
 */

    public void
    removeRegistryListener(RegistryListener listener)
        throws ConnectionException, NoRegistryException,
               NoSuchListenerException, TimedOutException {
        if (NamingProxy_Debug) {
            debug("NamingProxy: removeRegistryListener:" +
                  " listener: " + listener);
        }

        sendListenerMessage(listener, T_RemoveListener);

        synchronized (listeners) {
            Object reply = listeners.remove(listener);

            if (reply == null) {
                throw new NoSuchListenerException();
            }
        }

        synchronized (this) {
            notifyAll();
        }
    }


/**
 * <A NAME="SD_SENDLISTENERMESSAGE"></A>
 * send a listener related message to the Registry.
 *
 * @param action the message action.
 *
 * @exception ConnectionException if a connection error occured.
 * @exception NoSuchListenerException if this SessionListener doesn't exist.
 * @exception NoRegistryException if there is no Registry running of the
 * given type on the given host:port.
 */

    private void
    sendListenerMessage(RegistryListener listener, char action)
        throws ConnectionException, NoSuchListenerException,
               NoRegistryException {
        DataInputStream in;
        Message         message;
        int             retval;
        long            currentTime;
        int  id      = proxyThread.getId();
        long period  = Util.getLongProperty("cleanupPeriod", cleanupPeriod);

        if (NamingProxy_Debug) {
            debug("NamingProxy: sendListenerMessage:" +
                  " listener: " + listener +
                  " action: "   + action);
        }

        try {
            proxyThread.writeMessageHeader(proxyThread.dataOut, (short) 1,
                                           id, T_Registry, action, true, true);
            proxyThread.dataOut.writeUTF(listener.toString());
            proxyThread.flush();
            message = proxyThread.waitForReply();

            in     = message.thread.dataIn;
            retval = in.readInt();
            proxyThread.finishReply();
            lastPingTime = System.currentTimeMillis();
            maybeFailure = false;

            if (retval != 0) {
                switch (retval) {
                    case JSDTException.NO_REGISTRY:
                        throw new NoRegistryException();
                    case JSDTException.NO_SUCH_LISTENER:
                        throw new NoSuchListenerException();
                    default:
                        error("NamingProxy: sendListenerMessage: ",
                              "impl.unknown.exception.type", retval);
                }
            }
        } catch (IOException e) {
            proxyThread.finishReply();
            informListeners(null, null, ConnectionEvent.CONNECTION_FAILED);
            throw new ConnectionException();
        } catch (TimedOutException toe) {
            proxyThread.finishReply();
            maybeFailure = true;
            currentTime = System.currentTimeMillis();
            if (currentTime - lastPingTime > period) {
                informListeners(null, null, ConnectionEvent.CONNECTION_FAILED);
                maybeFailure = false;
                lastPingTime = System.currentTimeMillis();
            }
        }
    }


/**
 * <A NAME="SD_INFORMLISTENERS"></A>
 * <EM>informListeners</EM>
 *
 * @param clientName
 * @param resourceName
 * @param type
 */

    final synchronized void
    informListeners(String clientName, URLString resourceName, int type) {
        if (NamingProxy_Debug) {
            debug("NamingProxy: informListeners:" +
                  " client name: "   + clientName +
                  " resource name: " + resourceName +
                  " type: "          + type);
        }

        for (Enumeration e = listeners.elements(), k = listeners.keys();
             e.hasMoreElements();) {
            RegistryListener listener = (RegistryListener) k.nextElement();
            int                mask = (Integer) e.nextElement();

            if ((mask & type) != 0) {
                RegistryEvent event =
                                new RegistryEvent(clientName, resourceName,
                                                  host, port, type);

                RegistryMessage rm = new RegistryMessage(type, listener, event);

                Util.startThread(rm, "RegistryMessageThread", true);

                if (type == RegistryEvent.CONNECTION_FAILED) {

/* Remove this listener, so that continual connection failures aren't given. */

                    listeners.remove(listener);
                }
            }
        }
    }


/**
 * <A NAME="SD_AUTHENTICATECLIENT"></A>
 * <EM>authenticateClient</EM> authenticates this Client to see if it's
 * allowed to do this priviledged operation.
 *
 * @param urlString the URL String for the object.
 * @param action the authentication action.
 * @param client the client that is trying to perform this priviledged
 * operation.
 *
 * @exception ConnectionException if a connection error occured.
 * @exception InvalidClientException if the Client is invalid is some way (ie.
 * its getName() method returns null).
 * @exception NoRegistryException if no Registry process is running.
 * @exception TimedOutException if no reply was received for this operation
 * in the given timeout period.
 *
 * @return true if this Client is permitted to do this operation, false if not.
 */

    private boolean
    authenticateClient(URLString urlString, int action, Client client)
        throws ConnectionException, InvalidClientException,
               NoRegistryException, TimedOutException {
        AuthenticationInfo    info;
        String                clientName    = Util.getClientName(client);
        String                      url           = urlString.toString();
        Message               message;
        DataInputStream       in;
        int                   id            = proxyThread.getId();
        int                   retval;
        boolean               authenticated = false;
        int                   length;
        byte[]                cBytes;
        ByteArrayInputStream  bis;
        ObjectInputStream     ois;
        Object                challenge     = null;
        byte[]                rBytes;
        ByteArrayOutputStream bos           = new ByteArrayOutputStream();
        ObjectOutputStream    oos;
        Object                response      = null;

        if (NamingProxy_Debug) {
            debug("NamingProxy: authenticateClient:" +
                  " url string: " + urlString +
                  " action: "     + action +
                  " client: "     + client);
        }

        try {
            proxyThread.writeMessageHeader(proxyThread.dataOut, (short) 1, id,
                                   T_Registry, T_Authenticate, true, true);
            proxyThread.dataOut.writeUTF(clientName);
            proxyThread.dataOut.writeUTF(url);
            proxyThread.dataOut.writeInt(action);
            proxyThread.flush();
            message = proxyThread.waitForReply();

            in     = message.thread.dataIn;
            retval = in.readInt();
            if (retval == 0) {
                length    = in.readInt();
                cBytes    = message.thread.getData(length);
                bis       = new ByteArrayInputStream(cBytes);
                ois       = new ObjectInputStream(bis);
                challenge = ois.readObject();
            }
            proxyThread.finishReply();

            if (retval != 0) {
                switch (retval) {
                    case JSDTException.NO_REGISTRY:
                        throw new NoRegistryException();
                    default:
                        error("NamingProxy: authenticateClient: ",
                              "impl.unknown.exception.type", retval);
                }
            }

            info = new AuthenticationInfo(null, action, url,
                                          AuthenticationInfo.REGISTRY);
            info.setChallenge(challenge);

            try {
                response = client.authenticate(info);
            } catch (Throwable th) {
                error("NamingProxy: authenticateClient: ",
                      "impl.thrown", th + " by client.");
            }

            proxyThread.writeMessageHeader(proxyThread.dataOut, (short) 1, id,
                                         T_Registry, T_Challenge, true, true);
            proxyThread.dataOut.writeUTF(clientName);
            proxyThread.dataOut.writeUTF(url);
            proxyThread.dataOut.writeInt(action);
            oos = new ObjectOutputStream(bos);
            oos.writeObject(response);
            rBytes = bos.toByteArray();
            proxyThread.dataOut.writeInt(rBytes.length);
            proxyThread.dataOut.write(rBytes, 0, rBytes.length);

            message.thread.flush();
            message = proxyThread.waitForReply();

            in     = message.thread.dataIn;
            retval = in.readInt();
            if (retval == 0) {
                authenticated = in.readBoolean();
            }
            proxyThread.finishReply();

            if (retval != 0) {
                switch (retval) {
                    case JSDTException.NO_REGISTRY:
                        throw new NoRegistryException();
                    default:
                        error("NamingProxy: authenticateClient: ",
                              "impl.unknown.exception.type", retval);
                }
            }
        } catch (IOException e) {
            proxyThread.finishReply();
            throw new ConnectionException();
        } catch (ClassNotFoundException cnfe) {
            error("NamingProxy: authenticateClient: ", cnfe);
        }

        return(authenticated);
    }


/**
 * <A NAME="SD_ISMANAGED"></A>
 * <EM>isManaged</EM> returns an indication of whether the Registry has a
 * manager associated with it.
 *
 * @exception ConnectionException if a connection error occured.
 * @exception NoRegistryException if no Registry process is running.
 * @exception TimedOutException if no reply was received for this operation
 * in the given timeout period.
 *
 * @return true if the Registry is managed; false if not.
 */

    private boolean
    isManaged()
        throws ConnectionException, NoRegistryException, TimedOutException {
        DataInputStream in;
        Message         message;
        int             retval;
        int             id      = proxyThread.getId();
        boolean         managed = false;

        if (NamingProxy_Debug) {
            debug("NamingProxy: isManaged.");
        }

        try {
            proxyThread.writeMessageHeader(proxyThread.dataOut, (short) 1,
                                     id, T_Registry, T_IsManaged, true, true);
            proxyThread.flush();
            message = proxyThread.waitForReply();

            in     = message.thread.dataIn;
            retval = in.readInt();
            if (retval == 0) {
                managed = in.readBoolean();
            }
            proxyThread.finishReply();

            if (retval != 0) {
                switch (retval) {
                    case JSDTException.NO_REGISTRY:
                        throw new NoRegistryException();
                    default:
                        error("NamingProxy: isManaged: ",
                              "impl.unknown.exception.type", retval);
                }
            }
        } catch (IOException e) {
            proxyThread.finishReply();
            throw new ConnectionException();
        }

        return(managed);
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
        DataInputStream  in;
        Message          message;
        int              retval;
        int              n;
        URLString[]      names;
        int              id = proxyThread.getId();

        if (NamingProxy_Debug) {
            debug("NamingProxy: list.");
        }

        try {
            proxyThread.writeMessageHeader(proxyThread.dataOut, (short) 1,
                                           id, T_Registry, T_List, true, true);
            proxyThread.flush();
            message = proxyThread.waitForReply();

            in     = message.thread.dataIn;
            retval = in.readInt();
            n      = in.readInt();
            names  = new URLString[n];

            if (n != 0) {
                for (int i = 0; i < n; i++) {
                    names[i] = new URLString(in.readUTF());
                }
            }
            proxyThread.finishReply();

            if (retval != 0) {
                if (retval == JSDTException.NO_REGISTRY) {
                    throw new NoRegistryException();
                } else {
                    error("NamingProxy: list: ",
                          "impl.unknown.exception.type", retval);
                }
            }
        } catch (IOException e) {
            proxyThread.finishReply();
            throw new ConnectionException();
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

        connectionThread.addConnectionListener(listener);
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

        connectionThread.removeConnectionListener(listener);
    }


/**
 * <A NAME="SD_RUN"></A>
 * <EM>run</EM>
 */

    public void
    run() {
        long alivePeriod = Util.getLongProperty("keepAlivePeriod",
                                                keepAlivePeriod);

        if (NamingProxy_Debug) {
            debug("NamingProxy: run.");
        }

        while (!shutdown) {
            synchronized (this) {
                while (listeners.size() == 0) {
                    try {
                        wait();
                    } catch (InterruptedException ie) {
                        break;
                    }
                }
            }

            if (!shutdown) {
                try {
                    for (Enumeration k = listeners.keys(); k.hasMoreElements();) {
                        RegistryListener listener = (RegistryListener)
                                                        k.nextElement();

                        sendListenerMessage(listener, T_IsAlive);
                    }

                    Thread.sleep(alivePeriod);
                } catch (Exception ie) {
                }
            }
        }

        if (NamingProxy_Debug) {
            debug("NamingProxy: run ended.");
        }
    }

    /**
     * Sets if the thread should shutdown. <p>
     *
     * @param shutdown <b>true</b> if it should shutdown, <b>false</b> if not.
     */
    private void setShutdown(boolean shutdown) {
        this.shutdown = shutdown;
    }
}
