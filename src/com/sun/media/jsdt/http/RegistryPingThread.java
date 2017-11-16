
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

import com.sun.media.jsdt.*;
import com.sun.media.jsdt.event.RegistryListener;
import com.sun.media.jsdt.impl.*;
import java.io.*;
import java.net.UnknownHostException;
import java.util.*;

/**
 * JSDT Naming Proxy "ping registry for messages" thread class.
 *
 * @version     2.3 - 7th November 2017
 * @author      Rich Burridge
 */

public final class
RegistryPingThread extends HttpThread {

    // The listeners interested in registry events.
    final Vector<RegistryListener> listeners;

    // The NamingProxyThread that started this registry ping thread.
    NamingProxyThread thread;


/**
 * <A NAME="SD_REGISTRYPINGTHREAD"></A>
 * <EM>RegistryPingThread</EM> the constructor for the RegistryPingThread class.
 *
 * @param host
 * @param port
 * @param thread
 *
 * @exception UnknownHostException if this host doesn't exist.
 */

    RegistryPingThread(String host, int port,
                       NamingProxyThread thread) throws UnknownHostException {
        super(host, port, false);

        if (RegistryPingThread_Debug) {
            debug("RegistryPingThread: constructor:" +
                  " host: "  + host +
                  " port: "  + port +
                  " thread: "+ thread);
        }

        this.thread = thread;
        listeners   = new Vector<>();
    }


/**
 * <A NAME="SD_ADDENTRY"></A>
 * <EM>addEntry</EM>
 *
 * @param listener
 */

    void
    addEntry(RegistryListener listener) {
        if (RegistryPingThread_Debug) {
            debug("RegistryPingThread: addEntry:" +
                  " listener: " + listener);
        }

        synchronized (listeners) {
            listeners.addElement(listener);
        }
    }


/**
 * <A NAME="SD_REMOVEENTRY"></A>
 * <EM>removeEntry</EM>
 *
 * @param listener
 */

    void
    removeEntry(RegistryListener listener) {
        if (RegistryPingThread_Debug) {
            debug("RegistryPingThread: removeEntry:" +
                  " listener: " + listener);
        }

        synchronized (listeners) {
            listeners.removeElement(listener);
        }
    }


/**
 * <A NAME="SD_HASLISTENERS"></A>
 * <EM>hasListeners</EM>
 */

    boolean
    hasListeners() {
        if (RegistryPingThread_Debug) {
            debug("RegistryPingThread: hasListeners.");
        }

        return(listeners.size() != 0);
    }


    private void
    pingForMessage() throws TimedOutException {
        Message message;
        int     retval;
        int     length = 0;

        if (RegistryPingThread_Debug) {
            debug("RegistryPingThread: pingForMessage.");
        }

        try {
            writeMessageHeader(dataOut, (short) 1, getId(),
                               T_Registry, T_GetMessage, true, true);
            dataOut.writeInt(thread.getId());
            flush();
            message = waitForReply();

            in      = message.thread.dataIn;
            retval  = message.thread.dataIn.readInt();
            if (retval == 0) {
                length  = message.thread.dataIn.readInt();
                if (length != 0) {
                    byteIn.setByteArray(message.thread.getData(length),
                                        0, length);
                    dataIn = new DataInputStream(byteIn);
                }
            }

            message.thread.finishReply();

            if (retval != 0) {
                error("RegistryPingThread: pingForMessage: ",
                      "impl.unknown.exception.type", retval);
            }

            if (length != 0) {
                message.getMessageHeader(this);
                thread.handleMessage(message);
                dataIn = new DataInputStream(new BufferedInputStream(in));
            }
        } catch (IOException e) {
            finishReply();
        }
    }


/**
 * <A NAME="SD_RUN"></A>
 * <EM>run</EM>
 */

    public void
    run() {
        long period = Util.getLongProperty("registryPingPeriod",
                                           registryPingPeriod);

        if (RegistryPingThread_Debug) {
            debug("RegistryPingThread: run.");
        }

        try {
            while (true) {
                Thread.sleep(period);

                pingForMessage();
            }
        } catch (Exception e) {
            error("RegistryPingThread: run: ", e);
        }
    }
}
