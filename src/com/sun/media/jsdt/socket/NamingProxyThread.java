
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

import com.sun.media.jsdt.impl.Message;
import com.sun.media.jsdt.impl.SessionImpl;
import java.net.*;

/**
 * JSDT Naming proxy thread class (socket implementation).
 *
 * @version     2.3 - 27th October 2017
 * @author      Rich Burridge
 */

final class
NamingProxyThread extends TCPSocketThread {

    /** The Naming proxy message associated with this proxy thread. */
    private NamingProxyMessage npm = null;


/**
 * <A NAME="SD_NAMINGPROXYTHREAD"></A>
 * <EM>NamingProxyThread</EM>
 *
 * @param namingProxy
 * @param host
 * @param port
 */

    public
    NamingProxyThread(NamingProxy namingProxy, String host, int port)
                throws SocketException, UnknownHostException {
        super(host, port);

        if (NamingProxyThread_Debug) {
            debug("NamingProxyThread: constructor:" +
                  " naming proxy: " + namingProxy +
                  " host: "         + host +
                  " port: "         + port);
        }

        npm = new NamingProxyMessage(namingProxy);
    }


/**
 * <A NAME="SD_HANDLEMESSAGE"></A>
 * <EM>handleMessage</EM> process the next message.
 *
 * @param message the next message to be processed.
 */

    public void
    handleMessage(Message message) {
        if (NamingProxyThread_Debug) {
            debug("NamingProxyThread: handleMessage:" +
                  " message: " + message);
        }

        npm.handleMessage(message);
    }
}
