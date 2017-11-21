
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
import com.sun.media.jsdt.event.SessionEvent;
import java.io.*;
import java.util.*;

/**
 * JSDT Session Server-side class.
 *
 * @version     2.3 - 20th November 2017
 * @author      Rich Burridge
 */

final class
SessionServer extends ManageableServer implements AbstractSessionServer {

    // The url associated with this session.
    private String url = null;

    // The unique session number for this session.
    private final short sessionNo;

    // The server-side session associated with this session server.
    protected SessionImpl session;

    // The TCP socket server for this port number.
    private TCPSocketServer socketServer = null;

    // The UDP channel servers thread for unreliable channels.
    private UDPChannelServerThread cst = null;

    // The shared bytes arrays currently in use in this session.
    private Hashtable<String, ByteArray> byteArrays = null;

    // The channels currently in use by the clients in this session.
    private Hashtable<String, Channel> channels = null;

    // The tokens currently in use by the clients in this session.
    private Hashtable<String, Token> tokens = null;

    // Object that is cleanup up connections that are no longer pinging.
    private final CleanupConnections cc;

    // The thread cleaning up connections that are no longer pinging.
    private Thread cleanupThread = null;


/**
 * <A NAME="SD_SESSIONSERVER"></A>
 * <EM>SessionServer</EM>
 *
 * @param session
 * @param name
 * @param sessionNo
 * @param port
 *
 * @exception PortInUseException if this port is being used by another
 * application.
 */

    public
    SessionServer(SessionImpl session, String name,
                  short sessionNo, int port) throws PortInUseException {
        if (SessionServer_Debug) {
            debug("SessionServer: constructor:" +
                  " session: "   + session +
                  " name: "      + name +
                  " session #: " + sessionNo +
                  " port: "      + port);
        }

        this.session     = session;
        this.name        = name;
        this.sessionNo   = sessionNo;

        byteArrays       = new Hashtable<>();
        channels         = new Hashtable<>();
        clients          = new Hashtable<>();
        tokens           = new Hashtable<>();

        cc = new CleanupConnections(this);

        if (TCPSocketServer.socketServers == null) {
            TCPSocketServer.socketServers = new Hashtable<>();
        }

        synchronized (TCPSocketServer.socketServers) {
            Integer portNo = port;
            Thread  thread = null;

            if ((socketServer =
                TCPSocketServer.socketServers.get(portNo)) == null) {
                socketServer = new TCPSocketServer(port);
                TCPSocketServer.socketServers.put(portNo, socketServer);

                thread = Util.startThread(socketServer,
                                 "TCPSocketServerThread:" + port, false);
            }

            socketServer.addSession(session, sessionNo);
            socketServer.setThread(thread);
        }
    }


/**
 * <A NAME="SD_INITSERVER"></A>
 * <EM>initServer</EM>
 *
 * @param name
 * @param session
 * @param object
 */

    public void
    initServer(String name, SessionImpl session, Object object) {
        if (SessionServer_Debug) {
            debug("SessionServer: initServer:" +
                  " name: "    + name +
                  " session: " + session);
        }

        super.initServer(name, session, object);
    }


/**
 * <A NAME="SD_GETSERVER"></A>
 * <EM>getServer</EM>
 */

    public Object
    getServer() {
        if (SessionServer_Debug) {
            debug("SessionServer: getServer.");
        }

        return(this);
    }


/**
 * <A NAME="SD_GETSESSIONNO"></A>
 * <EM>getSessionNo</EM>
 */

    short
    getSessionNo() {
        if (SessionServer_Debug) {
            debug("SessionServer: getSessionNo.");
        }

        return(sessionNo);
    }


/**
 * <A NAME="SD_CLEANUPBYTEARRAYLISTENERS"></A>
 * <EM>cleanupByteArrayListeners</EM> cleanup all the listeners for all the
 * byte arrays associated with this thread. When a byte array is created,
 * each proxy connection has a byte array listener automatically associated
 * with it, so that the proxy-side can be informed when the byte arrays
 * value changes. This make getting the byte arrays value a local operation.
 * When a thread goes away, we need to remove those listeners from each of
 * the servers byte arrays.
 *
 * @param thread the thread that has just terminated.
 */

    private void
    cleanupByteArrayListeners(JSDTThread thread) {
        if (SessionServer_Debug) {
            debug("SessionServer: cleanupByteArrayListeners:" +
                  " thread: " + thread);
        }

        for (Enumeration e = byteArrays.elements(); e.hasMoreElements();) {
            ByteArrayImpl   byteArray = (ByteArrayImpl) e.nextElement();
            ByteArrayServer bs = (ByteArrayServer) byteArray.so.getServer();

            bs.removeListenerThread(thread);
        }
    }


/**
 * <A NAME="SD_CLEANUPLISTENERS"></A>
 * <EM>cleanupListeners</EM> cleanup all the listeners for all the objects
 * for the client with this name, on the given thread.
 *
 * @param thread the thread that has just terminated.
 * @param clientName the name of the client to cleanup.
 */

    private void
    cleanupListeners(JSDTThread thread, String clientName) {
        ClientImpl client     = getClientByName(clientName);
        Vector     byteArrays = client.getByteArrays();
        Vector     channels   = client.getChannels();
        Vector     tokens     = client.getTokens();

        if (SessionServer_Debug) {
            debug("SessionServer: cleanupListeners:" +
                  " thread: "      + thread +
                  " client name: " + clientName);
        }

        for (Enumeration e = byteArrays.elements(); e.hasMoreElements();) {
            ByteArrayImpl   byteArray = (ByteArrayImpl) e.nextElement();
            ByteArrayServer bs = (ByteArrayServer) byteArray.so.getServer();

            bs.removeListenerThread(thread);
        }


        for (Enumeration e = channels.elements(); e.hasMoreElements();) {
            ChannelImpl   channel = (ChannelImpl) e.nextElement();
            ChannelServer cs      = (ChannelServer) channel.so.getServer();

            cs.removeListenerThread(thread);
        }

        for (Enumeration e = tokens.elements(); e.hasMoreElements();) {
            TokenImpl   token = (TokenImpl) e.nextElement();
            TokenServer ts    = (TokenServer) token.so.getServer();

            ts.removeListenerThread(thread);
        }

        removeListenerThread(thread);
    }


/**
 * <A NAME="SD_CLEANUPLISTENERS"></A>
 * <EM>cleanupListeners</EM> cleanup all the listeners for all the objects
 * for the given thread.
 *
 * @param thread the thread that has just terminated.
 */

    private void
    cleanupListeners(JSDTThread thread) {
        if (SessionServer_Debug) {
            debug("SessionServer: cleanupListeners:" +
                  " thread: "      + thread);
        }

        for (Enumeration e = byteArrays.elements(); e.hasMoreElements();) {
            ByteArrayImpl   byteArray = (ByteArrayImpl) e.nextElement();
            ByteArrayServer bs = (ByteArrayServer) byteArray.so.getServer();

            bs.removeListenerThread(thread);
        }

        for (Enumeration e = channels.elements(); e.hasMoreElements();) {
            ChannelImpl   channel = (ChannelImpl) e.nextElement();
            ChannelServer cs      = (ChannelServer) channel.so.getServer();

            cs.removeListenerThread(thread);
        }

        for (Enumeration e = tokens.elements(); e.hasMoreElements();) {
            TokenImpl   token = (TokenImpl) e.nextElement();
            TokenServer ts    = (TokenServer) token.so.getServer();

            ts.removeListenerThread(thread);
        }

        removeListenerThread(thread);
    }


/**
 * <A NAME="SD_CLIENTAUTHENTICATE"></A>
 * <EM>clientAuthenticate</EM>
 *
 * @param message
 */

    void
    clientAuthenticate(Message message) {
        Message         joiningMessage;
        JSDTThread      joiningThread;
        DataInputStream in         = message.thread.dataIn;
        char            objectType = 0;
        String          objectName = null;
        String          clientName = null;
        boolean         admitted   = false;
        int             retval     = 0;

        if (SessionServer_Debug) {
            debug("SessionServer: clientAuthenticate:" +
                  " message: " + message);
        }

        try {
            objectType = in.readChar();
            objectName = in.readUTF();
            clientName = in.readUTF();
            admitted   = in.readBoolean();
        } catch (IOException e) {
            error("SessionServer: clientAuthenticate: ", e);
        }

        joiningMessage = getJoiningMessage(clientName, objectType, objectName);
        joiningThread = joiningMessage.thread;

        try {
            joiningThread.writeMessageHeader(joiningThread.dataOut,
                                message.sessionNo, joiningMessage.id,
                                objectType, T_Authenticate, false, true);
            joiningThread.dataOut.writeInt(retval);
            joiningThread.dataOut.writeBoolean(admitted);
            joiningThread.flush();
            joiningThread.finishMessage();
        } catch (IOException e) {
            error("SessionServer: clientAuthenticate: ", e);
        }
    }


/**
 * <A NAME="SD_CLOSE"></A>
 * <EM>close</EM>
 *
 * @param message
 */

    void
    close(Message message) {
        int retval = 0;

        if (SessionServer_Debug) {
            debug("SessionServer: close:" +
                  " message: " + message);
        }

        try {
            message.thread.writeMessageHeader(message.thread.dataOut,
                                message.sessionNo, message.id,
                                message.type, message.action, false, true);
            message.thread.dataOut.writeInt(retval);
            message.thread.flush();
            message.thread.finishMessage();
        } catch (IOException e) {
            error("SessionServer: close: ", e);
        }

        removeThread(message.thread);
    }


/**
 * <A NAME="SD_CREATEBYTEARRAY"></A>
 * <EM>createByteArray</EM> if the client is joined to this session, a new
 * shared byte array is created in the server for this session. A message
 * is returned to the calling proxy, to indicate the success or failure of
 * this operation.  If a shared byte array with this name already exists,
 * then that one is used and a new one is not created.
 *
 * If a shared byte array with this name already exists and it's value is
 * different than the new one, a change message is sent back to the proxy
 * observer to inform it of the correct value.
 *
 * @param message
 */

    void
    createByteArray(Message message) {
        DataInputStream in            = message.thread.dataIn;
        String          byteArrayName = null;
        String          clientName    = null;
        int             valueLength   = 0;
        byte[]          value         = null;
        ByteArrayImpl   byteArray;
        boolean         created       = false;

        if (SessionServer_Debug) {
            debug("SessionServer: createByteArray:" +
                  " message: " + message);
        }

        try {
            byteArrayName = in.readUTF();
            clientName    = in.readUTF();
            valueLength   = in.readInt();
            value         = message.thread.getData(valueLength);
        } catch (IOException e) {
            error("SessionServer: createByteArray: ", e);
        }

        if ((byteArray = getByteArrayByName(byteArrayName)) == null) {
            byteArray = new ByteArrayImpl(true, byteArrayName, session, value);
            byteArrays.put(byteArrayName, byteArray);
            created = true;
        }

        try {
            value       = byteArray.getValueAsBytes();
            valueLength = value.length;
        } catch (NoSuchByteArrayException nsbe) {
            error("SessionServer: createByteArray: ", nsbe);
        }

        try {
            message.thread.writeMessageHeader(message.thread.dataOut,
                                message.sessionNo, message.id,
                                message.type, message.action, false, true);
            message.thread.dataOut.writeInt(0);
            message.thread.dataOut.writeInt(valueLength);
            message.thread.dataOut.write(value, 0, valueLength);
            message.thread.flush();
            message.thread.finishMessage();
        } catch (IOException e) {
            error("SessionServer: createByteArray: ", e);
        }

        if (created) {
            informListeners(name, clientName, byteArrayName,
                            SessionEvent.BYTEARRAY_CREATED, message.type);
        }
    }


/**
 * <A NAME="SD_CREATECHANNEL"></A>
 * <EM>createChannel</EM> creates a new channel in the server for this
 * session. A message is returned to the calling proxy, to indicate the
 * success or failure of this operation.  Note that the channel may
 * already exist.
 *
 * @param message
 */

    void
    createChannel(Message message) {
        DataInputStream  in          = message.thread.dataIn;
        String           channelName = null;
        String           clientName  = null;
        boolean          reliable    = true;
        boolean          ordered     = true;
        ChannelImpl      channel;
        boolean          created     = false;

        if (SessionServer_Debug) {
            debug("SessionServer: createChannel:" +
                  " message: " + message);
        }

        try {
            channelName = in.readUTF();
            clientName  = in.readUTF();
            reliable    = in.readBoolean();
            ordered     = in.readBoolean();
        } catch (IOException e) {
            error("SessionServer: createChannel: ", e);
        }

        if ((channel = getChannelByName(channelName)) == null) {
            channel = new ChannelImpl(true, channelName, session,
                                      reliable, ordered);
            channels.put(channelName, channel);

            if (cst == null && !reliable) {
                URLString urlString = new URLString(url);

                cst = new UDPChannelServerThread(session, channel,
                                                 urlString.getPort());
                Util.startThread(cst,
                        "UDPChannelServerThread:" + session.getName() +
                        ":" + channel.getName(), false);
            }

            created = true;
        }

        try {
            message.thread.writeMessageHeader(message.thread.dataOut,
                                message.sessionNo, message.id,
                                message.type, message.action, false, true);
            message.thread.dataOut.writeInt(0);
            message.thread.dataOut.writeBoolean(channel.isOrdered());
            message.thread.dataOut.writeBoolean(channel.isReliable());
            message.thread.flush();
            message.thread.finishMessage();
        } catch (IOException e) {
            error("SessionServer: createChannel: ", e);
        }

        if (created) {
            informListeners(name, clientName, channelName,
                            SessionEvent.CHANNEL_CREATED, message.type);
        }
    }


/**
 * <A NAME="SD_CREATETOKEN"></A>
 * <EM>createToken</EM>
 *
 * @param message
 */

    void
    createToken(Message message) {
        DataInputStream  in         = message.thread.dataIn;
        String           tokenName  = null;
        String           clientName = null;
        TokenImpl        token;
        boolean          created    = false;

        if (SessionServer_Debug) {
            debug("SessionServer: createToken:" +
                  " message: " + message);
        }

        try {
            tokenName  = in.readUTF();
            clientName = in.readUTF();
        } catch (IOException e) {
            error("SessionServer: createChannel: ", e);
        }

        if (getTokenByName(tokenName) == null) {
            token = new TokenImpl(true, tokenName, session);
            tokens.put(tokenName, token);
            created = true;
        }

        try {
            message.thread.writeMessageHeader(message.thread.dataOut,
                                message.sessionNo, message.id,
                                message.type, message.action, false, true);
            message.thread.dataOut.writeInt(0);
            message.thread.flush();
            message.thread.finishMessage();
        } catch (IOException e) {
            error("SessionServer: createChannel: ", e);
        }

        if (created) {
            informListeners(name, clientName, tokenName,
                            SessionEvent.TOKEN_CREATED, message.type);
        }
    }


    void
    destroyByteArray(Message message) {
        DataInputStream  in            = message.thread.dataIn;
        String           byteArrayName = null;
        String           clientName    = null;
        int              retval        = 0;
        ByteArrayImpl    byteArray;

        if (SessionServer_Debug) {
            debug("SessionServer: destroyByteArray:" +
                  " message: " + message);
        }

        try {
            byteArrayName = in.readUTF();
            clientName    = in.readUTF();
        } catch (IOException e) {
            error("SessionServer: destroyByteArray: ", e);
        }

        if ((byteArray = getByteArrayByName(byteArrayName)) != null) {
            ByteArrayServer bs = (ByteArrayServer) byteArray.so.getServer();

            bs.expelAllClients(null, name, ByteArrayImpl.M_ByteArray, bs);
            byteArrays.remove(byteArrayName);
        } else {
            retval = JSDTException.NO_SUCH_BYTEARRAY;
        }

        try {
            message.thread.writeMessageHeader(message.thread.dataOut,
                                message.sessionNo, message.id,
                                message.type, message.action, false, true);
            message.thread.dataOut.writeInt(retval);
            message.thread.flush();
            message.thread.finishMessage();
        } catch (IOException e) {
            error("SessionServer: destroyByteArray: ", e);
        }

        if (retval == 0) {
            informListeners(name, clientName, byteArrayName,
                            SessionEvent.BYTEARRAY_DESTROYED, message.type);
        }
    }


    void
    destroyChannel(Message message) {
        DataInputStream  in          = message.thread.dataIn;
        String           channelName = null;
        String           clientName  = null;
        int              retval      = 0;
        ChannelImpl      channel;

        if (SessionServer_Debug) {
            debug("SessionServer: destroyChannel:" +
                  " message: " + message);
        }

        try {
            channelName = in.readUTF();
            clientName  = in.readUTF();
        } catch (IOException e) {
            error("SessionServer: destroyChannel: ", e);
        }

        if ((channel = getChannelByName(channelName)) != null) {
            ChannelServer cs = (ChannelServer) channel.so.getServer();

            cs.expelAllClients(null, name, ChannelImpl.M_Channel, cs);
            channels.remove(channelName);
        } else {
            retval = JSDTException.NO_SUCH_CHANNEL;
        }

        try {
            message.thread.writeMessageHeader(message.thread.dataOut,
                                message.sessionNo, message.id,
                                message.type, message.action, false, true);
            message.thread.dataOut.writeInt(retval);
            message.thread.flush();
            message.thread.finishMessage();
        } catch (IOException e) {
            error("SessionServer: destroyChannel: ", e);
        }

        if (retval == 0) {
            informListeners(name, clientName, channelName,
                            SessionEvent.CHANNEL_DESTROYED, message.type);
        }
    }


    void
    destroySession(Message message) {
        DataInputStream  in          = message.thread.dataIn;
        String           sessionName = null;
        String           clientName  = null;

        if (SessionServer_Debug) {
            debug("SessionServer: destroySession:" +
                  " message: " + message);
        }

        try {
            sessionName = in.readUTF();
            clientName  = in.readUTF();
        } catch (IOException e) {
            error("SessionServer: destroySession: ", e);
        }

        expelAllClients(null, sessionName, SessionImpl.M_Session, this);
        socketServer.removeSession(message.sessionNo);

        try {
            message.thread.writeMessageHeader(message.thread.dataOut,
                                message.sessionNo, message.id,
                                message.type, message.action, false, true);
            message.thread.dataOut.writeInt(0);
            message.thread.flush();
            message.thread.finishMessage();
        } catch (IOException e) {
            error("SessionServer: destroySession: ", e);
        }

        informListeners(name, clientName, sessionName,
                        SessionEvent.DESTROYED, message.type);

        session = null;

        if (message.thread != null) {
            message.thread.terminate();
            message.thread = null;
        }
    }


    void
    destroyToken(Message message) {
        DataInputStream  in         = message.thread.dataIn;
        String           tokenName  = null;
        String           clientName = null;
        int              retval     = 0;
        TokenImpl        token;

        if (SessionServer_Debug) {
            debug("SessionServer: destroyToken:" +
                  " message: " + message);
        }

        try {
            tokenName  = in.readUTF();
            clientName = in.readUTF();
        } catch (IOException e) {
            error("SessionServer: destroyToken: ", e);
        }

        if ((token = getTokenByName(tokenName)) != null) {
            TokenServer ts = (TokenServer) token.so.getServer();

            ts.expelAllClients(null, name, TokenImpl.M_Token, ts);
            tokens.remove(tokenName);
        } else {
            retval = JSDTException.NO_SUCH_TOKEN;
        }

        try {
            message.thread.writeMessageHeader(message.thread.dataOut,
                                message.sessionNo, message.id,
                                message.type, message.action, false, true);
            message.thread.dataOut.writeInt(retval);
            message.thread.flush();
            message.thread.finishMessage();
        } catch (IOException e) {
            error("SessionServer: destroyToken: ", e);
        }

        if (retval == 0) {
            informListeners(name, clientName, tokenName,
                            SessionEvent.TOKEN_DESTROYED, message.type);
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
        if (SessionServer_Debug) {
            debug("SessionServer: getByteArrayByName:" +
                  " name: " + byteArrayName);
        }

        return((ByteArrayImpl) byteArrays.get(byteArrayName));
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
        if (SessionServer_Debug) {
            debug("SessionServer: getChannelByName:" +
                  " name: " + channelName);
        }

        return((ChannelImpl) channels.get(channelName));
    }


/**
 * <A NAME="SD_GETJOININGMESSAGE"></A>
 * <EM>getJoiningMessage</EM>
 *
 * @param clientName
 * @param objectType
 * @param objectName
 *
 * @return
 */

    Message
    getJoiningMessage(String clientName, char objectType, String objectName) {
        if (SessionServer_Debug) {
            debug("SessionServer: getJoiningMessage:" +
                  " client name: " + clientName +
                  " object type: " + objectType +
                  " object name: " + objectName);
        }

        if (objectType == ByteArrayImpl.M_ByteArray) {
            ByteArrayImpl byteArray = getByteArrayByName(objectName);

            if (byteArray != null) {
                ByteArrayServer bs = (ByteArrayServer) byteArray.so.getServer();

                return(bs.joiningMessages.get(clientName));
            }
        } else if (objectType == ChannelImpl.M_Channel) {
            ChannelImpl channel = getChannelByName(objectName);

            if (channel != null) {
                ChannelServer cs = (ChannelServer) channel.so.getServer();

                return(cs.joiningMessages.get(clientName));
            }

        } else if (objectType == SessionImpl.M_Session) {

            return(joiningMessages.get(clientName));

        } else if (objectType == TokenImpl.M_Token) {
            TokenImpl token = getTokenByName(objectName);

            if (token != null) {
                TokenServer ts = (TokenServer) token.so.getServer();

                return(ts.joiningMessages.get(clientName));
            }
        }
        return(null);
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
        if (SessionServer_Debug) {
            debug("SessionServer: getTokenByName:" +
                  " name: " + tokenName);
        }

        return((TokenImpl) tokens.get(tokenName));
    }


/**
 * <A NAME="SD_ADDCONNECTION"></A>
 * <EM>addConnection</EM>
 *
 * @param message the current message being processed.
 */

    void
    addConnection(Message message) {
        int retval = 0;

        if (SessionServer_Debug) {
            debug("SessionServer: addConnection:" +
                  " message: " + message);
        }

        try {
            message.thread.writeMessageHeader(message.thread.dataOut,
                                  message.sessionNo, message.id,
                                  message.type, message.action, false, true);
            message.thread.dataOut.writeInt(retval);
            message.thread.flush();
            message.thread.finishMessage();

            checkCleanupStatus();
            cc.addPingEntry(message.id, message.thread);
        } catch (IOException e) {
            error("SessionServer: addConnection: ", e);
        }
    }


/**
 * <A NAME="SD_REMOVECONNECTION"></A>
 * <EM>removeConnection</EM>
 *
 * @param message the current message being processed.
 */

    void
    removeConnection(Message message) {
        int retval = 0;

        if (SessionServer_Debug) {
            debug("SessionServer: removeConnection:" +
                  " message: " + message);
        }

        try {
            message.thread.writeMessageHeader(message.thread.dataOut,
                                  message.sessionNo, message.id,
                                  message.type, message.action, false, true);
            message.thread.dataOut.writeInt(retval);
            message.thread.flush();
            message.thread.finishMessage();

            checkCleanupStatus();
            cc.removePingEntry(message.id);
        } catch (IOException e) {
            error("SessionServer: removeConnection: ", e);
        }
    }


/**
 * <A NAME="SD_CHECKCLEANUPSTATUS"></A>
 * <EM>checkCleanupStatus</EM>
 */

    private void
    checkCleanupStatus() {
        boolean cleanupPings = Util.getBooleanProperty("cleanupPingingClients",
                                                       cleanupPingingClients);

        if (SessionServer_Debug) {
            debug("SessionServer: checkCleanupStatus.");
        }

        if (cleanupPings) {
            if (cleanupThread == null) {
                cleanupThread = Util.startThread(cc, "CleanupThread", true);
            }
        } else {
            if (cleanupThread != null) {
                cc.setShutdown(true);
                cleanupThread.interrupt();
                cleanupThread = null;
            }
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
        int retval = 0;

        if (SessionServer_Debug) {
            debug("SessionServer: isAlive:" +
                  " message: " + message);
        }

        try {
            message.thread.writeMessageHeader(message.thread.dataOut,
                                  message.sessionNo, message.id,
                                  message.type, message.action, false, true);
            message.thread.dataOut.writeInt(retval);
            message.thread.flush();
            message.thread.finishMessage();

            checkCleanupStatus();
            cc.addPingEntry(message.id, message.thread);
        } catch (IOException e) {
            error("SessionServer: isAlive: ", e);
        }
    }


/**
 * <A NAME="SD_JOIN"></A>
 * <EM>join</EM>
 *
 * @param message
 */

    void
    join(Message message) {
        DataInputStream  in         = message.thread.dataIn;
        String           clientName = null;
        int              retval     = 0;
        ClientImpl       client     = null;

        if (SessionServer_Debug) {
            debug("SessionServer: join:" +
                  " message: " + message);
        }

        try {
            clientName = in.readUTF();
            client     = new ClientImpl(clientName, message.thread);
        } catch (IOException e) {
            error("SessionServer: join: ", e);
        }

        if (clients.containsKey(clientName)) {
            retval = JSDTException.NAME_IN_USE;
        } else {
            clients.put(clientName, client);
        }

        try {
            message.thread.writeMessageHeader(message.thread.dataOut,
                                message.sessionNo, message.id,
                                message.type, message.action, false, true);
            message.thread.dataOut.writeInt(retval);
            message.thread.flush();
            message.thread.finishMessage();
        } catch (IOException e) {
            error("SessionServer: join: ", e);
        }

        if (retval == 0) {
            informListeners(name, clientName, name,
                            SessionEvent.JOINED, message.type);
            addClientThreadConnection(clientName, message.thread);
        }
    }


/**
 * <A NAME="SD_EXPEL"></A>
 * <EM>expel</EM>
 *
 * @param message
 * @param objectType
 */

    protected void
    expel(Message message, char objectType) {
        if (SessionServer_Debug) {
            debug("SessionServer: expel:" +
                  " message: "     + message +
                  " object type: " + objectType);
        }

        super.expel(message, objectType, this);
    }


/**
 * <A NAME="SD_LEAVE"></A>
 * <EM>leave</EM> the given client is leaving this session. If the client
 * had previously grabbed or inhibited any tokens, then then they need to
 * be released.
 *
 * @param message the current message being processed.
 * @param clientName the name of the client leaving this session.
 */

    protected void
    leave(Message message, String clientName) {
        ClientImpl client     = getClientByName(clientName);
        Vector     byteArrays;
        Vector     channels;
        Vector     tokens;

        if (SessionServer_Debug) {
            debug("SessionServer: leave:" +
                  " message: "     + message +
                  " client name: " + clientName);
        }

        if (client != null) {
            byteArrays = client.getByteArrays();
            while (byteArrays.size() != 0) {
                ByteArrayImpl byteArray =
                                (ByteArrayImpl) byteArrays.firstElement();
                ByteArrayServer bs = (ByteArrayServer) byteArray.so.getServer();

                bs.leave(null, clientName);
            }

            if (message != null) {
                cleanupByteArrayListeners(message.thread);
            }

            channels = client.getChannels();
            while (channels.size() != 0) {
                ChannelImpl   channel = (ChannelImpl) channels.firstElement();
                ChannelServer cs      = (ChannelServer) channel.so.getServer();

                cs.leave(null, clientName);
            }

            tokens = client.getTokens();
            while (tokens.size() != 0) {
                TokenImpl   token = (TokenImpl)   tokens.firstElement();
                TokenServer ts    = (TokenServer) token.so.getServer();

                ts.leave(null, clientName);
            }
        }

        super.leave(message, session.getName(),
                    SessionImpl.M_Session, clientName);
    }


/**
 * <A NAME="SD_LISTNAMES"></A>
 * <EM>listNames</EM> lists the names of all the objects of the given type
 * known to this session.
 *
 * @param message
 */

    void
    listNames(Message message) {
        DataInputStream in     = message.thread.dataIn;
        Hashtable       table  = null;
        int             retval = 0;
        int             size;

        if (SessionServer_Debug) {
            debug("SessionServer: listNames:" +
                  " message: " + message);
        }

        try {
            in.readUTF();     // session name.

            message.thread.writeMessageHeader(message.thread.dataOut,
                                message.sessionNo, message.id,
                                message.type, message.action, false, true);
            message.thread.dataOut.writeInt(retval);

            switch (message.action) {
                case T_ListByteArrayNames : table = byteArrays;
                                            break;
                case T_ListChannelNames   : table = channels;
                                            break;
                case T_ListTokenNames     : table = tokens;
            }

            size = table.size();

            message.thread.dataOut.writeInt(size);
            for (Enumeration e = table.keys(); e.hasMoreElements();) {
                message.thread.dataOut.writeUTF((String) e.nextElement());
            }
            message.thread.flush();
            message.thread.finishMessage();
        } catch (IOException e) {
            error("SessionServer: listNames: ", e);
        }
    }


    void
    objectExists(Message message) {
        String           objectName;
        DataInputStream  in     = message.thread.dataIn;
        int              retval = 0;
        boolean          exists = false;

        if (SessionServer_Debug) {
            debug("SessionServer: objectExists:" +
                  " message: " + message);
        }

        try {
            objectName = in.readUTF();

            switch (message.action) {
                case T_ByteArrayExists:
                    exists = (getByteArrayByName(objectName) != null);
                    break;
                case T_ChannelExists:
                    exists = (getChannelByName(objectName) != null);
                    break;
                case T_TokenExists:
                    exists = (getTokenByName(objectName) != null);
            }

            message.thread.writeMessageHeader(message.thread.dataOut,
                                message.sessionNo, message.id,
                                message.type, message.action, false, true);
            message.thread.dataOut.writeInt(retval);
            message.thread.dataOut.writeBoolean(exists);
            message.thread.flush();
            message.thread.finishMessage();
        } catch (IOException e) {
            error("SessionServer: objectExists: ", e);
        }
    }


    void
    objectsJoined(Message message) {
        String           clientName;
        DataInputStream  in     = message.thread.dataIn;
        int              retval = 0;
        int              size   = 0;
        String[]         names  = null;

        if (SessionServer_Debug) {
            debug("SessionServer: objectsJoined:" +
                  " message: " + message);
        }

        try {
            clientName = in.readUTF();

            message.thread.writeMessageHeader(message.thread.dataOut,
                                message.sessionNo, message.id,
                                message.type, message.action, false, true);

            switch (message.action) {
                case T_ByteArraysJoined:
                    names = new String[byteArrays.size()];
                    for (Enumeration e = byteArrays.elements();
                                     e.hasMoreElements();) {
                        ByteArrayImpl b = (ByteArrayImpl) e.nextElement();
                        ByteArrayServer bs = (ByteArrayServer) b.so.getServer();

                        if (bs.getClientByName(clientName) != null) {
                            names[size++] = b.getName();
                        }
                    }
                    break;
                case T_ChannelsJoined:
                    names = new String[channels.size()];
                    for (Enumeration e = channels.elements();
                                     e.hasMoreElements();) {
                        ChannelImpl   c  = (ChannelImpl) e.nextElement();
                        ChannelServer cs = (ChannelServer) c.so.getServer();

                        if (cs.getClientByName(clientName) != null) {
                            names[size++] = c.getName();
                        }
                    }
                    break;
                case T_TokensJoined:
                    names = new String[byteArrays.size()];
                    for (Enumeration e = tokens.elements();
                                     e.hasMoreElements();) {
                        TokenImpl   t  = (TokenImpl) e.nextElement();
                        TokenServer ts = (TokenServer) t.so.getServer();

                        if (ts.getClientByName(clientName) != null) {
                            names[size++] = t.getName();
                        }
                    }
            }

            message.thread.dataOut.writeInt(retval);
            message.thread.dataOut.writeInt(size);
            for (int i = 0; i < size; i++) {
                message.thread.dataOut.writeUTF(names[i]);
            }
            message.thread.flush();
            message.thread.finishMessage();
        } catch (IOException e) {
            error("SessionServer: objectsJoined: ", e);
        }
    }


/**
 * <A NAME="SD_REMOVETHREAD"></A>
 * <EM>removeThread</EM> remove all references to this thread. Remove all
 * entries from all hashtables for any clients associated with this thread.
 * Do a session.leave() for each client associated with this thread. This in
 * turn will force a byteArray.leave(), channel.leave() and a token.leave()
 * for any byte arrays, channels and tokens that this client has joined.
 *
 * @param thread the thread to be removed.
 */

    void
    removeThread(JSDTThread thread) {
        Enumeration e, k;
        boolean found = false;
        Thread  t;

        if (SessionServer_Debug) {
            debug("SessionServer: removeThread:" +
                  " thread: " + thread);
        }

        for (e = clientConnections.elements(), k = clientConnections.keys();
             e.hasMoreElements();) {
            SocketThread currentThread = (SocketThread) e.nextElement();
            String       clientName    = (String) k.nextElement();

            if (thread == currentThread) {
                found = true;

                if (SessionServer_Debug) {
                    debug("SessionServer: removeThread:" +
                          " removing client: " + clientName);
                }

                cleanupListeners(thread, clientName);
                leave(null, clientName);
                clientConnections.remove(clientName);
            }
        }

/* If we've been unable to find this thread in the known client connections,
 * then we can't take the short cut of knowing which bytearrays, channels
 * and tokens to tidyup. Instead we've got to clean up the listeners by
 * iterating through all known manageable objects in this session, searching
 * for this thread.
 */
        if (!found) {
            cleanupListeners(thread);
        }

        t = socketServer.serverThreads.get(thread);
        thread.terminate();
        if (t != null) {
            t.interrupt();
        }
        if (thread instanceof TCPSocketThread) {
            ((TCPSocketThread) thread).closeSocket();
        }
    }


/**
 * <A NAME="SD_SETURL"></A>
 * <EM>setUrl</EM>
 *
 * @param url
 */

    void
    setUrl(String url) {
        if (SessionServer_Debug) {
            debug("SessionServer: setUrl:" +
                  " url: " + url);
        }

        this.url = url;
    }


/**
 * <A NAME="SD_VALIDCLIENT"></A>
 * <EM>validClient</EM>
 *
 * @param client
 *
 * @return
 */

    boolean
    validClient(Client client) {
        if (SessionServer_Debug) {
            debug("SessionServer: validClient:" +
                  " client name: " + client.getName());
        }

        return(clients.containsKey(client.getName()));
    }
}
