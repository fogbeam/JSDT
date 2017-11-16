
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

import com.sun.media.jsdt.impl.*;

/**
 * JSDT Byte Array server class.
 *
 * @version     2.3 - 6th November 2017
 * @author      Rich Burridge
 */

public final class
ByteArrayServer extends ManageableServer implements AbstractByteArrayServer {

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
    }


/**
 * <A NAME="SD_GETSERVER"></A>
 * <EM>getServer</EM> get a handle to the "server-side" for this ByteArray.
 *
 * @return the "server-side" for this ByteArray.
 */

    public Object
    getServer() {
        if (ByteArrayServer_Debug) {
            debug("ByteArrayServer: getServer.");
        }

        return(this);
    }
}
