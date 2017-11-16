
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
import com.sun.media.jsdt.impl.Message;
import java.net.*;

/**
 * JSDT Client proxy thread class.
 *
 * @version     2.3 - 27th October 2017
 * @author      Rich Burridge
 */

final class
ClientProxyThread extends TCPSocketThread {

    /** The Client-side client associated with this thread. */
    private Client client;


/**
 * <A NAME="SD_CLIENTPROXYTHREAD"></A>
 * <EM>ClientProxyThread</EM>
 *
 * @param client
 * @param host
 * @param port
 */

    public
    ClientProxyThread(Client client, String host, int port)
                throws SocketException, UnknownHostException {
        super(host, port);

        if (ClientProxyThread_Debug) {
            debug("ClientProxyThread: constructor:" +
                  " host: "   + host +
                  " port: "   + port +
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

    public void
    handleMessage(Message message) {
        ClientProxyMessage cpm = new ClientProxyMessage(client);

        if (ClientProxyThread_Debug) {
            debug("ClientProxyThread: handleMessage:" +
                  " message: " + message);
        }

        cpm.handleMessage(message);
    }
}
