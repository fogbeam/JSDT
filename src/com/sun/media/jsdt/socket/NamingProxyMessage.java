
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

import com.sun.media.jsdt.URLString;
import com.sun.media.jsdt.impl.*;
import java.io.*;

/**
 * JSDT Naming proxy message class.
 *
 * @version     2.3 - 20th November 2017
 * @author      Rich Burridge
 */

final class
NamingProxyMessage extends JSDTObject implements socketDebugFlags {

    /* The lock to only allow one proxy-side message to be processed at
     * any time.
     */
    private static Object proxyLock = null;

    // Handle to the NamingProxy object for this message.
    private final NamingProxy np;


/**
 * <A NAME="SD_SESSIONPROXYMESSAGE"></A>
 * <EM>SessionProxyMessage</EM>
 *
 * @param namingProxy
 */

    public
    NamingProxyMessage(NamingProxy namingProxy) {
        if (NamingProxyMessage_Debug) {
            debug("NamingProxyMessage: constructor:" +
                  " naming proxy: " + namingProxy);
        }

        if (proxyLock == null) {
            proxyLock = new Object();
        }

        np = namingProxy;
    }


/**
 * <A NAME="SD_HANDLEMESSAGE"></A>
 * <EM>handleMessage</EM>
 *
 * @param message
 */

    protected void
    handleMessage(Message message) {
        URLString resourceName = null;
        String    clientName   = null;
        int       type         = 0;

        if (NamingProxyMessage_Debug) {
            debug("NamingProxyMessage: handleMessage:" +
                  " message: " + message);
        }

        synchronized (proxyLock) {
            DataInputStream in = message.thread.dataIn;

            try {
                resourceName = new URLString(in.readUTF());
                clientName   = in.readUTF();
                type         = in.readInt();
            } catch (IOException e) {
                error("NamingProxyMessage: handleMessage: ", e);
            }

            np.informListeners(clientName, resourceName,  type);
        }
    }
}
