
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

import com.sun.media.jsdt.impl.JSDTListenerImpl;
import com.sun.media.jsdt.impl.JSDTObject;
import java.util.Hashtable;

/**
 * Server Listener Id class.
 *
 * @version     2.3 - 29th October 2017
 * @author      Rich Burridge
 */

final class
ServerListenerId extends JSDTObject implements httpDebugFlags {

    /** The name of this shared object. */
    protected String name;

    /** The listeners associated with this object for this id. */
    protected Hashtable<String, JSDTListenerImpl> listeners = null;


/**
 * <A NAME="SD_SERVERLISTENERID"></A>
 * <EM>ServerListenerId</EM> is a constructor for the Server Listener
 * Id class. This object keeps a hashtable of listeners associated with
 * this object for this id.
 *
 * @param name the name of the shared object that this id is associated with.
 */

    ServerListenerId(String name) {
        if (ServerListenerId_Debug) {
            debug("ServerListenerId: constructor:" +
                  " name: " + name);
        }

        listeners = new Hashtable<>();
    }


/**
 * <A NAME="SD_GETLISTENERS"></A>
 * <EM>getListeners</EM> get a hashtable of the listeners associated with
 * this object for this id.
 *
 * @return a Hashtable containing the listeners for this id.
 */

    public Hashtable<String, JSDTListenerImpl>
    getListeners() {
        if (ServerListenerId_Debug) {
            debug("ServerListenerId: getListeners.");
        }

        return(listeners);
    }
}
