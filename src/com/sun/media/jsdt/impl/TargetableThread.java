
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

import java.io.Serializable;
import java.lang.Thread;

/**
 * A Thread class that is targetable. In other words, this class retains
 * a handle to the Runnable object that was used to create it. This is so
 * that that thread can be shutdown gracefully.
 *
 * @version     2.3 - 20th November 2017
 * @author      Rich Burridge
 * @since       JSDT 2.2
 */

public class
TargetableThread extends Thread implements JSDTDebugFlags, Serializable {

    /** Handle to runnaqble target associated with this thread. */
    private final Runnable target;

/**
 * <A NAME="SD_TARGETABLETHREAD"></A>
 * <EM>TargetableThread</EM>
 *
 * @param target the object whose <code>run</code> method is called.
 * @param name the name of the new thread.
 */

    public
    TargetableThread(Runnable target, String name) {
        super(target, name);

        if (TargetableThread_Debug) {
            JSDTObject.Debug("TargetableThread: constructor:" +
                  " target: " + target +
                  " name: "   + name);
        }

        this.target = target;
    }


/**
 * <A NAME="SD_GETTARGET"></A>
 * <EM>getTarget</EM>
 *
 * @return the runnable target associated with this Thread.
 */

    public Runnable
    getTarget() {
        if (TargetableThread_Debug) {
            JSDTObject.Debug("TargetableThread: getTarget: " +
                  "target: " + target);
        }

        return(target);
    }
}
