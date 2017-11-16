
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
import java.net.*;
import java.io.*;
import java.util.*;

/**
 * JSDT Session Client-side class (socket implementation).
 *
 * @version     2.3 - 6th November 2017
 * @author      Rich Burridge
 */

final class
SessionProxy extends ManageableProxy implements AbstractSessionProxy {

    // The unique session number for this session.
    private short sessionNo;

    // The JSDT URL string associated with this session.
    private String url;

    // The shared bytes arrays currently created in this session.
    private Hashtable<String, ByteArray> byteArrays = null;

    // The channels currently in use by the clients in this session.
    private Hashtable<String, Channel> channels = null;

    // The tokens currently in use by the clients in this session.
    private Hashtable<String, Token> tokens = null;


/**
 * <A NAME="SD_SESSIONPROXY"></A>
 * <EM>SessionProxy</EM>
 *
 * @param session
 * @param url the JSDT URL string associated with this session.
 * @param name
 * @param sessionNo
 * @param host
 * @param port
 */

    public
    SessionProxy(SessionImpl session, String url, String name,
                 short sessionNo, String host, int port)
        throws NoSuchHostException {
        if (SessionProxy_Debug) {
            debug("SessionProxy: constructor:" +
                  " session: "   + session +
                  " url: "       + url +
                  " name: "      + name +
                  " session #: " + sessionNo +
                  " host: "      + host +
                  " port: "      + port);
        }

        this.session       = session;
        this.name          = name;
        this.sessionNo     = sessionNo;
        this.url           = url;

        byteArrays         = new Hashtable<>();
        channels           = new Hashtable<>();
        tokens             = new Hashtable<>();

        try {
            Integer         portNo = port;
            TCPSocketServer server;

            if (TCPSocketServer.socketServers != null &&
                (server = TCPSocketServer.socketServers.get(portNo)) != null) {
                serverThread =
                        server.createSameVMSessionServerThread(host, port);

                proxyThread = new SameVMSessionProxyThread(session, this,
                                                           host, port);

                ((SameVMThread) proxyThread).setReplyThread(serverThread);

                serverThread.setReplyThread((SameVMThread) proxyThread);
            } else {
                proxyThread = new SessionProxyThread(session, this,
                                                     host, port);
            }
        } catch (SocketException e) {
            error("SessionProxy: constructor: ", e);
        } catch (UnknownHostException uhe) {
            throw new NoSuchHostException();
        }

        Util.startThread(proxyThread, "SessionProxyThread:" + name, true);
    }


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
        if (SessionProxy_Debug) {
            debug("SessionProxy: initProxy:" +
                  " name: "    + name +
                  " session: " + session);
        }

        super.initProxy(name, session, object);
    }


/**
 * <A NAME="SD_GETPROXY"></A>
 * <EM>getProxy</EM>
 */

    public Object
    getProxy() {
        if (SessionProxy_Debug) {
            debug("SessionProxy: getProxy.");
        }

        return(this);
    }


/**
 * <A NAME="SD_GETSESSIONNO"></A>
 * <EM>getSessionNo</EM>
 */

    short
    getSessionNo() {
        if (SessionProxy_Debug) {
            debug("SessionProxy: getSessionNo.");
        }

        return(sessionNo);
    }


/**
 * <A NAME="SD_GETURL"></A>
 * gives the URLString used to create this Session.
 *
 * @return the URLString used to create this Session.
 *
 * @since       JSDT 2.1
 */

    public final URLString
    getURL() {
        if (SessionProxy_Debug) {
            debug("SessionProxy: getUrl.");
        }

        return(new URLString(url));
    }


