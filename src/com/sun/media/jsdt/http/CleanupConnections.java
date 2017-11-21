
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

import com.sun.media.jsdt.impl.*;
import java.util.*;

/**
 * JSDT cleanup connections thread class.
 *
 * @version     2.3 - 21st November 2017
 * @author      Rich Burridge
 */

final class
CleanupConnections extends JSDTObject implements Runnable, httpDebugFlags {

    // The session server that started this thread.
    private final SessionServer sessionServer;

    // The ids currently pinging, and their last ping times.
    private final Hashtable pingIds;


/**
 * <A NAME="SD_CLEANUPCONNECTIONS"></A>
 * <EM>CleanupConnections</EM> is a constructor for the CleanupConnections
 * class. This thread will continually check to see if we haven't heard from
 * any of the ids in the given cleanup period. If we haven't, then we need
 * to cleanup all resources associated with that id.
 *
 * @param sessionServer the session server that started this ping thread.
 * @param pingIds the ids currently pinging, and their last ping times.
 */

    public
    CleanupConnections(SessionServer sessionServer, Hashtable pingIds) {
        if (CleanupConnections_Debug) {
            debug("CleanupConnections: constructor:" +
                  " session server: " + sessionServer +
                  " ping ids: "       + pingIds);
        }

        this.sessionServer = sessionServer;
        this.pingIds       = pingIds;
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
                int     id           = idObject;
                long    lastPingTime = ((Long) e.nextElement());

                if (currentTime - lastPingTime > period) {
                    sessionServer.removeId(id);
                    pingIds.remove(idObject);
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

        while (true) {
            checkConnections();

            try {
                Thread.sleep(keepAlivePeriod);
            } catch (InterruptedException ie) {
            }
        }
    }
}
