
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

import com.sun.media.jsdt.ConnectionException;
import com.sun.media.jsdt.impl.*;
import java.net.UnknownHostException;
import java.util.*;

/**
 * JSDT Session Proxy "ping for message" thread class.
 *
 * @version     2.3 - 16th November 2017
 * @author      Rich Burridge
 */

public final class
PingThread extends HttpThread {

/**
 * <A NAME="SD_PINGTHREAD"></A>
 * <EM>PingThread</EM> the constructor for the PingThread class.
 *
 * @param host
 * @param port
 *
 * @exception UnknownHostException if this host doesn't exist.
 */

    PingThread(String host, int port) throws UnknownHostException {
        super(host, port, false);

        if (PingThread_Debug) {
            debug("PingThread: constructor:" +
                  " host: " + host +
                  " port: " + port) ;
        }

        sessionsById = new Hashtable<>();
        threadsById  = new Hashtable<>();
    }


/**
 * <A NAME="SD_RUN"></A>
 * <EM>run</EM>
 */

    public void
    run() {
        long period = Util.getLongProperty("pingPeriod", pingPeriod);

        if (PingThread_Debug) {
            debug("PingThread: run.");
        }

        try {
            while (true) {
                Thread.sleep(period);

                synchronized (sessionsById) {
                    for (Enumeration k = sessionsById.keys();
                                     k.hasMoreElements();) {
                        Integer     id      = (Integer)     k.nextElement();
                        SessionImpl session = (SessionImpl) sessionsById.get(id);
                        HttpThread  thread  = threadsById.get(id);

                        try {
                            pingForMessage(session, thread, id);
                        } catch (ConnectionException ce) {
                            sessionsById.remove(id);
                                threadsById.remove(id);
                        }
                    }
                }
            }
        } catch (Exception e) {
            error("PingThread: run: ", e);
        }
    }
}
