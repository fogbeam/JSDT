
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

/**
 * JSDT manager proxy message class.
 *
 * @version     2.3 - 5th November 2017
 * @author      Rich Burridge
 */

final class
ManagerProxyMessage extends JSDTMessage {

    // The client-side managable object for this message.
    private Manageable manageable;

    // The client-side manager associated with this message.
    private JSDTManager manager;


/**
 * <A NAME="SD_MANAGERPROXYMESSAGE"></A>
 * <EM>ManagerProxyMessage</EM>
 *
 * @param session
 * @param manager
 * @param manageable
 */

    public
    ManagerProxyMessage(SessionImpl session, JSDTManager manager,
                        Manageable manageable) {
        if (ManagerProxyMessage_Debug) {
            debug("ManagerProxyMessage: constructor:" +
                  " session: "    + session +
                  " manager: "    + manager +
                  " manageable: " + manageable);
        }

        this.session    = session;
        this.manager    = manager;
        this.manageable = manageable;
    }


/**
 * <A NAME="SD_HANDLEMESSAGE"></A>
 * <EM>handleMessage</EM>
 *
 * @param message
 */

    protected void
    handleMessage(Message message) {
        if (ManagerProxyMessage_Debug) {
            debug("ManagerProxyMessage: handleMessage:" +
                  " message: " + message);
        }

        if (message.type == T_Manager) {
            parseManagerMessage(message);
        } else {
            error("ManagerProxyMessage: handleMessage: ",
                  "impl.unknown.type", message);
        }
    }


/**
 * <A NAME="SD_PARSEMANAGERMESSAGE"></A>
 * <EM>parseManagerMessage</EM>
 *
 * @param message
 */

    private void
    parseManagerMessage(Message message) {
        if (ManagerProxyMessage_Debug) {
            debug("ManagerProxyMessage: parseManagerMessage:" +
                  " message: " + message);
        }

        if (message.action == T_Authenticate) {           /* AUTHENTICATE. */
            AuthenticateClient client = new AuthenticateClient(message,
                                                session, manager, manageable);

            Util.startThread(client, "AuthenticateClientThread:" +
                                     manageable.getName(), true);
        }
    }
}
