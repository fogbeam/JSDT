
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
import com.sun.media.jsdt.impl.Message;

/**
 * JSDT client proxy message class.
 *
 * @version     2.3 - 6th November 2017
 * @author      Rich Burridge
 */

final class
ClientProxyMessage extends JSDTMessage {

/**
 * <A NAME="SD_CLIENTPROXYMESSAGE"></A>
 * <EM>ClientProxyMessage</EM>
 *
 * @param client
 */

    public
    ClientProxyMessage(Client client) {
        if (ClientProxyMessage_Debug) {
            debug("ClientProxyMessage: constructor:" +
                  " client: " + client);
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
        if (ClientProxyMessage_Debug) {
            debug("ClientProxyMessage: handleMessage:" +
                  " message: " + message);
        }

        error("ClientProxyMessage: handleMessage: ",
              "impl.unknown.type", message);
    }
}
