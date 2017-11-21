
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

package com.sun.media.jsdt.http;

import com.sun.media.jsdt.Client;
import com.sun.media.jsdt.impl.*;
import com.sun.media.jsdt.event.*;

/**
 * JSDT client message thread class.
 *
 * @version     2.3 - 21st November 2017
 * @author      Rich Burridge
 */

final class
ClientMessage extends JSDTMessage implements Runnable {

    // The client to be notified.
    private final Client client;

    // The type of object the expel/invite message refers to.
    private final char objectType;

    // The name of the object being send a Client message.
    private final String objectName;

    // The message to process.
    private final Message message;


/**
 * <A NAME="SD_CLIENTMESSAGE"></A>
 * <EM>ClientMessage</EM>
 *
 * @param session
 * @param message
 * @param client
 * @param objectType
 * @param objectName
 */

    public
    ClientMessage(SessionImpl session, Message message, Client client,
                  char objectType, String objectName) {
        if (ClientMessage_Debug) {
            debug("ClientMessage: constructor:" +
                  " session: "     + session +
                  " message: "     + message +
                  " client: "      + client +
                  " object type: " + objectType +
                  " object name: " + objectName);
        }

        this.session    = session;
        this.message    = message;
        this.client     = client;
        this.objectType = objectType;
        this.objectName = objectName;
    }


/**
 * <A NAME="SD_HANDLEMESSAGE"></A>
 * <EM>handleMessage</EM>
 *
 * @param message
 */

    protected void
    handleMessage(Message message) {
        if (ClientMessage_Debug) {
            debug("ClientMessage: handleMessage:" +
                  " message: " + message);
        }

        if (client != null) {
            switch (message.action) {
                case T_Invite:                               /* INVITE. */
                    switch (objectType) {
                        case ByteArrayImpl.M_ByteArray:
                            ((ClientListener) client).byteArrayInvited(
                                new ClientEvent(session, client, objectName,
                                        ClientEvent.BYTEARRAY_INVITED));
                            break;
                        case ChannelImpl.M_Channel:
                            ((ClientListener) client).channelInvited(
                                new ClientEvent(session, client, objectName,
                                        ClientEvent.CHANNEL_INVITED));
                            break;
                        case SessionImpl.M_Session:
                            ((ClientListener) client).sessionInvited(
                                new ClientEvent(session, client, objectName,
                                        ClientEvent.SESSION_INVITED));
                            break;
                        case TokenImpl.M_Token:
                            ((ClientListener) client).tokenInvited(
                                new ClientEvent(session, client, objectName,
                                        ClientEvent.TOKEN_INVITED));
                            break;
                    }
                    break;
                case T_Expel:                                /* EXPEL. */
                    switch (objectType) {
                        case ByteArrayImpl.M_ByteArray:
                            ((ClientListener) client).byteArrayExpelled(
                                new ClientEvent(session, client, objectName,
                                        ClientEvent.BYTEARRAY_EXPELLED));
                            break;
                        case ChannelImpl.M_Channel:
                            ((ClientListener) client).channelExpelled(
                                new ClientEvent(session, client, objectName,
                                        ClientEvent.CHANNEL_EXPELLED));
                            break;
                        case SessionImpl.M_Session:
                            ((ClientListener) client).sessionExpelled(
                                new ClientEvent(session, client, objectName,
                                        ClientEvent.SESSION_EXPELLED));
                            break;
                        case TokenImpl.M_Token:
                            ((ClientListener) client).tokenExpelled(
                                new ClientEvent(session, client, objectName,
                                        ClientEvent.TOKEN_EXPELLED));
                            break;
                    }
                    break;
                case T_TokenGiven:                           /* TOKEN GIVEN. */
                    ((ClientListener) client).tokenGiven(
                                new ClientEvent(session, client, objectName,
                                        ClientEvent.TOKEN_GIVEN));
            }
        }
    }


/**
 * <A NAME="SD_RUN"></A>
 * <EM>run</EM>
 */

    public void
    run() {
        if (ClientMessage_Debug) {
            debug("ClientMessage: run.");
        }

        handleMessage(message);
    }
}
