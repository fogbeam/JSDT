
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

import com.sun.media.jsdt.*;
import com.sun.media.jsdt.impl.JSDTObject;
import com.sun.media.jsdt.impl.Util;
import java.net.*;

/**
 * JSDT Client client-side proxy class.
 *
 * @version     2.3 - 5th November 2017
 * @author      Rich Burridge
 */

final class
ClientProxy extends JSDTObject implements socketDebugFlags {

    // The name of this client.
    protected String name = null;

    // The client-side client associated with this client client.
    protected Client client;

    // Proxy thread for this client.
    ClientProxyThread proxyThread;


/**
 * <A NAME="SD_CLIENTPROXY"></A>
 * <EM>ClientProxy</EM>
 *
 * @param client
 * @param url the JSDT URL string associated with this client.
 * @param name
 * @param host
 * @param port
 *
 * @exception NoSuchHostException if the given host does not exist.
 */

    public
    ClientProxy(Client client, String url, String name, String host, int port)
                throws NoSuchHostException {
        if (ClientProxy_Debug) {
            debug("ClientProxy: constructor:" +
                  " client: " + client +
                  " url: "    + url +
                  " name: "   + name +
                  " host: "   + host +
                  " port: "   + port);
        }

        this.client = client;
        this.name   = name;

        try {
             proxyThread = new ClientProxyThread(client, host, port);
        } catch (SocketException e) {
            error("ClientProxy: constructor: ", e);
        } catch (UnknownHostException uhe) {
            throw new NoSuchHostException();
        }

        Util.startThread(proxyThread,
                         "ClientProxyThread:" + client.getName(), true);
    }

/*  Note that nothing should be sending messages to the client client-side
 *  object, hence no parseServerClientMessage(tok, thread) method declaration.
 */

}
