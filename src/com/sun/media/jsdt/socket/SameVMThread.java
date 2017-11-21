
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
import java.io.*;
import java.util.Vector;

/**
 * JSDT Same VM thread parent class (socket implementation).
 *
 * @version     2.3 - 21st November 2017
 * @author      Rich Burridge
 */

class
SameVMThread extends SocketThread {

    private int activeReaders  = 0;   // Threads executing getMessage_()
    private int activeWriters  = 0;   // Always zero or one.
    private int waitingReaders = 0;   // Threads not yet in getMessage_()
    private int waitingWriters = 0;   // Same for putMessage_()

    // The thread to send replies to.
    private SameVMThread replyThread;

    // The byte array input stream associated with this thread.
    final JSDTByteArrayInputStream in;

    // The byte array output stream associated with this thread.
    private final ByteArrayOutputStream out;

    // Vector of incoming data messages received from the server thread.
    private Vector<byte[]> messages = null;


/**
 * <A NAME="SD_SAMEVMTHREAD"></A>
 * <EM>SameVMThread</EM>
 *
 * @param address
 * @param port
 */

    public
    SameVMThread(String address, int port) {
        if (SameVMThread_Debug) {
            debug("SameVMThread: constructor:" +
                  " address: " + address +
                  " port: "    + port);
        }

        this.address = address;
        this.port    = port;
        in           = new JSDTByteArrayInputStream();
        dataIn       = new DataInputStream(in);
        out          = new ByteArrayOutputStream();
        dataOut      = new DataOutputStream(new BufferedOutputStream(out));
        messages     = new Vector<>();
    }


    final void
    setReplyThread(SameVMThread replyThread) {
        if (SameVMThread_Debug) {
            debug("SameVMThread: setReplyThread:" +
                  " reply thread: " + replyThread);
        }

        this.replyThread = replyThread;
    }


    public final void
    flush() {
        if (SameVMThread_Debug) {
            debug("SameVMThread: flush.");
        }

        try {
            dataOut.flush();
        } catch (IOException e) {
            error("SameVMThread: flush: ", e);
        }

        replyThread.sendSameVMMessage(out.toByteArray());
    }


    private synchronized void
    sendSameVMMessage(byte[] byteArray) {
        if (SameVMThread_Debug) {
            debug("SameVMThread: sendSameVMMessage:" +
                  " byte array: " + byteArray);
        }

        try {
            putMessage(byteArray);
        } catch (Exception e) {
            error("SameVMThread: sendSameVMMessage: ", e);
        }
    }


    final byte[]
    getMessage() {
        byte[] byteArray = null;

        if (SameVMThread_Debug) {
            debug("SameVMThread: getMessage.");
        }

        beforeGet();
        if (running) {
            byteArray = getMessage_();
            afterGet();
        }
        return(byteArray);
    }


    private boolean
    allowReader() {
        if (SameVMThread_Debug) {
            debug("SameVMThread: allowReader.");
        }

        return(messages.size() != 0);
    }


    private synchronized void
    beforeGet() {
        if (SameVMThread_Debug) {
            debug("SameVMThread: beforeGet.");
        }

        ++waitingReaders;
        while (!allowReader()) {
            try {
                if (running) {
                    wait();
                } else {
                    return;
                }
            } catch (InterruptedException ex) {
            }
        }
        --waitingReaders;
        ++activeReaders;
    }


    private synchronized void
    afterGet() {
        if (SameVMThread_Debug) {
            debug("SameVMThread: afterGet.");
        }

        --activeReaders;
        notifyAll();
    }


    private byte[]
    getMessage_() {
        byte[] byteArray;

        if (SameVMThread_Debug) {
            debug("SameVMThread: getMessage_.");
        }

        byteArray = messages.firstElement();
        messages.removeElement(byteArray);
        return(byteArray);
    }


    private void
    putMessage(byte[] byteArray) {
        if (SameVMThread_Debug) {
            debug("SameVMThread: putMessage:" +
                  " byte array: " + byteArray);
        }

        beforePut();
        putMessage_(byteArray);
        afterPut();
    }


    private boolean
    allowWriter() {
        int queueSize = Util.getIntProperty("maxQueueSize", maxQueueSize);

        if (SameVMThread_Debug) {
            debug("SameVMThread: allowWriter.");
        }

        return(messages.size() < queueSize &&
               activeReaders == 0 && activeWriters == 0);
    }


    private synchronized void
    beforePut() {
        if (SameVMThread_Debug) {
            debug("SameVMThread: beforePut.");
        }

        ++waitingWriters;
        while (!allowWriter()) {
            try {
                wait();
            } catch (InterruptedException ex) {
            }
        }
        --waitingWriters;
        ++activeWriters;
    }


    private synchronized void
    afterPut() {
        if (SameVMThread_Debug) {
            debug("SameVMThread: afterPut.");
        }

        --activeWriters;
        notifyAll();
    }


    private synchronized void
    putMessage_(byte[] byteArray) {
        if (SameVMThread_Debug) {
            debug("SameVMThread: putMessage_:" +
                  " byte array: " + byteArray);
        }

        messages.addElement(byteArray);
    }


    public final void
    writeMessageHeader(DataOutputStream stream, short sessionNo,
                       int id, char type, char action,
                       boolean toWait, boolean sendNow)
                throws IOException {
        if (SameVMThread_Debug) {
            debug("SameVMThread: writeMessageHeader:" +
                  " stream: "    + stream +
                  " session #: " + sessionNo +
                  " id: "        + id +
                  " type: "      + typeToString(type) +
                  " action: "    + actionToString(action) +
                  " wait?: "     + toWait +
                  " send now?: " + sendNow);
        }

        synchronized (this) {
            if (toWait) {
                try {
                    while (true) {
                        synchronized (waitValueLock) {
                            if (state == GET_MESSAGE && waitValue == 0) {
                                break;
                            }
                        }
                        try {
                            wait();
                        } catch (InterruptedException ie) {
                        }
                    }
                } catch (Exception e) {
                    error("SameVMThread: writeMessageHeader: ", e);
                }

                synchronized (waitValueLock) {
                    if (SameVMThread_Debug) {
                        debug("SameVMThread: writeMessageHeader:" +
                              " Old wait value" +
                              " for thread: " + this +
                              " was: "        + waitValue);
                    }

                    waitValue = (id   << 32) + (sessionNo << 16) +
                                (type <<  8) + action;

                    if (SameVMThread_Debug) {
                        debug("SameVMThread: writeMessageHeader:" +
                              " Changing wait value for:" +
                              " thread: "    + this +
                              " session #: " + sessionNo +
                              " id: "        + id +
                              " type: "   + typeToString(type) +
                              " action: " + actionToString(action));
                        debug("SameVMThread: writeMessageHeader:" +
                              " New wait value" +
                              " for thread: " + this +
                              " is: "         + waitValue);
                    }
                }
                state = WAITING_FOR_REPLY;
            } else {
                state = SENDING_MESSAGE;
            }

            out.reset();
            stream = dataOut;

            stream.writeChar(T_Version);
            stream.writeChar(version);
            stream.writeChar(T_Session_No);
            stream.writeShort(sessionNo);
            stream.writeInt(id);
            stream.writeChar(type);
            stream.writeChar(action);
        }
    }


    public final boolean
    getSocketMessage() {
        boolean retval = true;

        if (SameVMThread_Debug) {
            debug("SameVMThread: getSocketMessage.");
        }

        try {
            byte[] byteArray = getMessage();

            if (running) {
                in.setByteArray(byteArray, 0, byteArray.length);
                message.getMessageHeader(this);
            } else {
                retval = false;
            }
        } catch (Exception e) {
            error("SameVMThread: getSocketMessage: ", e);
        }

        if (retval) {
            retval = message.validMessageHeader();
        }

        return(retval);
    }
}
