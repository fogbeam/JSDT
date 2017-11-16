
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

import java.io.Serializable;
import java.util.EventListener;

/**
 * The Manageable interface.
 *
 * This is the interface definition that is the parent for all manageable
 * resources (ByteArrays, Channels, Sessions and Tokens).
 *
 * @version     2.3 - 26th October 2017
 * @author      Rich Burridge
 * @since       JSDT 1.0
 */

public interface
Manageable extends Serializable {

/**
 * <A NAME="SD_GETNAME"></A>
 * get the name of this object.
 *
 * @return the name of this object.
 */

    String
    getName();


/**
 * <A NAME="SD_GETSESSION"></A>
 * get the name of the Session that this manageable object belongs to. If
 * this method is applied to a Session, a reference to itself is returned.
 *
 * @return the name of the Session that this manageable object belongs to.
 *
 * @since JSDT 1.4
 */

    Session
    getSession();


/**
 * <A NAME="SD_ENABLELISTENEREVENTS"></A>
 * enable certain events for this JSDT listener.
 *
 * @param listener the listener whose event mask is being changed.
 * @param eventMask the mask of events to be enabled.
 *
 * @exception NoSuchSessionException if this Session doesn't exist.
 * @exception NoSuchByteArrayException if this ByteArray doesn't exist.
 * @exception NoSuchChannelException if this Channel doesn't exist.
 * @exception NoSuchTokenException if this Token doesn't exist.
 * @exception NoSuchListenerException if this Listener doesn't exist.
 */

    void
    enableListenerEvents(EventListener listener, int eventMask)
        throws NoSuchSessionException, NoSuchChannelException,
               NoSuchByteArrayException, NoSuchTokenException,
               NoSuchListenerException;


/**
 * <A NAME="SD_DISABLELISTENEREVENTS"></A>
 * disable certain events for this JSDT listener.
 *
 * @param listener the listener whose event mask is being changed.
 * @param eventMask the mask of events to be disabled.
 *
 * @exception NoSuchSessionException if this Session doesn't exist.
 * @exception NoSuchByteArrayException if this ByteArray doesn't exist.
 * @exception NoSuchChannelException if this Channel doesn't exist.
 * @exception NoSuchTokenException if this Token doesn't exist.
 * @exception NoSuchListenerException if this Listener doesn't exist.
 */

    void
    disableListenerEvents(EventListener listener, int eventMask)
        throws NoSuchSessionException, NoSuchChannelException,
               NoSuchByteArrayException, NoSuchTokenException,
               NoSuchListenerException;


/**
 * <A NAME="SD_ENABLEMANAGEREVENTS"></A>
 * enable certain events for the manager (if any) associated with this
 * manageable object.
 *
 * @param manager the manager whose event mask is being changed.
 * @param eventMask the mask of events to be enabled.
 *
 * @exception ConnectionException if a connection error occured.
 * @exception NoSuchSessionException if this Session doesn't exist.
 * @exception NoSuchByteArrayException if this ByteArray doesn't exist.
 * @exception NoSuchChannelException if this Channel doesn't exist.
 * @exception NoSuchTokenException if this Token doesn't exist.
 * @exception NoSuchManagerException if this Manager doesn't exist.
 * @exception TimedOutException if no reply was received for this operation
 * in the given timeout period.
 *
 * @since       JSDT 1.5
 */

    void
    enableManagerEvents(JSDTManager manager, int eventMask)
        throws ConnectionException, NoSuchSessionException,
               NoSuchChannelException, NoSuchByteArrayException,
               NoSuchTokenException, NoSuchManagerException,
               TimedOutException;


/**
 * <A NAME="SD_DISABLEMANAGEREVENTS"></A>
 * disable certain events for the manager (if any) associated with this
 * manageable object.
 *
 * @param manager the manager whose event mask is being changed.
 * @param eventMask the mask of events to be disabled.
 *
 * @exception ConnectionException if a connection error occured.
 * @exception NoSuchSessionException if this Session doesn't exist.
 * @exception NoSuchByteArrayException if this ByteArray doesn't exist.
 * @exception NoSuchChannelException if this Channel doesn't exist.
 * @exception NoSuchTokenException if this Token doesn't exist.
 * @exception NoSuchManagerException if this Manager doesn't exist.
 * @exception TimedOutException if no reply was received for this operation
 * in the given timeout period.
 *
 * @since       JSDT 1.5
 */

    void
    disableManagerEvents(JSDTManager manager, int eventMask)
        throws ConnectionException, NoSuchSessionException,
               NoSuchChannelException, NoSuchByteArrayException,
               NoSuchTokenException, NoSuchManagerException,
               TimedOutException;


/**
 * <A NAME="SD_EXPEL"></A>
 * expel Clients from this Manageable object.
 *
 * <P>This method should only be called by the manager for this object.
 * An indication is delivered to each listener of this Manageable object,
 * for each Client expelled.
 *
 * @param clients the list of Clients to be expelled from this object.
 *
 * @exception ConnectionException if a connection error occured.
 * @exception InvalidClientException if the Client is invalid is some way (ie.
 * its getName() method returns null).
 * @exception NoSuchSessionException if this Session doesn't exist.
 * @exception NoSuchByteArrayException if this ByteArray doesn't exist.
 * @exception NoSuchClientException if this Client doesn't exist.
 * @exception NoSuchChannelException if this Channel doesn't exist.
 * @exception NoSuchTokenException if this Token doesn't exist.
 * @exception PermissionDeniedException if this Client doesn't have
 * permission for this operation.
 * @exception TimedOutException if no reply was received for this operation
 * in the given timeout period.
 */

    void
    expel(Client[] clients)
        throws ConnectionException, InvalidClientException,
               NoSuchSessionException, NoSuchChannelException,
               NoSuchByteArrayException, NoSuchClientException,
               NoSuchTokenException, PermissionDeniedException,
               TimedOutException;


/**
 * <A NAME="SD_INVITE"></A>
 * invite Clients to join this Manageable object.
 *
 * <P>This method should only be called by the manager for this object.
 * An indication is delivered to each listener of this Manageable object,
 * for each Client invited.
 *
 * @param clients the list of Clients to be invited to join this object.
 *
 * @exception ConnectionException if a connection error occured.
 * @exception InvalidClientException if the Client is invalid is some way (ie.
 * its getName() method returns null).
 * @exception NoSuchSessionException if this Session doesn't exist.
 * @exception NoSuchByteArrayException if this ByteArray doesn't exist.
 * @exception NoSuchClientException if this Client doesn't exist.
 * @exception NoSuchChannelException if this Channel doesn't exist.
 * @exception NoSuchTokenException if this Token doesn't exist.
 * @exception PermissionDeniedException if this Client doesn't have
 * permission for this operation.
 * @exception TimedOutException if no reply was received for this operation
 * in the given timeout period.
 */

    void
    invite(Client[] clients)
        throws ConnectionException, InvalidClientException,
               NoSuchSessionException, NoSuchChannelException,
               NoSuchByteArrayException, NoSuchClientException,
               NoSuchTokenException, PermissionDeniedException,
               TimedOutException;


/**
 * <A NAME="SD_DESTROY"></A>
 * destroy this Manageable object.
 *
 * <P>An indication is delivered to each listener of this Manageable object,
 * that it has been destroyed. If this is for a ByteArray, Channel or Token
 * in a managed Session, then the Client is authenticated to determine if
 * it is permitted to do this operation.
 *
 * @param client the Client wishing to destroy this object.
 *
 * @exception ConnectionException if a connection error occured.
 * @exception InvalidClientException if the Client is invalid is some way (ie.
 * its getName() method returns null).
 * @exception NoSuchSessionException if this Session doesn't exist.
 * @exception NoSuchChannelException if this Channel doesn't exist.
 * @exception NoSuchByteArrayException if this ByteArray doesn't exist.
 * @exception NoSuchClientException if this Client doesn't exist.
 * @exception NoSuchTokenException if this Token doesn't exist.
 * @exception PermissionDeniedException if this Client doesn't have
 * permission for this operation.
 * @exception TimedOutException if no reply was received for this operation
 * in the given timeout period.
 */

    void
    destroy(Client client)
        throws ConnectionException, InvalidClientException,
               NoSuchSessionException, NoSuchChannelException,
               NoSuchByteArrayException, NoSuchClientException,
               NoSuchTokenException, PermissionDeniedException,
               TimedOutException;


/**
 * <A NAME="SD_ISMANAGED"></A>
 * test whether this managed object actually has a manager associated with it.
 *
 * @return whether this managed object has a manager associated with it.
 *
 * @exception ConnectionException if a connection error occured.
 * @exception NoSuchSessionException if this Session doesn't exist.
 * @exception NoSuchChannelException if this Channel doesn't exist.
 * @exception NoSuchByteArrayException if this ByteArray doesn't exist.
 * @exception NoSuchTokenException if this Token doesn't exist.
 * @exception TimedOutException if no reply was received for this operation
 * in the given timeout period.
 *
 * @since JSDT 1.3
 */

    boolean
    isManaged()
        throws ConnectionException, NoSuchSessionException,
               NoSuchChannelException, NoSuchByteArrayException,
               NoSuchTokenException, TimedOutException;


/**
 * <A NAME="SD_JOIN"></A>
 * join a Client to this Manageable object.
 *
 * <P>If this is a managed object, then the Client is authenticated to
 * determine if it is permitted to do this operation.
 *
 * @param client the Client wishing to join this Manageable object.
 *
 * @exception ConnectionException if a connection error occured.
 * @exception InvalidClientException if the Client is invalid is some way (ie.
 * its getName() method returns null).
 * @exception NoSuchByteArrayException if this ByteArray doesn't exist.
 * @exception NoSuchChannelException if this Channel doesn't exist.
 * @exception NoSuchClientException if this Client doesn't exist.
 * @exception NoSuchSessionException if this Session doesn't exist.
 * @exception NoSuchTokenException if this Token doesn't exist.
 * @exception PermissionDeniedException if this Client doesn't have
 * permission for this operation.
 * @exception NameInUseException if a Client with this name is already
 * joined to this Manageable object.
 * @exception TimedOutException if no reply was received for this operation
 * in the given timeout period.
 */

    void
    join(Client client)
        throws ConnectionException, InvalidClientException,
               NoSuchByteArrayException, NoSuchChannelException,
               NoSuchClientException, NoSuchSessionException,
               NoSuchTokenException, PermissionDeniedException,
               NameInUseException, TimedOutException;


/**
 * <A NAME="SD_LEAVE"></A>
 * removes a Client from this Manageable object. This Client will no
 * longer be known to this object. Listeners of this object will be
 * sent an indication when this happens.
 *
 * @param client the Client in question.
 *
 * @exception ConnectionException if a connection error occured.
 * @exception InvalidClientException if the Client is invalid is some way (ie.
 * its getName() method returns null).
 * @exception NoSuchByteArrayException if this ByteArray doesn't exist.
 * @exception NoSuchChannelException if this Channel doesn't exist.
 * @exception NoSuchClientException if this Client doesn't exist.
 * @exception NoSuchSessionException if this Session doesn't exist.
 * @exception NoSuchTokenException if this Token doesn't exist.
 * @exception TimedOutException if no reply was received for this operation
 * in the given timeout period.
 */

    void
    leave(Client client)
        throws ConnectionException, InvalidClientException,
               NoSuchByteArrayException, NoSuchChannelException,
               NoSuchClientException, NoSuchSessionException,
               NoSuchTokenException, TimedOutException;


/**
 * <A NAME="SD_LISTCLIENTNAMES"></A>
 * list the names of the Clients who are joined to this Manageable object.
 *
 * @return a sorted array of names of Clients currently joined to this object.
 *
 * @exception ConnectionException if a connection error occured.
 * @exception NoSuchByteArrayException if this ByteArray doesn't exist.
 * @exception NoSuchChannelException if this Channel doesn't exist.
 * @exception NoSuchSessionException if this Session doesn't exist.
 * @exception NoSuchTokenException if this Token doesn't exist.
 * @exception TimedOutException if no reply was received for this operation
 * in the given timeout period.
 */

    String[]
    listClientNames()
        throws ConnectionException, NoSuchSessionException,
               NoSuchChannelException, NoSuchByteArrayException,
               NoSuchTokenException, TimedOutException;
}