/**
 * <A NAME="SD_ATTACHSESSIONMANAGER"></A>
 * <EM>attachSessionManager</EM>
 *
 * @param sessionManager
 * @param session
 *
 * @exception ConnectionException if a connection error occured.
 * @exception ManagerExistsException if
 * @exception NoSuchHostException if
 * @exception NoSuchSessionException if the session given does not exist.
 * @exception TimedOutException if no reply was received for this operation
 * in the given timeout period.
*/

    public void
    attachSessionManager(SessionManager sessionManager, Session session)
        throws ConnectionException, ManagerExistsException,
               NoSuchHostException, NoSuchSessionException,
               TimedOutException {
        if (SessionProxy_Debug) {
            debug("SessionProxy: attachSessionManager:" +
                  " session manager: " + sessionManager +
                  " session: "         + session);
        }

        attachManager(sessionManager, SessionImpl.M_Session, session);
    }


    public ByteArray
    createByteArray(Client client, String byteArrayName,
                    byte[] value, int offset, int length, boolean autoJoin)
        throws ConnectionException, InvalidClientException,
               NameInUseException, NoSuchSessionException,
               NoSuchClientException, NoSuchHostException,
               PermissionDeniedException, TimedOutException {
        DataInputStream    in;
        int                id        = proxyThread.getId();
        char               type      = SessionImpl.M_Session;
        ByteArrayImpl      byteArray;
        Message            message;
        int                retval;
        int                newLength;
        byte[]             newValue  = null;
        AuthenticationInfo info;

        if (SessionProxy_Debug) {
            debug("SessionProxy: createByteArray:" +
                  " client: "          + client +
                  " byte array name: " + byteArrayName +
                  " value: "           + value +
                  " offset: "          + offset +
                  " length: "          + length +
                  " auto join? "       + autoJoin);
        }

        if (session == null) {
            throw new NoSuchSessionException();
        }

        putPriviledgedClient(client, AuthenticationInfo.CREATE_BYTEARRAY, type);
        info = new AuthenticationInfo(session,
                                      AuthenticationInfo.CREATE_BYTEARRAY,
                                      byteArrayName,
                                      AuthenticationInfo.SESSION);
        if (manager != null) {  /* Client and manager have same proxy? */
            try {
                if (!((SessionManager) manager).sessionRequest(session, info,
                                                               client)) {
                    throw new PermissionDeniedException();
                }
            } catch (Throwable th) {
                error("SessionProxy: createByteArray: ",
                      "impl.thrown", th + " by manager.");
            }
        } else if (!authenticateClient(id, info, Util.getClientName(client))) {
            throw new PermissionDeniedException();
        }

        try {
            proxyThread.writeMessageHeader(proxyThread.dataOut,
                           sessionNo, id, type, T_CreateByteArray, true, true);
            proxyThread.dataOut.writeUTF(byteArrayName);
            proxyThread.dataOut.writeUTF(Util.getClientName(client));
            proxyThread.dataOut.writeInt(length);
            proxyThread.dataOut.write(value, offset, length);
            proxyThread.flush();
            message = proxyThread.waitForReply();

            in        = message.thread.dataIn;
            retval    = in.readInt();
            if (retval == 0) {
                newLength = in.readInt();
                newValue  = message.thread.getData(newLength);
            }
            message.thread.finishReply();

            if (retval != 0) {
                switch (retval) {
                    case JSDTException.NO_SUCH_SESSION:
                        session = null;
                        throw new NoSuchSessionException();
                    case JSDTException.NO_SUCH_CLIENT:
                        throw new NoSuchClientException();
                    case JSDTException.PERMISSION_DENIED:
                        throw new PermissionDeniedException();
                    default:
                        error("SessionProxy: createByteArray: ",
                              "impl.unknown.exception.type", retval);
                }
            }
        } catch (IOException e) {
            proxyThread.finishReply();
            throw new ConnectionException();
        }

        if ((byteArray = getByteArrayByName(byteArrayName)) == null) {
            byteArray = new ByteArrayImpl(false, byteArrayName, session,
                                          newValue);
            addByteArray(byteArray);
            try {
                byteArray.addByteArrayListener(byteArray);
            } catch (NoSuchByteArrayException nsbe) {
                error("SessionProxy: createByteArray: ", nsbe);
            }
        }

        if (autoJoin) {
            try {
                byteArray.join(client);
            } catch (NoSuchByteArrayException nsbae) {
                error("SessionProxy: createByteArray: ", nsbae);
            } catch (NoSuchChannelException | NoSuchTokenException e) {
                // Can't happen.
            }
        }

        return(byteArray);
    }


