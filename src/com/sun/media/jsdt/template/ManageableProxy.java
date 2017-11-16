
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

package com.sun.media.jsdt.template;

import com.sun.media.jsdt.*;
import com.sun.media.jsdt.impl.*;
import java.util.EventListener;

/**
 * JSDT manageable proxy class.
 *
 * @version     2.3 - 6th November 2017
 * @author      Rich Burridge
 */

class
ManageableProxy extends JSDTObject
                implements AbstractManageableProxy, templateDebugFlags {

    // The client-side session associated with this manageable proxy.
    SessionImpl session;

/**
 * <A NAME="SD_INITPROXY"></A>
 * <EM>initProxy</EM> initialise the client-side proxy for this manageable
 * object.
 *
 * @param name the name of the manageable proxy being constructed.
 * @param session the client-side session the manageable object belongs to.
 * @param object the client-side manageable object.
 */

    public void
    initProxy(String name, SessionImpl session, Object object) {
        if (ManageableProxy_Debug) {
            debug("ManageableProxy: initProxy:" +
                  " name: "    + name +
                  " session: " + session +
                  " object: "  + object);
        }

        this.session = session;
    }


/**
 * <A NAME="SD_GETPROXY"></A>
 * <EM>getProxy</EM> get a handle to the client-side proxy for this manageable
 * object.
 *
 * @return the client-side proxy for this manageable object.
 */

    public Object
    getProxy() {
        if (ManageableProxy_Debug) {
            debug("ManageableProxy: getProxy.");
        }

        return(this);
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
 * <A NAME="SD_ADDLISTENER"></A>
 * <EM>addListener</EM> add a listener to this manageable object.
 *
 * @param listener the listener for this manageable object.
 * @param listenerType the type of listener. This will be one of:
 * SessionImpl.M_Session, ChannelImpl.M_Channel, ByteArrayImpl.M_ByteArray
 * or TokenImpl.M_Token.
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
    addListener(EventListener listener, char listenerType)
        throws ConnectionException,
               NoSuchByteArrayException, NoSuchChannelException,
               NoSuchSessionException, NoSuchTokenException,
               TimedOutException {
        if (ManageableProxy_Debug) {
            debug("ManageableProxy: addListener:" +
                  " listener: "      + listener +
                  " listener type: " + listenerType);
        }
    }


/**
 * <A NAME="SD_REMOVELISTENER"></A>
 * <EM>removeListener</EM> remove a listener from this manageable object.
 *
 * @param listener the listener for this manageable object.
 * @param listenerType the type of listener. This will be one of:
 * SessionImpl.M_Session, ChannelImpl.M_Channel, ByteArrayImpl.M_ByteArray
 * or TokenImpl.M_Token.
 *
 * @exception ConnectionException if a connection error occured.
 * @exception NoSuchByteArrayException if this ByteArray doesn't exist
 * @exception NoSuchChannelException if this Channel doesn't exist.
 * @exception NoSuchListenerException if this Listener doesn't exist.
 * @exception NoSuchSessionException if this Session doesn't exist.
 * @exception NoSuchTokenException if this Token doesn't exist.
 * @exception TimedOutException if no reply was received for this operation
 * in the given timeout period.
 */

    public void
    removeListener(EventListener listener, char listenerType)
        throws ConnectionException,
               NoSuchByteArrayException, NoSuchChannelException,
               NoSuchListenerException, NoSuchSessionException,
               NoSuchTokenException, TimedOutException {
        if (ManageableProxy_Debug) {
            debug("ManageableProxy: removeListener:" +
                  " listener: "      + listener +
                  " listener type: " + listenerType);
        }
    }


/**
 * <A NAME="SD_CHANGELISTENERMASK"></A>
 * <EM>changeListenerMask</EM> enables or disables certain events for
 * this JSDT listener.
 *
 * @param listener the listener whose event mask is being changed.
 * @param eventMask the mask of events to be enabled or disabled.
 * @param disable if set true, then disable these events for this listener,
 * else enable them.
 *
 * @exception NoSuchSessionException if this Session doesn't exist.
 * @exception NoSuchByteArrayException if this ByteArray doesn't exist.
 * @exception NoSuchChannelException if this Channel doesn't exist.
 * @exception NoSuchTokenException if this Token doesn't exist.
 * @exception NoSuchListenerException if this Listener doesn't exist.
 */

    public void
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

    public void
    changeManagerMask(JSDTManager manager, int eventMask,
                      boolean disable, char objectType)
        throws ConnectionException, NoSuchSessionException,
               NoSuchChannelException, NoSuchByteArrayException,
               NoSuchTokenException, NoSuchManagerException,
               TimedOutException {
        if (ManageableProxy_Debug) {
            debug("ManageableProxy: changeManagerMask:" +
                  " manager: "     + manager +
                  " event mask: "  + eventMask +
                  " disable?: "    + disable +
                  " object type: " + objectType);
        }
    }


/**
 * <A NAME="SD_ATTACHMANAGER"></A>
 * <EM>attachManager</EM> attaches a manager to the given manageable object.
 *
 * @param manager the manager to attach.
 * @param managerType an indication of the type of this manager.
 * @param manageable the manageable object.
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
        if (ManageableProxy_Debug) {
            debug("ManageableProxy: attachManager:" +
                  " manager: "      + manager +
                  " manager type: " + managerType +
                  " manageable: "   + manageable);
        }
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
 * @param objectType the type of the manageable object. This will be one of:
 * SessionImpl.M_Session, ChannelImpl.M_Channel, ByteArrayImpl.M_ByteArray or
 * TokenImpl.M_Token.
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
 * @param objectType the type of the manageable object. This will be one of:
 * SessionImpl.M_Session, ChannelImpl.M_Channel, ByteArrayImpl.M_ByteArray or
 * TokenImpl.M_Token.
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
    }


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
 * @param objectType the type of the manageable object. This will be one of:
 * SessionImpl.M_Session, ChannelImpl.M_Channel, ByteArrayImpl.M_ByteArray or
 * TokenImpl.M_Token.
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
    destroy(Client client, char objectType)
        throws ConnectionException, InvalidClientException,
               NoSuchSessionException, NoSuchChannelException,
               NoSuchByteArrayException, NoSuchClientException,
               NoSuchTokenException, PermissionDeniedException,
               TimedOutException {
        if (ManageableProxy_Debug) {
            debug("ManageableProxy: destroy:" +
                  " client: "     + client +
                  " objectType: " + objectType);
        }
    }


/**
 * <A NAME="SD_ISMANAGED"></A>
 * test whether this managed object actually has a manager associated with it.
 *
 * @param objectType the type of the manageable object. This will be one of:
 * SessionImpl.M_Session, ChannelImpl.M_Channel, ByteArrayImpl.M_ByteArray or
 * TokenImpl.M_Token.
 * @param objectName the name of the manageable object.
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
        boolean isManaged = false;

        if (ManageableProxy_Debug) {
            debug("ManageableProxy: isManaged:" +
                  " object type: " + objectType +
                  " object name: " + objectName);
        }

        return(isManaged);
    }


/**
 * <A NAME="SD_JOIN"></A>
 * join a Client to this Manageable object.
 *
 * <P>If this is a managed object, then the Client is authenticated to
 * determine if it is permitted to do this operation.
 *
 * @param client the Client wishing to join this Manageable object.
 * @param authenticate set true if this Client should be authenticated.
 * @param objectType the type of the manageable object. This will be one of:
 * SessionImpl.M_Session, ChannelImpl.M_Channel, ByteArrayImpl.M_ByteArray or
 * TokenImpl.M_Token.
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

    public void
    join(Client client, boolean authenticate, char objectType)
        throws ConnectionException, InvalidClientException,
               NoSuchByteArrayException, NoSuchChannelException,
               NoSuchClientException, NoSuchSessionException,
               NoSuchTokenException, PermissionDeniedException,
               NameInUseException, TimedOutException {
        if (ManageableProxy_Debug) {
            debug("ManageableProxy: join:" +
                  " client: "       + client +
                  " authenticate? " + authenticate +
                  " object type: "  + objectType);
        }
    }


/**
 * <A NAME="SD_LEAVE"></A>
 * removes a Client from this Manageable object. This Client will no
 * longer be known to this object. Listeners of this object will be
 * sent an indication when this happens.
 *
 * @param client the Client in question.
 * @param objectType the type of the manageable object. This will be one of:
 * SessionImpl.M_Session, ChannelImpl.M_Channel, ByteArrayImpl.M_ByteArray or
 * TokenImpl.M_Token.
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

    public void
    leave(Client client, char objectType)
        throws ConnectionException, InvalidClientException,
               NoSuchByteArrayException, NoSuchChannelException,
               NoSuchClientException, NoSuchSessionException,
               NoSuchTokenException, TimedOutException {
        if (ManageableProxy_Debug) {
            debug("ManageableProxy: leave:" +
                  " client: "     + client +
                  " objectType: " + objectType);
        }
    }


/**
 * <A NAME="SD_LISTCLIENTNAMES"></A>
 * list the names of the Clients who are joined to this Manageable object.
 *
 * @param objectType the type of the manageable object. This will be one of:
 * SessionImpl.M_Session, ChannelImpl.M_Channel, ByteArrayImpl.M_ByteArray or
 * TokenImpl.M_Token.
 *
 * @return a sorted array of names of Clients currently joined to this object.
 *
 * @exception ConnectionException if a connection error occured.
 * @exception NoSuchByteArrayException if this ByteArray doesn't exist.
 * @exception NoSuchChannelException if this Channel doesn't exist.
 * @exception NoSuchSessionException if this Session doesn't exist.
 * @exception NoSuchTokenException if this Token doesn't exist.
 * in the given timeout period.
 * @exception TimedOutException if no reply was received for this operation
 * in the given timeout period.
 */

    public String[]
    listClientNames(char objectType)
        throws ConnectionException,
               NoSuchSessionException, NoSuchChannelException,
               NoSuchByteArrayException, NoSuchTokenException,
               TimedOutException {
        String[] clientNames = null;

        if (ManageableProxy_Debug) {
            debug("ManageableProxy: listClientNames:" +
                  " object type: " + objectType);
        }

        return(clientNames);
    }
}
