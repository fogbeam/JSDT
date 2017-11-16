
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
import java.util.EventListener;

/**
 * JSDT Manageable (implementation) class.
 *
 * @version     2.3 - 6th November 2017
 * @author      Rich Burridge
 */

public abstract class
ManageableImpl extends JSDTObject implements Manageable {

    /** The name of this object.
     *
     *  @serial
     */
    protected String name = null;

    /** The type of this object (ByteArray, Channel, Session or Token).
     *
     *  @serial
     */
    protected char objectType;

    /** Handle to manageable client-side proxy.
     *
     *  @serial
     */
    public AbstractManageableProxy mpo;


/**
 * <A NAME="SD_GETNAME"></A>
 * <EM>getName</EM> get the name of this object.
 *
 * @return the name of this object.
 */

    public String
    getName() {
        if (ManageableImpl_Debug) {
            debug("ManageableImpl: getName.");
        }

        return(name);
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
        if (ManageableImpl_Debug) {
            debug("ManageableImpl: getSession.");
        }

        return(mpo.getSession());
    }


/**
 * <A NAME="SD_ADDLISTENER"></A>
 * <EM>addListener</EM> add a listener to this manageable object.
 *
 * @param listener the listener for this manageable object.
 *
 * @exception ConnectionException if a connection error occured.
 * @exception NoSuchByteArrayException if this ByteArray doesn't exist
 * @exception NoSuchChannelException if this Channel doesn't exist.
 * @exception NoSuchSessionException if this Session doesn't exist.
 * @exception NoSuchTokenException if this Token doesn't exist.
 * @exception TimedOutException if no reply was received for this operation
 * in the given timeout period.
 */

    public void
    addListener(EventListener listener)
                throws ConnectionException, NoSuchByteArrayException,
                        NoSuchChannelException, NoSuchSessionException,
                        NoSuchTokenException, TimedOutException {
        if (ManageableImpl_Debug) {
            debug("ManageableImpl: addListener:" +
                  " listener: " + listener);
        }

        mpo.addListener(listener, objectType);
    }


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

    public void
    enableListenerEvents(EventListener listener, int eventMask)
                throws NoSuchSessionException, NoSuchChannelException,
                       NoSuchByteArrayException, NoSuchTokenException,
                       NoSuchListenerException {
        if (ManageableImpl_Debug) {
            debug("ManageableImpl: enableListenerEvents:" +
                  " listener: "   + listener +
                  " event mask: " + eventMask);
        }

        mpo.changeListenerMask(listener, eventMask, false);
    }


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

    public void
    disableListenerEvents(EventListener listener, int eventMask)
                throws NoSuchSessionException, NoSuchChannelException,
                       NoSuchByteArrayException, NoSuchTokenException,
                       NoSuchListenerException {
        if (ManageableImpl_Debug) {
            debug("ManageableImpl: disableListenerEvents:" +
                  " listener: "   + listener +
                  " event mask: " + eventMask);
        }

        mpo.changeListenerMask(listener, eventMask, true);
    }


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
 */

    public void
    enableManagerEvents(JSDTManager manager, int eventMask)
                throws ConnectionException, NoSuchSessionException,
                       NoSuchChannelException, NoSuchByteArrayException,
                       NoSuchTokenException, NoSuchManagerException,
                       TimedOutException {
        if (ManageableImpl_Debug) {
            debug("ManageableImpl: enableManagerEvents:" +
                  " manager: "    + manager +
                  " event mask: " + eventMask);
        }

        mpo.changeManagerMask(manager, eventMask, false, objectType);
    }


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
 */

    public void
    disableManagerEvents(JSDTManager manager, int eventMask)
                throws ConnectionException, NoSuchSessionException,
                       NoSuchChannelException, NoSuchByteArrayException,
                       NoSuchTokenException, NoSuchManagerException,
                       TimedOutException {
        if (ManageableImpl_Debug) {
            debug("ManageableImpl: disableManagerEvents:" +
                  " manager: "    + manager +
                  " event mask: " + eventMask);
        }

        mpo.changeManagerMask(manager, eventMask, true, objectType);
    }


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

    public void
    expel(Client[] clients)
                throws ConnectionException, InvalidClientException,
                       NoSuchSessionException, NoSuchChannelException,
                       NoSuchByteArrayException, NoSuchClientException,
                       NoSuchTokenException, PermissionDeniedException,
                       TimedOutException {
        if (ManageableImpl_Debug) {
            debug("ManageableImpl: expel:");
            for (int i = 0; i < clients.length ; i++) {
                System.err.println("clients[" + i + "]: " + clients[i]);
            }
        }

        mpo.expel(clients, objectType);
    }


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

    public void
    invite(Client[] clients)
                throws ConnectionException, InvalidClientException,
                       NoSuchSessionException, NoSuchChannelException,
                       NoSuchByteArrayException, NoSuchClientException,
                       NoSuchTokenException, PermissionDeniedException,
                       TimedOutException {
        if (ManageableImpl_Debug) {
            debug("ManageableImpl: invite:");
            for (int i = 0; i < clients.length ; i++) {
                System.err.println("clients[" + i + "]: " + clients[i]);
            }
        }

        mpo.invite(clients, objectType);
    }


/**
 * <A NAME="SD_DESTROY"></A>
 * destroy this Manageable object.
 *
 * <P>An indication is delivered to each listener of this Manageable object,
 * for each Client invited. If this is for a ByteArray, Channel or Token
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

    public void
    destroy(Client client)
                throws ConnectionException, InvalidClientException,
                       NoSuchSessionException, NoSuchChannelException,
                       NoSuchByteArrayException, NoSuchClientException,
                       NoSuchTokenException, PermissionDeniedException,
                       TimedOutException {
        if (ManageableImpl_Debug) {
            debug("ManageableImpl: destroy:" +
                  " client: " + client);
        }

        mpo.destroy(client, objectType);
    }


/**
 * <A NAME="SD_ISMANAGED"></A>
 * test whether this managed object actually has a manager associated with it.
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
    isManaged()
                throws ConnectionException, NoSuchSessionException,
                       NoSuchChannelException, NoSuchByteArrayException,
                       NoSuchTokenException, TimedOutException {
        if (ManageableImpl_Debug) {
            debug("ManageableImpl: isManaged.");
        }

        return(mpo.isManaged(objectType, name));
    }


/**
 * <A NAME="SD_JOIN"></A>
 * <EM>join</EM> joins a client to this manageable object. This client will
 * now be known to this object. Listeners of this object will be sent an
 * indication, when this happens.
 *
 * <P>If this is a managed object, then the Client is authenticated to
 * determine if it is permitted to do this operation.
 *
 * @param client the client in question.
 *
 * @exception ConnectionException if a connection error occured.
 * @exception InvalidClientException if the Client is invalid is some way (ie.
 * its getName() method returns null).
 * @exception NoSuchByteArrayException if this byte array doesn't exist.
 * @exception NoSuchChannelException if this channel doesn't exist.
 * @exception NoSuchClientException if this client doesn't exist.
 * @exception NoSuchSessionException if this session doesn't exist.
 * @exception NoSuchTokenException if this token doesn't exist.
 * @exception PermissionDeniedException if this client doesn't have
 * permission to do this operation.
 * @exception NameInUseException if a Client with this name is already
 * joined to this Manageable object.
 * @exception TimedOutException if no reply was received for this operation
 * in the given timeout period.
 */

    public void
    join(Client client)
                throws ConnectionException, InvalidClientException,
                       NoSuchByteArrayException, NoSuchChannelException,
                       NoSuchClientException, NoSuchSessionException,
                       NoSuchTokenException, PermissionDeniedException,
                       NameInUseException, TimedOutException {
        if (ManageableImpl_Debug) {
            debug("ManageableImpl: join:" +
                  " client: " + client);
        }

        mpo.join(client, true, objectType);
    }


/**
 * <A NAME="SD_LEAVE"></A>
 * <EM>leave</EM> removes a client from this manageable object. This client
 * will no longer be known to this object. Observers of this object will be
 * sent an indication when this happens.
 *
 * @param client the client in question.
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
    leave(Client client)
                throws ConnectionException, InvalidClientException,
                       NoSuchByteArrayException, NoSuchChannelException,
                       NoSuchClientException, NoSuchSessionException,
                       NoSuchTokenException, TimedOutException {
        if (ManageableImpl_Debug) {
            debug("ManageableImpl: leave:" +
                  " client: " + client);
        }

        mpo.leave(client, objectType);
    }


/**
 * <A NAME="SD_LISTCLIENTNAMES"></A>
 * <EM>listClientNames</EM> list the names of the clients who are joined
 * to this manageable object.
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

    public String[]
    listClientNames()
                throws ConnectionException, NoSuchSessionException,
                        NoSuchChannelException, NoSuchByteArrayException,
                        NoSuchTokenException, TimedOutException {
        if (ManageableImpl_Debug) {
            debug("ManageableImpl: listClientNames.");
        }

        return(mpo.listClientNames(objectType));
    }
}
