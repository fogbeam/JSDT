
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

/**
 * JSDT abstract client-side Channel proxy interface.
 *
 * @version     2.3 - 6th November 2017
 * @author      Rich Burridge
 */

public interface
AbstractChannelProxy extends AbstractManageableProxy {

/**
 * <A NAME="SD_INITPROXY"></A>
 * <EM>initProxy</EM> initialise the "client-side" proxy for this Channel.
 *
 * @param name the name of the Channel proxy being constructed.
 * @param session the client-side session the Channel belongs to.
 * @param object the client-side Channel.
 */

    void
    initProxy(String name, SessionImpl session, Object object);


/**
 * <A NAME="SD_GETPROXY"></A>
 * <EM>getProxy</EM> get a handle to the "client-side" proxy for this Channel.
 *
 * @return a handle to the "client-side" proxy for this Channel.
 */

    Object
    getProxy();


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

    void
    addConsumer(Client client, ChannelConsumer consumer)
                throws ConnectionException, InvalidClientException,
                       NoSuchChannelException, NoSuchClientException,
                       NoSuchConsumerException, NoSuchSessionException,
                       PermissionDeniedException, TimedOutException;


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

    void
    removeConsumer(Client client, ChannelConsumer consumer)
                throws ConnectionException, InvalidClientException,
                       NoSuchChannelException, NoSuchClientException,
                       NoSuchConsumerException, NoSuchSessionException,
                       PermissionDeniedException, TimedOutException;


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

    String[]
    listConsumerNames()
                throws ConnectionException, NoSuchChannelException,
                       NoSuchSessionException, TimedOutException;


/**
 * <A NAME="SD_JOIN"></A>
 * <EM>join</EM> is used by a client to join an appropriate channel whose
 * use is defined by the application. This is a prerequesite for sending
 * and receiving data sent to the channel.
 *
 * @param client identifies the client wishing to join this channel.
 * @param authenticate if true, authenticate the client.
 * @param mode the mode which can be one of, READONLY, WRITEONLY or READWRITE.
 *
 * @exception ConnectionException if a connection error occured.
 * @exception InvalidClientException if the Client is invalid is some way (ie.
 * its getName() method returns null).
 * @exception NoSuchChannelException if this Channel doesn't exist.
 * @exception NoSuchClientException if the client given doesn't exist.
 * @exception NoSuchSessionException if this Session doesn't exist.
 * @exception PermissionDeniedException if you do not have permission for
 * this operation.
 * @exception NameInUseException if a Client with this name is already
 * joined to this Manageable object.
 * @exception TimedOutException if no reply was received for this operation
 * in the given timeout period.
 */

    void
    join(Client client, boolean authenticate, int mode)
                throws ConnectionException, InvalidClientException,
                       NoSuchChannelException, NoSuchClientException,
                       NoSuchSessionException, PermissionDeniedException,
                       NameInUseException, TimedOutException;


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

    void
    leave(Client client)
                throws ConnectionException, InvalidClientException,
                       NoSuchChannelException, NoSuchClientException,
                       NoSuchSessionException, TimedOutException;


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

    Data
    receive(Client client, long timeout, boolean ignoreTimeout)
                throws ConnectionException, InvalidClientException,
                       NoSuchClientException, NoSuchSessionException,
                       PermissionDeniedException, TimedOutException;


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

    boolean
    dataAvailable(Client client)
                throws ConnectionException, InvalidClientException,
                       NoSuchClientException, NoSuchSessionException,
                       PermissionDeniedException, TimedOutException;


/**
 * <A NAME="SD_SEND"></A>
 * <EM>send</EM> is used to send data to clients joined to the specified
 * channel. A message containing the data is sent to the session server
 * which then sends messages to the appropriate receiving channel listeners.
 *
 * @param sendingClient the client sending the data.
 * @param recipient who is going to receive this message. This can be all
 * channel receipts (ChannelImpl.D_All), or all other channel recipients
 * (ChannelImpl.D_Others) or just a single client (ChannelImpl.D_Client).
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

    void
    send(Client sendingClient, char recipient, String receivingClientName,
         Data data, boolean uniform)
                throws ConnectionException, InvalidClientException,
                       NoSuchChannelException, NoSuchClientException,
                       NoSuchConsumerException, NoSuchSessionException,
                       PermissionDeniedException, TimedOutException;
}
