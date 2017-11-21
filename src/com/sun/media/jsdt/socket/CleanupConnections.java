
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

import com.sun.media.jsdt.impl.*;
import java.util.*;

/**
 * JSDT cleanup connections thread class.
 *
 * @version     2.3 - 20th November 2017
 * @author      Rich Burridge
 * @author      Manfred N. Riem
 */

final class
CleanupConnections extends JSDTObject implements Runnable, socketDebugFlags {

    // The session server that started this thread.
    private final SessionServer sessionServer;

    // The latest proxy ping times, keyed by id.
    private Hashtable<Integer, Long> pingIds = null;

    // The threads currently pinging, keyed by id.
    private Hashtable<Integer, JSDTThread> pingThreads = null;

    // A boolean to signal if we should shutdown the thread.
    private boolean shutdown = false;

/**
 * <A NAME="SD_CLEANUPCONNECTIONS"></A>
 * <EM>CleanupConnections</EM> is a constructor for the CleanupConnections
 * class. This thread will continually check to see if we haven't heard from
 * any of the ids in the given cleanup period. If we haven't, then we need
 * to cleanup all resources associated with that id.
 *
 * @param sessionServer the session server that started this ping thread.
 */

    public
    CleanupConnections(SessionServer sessionServer) {
        if (CleanupConnections_Debug) {
            debug("CleanupConnections: constructor:" +
                  " session server: " + sessionServer);
        }

        this.sessionServer = sessionServer;
        pingIds            = new Hashtable<>();
        pingThreads        = new Hashtable<>();
    }


/**
 * <A NAME="SD_ADDPINGENTRY"></A>
 * <EM>addPingEntry</EM>
 */

    synchronized void
    addPingEntry(int id, JSDTThread thread) {
        Integer idObject = id;

        if (CleanupConnections_Debug) {
            debug("CleanupConnections: addPingEntry:" +
                  " id: "     + id +
                  " thread: " + thread);
        }

        pingIds.put(idObject, System.currentTimeMillis());
        pingThreads.put(idObject, thread);
    }


/**
 * <A NAME="SD_CHECKCONNECTIONS"></A>
 * <EM>checkConnections</EM>
 */

    synchronized void
    removePingEntry(int id) {
        Integer idObject = id;

        if (CleanupConnections_Debug) {
            debug("CleanupConnections: addPingEntry:" +
                  " id: "     + id);
        }

        pingIds.remove(idObject);
        pingThreads.remove(idObject);
    }


/**
 * <A NAME="SD_CHECKCONNECTIONS"></A>
 * <EM>checkConnections</EM>
 */

    private void
    checkConnections() {
        long period = Util.getLongProperty("cleanupPeriod", cleanupPeriod);
        Enumeration e, k;

        if (CleanupConnections_Debug) {
            debug("CleanupConnections: checkConnections.");
        }

        synchronized (pingIds) {
            long currentTime = System.currentTimeMillis();

            for (e = pingIds.elements(),
                 k = pingIds.keys(); e.hasMoreElements();) {
                Integer idObject     = (Integer) k.nextElement();
                long    lastPingTime = (Long) e.nextElement();

                if (currentTime - lastPingTime > period) {
                    SocketThread thread =
                                    (SocketThread) pingThreads.get(idObject);

                    sessionServer.removeThread(thread);
                    removePingEntry(idObject);
                }
            }
        }
    }


/**
 * <A NAME="SD_RUN"></A>
 * <EM>run</EM>
 */

    public void
    run() {
        if (CleanupConnections_Debug) {
            debug("CleanupConnections: run.");
        }

        while (!shutdown) {
            checkConnections();

            try {
                Thread.sleep(keepAlivePeriod);
            } catch (InterruptedException ie) {
            }
        }

        if (CleanupConnections_Debug) {
            debug("CleanupConnections: run exiting.");
        }
    }

    /**
     * Sets if we should shutdown the run method. <p>
     *
     * @param shutdown <b>true</b> if we should shutdown, <b>false</b> if not.
     */
    void setShutdown(boolean shutdown) {
        this.shutdown = shutdown;
    }
}
