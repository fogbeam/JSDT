
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

package com.sun.media.jsdt.impl;

import java.util.EventListener;

/**
 * JSDT Listener implementation.
 *
 * @version     2.3 - 20th November 2017
 * @author      Rich Burridge
 */

public final class
JSDTListenerImpl extends JSDTObject {

    /** The name of this JSDT listener.
     *
     *  @serial
     */
    private final String name;

    /** The JSDT manageable listener associated with this object.
     *
     *  @serial
     */
    private final EventListener listener;


/**
 * <A NAME="SD_JSDTLISTENERIMPL"></A>
 * <EM>JSDTListenerImpl</EM> the constructor for the JSDTListenerImpl class.
 *
 * @param name the name of the listener being constructed.
 */

    public
    JSDTListenerImpl(String name, EventListener listener) {
        if (JSDTListenerImpl_Debug) {
            debug("JSDTListenerImpl: constructor:" +
                  " name : "    + name +
                  " listener: " + listener);
        }

        this.name     = name;
        this.listener = listener;
    }


/**
 * <A NAME="SD_GETLISTENER"></A>
 * <EM>getListener</EM> get the real listener associated with this object.
 *
 * @return the listener.
 */

    public EventListener
    getListener() {
        if (JSDTListenerImpl_Debug) {
            debug("JSDTListenerImpl: getListener.");
        }

        return(listener);
    }


/**
 * <A NAME="SD_GETNAME"></A>
 * <EM>getName</EM> get the name of this listener.
 *
 * @return the name of the listener.
 */

    public String
    getName() {
        if (JSDTListenerImpl_Debug) {
            debug("JSDTListenerImpl: getName.");
        }

        return(name);
    }
}
