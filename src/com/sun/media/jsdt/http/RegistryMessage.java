
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

import com.sun.media.jsdt.event.RegistryEvent;
import com.sun.media.jsdt.event.RegistryListener;
import com.sun.media.jsdt.impl.Message;

/**
 * JSDT connection failed message thread class.
 *
 * @version     2.3 - 28th October 2017
 * @author      Rich Burridge
 */

final class
RegistryMessage extends JSDTMessage implements Runnable {

    /** The Registry event type. */
    private int type;

    /** The listener to inform of the registry state change. */
    private RegistryListener listener;

    /** The registry state change event. */
    private RegistryEvent event;


/**
 * <A NAME="SD_REGISTRYMESSAGE"></A>
 * <EM>RegistryMessage</EM>
 *
 * @param type
 * @param listener
 * @param event
 */

    public
    RegistryMessage(int type, RegistryListener listener, RegistryEvent event) {
        if (RegistryMessage_Debug) {
            debug("RegistryMessage: constructor:" +
                  " type: "     + type +
                  " listener: " + listener +
                  " event: "    + event);
        }

        this.type     = type;
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
        if (RegistryMessage_Debug) {
            debug("RegistryMessage: handleMessage:" +
                  " message: " + message);
        }

        try {
            switch (type) {
                case RegistryEvent.SESSION_CREATED:
                    listener.sessionCreated(event);
                    break;

                case RegistryEvent.SESSION_DESTROYED:
                    listener.sessionDestroyed(event);
                    break;

                case RegistryEvent.CLIENT_CREATED:
                    listener.clientCreated(event);
                    break;

                case RegistryEvent.CLIENT_DESTROYED:
                    listener.clientDestroyed(event);
                    break;

                case RegistryEvent.CONNECTION_FAILED:
                    listener.connectionFailed(event);
                    break;

                default: debug("RegistryMessage: handleMessage:" +
                               " unexpected type: " + type);
            }
        } catch (Throwable th) {
            error("RegistryMessage: handleMessage: ",
                  "impl.thrown", th + " by listener.");
        }
    }


/**
 * <A NAME="SD_RUN"></A>
 * <EM>run</EM>
 */

    public void
    run() {
        if (RegistryMessage_Debug) {
            debug("RegistryMessage: run.");
        }

        handleMessage(null);
    }
}
