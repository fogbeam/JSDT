
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

package com.sun.media.jsdt;

import com.sun.media.jsdt.event.ChannelListener;

/**
 * JSDT Channel interface.
 *
 * @version     2.3 - 25th September 2004
 * @author      Rich Burridge
 * @since       JSDT 1.0
 */

public interface
Channel extends Manageable {

    /** Channel Data top priority value. */
    int TOP_PRIORITY    = 0;

    /** Channel Data high priority value. */
    int HIGH_PRIORITY   = 1;

    /** Channel Data medium priority value. */
    int MEDIUM_PRIORITY = 2;

    /** Channel Data top priority value. */
    int LOW_PRIORITY    = 3;

    /** The maximum number of Data priorities. */
    int MAX_PRIORITIES = 4;

    /** The Channel read-only value. */
    int READONLY        = 0;

    /** The Channel write-only value. */
    int WRITEONLY       = 1;

    /** The Channel read/write value. */
    int READWRITE       = 2;


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

    void
    addChannelListener(ChannelListener listener)
        throws ConnectionException, NoSuchChannelException,
               NoSuchSessionException, TimedOutException;


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

    void
    removeChannelListener(ChannelListener listener)
        throws ConnectionException, NoSuchChannelException,
               NoSuchListenerException, NoSuchSessionException,
               TimedOutException;


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

    void
    addConsumer(Client client, ChannelConsumer consumer)
        throws ConnectionException, InvalidClientException,
               NoSuchChannelException, NoSuchClientException,
               NoSuchConsumerException, NoSuchSessionException,
               PermissionDeniedException, TimedOutException;


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
 * This array will be of zero length if there are no Clients currently
 * consuming this Channel.
 *
 * @exception ConnectionException if a connection error occured.
 * @exception NoSuchChannelException if this Channel doesn't exist.
 * @exception NoSuchSessionException if this Session doesn't exist.
 * @exception TimedOutException if no reply was received for this operation
 * in the given timeout period.
 *
 * @since        JSDT 1.3
 */

    String[]
    listConsumerNames()
        throws ConnectionException, NoSuchChannelException,
               NoSuchSessionException, TimedOutException;


/**
 * <A NAME="SD_ISRELIABLE"></A>
 * test whether this is a reliable channel.
 *
 * @return true if this is a reliable channel; false if not.
 */

    boolean
    isReliable();


/**
 * <A NAME="SD_ISORDERED"></A>
 * returns whether this is an ordered channel.
 *
 * @return true if this is an ordered channel; false if not.
 */

    boolean
    isOrdered();


/**
 * <A NAME="SD_JOINWITHMODE"></A>
 * join a Client to a Channel in the given mode. This is a prerequesite for
 * sending and receiving Data sent over the Channel.
 *
 * <P>If this is a managed Channel, then the Client is authenticated to
 * determine if it is permitted to do this operation.
 *
 * @param client the Client wishing to join this Channel.
 * @param mode the mode which can be one of, READONLY, WRITEONLY or READWRITE.
 *
 * @exception ConnectionException if a connection error occured.
 * @exception InvalidClientException if the Client is invalid is some way (ie.
 * its getName() method returns null).
 * @exception NoSuchChannelException if this Channel doesn't exist.
 * @exception NoSuchClientException if this Client doesn't exist.
 * @exception NoSuchSessionException if this Session doesn't exist.
 * @exception PermissionDeniedException if this Client doesn't have
 * permission for this operation.
 * @exception NameInUseException if a Client with this name is already
 * joined to this Manageable object.
 * @exception TimedOutException if no reply was received for this operation
 * in the given timeout period.
 */

    void
    join(Client client, int mode)
        throws ConnectionException, InvalidClientException,
               NoSuchChannelException, NoSuchClientException,
               NoSuchSessionException, PermissionDeniedException,
               NameInUseException, TimedOutException;


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

    Data
    receive(Client client)
        throws ConnectionException, InvalidClientException,
               NoSuchClientException, NoSuchSessionException,
               PermissionDeniedException, TimedOutException;


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
 * @return the next Data object available on this channel, or null if the
 * timeout period has expired, and there is currently no Data available.
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

    Data
    receive(Client client, long timeout)
        throws ConnectionException, InvalidClientException,
               NoSuchClientException, NoSuchSessionException,
               PermissionDeniedException, TimedOutException;


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

    boolean
    dataAvailable(Client client)
        throws ConnectionException, InvalidClientException,
               NoSuchClientException, NoSuchSessionException,
               PermissionDeniedException, TimedOutException;


/**
 * <A NAME="SD_SENDTOALL"></A>
 * is used to send Data to all Clients consuming this Channel. If the
 * sender is a consumer of this Channel, then it too will receive the Data.
 *
 * <P>Data from each sender sent at the same priority on the same Channel
 * arrives at a given receiver in the same order it was sent but may have
 * other sender Data interleaved differently.
 *
 * @param sendingClient the Client sending the Data.
 * @param data the Data being sent over this Channel.
 *
 * @exception ConnectionException if a connection error occured.
 * @exception InvalidClientException if the Client is invalid is some way (ie.
 * its getName() method returns null).
 * @exception NoSuchChannelException if this Channel doesn't exist.
 * @exception NoSuchClientException if this Client doesn't exist.
 * @exception NoSuchSessionException if this Session doesn't exist.
 * @exception PermissionDeniedException if this Client doesn't have
 * permission for this operation.
 * @exception TimedOutException if no reply was received for this operation
 * in the given timeout period.
 */

    void
    sendToAll(Client sendingClient, Data data)
        throws ConnectionException, InvalidClientException,
               NoSuchChannelException, NoSuchClientException,
               NoSuchSessionException, PermissionDeniedException,
               TimedOutException;


/**
 * <A NAME="SD_SENDTOOTHERS"></A>
 * is used to send Data to other Clients consuming this Channel. The
 * sender (irrespective of whether it's a consumer of this channel) will
 * not receive the Data.
 *
 * <P>Data from each sender sent at the same priority on the same Channel
 * arrives at a given receiver in the same order it was sent but may have
 * other sender Data interleaved differently.
 *
 * @param sendingClient the Client sending the Data.
 * @param data the Data being sent over this Channel.
 *
 * @exception ConnectionException if a connection error occured.
 * @exception InvalidClientException if the Client is invalid is some way (ie.
 * its getName() method returns null).
 * @exception NoSuchChannelException if this Channel doesn't exist.
 * @exception NoSuchClientException if this Client doesn't exist.
 * @exception NoSuchSessionException if this Session doesn't exist.
 * @exception PermissionDeniedException if this Client doesn't have
 * permission for this operation.
 * @exception TimedOutException if no reply was received for this operation
 * in the given timeout period.
 */

    void
    sendToOthers(Client sendingClient, Data data)
        throws ConnectionException, InvalidClientException,
               NoSuchChannelException, NoSuchClientException,
               NoSuchSessionException, PermissionDeniedException,
               TimedOutException;


/**
 * <A NAME="SD_SENDTOCLIENT"></A>
 * is used to send Data to a single Client consuming this Channel.
 *
 * @param sendingClient the Client sending the Data.
 * @param receivingClientName the name of the Client receiving the Data.
 * @param data the Data being sent over this Channel.
 *
 * @exception ConnectionException if a connection error occured.
 * @exception InvalidClientException if the Client is invalid is some way (ie.
 * its getName() method returns null).
 * @exception NoSuchChannelException if this Channel doesn't exist.
 * @exception NoSuchClientException if this Client doesn't exist.
 * @exception NoSuchConsumerException if this Client doesn't have a
 * ChannelConsumer associated with it.
 * @exception NoSuchSessionException if this Session doesn't exist.
 * @exception PermissionDeniedException if this Client doesn't have
 * permission for this operation.
 * @exception TimedOutException if no reply was received for this operation
 * in the given timeout period.
 */

    void
    sendToClient(Client sendingClient, String receivingClientName, Data data)
        throws ConnectionException, InvalidClientException,
               NoSuchChannelException, NoSuchClientException,
               NoSuchConsumerException, NoSuchSessionException,
               PermissionDeniedException, TimedOutException;
}
