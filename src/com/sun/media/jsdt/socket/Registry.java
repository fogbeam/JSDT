
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
import com.sun.media.jsdt.event.RegistryEvent;
import java.net.*;
import java.io.*;
import java.util.*;

/**
 * JSDT Registry class (socket implementation).
 *
 * A "registry" exists on every node that allows JSDT connections to
 * servers on that node.  The registry on a particular node contains
 * a transient database that maps names to JSDT objects.  When the
 * node boots, the registry database is empty.  The names stored in the
 * registry are pure and are not parsed.  A service storing itself in
 * the registry may want to prefix its name of the service by a package
 * name (although not required), to reduce name collisions in the
 * registry.
 *
 * Based on the RMI Registry class.
 *
 * @version     2.3 - 16th November 2017
 * @author      Rich Burridge
 */

public final class
Registry extends JSDTObject
         implements AbstractRegistry, Runnable, socketDebugFlags {

    // Thread Registry is running in, if started by the RegistryFactory.
    private Thread thread;

    /* The proxy-side thread used to send T_Exists and T_Stop messages to
     * the running Registry process.
     */
    private SocketThread proxyThread;

    // The server-side Registry thread.
    private TargetableThread serverThread = null;

    // The unique number to use for the next Session/Client binding.
    private short currentNo = 1;

    // The server socket to await connections on.
    private ServerSocket ssock;

    // The url keys for the thread values currently bound in the registry.
    private final Hashtable<String, JSDTThread> bindings;

    // The url keys for the unique session/client number values.
    private final Hashtable<String, Short> values;

    // An instance of the socket factory for creating server sockets.
    private JSDTSocketFactory factory = null;

    // A list of addresses that this host is known by.
    private String addresses[] = null;

    // The manager associated with this Registry (if any).
    private RegistryManager manager = null;

    // The listeners currently listening to this object.
    private Hashtable<String, JSDTListenerImpl> listeners = null;

    // The listeners and their associated threads.
    private Hashtable<JSDTThread, ServerListenerThread> listenerThreads = null;

    // Hashtable of client authentication id keys for client values.
    private final Hashtable<Integer, RegistryClient> idClients;

    // A boolean to signal if we should shutdown the thread.
    private boolean shutdown = false;

/**
 * <A NAME="SD_REGISTRY"></A>
 * <EM>Registry</EM> the constructor for the JSDT socket based registry.
 */

    public
    Registry() {
        String factoryClass = Util.getStringProperty("socketFactoryClass",
                                                     socketFactoryClass);

        if (Registry_Debug) {
            debug("Registry: constructor.");
        }

        try {
            factory = (JSDTSocketFactory)
                      Util.getClassForName(factoryClass).newInstance();
        } catch (Exception e) {
            error("Registry: constructor: ", e);
        }

        addresses       = createAddressList();
        bindings        = new Hashtable<>();
        values          = new Hashtable<>();
        listeners       = new Hashtable<>();
        listenerThreads = new Hashtable<>();
        idClients       = new Hashtable<>();
    }


/**
 * <A NAME="SD_CREATEADDRESSLIST"></A>
 * <EM>createAddressList</EM> generate a list of all the IP addresses the
 * current host is known by.
 *
 * @return a list of addresses for this host.
 */

    private String[]
    createAddressList() {
        if (Registry_Debug) {
            debug("Registry: createAddressList.");
        }

        try {
            InetAddress   localHost = InetAddress.getLocalHost();
            String        localName = localHost.getHostName();

            if (Registry_Debug) {
                debug("Registry: createAddressList:" +
                      " localHost: "     + localHost +
                      " localHostName: " + localName);
            }

            InetAddress[] inetAddrs = InetAddress.getAllByName(localName);

            addresses = new String[inetAddrs.length];

            for (int i = 0; i < inetAddrs.length; i++) {
                addresses[i] = inetAddrs[i].getHostAddress();

                if (Registry_Debug) {
                    debug("Registry: createAddressList:" +
                          " inet addr[" + i + "] " + inetAddrs[i] +
                          " ip["        + i + "] " + addresses[i]);
                }
            }
        } catch (Exception e) {
            error("Registry: createAddressList: ", e);
        }

        return(addresses);
    }


/**
 * <A NAME="SD_NAMEEXISTS"></A>
 * <EM>nameExists</EM> check to see if there is already a bound object in
 * the Registry with this name. All the alternate IP addresses for this
 * host are substituted for the host portion of the JSDT URL.
 *
 * @param name the name of the entry to look for.
 *
 * @return the actual bound name in the Registry if it exists, or null if
 * no entry was found for this name (with all it's alternate IP addresses).
 */

    private String
    nameExists(String name) {
        String boundName = null;

        if (Registry_Debug) {
            debug("Registry: nameExists:" +
                  " name: " + name);
        }

        for (int i = 0; i < addresses.length; i++) {
            String currentName = Util.adjustURLString(name, addresses[0]);

            if (bindings.get(currentName) != null) {
                boundName = currentName;
                break;
            }
        }

        return(boundName);
    }


/**
 * <A NAME="SD_STARTSERVER"></A>
 * <EM>startServer</EM> start up the Registry server socket.
 */

    private void
    startServer() {
        int port = Util.getIntProperty("registryPort", registryPort);

        if (Registry_Debug) {
            debug("Registry: startServer.");
        }

        try {
            ssock = factory.createServerSocket(port);
        } catch (Exception e) {
            PrintStream stream = getDebugStream();

            error("Registry: startServer: ", e);
            stream.println(JSDTI18N.getResource("impl.registry"));
        }
    }


/**
 * <A NAME="SD_STARTREGISTRY"></A>
 * <EM>startRegistry</EM> is a class method that will start a Registry of
 * the appropriate type. The Registry is started in a separate thread.
 * It can be stopped with the stopRegistry method. If the process that
 * started it terminates, then the Registry thread is terminated too.
 *
 * @param registryType the type of Registry to start.
 * @param port the port number that the Registry should run on.
 *
 * @exception RegistryExistsException if a Registry (or some other process)
 * is already running on the port used by the Registry on this machine.
 * @exception NoRegistryException if an invalid registry type was given.
 *
 * @since       JSDT 1.5
 */

    public void
    startRegistry(String registryType, int port)
        throws RegistryExistsException, NoRegistryException {
        if (Registry_Debug) {
            debug("Registry: startRegistry:" +
                  " registry type: " + registryType +
                  " port number: "   + port);
        }

        thread = Util.startThread(this,
                          "RegistryThread:" + registryType + ":" + port, false);
    }


/**
 * <A NAME="SD_STOPREGISTRY"></A>
 * <EM>stopRegistry</EM> is a class method that will stop a Registry of
 * the appropriate type. If the registry was started in this VM, then it
 * will just stop the Registry thread, else it will get a proxy-side
 * connection to the running registry. Then it will send it a T_Stop message,
 * and wait for a reply. If it doesn't get a reply (TimedOutException or
 * IOException), then it will throw a NoRegistryException. If it does get
 * a reply, it checks that the retval is zero. If the registry was started
 * with startRegistry(), then it will just stop that thread; otherwise it
 * will do a System.exit(0) to terminate the Registry application.
 *
 * @param registryType the type of Registry to start, (ie. "socket").
 * @param port the port number that the Registry is running on.
 *
 * @exception NoRegistryException if an invalid registry type was given,
 * or the Registry is not running, or wasn't started by the startRegistry
 * method.
 *
 * @since       JSDT 1.5
 */

    public void
    stopRegistry(String registryType, int port) throws NoRegistryException {
        DataInputStream in;
        Message         message;

        if (Registry_Debug) {
            debug("Registry: stopRegistry:" +
                  " registry type: " + registryType +
                  " port number: "   + port);
        }

        try {
            if (thread != null) {
                if (serverThread != null) {
                    RegistryServerThread rst = (RegistryServerThread)
                                                serverThread.getTarget();

                    rst.setShutdown(true);
                    serverThread.interrupt();
                    serverThread = null;
                }

                setShutdown(true);
                ssock.close();
                ssock  = null;
                thread = null;
            } else {
                if (proxyThread == null) {
                    InetAddress address  = InetAddress.getLocalHost();
                    String      hostName = address.getHostName();

                    initProxy(hostName, port);
                }

                proxyThread.writeMessageHeader(proxyThread.dataOut, (short) 1,
                                               proxyThread.getId(),
                                               T_Registry, T_Stop, true, true);
                proxyThread.flush();
                message = proxyThread.waitForReply();

                in     = message.thread.dataIn;
                in.readInt();
                proxyThread.finishReply();
                proxyThread = null;
            }
        } catch (TimedOutException | IOException e) {
            throw new NoRegistryException();
        }
    }


/**
 * <A NAME="SD_ADDLISTENER"></A>
 * <EM>addListener</EM>
 *
 * @param message
 */

    void
    addListener(Message message) {
        JSDTThread                          t            = message.thread;
        DataInputStream                     in           = t.dataIn;
        int                                 retval       = 0;
        String                              listenerName = null;
        JSDTListenerImpl                    listener     = null;
        boolean                             putBack      = false;
        ServerListenerThread                listenerThread;
        Hashtable<String, JSDTListenerImpl> threadListeners;

        if (Registry_Debug) {
            debug("Registry: addListener:" +
                  " message: " + message);
        }

        try {
            listenerName = in.readUTF();
            listener     = new JSDTListenerImpl(listenerName, null);
        } catch (IOException e) {
            error("Registry: addListener: ", e);
        }

        listeners.put(listenerName, listener);

        if ((listenerThread = listenerThreads.get(t)) == null) {
            listenerThread = new ServerListenerThread(this.toString());
            putBack = true;
        }

        threadListeners = listenerThread.getListeners();
        threadListeners.put(listenerName, listener);
        if (putBack) {
            listenerThreads.put(message.thread, listenerThread);
        }

        try {
            t.writeMessageHeader(t.dataOut, (short) 1, message.id,
                                 message.type, message.action, false, true);
            t.dataOut.writeInt(retval);
            t.flush();
            t.finishMessage();
        } catch (IOException e) {
            error("Registry: addListener: ", e);
        }
    }


/**
 * <A NAME="SD_REMOVELISTENER"></A>
 * <EM>removeListener</EM>
 *
 * @param message
 */

    void
    removeListener(Message message) {
        JSDTThread       t            = message.thread;
        DataInputStream  in           = t.dataIn;
        String           listenerName = null;
        int              retval       = 0;
        int              noListeners;

        if (Registry_Debug) {
            debug("Registry: removeListener:" +
                  " message: " + message);
        }

        try {
            listenerName = in.readUTF();
        } catch (IOException e) {
            error("Registry: removeListener: ", e);
        }

        if (listeners.remove(listenerName) == null) {
            retval = JSDTException.NO_SUCH_LISTENER;
        }

        if (retval == 0) {
            ServerListenerThread listenerThread = listenerThreads.get(t);

            if (listenerThread != null) {
                Hashtable threadListeners = listenerThread.getListeners();

                threadListeners.remove(listenerName);
                noListeners = threadListeners.size();

                if (noListeners == 0) {
                    listenerThreads.remove(t);
                }
            }
        }

        try {
            t.writeMessageHeader(t.dataOut, (short) 1, message.id,
                                 message.type, message.action, false, true);
            t.dataOut.writeInt(retval);
            t.flush();
            t.finishMessage();
        } catch (IOException e) {
            error("Registry: removeListener: ", e);
        }
    }


/**
 * <A NAME="SD_ISALIVE"></A>
 * <EM>isAlive</EM> replies to "is alive" messages from the various proxies.
 *
 * @param message the current message being processed.
 */

    void
    isAlive(Message message) {
        JSDTThread      t      = message.thread;
        DataInputStream in     = t.dataIn;
        int             retval = 0;

        if (Registry_Debug) {
            debug("Registry: isAlive:" +
                  " message: " + message);
        }

        try {
            in.readUTF();                     // listener name.
            t.writeMessageHeader(t.dataOut, (short) 1, message.id,
                                 message.type, message.action, false, true);
            t.dataOut.writeInt(retval);
            t.flush();
            t.finishMessage();
        } catch (IOException e) {
            error("Registry: isAlive: ", e);
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

    private void
    informListeners(String clientName, String resourceName, int type) {
        int eventType = 0;

        if (Registry_Debug) {
            debug("Registry: informListeners:" +
                  " client name: "   + clientName +
                  " resource name: " + resourceName +
                  " type: "          + type);
        }

        switch (type) {
            case AuthenticationInfo.CREATE_SESSION:
                eventType = RegistryEvent.SESSION_CREATED;
                break;
            case AuthenticationInfo.DESTROY_SESSION:
                eventType = RegistryEvent.SESSION_DESTROYED;
                break;
            case AuthenticationInfo.CREATE_CLIENT:
                eventType = RegistryEvent.CLIENT_CREATED;
                break;
            case AuthenticationInfo.DESTROY_CLIENT:
                eventType = RegistryEvent.CLIENT_DESTROYED;
                break;
        }

        for (Enumeration k = listenerThreads.keys(); k.hasMoreElements();) {
            SocketThread thread = (SocketThread) k.nextElement();

            try {
                thread.writeMessageHeader(thread.dataOut, (short) 1, 0,
                                   T_Registry, T_InformListener, false, true);
                thread.dataOut.writeUTF(resourceName);
                thread.dataOut.writeUTF(clientName);
                thread.dataOut.writeInt(eventType);
                thread.flush();
                thread.finishMessage();
            } catch (IOException ioe) {
                error("Registry: informListeners: ", ioe);
            }
        }
    }


/**
 * <A NAME="SD_ATTACHMANAGER"></A>
 * <EM>attachManager</EM> attachs a manager to this Registry. This manager
 * will authenticate clients trying to create or destroy Sessions or Clients
 * in this Registry.
 *
 * @param manager the registry manager to attach.
 *
 * @exception ManagerExistsException if there is already a manager associated
 * with this manageable object.
 */

    public void
    attachManager(RegistryManager manager) throws ManagerExistsException {
        if (Registry_Debug) {
            debug("Registry: attachManager:" +
                  " manager: " + manager);
        }

        if (this.manager != null) {
            throw new ManagerExistsException();
        } else {
            this.manager = manager;
        }
    }


/**
 * <A NAME="SD_ISMANAGED"></A>
 * <EM>isManaged</EM>
 *
 * @param message
 */

    void
    isManaged(Message message) {
        JSDTThread t      = message.thread;
        int        retval = 0;

        if (Registry_Debug) {
            debug("Registry: isManaged:" +
                  " message: " + message);
        }

        try {
            t.writeMessageHeader(t.dataOut, (short) 1, message.id,
                                 message.type, message.action, false, true);
            t.dataOut.writeInt(retval);
            t.dataOut.writeBoolean(manager != null);
            t.flush();
            t.finishMessage();
        } catch (IOException e) {
            error("Registry: isManaged: ", e);
        }
    }


/**
 * <A NAME="SD_AUTHENTICATE"></A>
 * <EM>authenticate</EM>
 *
 * @param message
 */

    void
    authenticate(Message message) {
        RegistryClient client = new RegistryClient(message, manager);

        if (Registry_Debug) {
            debug("Registry: authenticate:" +
                  " message: " + message);
        }

        Util.startThread(client,
                         "RegistryClientThread:" + client.getName(), true);

        synchronized (idClients) {
            idClients.put(message.id, client);
        }
    }


/**
 * <A NAME="SD_CHALLENGE"></A>
 * <EM>challenge</EM>
 *
 * @param message
 */

    void
    challenge(Message message) {
        RegistryClient client;
        Integer        idValue = message.id;

        if (Registry_Debug) {
            debug("Registry: challenge:" +
                  " message: " + message);
        }

        synchronized (idClients) {
            client = idClients.get(idValue);
            idClients.remove(idValue);
        }

        client.challenge(message);
    }


/**
 * <A NAME="SD_STOPREGISTRY"></A>
 * <EM>stopRegistry</EM>
 *
 * @param message
 */

    void
    stop(Message message) {
        JSDTThread t = message.thread;

        if (Registry_Debug) {
            debug("Registry: stop:" +
                  " message: " + message);
        }

        try {
            t.writeMessageHeader(t.dataOut, (short) 1, message.id,
                                 T_Registry, T_Stop, false, true);
            t.dataOut.writeInt(0);
            t.flush();
            t.finishMessage();

            if (thread != null) {
                if (serverThread != null) {
                    RegistryServerThread rst = (RegistryServerThread)
                                                serverThread.getTarget();

                    rst.setShutdown(true);
                    serverThread.interrupt();
                    serverThread = null;
                }

                setShutdown(true);
                ssock.close();
                ssock = null;
                thread = null;
            } else {
                System.exit(0);
            }
        } catch (IOException ioe) {
            error("Registry: stop: ", ioe);
        }
    }


/**
 * <A NAME="SD_REGISTRYEXISTS"></A>
 * <EM>registryExists</EM> checks if a Registry, of the given registry
 * type, is already running on the given port.  If the registry was
 * started in this VM, then return true, otherwise get a proxy-side
 * connection to the running registry, and send it a T_Exists message,
 * and wait for a reply. If it doesn't get a reply (TimedOutException
 * or IOException), then return false. If it does get a reply, and the
 * retval is 0, then return true.
 *
 * @param registryType the type of Registry to check on.
 * @param port the port number that the Registry is running on.
 *
 * @return true if a Registry is already running; false if it isn't.
 *
 * @exception NoRegistryException if an invalid registry type was given.
 *
 * @since       JSDT 1.5
 */

    public boolean
    registryExists(String registryType, int port) throws NoRegistryException {
        DataInputStream in;
        Message         message;
        int             retval;
        boolean         reply = false;

        if (Registry_Debug) {
            debug("Registry: registryExists:" +
                  " registry type: " + registryType +
                  " port number: "   + port);
        }

        if (!registryType.equals("socket")) {
            throw new NoRegistryException();
        }

        if (ssock != null) {
            reply = true;
        } else {
            try { 
                if (proxyThread == null) {
                    InetAddress address  = InetAddress.getLocalHost();
                    String      hostName = address.getHostName();

                    initProxy(hostName, port);
                }

                proxyThread.writeMessageHeader(proxyThread.dataOut, (short) 1,
                                       proxyThread.getId(),
                                       T_Registry, T_Exists, true, true);
                proxyThread.flush();
                message = proxyThread.waitForReply();

                in     = message.thread.dataIn;
                retval = in.readInt();
                proxyThread.finishReply();
                reply  = (retval == 0);
            } catch (TimedOutException | IOException e) {
            }
        }

        return(reply);
    }


/**
 * <A NAME="SD_EXISTS"></A>
 * <EM>exists</EM>
 *
 * @param message
 */

    void
    exists(Message message) {
        JSDTThread t = message.thread;

        if (Registry_Debug) {
            debug("Registry: registryExists:" +
                  " message: " + message);
        }

        try {
            t.writeMessageHeader(t.dataOut, (short) 1, message.id,
                                 T_Registry, T_Exists, false, true);
            t.dataOut.writeInt(0);
            t.flush();
            t.finishMessage();
        } catch (IOException ioe) {
            error("Registry: exists: ", ioe);
        }
    }


/**
 * <A NAME="SD_INITPROXY"></A>
 * <EM>initProxy</EM>
 *
 * @param host
 * @param port
 */

    private void
    initProxy(String host, int port)
        throws SocketException, UnknownHostException {
        if (Registry_Debug) {
            debug("Registry: initProxy:" +
                  " host: " + host +
                  " port: " + port);
        }

        proxyThread = new TCPSocketThread(host, port);
        Util.startThread(proxyThread,
                         "RegistryProxyThread" + ":" + host + ":" + port, true);
    }


/**
 * <A NAME="SD_BIND"></A>
 * <EM>bind</EM> binds the name to the specified JSDT object.
 *
 * @param message
 */

    void
    bind(Message message) {
        JSDTThread      t          = message.thread;
        DataInputStream in         = t.dataIn;
        int             retval     = 0;
        String          name;
        String          clientName;
        int             type;
        short           no;

        if (Registry_Debug) {
            debug("Registry: bind:" +
                  " message: " + message);
        }

        try {
            no         = in.readShort();
            name       = Util.adjustURLString(in.readUTF(), in.readUTF());
            clientName = in.readUTF();
            type       = in.readInt();

            synchronized (bindings) {
                if (nameExists(name) != null) {
                    retval = JSDTException.ALREADY_BOUND;
                } else {
                    bindings.put(name, t);
                }
            }

            t.writeMessageHeader(t.dataOut, (short) 1, message.id,
                                 T_Registry, T_Bind, false, true);
            t.dataOut.writeInt(retval);
            if (retval == 0) {
                synchronized (values) {
                    values.put(name, no);
                }
            }
            t.flush();
            t.finishMessage();

            informListeners(clientName, name, type);
        } catch (IOException e) {
            error("Registry: bind: ", e);
        }
    }


/**
 * <A NAME="SD_GETSESSIONNO"></A>
 * <EM>getSessionNo</EM>
 *
 * @param message
 */

    void
    getSessionNo(Message message) {
        JSDTThread t = message.thread;

        if (Registry_Debug) {
            debug("Registry: getsessionNo:" +
                  " message: " + message);
        }

        try {
            t.writeMessageHeader(t.dataOut, (short) 1, message.id,
                                 T_Registry, T_GetSessionNo, false, true);
            t.dataOut.writeInt(0);
            t.dataOut.writeShort(currentNo);
            t.flush();
            t.finishMessage();
        } catch (IOException e) {
            error("Registry: getSessionNo: ", e);
        }

        currentNo++;
    }


/**
 * <A NAME="SD_UNBIND"></A>
 * <EM>unbind</EM>
 *
 * @param message
 */

    void
    unbind(Message message) {
        String          name;
        String          boundName;
        String          clientName;
        int             type;
        JSDTThread      t      = message.thread;
        DataInputStream in     = t.dataIn;
        int             retval = 0;

        if (Registry_Debug) {
            debug("Registry: unbind:" +
                  " message: " + message);
        }

        try {
            name       = Util.adjustURLString(in.readUTF(), in.readUTF());
            clientName = in.readUTF();
            type       = in.readInt();

            synchronized (bindings) {
                if ((boundName = nameExists(name)) == null) {
                    retval = JSDTException.NOT_BOUND;
                } else {
                    bindings.remove(boundName);
                    values.remove(boundName);
                }
            }

            t.writeMessageHeader(t.dataOut, (short) 1, message.id,
                                 T_Registry, T_Unbind, false, true);
            t.dataOut.writeInt(retval);
            t.flush();
            t.finishMessage();

            informListeners(clientName, name, type);
        } catch (IOException e) {
            error("Registry: unbind: ", e);
        }
    }


/**
 * <A NAME="SD_LOOKUP"></A>
 * <EM>lookup</EM>
 *
 * @param message
 */

    void
    lookup(Message message) {
        JSDTThread       t         = message.thread;
        DataInputStream  in        = t.dataIn;
        int              retval    = 0;
        String           name;
        String           boundName;

        if (Registry_Debug) {
            debug("Registry: lookup:" +
                  " message: " + message);
        }

        try {
            name = Util.adjustURLString(in.readUTF(), in.readUTF());

            synchronized (bindings) {
                if ((boundName = nameExists(name)) == null) {
                    retval = JSDTException.NOT_BOUND;
                }
            }

            t.writeMessageHeader(t.dataOut, (short) 1, message.id,
                                 T_Registry, T_Lookup, false, true);
            t.dataOut.writeInt(retval);
            if (retval == 0) {
                t.dataOut.writeShort(values.get(boundName));
            }
            t.flush();
            t.finishMessage();
        } catch (IOException e) {
            error("Registry: lookup: ", e);
        }
    }


/**
 * <A NAME="SD_LIST"></A>
 * <EM>list</EM>
 *
 * @param message
 */

    void
    list(Message message) {
        JSDTThread t = message.thread;

        if (Registry_Debug) {
            debug("Registry: list:" +
                  " message: " + message);
        }

        try {
            synchronized (bindings) {
                int         size  = bindings.size();
                Enumeration elist = bindings.keys();

                t.writeMessageHeader(t.dataOut, (short) 1, message.id,
                                     T_Registry, T_List, false, true);
                t.dataOut.writeInt(0);
                t.dataOut.writeInt(size);

                while ((--size) >= 0) {
                    t.dataOut.writeUTF((String) elist.nextElement());
                }
                t.flush();
                t.finishMessage();
            }
        } catch (IOException e) {
            error("Registry: list: ", e);
        }
    }


/**
 * <A NAME="SD_GETBINDINGS"></A>
 * <EM>getBindings</EM> get the name bindings known to this registry.
 *
 * @return a hashtable containing this registry's name bindings.
 */

    Hashtable
    getBindings() {
        if (Registry_Debug) {
            debug("Registry: getBindings.");
        }

        return(bindings);
    }


/**
 * <A NAME="SD_HANDLEREQUESTS"></A>
 * <EM>handleRequests</EM>
 */

    private void
    handleRequests() {
        if (Registry_Debug) {
            debug("Registry: handleRequests.");
        }

        try {
            while (!shutdown) {
                Socket               sock;
                RegistryServerThread rst;

                if (Registry_Debug) {
                    debug("Registry: handleRequests:" +
                          " Waiting...");
                }
                sock = ssock.accept();
                if (Registry_Debug) {
                    debug("Registry: handleRequests:" +
                          " Got a connection.");
                }

                rst = new RegistryServerThread(sock, this);
                serverThread = (TargetableThread)
                                Util.startThread(rst,
                                        "RegistryServerThread:" + sock, true);
            }
        } catch (Exception e) {
            error("Registry: handleRequests: ", e);
        }
    }


/**
 * <A NAME="SD_RUN"></A>
 * <EM>run</EM>
 */

    public void
    run() {
        if (Registry_Debug) {
            debug("Registry: run.");
        }

        startServer();
        handleRequests();
    }


/**
 * <A NAME="SD_MAIN"></A>
 * <EM>main</EM> the main program to start a registry.
 *
 * @param args
 */

    public static void
    main(String[] args) {
        Registry registry = new Registry();

        if (Registry_Debug) {
            Debug("Registry: main:");
            for (int i = 0; i < args.length ; i++) {
                System.err.println("args[" + i + "]: " + args[i]);
            }
        }

        registry.run();
    }

    /**
     * Sets if we should shutdown the thread. <p>
     *
     * @param shutdown <b>true</b> if we should shutdown, <b>false</b> if not.
     */
    private void setShutdown(boolean shutdown) {
        this.shutdown = shutdown;
    }
}
