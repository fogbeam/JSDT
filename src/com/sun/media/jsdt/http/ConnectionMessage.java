
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

import com.sun.media.jsdt.event.ConnectionEvent;
import com.sun.media.jsdt.event.ConnectionListener;
import com.sun.media.jsdt.impl.Message;

/**
 * JSDT connection failed message thread class.
 *
 * @version     2.3 - 21st November 2017
 * @author      Rich Burridge
 */

final class
ConnectionMessage extends JSDTMessage implements Runnable {

    // The listener to inform of the connection failure.
    private final ConnectionListener listener;

    // The Connection failure event.
    private final ConnectionEvent event;


/**
 * <A NAME="SD_CONNECTIONMESSAGE"></A>
 * <EM>ConnectionMessage</EM>
 *
 * @param listener
 * @param event
 */

    public
    ConnectionMessage(ConnectionListener listener, ConnectionEvent event) {
        if (ConnectionMessage_Debug) {
            debug("ConnectionMessage: constructor:" +
                  " listener: " + listener +
                  " event: "    + event);
        }

        this.listener = listener;
        this.event    = event;
    }


/**
 * <A NAME="SD_HANDLEMESSAGE"></A>
 * <EM>handleMessage</EM>
 *
 * @param message
 */

    protected void
    handleMessage(Message message) {
        if (ConnectionMessage_Debug) {
            debug("ConnectionMessage: handleMessage:" +
                  " message: " + message);
        }

        try {
            listener.connectionFailed(event);
        } catch (Throwable th) {
            error("ConnectionMessage: handleMessage: ",
                  "impl.thrown", th + " by listener.");
        }
    }


/**
 * <A NAME="SD_RUN"></A>
 * <EM>run</EM>
 */

    public void
    run() {
        if (ConnectionMessage_Debug) {
            debug("ConnectionMessage: run.");
        }

        handleMessage(null);
    }
}
