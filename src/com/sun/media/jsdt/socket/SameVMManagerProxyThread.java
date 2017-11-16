
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

/**
 * JSDT Same VM Manager proxy thread class.
 *
 * @version     2.3 - 27th October 2017
 * @author      Rich Burridge
 */

final class
SameVMManagerProxyThread extends SameVMThread {

    /** The Manager proxy message associated with this proxy thread. */
    private ManagerProxyMessage mpm = null;


/**
 * <A NAME="SD_SAMEVMMANAGERPROXYTHREAD"></A>
 * <EM>SameVMManagerProxyThread</EM>
 *
 * @param session
 * @param manageable
 * @param manager
 * @param host
 * @param port
 */

    public
    SameVMManagerProxyThread(SessionImpl session, Manageable manageable,
                       JSDTManager manager, String host, int port) {
        super(host, port);

        if (SameVMManagerProxyThread_Debug) {
            debug("SameVMManagerProxyThread: constructor:" +
                  " session: "    + session +
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
        if (SameVMManagerProxyThread_Debug) {
            debug("SameVMManagerProxyThread: handleMessage:" +
                  " message: " + message);
        }

        mpm.handleMessage(message);
    }
}
