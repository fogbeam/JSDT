
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

import com.sun.media.jsdt.Client;
import com.sun.media.jsdt.impl.*;
import com.sun.media.jsdt.event.ClientListener;
import java.io.*;

/**
 * JSDT session proxy message class.
 *
 * @version     2.3 - 20th November 2017
 * @author      Rich Burridge
 */

final class
SessionProxyMessage extends JSDTMessage {

    /* The lock to only allow one proxy-side message to be processed at
     * any time.
     */
    private static Object proxyLock = null;

    // Handle to the SessionProxy object for this message.
    private final SessionProxy sp;


/**
 * <A NAME="SD_SESSIONPROXYMESSAGE"></A>
 * <EM>SessionProxyMessage</EM>
 *
 * @param session
 * @param sessionProxy
 */

    public
    SessionProxyMessage(SessionImpl session, SessionProxy sessionProxy) {
        if (SessionProxyMessage_Debug) {
            debug("SessionProxyMessage: constructor:" +
                  " session: "       + session +
                  " session proxy: " + sessionProxy);
        }

        if (proxyLock == null) {
            proxyLock = new Object();
        }

        this.session = session;
        sessionName  = session.getName();
        sp           = sessionProxy;
    }


/**
 * <A NAME="SD_HANDLEMESSAGE"></A>
 * <EM>handleMessage</EM>
 *
 * @param message
 */

    protected void
    handleMessage(Message message) {
        if (SessionProxyMessage_Debug) {
            debug("SessionProxyMessage: handleMessage:" +
                  " message: " + message);
        }

        synchronized (proxyLock) {
            switch (message.type) {
                case ByteArrayImpl.M_ByteArray:
                    parseServerByteArrayMessage(message);
                    break;
                case ChannelImpl.M_Channel:
                    parseServerChannelMessage(message);
                    break;
                case ClientImpl.M_Client:
                    parseServerClientMessage(message);
                    break;
                case SessionImpl.M_Session:
                    parseServerSessionMessage(message);
                    break;
                case TokenImpl.M_Token:
                    parseServerTokenMessage(message);
                    break;
                default:
                    error("SessionProxyMessage: handleMessage: ",
                          "impl.unknown.type", message);
            }
        }
    }


/**
 * <A NAME="SD_PARSESERVERBYTEARRAYMESSAGE"></A>
 * <EM>parseServerByteArrayMessage</EM>
 *
 * @param message
 */

    private void
    parseServerByteArrayMessage(Message message) {
        DataInputStream in            = message.thread.dataIn;
        String          byteArrayName = null;
        ByteArrayImpl   byteArray     = null;

        if (SessionProxyMessage_Debug) {
            debug("SessionProxyMessage: parseServerByteArrayMessage:" +
                  " message: " + message);
        }

        try {
            byteArrayName = in.readUTF();
            byteArray     = sp.getByteArrayByName(byteArrayName);
        } catch (IOException e) {
            error("SessionProxyMessage: parseServerByteArrayMessage: ", e);
        }

        if (byteArray == null) {
            error("SessionProxyMessage: parseServerByteArrayMessage: ",
                  "impl.cannot.find", byteArrayName);
        } else {
            switch (message.action) {
                case T_InformListener:                   /* INFORMLISTENER. */
                    informListeners(message, byteArray, byteArrayName);
                    break;
                case T_ValueChanged:                     /* VALUECHANGED. */
                    ByteArrayProxy bp = (ByteArrayProxy)
                                                byteArray.po.getProxy();

                    bp.valueChanged(message);
                    break;
                default:
                    waited("SessionProxyMessage: parseServerByteArrayMessage: ",
                           message.action);
            }
        }
    }


/**
 * <A NAME="SD_PARSESERVERCLIENTMESSAGE"></A>
 * <EM>parseServerClientMessage</EM>
 *
 * @param message
 */

    private void
    parseServerClientMessage(Message message) {
        if (SessionProxyMessage_Debug) {
            debug("SessionProxyMessage: parseServerClientMessage:" +
                  " message: " + message);
        }

        if (message.action == T_Challenge) {           /* CHALLENGE. */
            clientChallenge(message);
        } else if (message.action == T_TokenGiven) {   /* TOKEN GIVEN. */
            DataInputStream in         = message.thread.dataIn;
            String          tokenName  = null;
            String          clientName;
            Client          client     = null;

            try {
                tokenName  = in.readUTF();
                clientName = in.readUTF();
                client     = sp.getClientByName(clientName);
            } catch (IOException e) {
                error("SessionProxyMessage: parseServerClientMessage: ", e);
            }

            if (client instanceof ClientListener) {
                ClientMessage cm = new ClientMessage(session, message, client,
                                                TokenImpl.M_Token, tokenName);

                Util.startThread(cm, "ClientMessageThread:" + client.getName() +
                                     ":" + tokenName, true);
            }
        } else {                                       /* INVITE and EXPEL. */
            DataInputStream in         = message.thread.dataIn;
            char            objectType = 0;
            String          objectName = null;
            String          clientName;
            Client          client     = null;

            try {
                objectType = in.readChar();
                objectName = in.readUTF();
                clientName = in.readUTF();
                client     = sp.getClientByName(clientName);
            } catch (IOException e) {
                error("SessionProxyMessage: parseServerClientMessage: ", e);
            }

            if (client instanceof ClientListener) {
                ClientMessage cm = new ClientMessage(session, message,
                                            client, objectType, objectName);

                Util.startThread(cm, "ClientMessageThread:" + client.getName() +
                                     ":" + objectName, true);
            }
        }
    }


/**
 * <A NAME="SD_PARSESERVERCHANNELMESSAGE"></A>
 * <EM>parseServerChannelMessage</EM>
 *
 * @param message
 */

    private void
    parseServerChannelMessage(Message message) {
        DataInputStream in          = message.thread.dataIn;
        String          channelName = null;
        ChannelImpl     channel     = null;

        if (SessionProxyMessage_Debug) {
            debug("SessionProxyMessage: parseServerChannelMessage:" +
                  " message: " + message);
        }

        try {
            channelName = in.readUTF();
            channel     = sp.getChannelByName(channelName);
        } catch (IOException e) {
            error("SessionProxyMessage: parseServerChannelMessage: ", e);
        }

        if (channel == null) {
            error("SessionProxyMessage: parseServerChannelMessage: ",
                  "impl.cannot.find", channelName);
        } else {
            switch (message.action) {
                case T_DataReceived:                     /* DATARECEIVED. */
                    ChannelProxy cp = (ChannelProxy) channel.po.getProxy();

                    cp.dataReceived(message, channelName);
                    break;
                case T_InformListener:                   /* INFORMLISTENER. */
                    informListeners(message, channel, channelName);
                    break;
                default:
                    waited("SessionProxyMessage: parseServerChannelMessage: ",
                           message.action);
            }
        }
    }


/**
 * <A NAME="SD_PARSESERVERSESSIONMESSAGE"></A>
 * <EM>parseServerSessionMessage</EM>
 *
 * @param message
 */

    private void
    parseServerSessionMessage(Message message) {
        String          resourceName = null;
        DataInputStream in           = message.thread.dataIn;

        if (SessionProxyMessage_Debug) {
            debug("SessionProxyMessage: parseServerSessionMessage:" +
                  " message: " + message);
        }

        switch (message.action) {
            case T_InformListener:          /* INFORMLISTENER. */
                try {
                    resourceName = in.readUTF();
                } catch (IOException e) {
                    error("SessionProxyMessage:" +
                          " parseServerSessionMessage: ", e);
                }
                informListeners(message, session, resourceName);
                break;
            case T_Close:          /* INFORMLISTENER. */

/* Depending upon where the SocketThread: run() method currently was when the
 * SocketThread: terminate() method, was called, it's possible that we might
 * get a "reply" back from the server for the SessionProxy: close() operation.
 * We just catch it here, and discard it.
 */

                try {
                    in.readInt();           // Return value (always 0).
                } catch (IOException e) {
                    error("SessionProxyMessage:" +
                          " parseServerSessionMessage: ", e);
                }
                break;
            default:
                waited("SessionProxyMessage: parseServerSessionMessage: ",
                       message.action);
        }
    }


/**
 * <A NAME="SD_PARSESERVERTOKENMESSAGE"></A>
 * <EM>parseServerTokenMessage</EM>
 *
 * @param message
 */

    private void
    parseServerTokenMessage(Message message) {
        DataInputStream in        = message.thread.dataIn;
        String          tokenName = null;
        TokenImpl       token     = null;

        if (SessionProxyMessage_Debug) {
            debug("SessionProxyMessage: parseServerTokenMessage:" +
                  " message: " + message);
        }

        try {
            tokenName = in.readUTF();
            token     = sp.getTokenByName(tokenName);
        } catch (IOException e) {
            error("SessionProxyMessage: parseServerTokenMessage: ", e);
        }

        if (token == null) {
            error("SessionProxyMessage: parseServerTokenMessage: ",
                  "impl.cannot.find", tokenName);
        } else {
            switch (message.action) {
                case T_InformListener:               /* INFORMLISTENER. */
                    informListeners(message, token, tokenName);
                    break;
                default:
                    waited("SessionProxyMessage: parseServerTokenMessage: ",
                           message.action);
            }
        }
    }
}
