
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

import com.sun.media.jsdt.*;
import com.sun.media.jsdt.impl.*;
import java.io.*;

/**
 * JSDT Client Server-side class.
 *
 * @version     2.3 - 21st November 2017
 * @author      Rich Burridge
 */

final class
ClientServer extends JSDTObject implements httpDebugFlags {

    // The name of this client server.
    protected String name = null;

    // The server-side client associated with this client server.
    protected final Client client;

    // Server socket instance for this client.
    private TCPSocketServer socketServer;


/**
 * <A NAME="SD_CLIENTSERVER"></A>
 * <EM>ClientServer</EM>
 *
 * @param client
 * @param name
 * @param port
 *
 * @exception PortInUseException if this port is being used by another
 * application.
 */

    public
    ClientServer(Client client, String name, int port)
                throws PortInUseException {
        if (ClientServer_Debug) {
            debug("ClientServer: constructor:" +
                  " client: " + client +
                  " name: "   + name +
                  " port: "   + port);
        }

        this.client  = client;
        this.name    = name;
        socketServer = new TCPSocketServer(port);
        socketServer.addClient(client);

        Util.startThread(socketServer, "ClientServerThread:" + port, false);
    }


/**
 * <A NAME="SD_PARSEPROXYCLIENTMESSAGE"></A>
 * <EM>parseProxyClientMessage</EM>
 *
 * @param message
 */

    void
    parseProxyClientMessage(Message message) {
        int retval = 0;

        if (ClientServer_Debug) {
            debug("ClientServer: parseProxyClientMessage:" +
                  " message: " + message);
        }

        switch (message.action) {
            case T_Invite:                             // INVITE.
                String          objectName;
                ClientMessage   cm;
                DataInputStream in      = message.thread.dataIn;
                SessionImpl     session = null;

                try {
                    objectName = in.readUTF();

                    try {
                        session = (SessionImpl)
                                    SessionFactory.createSession(client,
                                            new URLString(objectName), false);
                    } catch (JSDTException je) {
                        error("ClientServer: parseProxyClientMessage: ", je);
                    }

                    cm = new ClientMessage(session, message, client,
                                           SessionImpl.M_Session, objectName);
                    Util.startThread(cm,
                                     "ClientMessageThread:" + client.getName() +
                                     ":" + objectName, false);

                    message.thread.writeMessageHeader(message.thread.dataOut,
                                    message.sessionNo, message.id,
                                    message.type, message.action, false, true);
                    message.thread.dataOut.writeInt(retval);
                    message.thread.flush();
                    message.thread.finishMessage();
                } catch (IOException e) {
                    error("ClientServer: parseProxyClientMessage: ", e);
                }
                break;
            case T_DestroyClient:                       // DESTROY CLIENT.
                socketServer.close();

                try {
                    message.thread.writeMessageHeader(message.thread.dataOut,
                                    message.sessionNo, message.id,
                                    message.type, message.action, false, true);
                    message.thread.dataOut.writeInt(retval);
                    message.thread.flush();
                    message.thread.finishMessage();
                } catch (IOException e) {
                    error("SessionServer: close: ", e);
                }
                break;
            default:
                error("ClientServer: parseProxyClientMessage: ",
                      "impl.unknown.action", message);
        }
    }
}
