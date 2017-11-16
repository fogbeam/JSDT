
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

package com.sun.media.jsdt;

import com.sun.media.jsdt.impl.JSDTObject;

/**
 * Thrown when an attempt was made to use a URL with the SessionFactory or
 * Naming classes, which is not in the required format.
 *
 * <PRE>
 * JSDT URL's have the following format:
 *
 *     jsdt://<host>:<port>/<impl type>/<object type>/<objectname>
 *
 * where:
 *
 * <host> is the name of the machine serving this JSDT object.
 * <port> is the port number to use on that machine.
 * <impl type> is the implementation type (eg. "socket"),
 * <object type> is either "Session" or "Client".
 * <objectName> is the name of the JSDT object.
 *
 * For example:
 *     jsdt://stard:3355/socket/Session/chatSession
 *
 *     jsdt://stard:4386/socket/Client/fredClient
 * </PRE>
 *
 * @version     2.3 - 26th October 2017
 * @author      Rich Burridge
 * @since       JSDT 1.0
 */

public class
InvalidURLException extends JSDTException {

    public
    InvalidURLException() {
        super(JSDTException.INVALID_URL);

        if (InvalidURLException_Debug) {
            JSDTObject.Debug("InvalidURLException: constructor.");
        }
    }
}
