
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

import java.io.ByteArrayInputStream;

/**
 * JSDT Byte Array Input Stream class.
 *
 * @version     2.3 - 6th November 2017
 * @author      Rich Burridge
 */

public final class
JSDTByteArrayInputStream extends ByteArrayInputStream
                         implements JSDTDebugFlags {

    public
    JSDTByteArrayInputStream() {
        super(new byte[0]);     /* Have to give the superclass something. */

        if (JSDTByteArrayInputStream_Debug) {
            JSDTObject.Debug("JSDTByteArrayInputStream: constructor.");
        }
    }


    public synchronized void
    setByteArray(byte[] bytes, int off, int len) {
        if (JSDTByteArrayInputStream_Debug) {
            JSDTObject.Debug("JSDTByteArrayInputStream: setByteArray:" +
                             " bytes: "  + bytes +
                             " offset: " + off +
                             " length: " + len);
        }

        buf   = bytes;
        pos   = off;
        count = Math.min(off + len, buf.length);
    }
}
