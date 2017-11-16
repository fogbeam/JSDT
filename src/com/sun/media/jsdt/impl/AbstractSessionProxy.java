
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
 * JSDT abstract client-side Session proxy interface.
 *
 * @version     2.3 - 6th November 2017
 * @author      Rich Burridge
 */

public interface
AbstractSessionProxy extends AbstractManageableProxy {

/**
 * <A NAME="SD_INITPROXY"></A>
 * <EM>initProxy</EM> initialise the "client-side" proxy for this Session.
 *
 * @param name the name of the Session proxy being constructed.
 * @param session the client-side Session.
 * @param object the client-side Session (same as session).
 */

    void
    initProxy(String name, SessionImpl session, Object object);


/**
 * <A NAME="SD_GETPROXY"></A>
 * <EM>getProxy</EM> get a handle to the proxy for this Session.
 *
 * @return a handle to the proxy for this Session.
 */

    Object
    getProxy();


/**
 * <A NAME="SD_ATTACHSESSIONMANAGER"></A>
 * <EM>attachSessionManager</EM> attach a SessionManager to this Session.
 *
 * @param sessionManager the manager to attach to this Session.
 * @param session the Session to attach the manager to.
 *
 * @exception ConnectionException if a connection error occured.
 * @exception ManagerExistsException if a manager already exists for this
 * Session.
 * @exception NoSuchHostException if the remote host associated with this
 * Session doesn't exist.
 * @exception NoSuchSessionException if this Session doesn't exist.
 * @exception TimedOutException if no reply was received for this operation
 * in the given timeout period.
 */

    void
    attachSessionManager(SessionManager sessionManager, Session session)
                throws ConnectionException, ManagerExistsException,
                       NoSuchHostException, NoSuchSessionException,
                       TimedOutException;


/**
 * <A NAME="SD_CREATEBYTEARRAY"></A>
 * creates a shared ByteArray with the given name and value which can then
 * be used with the various ByteArray operations. This ByteArray is then
 * known to the Session. No ByteArrayManager is associated with this
 * ByteArray; any Client which has already joined the Session may freely
 * join this ByteArray.
 *
 * <P>If a ByteArray with this name already exists, a reference to that
 * ByteArray is returned.
 *
 * @param client a Client that will be used for authentication purposes if
 * this is a managed Session.
 * @param byteArrayName the name to give this ByteArray.
 * @param value the byte array that is the source of the initial new shared
 * ByteArray value.
 * @param offset the initial offset within the byte array.
 * @param length the number of bytes to use.
 * @param autoJoin if true, automatically join the ByteArray when it's created.
 *
 * @exception ConnectionException if a connection error occured.
 * @exception InvalidClientException if the Client is invalid is some way (ie.
 * its getName() method returns null).
 * @exception NameInUseException if a Client with this name is already
 * joined to this ByteArray.
 * @exception NoSuchSessionException if this Session doesn't exist.
 * @exception NoSuchClientException if this Client doesn't exist.
 * @exception NoSuchHostException if the remote host associated with this
 * Session doesn't exist.
 * @exception PermissionDeniedException if this Client doesn't have
 * permission for this operation.
 * @exception TimedOutException if no reply was received for this operation
 * in the given timeout period.
 *
 * @return the newly created ByteArray or a local reference to it, if it was
 * already created.
 */

    ByteArray
    createByteArray(Client client, String byteArrayName,
                    byte[] value, int offset, int length, boolean autoJoin)
                throws ConnectionException, InvalidClientException,
                       NameInUseException, NoSuchSessionException,
                       NoSuchClientException, NoSuchHostException,
                       PermissionDeniedException, TimedOutException;


/**
 * <A NAME="SD_CREATECHANNEL"></A>
 * creates a Channel with the given name which can then be used with the
 * various Channel operations. This Channel is then known to the Session.
 * No ChannelManager is associated with this Channel; any Client which has
 * already joined the Session may freely join this Channel.
 *
 * <P>If a Channel with this name already exists, a reference to that Channel
 * is returned.
 *
 * @param client a client that will be used for authentication purposes if
 * this is a managed session.
 * @param channelName the name to give this channel.
 * @param reliable whether the channel is reliable. In other words whether
 * data delivery is guarenteed.
 * @param ordered whether data sent over the channel is ordered.
 * @param autoJoin if true, automatically join the Channel when it's created.
 *
 * @exception ConnectionException if a connection error occured.
 * @exception InvalidClientException if the Client is invalid is some way (ie.
 * its getName() method returns null).
 * @exception NameInUseException if a Client with this name is already
 * joined to this Channel.
 * @exception NoSuchSessionException if this Session doesn't exist.
 * @exception NoSuchClientException if this Client doesn't exist.
 * @exception NoSuchHostException if the remote host associated with this
 * Session doesn't exist.
 * @exception PermissionDeniedException if this Client doesn't have
 * permission for this operation.
 * @exception TimedOutException if no reply was received for this operation
 * in the given timeout period.
 *
 * @return the newly created Channel or a local reference to it, if it was
 * already created.
 */

    Channel
    createChannel(Client client, String channelName,
                  boolean reliable, boolean ordered, boolean autoJoin)
                throws ConnectionException, InvalidClientException,
                       NameInUseException, NoSuchSessionException,
                       NoSuchClientException, NoSuchHostException,
                       PermissionDeniedException, TimedOutException;


/**
 * <A NAME="SD_CREATETOKEN"></A>
 * creates a Token with the given name which can then be used with the
 * various Token operations. This Token is then known to the session. No
 * TokenManager is associated with this Token; any Client which has
 * already joined the Session may freely join this Token.
 *
 * <P>If a Token with this name already exists, a reference to that Token is
 * returned.
 *
 * @param client a Client that will be used for authentication purposes if
 * this is a managed Session.
 * @param tokenName the name to give this Token.
 * @param autoJoin if true, automatically join the Token when it's created.
 *
 * @exception ConnectionException if a connection error occured.
 * @exception InvalidClientException if the Client is invalid is some way (ie.
 * its getName() method returns null).
 * @exception NameInUseException if a Client with this name is already
 * joined to this Token.
 * @exception NoSuchSessionException if this Session doesn't exist.
 * @exception NoSuchClientException if this Client doesn't exist.
 * @exception NoSuchHostException if the remote host associated with this
 * Session doesn't exist.
 * @exception PermissionDeniedException if this Client doesn't have
 * permission for this operation.
 * @exception TimedOutException if no reply was received for this operation
 * in the given timeout period.
 *
 * @return the newly created Token or a local reference to it, if it was
 * already created.
 */

    Token
    createToken(Client client, String tokenName, boolean autoJoin)
                throws ConnectionException, InvalidClientException,
                       NameInUseException, NoSuchSessionException,
                       NoSuchClientException, NoSuchHostException,
                       PermissionDeniedException, TimedOutException;


/**
 * <A NAME="SD_BYTEARRAYEXISTS"></A>
 * checks if a Bytearray with this name exists.
 *
 * @param byteArrayName the name of the ByteArray to check on.
 *
 * @exception ConnectionException if a connection error occured.
 * @exception NoSuchSessionException if this Session doesn't exist.
 * @exception TimedOutException if no reply was received for this operation
 * in the given timeout period.
 *
 * @return true if a ByteArray with this name exists; false if it doesn't.
 */

    boolean
    byteArrayExists(String byteArrayName)
                throws ConnectionException, NoSuchSessionException,
                       TimedOutException;


/**
 * <A NAME="SD_CHANNELEXISTS"></A>
 * checks if a Channel with this name exists.
 *
 * @param channelName the name of the Channel to check on.
 *
 * @exception ConnectionException if a connection error occured.
 * @exception NoSuchSessionException if this Session doesn't exist.
 * @exception TimedOutException if no reply was received for this operation
 * in the given timeout period.
 *
 * @return true if a Channel with this name exists; false if it doesn't.
 */

    boolean
    channelExists(String channelName)
                throws ConnectionException, NoSuchSessionException,
                       TimedOutException;


/**
 * <A NAME="SD_TOKENEXISTS"></A>
 * checks if a Token with this name exists.
 *
 * @param tokenName the name of the Token to check on.
 *
 * @exception ConnectionException if a connection error occured.
 * @exception NoSuchSessionException if this Session doesn't exist.
 * @exception TimedOutException if no reply was received for this operation
 * in the given timeout period.
 *
 * @return true if a Token with this name exists; false if it doesn't.
 */

    boolean
    tokenExists(String tokenName)
                throws ConnectionException, NoSuchSessionException,
                       TimedOutException;


/**
 * <A NAME="SD_BYTEARRAYMANAGED"></A>
 * checks if the Bytearray with this name is managed.
 *
 * @param byteArrayName the name of the ByteArray to check on.
 *
 * @exception ConnectionException if a connection error occured.
 * @exception NoSuchByteArrayException if the byte array doesn't exist.
 * @exception NoSuchSessionException if this Session doesn't exist.
 * @exception TimedOutException if no reply was received for this operation
 * in the given timeout period.
 *
 * @return true if the ByteArray with this name is managed; false if it isn't.
 */

    boolean
    byteArrayManaged(String byteArrayName)
                throws ConnectionException, NoSuchByteArrayException,
                       NoSuchSessionException, TimedOutException;


/**
 * <A NAME="SD_CHANNELMANAGED"></A>
 * checks if the Channel with this name is managed.
 *
 * @param channelName the name of the Channel to check on.
 *
 * @exception ConnectionException if a connection error occured.
 * @exception NoSuchChannelException if the channel doesn't exist.
 * @exception NoSuchSessionException if this Session doesn't exist.
 * @exception TimedOutException if no reply was received for this operation
 * in the given timeout period.
 *
 * @return true if the Channel with this name is managed; false if it isn't.
 */

    boolean
    channelManaged(String channelName)
                throws ConnectionException, NoSuchChannelException,
                       NoSuchSessionException, TimedOutException;


/**
 * <A NAME="SD_TOKENMANAGED"></A>
 * checks if the Token with this name is managed.
 *
 * @param tokenName the name of the Token to check on.
 *
 * @exception ConnectionException if a connection error occured.
 * @exception NoSuchTokenException if the token doesn't exist.
 * @exception NoSuchSessionException if this Session doesn't exist.
 * @exception TimedOutException if no reply was received for this operation
 * in the given timeout period.
 *
 * @return true if the Token with this name is managed; false if it isn't.
 */

    boolean
    tokenManaged(String tokenName)
                throws ConnectionException, NoSuchTokenException,
                       NoSuchSessionException, TimedOutException;


/**
 * <A NAME="SD_GETBYTEARRAYSJOINED"></A>
 * return an array of ByteArrays that this Client has successfully joined.
 *
 * @param client the Client to check on.
 *
 * @exception ConnectionException if a connection error occured.
 * @exception InvalidClientException if the Client is invalid is some way (ie.
 * its getName() method returns null).
 * @exception NoSuchSessionException if this Session doesn't exist.
 * @exception TimedOutException if no reply was received for this operation
 * in the given timeout period.
 *
 * @return an array of ByteArrays that this Client has successfully joined.
 */

    ByteArray[]
    getByteArraysJoined(Client client)
                throws ConnectionException, InvalidClientException,
                       NoSuchSessionException, TimedOutException;


/**
 * <A NAME="SD_GETCHANNELSJOINED"></A>
 * return an array of Channels that this Client has successfully joined.
 *
 * @param client the Client to check on.
 *
 * @exception ConnectionException if a connection error occured.
 * @exception InvalidClientException if the Client is invalid is some way (ie.
 * its getName() method returns null).
 * @exception NoSuchSessionException if this Session doesn't exist.
 * @exception TimedOutException if no reply was received for this operation
 * in the given timeout period.
 *
 * @return an array of Channels that this Client has successfully joined.
 */

    Channel[]
    getChannelsJoined(Client client)
                throws ConnectionException, InvalidClientException,
                       NoSuchSessionException, TimedOutException;


/**
 * <A NAME="SD_GETTOKENSJOINED"></A>
 * return an array of Tokens that this Client has successfully joined.
 *
 * @param client the Client to check on.
 *
 * @exception ConnectionException if a connection error occured.
 * @exception InvalidClientException if the Client is invalid is some way (ie.
 * its getName() method returns null).
 * @exception NoSuchSessionException if this Session doesn't exist.
 * @exception TimedOutException if no reply was received for this operation
 * in the given timeout period.
 *
 * @return an array of Tokens that this Client has successfully joined.
 */

    Token[]
    getTokensJoined(Client client)
                throws ConnectionException, InvalidClientException,
                       NoSuchSessionException, TimedOutException;


/**
 * <A NAME="SD_LISTBYTEARRAYNAMES"></A>
 * list the names of the ByteArrays that are known to this Session.
 *
 * @exception ConnectionException if a connection error occured.
 * @exception NoSuchSessionException if this Session doesn't exist.
 * @exception TimedOutException if no reply was received for this operation
 * in the given timeout period.
 *
 * @return a sorted array of names of the ByteArrays that are known to
 * this Session.
 */

    String[]
    listByteArrayNames()
                throws ConnectionException, NoSuchSessionException,
                       TimedOutException;


/**
 * <A NAME="SD_LISTCHANNELNAMES"></A>
 * list the names of the Channels that are known to this Session.
 *
 * @exception ConnectionException if a connection error occured.
 * @exception NoSuchSessionException if this Session doesn't exist.
 * @exception TimedOutException if no reply was received for this operation
 * in the given timeout period.
 *
 * @return a sorted array of names of the Channels that are known to
 * this Session.
 */

    String[]
    listChannelNames()
                throws ConnectionException, NoSuchSessionException,
                       TimedOutException;


/**
 * <A NAME="SD_LISTTOKENNAMES"></A>
 * list the names of the Tokens that are known to this Session.
 *
 * @exception ConnectionException if a connection error occured.
 * @exception NoSuchSessionException if this Session doesn't exist.
 * @exception TimedOutException if no reply was received for this operation
 * in the given timeout period.
 *
 * @return a sorted array of names of the Tokens that are known to
 * this Session.
 */

    String[]
    listTokenNames()
                throws ConnectionException, NoSuchSessionException,
                       TimedOutException;


/**
 * <A NAME="SD_GETURL"></A>
 * gives the URLString used to create this Session.
 *
 * @return the URLString used to create this Session.
 *
 * @since       JSDT 2.1
 */

    URLString
    getURL();


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

    void
    close(boolean closeConnection)
                throws ConnectionException, NoSuchSessionException;
}
