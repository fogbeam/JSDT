
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

package com.sun.media.jsdt.template;

import com.sun.media.jsdt.*;
import com.sun.media.jsdt.impl.*;

/**
 * JSDT Byte Array proxy class.
 *
 * @version     2.3 - 6th November 2017
 * @author      Rich Burridge
 */

public final class
ByteArrayProxy extends ManageableProxy implements AbstractByteArrayProxy {

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
    }


/**
 * <A NAME="SD_GETPROXY"></A>
 * <EM>getProxy</EM> get a handle to the "client-side" proxy for this ByteArray.
 *
 * @return a handle to the "client-side" proxy for this ByteArray.
 */

    public Object
    getProxy() {
        if (ChannelProxy_Debug) {
            debug("ByteArrayProxy: getProxy.");
        }

        return(this);
    }


/**
 * <A NAME="SD_SETVALUE"></A>
 * <EM>setValue</EM> update a shared byte array with the given name for this
 * session.  Send a change notification to all the listeners of this shared
 * byte array.
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
        if (ByteArrayProxy_Debug) {
            debug("ByteArrayProxy: setValue:" +
                  " client: " + client +
                  " value: "  + value +
                  " offset: " + offset +
                  " length: " + length);
        }
    }
}
