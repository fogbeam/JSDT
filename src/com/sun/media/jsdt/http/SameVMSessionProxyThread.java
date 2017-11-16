
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

import com.sun.media.jsdt.Connection;
import com.sun.media.jsdt.TimedOutException;
import com.sun.media.jsdt.impl.*;
import java.io.IOException;

/**
 * JSDT Same VM Session proxy thread class (HTTP implementation).
 *
 * @version     2.3 - 28th October 2017
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
        if (mustPing()) {
            startPingThread(session);
        }
    }


/**
 * <A NAME="SD_WAITFORREPLY"></A>
 * <EM>waitForReply</EM>
 *
 * @exception TimedOutException if no reply was received for this operation
 * in the given timeout period.
 *
 * @return
 */

    public final Message
    waitForReply() throws TimedOutException {
        long startTime = System.currentTimeMillis();
        long currentTime, period;
        long timeoutValue = Util.getLongProperty("timeoutPeriod", timeoutPeriod);

        if (SameVMSessionProxyThread_Debug) {
            debug("SameVMSessionProxyThread: waitForReply.");
        }

        synchronized (this) {
            while (state != FOUND_REPLY) {
                currentTime = System.currentTimeMillis();
                if ((currentTime - startTime) > timeoutValue) {
                    state = GET_MESSAGE;
                    synchronized (waitValueLock) {
                        waitValue = 0;
                    }
                    throw new TimedOutException();
                }
                period = timeoutValue - (currentTime - startTime);
                try {
                    wait(period);
                } catch (InterruptedException ie) {
                }
            }
            state = PROCESSING_REPLY;
        }
        return(message);
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


/**
 * <A NAME="SD_RUN"></A>
 * <EM>run</EM>
 */

    public void
    run() {
        if (SameVMSessionProxyThread_Debug) {
            debug("SameVMSessionProxyThread: run.");
        }

        try {
            message = new Message();
            while (running) {
                boolean found;

                synchronized (this) {
                    while (running &&
                          (state == FOUND_REPLY ||
                           state == PROCESSING_REPLY ||
                           state == SENDING_MESSAGE)) {
                        try {
                            wait();
                        } catch (InterruptedException ie) {
                        }
                    }
                }

                if (!getSocketMessage()) {
                    syncInput();
                    continue;
                }

                if (SameVMSessionProxyThread_Debug) {
                    debug("SameVMSessionProxyThread: run:" +
                          " got a message: " + message);
                }

                found = false;
                synchronized (waitValueLock) {
                    if (waitValue != 0) {
                        long s = (message.id        << 32) +
                                 (message.sessionNo << 16) +
                                 (message.type      <<  8) + message.action;

                        if (SameVMSessionProxyThread_Debug) {
                            debug("SameVMSessionProxyThread: run:" +
                                  " comparing wait value: " + waitValue +
                                  " against: " + s);
                        }

                        if (waitValue == s) {
                            if (SameVMSessionProxyThread_Debug) {
                                debug("SameVMSessionProxyThread: run:" +
                                      " found a match.");
                            }

                            found = true;
                        }
                    }
                }

                if (found) {
                    synchronized (this) {
                        state = FOUND_REPLY;
                        notifyAll();

                        while (state == FOUND_REPLY) {
                            try {
                                wait();
                            } catch (InterruptedException ie) {
                            }
                            notifyAll();
                        }
                    }
                } else {
                    handleMessage(message);
                }
            }
        } catch (Exception e) {
            error("SameVMSessionProxyThread: run: ", e);
        }
    }
}
