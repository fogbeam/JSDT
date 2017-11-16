
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
import java.io.*;
import java.util.Hashtable;

/**
 * JSDT Session client-side proxy class.
 *
 * @version     2.3 - 30th October 2017
 * @author      Rich Burridge
 * @author      Andrea Colpo
 */

public final class
ChannelProxy extends ManageableProxy implements AbstractChannelProxy {

    // The client-side channel associated with this channel client.
    private Channel channel;

    // Clients currently joined to this channel and their data direction.
    private Hashtable<Client, Integer> clientDataDirections = null;

    // Clients currently receiving data from this channel.
    private Hashtable<String, ReceiveClient> receiveClients = null;

    // The consumers for the various clients on this channel.
    protected Hashtable<Client, Client> clientConsumers = null;

    /* Channel threads for sending and consuming data (one for each
     * priority level).
     */
    private HttpThread[] channelThreads;

    // Queue of incoming data messages received from the channel server.
    static DataReceivedQueue drq = null;

    // Number of DataReceivedThreads currently created.
    private static int noThreads = 0;


/**
 * <A NAME="SD_INITPROXY"></A>
 * <EM>initProxy</EM> initialise the "client-side" proxy for this Channel.
 *
 * @param name the name of the Channel proxy being constructed.
 * @param session the client-side session the Channel belongs to.
 * @param object the client-side Channel.
 */

    public void
    initProxy(String name, SessionImpl session, Object object) {
        int poolSize = Util.getIntProperty("maxThreadPoolSize",
                                           maxThreadPoolSize);

        if (ChannelProxy_Debug) {
            debug("ChannelProxy: initProxy:" +
                  "\n     name: "    + name +
                  "\n     session: " + session +
                  "\n     drq: "     + drq +
                  "\n     channel: " + object);
        }

        this.name    = name;
        this.session = session;
        this.channel = (Channel) object;
        super.initProxy(name, session, object);

        clientDataDirections = new Hashtable<>();
        clientConsumers      = new Hashtable<>();
        receiveClients       = new Hashtable<>();

        synchronized (this) {
            if (drq == null) {
                drq = new DataReceivedQueue();
            }

            if (ChannelProxy_Debug) {
                debug("ChannelProxy: initProxy: before (noThreads++[" +
                      (noThreads+1) +
                       "] < poolSize[" + poolSize + "])");
            }

            if ((noThreads+1) < poolSize) {
                incrementNoThreads();
                Util.startThread(new DataReceivedThread(drq, this),
                                 "DataReceivedThread:" + noThreads, true);
            }
        }
    }


    public Object
    getProxy() {
        if (ChannelProxy_Debug) {
            debug("ChannelProxy: getProxy.");
        }

        return(this);
    }


/**
 * <A NAME="SD_ADDCONSUMER"></A>
 * <EM>addConsumer</EM> add a new channel consumer for this client.
 * The client's channel consumers gets informed when there is data available
 * on a channel.
 *
 * @param client the client to associate this consumer with.
 * @param consumer the new channel consumer for this client.
 *
 * @exception ConnectionException if a connection error occured.
 * @exception InvalidClientException if the Client is invalid is some way (ie.
 * its getName() method returns null).
 * @exception NoSuchChannelException if the channel given does not exist.
 * @exception NoSuchClientException if the client given doesn't exist.
 * @exception NoSuchConsumerException if the consumer given doesn't exist.
 * @exception NoSuchSessionException if this Session doesn't exist.
 * @exception PermissionDeniedException if you do not have permission for
 * this operation.
 * @exception TimedOutException if no reply was received for this operation
 * in the given timeout period.
 */

    public void
    addConsumer(Client client, ChannelConsumer consumer)
                throws ConnectionException, InvalidClientException,
                       NoSuchChannelException, NoSuchClientException,
                       NoSuchConsumerException, NoSuchSessionException,
                       PermissionDeniedException, TimedOutException {
        DataInputStream in;
        Message         message;
        int             retval;
        int             id           = sp.proxyThread.getId();
        short           sessionNo    = sp.getSessionNo();
        String          consumerName = consumer.toString();
        String          clientName   = Util.getClientName(client);
        char            type         = ChannelImpl.M_Channel;

        if (ChannelProxy_Debug) {
            debug("ChannelProxy: addConsumer:" +
                  " client: "   + client  +
                  " consumer: " + consumer);
        }

        if (session == null) {
            throw new NoSuchSessionException();
        }

        try {
            sp.proxyThread.writeMessageHeader(sp.proxyThread.dataOut,
                               sessionNo, id, type, T_AddConsumer, true, true);
            sp.proxyThread.dataOut.writeUTF(name);
            sp.proxyThread.dataOut.writeUTF(clientName);
            sp.proxyThread.dataOut.writeUTF(consumerName);
            sp.proxyThread.flush();
            message = sp.proxyThread.waitForReply();

            in     = message.thread.dataIn;
            retval = in.readInt();
            sp.proxyThread.finishReply();

            if (retval != 0) {
                switch (retval) {
                    case JSDTException.NO_SUCH_CHANNEL:
                        throw new NoSuchChannelException();
                    case JSDTException.NO_SUCH_CLIENT:
                        throw new NoSuchClientException();
                    case JSDTException.NO_SUCH_CONSUMER:
                        throw new NoSuchConsumerException();
                    case JSDTException.NO_SUCH_SESSION:
                        session = null;
                        throw new NoSuchSessionException();
                    case JSDTException.PERMISSION_DENIED:
                        throw new PermissionDeniedException();
                    default:
                        error("ChannelProxy: addConsumer: ",
                              "impl.unknown.exception.type", retval);
                }
            }
        } catch (IOException e) {
            sp.proxyThread.finishReply();
            throw new ConnectionException();
        }

        synchronized (clientConsumers) {
            Hashtable<String, ChannelConsumer> consumers;
            ClientImpl c;

            if ((c = (ClientImpl) clientConsumers.get(client)) == null) {
                c = new ClientImpl(clientName, sp.proxyThread);
                clientConsumers.put(client, c);
            }
            consumers = c.getConsumers();
            consumers.put(consumerName, consumer);
        }
    }


/**
 * <A NAME="SD_DATARECEIVED"></A>
 * <EM>dataReceived</EM>
 *
 * @param message
 * @param channelName
 */

    void
    dataReceived(Message message, String channelName) {
        DataInputStream in           = message.thread.dataIn;
        String          senderName   = null;
        String          receiverName = null;
        int             priority     = 0;
        int             length;
        Data            data         = null;

        if (ChannelProxy_Debug) {
            debug("ChannelProxy: dataReceived:" +
                  " message: "     + message +
                  "channel name: " + channelName);
        }

        try {
            senderName   = in.readUTF();
            receiverName = in.readUTF();
            priority     = in.readInt();
            length       = in.readInt();
            data         = new Data(message.thread.getData(length));
        } catch (IOException e) {
            error("ChannelProxy: dataReceived: ", e);
        }

        data.setPriority(priority);
        data.setSenderName(senderName);
        data.setChannel(channel);
        if (drq != null) {
            drq.putMessage(this, receiverName, channel.isOrdered(), data);
        }
    }


/**
 * <A NAME="SD_REMOVECONSUMER"></A>
 * <EM>removeConsumer</EM> delete a channel consumer from this client.
 * This channel consumer will no longer get informed when there is data
 * available on this channel.
 *
 * @param client the client to associate this consumer with.
 * @param consumer the channel consumer to delete from this client.
 *
 * @exception ConnectionException if a connection error occured.
 * @exception InvalidClientException if the Client is invalid is some way (ie.
 * its getName() method returns null).
 * @exception NoSuchChannelException if the channel given does not exist.
 * @exception NoSuchClientException if the client given doesn't exist.
 * @exception NoSuchConsumerException if the consumer given doesn't exist.
 * @exception NoSuchSessionException if this Session doesn't exist.
 * @exception PermissionDeniedException if you do not have permission for
 * this operation.
 * @exception TimedOutException if no reply was received for this operation
 * in the given timeout period.
 */

    public void
    removeConsumer(Client client, ChannelConsumer consumer)
                throws ConnectionException, InvalidClientException,
                       NoSuchChannelException, NoSuchClientException,
                       NoSuchConsumerException, NoSuchSessionException,
                       PermissionDeniedException, TimedOutException {
        DataInputStream in;
        Message         message;
        int             retval;
        int             id           = sp.proxyThread.getId();
        short           sessionNo    = sp.getSessionNo();
        String          consumerName = consumer.toString();
        String          clientName   = Util.getClientName(client);
        char            type         = ChannelImpl.M_Channel;

        if (ChannelProxy_Debug) {
            debug("ChannelProxy: removeConsumer:" +
                  " client: "   + client +
                  " consumer: " + consumer);
        }

        if (session == null) {
            throw new NoSuchSessionException();
        }

        try {
            sp.proxyThread.writeMessageHeader(sp.proxyThread.dataOut,
                           sessionNo, id, type, T_RemoveConsumer, true, true);
            sp.proxyThread.dataOut.writeUTF(name);
            sp.proxyThread.dataOut.writeUTF(clientName);
            sp.proxyThread.dataOut.writeUTF(consumerName);
            sp.proxyThread.flush();
            message = sp.proxyThread.waitForReply();

            in     = message.thread.dataIn;
            retval = in.readInt();
            sp.proxyThread.finishReply();

            if (retval != 0) {
                switch (retval) {
                    case JSDTException.NO_SUCH_CHANNEL:
                        throw new NoSuchChannelException();
                    case JSDTException.NO_SUCH_CLIENT:
                        throw new NoSuchClientException();
                    case JSDTException.NO_SUCH_CONSUMER:
                        throw new NoSuchConsumerException();
                    case JSDTException.NO_SUCH_SESSION:
                        session = null;
                        throw new NoSuchSessionException();
                    case JSDTException.PERMISSION_DENIED:
                        throw new PermissionDeniedException();
                    default:
                        error("ChannelProxy: removeConsumer: ",
                              "impl.unknown.exception.type", retval);
                }
            }
        } catch (IOException e) {
            sp.proxyThread.finishReply();
            throw new ConnectionException();
        }

        synchronized (clientConsumers) {
            Hashtable  consumers;
            ClientImpl c;

            if ((c = (ClientImpl) clientConsumers.get(client)) == null) {
                c = new ClientImpl(clientName, sp.proxyThread);
                clientConsumers.put(client, c);
            }
            consumers = c.getConsumers();
            consumers.remove(consumerName);
        }
    }


/**
 * <A NAME="SD_LISTCONSUMERNAMES"></A>
 * list the names of the Clients who are currently consuming this Channel.
 *
 * @return a sorted array of names of Clients currently comsuming this Channel.
 *
 * @exception ConnectionException if a connection error occured.
 * @exception NoSuchChannelException if this Channel doesn't exist.
 * @exception NoSuchSessionException if this Session doesn't exist.
 * @exception TimedOutException if no reply was received for this operation
 * in the given timeout period.
 */

    public String[]
    listConsumerNames()
                throws ConnectionException, NoSuchChannelException,
                       NoSuchSessionException, TimedOutException {
        DataInputStream in;
        Message         message;
        int             retval;
        int             n;
        HttpThread      thread      = sp.proxyThread;
        int             id          = thread.getId();
        short           sessionNo   = sp.getSessionNo();
        char            type        = ChannelImpl.M_Channel;
        String[]        clientNames = null;

        if (ChannelProxy_Debug) {
            debug("ChannelProxy: listConsumerNames.");
        }

        if (session == null) {
            throw new NoSuchSessionException();
        }

        try {
            thread.writeMessageHeader(thread.dataOut, sessionNo, id,
                                      type, T_ListConsumerNames, true, true);
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
                    case JSDTException.NO_SUCH_CHANNEL:
                        throw new NoSuchChannelException();
                    case JSDTException.NO_SUCH_SESSION:
                        session = null;
                        throw new NoSuchSessionException();
                    default:
                        error("ChannelProxy: listConsumerNames: ",
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
 * <A NAME="SD_JOIN"></A>
 * <EM>join</EM>
 *
 * @param client identifies the client wishing to join this channel.
 * @param authenticate if true, authenticate the client.
 * @param mode the mode which can be one of, READONLY, WRITEONLY or READWRITE.
 *
 * @exception ConnectionException if a connection error occured.
 * @exception InvalidClientException if the Client is invalid is some way (ie.
 * its getName() method returns null).
 * @exception NameInUseException if
 * @exception NoSuchChannelException if
 * @exception NoSuchClientException if
 * @exception NoSuchSessionException if the session given does not exist.
 * @exception PermissionDeniedException if
 * @exception TimedOutException if no reply was received for this operation
 * in the given timeout period.
 */

    public void
    join(Client client, boolean authenticate, int mode)
                throws ConnectionException, InvalidClientException,
                       NoSuchChannelException, NoSuchClientException,
                       NoSuchSessionException, PermissionDeniedException,
                       NameInUseException, TimedOutException {
        DataInputStream in;
        Message         message;
        int             retval;
        int             id          = sp.proxyThread.getId();
        short           sessionNo   = sp.getSessionNo();
        String          channelName = channel.getName();
        char            type        = ChannelImpl.M_Channel;
        ChannelImpl     channel     = null;
        String          clientName  = Util.getClientName(client);

        if (ChannelProxy_Debug) {
            debug("ChannelProxy: join:" +
                  " client: "       + client +
                  " authenticate: " + authenticate +
                  " mode: "         + mode);
        }

        if (session == null) {
            throw new NoSuchSessionException();
        }

        sp.putPriviledgedClient(client, AuthenticationInfo.JOIN, type);

        if (authenticate) {
            AuthenticationInfo info = new
                AuthenticationInfo(session, AuthenticationInfo.JOIN,
                                   channelName, AuthenticationInfo.CHANNEL);

            if (manager != null) {  /* Client and manager have same proxy? */
                try {
                    if (!((ChannelManager) manager).channelRequest(
                                                 channel, info, client)) {
                        throw new PermissionDeniedException();
                    }
                } catch (Throwable th) {
                    error("ChannelProxy: join: ",
                          "impl.thrown", th + " by manager.");
                }
            } else if (!authenticateClient(id, info, clientName)) {
                throw new PermissionDeniedException();
            }
        }

        try {
            sp.proxyThread.writeMessageHeader(sp.proxyThread.dataOut,
                                  sessionNo, id, type, T_Join, true, true);
            sp.proxyThread.dataOut.writeUTF(channelName);
            sp.proxyThread.dataOut.writeUTF(clientName);
            sp.proxyThread.dataOut.writeInt(mode);
            sp.proxyThread.flush();
            message = sp.proxyThread.waitForReply();

            in     = message.thread.dataIn;
            retval = in.readInt();
            message.thread.finishReply();

            if (retval != 0) {
                switch (retval) {
                    case JSDTException.NO_SUCH_CHANNEL:
                        throw new NoSuchChannelException();
                    case JSDTException.NO_SUCH_CLIENT:
                        throw new NoSuchClientException();
                    case JSDTException.NO_SUCH_SESSION:
                        session = null;
                        throw new NoSuchSessionException();
                    case JSDTException.PERMISSION_DENIED:
                        throw new PermissionDeniedException();
                    case JSDTException.NAME_IN_USE:
                        throw new NameInUseException();
                    default:
                        error("ChannelProxy: join: ",
                              "impl.unknown.exception.type", retval);
                }
            }
        } catch (IOException e) {
            sp.proxyThread.finishReply();
            throw new ConnectionException();
        }

        synchronized (clients) {
            clients.put(Util.getClientName(client), client);
        }

        synchronized (clientDataDirections) {
            clientDataDirections.put(client, mode);
        }

        setChannelThreads();
    }


/**
 * <A NAME="SD_LEAVE"></A>
 * <EM>leave</EM> removes a Client from this Channel. This Client will no
 * longer be known to this Channel. Listeners of this Channel will be
 * sent an indication when this happens. Any consumers that this Client had,
 * will also be removed.
 *
 * @param client the Client in question.
 *
 * @exception ConnectionException if a connection error occured.
 * @exception InvalidClientException if the Client is invalid is some way (ie.
 * its getName() method returns null).
 * @exception NoSuchChannelException if this Channel doesn't exist.
 * @exception NoSuchClientException if this Client doesn't exist.
 * @exception NoSuchSessionException if this Session doesn't exist.
 * @exception TimedOutException if no reply was received for this operation
 * in the given timeout period.
 */

    public void
    leave(Client client)
                throws ConnectionException, InvalidClientException,
                       NoSuchChannelException, NoSuchClientException,
                     NoSuchSessionException, TimedOutException {
        if (ChannelProxy_Debug) {
            debug("ChannelProxy: leave:" +
                  " client: " + client);
        }

        try {
            super.leave(client, ChannelImpl.M_Channel);
        } catch (NoSuchByteArrayException | NoSuchTokenException nse) {
        }

        synchronized (clientConsumers) {
            clientConsumers.remove(client);
        }

        synchronized (receiveClients) {
            receiveClients.remove(Util.getClientName(client));
        }
    }


/**
 * <A NAME="SD_RECEIVE"></A>
 * <EM>receive</EM> is used to receive data from other clients joined to
 * this channel.
 *
 * If the timeout value is to be ignored, then it will block if there is no
 * data to read. The <EM>dataAvailable</EM> method can be used to check if
 * there is data that can be received on this channel.
 *
 * Otherwise if Data is immediately available then it will return with it,
 * else it will wait until the timeout period, specified by the
 * <code>timeout</code> argument in milliseconds, has elapsed. If no Data is
 * available at this time, it will return null. Note that if Data becomes
 * available during the timeout period, this method will be woken up and
 * that Data is immediately returned.
 *
 * @param client identifies the client wishing to receive data from this
 * channel. This client must already be successfully joined to this channel.
 * @param timeout the maximum time to wait in milliseconds.
 * @param ignoreTimeout ignore the timeout argument, and block until data
 * becomes available.
 *
 * @return the next Data object available on this channel, or null if a
 * timeout value was specified and the timeout period has expired, and there
 * is currently no Data available.
 *
 * @exception ConnectionException if a connection error occured.
 * @exception InvalidClientException if the Client is invalid is some way (ie.
 * its getName() method returns null).
 * @exception NoSuchClientException if the client given doesn't exist.
 * @exception NoSuchSessionException if this Session doesn't exist.
 * @exception PermissionDeniedException if you do not have permission for
 * this operation.
 * @exception TimedOutException if no reply was received for this operation
 * in the given timeout period.
 */

    public Data
    receive(Client client, long timeout, boolean ignoreTimeout)
                throws ConnectionException, InvalidClientException,
                       NoSuchClientException, NoSuchSessionException,
                       PermissionDeniedException, TimedOutException {
        ReceiveClient receiveClient;
        String        clientName    = Util.getClientName(client);
        Data          data          = null;

        if (ChannelProxy_Debug) {
            debug("ChannelProxy: receive:" +
                  " client: "         + client +
                  " timeout: "        + timeout +
                  " ignore timeout? " + ignoreTimeout);
        }

        if (session == null) {
            throw new NoSuchSessionException();
        }

        synchronized (clients) {
            if (clients.get(clientName) == null) {
                throw new NoSuchClientException();
            }
        }

        if (clientDataDirections.get(client) == Channel.WRITEONLY) {
            throw new PermissionDeniedException();
        }

        if ((receiveClient = receiveClients.get(clientName)) == null) {
            try {
                receiveClient = new ReceiveClient(channel, client);
                receiveClients.put(clientName, receiveClient);
                addConsumer(client, receiveClient);
            } catch (PermissionDeniedException | ConnectionException |
                     NoSuchSessionException e) {
                throw e;
            } catch (JSDTException je) {
                error("ChannelProxy: receive: ", je);
            }
        }

        try {
            data = receiveClient.getMessage(timeout, ignoreTimeout);
        } catch (Exception e) {
            error("ChannelProxy: receive: ", e);
        }
        return(data);
    }


/**
 * <A NAME="SD_DATAAVAILABLE"></A>
 * <EM>dataAvailable</EM>
 *
 * @param client identifies the client wishing to check if there is data
 * available to receive on this channel. This client must already be
 * successfully joined to this channel.
 *
 * @return true if data available to read on this channel; false if not.
 *
 * @exception ConnectionException if a connection error occured.
 * @exception InvalidClientException if the Client is invalid is some way (ie.
 * its getName() method returns null).
 * @exception NoSuchClientException if the client given doesn't exist.
 * @exception NoSuchSessionException if this Session doesn't exist.
 * @exception PermissionDeniedException if you do not have permission for
 * this operation.
 * @exception TimedOutException if no reply was received for this operation
 * in the given timeout period.
 */

    public boolean
    dataAvailable(Client client)
                throws ConnectionException, InvalidClientException,
                       NoSuchClientException, NoSuchSessionException,
                       PermissionDeniedException, TimedOutException {
        ReceiveClient receiveClient;
        String        clientName    = Util.getClientName(client);
        Integer       direction;

        if (ChannelProxy_Debug) {
            debug("ChannelProxy: dataAvailable:" +
                  " client: " + client);
        }

        if (session == null) {
            throw new NoSuchSessionException();
        }

        if ((direction = clientDataDirections.get(client)) != null) {
            if (direction == Channel.WRITEONLY) {
                throw new PermissionDeniedException();
            }
        } else {
            throw new NoSuchClientException();
        }

        if ((receiveClient = receiveClients.get(clientName)) == null) {
            try {
                receiveClient = new ReceiveClient(channel, client);
                addConsumer(client, receiveClient);
                receiveClients.put(clientName, receiveClient);
            } catch (PermissionDeniedException | ConnectionException |
                     NoSuchSessionException e) {
                throw e;
            } catch (JSDTException je) {
                error("ChannelProxy: dataAvailable: ", je);
            }
        }

        return(receiveClient.dataAvailable());
    }


/**
 * <A NAME="SD_SEND"></A>
 * <EM>send</EM> is used to send data to clients joined to the specified
 * channel. A message containing the data is sent to the session server
 * which then sends messages to the appropriate receiving channel listeners.
 *
 * @param sendingClient the client sending the data.
 * @param recipient who is going to receive this message. This can be all
 * channel receipts, or all other channel recipients or just a single client.
 * @param receivingClientName the name of the client receiving the data, or
 * null if we are sending this message to all (or all other) channel
 * recipients.
 * @param data the data being send over this channel.
 * @param uniform set true if this is a uniform send.
 *
 * @exception ConnectionException if a connection error occured.
 * @exception InvalidClientException if the Client is invalid is some way (ie.
 * its getName() method returns null).
 * @exception NoSuchChannelException if the channel given does not exist.
 * @exception NoSuchClientException if the client given does not exist.
 * @exception NoSuchConsumerException if this Client doesn't have a
 * ChannelConsumer associated with it.
 * @exception NoSuchSessionException if this Session doesn't exist.
 * @exception PermissionDeniedException if you do not have permission for
 * this operation.
 * @exception TimedOutException if no reply was received for this operation
 * in the given timeout period.
 */

    public void
    send(Client sendingClient, char recipient, String receivingClientName,
         Data data, boolean uniform)
                throws ConnectionException, InvalidClientException,
                       NoSuchChannelException, NoSuchClientException,
                       NoSuchConsumerException, NoSuchSessionException,
                       PermissionDeniedException, TimedOutException {
        DataInputStream in;
        Message         message;
        int             retval;
        int             id;
        int             priority  = data.getPriority();
        HttpThread      thread    = null;
        int             length    = data.getLength();
        short           sessionNo = sp.getSessionNo();
        char            type      = ChannelImpl.M_Channel;

        if (ChannelProxy_Debug) {
            debug("ChannelProxy: send:" +
                  " sending client: "   + sendingClient +
                  " recipient: "        + recipient +
                  " receiving client: " + receivingClientName +
                  " data: "             + data +
                  " uniform: "          + uniform);
        }

        if (session == null) {
            throw new NoSuchSessionException();
        }

        if (sp.proxyThread == null) {
            throw new ConnectionException();
        }

        id = sp.proxyThread.getId();

        synchronized (clients) {
            if (clients.get(Util.getClientName(sendingClient)) == null) {
                throw new NoSuchClientException();
            }
        }

        try {
            thread = channelThreads[priority];
            thread.writeMessageHeader(thread.dataOut, sessionNo, id,
                                    type, T_Send, true, true);
            thread.dataOut.writeUTF(channel.getName());
            thread.dataOut.writeUTF(Util.getClientName(sendingClient));
            thread.dataOut.writeChar(recipient);
            if (recipient == ChannelImpl.D_Client) {
                thread.dataOut.writeUTF(receivingClientName);
            }
            thread.dataOut.writeInt(data.getPriority());
            thread.dataOut.writeBoolean(uniform);
            thread.dataOut.writeInt(length);
            thread.dataOut.write(data.getDataAsBytes(), 0, length);
            thread.flush();

            message = thread.waitForReply();
            in      = message.thread.dataIn;
            retval  = in.readInt();
            thread.finishReply();

            if (retval != 0) {
                switch (retval) {
                    case JSDTException.NO_SUCH_CHANNEL:
                        throw new NoSuchChannelException();
                    case JSDTException.NO_SUCH_CLIENT:
                        throw new NoSuchClientException();
                    case JSDTException.NO_SUCH_CONSUMER:
                        throw new NoSuchConsumerException();
                    case JSDTException.NO_SUCH_SESSION:
                        session = null;
                        throw new NoSuchSessionException();
                    case JSDTException.PERMISSION_DENIED:
                        throw new PermissionDeniedException();
                    default:
                        error("ChannelProxy: send: ",
                              "impl.unknown.exception.type", retval);
                }
            }
        } catch (IOException e) {
            if (channel.isReliable()) {
                thread.finishReply();
            } else {
                thread.finishMessage();
            }
            throw new ConnectionException();
        }
    }


    private void
    setChannelThreads() {
        if (ChannelProxy_Debug) {
            debug("ChannelProxy: setChannelThreads.");
        }

        channelThreads = new HttpThread[Channel.MAX_PRIORITIES];

        for (int i = 0; i < Channel.MAX_PRIORITIES; i++) {
            channelThreads[i] = sp.proxyThread;
        }
    }


/**
 * <A NAME="SD_INCREMENTNOTHREADS"></A>
 * <EM>incrementNoThreads</EM> increment 'noThreads' static variable
 * in a synchronized way.
 *
 * @return int 'noThreads' value after its increment.
 */

    private static synchronized int
    incrementNoThreads() {
        return(++noThreads);
    }


/**
 * <A NAME="SD_DECREMENTNOTHREADS"></A>
 * <EM>decrementNoThreads</EM> decrement 'noThreads' static variable
 * in a synchronized way.
 *
 * @return int 'noThreads' value after its decrement.
 */

    static synchronized int
    decrementNoThreads() {
        return(--noThreads);
    }
}
