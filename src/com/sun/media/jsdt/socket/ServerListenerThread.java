
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

import com.sun.media.jsdt.impl.JSDTListenerImpl;
import com.sun.media.jsdt.impl.JSDTObject;
import java.util.Hashtable;

/**
 * Server Observer Thread class.
 *
 * @version     2.3 - 29th October 2017
 * @author      Rich Burridge
 */

final class
ServerListenerThread extends JSDTObject implements socketDebugFlags {

    /** The name of this shared object. */
    protected String name;

    /** The listeners associated with this thread. */
    protected Hashtable<String, JSDTListenerImpl> listeners = null;


/**
 * <A NAME="SD_SERVERLISTENERTHREAD"></A>
 * <EM>ServerListenerThread</EM> is a constructor for the Server Listener
 * Thread class. This object keeps a hashtable of listeners associated with
 * this thread for this object.
 *
 * @param name the name of the shared object that this thread is associated
 * with.
 */

    ServerListenerThread(String name) {
        if (ServerListenerThread_Debug) {
            debug("ServerListenerThread: constructor:" +
                  " name: " + name);
        }

        listeners = new Hashtable<>();
    }


/**
 * <A NAME="SD_GETLISTENERS"></A>
 * <EM>getListeners</EM> get a hashtable of the listeners associated with
 * this thread.
 *
 * @return a Hashtable containing the listeners for this thread.
 */

    public Hashtable<String, JSDTListenerImpl>
    getListeners() {
        if (ServerListenerThread_Debug) {
            debug("ServerListenerThread: getListeners.");
        }

        return(listeners);
    }
}
