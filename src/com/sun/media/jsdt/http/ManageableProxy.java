
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

package com.sun.media.jsdt.http;

import com.sun.media.jsdt.*;
import com.sun.media.jsdt.impl.*;
import java.net.*;
import java.io.*;
import java.util.*;

/**
 * JSDT client-side proxy parent class (HTTP implementation).
 *
 * @version     2.3 - 6th November 2017
 * @author      Rich Burridge
 */

class
ManageableProxy extends JSDTObject
                implements AbstractManageableProxy, httpDebugFlags {

    // The name of this proxy object.
    protected String name = null;

    // Proxy thread for this object.
    HttpThread proxyThread;

    // Proxy thread that is permanently connected to the server.
    HttpThread permThread;

    // Server thread for this object (if in the same VM).
    protected SameVMSessionServerThread serverThread = null;

    // Server thread for the manager for this object (if in the same VM).
    protected SameVMSessionServerThread managerServerThread = null;

    // The client-side session associated with this object.
    protected SessionImpl session;

    // The client-side proxy associated with the Session for this object.
    protected SessionProxy sp;

    // Handle to ManageableProxy for this manageable object.
    private ManageableProxy sdp;

    // The clients currently joined to this object.
    protected Hashtable<String, Client> clients = null;

    // The clients (potentially) doing a priviledged action.
    Hashtable<String, Client> priviledgedClients = null;

    // The listeners (and event masks), observing changes for this object.
    protected Hashtable<EventListener, Integer> listeners = null;

    // The client-side manager (if any).
    protected JSDTManager manager = null;

    // Manager thread for this object (if any).
    protected HttpThread managerThread;

    // Manager thread that is permanently connected to the server.
    private HttpThread permManagerThread;


/**
 * <A NAME="SD_INITPROXY"></A>
 * <EM>initProxy</EM>
 *
 * @param name
 * @param session
 * @param object
 */

    public void
    initProxy(String name, SessionImpl session, Object object) {
        if (ManageableProxy_Debug) {
            debug("ManageableProxy: initProxy:" +
                  " name: "    + name +
                  " session: " + session +
                  " object: "  + object);
        }

        if (session.po != null) {
            sp = (SessionProxy) session.po.getProxy();
        }
        sdp = this;

        clients            = new Hashtable<>();
        listeners          = new Hashtable<>();
        priviledgedClients = new Hashtable<>();
    }


/**
 * <A NAME="SD_GETPROXY"></A>
 * <EM>getProxy</EM>
 *
 * @return
 */

    public Object
    getProxy() {
        if (ManageableProxy_Debug) {
            debug("ManageableProxy: getProxy.");
        }

        return(sdp);
    }


    public final void
    changeListenerMask(EventListener listener, int eventMask, boolean disable)
                throws NoSuchSessionException, NoSuchChannelException,
                       NoSuchByteArrayException, NoSuchTokenException,
                       NoSuchListenerException {
        if (ManageableProxy_Debug) {
            debug("ManageableProxy: changeListenerMask:" +
                  " listener: "   + listener +
                  " event mask: " + eventMask +
                  " disable?: "   + disable);
        }

        synchronized (listeners) {
            Integer mask = listeners.get(listener);

            if (mask != null) {
                int currentMask = mask;

                if (disable) {
                    currentMask &= ~eventMask;
                } else {
                    currentMask |= eventMask;
                }

                listeners.put(listener, currentMask);
            } else {
                throw new NoSuchListenerException();
            }
        }
    }


/**
 * <A NAME="SD_CHANGEMANAGERMASK"></A>
 * <EM>changeManagerMask</EM> enables or disables certain events for
 * this JSDT Manager.
 *
 * @param manager the manager whose event mask is being changed.
 * @param eventMask the mask of events to be enabled or disabled.
 * @param disable if set true, then disable these events for this manager,
 * else enable them.
 * @param objectType the type of the manageable object. This will be one of:
 * SessionImpl.M_Session, ChannelImpl.M_Channel, ByteArrayImpl.M_ByteArray or
 * TokenImpl.M_Token.
 *
 * @exception ConnectionException if a connection error occured.
 * @exception NoSuchSessionException if this Session doesn't exist.
 * @exception NoSuchByteArrayException if this ByteArray doesn't exist.
 * @exception NoSuchChannelException if this Channel doesn't exist.
 * @exception NoSuchTokenException if this Token doesn't exist.
 * @exception NoSuchManagerException if this Manager doesn't exist.
 * @exception TimedOutException if no reply was received for this operation
 * in the given timeout period.
 */

    public final void
    changeManagerMask(JSDTManager manager, int eventMask,
                    boolean disable, char objectType)
                throws ConnectionException, NoSuchSessionException,
                     NoSuchChannelException, NoSuchByteArrayException,
                     NoSuchTokenException, NoSuchManagerException,
                     TimedOutException {
        DataInputStream in;
        Message         message;
        int             retval;
        HttpThread      thread     = sp.proxyThread;
        int             id         = thread.getId();
        short           sessionNo  = sp.getSessionNo();

        if (ManageableProxy_Debug) {
            debug("ManageableProxy: changeManagerMask:" +
                  " manager: "     + manager +
                  " event mask: "  + eventMask +
                  " disable?: "    + disable +
                  " object type: " + objectType);
        }

        if (this.manager != manager) {
            throw new NoSuchManagerException();
        }

        try {
            thread.writeMessageHeader(thread.dataOut, sessionNo, id,
                                  objectType, T_ChangeManagerMask, true, true);
            thread.dataOut.writeUTF(name);
            thread.dataOut.writeInt(eventMask);
            thread.dataOut.writeBoolean(disable);
            thread.flush();
            message = thread.waitForReply();

            in     = message.thread.dataIn;
            retval = in.readInt();
            message.thread.finishReply();

            if (retval != 0) {
                switch (retval) {
                    case JSDTException.NO_SUCH_BYTEARRAY:
                        throw new NoSuchByteArrayException();
                    case JSDTException.NO_SUCH_CHANNEL:
                        throw new NoSuchChannelException();
                    case JSDTException.NO_SUCH_SESSION:
                        session = null;
                        throw new NoSuchSessionException();
                    case JSDTException.NO_SUCH_TOKEN:
                        throw new NoSuchTokenException();
                    default:
                        error("ManageableProxy: changeManagerMask: ",
                              "impl.unknown.exception.type", retval);
                }
            }
        } catch (IOException e) {
            thread.finishReply();
            throw new ConnectionException();
        }
    }


    public final void
    expel(Client[] clients, char objectType)
                throws ConnectionException, InvalidClientException,
                       NoSuchSessionException, NoSuchChannelException,
                       NoSuchByteArrayException, NoSuchClientException,
                       NoSuchTokenException, PermissionDeniedException,
                       TimedOutException {
        if (ManageableProxy_Debug) {
            debug("ManageableProxy: expel:" +
                  " objectType: " + objectType);
            for (int i = 0; i < clients.length ; i++) {
                System.err.println("clients[" + i + "]: " + clients[i]);
            }
        }

        if (session == null) {
            throw new NoSuchSessionException();
        }

        expelInvite(T_Expel, clients, objectType);
    }


    public final void
    invite(Client[] clients, char objectType)
                throws ConnectionException, InvalidClientException,
                       NoSuchSessionException, NoSuchChannelException,
                       NoSuchByteArrayException, NoSuchClientException,
                       NoSuchTokenException, PermissionDeniedException,
                       TimedOutException {
        if (ManageableProxy_Debug) {
            debug("ManageableProxy: invite:" +
                  " objectType: " + objectType);
            for (int i = 0; i < clients.length ; i++) {
                System.err.println("clients[" + i + "]: " + clients[i]);
            }

        }

        if (session == null) {
            throw new NoSuchSessionException();
        }

        if (objectType == SessionImpl.M_Session) {
            sp.invite(clients);
        } else {
            expelInvite(T_Invite, clients, objectType);
        }
    }


    public final void
    destroy(Client client, char objectType)
                throws ConnectionException, InvalidClientException,
                       NoSuchSessionException, NoSuchChannelException,
                       NoSuchByteArrayException, NoSuchClientException,
                       NoSuchTokenException, PermissionDeniedException,
                       TimedOutException {
        DataInputStream in;
        Message         message;
        int             retval;
        HttpThread      thread     = sp.proxyThread;
        int             id         = thread.getId();
        short           sessionNo  = sp.getSessionNo();
        char            type       = SessionImpl.M_Session;
        char            action     = 0;
        String          clientName = Util.getClientName(client);

        if (ManageableProxy_Debug) {
            debug("ManageableProxy: destroy:" +
                  " client: "     + client +
                  " objectType: " + objectType);
        }

        if (session == null) {
            throw new NoSuchSessionException();
        }

               if (objectType == ByteArrayImpl.M_ByteArray) {
            action = T_DestroyByteArray;
        } else if (objectType == ChannelImpl.M_Channel) {
            action = T_DestroyChannel;
        } else if (objectType == SessionImpl.M_Session) {
            URLString   urlString   = sp.getURL();
            NamingProxy namingProxy = ((httpSession) session).namingProxy;

            try {
                namingProxy.unbind(urlString, session, client);
            } catch (NoRegistryException | InvalidURLException |
                     NotBoundException e) {
            }

            action = T_DestroySession;
        } else if (objectType == TokenImpl.M_Token) {
            action = T_DestroyToken;
        }

        try {
            thread.writeMessageHeader(thread.dataOut, sessionNo, id,
                                      type, action, true, true);
            thread.dataOut.writeUTF(name);
            thread.dataOut.writeUTF(clientName);
            thread.flush();
            message = thread.waitForReply();

            in     = message.thread.dataIn;
            retval = in.readInt();
            message.thread.finishReply();

            if (retval != 0) {
                switch (retval) {
                    case JSDTException.NO_SUCH_BYTEARRAY:
                        throw new NoSuchByteArrayException();
                    case JSDTException.NO_SUCH_CHANNEL:
                        throw new NoSuchChannelException();
                    case JSDTException.NO_SUCH_CLIENT:
                        throw new NoSuchClientException();
                    case JSDTException.NO_SUCH_SESSION:
                        session = null;
                        throw new NoSuchSessionException();
                    case JSDTException.NO_SUCH_TOKEN:
                        throw new NoSuchTokenException();
                    case JSDTException.PERMISSION_DENIED:
                        throw new PermissionDeniedException();
                    default:
                        error("ManageableProxy: destroy: ",
                              "impl.unknown.exception.type", retval);
                }
            }
        } catch (IOException e) {
            thread.finishReply();
            throw new ConnectionException();
        }

               if (objectType == ByteArrayImpl.M_ByteArray) {
            sp.removeByteArray(name);
        } else if (objectType == ChannelImpl.M_Channel) {
            sp.removeChannel(name);
        } else if (objectType == SessionImpl.M_Session) {
            sp.removeSession(name);
        } else if (objectType == TokenImpl.M_Token) {
            sp.removeToken(name);
        }
    }


/**
 * <A NAME="SD_GETSESSION"></A>
 * get the name of the Session that this manageable object belongs to. If
 * this method is applied to a Session, a reference to itself is returned.
 *
 * @return the name of the Session that this manageable object belongs to.
 */

    public Session
    getSession() {
        if (ManageableProxy_Debug) {
            debug("ManageableProxy: getSession.");
        }

        return(session);
    }


/**
 * <A NAME="SD_ISMANAGED"></A>
 * test whether this managed object actually has a manager associated with it.
 *
 * @param objectType
 * @param objectName
 *
 * @exception ConnectionException if a connection error occured.
 * @exception NoSuchSessionException if this Session doesn't exist.
 * @exception NoSuchChannelException if this Channel doesn't exist.
 * @exception NoSuchByteArrayException if this ByteArray doesn't exist.
 * @exception NoSuchTokenException if this Token doesn't exist.
 * @exception TimedOutException if no reply was received for this operation
 * in the given timeout period.
 */

    public boolean
    isManaged(char objectType, String objectName)
                throws ConnectionException, NoSuchSessionException,
                       NoSuchChannelException, NoSuchByteArrayException,
                       NoSuchTokenException, TimedOutException {
        DataInputStream    in;
        Message            message;
        int                retval;
        int                id         = sp.proxyThread.getId();
        short              sessionNo  = sp.getSessionNo();
        boolean            isManaged  = false;

        if (ManageableProxy_Debug) {
            debug("ManageableProxy: isManaged:" +
                  " object type: " + objectType +
                  " object name: " + objectName);
        }

        if (session == null) {
            throw new NoSuchSessionException();
        }

        try {
            sp.proxyThread.writeMessageHeader(sp.proxyThread.dataOut,
                                              sessionNo, id, objectType,
                                              T_IsManaged, true, true);
            sp.proxyThread.dataOut.writeUTF(objectName);
            sp.proxyThread.flush();
            message = sp.proxyThread.waitForReply();

            in     = message.thread.dataIn;
            retval = in.readInt();
            if (retval == 0) {
                isManaged = in.readBoolean();
            }
            message.thread.finishReply();

            if (retval != 0) {
                switch (retval) {
                    case JSDTException.NO_SUCH_BYTEARRAY:
                        throw new NoSuchByteArrayException();
                    case JSDTException.NO_SUCH_CHANNEL:
                        throw new NoSuchChannelException();
                    case JSDTException.NO_SUCH_SESSION:
                        session = null;
                        throw new NoSuchSessionException();
                    case JSDTException.NO_SUCH_TOKEN:
                        throw new NoSuchTokenException();
                    default:
                        error("ManageableProxy: isManaged: ",
                              "impl.unknown.exception.type", retval);
                }
            }
        } catch (IOException e) {
            sp.proxyThread.finishReply();
            throw new ConnectionException();
        }

        return(isManaged);
    }


/**
 * <A NAME="SD_JOIN"></A>
 * <EM>join</EM>
 *
 * @param client
 * @param authenticate
 * @param objectType
 *
 * @exception ConnectionException if a connection error occured.
 * @exception InvalidClientException if the Client is invalid is some way (ie.
 * its getName() method returns null).
 * @exception NoSuchByteArrayException if
 * @exception NoSuchChannelException if
 * @exception NoSuchClientException if
 * @exception NoSuchSessionException if
 * @exception NoSuchTokenException if
 * @exception PermissionDeniedException if
 * @exception NameInUseException if
 * @exception TimedOutException if no reply was received for this operation
 * in the given timeout period.
 */

    public void
    join(Client client, boolean authenticate, char objectType)
                throws ConnectionException, InvalidClientException,
                       NoSuchByteArrayException, NoSuchChannelException,
                       NoSuchClientException, NoSuchSessionException,
                       NoSuchTokenException, PermissionDeniedException,
                       NameInUseException, TimedOutException {
        DataInputStream    in;
        Message            message;
        int                retval;
        AuthenticationInfo info;
        int                id         = sp.proxyThread.getId();
        short              sessionNo  = sp.getSessionNo();
        String             clientName = Util.getClientName(client);

        if (ManageableProxy_Debug) {
            debug("ManageableProxy: join:" +
                  " client: "       + client +
                  " authenticate? " + authenticate +
                  " object type: "  + objectType);
        }

        if (session == null) {
            throw new NoSuchSessionException();
        }

        sp.putPriviledgedClient(client, AuthenticationInfo.JOIN, objectType);

        if (authenticate) {
            char authType = 0;

            switch (objectType) {
                case ByteArrayImpl.M_ByteArray:
                    authType = AuthenticationInfo.BYTEARRAY;
                    break;
                case ChannelImpl.M_Channel:
                    authType = AuthenticationInfo.CHANNEL;
                    break;
                case SessionImpl.M_Session:
                    authType = AuthenticationInfo.SESSION;
                    break;
                case TokenImpl.M_Token:
                    authType = AuthenticationInfo.TOKEN;
                    break;
            }

            info = new AuthenticationInfo(session, AuthenticationInfo.JOIN,
                                          name, authType);

            if (manager != null) {   /* Client and manager have same proxy? */
                try {
                    switch (objectType) {
                        case ByteArrayImpl.M_ByteArray:
                            if (!((ByteArrayManager) manager).byteArrayRequest(
                                            sp.getByteArrayByName(name),
                                            info, client)) {
                                throw new PermissionDeniedException();
                            }
                            break;
                        case ChannelImpl.M_Channel:
                            if (!((ChannelManager) manager).channelRequest(
                                            sp.getChannelByName(name),
                                            info, client)) {
                                throw new PermissionDeniedException();
                            }
                            break;
                        case SessionImpl.M_Session:
                            if (!((SessionManager) manager).sessionRequest(
                                            session, info, client)) {
                                throw new PermissionDeniedException();
                            }
                            break;
                        case TokenImpl.M_Token:
                            if (!((TokenManager) manager).tokenRequest(
                                            sp.getTokenByName(name),
                                            info, client)) {
                                throw new PermissionDeniedException();
                            }
                            break;
                    }
                } catch (Throwable th) {
                    error("ManageableProxy: join: ",
                          "impl.thrown", th + " by manager.");
                }
            } else if (!authenticateClient(id, info, clientName)) {
                throw new PermissionDeniedException();
            }
        }

        try {
            sp.proxyThread.writeMessageHeader(sp.proxyThread.dataOut,
                              sessionNo, id, objectType, T_Join, true, true);
            sp.proxyThread.dataOut.writeUTF(name);
            sp.proxyThread.dataOut.writeUTF(clientName);
            sp.proxyThread.flush();
            message = sp.proxyThread.waitForReply();

            in     = message.thread.dataIn;
            retval = in.readInt();
            message.thread.finishReply();

            if (retval != 0) {
                switch (retval) {
                    case JSDTException.NO_SUCH_BYTEARRAY:
                        throw new NoSuchByteArrayException();
                    case JSDTException.NO_SUCH_CHANNEL:
                        throw new NoSuchChannelException();
                    case JSDTException.NO_SUCH_CLIENT:
                        throw new NoSuchClientException();
                    case JSDTException.NO_SUCH_SESSION:
                        session = null;
                        throw new NoSuchSessionException();
                    case JSDTException.NO_SUCH_TOKEN:
                        throw new NoSuchClientException();
                    case JSDTException.PERMISSION_DENIED:
                        throw new PermissionDeniedException();
                     case JSDTException.NAME_IN_USE:
                         throw new NameInUseException();
                    default:
                        error("ManageableProxy: join: ",
                              "impl.unknown.exception.type", retval);
                }
            }
        } catch (IOException e) {
            sp.proxyThread.finishReply();
            throw new ConnectionException();
        }

        clients.put(clientName, client);
    }


/**
 * <A NAME="SD_LEAVE"></A>
 * <EM>leave</EM>
 *
 * @param client
 * @param objectType
 *
 * @exception ConnectionException if a connection error occured.
 * @exception InvalidClientException if the Client is invalid is some way (ie.
 * its getName() method returns null).
 * @exception NoSuchByteArrayException if this byte array doesn't exist.
 * @exception NoSuchChannelException if this channel doesn't exist.
 * @exception NoSuchClientException if this client doesn't exist.
 * @exception NoSuchSessionException if this session doesn't exist.
 * @exception NoSuchTokenException if this token doesn't exist.
 * @exception TimedOutException if no reply was received for this operation
 * in the given timeout period.
 */

    public void
    leave(Client client, char objectType)
                throws ConnectionException, InvalidClientException,
                       NoSuchByteArrayException, NoSuchChannelException,
                       NoSuchClientException, NoSuchSessionException,
                       NoSuchTokenException, TimedOutException {
        DataInputStream in;
        Message         message;
        int             retval;
        HttpThread      thread     = sp.proxyThread;
        int             id         = thread.getId();
        short           sessionNo  = sp.getSessionNo();
        String          clientName = Util.getClientName(client);

        if (ManageableProxy_Debug) {
            debug("ManageableProxy: leave:" +
                  " client: "     + client +
                  " objectType: " + objectType);
        }

        if (session == null) {
            throw new NoSuchSessionException();
        }

        try {
            thread.writeMessageHeader(thread.dataOut, sessionNo, id,
                                      objectType, T_Leave, true, true);
            thread.dataOut.writeUTF(name);
            thread.dataOut.writeUTF(clientName);
            thread.flush();
            message = thread.waitForReply();

            in     = message.thread.dataIn;
            retval = in.readInt();
            message.thread.finishReply();

            if (retval != 0) {
                switch (retval) {
                    case JSDTException.NO_SUCH_BYTEARRAY:
                        throw new NoSuchByteArrayException();
                    case JSDTException.NO_SUCH_CHANNEL:
                        throw new NoSuchChannelException();
                    case JSDTException.NO_SUCH_CLIENT:
                        throw new NoSuchClientException();
                    case JSDTException.NO_SUCH_SESSION:
                        session = null;
                        throw new NoSuchSessionException();
                    case JSDTException.NO_SUCH_TOKEN:
                        throw new NoSuchTokenException();
                    default:
                        error("ManageableProxy: leave: ",
                              "impl.unknown.exception.type", retval);
                }
            }
        } catch (IOException e) {
            thread.finishReply();
            throw new ConnectionException();
        }

        synchronized (clients) {
            clients.remove(clientName);
        }

/* If the Client is leaving a Session, then we also need to remove it from
 * any ByteArrays, Channels or Tokens that it might have joined.
 */

        if (objectType == SessionImpl.M_Session) {
            sp.leave(client);
        }
    }


/**
 * <A NAME="SD_LISTCLIENTNAMES"></A>
 * <EM>listClientNames</EM>
 *
 * @param objectType
 *
 * @return an array of names of clients currently joined to this object.
 *
 * @exception ConnectionException if a connection error occured.
 * @exception NoSuchByteArrayException if this byte array doesn't exist.
 * @exception NoSuchChannelException if this channel doesn't exist.
 * @exception NoSuchSessionException if this session doesn't exist.
 * @exception NoSuchTokenException if this token doesn't exist.
 * @exception TimedOutException if no reply was received for this operation
 * in the given timeout period.
 */

    public final String[]
    listClientNames(char objectType)
                throws ConnectionException, NoSuchSessionException,
                       NoSuchChannelException, NoSuchByteArrayException,
                       NoSuchTokenException, TimedOutException {
        DataInputStream in;
        Message         message;
        int             retval;
        int             n;
        HttpThread      thread      = sp.proxyThread;
        int             id          = thread.getId();
        short           sessionNo   = sp.getSessionNo();
        String[]        clientNames = null;

        if (ManageableProxy_Debug) {
            debug("ManageableProxy: listClientNames.");
        }

        if (session == null) {
            throw new NoSuchSessionException();
        }

        try {
            thread.writeMessageHeader(thread.dataOut, sessionNo, id,
                                   objectType, T_ListClientNames, true, true);
            thread.dataOut.writeUTF(name);
            thread.flush();
            message = thread.waitForReply();

            in     = message.thread.dataIn;
            retval = in.readInt();
            if (retval == 0) {
                n           = in.readInt();
                clientNames = new String[n];
                for (int i = 0; i < n; i++) {
                    clientNames[i] = in.readUTF();
                }
                Util.sort(clientNames);
            }

            message.thread.finishReply();

            if (retval != 0) {
                switch (retval) {
                    case JSDTException.NO_SUCH_BYTEARRAY:
                        throw new NoSuchByteArrayException();
                    case JSDTException.NO_SUCH_CHANNEL:
                        throw new NoSuchChannelException();
                    case JSDTException.NO_SUCH_SESSION:
                        session = null;
                        throw new NoSuchSessionException();
                    case JSDTException.NO_SUCH_TOKEN:
                        throw new NoSuchTokenException();
                    default:
                        error("ManageableProxy: listClientNames: ",
                              "impl.unknown.exception.type", retval);
                }
            }
        } catch (IOException e) {
            thread.finishReply();
            throw new ConnectionException();
        }
        return(clientNames);
    }


/**
 * <A NAME="SD_ADDLISTENER"></A>
 * <EM>addListener</EM>
 *
 * @param listener
 *
 * @exception ConnectionException if a connection error occured.
 * @exception NoSuchByteArrayException if
 * @exception NoSuchChannelException if
 * @exception NoSuchSessionException if
 * @exception NoSuchTokenException if
 * @exception TimedOutException if no reply was received for this operation
 * in the given timeout period.
 */

    public final void
    addListener(EventListener listener, char listenerType)
                throws ConnectionException, NoSuchByteArrayException,
                       NoSuchChannelException, NoSuchSessionException,
                       NoSuchTokenException, TimedOutException {
        DataInputStream in;
        Message         message;
        int             retval;
        int             id           = sp.proxyThread.getId();
        short           sessionNo    = sp.getSessionNo();
        String          listenerName = listener.toString();

        if (ManageableProxy_Debug) {
            debug("ManageableProxy: addListener:" +
                  " listener: "      + listener +
                  " listener type: " + listenerType);
        }

        if (session == null) {
            throw new NoSuchSessionException();
        }

        try {
            sp.proxyThread.writeMessageHeader(sp.proxyThread.dataOut,
                                              sessionNo, id, listenerType,
                                              T_AddListener, true, true);
            sp.proxyThread.dataOut.writeUTF(name);
            sp.proxyThread.dataOut.writeUTF(listenerName);
            sp.proxyThread.flush();
            message = sp.proxyThread.waitForReply();

            in     = message.thread.dataIn;
            retval = in.readInt();
            message.thread.finishReply();

            if (retval != 0) {
                switch (retval) {
                    case JSDTException.NO_SUCH_BYTEARRAY:
                        throw new NoSuchByteArrayException();
                    case JSDTException.NO_SUCH_CHANNEL:
                        throw new NoSuchChannelException();
                    case JSDTException.NO_SUCH_SESSION:
                        session = null;
                        throw new NoSuchSessionException();
                    case JSDTException.NO_SUCH_TOKEN:
                        throw new NoSuchTokenException();
                    default:
                        error("ManageableProxy: addListener: ",
                              "impl.unknown.exception.type", retval);
                }
            }
        } catch (IOException e) {
            sp.proxyThread.finishReply();
            throw new ConnectionException();
        }

/* By default, a listener will listen for every type of event, so set all
 * bits in the event mask.
 */

        synchronized (listeners) {
            listeners.put(listener, 0XFFFFFFFF);
        }
    }


/**
 * <A NAME="SD_ATTACHMANAGER"></A>
 * <EM>attachManager</EM>
 *
 * @param manager
 * @param managerType
 * @param manageable
 *
 * @exception ConnectionException if a connection error occured.
 * @exception ManagerExistsException if there is already a manager associated
 * with this manageable object.
 * @exception NoSuchHostException if the host serving this object does not
 * exist.
 * @exception NoSuchSessionException if the session given does not exist.
 * @exception TimedOutException if no reply was received for this operation
 * in the given timeout period.
 */

    public void
    attachManager(JSDTManager manager, char managerType,
                  Manageable manageable)
                throws ConnectionException, ManagerExistsException,
                       NoSuchHostException, NoSuchSessionException,
                       TimedOutException {
        // The thread handle to the permanent managerthread connection.
        Thread mpThread;

        DataInputStream in;
        Message         message;
        int             retval;
        HttpThread      managerThread = null;
        HttpThread      thread        = sp.proxyThread;
        short           sessionNo     = sp.getSessionNo();

        if (ManageableProxy_Debug) {
            debug("ManageableProxy: attachManager:" +
                  " manager: "    + manager +
                  " type: "       + managerType +
                  " manageable: " + manageable);
        }

        if (session == null) {
            throw new NoSuchSessionException();
        }

        try {
            Integer         portNo = thread.getPort();
            TCPSocketServer server;

            if (TCPSocketServer.socketServers != null &&
                (server = TCPSocketServer.socketServers.get(portNo)) != null) {
                managerServerThread = server.createSameVMSessionServerThread(
                                                thread.getAddress(),
                                                thread.getPort());

                managerThread = new SameVMManagerProxyThread(session,
                                        manageable, manager,
                                        thread.getAddress(), thread.getPort());

                ((SameVMThread) managerThread).setReplyThread(sessionNo,
                        managerServerThread.getId(), managerServerThread);

                managerServerThread.setReplyThread(sessionNo,
                        managerThread.getId(), (SameVMThread) managerThread);
            } else {
                managerThread = new ManagerProxyThread(session, manageable,
                                        manager, thread.getAddress(),
                                                 thread.getPort());
                if (!managerThread.mustPing()) {
                    permManagerThread = new ManagerPermThread(session,
                                    manageable, manager, thread.getAddress(),
                                    thread.getPort(), managerThread.getId());
                }
            }
        } catch (SocketException se) {
            error("ManageableProxy: attachManager: ", se);
        } catch (UnknownHostException uhe) {
            throw new NoSuchHostException();
        }

        setManagerThread(managerThread);

        if (!managerThread.mustPing()) {
            mpThread = Util.startThread(permManagerThread,
                           "ManagerPermThread:" + manageable.getName(), true);
        }

        Util.startThread(managerThread,
                         "ManagerProxyThread:" + manageable.getName(), true);

        try {
            managerThread.writeMessageHeader(managerThread.dataOut,
                                     sessionNo, managerThread.getId(),
                                     managerType, T__Manager, true, true);
            managerThread.dataOut.writeUTF(name);
            managerThread.dataOut.writeUTF(manager.toString());
            managerThread.flush();
            message = managerThread.waitForReply();

            in     = message.thread.dataIn;
            retval = in.readInt();
            message.thread.finishReply();

            if (retval != 0) {
                switch (retval) {
                    case JSDTException.MANAGER_EXISTS:
                        throw new ManagerExistsException();
                    case JSDTException.NO_SUCH_SESSION:
                        session = null;
                        throw new NoSuchSessionException();
                    default:
                        error("ManageableProxy: attachManager: ",
                              "impl.unknown.exception.type", retval);
                }
            }
        } catch (IOException e) {
            managerThread.finishReply();
            throw new ConnectionException();
        }
        setManager(manager);
    }


/**
 * <A NAME="SD_AUTHENTICATECLIENT"></A>
 * <EM>authenticateClient</EM>
 *
 * @param id the id to use for client/server messages.
 * @param info
 * @param clientName
 *
 * @return
 *
 * @exception ConnectionException if a connection error occured.
 * @exception NoSuchSessionException if the session given does not exist.
 * @exception TimedOutException if no reply was received for this operation
 * in the given timeout period.
 */

    final boolean
    authenticateClient(int id, AuthenticationInfo info, String clientName)
                throws ConnectionException, NoSuchSessionException,
                       TimedOutException {
        DataInputStream in;
        Message         message;
        int             retval;
        short           sessionNo     = sp.getSessionNo();
        boolean         isUnmanaged   = true;
        boolean         authenticated = false;

        if (ManageableProxy_Debug) {
            debug("ManageableProxy: authenticateClient:" +
                  " id: "          + id +
                  " info: "        + info +
                  " client name: " + clientName);
        }

        if (session == null) {
            throw new NoSuchSessionException();
        }

        try {
            char authType   = info.getType();
            char objectType = 0;

            switch (authType) {
                case AuthenticationInfo.BYTEARRAY:
                    objectType = ByteArrayImpl.M_ByteArray;
                    break;
                case AuthenticationInfo.CHANNEL:
                    objectType = ChannelImpl.M_Channel;
                    break;
                case AuthenticationInfo.SESSION:
                    objectType = SessionImpl.M_Session;
                    break;
                case AuthenticationInfo.TOKEN:
                    objectType = TokenImpl.M_Token;
                    break;
            }

            sp.proxyThread.waitForPermanentConnection();

            sp.proxyThread.writeMessageHeader(sp.proxyThread.dataOut,
                      sessionNo, id, objectType, T_Authenticate, true, true);
            sp.proxyThread.dataOut.writeUTF(info.getName());
            sp.proxyThread.dataOut.writeUTF(clientName);
            sp.proxyThread.dataOut.writeInt(info.getAction());
            sp.proxyThread.flush();
            sp.proxyThread.setAuthenticateWaitStatus(true);
            message = sp.proxyThread.waitForReply();

            in = message.thread.dataIn;
            if ((retval = in.readInt()) == 0) {
                if ((isUnmanaged = in.readBoolean())) {
                    authenticated = in.readBoolean();
                }
            }
            sp.proxyThread.setAuthenticateWaitStatus(false);
            message.thread.finishReply();

            if (retval != 0) {
                switch (retval) {
                    case JSDTException.NO_SUCH_SESSION:
                        session = null;
                        throw new NoSuchSessionException();
                    default:
                        error("ManageableProxy: authenticateClient: ",
                              "impl.unknown.exception.type", retval);
                }
            }

            if (!isUnmanaged) {
                String key    = info.getName() + ":" + sessionNo + ":" + id;
                Thread thread = Thread.currentThread();

                authenticated = sp.proxyThread.suspendClientThread(key, thread);
            }
        } catch (IOException e) {
            sp.proxyThread.finishReply();
            throw new ConnectionException();
        }
        return(authenticated);
    }


    final void
    expelInvite(char messageType, Client[] clients, char objectType)
                throws ConnectionException, InvalidClientException,
                       NoSuchSessionException, NoSuchChannelException,
                       NoSuchByteArrayException, NoSuchClientException,
                       NoSuchTokenException, PermissionDeniedException,
                       TimedOutException {
        DataInputStream in;
        Message         message;
        int             retval;
        int             id        = sp.proxyThread.getId();
        short           sessionNo = sp.getSessionNo();

        if (ManageableProxy_Debug) {
            debug("ManageableProxy: expelInvite:" +
                  " message type: " + messageType +
                  " objectType: "   + objectType);
            for (int i = 0; i < clients.length ; i++) {
                System.err.println("clients[" + i + "]: " + clients[i]);
            }

        }

        if (getManager() == null) {
            throw new PermissionDeniedException();
        }

        try {
            sp.proxyThread.writeMessageHeader(sp.proxyThread.dataOut,
                                              sessionNo, id, objectType,
                                              messageType, true, true);
            sp.proxyThread.dataOut.writeUTF(name);
            sp.proxyThread.dataOut.writeInt(clients.length);

            for (int i = 0; i < clients.length; i++) {
                sp.proxyThread.dataOut.writeUTF(Util.getClientName(clients[i]));
            }

            sp.proxyThread.flush();
            message = sp.proxyThread.waitForReply();

            in     = message.thread.dataIn;
            retval = in.readInt();
            message.thread.finishReply();

            if (retval != 0) {
                switch (retval) {
                    case JSDTException.NO_SUCH_BYTEARRAY:
                        throw new NoSuchByteArrayException();
                    case JSDTException.NO_SUCH_CHANNEL:
                        throw new NoSuchChannelException();
                    case JSDTException.NO_SUCH_CLIENT:
                        throw new NoSuchClientException();
                    case JSDTException.NO_SUCH_SESSION:
                        session = null;
                        throw new NoSuchSessionException();
                    case JSDTException.NO_SUCH_TOKEN:
                        throw new NoSuchTokenException();
                    case JSDTException.PERMISSION_DENIED:
                        throw new PermissionDeniedException();
                    default:
                        error("ManageableProxy: expelInvite: ",
                              "impl.unknown.exception.type", retval);
                }
            }
        } catch (IOException e) {
            sp.proxyThread.finishReply();
            throw new ConnectionException();
        }
    }


/**
 * <A NAME="SD_GETMANAGER"></A>
 * <EM>getManager</EM>
 *
 * @return
 */

    private synchronized JSDTManager
    getManager() {
        if (ManageableProxy_Debug) {
            debug("ManageableProxy: getManager.");
        }

        return(manager);
    }


/**
 * <A NAME="SD_GETMANAGERTHREAD"></A>
 * <EM>getManagerThread</EM>
 *
 * @return
 */

    final synchronized HttpThread
    getManagerThread() {
        if (ManageableProxy_Debug) {
            debug("ManageableProxy: getManagerThread.");
        }

        return(managerThread);
    }


    public final void
    removeListener(EventListener listener, char listenerType)
                throws ConnectionException, NoSuchByteArrayException,
                       NoSuchChannelException, NoSuchListenerException,
                       NoSuchSessionException, NoSuchTokenException,
                       TimedOutException {
        DataInputStream in;
        Message         message;
        int             retval;
        int             id        = sp.proxyThread.getId();
        short           sessionNo = sp.getSessionNo();

        if (ManageableProxy_Debug) {
            debug("ManageableProxy: removeListener:" +
                  " listener: "      + listener +
                  " listener type: " + listenerType);
        }

        if (session == null) {
            throw new NoSuchSessionException();
        }

        try {
            sp.proxyThread.writeMessageHeader(sp.proxyThread.dataOut,
                                              sessionNo, id, listenerType,
                                              T_RemoveListener, true, true);
            sp.proxyThread.dataOut.writeUTF(name);
            sp.proxyThread.dataOut.writeUTF(listener.toString());
            sp.proxyThread.flush();
            message = sp.proxyThread.waitForReply();

            in     = message.thread.dataIn;
            retval = in.readInt();
            message.thread.finishReply();

            if (retval != 0) {
                switch (retval) {
                    case JSDTException.NO_SUCH_BYTEARRAY:
                        throw new NoSuchByteArrayException();
                    case JSDTException.NO_SUCH_CHANNEL:
                        throw new NoSuchChannelException();
                    case JSDTException.NO_SUCH_LISTENER:
                        throw new NoSuchListenerException();
                    case JSDTException.NO_SUCH_SESSION:
                        session = null;
                        throw new NoSuchSessionException();
                    case JSDTException.NO_SUCH_TOKEN:
                        throw new NoSuchTokenException();
                    default:
                        error("ManageableProxy: removeListener: ",
                              "impl.unknown.exception.type", retval);
                }
            }
        } catch (IOException e) {
            sp.proxyThread.finishReply();
            throw new ConnectionException();
        }

        synchronized (listeners) {
            listeners.remove(listener);
        }
    }


/**
 * <A NAME="SD_SETMANAGER"></A>
 * <EM>setManager</EM>
 *
 * @param manager
 *
 * @return
 */

    private synchronized void
    setManager(JSDTManager manager) {
        if (ManageableProxy_Debug) {
            debug("ManageableProxy: setManager: " +
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

    private synchronized void
    setManagerThread(HttpThread thread) {
        if (ManageableProxy_Debug) {
            debug("ManageableProxy: setManagerThread:" +
                  " thread: " + thread);
        }

        managerThread = thread;
    }
}
