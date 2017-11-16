
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

import com.sun.media.jsdt.impl.Message;
import com.sun.media.jsdt.impl.SessionImpl;

/**
 * JSDT Same VM Session proxy thread class (socket implementation).
 *
 * @version     2.3 - 27th October 2017
 * @author      Rich Burridge
 */

final class
SameVMSessionProxyThread extends SameVMThread {

    /** The Session proxy message for this same VM proxy thread. */
    private SessionProxyMessage spm = null;

/**
 * <A NAME="SD_SAMEVMSESSIONPROXYTHREAD"></A>
 * <EM>SameVMSessionProxyThread</EM>
 *
 * @param session
 * @param sessionProxy
 * @param host
 * @param port
 */

    public
    SameVMSessionProxyThread(SessionImpl session, SessionProxy sessionProxy,
                             String host, int port) {
        super(host, port);

        if (SameVMSessionProxyThread_Debug) {
            debug("SameVMSessionProxyThread: constructor:" +
                  " session: "       + session +
                  " session proxy: " + sessionProxy +
                  " host: "          + host +
                  " port: "          + port);
        }

        spm = new SessionProxyMessage(session, sessionProxy);
    }


/**
 * <A NAME="SD_HANDLEMESSAGE"></A>
 * <EM>handleMessage</EM> process the next message.
 *
 * @param message the next message to be processed.
 */

    public void
    handleMessage(Message message) {
        if (SameVMSessionProxyThread_Debug) {
            debug("SameVMSessionProxyThread: handleMessage:" +
                  " message: " + message);
        }

        spm.handleMessage(message);
    }
}
