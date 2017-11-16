
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

/**
 * JSDT client server message class.
 *
 * @version     2.3 - 28th October 2017
 * @author      Rich Burridge
 */

final class
ClientServerMessage extends JSDTMessage {

    /** The client associated with this client server message. */
    private Client client;


/**
 * <A NAME="SD_CLIENTSERVERMESSAGE"></A>
 * <EM>ClientServerMessage</EM>
 *
 * @param client
 */

    public
    ClientServerMessage(Client client) {
        if (ClientServerMessage_Debug) {
            debug("ClientServerMessage: constructor:" +
                  " client: " + client);
        }

        this.client = client;
    }


/**
 * <A NAME="SD_HANDLEMESSAGE"></A>
 * <EM>handleMessage</EM>
 *
 * @param message
 */

    protected void
    handleMessage(Message message) {
        char action = message.action;
        char type   = message.type;

        if (ClientServerMessage_Debug) {
            debug("ClientServerMessage: handleMessage:" +
                  " message: " + message);
        }

        if ((type == SessionImpl.M_Session && action == T_Invite) ||
            (type == ClientImpl.M_Client   && action == T_DestroyClient)) {
            ((httpClient) client).cs.parseProxyClientMessage(message);
        } else {
            error("ClientServerMessage: handleMessage: ",
                  "impl.unknown.type", message);
        }
    }
}
