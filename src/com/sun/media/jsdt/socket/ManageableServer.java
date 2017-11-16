
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
import java.io.*;
import java.util.*;

/**
 * JSDT server-side manageable parent class.
 *
 * @version     2.3 - 5th November 2017
 * @author      Rich Burridge
 */

class
ManageableServer extends JSDTObject
                 implements AbstractManageableServer, socketDebugFlags {

    // The name of this server object.
    protected String name = null;

    // The server-side handle to the Session.
    protected Session session;

    // The server-side handle to the Session Server.
    protected SessionServer ss;

    // The clients currently attached to this object.
    protected Hashtable<String, Client> clients = null;

    // The listeners currently listening to this object.
    protected Hashtable<String, JSDTListenerImpl> listeners = null;

    // The thread/client-name pairs for clients joined to this object.
    Hashtable<String, JSDTThread> clientConnections = null;

    /* Thread connections back to the proxy for the clients potentially
     *  joining this object.
     */
    Hashtable<String, Message> joiningMessages;

    // The server-side manager (if any).
    protected JSDTManagerImpl manager = null;

    // The event mask associated with the manager (if any).
    private int eventMask = 0XFFFFFFFF;

    // Thread connection back to the proxy running the manager.
    private JSDTThread managerThread;

    // The listeners and their associated threads.
    protected Hashtable<JSDTThread, ServerListenerThread> listenerThreads = null;


/**
 * <A NAME="SD_INITSERVER"></A>
 * <EM>initServer</EM> initialise the "server-side" for this object.
 *
 * @param name the name of the server object being constructed.
 * @param session the server-side session the object belongs to.
 * @param object the server-side object.
 */

    public void
    initServer(String name, SessionImpl session, Object object) {
        if (ManageableServer_Debug) {
            debug("ManageableServer: initServer:" +
                  " name: "    + name +
                  " session: " + session +
                  " object: "  + object);
        }

        this.session = session;

        if (session.so != null) {
            ss = (SessionServer) session.so.getServer();
        }

        clientConnections = new Hashtable<>();
        joiningMessages   = new Hashtable<>();
        listeners         = new Hashtable<>();
        listenerThreads   = new Hashtable<>();
    }


/**
 * <A NAME="SD_GETSERVER"></A>
 * <EM>getServer</EM>
 *
 * @return
 */

    public Object
    getServer() {
        if (ManageableServer_Debug) {
            debug("ManageableServer: getServer.");
        }

        error("ManageableServer: getServer: ",
              "impl.subclass");
        return(null);
    }


/**
 * <A NAME="SD_ADDCLIENTTHREADCONNECTION"></A>
 * <EM>addClientThreadConnection</EM>
 *
 * @param clientName
 * @param thread
 */

    final void
    addClientThreadConnection(String clientName, JSDTThread thread) {
        if (ManageableServer_Debug) {
            debug("ManageableServer: addClientThreadConnection:" +
                  " client name: " + clientName +
                  " thread: "      + thread);
        }

        clientConnections.put(clientName, thread);
    }


/**
 * <A NAME="SD_ADDLISTENER"></A>
 * <EM>addListener</EM>
 *
 * @param message
 */

    protected final void
    addListener(Message message) {
        DataInputStream                     in = message.thread.dataIn;
        String                              listenerName = null;
        JSDTListenerImpl                    listener     = null;
        boolean                             putBack      = false;
        ServerListenerThread                listenerThread;
        Hashtable<String, JSDTListenerImpl> threadListeners;

        if (ManageableServer_Debug) {
            debug("ManageableServer: addListener:" +
                  " message: " + message);
        }

        try {
            listenerName = in.readUTF();
            listener     = new JSDTListenerImpl(listenerName, null);
            listeners.put(listenerName, listener);
        } catch (IOException e) {
            error("ManageableServer: addListener: ", e);
        }

        if ((listenerThread = listenerThreads.get(message.thread)) == null) {
            listenerThread = new ServerListenerThread(name);
            putBack = true;
        }

        threadListeners = listenerThread.getListeners();
        threadListeners.put(listenerName, listener);
        if (putBack) {
            listenerThreads.put(message.thread, listenerThread);
        }

        try {
            message.thread.writeMessageHeader(message.thread.dataOut,
                                message.sessionNo, message.id,
                                message.type, message.action, false, true);
            message.thread.dataOut.writeInt(0);
            message.thread.flush();
            message.thread.finishMessage();
        } catch (IOException e) {
            error("ManageableServer: addListener: ", e);
        }
    }


/**
 * <A NAME="SD_ATTACHMANAGER"></A>
 * <EM>attachManager</EM>
 *
 * @param message
 */

    protected final void
    attachManager(Message message) {
        DataInputStream  in          = message.thread.dataIn;
        String           managerName = null;
        int              retval      = 0;

        if (ManageableServer_Debug) {
            debug("ManageableServer: attachManager:" +
                  " message: " + message);
        }

        try {
            managerName = in.readUTF();
        } catch (IOException e) {
            error("ManageableServer: attachManager: ", e);
        }

        if (getManager() != null) {
            retval = JSDTException.MANAGER_EXISTS;
        } else {
            setManager(new JSDTManagerImpl(managerName));
            if (ManageableServer_Debug) {
                debug("ManageableServer: attachManager:" +
                      " created manager with name: " + managerName);
            }
            setManagerThread(message.thread);
        }

        try {
            message.thread.writeMessageHeader(message.thread.dataOut,
                                message.sessionNo, message.id,
                                message.type, message.action, false, true);
            message.thread.dataOut.writeInt(retval);
            message.thread.flush();
            message.thread.finishMessage();
        } catch (IOException e) {
            error("ManageableServer: attachManager: ", e);
        }
    }


/**
 * <A NAME="SD_AUTHENTICATECLIENT"></A>
 * <EM>authenticateClient</EM>
 *
 * @param message
 * @param objectName
 */

    protected final void
    authenticateClient(Message message, String objectName) {
        DataInputStream  in         = message.thread.dataIn;
        String           clientName = null;
        int              action     = 0;

        if (ManageableServer_Debug) {
            debug("ManageableServer: authenticateClient:" +
                  " message: "     + message +
                  " object name: " + objectName);
        }

        try {
            clientName = in.readUTF();
            action     = in.readInt();
        } catch (IOException e) {
            error("ManageableServer: authenticateClient: ", e);
        }

        if (getManager() != null && (eventMask & action) != 0) {
            JSDTThread thread         = getManagerThread();
            Message    joiningMessage = new Message();

            joiningMessage.setMessageHeader(message);
            joiningMessages.put(clientName, joiningMessage);

            try {
                thread.writeMessageHeader(thread.dataOut, message.sessionNo,
                          message.id, T_Manager, T_Authenticate, false, true);
                thread.dataOut.writeChar(message.type);
                thread.dataOut.writeUTF(objectName);
                thread.dataOut.writeUTF(clientName);
                thread.dataOut.writeInt(action);
                thread.flush();
                thread.finishMessage();
            } catch (IOException e) {
                error("ManageableServer: authenticateClient: ", e);
            }
        } else {
            try {
                message.thread.writeMessageHeader(message.thread.dataOut,
                                message.sessionNo, message.id,
                                message.type, T_Authenticate, false, true);
                message.thread.dataOut.writeInt(0);
                message.thread.dataOut.writeBoolean(true);
                message.thread.flush();
                message.thread.finishMessage();
            } catch (IOException e) {
                error("ManageableServer: authenticateClient: ", e);
            }
        }
    }


/**
 * <A NAME="SD_CHANGEMANAGERMASK"></A>
 * <EM>changeManagerMask</EM>
 *
 * @param message
 */

    protected final void
    changeManagerMask(Message message) {
        DataInputStream in     = message.thread.dataIn;
        int             retval = 0;

        if (ManageableServer_Debug) {
            debug("ManageableServer: changeManagerMask:" +
                  " message: " + message);
        }

        try {
            int     eventMask = in.readInt();
            boolean disable   = in.readBoolean();

            if (disable) {
                this.eventMask &= ~eventMask;
            } else {
                this.eventMask |= eventMask;
            }

            message.thread.writeMessageHeader(message.thread.dataOut,
                                   message.sessionNo, message.id,
                                   message.type, message.action, false, true);
            message.thread.dataOut.writeInt(retval);
            message.thread.flush();
            message.thread.finishMessage();
        } catch (IOException e) {
            error("ManageableServer: changeManagerMask: ", e);
        }
    }


/**
 * <A NAME="SD_CLIENTCHALLENGE"></A>
 * <EM>clientChallenge</EM>
 *
 * @param message
 */

    protected final void
    clientChallenge(Message message) {
        DataInputStream in             = message.thread.dataIn;
        char                 objectType     = 0;
        String               objectName     = null;
        String               clientName     = null;
        int                  action         = 0;
        int                  length         = 0;
        byte[]               challenge      = null;
        Message              joiningMessage;

        if (ManageableServer_Debug) {
            debug("ManageableServer: clientChallenge:" +
                  " message: " + message);
        }

        try {
            objectType = in.readChar();
            objectName = in.readUTF();
            clientName = in.readUTF();
            action     = in.readInt();
            length     = in.readInt();
            challenge  = message.thread.getData(length);
        } catch (IOException e) {
            error("ManageableServer: clientChallenge: ", e);
        }

        if ((joiningMessage = ss.getJoiningMessage(clientName, objectType,
                                                   objectName)) != null) {
            JSDTThread thread = joiningMessage.thread;

            try {
                thread.writeMessageHeader(thread.dataOut, message.sessionNo,
                    message.id, ClientImpl.M_Client, T_Challenge, false, true);
                thread.dataOut.writeChar(objectType);
                thread.dataOut.writeUTF(objectName);
                thread.dataOut.writeUTF(clientName);
                thread.dataOut.writeInt(action);
                thread.dataOut.writeInt(length);
                thread.dataOut.write(challenge, 0, challenge.length);
                thread.flush();
                thread.finishMessage();
            } catch (IOException e) {
                error("ManageableServer: clientChallenge: ", e);
            }
        }
    }


/**
 * <A NAME="SD_CLIENTRESPONSE"></A>
 * <EM>clientResponse</EM>
 *
 * @param message
 */

    protected final void
    clientResponse(Message message) {
        DataInputStream  in         = message.thread.dataIn;
        char             objectType = 0;
        String           objectName = null;
        String           clientName = null;
        int              action     = 0;
        int              length     = 0;
        byte[]           response   = null;

        if (ManageableServer_Debug) {
            debug("ManageableServer: clientResponse:" +
                  " message: " + message);
        }

        try {
            objectType = in.readChar();
            objectName = in.readUTF();
            clientName = in.readUTF();
            action     = in.readInt();
            length     = in.readInt();
            response   = message.thread.getData(length);
        } catch (IOException e) {
            error("ManageableServer: clientResponse: ", e);
        }

        try {
            JSDTThread thread = null;

            if (objectType == ByteArrayImpl.M_ByteArray) {
                ByteArrayImpl   b  = ss.getByteArrayByName(objectName);
                ByteArrayServer bs = (ByteArrayServer) b.so.getServer();

                thread = bs.getManagerThread();
            } else if (objectType == ChannelImpl.M_Channel) {
                ChannelImpl   c  = ss.getChannelByName(objectName);
                ChannelServer cs = (ChannelServer) c.so.getServer();

                thread = cs.getManagerThread();
            } else if (objectType == SessionImpl.M_Session) {
                thread = getManagerThread();
            } else if (objectType == TokenImpl.M_Token) {
                TokenImpl   t  = ss.getTokenByName(objectName);
                TokenServer ts = (TokenServer) t.so.getServer();

                thread = ts.getManagerThread();
            }

            thread.writeMessageHeader(thread.dataOut, message.sessionNo,
                 message.id, ClientImpl.M_Client, T_Authenticate, false, true);
            thread.dataOut.writeChar(objectType);
            thread.dataOut.writeUTF(objectName);
            thread.dataOut.writeUTF(clientName);
            thread.dataOut.writeInt(action);
            thread.dataOut.writeInt(0);
            thread.dataOut.writeInt(length);
            thread.dataOut.write(response, 0, response.length);
            thread.flush();
            thread.finishMessage();
        } catch (IOException e) {
            error("ManageableServer: clientResponse: ", e);
        }
    }


/**
 * <A NAME="SD_EXPELALLCLIENTS"></A>
 * <EM>expelAllClients</EM>
 *
 * @param message
 * @param sessionName
 * @param objectType
 * @param server
 */

    protected final void
    expelAllClients(Message message, String sessionName, char objectType,
                    ManageableServer server) {
        if (ManageableServer_Debug) {
            debug("ManageableServer: expelAllClients:" +
                  " message: "      + message +
                  " session name: " + sessionName +
                  " object type: "  + objectType +
                  " server: "       + server);
        }

        for (Enumeration e = clients.keys(); e.hasMoreElements();) {
            String clientName = (String) e.nextElement();

            expelClient(message, sessionName, clientName, objectType, server);
        }
    }


/**
 * <A NAME="SD_EXPELCLIENT"></A>
 * <EM>expelClient</EM>
 *
 * @param message
 * @param sessionName
 * @param clientName
 * @param objectType
 * @param server
 */

    private int
    expelClient(Message message, String sessionName, String clientName,
                char objectType, ManageableServer server) {
        SocketThread thread;
        int          eventType = 0;
        int          retval    = 0;

        if (ManageableServer_Debug) {
            debug("ManageableServer: expelClient:" +
                  " message: "      + message +
                  " session name: " + sessionName +
                  " client name: "  + clientName +
                  " object type: "  + objectType +
                  " server: "       + server);
        }

        if (objectType == ByteArrayImpl.M_ByteArray) {
            eventType = ByteArrayEvent.EXPELLED;
        } else if (objectType == ChannelImpl.M_Channel) {
            eventType = ChannelEvent.EXPELLED;
        } else if (objectType == SessionImpl.M_Session) {
            eventType = SessionEvent.EXPELLED;
        } else if (objectType == TokenImpl.M_Token) {
            eventType = TokenEvent.EXPELLED;
        }

        if ((thread = getThreadForClient(clientName)) != null) {
            try {
                thread.writeMessageHeader(thread.dataOut, ss.getSessionNo(), 0,
                                  ClientImpl.M_Client, T_Expel, false, true);
                thread.dataOut.writeChar(objectType);
                thread.dataOut.writeUTF(name);
                thread.dataOut.writeUTF(clientName);
                thread.flush();
                thread.finishMessage();
            } catch (IOException e) {
                error("ManageableServer: expelClient: ", e);
            }
        } else {
            retval = JSDTException.NO_SUCH_CLIENT;
        }

        if (retval == 0) {
            informListeners(sessionName, clientName, name,
                            eventType, objectType);

            if (objectType == ByteArrayImpl.M_ByteArray) {
                ByteArrayServer bs = (ByteArrayServer) server;

                bs.leave(null, clientName);
            } else if (objectType == ChannelImpl.M_Channel) {
                ChannelServer cs = (ChannelServer) server;

                cs.leave(null, clientName);
            } else if (objectType == SessionImpl.M_Session) {
                SessionServer ss = (SessionServer) server;

                ss.leave(null, clientName);
            } else if (objectType == TokenImpl.M_Token) {
                TokenServer ts = (TokenServer) server;

                ts.leave(null, clientName);
            }
        }

        return(retval);
    }


/**
 * <A NAME="SD_EXPEL"></A>
 * <EM>expel</EM>
 *
 * @param message
 * @param objectType
 * @param server
 */

    protected final void
    expel(Message message, char objectType, ManageableServer server) {
        DataInputStream  in          = message.thread.dataIn;
        int              count       = 0;
        int              retval      = 0;
        String[]         clientNames = null;

        if (ManageableServer_Debug) {
            debug("ManageableServer: expel:" +
                  " message: "     + message +
                  " object type: " + objectType +
                  " server: "      + server);
        }

        try {
            count       = in.readInt();
            clientNames = new String[count];

            for (int i = 0; i < count; i++) {
                clientNames[i] = in.readUTF();
            }
        } catch (IOException e) {
            error("ManageableServer: expel: ", e);
        }

        for (int i = 0; i < count; i++) {
            expelClient(message, session.getName(), clientNames[i],
                        message.type, server);
        }

        try {
            message.thread.writeMessageHeader(message.thread.dataOut,
                                message.sessionNo, message.id,
                                message.type, message.action, false, true);
            message.thread.dataOut.writeInt(retval);
            message.thread.flush();
            message.thread.finishMessage();
        } catch (IOException e) {
            error("ManageableServer: expel: ", e);
        }
    }


/**
 * <A NAME="SD_GETCLIENTBYNAME"></A>
 * <EM>getClientByName</EM>
 *
 * @param clientName
 *
 * @return
 */

    protected final ClientImpl
    getClientByName(String clientName) {
        if (ManageableServer_Debug) {
            debug("ManageableServer: getClientByName:" +
                  " client name: " + clientName);
        }

        return((ClientImpl) clients.get(clientName));
    }


/**
 * <A NAME="SD_GETMANAGER"></A>
 * <EM>getManager</EM>
 *
 * @return
 */

    private final JSDTManagerImpl
    getManager() {
        if (ManageableServer_Debug) {
            debug("ManageableServer: getManager.");
        }

        return(manager);
    }


/**
 * <A NAME="SD_GETMANAGERTHREAD"></A>
 * <EM>getManagerThread</EM>
 *
 * @return
 */

    protected final JSDTThread
    getManagerThread() {
        if (ManageableServer_Debug) {
            debug("ManageableServer: getManagerThread.");
        }

        return(managerThread);
    }


/**
 * <A NAME="SD_GETTHREADFORCLIENT"></A>
 * <EM>getThreadForClient</EM>
 *
 * @param clientName
 *
 * @return
 */

    final SocketThread
    getThreadForClient(String clientName) {
        if (ManageableServer_Debug) {
            debug("ManageableServer: getThreadForClient:" +
                  " client name: " + clientName);
        }

        return((SocketThread) clientConnections.get(clientName));
    }


/**
 * <A NAME="SD_INFORMLISTENERS"></A>
 * <EM>informListeners</EM>
 *
 * @param sessionName
 * @param clientName
 * @param resourceName
 * @param type
 * @param listenerType
 */

    protected final void
    informListeners(String sessionName, String clientName,
                    String resourceName, int type, char listenerType) {
        if (ManageableServer_Debug) {
            debug("ManageableServer: informListeners:" +
                  " session name: "  + sessionName +
                  " client name: "   + clientName +
                  " resource name: " + resourceName +
                  " type: "          + type +
                  " listener type: " + listenerType);
        }

        for (Enumeration k = listenerThreads.keys(); k.hasMoreElements();) {
            SocketThread thread = (SocketThread) k.nextElement();

            try {
                thread.writeMessageHeader(thread.dataOut, ss.getSessionNo(), 0,
                                  listenerType, T_InformListener, false, true);
                thread.dataOut.writeUTF(resourceName);
                thread.dataOut.writeUTF(clientName);
                thread.dataOut.writeInt(type);
                thread.flush();
                thread.finishMessage();
            } catch (IOException ioe) {
            }
        }
    }


/**
 * <A NAME="SD_INVITE"></A>
 * <EM>invite</EM>
 *
 * @param message
 */

    protected final void
    invite(Message message, char objectType) {
        DataInputStream  in          = message.thread.dataIn;
        int              count;
        int              retval      = 0;
        int              event       = 0;
        String[]         clientNames;
        SocketThread     thread;

        if (ManageableServer_Debug) {
            debug("ManageableServer: invite:" +
                  " message: " + message);
        }

        if (objectType == ByteArrayImpl.M_ByteArray) {
            event = ByteArrayEvent.INVITED;
        } else if (objectType == ChannelImpl.M_Channel) {
            event = ChannelEvent.INVITED;
        } else if (objectType == SessionImpl.M_Session) {
            event = SessionEvent.INVITED;
        } else if (objectType == TokenImpl.M_Token) {
            event = TokenEvent.INVITED;
        }

        try {
            count       = in.readInt();
            clientNames = new String[count];

            for (int i = 0; i < count; i++) {
                clientNames[i] = in.readUTF();
                thread         = ss.getThreadForClient(clientNames[i]);

                if (thread != null) {
                    thread.writeMessageHeader(thread.dataOut,
                                message.sessionNo, message.id,
                                ClientImpl.M_Client, T_Invite, false, true);
                    thread.dataOut.writeChar(objectType);
                    thread.dataOut.writeUTF(name);
                    thread.dataOut.writeUTF(clientNames[i]);
                    thread.flush();
                    thread.finishMessage();
                } else {
                    retval = JSDTException.NO_SUCH_CLIENT;
                }
            }

            message.thread.writeMessageHeader(message.thread.dataOut,
                                message.sessionNo, message.id,
                                message.type, message.action, false, true);
            message.thread.dataOut.writeInt(retval);
            message.thread.flush();
            message.thread.finishMessage();

            if (retval == 0) {
                for (int i = 0; i < count; i++) {
                    informListeners(session.getName(), clientNames[i],
                                    name, event, objectType);
                }
            }
        } catch (IOException e) {
            error("ManageableServer: invite: ", e);
        }
    }


/**
 * <A NAME="SD_ISMANAGED"></A>
 * <EM>isManaged</EM> "server-side" method that returns an indication of
 * whether this managable object actually has a manager associated with it.
 *
 * @param message
 */

    protected final void
    isManaged(Message message) {
        int retval = 0;

        if (ManageableServer_Debug) {
            debug("ManageableServer: isManaged:" +
                  " message: " + message);
        }

        try {
            message.thread.writeMessageHeader(message.thread.dataOut,
                                  message.sessionNo, message.id,
                                  message.type, message.action, false, true);
            message.thread.dataOut.writeInt(retval);
            message.thread.dataOut.writeBoolean(getManager() != null);
            message.thread.flush();
            message.thread.finishMessage();
        } catch (IOException e) {
            error("ManageableServer: isManaged: ", e);
        }
    }


/**
 * <A NAME="SD_LEAVE"></A>
 * <EM>leave</EM> the client of this name has left the managed object.
 * Remove the client connection, and inform all observers of this change.
 * If we have an incoming message, than send a reply message back to the
 * proxy side.
 *
 * @param message the message being processed or null. If this parameter is
 * null, this means that we had a client leave a Session (either deliberately
 * or the result of a lost connection), and we are tidying up it's session,
 *  channel and token connections.
 * @param sessionName the current session name.
 * @param objectType the type of object this client is leaving.
 * @param clientName the name of the client leaving this managed object.
 */

    protected void
    leave(Message message, String sessionName,
          char objectType, String clientName) {
        int    retval = 0;
        int    event  = 0;

        if (ManageableServer_Debug) {
            debug("ManageableServer: leave:" +
                  " message: "      + message +
                  " session name: " + sessionName +
                  " object type: "  + objectType +
                  " client name: "  + clientName);
        }

        if (clients.remove(clientName) != null) {
                   if (objectType == SessionImpl.M_Session) {
                event = SessionEvent.LEFT;
            } else if (objectType == ByteArrayImpl.M_ByteArray) {
                event = ByteArrayEvent.LEFT;
            } else if (objectType == ChannelImpl.M_Channel) {
                event = ChannelEvent.LEFT;
            } else if (objectType == TokenImpl.M_Token) {
                event = TokenEvent.LEFT;
            }
        } else {
            retval = JSDTException.NO_SUCH_CLIENT;
        }

        try {
            if (message != null) {
                message.thread.writeMessageHeader(message.thread.dataOut,
                                message.sessionNo, message.id,
                                message.type, message.action, false, true);
                message.thread.dataOut.writeInt(retval);
                message.thread.flush();
                message.thread.finishMessage();
            }
        } catch (IOException e) {
            error("ManageableServer: leave: ", e);
        }

        if (retval == 0) {
            informListeners(sessionName, clientName, name, event, objectType);
            removeClientThreadConnection(clientName);
        }
    }


/**
 * <A NAME="SD_LISTCLIENTNAMES"></A>
 * <EM>listClientNames</EM> "server-side" method that provides a list
 *  of all the clients who are currently joined to this object.
 *
 * @param message
 */

    protected final void
    listClientNames(Message message) {
        int retval = 0;
        int size   = 0;

        if (ManageableServer_Debug) {
            debug("ManageableServer: listClientNames:" +
                  " message: " + message);
        }

        if (clients != null) {
            size = clients.size();
        }

        try {
            message.thread.writeMessageHeader(message.thread.dataOut,
                                message.sessionNo, message.id,
                                message.type, message.action, false, true);
            message.thread.dataOut.writeInt(retval);
            message.thread.dataOut.writeInt(size);
            if (clients != null) {
                for (Enumeration e = clients.keys(); e.hasMoreElements();) {
                    message.thread.dataOut.writeUTF((String) e.nextElement());
                }
            }
            message.thread.flush();
            message.thread.finishMessage();
        } catch (IOException e) {
            error("ManageableServer: listClientNames: ", e);
        }
    }


/**
 * <A NAME="SD_REMOVECLIENTTHREADCONNECTION"></A>
 * <EM>removeClientThreadConnection</EM>
 *
 * @param clientName
 *
 * @return
 */

    private boolean
    removeClientThreadConnection(String clientName) {
        if (ManageableServer_Debug) {
            debug("ManageableServer: removeClientThreadConnection:" +
                  " client name: " + clientName);
        }

        return(clientConnections.remove(clientName) != null);
    }


    protected final void
    removeListener(Message message) {
        DataInputStream in           = message.thread.dataIn;
        String          listenerName = null;
        int             retval       = 0;
        int             noListeners;

        if (ManageableServer_Debug) {
            debug("ManageableServer: removeListener:" +
                  " message: " + message);
        }

        try {
            listenerName = in.readUTF();
        } catch (IOException e) {
            error("ManageableServer: removeClientThreadConnection: ", e);
        }

        if (listeners.remove(listenerName) == null) {
            retval = JSDTException.NO_SUCH_LISTENER;
        }

        if (retval == 0) {
            ServerListenerThread listenerThread =
                                     listenerThreads.get(message.thread);
            Hashtable threadListeners = listenerThread.getListeners();

            threadListeners.remove(listenerName);
            noListeners = threadListeners.size();

            if (noListeners == 0) {
                listenerThreads.remove(message.thread);
            }
        }

        try {
            message.thread.writeMessageHeader(message.thread.dataOut,
                                message.sessionNo, message.id,
                                message.type, message.action, false, true);
            message.thread.dataOut.writeInt(retval);
            message.thread.flush();
            message.thread.finishMessage();
        } catch (IOException e) {
            error("ManageableServer: removeClientThreadConnection: ", e);
        }
    }


/**
 * <A NAME="SD_REMOVELISTENERTHREAD"></A>
 * <EM>removeListenerThread</EM>
 *
 * @param thread
 */

    final void
    removeListenerThread(JSDTThread thread) {
        if (ManageableServer_Debug) {
            debug("ManageableServer: removeListenerThread:" +
                  " thread: " + thread);
        }

        listenerThreads.remove(thread);
    }


/**
 * <A NAME="SD_SETMANAGER"></A>
 * <EM>setManager</EM>
 *
 * @param manager
 *
 * @return
 */

    private void
    setManager(JSDTManagerImpl manager) {
        if (ManageableServer_Debug) {
            debug("ManageableServer: setManager:" +
                  " manager: " + manager);
        }

        this.manager = manager;
    }


/**
 * <A NAME="SD_SETMANAGERTHREAD"></A>
 * <EM>setManagerThread</EM>
 *
 * @param thread
 *
 * @return
 */

    private void
    setManagerThread(JSDTThread thread) {
        if (ManageableServer_Debug) {
            debug("ManageableServer: setManagerThread:" +
                  " thread: " + thread);
        }

        managerThread = thread;
    }
}
