
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

package com.sun.media.jsdt.socket;

import com.sun.media.jsdt.JSDTException;
import com.sun.media.jsdt.impl.*;
import java.io.*;

/**
 * JSDT session server message thread class.
 *
 * @version     2.3 - 6th November 2017
 * @author      Rich Burridge
 */

final class
SessionServerMessage extends JSDTMessage {

    /* The lock to only allow one server-side message to be processed at
     * any time.
     */
    private static Object serverLock = null;

    // Handle to the SessionServer object for this message.
    private SessionServer ss;


/**
 * <A NAME="SD_SESSIONSERVERMESSAGE"></A>
 * <EM>SessionServerMessage</EM>
 */

    public
    SessionServerMessage() {
        if (SessionServerMessage_Debug) {
            debug("SessionServerMessage: constructor.");
        }

        if (serverLock == null) {
            serverLock = new Object();
        }
    }


/**
 * <A NAME="SD_SETSESSION"></A>
 * <EM>setSession</EM> set the new Session associated with the incoming
 * message, if it differs from the one we've currently got.
 *
 * @param session session associated with incoming server message.
 */

    void
    setSession(SessionImpl session) {
        if (SessionServerMessage_Debug) {
            debug("SessionServerMessage: setSession:" +
                  " session: " + session);
        }

        if (this.session != session) {
            this.session = session;
            ss = (SessionServer) session.so.getServer();
        }
    }


/**
 * <A NAME="SD_HANDLEMESSAGE"></A>
 * <EM>handleMessage</EM>
 *
 * @param message
 */

    protected void
    handleMessage(Message message) {
        if (SessionServerMessage_Debug) {
            debug("SessionServerMessage: handleMessage:" +
                  " message: " + message);
        }

        synchronized (serverLock) {
            switch (message.type) {
                case ByteArrayImpl.M_ByteArray :
                    parseProxyByteArrayMessage(message);
                    break;
                case SessionImpl.M_Session :
                    parseProxySessionMessage(message);
                    break;
                case ChannelImpl.M_Channel :
                    parseProxyChannelMessage(message);
                    break;
                case TokenImpl.M_Token :
                    parseProxyTokenMessage(message);
                    break;
                case ClientImpl.M_Client :
                    parseProxyClientMessage(message);
                    break;
                default :
                    error("SessionServerMessage: handleMessage: ",
                          "impl.unknown.type", message);
            }
        }
    }


/**
 * <A NAME="SD_PARSEPROXYBYTEARRAYMESSAGE"></A>
 * <EM>parseProxyByteArrayMessage</EM>
 *
 * @param message
 */

    private void
    parseProxyByteArrayMessage(Message message) {
        String           byteArrayName;
        ByteArrayImpl    byteArray;
        DataInputStream  in = message.thread.dataIn;

        if (SessionServerMessage_Debug) {
            debug("SessionServerMessage: parseProxyByteArrayMessage:" +
                  " message: " + message);
        }

        try {
            byteArrayName = in.readUTF();
            byteArray     = ss.getByteArrayByName(byteArrayName);

            if (byteArray == null) {
                message.thread.writeMessageHeader(message.thread.dataOut,
                                message.sessionNo, message.id,
                                message.type, message.action, false, true);
                message.thread.dataOut.writeInt(JSDTException.NO_SUCH_BYTEARRAY);
                message.thread.flush();
                message.thread.finishMessage();
            } else {
                ByteArrayServer bas = (ByteArrayServer)
                                                byteArray.so.getServer();

                switch (message.action) {
                    case T_AddListener:                   // ADDLISTENER.
                        bas.addListener(message);
                        break;
                    case T_Authenticate:                  // AUTHENTICATE.
                        bas.authenticateClient(message, byteArrayName);
                        break;
                    case T_ChangeManagerMask:             // CHANGEMANAGERMASK.
                        bas.changeManagerMask(message);
                        break;
                    case T_Expel:                         // EXPEL.
                        bas.expel(message, ByteArrayImpl.M_ByteArray);
                        break;
                    case T_Invite:                        // INVITE.
                        bas.invite(message, ByteArrayImpl.M_ByteArray);
                        break;
                    case T_IsManaged:                     // ISMANAGED.
                        bas.isManaged(message);
                        break;
                    case T_Join:                          // JOIN.
                        bas.join(message);
                        break;
                    case T_Leave:                         // LEAVE.
                        bas.leave(message, in.readUTF());
                        break;
                    case T_ListClientNames:               // LISTCLIENTNAMES.
                        bas.listClientNames(message);
                        break;
                    case T_RemoveListener:                // REMOVELISTENER.
                        bas.removeListener(message);
                        break;
                    case T_SetValue:                      // SETVALUE.
                        bas.setValue(message);
                        break;
                    case T__Manager:                      // _MANAGER.
                        bas.attachManager(message);
                        break;
                    default:
                        error("SessionServerMessage:" +
                              " parseProxyByteArrayMessage: ",
                              "impl.unknown.action", message);
                }
            }
        } catch (IOException e) {
            error("SessionServerMessage: parseProxyByteArrayMessage: ", e);
        }
    }


/**
 * <A NAME="SD_PARSEPROXYCLIENTMESSAGE"></A>
 * <EM>parseProxyClientMessage</EM>
 *
 * @param message
 */

    private void
    parseProxyClientMessage(Message message) {
        if (SessionServerMessage_Debug) {
            debug("SessionServerMessage: parseProxyClientMessage:" +
                  " message: " + message);
        }

        switch (message.action) {
            case T_Authenticate:                   // AUTHENTICATE.
                ss.clientChallenge(message);
                break;
            case T_Challenge:                      // CHALLENGE.
                ss.clientResponse(message);
                break;
            case T_Join:                           // JOIN.
                ss.clientAuthenticate(message);
                break;
            default:
                error("SessionServerMessage: parseProxyClientMessage: ",
                      "impl.unknown.action", message);
        }
    }


/**
 * <A NAME="SD_PARSEPROXYSESSIONMESSAGE"></A>
 * <EM>parseProxySessionMessage</EM>
 *
 * @param message
 */

    private void
    parseProxySessionMessage(Message message) {
        DataInputStream  in  = message.thread.dataIn;

        if (SessionServerMessage_Debug) {
            debug("SessionServerMessage: parseProxySessionMessage:" +
                  " message: " + message);
        }

        try {
            if (session == null) {
                message.thread.writeMessageHeader(message.thread.dataOut,
                                message.sessionNo, message.id,
                                message.type, message.action, false, true);
                message.thread.dataOut.writeInt(JSDTException.NO_SUCH_SESSION);
                message.thread.flush();
                message.thread.finishMessage();
            } else {
                switch (message.action) {
                    case T_AddConnection:          // ADDCONNECTION.
                        ss.addConnection(message);
                        break;
                    case T_AddListener:            // ADDLISTENER.
                        in.readUTF();
                        ss.addListener(message);
                        break;
                    case T_Authenticate:           // AUTHENTICATE.
                        String objectName = in.readUTF();

                        ss.authenticateClient(message, objectName);
                        break;
                    case T_ByteArrayExists:        // BYTEARRAYEXISTS.
                        ss.objectExists(message);
                        break;
                    case T_ByteArraysJoined:       // BYTEARRAYSJOINED.
                        ss.objectsJoined(message);
                        break;
                    case T_ChangeManagerMask:      // CHANGEMANAGERMASK.
                        in.readUTF();
                        ss.changeManagerMask(message);
                        break;
                    case T_ChannelExists:          // CHANNELEXISTS.
                        ss.objectExists(message);
                        break;
                    case T_ChannelsJoined:         // CHANNELSJOINED.
                        ss.objectsJoined(message);
                        break;
                    case T_Close:                  // CLOSE.
                        ss.close(message);
                        break;
                    case T_CreateByteArray:        // CREATEBYTEARRAY.
                        ss.createByteArray(message);
                        break;
                    case T_CreateChannel:          // CREATECHANNEL.
                        ss.createChannel(message);
                        break;
                    case T_CreateToken:            // CREATETOKEN.
                        ss.createToken(message);
                        break;
                    case T_DestroyByteArray:       // DESTROYBYTEARRAY.
                        ss.destroyByteArray(message);
                        break;
                    case T_DestroyChannel:         // DESTROYCHANNEL.
                        ss.destroyChannel(message);
                        break;
                    case T_DestroySession:         // DESTROYSESSION.
                        ss.destroySession(message);
                        break;
                    case T_DestroyToken:           // DESTROYTOKEN.
                        ss.destroyToken(message);
                        break;
                    case T_Expel:                  // EXPEL.
                        in.readUTF();
                        ss.expel(message, SessionImpl.M_Session);
                        break;
                    case T_IsAlive:                // ISALIVE.
                        ss.isAlive(message);
                        break;
                    case T_IsManaged:              // ISMANAGED.
                        ss.isManaged(message);
                        break;
                    case T_Join:                   // JOIN.
                        in.readUTF();
                        ss.join(message);
                        break;
                    case T_Leave:                  // LEAVE.
                        in.readUTF();
                        ss.leave(message, in.readUTF());
                        break;
                    case T_ListByteArrayNames:     // LISTBYTEARRAYNAMES.
                        ss.listNames(message);
                        break;
                    case T_ListChannelNames:       // LISTCHANNELNAMES.
                        ss.listNames(message);
                        break;
                    case T_ListClientNames:        // LISTCLIENTNAMES.
                        in.readUTF();
                        ss.listClientNames(message);
                        break;
                    case T_ListTokenNames:         // LISTTOKENNAMES.
                        ss.listNames(message);
                        break;
                    case T_RemoveConnection:       // REMOVECONNECTION.
                        ss.removeConnection(message);
                        break;
                    case T_RemoveListener:         // REMOVELISTENER.
                        in.readUTF();
                        ss.removeListener(message);
                        break;
                    case T_TokenExists:            // TOKENEXISTS
                        ss.objectExists(message);
                        break;
                    case T_TokensJoined:           // TOKENSJOINED
                        ss.objectsJoined(message);
                        break;
                    case T__Manager:               // _MANAGER.
                        in.readUTF();
                        ss.attachManager(message);
                        break;
                    default:
                        error("SessionServerMessage:" +
                              " parseProxySessionMessage: ",
                              "impl.unknown.action", message);
                }
            }
        } catch (IOException e) {
            error("SessionServerMessage: parseProxySessionMessage: ", e);
        }
    }


/**
 * <A NAME="SD_PARSEPROXYCHANNELMESSAGE"></A>
 * <EM>parseProxyChannelMessage</EM>
 *
 * @param message
 */

    private void
    parseProxyChannelMessage(Message message) {
        String           channelName;
        ChannelImpl      channel;
        DataInputStream  in = message.thread.dataIn;

        if (SessionServerMessage_Debug) {
            debug("SessionServerMessage: parseProxyChannelMessage:" +
                  " message: " + message);
        }

        try {
            channelName = in.readUTF();
            channel     = ss.getChannelByName(channelName);

            if (channel == null) {
                message.thread.writeMessageHeader(message.thread.dataOut,
                                message.sessionNo, message.id,
                                message.type, message.action, false, true);
                message.thread.dataOut.writeInt(JSDTException.NO_SUCH_CHANNEL);
                message.thread.flush();
                message.thread.finishMessage();
            } else {
                ChannelServer cs = (ChannelServer) channel.so.getServer();

                switch (message.action) {
                    case T_AddConsumer:                   // ADDCONSUMER.
                        cs.addConsumer(message);
                        break;
                    case T_AddListener:                   // ADDLISTENER.
                        cs.addListener(message);
                        break;
                    case T_Authenticate:                  // AUTHENTICATE.
                        cs.authenticateClient(message, channelName);
                        break;
                    case T_ChangeManagerMask:             // CHANGEMANAGERMASK.
                        cs.changeManagerMask(message);
                        break;
                    case T_Expel:                         // EXPEL.
                        cs.expel(message, ChannelImpl.M_Channel);
                        break;
                    case T_Invite:                        // INVITE.
                        cs.invite(message, ChannelImpl.M_Channel);
                        break;
                    case T_IsManaged:                     // ISMANAGED.
                        cs.isManaged(message);
                        break;
                    case T_Join:                          // JOIN.
                        cs.join(message);
                        break;
                    case T_Leave:                         // LEAVE.
                        cs.leave(message, in.readUTF());
                        break;
                    case T_ListClientNames:               // LISTCLIENTNAMES.
                        cs.listClientNames(message);
                        break;
                    case T_ListConsumerNames:             // LISTCONSUMERNAMES.
                        cs.listConsumerNames(message);
                        break;
                    case T_RemoveConsumer:                // REMOVECONSUMER.
                        cs.removeConsumer(message);
                        break;
                    case T_RemoveListener:                // REMOVELISTENER.
                        cs.removeListener(message);
                        break;
                    case T_Send:                          // SEND.
                        cs.send(message, channelName);
                        break;
                    case T__Manager:                      // _MANAGER.
                        cs.attachManager(message);
                        break;
                    default:
                        error("SessionServerMessage:" +
                              " parseProxyChannelMessage: ",
                              "impl.unknown.action", message);
                }
            }
        } catch (IOException e) {
            error("SessionServerMessage: parseProxyChannelMessage: ", e);
        }
    }


/**
 * <A NAME="SD_PARSEPROXYTOKENMESSAGE"></A>
 * <EM>parseProxyTokenMessage</EM>
 *
 * @param message
 */

    private void
    parseProxyTokenMessage(Message message) {
        String           tokenName;
        TokenImpl        token;
        DataInputStream  in = message.thread.dataIn;

        if (SessionServerMessage_Debug) {
            debug("SessionServerMessage: parseProxyTokenMessage:" +
                  " message: " + message);
        }

        try {
            tokenName = in.readUTF();
            token     = ss.getTokenByName(tokenName);

            if (token == null) {
                message.thread.writeMessageHeader(message.thread.dataOut,
                                message.sessionNo, message.id,
                                message.type, message.action, false, true);
                message.thread.dataOut.writeInt(JSDTException.NO_SUCH_TOKEN);
                message.thread.flush();
                message.thread.finishMessage();
            } else {
                TokenServer ts = (TokenServer) token.so.getServer();

                switch (message.action) {
                    case T_AddListener:                 // ADDLISTENER.
                        ts.addListener(message);
                        break;
                    case T_Authenticate:                // AUTHENTICATE.
                        ts.authenticateClient(message, tokenName);
                        break;
                    case T_ChangeManagerMask:           // CHANGEMANAGERMASK.
                        ts.changeManagerMask(message);
                        break;
                    case T_Expel:                       // EXPEL.
                        ts.expel(message, TokenImpl.M_Token);
                        break;
                    case T_Give:                        // GIVE.
                        ts.give(message);
                        break;
                    case T_Grab:                        // GRAB.
                        ts.grab(message);
                        break;
                    case T_Invite:                      // INVITE.
                        ts.invite(message, TokenImpl.M_Token);
                        break;
                    case T_IsManaged:                   // ISMANAGED.
                        ts.isManaged(message);
                        break;
                    case T_Join:                        // JOIN.
                        ts.join(message);
                        break;
                    case T_Leave:                       // LEAVE.
                        ts.leave(message, in.readUTF());
                        break;
                    case T_ListClientNames:             // LISTCLIENTNAMES.
                        ts.listClientNames(message);
                        break;
                    case T_ListHolderNames:             // LISTHOLDERNAMES.
                        ts.listHolderNames(message);
                        break;
                    case T_Release:                     // RELEASE.
                        ts.release(message);
                        break;
                    case T_RemoveListener:              // REMOVELISTENER.
                        ts.removeListener(message);
                        break;
                    case T_Request:                     // REQUEST.
                        ts.request(message);
                        break;
                    case T_Test:                        // TEST.
                        ts.test(message);
                        break;
                    case T__Manager:                    // _MANAGER.
                        ts.attachManager(message);
                        break;
                    default:
                        error("SessionServerMessage: parseProxyTokenMessage: ",
                              "impl.unknown.action", message);
                }
            }
        } catch (IOException e) {
            error("SessionServerMessage: parseProxyTokenMessage: ", e);
        }
    }
}
