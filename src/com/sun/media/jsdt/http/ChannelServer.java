
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
import com.sun.media.jsdt.event.ChannelEvent;
import java.io.*;
import java.util.*;

/**
 * JSDT Channel Server-side class.
 *
 * @version     2.3 - 16th November 2017
 * @author      Rich Burridge
 */

public final class
ChannelServer extends ManageableServer implements AbstractChannelServer {

    // The server-side session associated with this channel server.
    private SessionImpl session;

    // The server-side channel associated with this channel server.
    private ChannelImpl channel;

    // Clients currently joined to this channel and their data direction.
    private Hashtable<Client, Integer> clientDataDirections = null;

    // The clients that have consumers and their associated client id's.
    private Hashtable<Client, Integer> consumerThreads;


/**
 * <A NAME="SD_INITSERVER"></A>
 * <EM>initServer</EM> initialise the "server-side" for this Channel.
 *
 * @param name the name of the server Channel being constructed.
 * @param session the server-side session the Channel belongs to.
 * @param object the server-side Channel.
 */

    public void
    initServer(String name, SessionImpl session, Object object) {
        if (ChannelServer_Debug) {
            debug("ChannelServer: initServer:" +
                  " name: "    + name +
                      " session: " + session +
                      " channel: " + object);
        }

        this.name       = name;
        this.session    = session;
        this.channel    = (ChannelImpl) object;

        clients              = new Hashtable<>();
        clientDataDirections = new Hashtable<>();
        consumerThreads      = new Hashtable<>();

        super.initServer(name, session, object);
    }


/**
 * <A NAME="SD_GETSERVER"></A>
 * <EM>getServer</EM>
 */

    public Object
    getServer() {
        if (ChannelServer_Debug) {
            debug("ChannelServer: getServer.");
        }

        return(this);
    }


/**
 * <A NAME="SD_ADDCONSUMER"></A>
 * <EM>addConsumer</EM>
 *
 * @param message
 */

    void
    addConsumer(Message message) {
        DataInputStream     in           = message.thread.dataIn;
        String              clientName   = null;
        String              consumerName = null;
        int                 retval       = 0;
        ClientImpl          client       = null;
        ChannelConsumerImpl consumer;

        if (ChannelServer_Debug) {
            debug("ChannelServer: addConsumer:" +
                  " message: " + message);
        }

        try {
            clientName   = in.readUTF();
            consumerName = in.readUTF();
            client       = getClientByName(clientName);
        } catch (IOException e) {
            error("ChannelServer: addConsumer: ", e);
        }

        if (client == null) {
            retval = JSDTException.NO_SUCH_CLIENT;
        } else if (clientDataDirections == null) {
            if (clientDataDirections.get(client) == Channel.WRITEONLY) {
                retval = JSDTException.PERMISSION_DENIED;
            }
        }

        if (retval == 0) {
            Hashtable<String, ChannelConsumer> consumers = client.getConsumers();

            consumer = new ChannelConsumerImpl(consumerName, client);
            consumers.put(consumerName, consumer);

            if (!consumerThreads.containsKey(client)) {
                consumerThreads.put(client, message.id);
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
            error("ChannelServer: addConsumer: ", e);
        }

        if (retval == 0) {
            informListeners(message.thread, session.getName(),
                            clientName, name, ChannelEvent.CONSUMER_ADDED,
                            ChannelImpl.M_Channel);
        }
    }


/**
 * <A NAME="SD_ADDJOINEDCLIENT"></A>
 * <EM>addJoinedClient</EM> add this client to the hashtable of clients
 * currently joined to this channel. This channel is also added to the
 * vector of channels that this client is joined to for this session,
 * to make any later cleanup easier.
 *
 * @param client the client to be added.
 */

    private void
    addJoinedClient(ClientImpl client) {
        String        clientName     = client.getName();
        SessionServer ss             = (SessionServer) session.so.getServer();
        ClientImpl    ci             = ss.getClientByName(clientName);
        Vector<Channel> clientChannels = ci.getChannels();

        if (ChannelServer_Debug) {
            debug("ChannelServer: addJoinedClient:" +
                  " client: " + client);
        }

        clients.put(client.getName(), client);
        clientChannels.addElement(channel);
    }


/**
 * <A NAME="SD_JOIN"></A>
 * <EM>join</EM> join a client to a channel. A message is returned to the
 * calling proxy, to indicate the success or failure of this operation.
 * If the client hasn't already joined this session, then the return value
 * is non-zero, indicating an exception will be thrown on the other side.
 * If it's a valid client, then all the observers of this channel are
 * informed that a new client has joined.
 * If successful, part of the return message to the calling proxy indicates
 * whether the channel the client has joined is reliable or not.
 *
 * @param message
 */

    void
    join(Message message) {
        DataInputStream  in         = message.thread.dataIn;
        String           clientName = null;
        ClientImpl       client     = null;
        int              mode       = 0;
        int              retval     = 0;

        if (ChannelServer_Debug) {
            debug("ChannelServer: join:" +
                  " message: " + message);
        }

        try {
            clientName = in.readUTF();
            client     = new ClientImpl(clientName, message.id);
            mode       = in.readInt();
        } catch (IOException e) {
            error("ChannelServer: join: ", e);
        }

        if (!((SessionServer) session.so).validClient(client)) {
            retval = JSDTException.NO_SUCH_CLIENT;
        } else {
            if (clients.containsKey(clientName)) {
                retval = JSDTException.NAME_IN_USE;
            } else {
                addJoinedClient(client);
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
            error("ChannelServer: join: ", e);
        }

        if (retval == 0) {
            clientDataDirections.put(client, mode);

            informListeners(message.thread, session.getName(),
                            clientName, name, ChannelEvent.JOINED,
                            ChannelImpl.M_Channel);
            addClientIdConnection(clientName, message.id);
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
        if (ChannelServer_Debug) {
            debug("ChannelServer: expel:" +
                  " message: "     + message +
                  " object type: " + objectType);
        }

        super.expel(message, objectType, this);
    }


/**
 * <A NAME="SD_LEAVE"></A>
 * <EM>leave</EM> the given client is leaving this channel.
 *
 * @param message the current message being processed.
 * @param clientName the name of the client leaving this channel.
 * @param isCleanup set if we had a client leave a Session (either
 * deliberately or the result of a lost connection), and we are tidying up
 * it's channel connections.
 */

    protected void
    leave(Message message, String clientName, boolean isCleanup) {
        ClientImpl client;

        if (ChannelServer_Debug) {
            debug("ChannelServer: leave:" +
                  " message: "     + message +
                  " client name: " + clientName +
                  " isCleanup: "   + isCleanup);
        }

        if ((client = getClientByName(clientName)) != null) {
            removeJoinedClient(client);
        }
        super.leave(message, session.getName(),
                    ChannelImpl.M_Channel, clientName, isCleanup);

        if (client != null) {
            clientDataDirections.remove(client);

            if (consumerThreads.remove(client) != null) {
                if (message != null) {
                    informListeners(message.thread, session.getName(),
                            clientName, name, ChannelEvent.CONSUMER_REMOVED,
                            ChannelImpl.M_Channel);
                }
            }
        }
    }


/**
 * <A NAME="SD_REMOVECONSUMER"></A>
 * <EM>removeConsumer</EM>
 *
 * @param message
 */

    void
    removeConsumer(Message message) {
        DataInputStream  in           = message.thread.dataIn;
        String           clientName   = null;
        String           consumerName = null;
        int              retval       = 0;
        int              noConsumers  = 0;
        ClientImpl       client       = null;
        Hashtable        consumers;
        ChannelConsumer  consumer     = null;

        if (ChannelServer_Debug) {
            debug("ChannelServer: removeConsumer:" +
                  " message: " + message);
        }

        try {
            clientName   = in.readUTF();
            consumerName = in.readUTF();
            client       = getClientByName(clientName);
        } catch (IOException e) {
            error("ChannelServer: removeConsumer: ", e);
        }

        if (client == null) {
            retval = JSDTException.NO_SUCH_CLIENT;
        } else {
            consumers = client.getConsumers();

            if ((consumer = (ChannelConsumer)
                                consumers.remove(consumerName)) == null) {
                retval = JSDTException.NO_SUCH_CONSUMER;
            } else {
                noConsumers = consumers.size();
            }
        }

        if (retval == 0) {
            if (noConsumers == 0) {
                // XXX: Probably wrong; should be "client" not "consumer".
               consumerThreads.remove(consumer);
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
            error("ChannelServer: removeConsumer: ", e);
        }

        if (retval == 0) {
            informListeners(message.thread, session.getName(),
                            clientName, name, ChannelEvent.CONSUMER_REMOVED,
                            ChannelImpl.M_Channel);
        }
    }


/**
 * <A NAME="SD_LISTCONSUMERNAMES"></A>
 * <EM>listConsumerNames</EM> "server-side" method that provides a list
 *  of all the clients who are currently consuming this Channel.
 *
 * @param message
 */

    protected void
    listConsumerNames(Message message) {
        int retval = 0;
        int size   = 0;

        if (ChannelServer_Debug) {
            debug("ChannelServer: listConsumerNames:" +
                  " message: " + message);
        }

        try {
            message.thread.writeMessageHeader(message.thread.dataOut,
                                message.sessionNo, message.id,
                                message.type, message.action, false, true);
            message.thread.dataOut.writeInt(retval);
            for (Enumeration e = clients.elements(); e.hasMoreElements();) {
                ClientImpl client = (ClientImpl) e.nextElement();

                if (client.getConsumers().size() != 0) {
                    size++;
                }
            }
            message.thread.dataOut.writeInt(size);

            for (Enumeration e = clients.elements(); e.hasMoreElements();) {
                ClientImpl client = (ClientImpl) e.nextElement();

                if (client.getConsumers().size() != 0) {
                    message.thread.dataOut.writeUTF(client.getName());
                }
            }

            message.thread.flush();
            message.thread.finishMessage();
        } catch (IOException e) {
            error("ChannelServer: listConsumerNames: ", e);
        }
    }


/**
 * <A NAME="SD_REMOVEJOINEDCLIENT"></A>
 * <EM>removeJoinedClient</EM> remove this channel from the vector of
 * channels that this client has joined.
 *
 * @param client the client to be removed.
 */

    private void
    removeJoinedClient(ClientImpl client) {
        String        clientName     = client.getName();
        SessionServer ss             = (SessionServer) session.so.getServer();
        ClientImpl    ci             = ss.getClientByName(clientName);
        Vector        clientChannels = ci.getChannels();

        if (ChannelServer_Debug) {
            debug("ChannelServer: removeJoinedClient:" +
                  " client: " + client);
        }

        clientChannels.removeElement(channel);
    }


/**
 * <A NAME="SD_SEND"></A>
 * <EM>send</EM> is used to send a data message to the appropriate listeners
 * on the channel. This can be a single client, all clients or all other
 * clients except the sender.
 *
 * @param message
 * @param channelName
 */

    void
    send(Message message, String channelName) {
        DataInputStream  in           = message.thread.dataIn;
        String           senderName   = null;
        char             recipient    = 0;
        String           receiverName = null;
        int              priority     = 0;
        int              length       = 0;
        byte[]           data         = null;
        int              retval       = 0;
        Client           sender;

        if (ChannelServer_Debug) {
            debug("ChannelServer: send:" +
                  " message: "      + message +
                  " channel name: " + channelName);
        }

        try {
            senderName = in.readUTF();
            recipient  = in.readChar();
            if (recipient == ChannelImpl.D_Client) {
                receiverName = in.readUTF();
            }
            priority  = in.readInt();
            in.readBoolean();                  // uniform
            length    = in.readInt();
            data      = message.thread.getData(length);
        } catch (IOException e) {
            error("ChannelServer: send: ", e);
        }

        sender = getClientByName(senderName);
        if (sender == null) {
            retval = JSDTException.NO_SUCH_CLIENT;
        } else {
            if (channel.isReliable()) {
                int clientId = ((Integer) ((ClientImpl) sender).getCheck());

                if (clientId != message.id) {
                    retval = JSDTException.PERMISSION_DENIED;
                }
            }

            if (clientDataDirections.get(sender) == Channel.READONLY) {
                retval = JSDTException.PERMISSION_DENIED;
            }
        }

        if (retval == 0) {
            if (recipient == ChannelImpl.D_All ||
                recipient == ChannelImpl.D_Others) {
                Enumeration e, k;

                for (e = consumerThreads.elements(),
                     k = consumerThreads.keys(); e.hasMoreElements();) {
                    Integer idObj  = (Integer) e.nextElement();
                    Client  client = (Client)  k.nextElement();

                    if (recipient == ChannelImpl.D_Others) {
                        String name = client.getName();

                        if (name.equals(senderName)) {
                            continue;
                        }
                    }

                    try {
                        message.thread.writeMessageHeader(
                                    message.thread.dataOut, message.sessionNo,
                                    idObj, message.type,
                                    T_DataReceived, false, false);
                        message.thread.dataOut.writeUTF(channelName);
                        message.thread.dataOut.writeUTF(senderName);
                        message.thread.dataOut.writeUTF(client.getName());
                        message.thread.dataOut.writeInt(priority);
                        message.thread.dataOut.writeInt(length);
                        message.thread.dataOut.write(data, 0, length);
                        message.thread.flush();
                        message.thread.finishMessage();
                    } catch (IOException ce) {
                        error("ChannelServer: send: ", ce);
                    }
                }
            } else {
                int clientId = getIdForClient(receiverName);

                if (clientId != 0) {
                    ClientImpl client    = getClientByName(receiverName);
                    Hashtable  consumers = client.getConsumers();

                    if (consumers.size() == 0) {
                        retval = JSDTException.NO_SUCH_CONSUMER;
                    } else {
                        try {
                            message.thread.writeMessageHeader(
                                message.thread.dataOut, message.sessionNo,
                                clientId, message.type,
                                T_DataReceived, false, false);
                            message.thread.dataOut.writeUTF(channelName);
                            message.thread.dataOut.writeUTF(senderName);
                            message.thread.dataOut.writeUTF(receiverName);
                            message.thread.dataOut.writeInt(priority);
                            message.thread.dataOut.writeInt(length);
                            message.thread.dataOut.write(data, 0, length);
                            message.thread.flush();
                            message.thread.finishMessage();
                        } catch (IOException e) {
                            retval = JSDTException.NO_SUCH_CLIENT;
                        }
                    }
                } else {
                    retval = JSDTException.NO_SUCH_CLIENT;
                }
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
            error("ChannelServer: send: ", e);
        }
    }
}
