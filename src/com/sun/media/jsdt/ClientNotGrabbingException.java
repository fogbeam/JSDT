
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

package com.sun.media.jsdt;

import com.sun.media.jsdt.impl.JSDTObject;

/**
 * Thrown to indicate that an attempt has been made to release a Token that
 * had not been previously grabbed.
 *
 * @version     2.3 - 26th October 2017
 * @author      Rich Burridge
 * @since       JSDT 1.0
 */

public class
ClientNotGrabbingException extends JSDTException {

    public
    ClientNotGrabbingException() {
        super(JSDTException.CLIENT_NOT_GRABBING);

        if (ClientNotGrabbingException_Debug) {
            JSDTObject.Debug("ClientNotGrabbingException: constructor.");
        }
    }
}
