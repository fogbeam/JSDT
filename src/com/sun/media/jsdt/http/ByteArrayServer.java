
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

import com.sun.media.jsdt.JSDTException;
import com.sun.media.jsdt.impl.*;
import com.sun.media.jsdt.ByteArray;
import com.sun.media.jsdt.event.ByteArrayEvent;
import java.io.*;
import java.util.*;

/**
 * JSDT Byte Array Server-side class.
 *
 * @version     2.3 - 30th October 2017
 * @author      Rich Burridge
 */

public final class
ByteArrayServer extends ManageableServer implements AbstractByteArrayServer {

    // The server-side session associated with this byte array server.
    private SessionImpl session;

    // The session server associated with this byte array server.
    private SessionServer ss;

    // The server-side byte array associated with this byte array server.
    private ByteArrayImpl byteArray;

/**
 * <A NAME="SD_INITSERVER"></A>
 * <EM>initServer</EM> initialise the "server-side" for this ByteArray.
 *
 * @param name the name of the server ByteArray being constructed.
 * @param session the server-side session the ByteArray belongs to.
 * @param object the server-side ByteArray.
 */

    public void
    initServer(String name, SessionImpl session, Object object) {
        if (ByteArrayServer_Debug) {
            debug("ByteArrayServer: initServer:" +
                  " name: "       + name +
                  " session: "    + session +
                  " byte array: " + object);
        }

        this.name       = name;
        this.session    = session;
        this.byteArray  = (ByteArrayImpl) object;
        clients         = new Hashtable<>();
        ss              = (SessionServer) session.so.getServer();
        super.initServer(name, session, object);
    }


/**
 * <A NAME="SD_GETSERVER"></A>
 * <EM>getServer</EM>
 */

    public Object
    getServer() {
        if (ByteArrayServer_Debug) {
            debug("ByteArrayServer: getServer.");
        }

        return(this);
    }


/**
 * <A NAME="SD_ADDJOINEDCLIENT"></A>
 * <EM>addJoinedClient</EM> add this client to the hashtable of clients
 * currently joined to this byte array. This byte array is also added to
 * the vector of byte arrays that this client is joined to for this session,
 * to make any later cleanup easier.
 *
 * @param client the client to be added.
 */

    private void
    addJoinedClient(ClientImpl client) {
        String            clientName       = client.getName();
        ClientImpl        ci               = ss.getClientByName(clientName);
        Vector<ByteArray> clientByteArrays = ci.getByteArrays();

        if (ByteArrayServer_Debug) {
            debug("ByteArrayServer: addJoinedClient:" +
                  " client: " + client);
        }

        clients.put(client.getName(), client);
        clientByteArrays.addElement(byteArray);
    }


/**
 * <A NAME="SD_INFORMBYTEARRAYLISTENERS"></A>
 * <EM>informByteArrayListeners</EM> the value of a shared byte array in the
 * server for this session has changed. Inform all the byte array listeners.
 *
 * @param thread the thread to write this message out on.
 * @param sba the shared byte array whose value has just changed.
 * @param clientName name of the client associated with this session.
 * @param byteArrayName the name of the shared byte array to update.
 * @param value the new value of the byte array.
 */

    private void
    informByteArrayListeners(JSDTThread thread, ByteArrayImpl sba,
                     String clientName, String byteArrayName, byte[] value) {
        if (ByteArrayServer_Debug) {
            debug("ByteArrayServer: informByteArrayListeners:" +
                  " thread: "            + thread +
                  " server byte array: " + sba +
                  " client name: "       + clientName +
                  " byte array name: "   + byteArrayName +
                  " value: "             + value);
        }

        for (Enumeration k = listenerIds.keys(); k.hasMoreElements();) {
            int id = (Integer) k.nextElement();

            try {
                thread.writeMessageHeader(thread.dataOut, ss.getSessionNo(),
                                          id, ByteArrayImpl.M_ByteArray,
                                          T_ValueChanged, false, false);
                thread.dataOut.writeUTF(name);
                thread.dataOut.writeUTF(clientName);
                thread.dataOut.writeUTF(byteArrayName);
                thread.dataOut.writeInt(value.length);
                thread.dataOut.write(value, 0, value.length);
                thread.flush();
                thread.finishMessage();
            } catch (IOException oe) {
                error("ByteArrayServer: informByteArrayListeners: ", oe);
            }
        }
    }


/**
 * <A NAME="SD_JOIN"></A>
 * <EM>join</EM>
 *
 * @param message
 */

    void
    join(Message message) {
        DataInputStream  in         = message.thread.dataIn;
        int              retval     = 0;
        String           clientName = null;
        ClientImpl       client     = null;

        if (ByteArrayServer_Debug) {
            debug("ByteArrayServer: join:" +
                  " message: " + message);
        }

        try {
            clientName = in.readUTF();
            client     = new ClientImpl(clientName, message.id);
        } catch (IOException e) {
            error("ByteArrayServer: join: ", e);
        }

        if (!((SessionServer) session.so).validClient(client)) {
            retval = JSDTException.NO_SUCH_CLIENT;
        } else {
            if (clients.containsKey(clientName)) {
                retval = JSDTException.NAME_IN_USE;
            } else {
                addJoinedClient(client);
            }
        }

        try {
            message.thread.writeMessageHeader(message.thread.dataOut,
                                message.sessionNo, message.id,
                                message.type, message.action, false, true);
            message.thread.dataOut.writeInt(retval);
            message.thread.flush();
            message.thread.finishMessage();
        } catch (IOException e) {
            error("ByteArrayServer: join: ", e);
        }

        if (retval == 0) {
            informListeners(message.thread, session.getName(),
                            clientName, name,
                            ByteArrayEvent.JOINED, message.type);
            addClientIdConnection(clientName, message.id);
        }
    }


/**
 * <A NAME="SD_EXPEL"></A>
 * <EM>expel</EM>
 *
 * @param message
 * @param objectType
 */

    protected void
    expel(Message message, char objectType) {
        if (ByteArrayServer_Debug) {
            debug("ByteArrayServer: expel:" +
                  " message: "     + message +
                  " object type: " + objectType);
        }

        super.expel(message, objectType, this);
    }


/**
 * <A NAME="SD_LEAVE"></A>
 * <EM>leave</EM> the given client is leaving this byte array.
 *
 * @param message the current message being processed.
 * @param clientName the name of the client leaving this byte array.
 * @param isCleanup set if we had a client leave a Session (either
 * deliberately or the result of a lost connection), and we are tidying up
 * it's bytearray connections.
 */

    protected void
    leave(Message message, String clientName, boolean isCleanup) {
        ClientImpl client;

        if (ByteArrayServer_Debug) {
            debug("ByteArrayServer: leave:" +
                  " message: "     + message +
                  " client name: " + clientName +
                  " isCleanup: "   + isCleanup);
        }

        if ((client = getClientByName(clientName)) != null) {
            removeJoinedClient(client);
        }
        super.leave(message, session.getName(),
                    ByteArrayImpl.M_ByteArray, clientName, isCleanup);
    }


/**
 * <A NAME="SD_REMOVEJOINEDCLIENT"></A>
 * <EM>removeJoinedClient</EM> remove this byte array from the vector of
 * byte arrays that this client has joined.
 *
 * @param client the client to be removed.
 */

    private void
    removeJoinedClient(ClientImpl client) {
        String        clientName       = client.getName();
        SessionServer ss               = (SessionServer) session.so.getServer();
        ClientImpl    ci               = ss.getClientByName(clientName);
        Vector        clientByteArrays = ci.getByteArrays();

        if (ByteArrayServer_Debug) {
            debug("ByteArrayServer: removeJoinedClient:" +
                  " client: " + client);
        }

        clientByteArrays.removeElement(byteArray);
    }


/**
 * <A NAME="SD_SETVALUE"></A>
 * <EM>setValue</EM> updates a shared byte array in the server for this
 * session. A message is returned to the calling proxy, to indicate
 * the success or failure of this operation. The server sends a change
 * notification to all the listeners of this shared byte array.
 *
 * @param message
 */

    void
    setValue(Message message) {
        DataInputStream  in         = message.thread.dataIn;
        String           clientName = null;
        int              length;
        byte[]           value      = null;
        int              retval     = 0;
        int              clientId;
        ClientImpl       client;
        ByteArrayImpl    byteArray  = null;

        if (ByteArrayServer_Debug) {
            debug("ByteArrayServer: setValue:" +
                  " message: " + message);
        }

        try {
            clientName = in.readUTF();
            length     = in.readInt();
            value      = message.thread.getData(length);
        } catch (IOException e) {
            error("ByteArrayServer: setValue: ", e);
        }

        if ((client = getClientByName(clientName)) == null) {
            retval = JSDTException.NO_SUCH_CLIENT;
        }

        clientId = (Integer) client.getCheck();
        if (clientId != message.id) {
            retval = JSDTException.PERMISSION_DENIED;
        }

        if (retval == 0) {
            if ((byteArray = ss.getByteArrayByName(name)) != null) {
                byteArray.setLocalValue(value);
            } else {
                retval = JSDTException.NO_SUCH_BYTEARRAY;
            }
        }

        try {
            message.thread.writeMessageHeader(message.thread.dataOut,
                                message.sessionNo, message.id,
                                message.type, message.action, false, true);
            message.thread.dataOut.writeInt(retval);
            message.thread.flush();
            message.thread.finishMessage();
        } catch (IOException e) {
            error("ByteArrayServer: setValue: ", e);
        }

        if (retval == 0) {
            informByteArrayListeners(message.thread, byteArray, clientName,
                                     name, value);
        }
    }
}
