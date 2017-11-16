
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

import com.sun.media.jsdt.TimedOutException;
import java.io.*;
import java.util.*;

/**
 * JSDT Thread interface.
 *
 * @version     2.3 - 16th November 2017
 * @author      Rich Burridge
 */

public abstract class
JSDTThread extends JSDTObject implements JSDTDebugFlags {

    // States that we can be in when processing a message.
    protected static final int GET_MESSAGE       = 1;
    protected static final int WAITING_FOR_REPLY = 2;
    protected static final int FOUND_REPLY       = 3;
    protected static final int PROCESSING_REPLY  = 4;
    protected static final int SENDING_MESSAGE   = 5;

    // The current state of message processing.
    protected volatile int state = GET_MESSAGE;

    // The current message that is being waited for.
    public Message message = new Message();

    /* Calculated value for the command the client is waiting to complete
     * (if any).
     */
    protected volatile long waitValue = 0;

    // Object used with waitValue for synchronization purposes.
    protected Object waitValueLock = new Object();

    // The address of the server machine.
    protected String address;

    // The port number to communicate on.
    protected int port;

    // Allocated ids for this VM.
    private static Vector<Integer> ids = null;

    // A unique id for this JSDT thread.
    public int id = createId();

    // Indicates whether this JSDT thread should be running.
    protected boolean running = true;

    // The data input stream associated with this socket.
    public DataInputStream dataIn;

    // The data output stream associated with this socket.
    public DataOutputStream dataOut;


/**
 * <A NAME="SD_CLEANUPCONNECTION"></A>
 * <EM>cleanupConnection</EM>
 */

    public abstract void
    cleanupConnection();


/**
 *  <A NAME="SD_CREATEID"></A>
 * <EM>createId</EM> create a unique ID value for this JSDTThread.
 *
 * @return a unique id for this JSDTThread.
 */

    private static int
    createId() {
        boolean found = false;
        int     id    = 0;
        Integer value;

        if (JSDTThread_Debug) {
            Debug("JSDTThread: createId.");
        }

        if (ids == null) {
            ids = new Vector<>();
        }

        while (!found) {
            id    = new Random().nextInt();
            value = id;

            if (!ids.contains(value)) {
                ids.addElement(value);
                found = true;
            }
        }

        return(id);
    }


/**
 * <A NAME="SD_FINISHMESSAGE"></A>
 * <EM>finishMessage</EM>
 */

    public void
    finishMessage() {
        if (JSDTThread_Debug) {
            debug("JSDTThread: finishMessage.");
        }

        try {
            synchronized (this) {
                while (state == FOUND_REPLY) {
                    try {
                        wait();
                    } catch (InterruptedException ie) {
                    }
                }

                state = GET_MESSAGE;
                notifyAll();
            }
        } catch (IllegalMonitorStateException e) {
            error("JSDTThread: finishMessage: ", e);
        }
    }


/**
 * <A NAME="SD_FINISHREPLY"></A>
 * <EM>finishReply</EM>
 */

    public synchronized void
    finishReply() {
        if (JSDTThread_Debug) {
            debug("JSDTThread: finishReply.");
        }

        try {
            state = GET_MESSAGE;
            synchronized (waitValueLock) {
                waitValue = 0;
            }
            notifyAll();
        } catch (IllegalMonitorStateException e) {
            error("JSDTThread: finishReply: ", e);
        }
    }


/**
 * <A NAME="SD_FLUSH"></A>
 * <EM>flush</EM> flush the message written to this socket.
 *
 * @exception IOException if an IO exception has occured.
 */

    public abstract void
    flush() throws IOException;


/**
 * <A NAME="SD_GETDATA"></A>
 * <EM>getData</EM>
 *
 * @param length
 *
 * @return the data read in an array of bytes.
 *
 * @exception IOException if an IO exception has occured.
 */

    public final byte[]
    getData(int length) throws IOException {
        byte[] data      = new byte[length];
        int    remaining = length;
        int    off       = 0;

        if (JSDTThread_Debug) {
            debug("JSDTThread: getData:" +
                  " length: " + length);
        }

        while (remaining > 0) {
            int bytesRead = dataIn.read(data, off, remaining);

            if (bytesRead == -1) {
                throw new IOException();
            } else {
                off       += bytesRead;
                remaining -= bytesRead;
            }
        }

        return(data);
    }


/**
 * <A NAME="SD_GETID"></A>
 * <EM>getId</EM>
 *
 * @return a unique id for this thread.
 */

    public final int
    getId() {
        if (JSDTThread_Debug) {
            debug("JSDTThread: getId.");
        }

        return(id);
    }


/**
 * <A NAME="SD_GETSOCKETMESSAGE"></A>
 * <EM>getSocketMessage</EM> gets the next message off the socket.
 *
 * @return true if there is a valid message to be processed.
 */

    public abstract boolean
    getSocketMessage() throws IOException;


/**
 * <A NAME="SD_GETADDRESS"></A>
 * <EM>getAddress</EM>
 *
 * @return the address of the server machine.
 */

    public final String
    getAddress() {
        if (JSDTThread_Debug) {
            debug("JSDTThread: getAddress.");
        }

        return(address);
    }


/**
 * <A NAME="SD_GETPORT"></A>
 * <EM>getPort</EM>
 *
 * @return the port number to communicate on.
 */

    public final int
    getPort() {
        if (JSDTThread_Debug) {
            debug("JSDTThread: getPort.");
        }

        return(port);
    }


/**
 * <A NAME="SD_TERMINATE"></A>
 * <EM>terminate</EM>
 */

    public final synchronized void
    terminate() {
        if (JSDTThread_Debug) {
            debug("JSDTThread: terminate.");
        }

        running = false;
        notifyAll();
    }


/**
 * <A NAME="SD_WAITFORREPLY"></A>
 * <EM>waitForReply</EM>
 *
 * @exception TimedOutException if no reply was received for this operation
 * in the given timeout period.
 *
 * @return the current message that was being waited for.
 */

    public Message
    waitForReply() throws TimedOutException {
        long currentTime, period;
        long startTime    = System.currentTimeMillis();
        long timeoutValue = Util.getLongProperty("timeoutPeriod",
                                                  timeoutPeriod);

        if (JSDTThread_Debug) {
            debug("JSDTThread: waitForReply.");
        }

        synchronized (this) {
            while (state != FOUND_REPLY) {
                currentTime = System.currentTimeMillis();
                if ((currentTime - startTime) >= timeoutValue) {
                    finishReply();
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
 * <A NAME="SD_WRITEMESSAGEHEADER"></A>
 * <EM>writeMessageHeader</EM> write the "standard" header portion of a message. * Each client/server message contains an initial standard set of fields:
 *
 * T_Version        - char.
 * version          - char.
 * T_Session_No     - char
 * sessionNo        - short    (unique for each session name).
 * id               - integer  (unique for each sending thread).
 * type             - char     (message type).
 * action           - char     (message action).
 *
 * Each of these fields are sent over the connection via a DataOutputStream.
 *
 * @param stream the DataOutputStream to write the fields to.
 * @param sessionNo the unique session number for this message.
 * @param id the unique identifier for this sending thread.
 * @param type the message type.
 * @param action the message action.
 * @param toWait do we wait for a reply to this message?
 * @param sendNow if true, send this message out now, otherwise add it to a
 * buffer of outgoing messages that will be retrieved by the receiver.
 *
 * @exception IOException if an IO exception has occured.
 */

    public abstract void
    writeMessageHeader(DataOutputStream stream, short sessionNo,
                       int id, char type, char action,
                       boolean toWait, boolean sendNow)
                throws IOException;

    public abstract void
    handleMessage(Message message);
}
