
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
import com.sun.media.jsdt.event.SessionListener;

/**
 * JSDT Session (implementation) class.
 *
 * @version     2.3 - 6th November 2017
 * @author      Rich Burridge
 */

public abstract class
SessionImpl extends ManageableImpl implements Session {

    // To signify a manageable object of type Session.
    public static final char M_Session = '\u00F0';

    /** Handle to client-side proxy.
     *
     *  @serial
     */
    public AbstractSessionProxy po;

    /** Handle to server-side object.
     *
     *  @serial
     */
    public AbstractSessionServer so;


/**
 * <A NAME="SD_CREATEBYTEARRAY"></A>
 * creates a shared ByteArray with the given name which can then
 * be used with the various ByteArray operations. This ByteArray is then
 * known to the Session. No ByteArrayManager is associated with this
 * ByteArray; any Client which has already joined the Session may freely
 * join this ByteArray.
 *
 * <P>If a ByteArray with this name already exists, a reference to that
 * ByteArray is returned. If the ByteArray didn't already exist, then it's
 * initial value will be a single zero filled byte.
 *
 * @param client a Client that will be used for authentication purposes if
 * this is a managed Session.
 * @param byteArrayName the name to give this ByteArray.
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

    public ByteArray
    createByteArray(Client client, String byteArrayName, boolean autoJoin)
                throws ConnectionException, InvalidClientException,
                       NameInUseException, NoSuchSessionException,
                       NoSuchClientException, NoSuchHostException,
                       PermissionDeniedException, TimedOutException {
        byte[] value = new byte[1];

        if (SessionImpl_Debug) {
            debug("SessionImpl: createByteArray:" +
                  " client name : "    + client.getName() +
                  " byte array name: " + byteArrayName +
                  " auto join? "       + autoJoin);
        }

        return(po.createByteArray(client, byteArrayName,
                                  value, 0, value.length, autoJoin));
    }


/**
 * <A NAME="SD_CREATEBYTEARRAYWITHMANAGER"></A>
 * creates a shared ByteArray with the given name which can then
 * be used with the various ByteArray operations. This ByteArray is then
 * known to the Session. A ByteArrayManager is associated with this ByteArray;
 * Clients are authenticated before they are allowed to join the ByteArray.
 *
 * <P>The initial value for this newly created ByteArray will be a zero
 * filled byte array, one byte long.
 *
 * @param client a Client that will be used for authentication purposes if
 * this is a managed Session.
 * @param byteArrayName the name to give this ByteArray.
 * @param autoJoin if true, automatically join the ByteArray when it's created.
 * @param byteArrayManager the manager of this ByteArray.
 *
 * @exception ConnectionException if a connection error occured.
 * @exception InvalidClientException if the Client is invalid is some way (ie.
 * its getName() method returns null).
 * @exception ManagerExistsException if a manager already exists for this
 * ByteArray.
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
 * @return the newly created managed ByteArray.
 */

    public ByteArray
    createByteArray(Client client, String byteArrayName,
                    boolean autoJoin, ByteArrayManager byteArrayManager)
                throws ConnectionException, InvalidClientException,
                       ManagerExistsException, NameInUseException,
                       NoSuchSessionException, NoSuchClientException,
                       NoSuchHostException, PermissionDeniedException,
                       TimedOutException {
        boolean       alreadyExists = false;
        ByteArrayImpl byteArray     = null;

        if (SessionImpl_Debug) {
            debug("SessionImpl: createByteArray:" +
                  " client name : "    + client.getName() +
                  " byte array name: " + byteArrayName +
                  " auto join? "       + autoJoin +
                  " manager: "         + byteArrayManager);
        }

        try {
            if (byteArrayExists(byteArrayName)) {
                alreadyExists = true;
            }

            byteArray = (ByteArrayImpl) createByteArray(client,
                                                        byteArrayName, false);
            if (alreadyExists) {
                try {
                    if (byteArray.isManaged()) {
                        throw new ManagerExistsException();
                    } else {
                        throw new PermissionDeniedException();
                    }
                } catch (NoSuchByteArrayException | NoSuchChannelException |
                         NoSuchTokenException nse) {
                }
            }

            if (byteArrayManager != null) {
                AbstractByteArrayProxy bp = (AbstractByteArrayProxy)
                                                byteArray.po.getProxy();

                bp.attachManager(byteArrayManager, ByteArrayImpl.M_ByteArray,
                                 byteArray);
            }
        } catch (NameInUseException niue) {
            error("SessionImpl: createByteArray: ", niue);
        }

        if (autoJoin) {
            try {
                byteArray.join(client);
            } catch (NoSuchByteArrayException nsbae) {
                error("SessionImpl: createByteArray: ", nsbae);
            } catch (NoSuchChannelException | NoSuchTokenException nse) {
                // Can't happen.
            }
        }

        return(byteArray);
    }


/**
 * <A NAME="SD_CREATECHANNEL"></A>
 * <EM>createChannel</EM> creates a channel with the given name which can
 * then be used with the various channel operations. This channel is then
 * known to the session. No channel manager is associated with this channel;
 * any client which has already joined the session may freely join this channel. *
 * If the given name already exists (ie. a channel is already created with
 * this name), a pointer to it is returned.
 *
 * @param client a client that will be used for authentication purposes if
 * this is a managed session.
 * @param channelName the name to give this channel.
 * @param reliable whether the channel is reliable. In other words whether
 * data delivery is guarenteed.
 * @param ordered whether data sent over the channel is ordered.
 * @param autoJoin if true, then the given client is automatically joined to
 * this channel.
 *
 * @exception ConnectionException if a connection error occured.
 * @exception InvalidClientException if the Client is invalid is some way (ie.
 * its getName() method returns null).
 * @exception NameInUseException if a Client with this name is already
 * joined to this Channel.
 * @exception NoSuchSessionException if this session does not exist.
 * @exception NoSuchClientException if the client given doesn't exist.
 * @exception NoSuchHostException if the remote host associated with this
 * Session doesn't exist.
 * @exception PermissionDeniedException if the client doesn't have permission
 * to create and/or join this channel.
 * @exception TimedOutException if no reply was received for this operation
 * in the given timeout period.
 *
 * @return a pointer to the Channel.
 */

    public Channel
    createChannel(Client client, String channelName,
                  boolean reliable, boolean ordered, boolean autoJoin)
                throws ConnectionException, InvalidClientException,
                       NameInUseException, NoSuchSessionException,
                       NoSuchClientException, NoSuchHostException,
                       PermissionDeniedException, TimedOutException {
        if (SessionImpl_Debug) {
            debug("SessionImpl: createChannel:" +
                  " client: "       + client +
                  " channel name: " + channelName +
                  " reliable: "     + reliable +
                  " ordered: "      + ordered +
                  " auto join? "    + autoJoin);
        }

        return(po.createChannel(client, channelName,
                                reliable, ordered, autoJoin));
    }


/**
 * <A NAME="SD_CREATECHANNELWITHMANAGER"></A>
 * creates a Channel with the given name which can then be used with the
 * various Channel operations. This Channel is then known to the Session.
 * A ChannelManager is associated with this Channel; Clients are
 * authenticated before they are allowed to join the Channel (ie. the
 * equivalent of a private channel).
 *
 * @param client a Client that will be used for authentication purposes if
 * this is a managed Session.
 * @param channelName the name to give this Channel.
 * @param reliable whether the Channel is reliable. In other words whether
 * data delivery is guarenteed.
 * @param ordered whether Data sent over the Channel is ordered.
 * @param autoJoin if true, then the given client is automatically joined to
 * this channel.
 * @param channelManager the manager for this Channel.
 *
 * @exception ConnectionException if a connection error occured.
 * @exception InvalidClientException if the Client is invalid is some way (ie.
 * its getName() method returns null).
 * @exception ManagerExistsException if a manager already exists for this
 * Channel.
 * @exception NameInUseException if a Client with this name is already
 * joined to this Channel.
 * @exception NoSuchClientException if this Client doesn't exist.
 * @exception NoSuchHostException if the remote host associated with this
 * Session doesn't exist.
 * @exception NoSuchSessionException if this Session doesn't exist.
 * @exception PermissionDeniedException if this Client doesn't have
 * permission for this operation.
 * @exception TimedOutException if no reply was received for this operation
 * in the given timeout period.
 *
 * @return the newly created managed Channel.
 */

    public Channel
    createChannel(Client client, String channelName,
                  boolean reliable, boolean ordered, boolean autoJoin,
                  ChannelManager channelManager)
                throws ConnectionException, InvalidClientException,
                       ManagerExistsException, NameInUseException,
                       NoSuchClientException, NoSuchHostException,
                       NoSuchSessionException, PermissionDeniedException,
                       TimedOutException {
        boolean     alreadyExists = false;
        ChannelImpl channel       = null;

        if (SessionImpl_Debug) {
            debug("SessionImpl: createChannel:" +
                  " client: "       + client +
                  " channel name: " + channelName +
                  " reliable: "     + reliable +
                  " ordered: "      + ordered +
                  " auto join? "    + autoJoin +
                  " manager: "      + channelManager);
        }

        try {
            if (channelExists(channelName)) {
                alreadyExists = true;
            }

            channel = (ChannelImpl) createChannel(client, channelName,
                                                reliable, ordered, false);

            if (alreadyExists) {
                try {
                    if (channel.isManaged()) {
                        throw new ManagerExistsException();
                    } else {
                        throw new PermissionDeniedException();
                    }
                } catch (NoSuchByteArrayException | NoSuchChannelException |
                         NoSuchTokenException nse) {
                }
            }

            if (channelManager != null) {
                AbstractChannelProxy cp = (AbstractChannelProxy)
                                                channel.po.getProxy();

                cp.attachManager(channelManager,
                                 ChannelImpl.M_Channel, channel);
            }
        } catch (NameInUseException niue) {
            error("SessionImpl: createChannel: ", niue);
        }

        if (autoJoin) {
            try {
                channel.join(client);
            } catch (NoSuchChannelException nsce) {
                error("SessionImpl: createChannel: ", nsce);
            }
        }

        return(channel);
    }


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

    public Token
    createToken(Client client, String tokenName, boolean autoJoin)
                throws ConnectionException, InvalidClientException,
                       NameInUseException, NoSuchSessionException,
                       NoSuchClientException, NoSuchHostException,
                       PermissionDeniedException, TimedOutException {
        if (SessionImpl_Debug) {
            debug("SessionImpl: createToken:" +
                  " client: "     + client +
                  " token name: " + tokenName +
                  " autoJoin? "   + autoJoin);
        }

        return(po.createToken(client, tokenName, autoJoin));
    }


/**
 * <A NAME="SD_CREATETOKENWITHMANAGER"></A>
 * creates a Token with the given name which can then be used with the
 * various Token operations. This Token is then known to the Session.
 * A TokenManager is associated with this Token; Clients are authenticated
 * before they are allowed to join the Token (ie. the equivalent of a
 * private Token).
 *
 * @param client a Client that will be used for authentication purposes if
 * this is a managed Session.
 * @param tokenName the name of the Token to create.
 * @param autoJoin if true, automatically join the Token when it's created.
 * @param tokenManager the manager of this Token.
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
 * @exception ManagerExistsException if a manager already exists for this
 * Token.
 * @exception TimedOutException if no reply was received for this operation
 * in the given timeout period.
 *
 * @return the newly created managed Token.
 */

    public Token
    createToken(Client client, String tokenName,
                boolean autoJoin, TokenManager tokenManager)
                throws ConnectionException, InvalidClientException,
                       ManagerExistsException, NameInUseException,
                       NoSuchClientException, NoSuchHostException,
                       NoSuchSessionException, PermissionDeniedException,
                       TimedOutException {
        boolean   alreadyExists = false;
        TokenImpl token         = null;

        if (SessionImpl_Debug) {
            debug("SessionImpl: createToken:" +
                  " client: "     + client +
                  " token name: " + tokenName +
                  " autoJoin? "   + autoJoin +
                  " manager: "    + tokenManager);
        }

        try {
            if (tokenExists(tokenName)) {
                alreadyExists = true;
            }

            token = (TokenImpl) createToken(client, tokenName, false);

            if (alreadyExists) {
                try {
                    if (token.isManaged()) {
                        throw new ManagerExistsException();
                    } else {
                        throw new PermissionDeniedException();
                    }
                } catch (NoSuchByteArrayException | NoSuchChannelException |
                         NoSuchTokenException nse) {
                }
            }

            if (tokenManager != null) {
                AbstractTokenProxy tp = (AbstractTokenProxy)
                                                token.po.getProxy();

                tp.attachManager(tokenManager, TokenImpl.M_Token, token);
            }
        } catch (NameInUseException niue) {
            error("SessionProxy: createToken: ", niue);
        }

        if (autoJoin) {
            try {
                token.join(client);
            } catch (NoSuchByteArrayException | NoSuchChannelException nse) {
                // Can't happen.
            } catch (NoSuchTokenException nste) {
                error("SessionProxy: createToken: ", nste);
            }
        }

        return(token);
    }


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

    public boolean
    byteArrayExists(String byteArrayName)
                throws ConnectionException, NoSuchSessionException,
                       TimedOutException {
        if (SessionImpl_Debug) {
            debug("SessionImpl: byteArrayExists:" +
                  " byte array name: " + byteArrayName);
        }

        return(po.byteArrayExists(byteArrayName));
    }


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

    public boolean
    channelExists(String channelName)
                throws ConnectionException, NoSuchSessionException,
                       TimedOutException {
        if (SessionImpl_Debug) {
            debug("SessionImpl: channelExists:" +
                  " channel name: " + channelName);
        }

        return(po.channelExists(channelName));
    }


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

    public boolean
    tokenExists(String tokenName)
                throws ConnectionException, NoSuchSessionException,
                       TimedOutException {
        if (SessionImpl_Debug) {
            debug("SessionImpl: tokenExists:" +
                  " token name: " + tokenName);
        }

        return(po.tokenExists(tokenName));
    }


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

    public boolean
    byteArrayManaged(String byteArrayName)
                throws ConnectionException, NoSuchByteArrayException,
                       NoSuchSessionException, TimedOutException {
        if (SessionImpl_Debug) {
            debug("SessionImpl: byteArrayManaged:" +
                  " byte array name: " + byteArrayName);
        }

        return(po.byteArrayManaged(byteArrayName));
    }


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

    public boolean
    channelManaged(String channelName)
                throws ConnectionException, NoSuchChannelException,
                       NoSuchSessionException, TimedOutException {
        if (SessionImpl_Debug) {
            debug("SessionImpl: channelManaged:" +
                  " channel name: " + channelName);
        }

        return(po.channelManaged(channelName));
    }


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

    public boolean
    tokenManaged(String tokenName)
                throws ConnectionException, NoSuchTokenException,
                       NoSuchSessionException, TimedOutException {
        if (SessionImpl_Debug) {
            debug("SessionImpl: tokenManaged:" +
                  " token name: " + tokenName);
        }

        return(po.tokenManaged(tokenName));
    }


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

    public ByteArray[]
    getByteArraysJoined(Client client)
                throws ConnectionException, InvalidClientException,
                       NoSuchSessionException, TimedOutException {
        if (SessionImpl_Debug) {
            debug("SessionImpl: getByteArraysJoined:" +
                  " client: " + client);
        }

        return(po.getByteArraysJoined(client));
    }


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

    public Channel[]
    getChannelsJoined(Client client)
                throws ConnectionException, InvalidClientException,
                       NoSuchSessionException, TimedOutException {
        if (SessionImpl_Debug) {
            debug("SessionImpl: getChannelsJoined:" +
                  " client: " + client);
        }

        return(po.getChannelsJoined(client));
    }


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

    public Token[]
    getTokensJoined(Client client)
                throws ConnectionException, InvalidClientException,
                       NoSuchSessionException, TimedOutException {
        if (SessionImpl_Debug) {
            debug("SessionImpl: getTokensJoined:" +
                  " client: " + client);
        }

        return(po.getTokensJoined(client));
    }


/**
 * <A NAME="SD_LISTBYTEARRAYNAMES"></A>
 * <EM>listByteArrayNames</EM> list the names of the shared byte arrays that
 * are known to this session.
 *
 * @exception ConnectionException if a connection error occured.
 * @exception NoSuchSessionException if this session does not exist.
 * @exception TimedOutException if no reply was received for this operation
 * in the given timeout period.
 *
 * @return an array of names of the shared byte arrays that are known to
 * this Session.
 */

    public String[]
    listByteArrayNames()
                throws ConnectionException, NoSuchSessionException,
                       TimedOutException {
        if (SessionImpl_Debug) {
            debug("SessionImpl: listByteArrayNames.");
        }

        return(po.listByteArrayNames());
    }


/**
 * <A NAME="SD_LISTCHANNELNAMES"></A>
 * <EM>listChannelNames</EM> list the names of the channels that are known
 * to this session.
 *
 * @exception ConnectionException if a connection error occured.
 * @exception NoSuchSessionException if this session does not exist.
 * @exception TimedOutException if no reply was received for this operation
 * in the given timeout period.
 *
 * @return an array of names of the Channels that are known to this Session.
 */

    public String[]
    listChannelNames()
                throws ConnectionException, NoSuchSessionException,
                       TimedOutException {
        if (SessionImpl_Debug) {
            debug("SessionImpl: listChannelNames.");
        }

        return(po.listChannelNames());
    }


/**
 * <A NAME="SD_LISTTOKENNAMES"></A>
 * <EM>listTokenNames</EM> list the names of the tokens that are known
 * to this session.
 *
 * @exception ConnectionException if a connection error occured.
 * @exception NoSuchSessionException if this session does not exist.
 * @exception TimedOutException if no reply was received for this operation
 * in the given timeout period.
 *
 * @return an array of names of Tokens that are known to this Session.
 */

    public String[]
    listTokenNames()
                throws ConnectionException, NoSuchSessionException,
                       TimedOutException {
        if (SessionImpl_Debug) {
            debug("SessionImpl: listTokenNames.");
        }

        return(po.listTokenNames());
    }


/**
 * <A NAME="SD_ADDSESSIONLISTENER"></A>
 * <EM>addSessionListener</EM> add a listener to the list that will be
 * informed when any client undergoes any changes for this Session.
 *
 * @param listener the listener being added.
 *
 * @exception ConnectionException if a connection error occured.
 * @exception NoSuchSessionException if the session doesn't exist.
 * @exception TimedOutException if no reply was received for this operation
 * in the given timeout period.
 */

    public void
    addSessionListener(SessionListener listener)
                throws ConnectionException, NoSuchSessionException,
                       TimedOutException {
        if (SessionImpl_Debug) {
            debug("SessionImpl: addSessionListener:" +
                  " listener: " + listener);
        }

        try {
            po.addListener(listener, objectType);
        } catch (NoSuchSessionException | ConnectionException |
                 TimedOutException nse) {
            throw nse;
        } catch (JSDTException e) {
            error("SessionImpl: addSessionListener: ", e);
        }
    }


/**
 * <A NAME="SD_REMOVESESSIONLISTENER"></A>
 * removes the specified Session listener so that it no longer receives
 * Session events for this Session.
 *
 * @param listener the Session listener.
 *
 * @exception ConnectionException if a connection error occured.
 * @exception NoSuchSessionException if this Session doesn't exist.
 * @exception NoSuchListenerException if this SessionListener doesn't exist.
 * @exception TimedOutException if no reply was received for this operation
 * in the given timeout period.
 */

    public void
    removeSessionListener(SessionListener listener)
                throws ConnectionException, NoSuchSessionException,
                       NoSuchListenerException, TimedOutException {
        if (SessionImpl_Debug) {
            debug("SessionImpl: removeSessionListener:" +
                  " listener: " + listener);
        }

        try {
            mpo.removeListener(listener, objectType);
        } catch (NoSuchSessionException | NoSuchListenerException |
                 ConnectionException | TimedOutException nse) {
            throw nse;
        } catch (JSDTException e) {
            error("SessionImpl: removeSessionListener: ", e);
        }
    }


/**
 * <A NAME="SD_GETURL"></A>
 * gives the URLString used to create this Session.
 *
 * @return the URLString used to create this Session.
 *
 * @since       JSDT 2.1
 */

    public final URLString
    getURL() {
        if (SessionImpl_Debug) {
            debug("SessionProxy: getUrl.");
        }

        return(po.getURL());
    }


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
 * @param closeConnection indicates whether the underlying connection
 * used by this applet or application should be forcefully closed. Forcefully
 * closing this connection would automatically render all references to other
 * Sessions on the "host:port" being used by this Session as invalid.
 *
 * @exception ConnectionException if a connection error occured.
 * @exception NoSuchSessionException if this Session doesn't exist.
 */

    public void
    close(boolean closeConnection)
                throws ConnectionException, NoSuchSessionException {
        if (SessionImpl_Debug) {
            debug("SessionImpl: close:" +
                  " close connection: " + closeConnection);
        }

        po.close(closeConnection);
    }


/**
 * <A NAME="SD_SETNAME"></A>
 * <EM>setName</EM> set the name of this session. This needs to be a separate
 * method as the session is now dynamically created with newInstance(), and
 * therefore can't be done in the constructor. It should only be called by
 * the methods in the SessionFactory class.
 *
 * @param sessionName the name of the session.
 */

    public void
    setName(String sessionName) {
        if (SessionImpl_Debug) {
            debug("SessionImpl: setName:" +
                  " session name: " + sessionName);
        }

        name = sessionName;
    }
}
