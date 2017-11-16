
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
import com.sun.media.jsdt.impl.*;
import com.sun.media.jsdt.event.ByteArrayEvent;
import java.io.*;
import java.util.*;

/**
 * JSDT Byte Array client-side proxy class.
 *
 * @version     2.3 - 16th November 2017
 * @author      Rich Burridge
 */

public final class
ByteArrayProxy extends ManageableProxy implements AbstractByteArrayProxy {

    /** The client-side byte array associated with this byte array client. */
    private ByteArrayImpl byteArray;


/**
 * <A NAME="SD_GETPROXY"></A>
 * <EM>getProxy</EM>
 *
 * @return a handle to this byte array client-side proxy.
 */

    public Object
    getProxy() {
        if (ByteArrayProxy_Debug) {
            debug("ByteArrayProxy: getProxy.");
        }

        return(this);
    }


/**
 * <A NAME="SD_INITPROXY"></A>
 * <EM>initProxy</EM> initialise the "client-side" proxy for this ByteArray.
 *
 * @param name the name of the ByteArray proxy being constructed.
 * @param session the client-side session the ByteArray belongs to.
 * @param object the client-side ByteArray.
 */

    public void
    initProxy(String name, SessionImpl session, Object object) {
        if (ByteArrayProxy_Debug) {
            debug("ByteArrayProxy: initProxy:" +
                  " name: "       + name +
                  " session: "    + session +
                  " byte array: " + object);
        }

        this.name      = name;
        this.session   = session;
        this.byteArray = (ByteArrayImpl) object;
        super.initProxy(name, session, object);
    }


/**
 * <A NAME="SD_SETVALUE"></A>
 * <EM>setValue</EM> send a message to the session server to update a
 * shared byte array with the given name for this session. The session server
 * sends a change notification to all the listeners of this shared byte array.
 *
 * @param client a client successfully joined to this session.
 * @param value the new value of the byte array.
 * @param offset the offset into the byte array.
 * @param length the length of the byte array.
 *
 * @exception ConnectionException if a connection error occured.
 * @exception InvalidClientException if the Client is invalid is some way (ie.
 * its getName() method returns null).
 * @exception NoSuchByteArrayException if this byte array does exist.
 * @exception NoSuchClientException if this Client doesn't exist.
 * @exception NoSuchSessionException if this Session doesn't exist.
 * @exception PermissionDeniedException if this Client does not have permission
 * to change the value of this ByteArray.
 * @exception TimedOutException if no reply was received for this operation
 * in the given timeout period.
 */

    public void
    setValue(Client client, byte[] value, int offset, int length)
                throws ConnectionException, InvalidClientException,
                       NoSuchByteArrayException, NoSuchClientException,
                       NoSuchSessionException, PermissionDeniedException,
                       TimedOutException {
        DataInputStream  in;
        int              id         = sp.proxyThread.getId();
        short            sessionNo  = sp.getSessionNo();
        String           clientName = Util.getClientName(client);
        char             type       = ByteArrayImpl.M_ByteArray;
        Message          message;
        int              retval;

        if (ByteArrayProxy_Debug) {
            debug("ByteArrayProxy: setValue:" +
                  " value: "  + value +
                  " offset: " + offset +
                  " length: " + length);
        }

        if (session == null) {
            throw new NoSuchSessionException();
        }

        if (clients.get(clientName) == null) {
            throw new NoSuchClientException();
        }

        try {
            sp.proxyThread.writeMessageHeader(sp.proxyThread.dataOut,
                                   sessionNo, id, type, T_SetValue, true, true);
            sp.proxyThread.dataOut.writeUTF(name);
            sp.proxyThread.dataOut.writeUTF(clientName);
            sp.proxyThread.dataOut.writeInt(length);
            sp.proxyThread.dataOut.write(value, offset, length);
            sp.proxyThread.flush();
            message = sp.proxyThread.waitForReply();

            in     = message.thread.dataIn;
            retval = in.readInt();
            message.thread.finishReply();

            if (retval != 0) {
                switch (retval) {
                    case JSDTException.NO_SUCH_BYTEARRAY:
                        throw new NoSuchByteArrayException();
                    case JSDTException.NO_SUCH_CLIENT:
                        throw new NoSuchClientException();
                    case JSDTException.NO_SUCH_SESSION:
                        session = null;
                        throw new NoSuchSessionException();
                    case JSDTException.PERMISSION_DENIED:
                        throw new PermissionDeniedException();
                    default:
                        error("ByteArrayProxy: setValue: ",
                              "impl.unknown.exception.type", retval);
                }
            }
        } catch (IOException e) {
            sp.proxyThread.finishReply();
            throw new ConnectionException();
        }
    }


/**
 * <A NAME="SD_VALUECHANGED"></A>
 * <EM>valueChanged</EM> the value of a shared byte array in the server for
 * this session has changed. Inform all the proxy listeners.
 *
 * @param message
 */

    void
    valueChanged(Message message) {
        DataInputStream in = message.thread.dataIn;
        String          clientName    = null;
        int             length;
        byte[]          data          = null;
        int             type          = ByteArrayEvent.VALUE_CHANGED;
        ManageableProxy mp = (ManageableProxy) byteArray.mpo.getProxy();

        if (ByteArrayProxy_Debug) {
            debug("ByteArrayProxy: valueChanged:" +
                  " message: " + message);
        }

        try {
            clientName    = in.readUTF();
                            in.readUTF();
            length        = in.readInt();
            data          = message.thread.getData(length);
        } catch (IOException e) {
            error("ByteArrayProxy: valueChanged: ", e);
        }

        synchronized (this) {
            byteArray.setLocalValue(data);

            if (mp.listeners != null) {
                synchronized (mp.listeners) {
                    Enumeration e, k;

                    for (e = mp.listeners.elements(), k = mp.listeners.keys();
                         e.hasMoreElements();) {
                        EventListener listener =
                                        (EventListener) k.nextElement();
                        int mask = (Integer) e.nextElement();

                        if ((mask & type) != 0) {
                            ListenerMessage lm =
                                new ListenerMessage(listener, session,
                                            byteArray.getName(), clientName,
                                            byteArray, type);

                            Util.startThread(lm, "ListenerMessageThread:" +
                                             byteArray.getName(), true);
                        }
                    }
                }
            }
        }
    }
}
