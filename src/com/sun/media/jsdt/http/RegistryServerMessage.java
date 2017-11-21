
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

import com.sun.media.jsdt.impl.Message;

/**
 * JSDT Registry server message class.
 *
 * @version     2.3 - 20th November 2017
 * @author      Rich Burridge
 */

final class
RegistryServerMessage extends JSDTMessage {

    // Handle to server-side registry object.
    private final Registry registry;


/**
 * <A NAME="SD_REGISTRYSERVERMESSAGE"></A>
 * <EM>RegistryServerMessage</EM>
 *
 * @param registry
 * @param thread
 */

    public
    RegistryServerMessage(Registry registry, RegistryServerThread thread) {
        if (RegistryServerMessage_Debug) {
            debug("RegistryServerMessage: constructor:" +
                  " registry: " + registry +
                  " thread: "   + thread);
        }

        this.registry = registry;
    }


/**
 * <A NAME="SD_HANDLEMESSAGE"></A>
 * <EM>handleMessage</EM>
 *
 * @param message
 */

    protected void
    handleMessage(Message message) {
        if (RegistryServerMessage_Debug) {
            debug("RegistryServerMessage: handleMessage:" +
                  " message: " + message);
        }

        switch (message.action) {
            case T_Bind:
                registry.bind(message);
                break;
            case T_GetSessionNo:
                registry.getSessionNo(message);
                break;
            case T_Unbind:
                registry.unbind(message);
                break;
            case T_Lookup:
                registry.lookup(message);
                break;
            case T_List:
                registry.list(message);
                break;
            case T_Exists:
                registry.exists(message);
                break;
            case T_AddListener:
                registry.addListener(message);
                break;
            case T_RemoveListener:
                registry.removeListener(message);
                break;
            case T_IsAlive:
                registry.isAlive(message);
                break;
            case T_GetMessage:
                registry.getMessage(message);
                break;
            case T_Permanent:
                registry.permanent(message);
                break;
            case T_IsManaged:
                registry.isManaged(message);
                break;
            case T_Authenticate:
                registry.authenticate(message);
                break;
            case T_Challenge:
                registry.challenge(message);
                break;
            case T_Stop:
                registry.stop(message);
                break;
        }
    }
}