/**
 * <A NAME="SD_CREATECHANNEL"></A>
 * <EM>createChannel</EM> send a message to the session server to create a
 * new channel for this session. A value in returned from the server. If
 * this is non-zero, then it indicates the exception type, and a Shared
 * Data exception is thrown.
 *
 * If a Channel with the given name already exists (ie. a channel is
 * already created with this name), a new proxy-side Channel is created
 * and associated with this server-side Channel.
 *
 * If this is a managed session, then the client be authenticated to check if
 * it is allowed to perform this operation.
 *
 * @param client a client that will be used for authentication purposes if
 * this is a managed session.
 * @param channelName the name to give this channel.
 * @param reliable whether the channel is reliable. In other words whether
 * data delivery is guarenteed.
 * @param ordered whether data delivered over this channel is ordered.
 * @param autoJoin if true, then the given client is automatically joined to
 * this channel.
 *
 * @exception ConnectionException if a connection error occured.
 * @exception InvalidClientException if the Client is invalid is some way (ie.
 * its getName() method returns null).
 * @exception NameInUseException if a Client with this name is already
 * joined to this Channel.
 * @exception NoSuchSessionException if the session given does not exist.
 * @exception NoSuchClientException if the client given doesn't exist.
 * @exception NoSuchHostException if
 * @exception PermissionDeniedException if the client doesn't have permission
 * to create and/or join this channel.
 * @exception TimedOutException if no reply was received for this operation
 * in the given timeout period.
 *
 * @return a pointer to the Channel.
 */

    public Channel
    createChannel(Client client, String channelName,
                  boolean reliable, boolean ordered, boolean autoJoin)
        throws ConnectionException, InvalidClientException,
               NameInUseException, NoSuchSessionException,
               NoSuchClientException, NoSuchHostException,
               PermissionDeniedException, TimedOutException {
        DataInputStream    in;
        Message            message;
        int                retval;
        AuthenticationInfo info;
        ChannelImpl        channel;
        int                id   = proxyThread.getId();
        char               type = SessionImpl.M_Session;

        if (SessionProxy_Debug) {
            debug("SessionProxy: createChannel:" +
                  " client: "       + client +
                  " channel name: " + channelName +
                  " reliable: "     + reliable +
                  " ordered: "      + ordered +
                  " auto join? "    + autoJoin);
        }

        if (session == null) {
            throw new NoSuchSessionException();
        }

        putPriviledgedClient(client, AuthenticationInfo.CREATE_CHANNEL, type);
        info = new AuthenticationInfo(session,
                                      AuthenticationInfo.CREATE_CHANNEL,
                                      channelName,
                                      AuthenticationInfo.SESSION);
        if (manager != null) {  /* Client and manager have same proxy? */
            try {
                if (!((SessionManager) manager).sessionRequest(session, info,
                                                               client)) {
                    throw new PermissionDeniedException();
                }
            } catch (Throwable th) {
                error("SessionProxy: createChannel: ",
                      "impl.thrown", th + " by manager.");
            }
        } else if (!authenticateClient(id, info, Util.getClientName(client))) {
            throw new PermissionDeniedException();
        }

        try {
            proxyThread.writeMessageHeader(proxyThread.dataOut, sessionNo, id,
                                           type, T_CreateChannel, true, true);
            proxyThread.dataOut.writeUTF(channelName);
            proxyThread.dataOut.writeUTF(Util.getClientName(client));
            proxyThread.dataOut.writeBoolean(reliable);
            proxyThread.dataOut.writeBoolean(ordered);

            proxyThread.flush();
            message = proxyThread.waitForReply();

            in       = message.thread.dataIn;
            retval   = in.readInt();
            if (retval == 0) {
                ordered  = in.readBoolean();
                reliable = in.readBoolean();
            }

            message.thread.finishReply();

            if (retval != 0) {
                switch (retval) {
                    case JSDTException.NO_SUCH_SESSION:
                        session = null;
                        throw new NoSuchSessionException();
                    case JSDTException.NO_SUCH_CLIENT:
                        throw new NoSuchClientException();
                    default:
                        error("SessionProxy: createChannel: ",
                              "impl.unknown.exception.type", retval);
                }
            }
        } catch (IOException e) {
            proxyThread.finishReply();
            throw new ConnectionException();
        }

        if ((channel = getChannelByName(channelName)) == null) {
            channel = new ChannelImpl(false, channelName, session,
                                      reliable, ordered);
            addChannel(channel);
        }

        if (autoJoin) {
            try {
                channel.join(client);
            } catch (NoSuchChannelException nsce) {
                error("SessionProxy: createChannel: ", nsce);
            }
        }

        return(channel);
    }


    public Token
    createToken(Client client, String tokenName, boolean autoJoin)
        throws ConnectionException, InvalidClientException,
               NameInUseException, NoSuchSessionException,
               NoSuchClientException, PermissionDeniedException,
               TimedOutException {
        DataInputStream    in;
        Message            message;
        int                retval;
        AuthenticationInfo info;
        Token              token;
        int                id   = proxyThread.getId();
        char               type = SessionImpl.M_Session;

        if (SessionProxy_Debug) {
            debug("SessionProxy: createToken:" +
                  " client: "     + client +
                  " token name: " + tokenName +
                  " autoJoin? "   + autoJoin);
        }

        if (session == null) {
            throw new NoSuchSessionException();
        }

        putPriviledgedClient(client, AuthenticationInfo.CREATE_TOKEN, type);
        info = new AuthenticationInfo(session,
                                      AuthenticationInfo.CREATE_TOKEN,
                                      tokenName,
                                      AuthenticationInfo.SESSION);

        if (manager != null) {  /* Client and manager have same proxy? */
            try {
                if (!((SessionManager) manager).sessionRequest(session, info,
                                                               client)) {
                    throw new PermissionDeniedException();
                }
            } catch (Throwable th) {
                error("SessionProxy: createToken: ",
                      "impl.thrown", th + " by manager.");
            }
        } else if (!authenticateClient(id, info, Util.getClientName(client))) {
            throw new PermissionDeniedException();
        }

        try {
            proxyThread.writeMessageHeader(proxyThread.dataOut, sessionNo, id,
                                           type, T_CreateToken, true, true);
            proxyThread.dataOut.writeUTF(tokenName);
            proxyThread.dataOut.writeUTF(Util.getClientName(client));
            proxyThread.flush();
            message = proxyThread.waitForReply();

            in     = message.thread.dataIn;
            retval = in.readInt();
            message.thread.finishReply();

            if (retval != 0) {
                switch (retval) {
                    case JSDTException.NO_SUCH_SESSION:
                        session = null;
                        throw new NoSuchSessionException();
                    case JSDTException.NO_SUCH_CLIENT:
                        throw new NoSuchClientException();
                    default:
                        error("SessionProxy: createToken: ",
                              "impl.unknown.exception.type", retval);
                }
            }
        } catch (IOException e) {
            proxyThread.finishReply();
            throw new ConnectionException();
        }

        if ((token = getTokenByName(tokenName)) == null) {
            token = new TokenImpl(false, tokenName, session);
            addToken(token);
        }

        if (autoJoin) {
            try {
                token.join(client);
            } catch (NoSuchByteArrayException | NoSuchChannelException e) {
                // Can't happen.
            } catch (NoSuchTokenException nste) {
                error("SessionProxy: createToken: ", nste);
            }
        }

        return(token);
    }


    public boolean
    byteArrayExists(String byteArrayName)
        throws ConnectionException, NoSuchSessionException, TimedOutException {
        if (SessionProxy_Debug) {
            debug("SessionProxy: byteArrayExists:" +
                  " name: " + byteArrayName);
        }

        return(objectExists(T_ByteArrayExists, byteArrayName));
    }


    public boolean
    channelExists(String channelName)
        throws ConnectionException, NoSuchSessionException, TimedOutException {
        if (SessionProxy_Debug) {
            debug("SessionProxy: channelExists:" +
                  " name: " + channelName);
        }

        return(objectExists(T_ChannelExists, channelName));
    }


    public boolean
    tokenExists(String tokenName)
        throws ConnectionException, NoSuchSessionException, TimedOutException {
        if (SessionProxy_Debug) {
            debug("SessionProxy: tokenExists:" +
                  " name: " + tokenName);
        }

        return(objectExists(T_TokenExists, tokenName));
    }


    private boolean
    objectExists(char objectType, String objectName)
        throws ConnectionException, NoSuchSessionException, TimedOutException {
        DataInputStream in;
        Message         message;
        int             retval;
        int             id      = proxyThread.getId();
        char            type    = SessionImpl.M_Session;
        boolean         exists  = false;

        if (SessionProxy_Debug) {
            debug("SessionProxy: objectExists:" +
                  " type: " + objectType +
                  " name: " + objectName);
        }

        if (session == null) {
            throw new NoSuchSessionException();
        }

        try {
            proxyThread.writeMessageHeader(proxyThread.dataOut, sessionNo, id,
                                           type, objectType, true, true);
            proxyThread.dataOut.writeUTF(objectName);
            proxyThread.flush();
            message = proxyThread.waitForReply();

            in     = message.thread.dataIn;
            retval = in.readInt();
            if (retval == 0) {
                exists = in.readBoolean();
            }

            message.thread.finishReply();

            if (retval != 0) {
                switch (retval) {
                    case JSDTException.NO_SUCH_SESSION:
                        session = null;
                        throw new NoSuchSessionException();
                    default:
                        error("SessionProxy: objectExists: ",
                              "impl.unknown.exception.type", retval);
                }
            }
        } catch (IOException e) {
            proxyThread.finishReply();
            throw new ConnectionException();
        }
        return(exists);
    }


    public boolean
    byteArrayManaged(String byteArrayName)
        throws ConnectionException, NoSuchByteArrayException,
               NoSuchSessionException, TimedOutException {
        boolean reply = false;

        if (SessionProxy_Debug) {
            debug("SessionProxy: byteArrayManaged:" +
                  " name: " + byteArrayName);
        }

        try {
            reply = isManaged(ByteArrayImpl.M_ByteArray, byteArrayName);
        } catch (NoSuchChannelException | NoSuchTokenException e) {
        }

        return(reply);
    }


    public boolean
    channelManaged(String channelName)
        throws ConnectionException, NoSuchChannelException,
               NoSuchSessionException, TimedOutException {
        boolean reply = false;

        if (SessionProxy_Debug) {
            debug("SessionProxy: channelManaged:" +
                  " name: " + channelName);
        }

        try {
            reply = isManaged(ChannelImpl.M_Channel, channelName);
        } catch (NoSuchByteArrayException | NoSuchTokenException e) {
        }

        return(reply);
    }


    public boolean
    tokenManaged(String tokenName)
        throws ConnectionException, NoSuchTokenException,
               NoSuchSessionException, TimedOutException {
        boolean reply = false;

        if (SessionProxy_Debug) {
            debug("SessionProxy: tokenManaged:" +
                  " name: " + tokenName);
        }

        try {
            reply = isManaged(TokenImpl.M_Token, tokenName);
        } catch (NoSuchByteArrayException | NoSuchChannelException e) {
        }

        return(reply);
    }


    public ByteArray[]
    getByteArraysJoined(Client client)
        throws ConnectionException, InvalidClientException,
               NoSuchSessionException, TimedOutException {
        String[]    names;
        ByteArray[] joined;

        if (SessionProxy_Debug) {
            debug("SessionProxy: getByteArraysJoined:" +
                  " client: " + client);
        }

        names = objectsJoined(T_ByteArraysJoined, client);
        joined = new ByteArray[names.length];
        for (int i = 0; i < names.length; i++) {
            joined[i] = getByteArrayByName(names[i]);
        }
        return(joined);
    }


    public Channel[]
    getChannelsJoined(Client client)
        throws ConnectionException, InvalidClientException,
               NoSuchSessionException, TimedOutException {
        String[]  names;
        Channel[] joined;

        if (SessionProxy_Debug) {
            debug("SessionProxy: getChannelsJoined:" +
                  " client: " + client);
        }

        names = objectsJoined(T_ChannelsJoined, client);
        joined = new Channel[names.length];
        for (int i = 0; i < names.length; i++) {
            joined[i] = getChannelByName(names[i]);
        }
        return(joined);
    }


    public Token[]
    getTokensJoined(Client client)
        throws ConnectionException, InvalidClientException,
               NoSuchSessionException, TimedOutException {
        String[] names;
        Token[]  joined;

        if (SessionProxy_Debug) {
            debug("SessionProxy: getTokensJoined:" +
                  " client: " + client);
        }

        names = objectsJoined(T_TokensJoined, client);
        joined = new Token[names.length];
        for (int i = 0; i < names.length; i++) {
            joined[i] = getTokenByName(names[i]);
        }
        return(joined);
    }


    private String[]
    objectsJoined(char objectType, Client client)
        throws ConnectionException, InvalidClientException,
               NoSuchSessionException, TimedOutException {
        DataInputStream in;
        Message         message;
        int             retval;
        int             n;
        int             id    = proxyThread.getId();
        char            type  = SessionImpl.M_Session;
        String[]        names = null;

        if (SessionProxy_Debug) {
            debug("SessionProxy: objectsJoined:" +
                  " type: "   + objectType +
                  " client: " + client);
        }

        if (session == null) {
            throw new NoSuchSessionException();
        }

        try {
            proxyThread.writeMessageHeader(proxyThread.dataOut, sessionNo, id,
                                           type, objectType, true, true);
            proxyThread.dataOut.writeUTF(Util.getClientName(client));
            proxyThread.flush();
            message = proxyThread.waitForReply();

            in     = message.thread.dataIn;
            retval = in.readInt();
            if (retval == 0) {
                n     = in.readInt();
                names = new String[n];
                for (int i = 0; i < n; i++) {
                    names[i] = in.readUTF();
                }
                Util.sort(names);
            }

            message.thread.finishReply();

            if (retval != 0) {
                switch (retval) {
                    case JSDTException.NO_SUCH_SESSION:
                        session = null;
                        throw new NoSuchSessionException();
                    default:
                        error("SessionProxy: objectsJoined: ",
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
 * <A NAME="SD_LEAVE"></A>
 * <EM>leave</EM> removes this Client from any ByteArrays, Channels or Tokens
 * it might have joined within this Session. This is a proxy-side cleanup
 * when a Client does a <CODE>Session.leave</CODE>
 *
 * @param client the Client to be removed.
 */

    void
    leave(Client client) throws InvalidClientException {
        if (SessionProxy_Debug) {
            debug("SessionProxy: leave:" +
                  " client: " + client);
        }

        if (client != null) {
            String clientName = Util.getClientName(client);

            for (Enumeration e = byteArrays.elements(); e.hasMoreElements();) {
                ByteArrayImpl  byteArray = (ByteArrayImpl) e.nextElement();
                ByteArrayProxy bp = (ByteArrayProxy) byteArray.po.getProxy();

                synchronized (bp.clients) {
                    bp.clients.remove(clientName);
                }
            }

            for (Enumeration e = channels.elements(); e.hasMoreElements();) {
                ChannelImpl  channel = (ChannelImpl) e.nextElement();
                ChannelProxy cp      = (ChannelProxy) channel.po.getProxy();

                synchronized (cp.clients) {
                    cp.clients.remove(clientName);
                }
            }

            for (Enumeration e = tokens.elements(); e.hasMoreElements();) {
                TokenImpl  token = (TokenImpl) e.nextElement();
                TokenProxy tp    = (TokenProxy) token.po.getProxy();

                synchronized (tp.clients) {
                    tp.clients.remove(clientName);
                }
            }
        }
    }


    public String[]
    listByteArrayNames()
        throws ConnectionException, NoSuchSessionException, TimedOutException {
        if (SessionProxy_Debug) {
            debug("SessionProxy: listByteArrayNames.");
        }

        return(listNames(T_ListByteArrayNames));
    }


    public String[]
    listChannelNames()
        throws ConnectionException, NoSuchSessionException, TimedOutException {
        if (SessionProxy_Debug) {
            debug("SessionProxy: listChannelNames.");
        }

        return(listNames(T_ListChannelNames));
    }


    public String[]
    listTokenNames()
        throws ConnectionException, NoSuchSessionException, TimedOutException {
        if (SessionProxy_Debug) {
            debug("SessionProxy: listTokenNames.");
        }

        return(listNames(T_ListTokenNames));
    }


/**
 * <A NAME="SD_LISTNAMES"></A>
 * <EM>listNames</EM> lists the names of all the objects of the given type
 * known to this session.
 *
 * @param nameType the type of names to list (Channel, Token or ByteArray).
 *
 * @exception ConnectionException if a connection error occured.
 * @exception NoSuchSessionException if the given session no longer exists.
 * @exception TimedOutException if no reply was received for this operation
 * in the given timeout period.
 *
 * @return an array of names for this type of session object.
 */

    private String[]
    listNames(char nameType)
        throws ConnectionException, NoSuchSessionException, TimedOutException {
        DataInputStream in;
        int             id      = proxyThread.getId();
        char            type    = SessionImpl.M_Session;
        Message         message;
        String[]        names   = null;
        int             retval;
        int             n;

        if (SessionProxy_Debug) {
            debug("SessionProxy: listNames:" +
                  " type: " + nameType);
        }

        if (session == null) {
            throw new NoSuchSessionException();
        }

        try {
            proxyThread.writeMessageHeader(proxyThread.dataOut, sessionNo, id,
                                           type, nameType, true, true);
            proxyThread.dataOut.writeUTF(name);
            proxyThread.flush();
            message = proxyThread.waitForReply();

            in     = message.thread.dataIn;
            retval = in.readInt();
            if (retval == 0) {
                n     = in.readInt();
                names = new String[n];
                for (int i = 0; i < n; i++) {
                    names[i] = in.readUTF();
                }
                Util.sort(names);
            }

            message.thread.finishReply();

            if (retval != 0) {
                switch (retval) {
                    case JSDTException.NO_SUCH_SESSION:
                        session = null;
                        throw new NoSuchSessionException();
                    default:
                        error("SessionProxy: listNames: ",
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
 * <A NAME="SD_ADDBYTEARRAY"></A>
 * <EM>addByteArray</EM>
 *
 * @param byteArray
 */

    private void
    addByteArray(ByteArray byteArray) {
        if (SessionProxy_Debug) {
            debug("SessionProxy: addByteArray:" +
                  " byte array: " + byteArray);
        }

        synchronized (byteArrays) {
            byteArrays.put(byteArray.getName(), byteArray);
        }
    }


/**
 * <A NAME="SD_ADDCHANNEL"></A>
 * <EM>addChannel</EM>
 *
 * @param channel
 */

    private void
    addChannel(Channel channel) {
        if (SessionProxy_Debug) {
            debug("SessionProxy: addChannel:" +
                  " channel: " + channel);
        }

        synchronized (channels) {
            channels.put(channel.getName(), channel);
        }
    }


/**
 * <A NAME="SD_ADDTOKEN"></A>
 * <EM>addToken</EM>
 *
 * @param token
 */

    private void
    addToken(Token token) {
        if (SessionProxy_Debug) {
            debug("SessionProxy: addToken:" +
                  " token name: " + token.getName());
        }

        synchronized (tokens) {
            tokens.put(token.getName(), token);
        }
    }


/**
 * <A NAME="SD_GETBYTEARRAYBYNAME"></A>
 * <EM>getByteArrayByName</EM>
 *
 * @param byteArrayName
 *
 * @return
 */

    ByteArrayImpl
    getByteArrayByName(String byteArrayName) {
        if (SessionProxy_Debug) {
            debug("SessionProxy: getByteArrayByName:" +
                  " byte array name: " + byteArrayName);
        }

        synchronized (byteArrays) {
            return((ByteArrayImpl) byteArrays.get(byteArrayName));
        }
    }


/**
 * <A NAME="SD_GETCHANNELBYNAME"></A>
 * <EM>getChannelByName</EM>
 *
 * @param channelName
 *
 * @return
 */

    ChannelImpl
    getChannelByName(String channelName) {
        if (SessionProxy_Debug) {
            debug("SessionProxy: getChannelByName:" +
                  " channel name: " + channelName);
        }

        synchronized (channels) {
            return((ChannelImpl) channels.get(channelName));
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

    Client
    getClientByName(String clientName) {
        if (SessionProxy_Debug) {
            debug("SessionProxy: getClientByName:" +
                  " client name: " + clientName);
        }

        synchronized (clients) {
            return(clients.get(clientName));
        }
    }


/**
 * <A NAME="SD_GETPRIVILEDGEDCLIENT"></A>
 * <EM>getPriviledgedClient</EM> get the client that is attempting to do
 * a priviledged operation on a managed object.
 *
 * @param clientName the name of the client attempting to do the
 * priviledged operation.
 * @param action the action being performed.
 * @param objectType the type of managed object.
 *
 * @return the client (potentially) performing this action on this object.
 */

    Client
    getPriviledgedClient(String clientName, int action, char objectType) {
        StringBuffer buffer = new StringBuffer();

        if (SessionProxy_Debug) {
            debug("SessionProxy: getPriviledgedClient:" +
                  " client name: " + clientName +
                  " action: "      + action +
                  " object type: " + objectType);
        }

        buffer.append(clientName);
        buffer.append(action);
        buffer.append(objectType);

        synchronized (priviledgedClients) {
            return(priviledgedClients.get(new String(buffer)));
        }
    }


/**
 * <A NAME="SD_GETTOKENBYNAME"></A>
 * <EM>getTokenByName</EM>
 *
 * @param tokenName
 *
 * @return
 */

    TokenImpl
    getTokenByName(String tokenName) {
        if (SessionProxy_Debug) {
            debug("SessionProxy: getTokenByName:" +
                  " tokenname: " + tokenName);
        }

        synchronized (tokens) {
            return((TokenImpl) tokens.get(tokenName));
        }
    }


/**
 * <A NAME="SD_INVITE"></A>
 * <EM>invite</EM>
 *
 * @param clients
 *
 * @exception ConnectionException if a connection error occured.
 * @exception NoSuchSessionException if the session given does not exist.
 * @exception NoSuchClientException if a client given doesn't exist.
 * @exception PermissionDeniedException if the caller doesn't have
 * permission to invite these client[s].
 * @exception TimedOutException if no reply was received for this operation
 * in the given timeout period.
 */

    public void
    invite(Client[] clients)
        throws ConnectionException, NoSuchSessionException,
               NoSuchClientException, PermissionDeniedException,
               TimedOutException {
        int  id     = proxyThread.getId();
        char type   = SessionImpl.M_Session;

        if (SessionProxy_Debug) {
            debug("SessionProxy: invite:");
            for (int i = 0; i < clients.length ; i++) {
                System.err.println("clients[" + i + "]: " + clients[i]);
            }
        }

        if (manager == null) {
            throw new PermissionDeniedException();
        }

        if (clients != null) {
            for (int i = 0; i < clients.length; i++) {
                if (clients[i] instanceof socketClient) {
                    socketClient client = (socketClient) clients[i];
                    SocketThread thread = client.cc.proxyThread;

                    try {
                        thread.writeMessageHeader(thread.dataOut, sessionNo,
                                          id, type, T_Invite, false, true);
                        thread.dataOut.writeUTF(url);
                        thread.flush();
                        thread.finishMessage();
                    } catch (IOException e) {
                        thread.finishMessage();
                        throw new ConnectionException();
                    }
                } else {
                    error("SessionProxy: invite: ",
                          "impl.cannot.invite.client", clients[i]);
                }
            }
        }
    }


/**
 * <A NAME="SD_PUTPRIVILEDGEDCLIENT"></A>
 * <EM>putPriviledgedClient</EM> put an entry into the hash table of
 * clients that are attempting to do a priviledged operation on a managed
 * object.
 *
 * @param client the client attempting the priviledged operation.
 * @param action the action being performed.
 * @param objectType the type of managed object.
 */

    void
    putPriviledgedClient(Client client, int action, char objectType)
        throws InvalidClientException {
        StringBuffer buffer = new StringBuffer();

        if (SessionProxy_Debug) {
            debug("SessionProxy: putPriviledgedClient:" +
                  " client name: " + Util.getClientName(client) +
                  " action: "      + action +
                  " object type: " + objectType);
        }

        buffer.append(Util.getClientName(client));
        buffer.append(action);
        buffer.append(objectType);

        synchronized (priviledgedClients) {
            priviledgedClients.put(new String(buffer), client);
        }
    }


/**
 * <A NAME="SD_REMOVEBYTEARRAY"></A>
 * <EM>removeByteArray</EM>
 *
 * @param byteArrayName
 */

    void
    removeByteArray(String byteArrayName) {
        if (SessionProxy_Debug) {
            debug("SessionProxy: removeByteArray:" +
                  " byte array name: " + byteArrayName);
        }

        synchronized (byteArrays) {
            byteArrays.remove(byteArrayName);
        }
    }


/**
 * <A NAME="SD_REMOVECHANNEL"></A>
 * <EM>removeChannel</EM>
 *
 * @param channelName
 */

    void
    removeChannel(String channelName) {
        if (SessionProxy_Debug) {
            debug("SessionProxy: removeChannel:" +
                  " channel name: " + channelName);
        }

        synchronized (channels) {
            channels.remove(channelName);
        }
    }


/**
 * <A NAME="SD_REMOVESESSION"></A>
 * <EM>removeSession</EM>
 *
 * @param sessionName
 */

    void
    removeSession(String sessionName) {
        if (SessionProxy_Debug) {
            debug("SessionProxy: removeSession:" +
                  " session name: " + sessionName);
        }

        if (session != null) {
            URLString urlString = new URLString(url);

            ((socketSession) session).namingProxy.cleanupSession(
                        Util.adjustURLString(url, urlString.getHostAddress()));

            session = null;

            if (managerThread != null) {
                managerThread.terminate();
                managerThread = null;
            }

            if (proxyThread != null) {
                proxyThread.terminate();
                proxyThread = null;
            }

            if (serverThread != null) {
                serverThread.terminate();
                serverThread = null;
            }

            if (managerServerThread != null) {
                managerServerThread.terminate();
                managerServerThread = null;
            }
        }
    }


/**
 * <A NAME="SD_REMOVETOKEN"></A>
 * <EM>removeToken</EM>
 *
 * @param tokenName
 */

    void
    removeToken(String tokenName) {
        if (SessionProxy_Debug) {
            debug("SessionProxy: removeToken:" +
                  " token name: " + tokenName);
        }

        synchronized (tokens) {
            tokens.remove(tokenName);
        }
    }


/**
 * <A NAME="SD_CLOSE"></A>
 * closes the session, rendering the session handle invalid. This method
 * should be called when an applet or application terminates to facilitate
 * the cleanup process. If a client wishes to further participate in this
 * session, then it will need to get a new session handle with
 * <CODE>SessionFactory.createSession</CODE>
 *
 * If there are no other Sessions on the same "host:port" as this Session,
 * the underlying connection being used by this applet or application will
 * automatically be closed.
 *
 * @param closeConnection indicates whether the underlying connection used
 * by this applet or application should be forcefully closed. Forcefully
 * closing this connection would automatically render all references to other
 * Sessions on the "host:port" being used by this Session as invalid.
 *
 * @exception ConnectionException if a connection error occured.
 * @exception NoSuchSessionException if this Session doesn't exist.
 */

    public void
    close(boolean closeConnection)
        throws ConnectionException, NoSuchSessionException {
        int         id;
        NamingProxy namingProxy  = null;
        boolean     gotException = false;
        char        type         = SessionImpl.M_Session;

        if (SessionProxy_Debug) {
            debug("SessionProxy: close:" +
                  " close connection: " + closeConnection);
        }

        if (session == null) {
            throw new NoSuchSessionException();
        }

        namingProxy = ((socketSession) session).namingProxy;
        id          = proxyThread.getId();

        try {
            proxyThread.terminate();
            proxyThread.writeMessageHeader(proxyThread.dataOut, sessionNo, id,
                                           type, T_Close, false, true);
            proxyThread.flush();
        } catch (IOException e) {
            gotException = true;
        }

        proxyThread.finishReply();
        namingProxy.cleanupSession(url);
        if (closeConnection ||
            !namingProxy.hasSessions(proxyThread.getAddress(),
                                     proxyThread.getPort())) {
            namingProxy.cleanupConnection();
            proxyThread.cleanupConnection();
        }
        if (gotException) {
            throw new ConnectionException();
        }
    }
}
