
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
import com.sun.media.jsdt.impl.Message;
import com.sun.media.jsdt.impl.SessionImpl;
import java.net.*;

/**
 * JSDT Manager proxy thread class.
 *
 * @version     2.3 - 27th October 2017
 * @author      Rich Burridge
 */

final class
ManagerProxyThread extends TCPSocketThread {

    /** The Manager proxy message associated with this proxy thread. */
    private ManagerProxyMessage mpm = null;


/**
 * <A NAME="SD_MANAGERPROXYTHREAD"></A>
 * <EM>ManagerProxyThread</EM>
 *
 * @param session
 * @param manageable
 * @param manager
 * @param host
 * @param port
 */

    public
    ManagerProxyThread(SessionImpl session, Manageable manageable,
                       JSDTManager manager, String host, int port)
                throws SocketException, UnknownHostException {
        super(host, port);

        if (ManagerProxyThread_Debug) {
            debug("ManagerProxyThread: constructor:" +
                  " manageable: " + manageable +
                  " manager: "    + manager +
                  " host: "       + host +
                  " port: "       + port);
        }

        mpm = new ManagerProxyMessage(session, manager, manageable);
    }


/**
 * <A NAME="SD_HANDLEMESSAGE"></A>
 * <EM>handleMessage</EM>
 *
 * @param message
 */

    public void
    handleMessage(Message message) {
        if (ManagerProxyThread_Debug) {
            debug("ManagerProxyThread: handleMessage:" +
                  " message: " + message);
        }

        mpm.handleMessage(message);
    }
}
