
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
import com.sun.media.jsdt.impl.*;

/**
 * JSDT Same VM Manager proxy thread class.
 *
 * @version     2.3 - 7th November 2017
 * @author      Rich Burridge
 */

final class
SameVMManagerProxyThread extends SameVMThread {

    // The Manager proxy message associated with this proxy thread.
    private ManagerProxyMessage mpm = null;


/**
 * <A NAME="SD_SAMEVMMANAGERPROXYTHREAD"></A>
 * <EM>SameVMManagerProxyThread</EM>
 *
 * @param session
 * @param manageable
 * @param manager
 * @param host
 * @param port
 */

    public
    SameVMManagerProxyThread(SessionImpl session, Manageable manageable,
                       JSDTManager manager, String host, int port) {
        super(host, port);

        if (SameVMManagerProxyThread_Debug) {
            debug("SameVMManagerProxyThread: constructor:" +
                  " session: "    + session +
                  " manageable: " + manageable +
                  " manager: "    + manager +
                  " host: "       + host +
                  " port: "       + port);
        }

        mpm = new ManagerProxyMessage(session, manager, manageable);
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
        long timeoutValue = Util.getLongProperty("timeoutPeriod",
                                                 timeoutPeriod);

        if (SameVMManagerProxyThread_Debug) {
            debug("SameVMManagerProxyThread: waitForReply.");
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
 * <EM>handleMessage</EM>
 *
 * @param message
 */

    public void
    handleMessage(Message message) {
        if (SameVMManagerProxyThread_Debug) {
            debug("SameVMManagerProxyThread: handleMessage:" +
                  " message: " + message);
        }

        mpm.handleMessage(message);
    }


/**
 * <A NAME="SD_RUN"></A>
 * <EM>run</EM>
 */

    public void
    run() {
        if (SameVMManagerProxyThread_Debug) {
            debug("SameVMManagerProxyThread: run.");
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

                if (SameVMManagerProxyThread_Debug) {
                    debug("SameVMManagerProxyThread: run:" +
                          " got a message: " + message);
                }

                if (message.type == ClientImpl.M_Client &&
                    message.action == T_Authenticate) {
                    Integer idValue = message.id;

                    while (!idThreads.containsKey(idValue)) {
                        try {
                            synchronized (this) {
                                wait();
                            }
                        } catch (InterruptedException ie) {
                        }
                    }

                    synchronized (idThreads) {
                        idThreads.remove(idValue);
                    }

                    synchronized (clientResponseLock) {
                        clientResponseLock.notifyAll();
                    }
                    continue;
                }

                found = false;
                synchronized (waitValueLock) {
                    if (waitValue != 0) {
                        long s = ((long) message.id << 32) +
                                 (message.sessionNo << 16) +
                                 (message.type      <<  8) + message.action;

                        if (SameVMManagerProxyThread_Debug) {
                            debug("SameVMManagerProxyThread: run:" +
                                  " comparing wait value: " + waitValue +
                                  " against: " + s);
                        }

                        if (waitValue == s) {
                            if (SameVMManagerProxyThread_Debug) {
                                debug("SameVMManagerProxyThread: run:" +
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
            error("SameVMManagerProxyThread: run: ", e);
        }
    }
}
