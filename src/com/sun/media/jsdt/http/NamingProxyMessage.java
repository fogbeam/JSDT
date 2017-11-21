
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

import com.sun.media.jsdt.URLString;
import com.sun.media.jsdt.impl.*;
import java.io.*;

/**
 * JSDT Naming proxy message class.
 *
 * @version     2.3 - 21st November 2017
 * @author      Rich Burridge
 */

final class
NamingProxyMessage extends JSDTObject implements httpDebugFlags {

    /* The lock to only allow one proxy-side message to be processed at
     * any time.
     */
    private static Object proxyLock = null;

    // Handle to the NamingProxy object for this message.
    private final NamingProxy np;


/**
 * <A NAME="SD_NAMINGPROXYMESSAGE"></A>
 * <EM>NamingProxyMessage</EM>
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
        if (NamingProxyMessage_Debug) {
            debug("NamingProxyMessage: handleMessage:" +
                  " message: " + message);
        }

        synchronized (proxyLock) {
            switch (message.action) {
                case T_Permanent:               /* PERMANENT. */
                    DataInputStream in = message.thread.dataIn;

/* The registry will send back a reply to the T_Permanent message sent by the
 * NamingPermThread. Just ignore it.
 */

                    try {
                        in.readInt();           // Return value (always 0).
                    } catch (IOException e) {
                        error("NamingProxyMessage: handleMessage: ", e);
                    }
                    np.proxyThread.havePermanent = true;
                    message.thread.finishReply();
                    break;
                case T_InformListener:              /* INFORMLISTENER. */
                    informListeners(message);
                    break;
            }
        }
    }


/**
 * <A NAME="SD_INFORMLISTENERS"></A>
 * <EM>informListeners</EM>
 *
 * @param message
 */

    final void
    informListeners(Message message) {
        URLString       resourceName = null;
        String          clientName   = null;
        int             type         = 0;
        DataInputStream in           = message.thread.dataIn;

        if (NamingProxyMessage_Debug) {
            debug("NamingProxyMessage: informListeners:" +
                  " message: " + message);
        }

        try {
            resourceName = new URLString(in.readUTF());
            clientName   = in.readUTF();
            type         = in.readInt();
        } catch (IOException e) {
            error("NamingProxyMessage: informListeners: ", e);
        }

        np.informListeners(clientName, resourceName,  type);
    }
}
