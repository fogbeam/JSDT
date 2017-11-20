
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

package com.sun.media.jsdt.impl;

import com.sun.media.jsdt.*;
import com.sun.media.jsdt.event.ChannelListener;

/**
 * JSDT Channel (implementation) class.
 *
 * @version     2.3 - 20th November 2017
 * @author      Rich Burridge
 */

public class
ChannelImpl extends ManageableImpl implements Channel {

    // To signify a manageable object of type Channel.
    public static final char M_Channel = '\u00F1';

    // Send Data to all consumers (including self) of the Channel.
    public static final char D_All    = '\u00F2';

    // Send Data to all other consumers of the Channel.
    public static final char D_Others = '\u00F3';

    // Send Data to a specific consumer of the Channel.
    public static final char D_Client = '\u00F4';

    /** The session that this channel belongs to.
     *
     *  @serial
     */
    protected final SessionImpl session;

    /** Handle to client-side proxy.
     *
     *  @serial
     */
    public AbstractChannelProxy po;

    /** Handle to server-side object.
     *
     *  @serial
     */
    public AbstractChannelServer so;

    /** Indicates if data sent over this channel will be reliably delivered.
     *
     *  @serial
     */
    protected boolean reliable = false;

    /** Indicates whether data sent over this channel will be ordered.
     *
     *  @serial
     */
    protected boolean ordered = false;


/**
 * <A NAME="SD_CHANNEL"></A>
 * <EM>Channel</EM> the constructor for the Channel class.
 *
 * @param isServer set if are we creating object on the server-side.
 * @param name the name of the channel being constructed.
 * @param session the session this channel belongs to.
 * @param reliable indicates whether the channel is reliable.
 * @param ordered indicates whether the channel is ordered.
 */

    public
    ChannelImpl(boolean isServer, String name, SessionImpl session,
                boolean reliable, boolean ordered) {
        String className;

        if (ChannelImpl_Debug) {
            debug("ChannelImpl: constructor:" +
                  " server? "   + isServer +
                  " name: "     + name +
                  " session: "  + session +
                  " reliable: " + reliable +
                  " ordered: "  + ordered);
        }

        this.name     = name;
        this.session  = session;
        this.reliable = reliable;
        this.ordered  = ordered;

        try {
            if (!isServer) {
                className = "com.sun.media.jsdt." + session.getConnectionType() +
                            ".ChannelProxy";
                po = (AbstractChannelProxy)
                                Util.getClassForName(className).newInstance();
                po.initProxy(name, session, this);
            } else {
                className = "com.sun.media.jsdt." + session.getConnectionType() +
                            ".ChannelServer";
                so = (AbstractChannelServer)
                                Util.getClassForName(className).newInstance();
                so.initServer(name, session, this);
            }
        } catch (Exception e) {
            error("ChannelImpl: constructor: ", e);
        }

        objectType = M_Channel;
        mpo = po;
    }


/**
 * <A NAME="SD_ADDCHANNELLISTENER"></A>
 * add the specified Channel listener to receive Channel events for this
 * Channel.
 *
 * @param listener the Channel listener.
 *
 * @exception ConnectionException if a connection error occured.
 * @exception NoSuchChannelException if this Channel doesn't exist.
 * @exception NoSuchSessionException if this Session doesn't exist.
 * @exception TimedOutException if no reply was received for this operation
 * in the given timeout period.
 */

    public void
    addChannelListener(ChannelListener listener)
                throws ConnectionException, NoSuchChannelException,
                       NoSuchSessionException, TimedOutException {
        if (ChannelImpl_Debug) {
            debug("ChannelImpl: addChannelListener:" +
                  " listener: " + listener);
        }

        try {
            mpo.addListener(listener, objectType);
        } catch (NoSuchChannelException | NoSuchSessionException |
                 ConnectionException | TimedOutException nse) {
            throw nse;
        } catch (JSDTException e) {
            error("ChannelImpl: addChannelListener: ", e);
        }
    }


/**
 * <A NAME="SD_REMOVECHANNELLISTENER"></A>
 * removes the specified Channel listener so that it no longer receives
 * Channel events for this Channel.
 *
 * @param listener the Channel listener.
 *
 * @exception ConnectionException if a connection error occured.
 * @exception NoSuchChannelException if this Channel doesn't exist.
 * @exception NoSuchListenerException if this ChannelListener doesn't exist.
 * @exception NoSuchSessionException if this Session doesn't exist.
 * @exception TimedOutException if no reply was received for this operation
 * in the given timeout period.
 */

    public void
    removeChannelListener(ChannelListener listener)
                throws ConnectionException, NoSuchChannelException,
                       NoSuchListenerException, NoSuchSessionException,
                       TimedOutException {
        if (ChannelImpl_Debug) {
            debug("ChannelImpl: removeChannelListener:" +
                  " listener: " + listener);
        }

        try {
            mpo.removeListener(listener, objectType);
        } catch (NoSuchChannelException | NoSuchListenerException |
                 NoSuchSessionException | ConnectionException |
                 TimedOutException nse) {
            throw nse;
        } catch (JSDTException e) {
            error("ChannelImpl: removeChannelListener: ", e);
        }
    }


/**
 * <A NAME="SD_ADDCONSUMER"></A>
 * add a new ChannelConsumer for this Client. The Client's ChannelConsumers
 * gets informed when there is Data available on a Channel.
 *
 * @param client the Client to associate this ChannelConsumer with.
 * @param consumer the new ChannelConsumer for this client.
 *
 * @exception ConnectionException if a connection error occured.
 * @exception InvalidClientException if the Client is invalid is some way (ie.
 * its getName() method returns null).
 * @exception NoSuchChannelException if this Channel doesn't exist.
 * @exception NoSuchClientException if this Client doesn't exist.
 * @exception NoSuchConsumerException if this ChannelConsumer doesn't exist.
 * @exception NoSuchSessionException if this Session doesn't exist.
 * @exception PermissionDeniedException if this Client doesn't have
 * permission for this operation. This would occur if the Client had joined
 * the Channel in write-only mode.
 * @exception TimedOutException if no reply was received for this operation
 * in the given timeout period.
 */

    public void
    addConsumer(Client client, ChannelConsumer consumer)
                throws ConnectionException, InvalidClientException,
                       NoSuchChannelException, NoSuchClientException,
                       NoSuchConsumerException, NoSuchSessionException,
                       PermissionDeniedException, TimedOutException {
        if (ChannelImpl_Debug) {
            debug("ChannelImpl: addConsumer:" +
                  " client: "    + client +
                  " consumer: " + consumer);
        }

        po.addConsumer(client, consumer);
    }


/**
 * <A NAME="SD_REMOVECONSUMER"></A>
 * remove a ChannelConsumer from this Client for this Channel. The
 * ChannelConsumer will no longer get informed when there is Data available
 * on this channel.
 *
 * @param client the Client to associate this ChannelConsumer with.
 * @param consumer the ChannelConsumer to remove from this Client.
 *
 * @exception ConnectionException if a connection error occured.
 * @exception InvalidClientException if the Client is invalid is some way (ie.
 * its getName() method returns null).
 * @exception NoSuchChannelException if this Channel doesn't exist.
 * @exception NoSuchClientException if this Client doesn't exist.
 * @exception NoSuchConsumerException if this ChannelConsumer doesn't exist.
 * @exception NoSuchSessionException if this Session doesn't exist.
 * @exception PermissionDeniedException if this Client doesn't have
 * permission for this operation.
 * @exception TimedOutException if no reply was received for this operation
 * in the given timeout period.
 */

    public void
    removeConsumer(Client client, ChannelConsumer consumer)
                throws ConnectionException, InvalidClientException,
                       NoSuchChannelException, NoSuchClientException,
                       NoSuchConsumerException, NoSuchSessionException,
                       PermissionDeniedException, TimedOutException {
        if (ChannelImpl_Debug) {
            debug("ChannelImpl: removeConsumer:" +
                  " client: "   + client  +
                  " consumer: " + consumer);
        }

        po.removeConsumer(client, consumer);
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
        if (ChannelImpl_Debug) {
            debug("ChannelImpl: listConsumerNames.");
        }

        return(po.listConsumerNames());
    }


/**
 * <A NAME="SD_ISRELIABLE"></A>
 * <EM>isReliable</EM> test whether this is a reliable channel.
 *
 * @return true if this is a reliable channel; false if not.
 */

    public boolean
    isReliable() {
        if (ChannelImpl_Debug) {
            debug("ChannelImpl: isReliable.");
        }

        return(reliable);
    }


/**
 * <A NAME="SD_ISORDERED"></A>
 * <EM>isOrdered</EM> returns whether this is an ordered channel.
 *
 * @return true if this is an ordered channel; false if not.
 */

    public boolean
    isOrdered() {
        if (ChannelImpl_Debug) {
            debug("ChannelImpl: isOrdered.");
        }

        return(ordered);
    }


/**
 * <A NAME="SD_JOIN"></A>
 * <EM>join</EM> is used by a client to join an appropriate channel whose
 * use is defined by the application. This is a prerequesite for sending
 * and receiving data sent to the channel.
 *
 * @param client identifies the client wishing to join this channel.
 *
 * @exception ConnectionException if a connection error occured.
 * @exception InvalidClientException if the Client is invalid is some way (ie.
 * its getName() method returns null).
 * @exception NoSuchChannelException if the channel given does not exist,
 * @exception NoSuchClientException if the client given does not exist.
 * @exception NoSuchSessionException if this Session doesn't exist.
 * @exception PermissionDeniedException if you do not have permission for
 * this operation.
 * @exception NameInUseException if a Client with this name is already
 * joined to this Manageable object.
 * @exception TimedOutException if no reply was received for this operation
 * in the given timeout period.
 */

    public void
    join(Client client)
                throws ConnectionException, InvalidClientException,
                       NoSuchChannelException, NoSuchClientException,
                       NoSuchSessionException, PermissionDeniedException,
                       NameInUseException, TimedOutException {
        if (ChannelImpl_Debug) {
            debug("ChannelImpl: join:" +
                  " client: " + client);
        }

        join(client, READWRITE);
    }


/**
 * <A NAME="SD_JOINWITHMODE"></A>
 * <EM>join</EM> is used by a client to join an appropriate channel whose
 * use is defined by the application. This is a prerequesite for sending
 * and receiving data sent to the channel.
 *
 * @param client identifies the client wishing to join this channel.
 * @param mode the mode which can be one of, READONLY, WRITEONLY or READWRITE.
 *
 * @exception ConnectionException if a connection error occured.
 * @exception InvalidClientException if the Client is invalid is some way (ie.
 * its getName() method returns null).
 * @exception NoSuchChannelException if the channel given does not exist,
 * @exception NoSuchClientException if the client given does not exist.
 * @exception NoSuchSessionException if this Session doesn't exist.
 * @exception PermissionDeniedException if you do not have permission for
 * this operation.
 * @exception NameInUseException if a Client with this name is already
 * joined to this Manageable object.
 * @exception TimedOutException if no reply was received for this operation
 * in the given timeout period.
 */

    public void
    join(Client client, int mode)
                throws ConnectionException, InvalidClientException,
                       NoSuchChannelException, NoSuchClientException,
                       NoSuchSessionException, PermissionDeniedException,
                       NameInUseException, TimedOutException {
        if (ChannelImpl_Debug) {
            debug("ChannelImpl: join:" +
                  " client: " + client +
                  " mode: "   + mode);
        }

        po.join(client, true, mode);
    }


/**
 * <A NAME="SD_LEAVE"></A>
 * <EM>leave</EM> removes a Client from this Channel. This Client will no
 * longer be known to this Channel. Listeners of this Channel will be
 * sent an indication when this happens.
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
        if (ChannelImpl_Debug) {
            debug("ChannelImpl: leave:" +
                  " client: " + client);
        }

        po.leave(client);
    }


/**
 * <A NAME="SD_RECEIVE"></A>
 * is used to receive Data from other Clients joined to this Channel.
 *
 * <P>It will block if there is no Data to read. The <CODE>dataAvailable</CODE>
 * method can be used to check if there is Data that can be received on
 * this channel.
 *
 * @param client identifies the client wishing to receive data from this
 * channel. This client must already be successfully joined to this channel.
 *
 * @return the next Data object available on this channel.
 *
 * @exception ConnectionException if a connection error occured.
 * @exception InvalidClientException if the Client is invalid is some way (ie.
 * its getName() method returns null).
 * @exception NoSuchClientException if the client given doesn't exist.
 * @exception NoSuchSessionException if this Session doesn't exist.
 * @exception PermissionDeniedException if this Client doesn't have
 * permission for this operation.
 * @exception TimedOutException if no reply was received for this operation
 * in the given timeout period.
 */

    public Data
    receive(Client client)
                throws ConnectionException, InvalidClientException,
                       NoSuchClientException, NoSuchSessionException,
                       PermissionDeniedException, TimedOutException {
        if (ChannelImpl_Debug) {
            debug("ChannelImpl: receive:" +
                  " client: " + client);
        }

        return(po.receive(client, 0, true));
    }


/**
 * <A NAME="SD_RECEIVEANDTIMEOUT"></A>
 * is used to receive Data from other Clients joined to this Channel.
 *
 * <P>If Data is immediately available then it will return with it, else it
 * will wait until the timeout period, specified by the <code>timeout</code>
 * argument in milliseconds, has elapsed. If no Data is available at this
 * time, it will return null. Note that if Data becomes available during the
 * timeout period, this method will be woken up and that Data is immediately
 * returned.
 *
 * @param client identifies the client wishing to receive data from this
 * channel. This client must already be successfully joined to this channel.
 * @param timeout the maximum time to wait in milliseconds.
 *
 * @return the next Data object available on this channel, or null if there
 * is currently no Data available and the timeout period has expired.
 *
 * @exception ConnectionException if a connection error occured.
 * @exception InvalidClientException if the Client is invalid is some way (ie.
 * its getName() method returns null).
 * @exception NoSuchClientException if the client given doesn't exist.
 * @exception NoSuchSessionException if this Session doesn't exist.
 * @exception PermissionDeniedException if this Client doesn't have
 * permission for this operation.
 * @exception TimedOutException if no reply was received for this operation
 * in the given timeout period.
 */

    public Data
    receive(Client client, long timeout)
                throws ConnectionException, InvalidClientException,
                       NoSuchClientException, NoSuchSessionException,
                       PermissionDeniedException, TimedOutException {
        if (ChannelImpl_Debug) {
            debug("ChannelImpl: receive:" +
                  " client: "  + client +
                  " timeout: " + timeout);
        }

        return(po.receive(client, timeout, false));
    }


/**
 * <A NAME="SD_DATAAVAILABLE"></A>
 * tests whether there is Data available to read on this Channel.
 *
 * @param client identifies the client wishing to check if there is data
 * available to receive on this channel. This client must already be
 * successfully joined to this channel.
 *
 * @return true if Data is available; false if not.
 *
 * @exception ConnectionException if a connection error occured.
 * @exception InvalidClientException if the Client is invalid is some way (ie.
 * its getName() method returns null).
 * @exception NoSuchClientException if the client given doesn't exist.
 * @exception NoSuchSessionException if this Session doesn't exist.
 * @exception PermissionDeniedException if this Client doesn't have
 * permission for this operation.
 * @exception TimedOutException if no reply was received for this operation
 * in the given timeout period.
 */

    public boolean
    dataAvailable(Client client)
                throws ConnectionException, InvalidClientException,
                       NoSuchClientException, NoSuchSessionException,
                       PermissionDeniedException, TimedOutException {
        if (ChannelImpl_Debug) {
            debug("ChannelImpl: dataAvailable:" +
                  " client: " + client);
        }

        return(po.dataAvailable(client));
    }


/**
 * <A NAME="SD_SENDTOALL"></A>
 * <EM>sendToAll</EM> is used to send data to all clients joined to this
 * channel. If the sender is listening on this channel, then it will
 * receive the data.
 *
 * Data from each sender sent at the same priority on the same channel
 * arrives at a given receiver in the same order it was sent but may have
 * other sender data interleaved differently.
 *
 * @param sendingClient the client sending the data.
 * @param data the data being sent over this channel.
 *
 * @exception ConnectionException if a connection error occured.
 * @exception InvalidClientException if the Client is invalid is some way (ie.
 * its getName() method returns null).
 * @exception NoSuchChannelException if the channel given does not exist.
 * @exception NoSuchClientException if the client given does not exist.
 * @exception NoSuchSessionException if this Session doesn't exist.
 * @exception PermissionDeniedException if you do not have permission for
 * this operation.
 * @exception TimedOutException if no reply was received for this operation
 * in the given timeout period.
 */

    public void
    sendToAll(Client sendingClient, Data data)
                throws ConnectionException, InvalidClientException,
                       NoSuchChannelException, NoSuchClientException,
                       NoSuchSessionException, PermissionDeniedException,
                       TimedOutException {
        if (ChannelImpl_Debug) {
            debug("ChannelImpl: sendToAll:" +
                  " sending client: "       + sendingClient +
                  " receiving clients: all" +
                  " data:                 " + data);
        }

        try {
            po.send(sendingClient, D_All, null, data, false);
        } catch (NoSuchConsumerException nsce) {
        }
    }


/**
 * <A NAME="SD_SENDTOOTHERS"></A>
 * <EM>sendToOthers</EM> is used to send data to other clients joined to
 * this channel. The sender (irrespective of whether it's listening on this
 * channel) will not receive the data.
 *
 * Data from each sender sent at the same priority on the same channel
 * arrives at a given receiver in the same order it was sent but may have
 * other sender data interleaved differently.
 *
 * @param sendingClient the client sending the data.
 * @param data the data being sent over this channel.
 *
 * @exception ConnectionException if a connection error occured.
 * @exception InvalidClientException if the Client is invalid is some way (ie.
 * its getName() method returns null).
 * @exception NoSuchChannelException if the channel given does not exist.
 * @exception NoSuchClientException if the client given does not exist.
 * @exception NoSuchSessionException if this Session doesn't exist.
 * @exception PermissionDeniedException if you do not have permission for
 * this operation.
 * @exception TimedOutException if no reply was received for this operation
 * in the given timeout period.
 */

    public void
    sendToOthers(Client sendingClient, Data data)
                throws ConnectionException, InvalidClientException,
                       NoSuchChannelException, NoSuchClientException,
                       NoSuchSessionException, PermissionDeniedException,
                       TimedOutException {
        if (ChannelImpl_Debug) {
            debug("ChannelImpl: sendToOthers:" +
                  " sending client: "       + sendingClient +
                  " receiving clients: all" +
                  " data:                 " + data);
        }

        try {
            po.send(sendingClient, D_Others, null, data, false);
        } catch (NoSuchConsumerException nsce) {
        }
    }


/**
 * <A NAME="SD_SENDTOCLIENT"></A>
 * <EM>sendToClient</EM> is used to send data to a single client joined to
 * this channel.
 *
 * @param sendingClient the client sending the data.
 * @param receivingClientName the name of the client receiving the data.
 * @param data the data being sent over this channel.
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
    sendToClient(Client sendingClient, String receivingClientName, Data data)
                throws ConnectionException, InvalidClientException,
                       NoSuchChannelException, NoSuchClientException,
                       NoSuchConsumerException, NoSuchSessionException,
                       PermissionDeniedException, TimedOutException {
        if (ChannelImpl_Debug) {
            debug("ChannelImpl: sendToClient:" +
                  " sending client: "        + sendingClient +
                  " receiving client name: " + receivingClientName +
                  " data: "                  + data);
        }

        po.send(sendingClient, D_Client, receivingClientName, data, false);
    }
}
