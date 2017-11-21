
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

/**
 * JSDT Manager implementation.
 *
 * @version     2.3 - 20th November 2017
 * @author      Rich Burridge
 */

public class
JSDTManagerImpl extends JSDTObject {

    /** The name of this manager.
     *
     *  @serial
     */
    protected final String name;


/**
 * <A NAME="SD_JSDTMANAGERIMPL"></A>
 * <EM>JSDTManagerImpl</EM> the constructor for the JSDTManagerImpl class.
 *
 * @param name the name of the manager being constructed.
 */

    public
    JSDTManagerImpl(String name) {
        if (JSDTManagerImpl_Debug) {
            debug("JSDTManagerImpl: constructor:" +
                  " name: " + name);
        }

        this.name = name;
    }


/**
 * <A NAME="SD_GETNAME"></A>
 * <EM>getName</EM> get the name of this manager.
 *
 * @return the name of the manager.
 */

    public String
    getName() {
        if (JSDTManagerImpl_Debug) {
            debug("JSDTManagerImpl: getName.");
        }

        return(name);
    }
}
