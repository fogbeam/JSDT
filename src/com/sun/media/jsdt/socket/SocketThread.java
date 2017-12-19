
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

/**
 * JSDT Socket thread class.
 *
 * @version     2.3 - 21st November 2017
 * @author      Rich Burridge
 */

class
SocketThread extends JSDTThread implements Runnable, socketDebugFlags {

/**
 * <A NAME="SD_CLEANUPCONNECTION"></A>
 * <EM>cleanupConnection</EM>
 */

    public void
    cleanupConnection() {
        if (SocketThread_Debug) {
            debug("SocketThread: cleanupConnection.");
        }
    }


/**
 * <A NAME="SD_FLUSH"></A>
 * <EM>flush</EM> flush the message written to this socket.
 *
 * @exception IOException if an IO exception has occured.
 */

    public void
    flush() throws IOException {
        if (SocketThread_Debug) {
            debug("SocketThread: flush.");
        }

        dataOut.flush();
    }


/**
 * <A NAME="SD_GETSOCKETMESSAGE"></A>
 * <EM>getSocketMessage</EM> gets the next message off the socket.
 *
 * @return true if there is a valid message to be processed.
 */

    public boolean
    getSocketMessage() throws IOException {
        if (SocketThread_Debug) {
            debug("SocketThread: getSocketMessage.");
        }

        error("SocketThread: getSocketMessage: ",
              "impl.subclass");
        return(false);
    }


/**
 * <A NAME="SD_SYNCINPUT"></A>
 * <EM>syncInput</EM> there has been an error on some kind while trying to
 * read the incoming data. Try to sync up with the beginning of the next
 * message.
 */

    private void
    syncInput() {
        int toRead, value;

        if (SocketThread_Debug) {
            debug("SocketThread: syncInput.");
        }

        try {
            toRead = dataIn.available();
            while (toRead > 0) {
                dataIn.mark(4);
                value = dataIn.read();
                toRead--;
                if (value == T_Version) {
                    value = dataIn.read();
                    toRead--;
                    if (value == version) {
                        value = dataIn.read();
                        toRead--;
                        if (value == T_Session_No) {
                            dataIn.reset();
                            break;
                        }
                    }
                }
            }
        } catch (IOException ioe) {
            error("SocketThread: syncInput: ", ioe);
        }
    }


/**
 * <A NAME="SD_WRITEMESSAGEHEADER"></A>
 * <EM>writeMessageHeader</EM> write the "standard" header portion of a message.
 * Each client/server message contains an initial standard set of fields:
 *
 * T_Version        - char.
 * version          - char.
 * T_Session_No     - char
 * sessionNo        - short    (unique for each session name).
 * id               - integer  (unique for each sending thread).
 * type             - char     (message type).
 * action           - char     (message action).
 *
 * Each of these fields are sent over a socket via a DataOutputStream.
 *
 * @param stream the DataOutputStream to write the fields to.
 * @param sessionNo the unique session number for this message.
 * @param id the unique identifier for this sending thread.
 * @param type the message type.
 * @param action the message action.
 * @param toWait do we wait for a reply to this message?
 * @param sendNow aleways true for the "socket" implementation.
 *
 * @exception IOException if an IO exception has occured.
 */

    public void
    writeMessageHeader(DataOutputStream stream, short sessionNo,
                       int id, char type, char action,
                       boolean toWait, boolean sendNow)
        throws IOException {
        if (SocketThread_Debug) {
            debug("SocketThread: writeMessageHeader:" +
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
                    error("SocketThread: writeMessageHeader: ", e);
                }

                synchronized (waitValueLock) {
                    if (SocketThread_Debug) {
                        debug("SocketThread: writeMessageHeader:" +
                              " Old wait value" +
                              " for thread: " + this +
                              " was: "        + waitValue);
                    }

                    waitValue = ((long) id << 32) + (sessionNo << 16) +
                                (type <<  8) + action;

                    if (SocketThread_Debug) {
                        debug("SocketThread: writeMessageHeader:" +
                              " Changing wait value for:" +
                              " thread: "    + this +
                              " session #: " + sessionNo +
                              " id: "        + id +
                              " type: "   + typeToString(type) +
                              " action: " + actionToString(action));
                        debug("SocketThread: writeMessageHeader:" +
                              " New wait value" +
                              " for thread: " + this +
                              " is: "         + waitValue);
                    }
                }
                state = WAITING_FOR_REPLY;
            } else {
                state = SENDING_MESSAGE;
            }

            stream.writeChar(T_Version);
            stream.writeChar(version);
            stream.writeChar(T_Session_No);
            stream.writeShort(sessionNo);
            stream.writeInt(id);
            stream.writeChar(type);
            stream.writeChar(action);
        }
    }


/**
 * <A NAME="SD_HANDLEMESSAGE"></A>
 * <EM>handleMessage</EM>
 *
 * @param message
 */

    public void
    handleMessage(Message message) {
        if (SocketThread_Debug) {
            debug("SocketThread: handleMessage:" +
                  " message: " + message);
        }

        error("SocketThread: handleMessage: ",
              "impl.subclass");
    }


/**
 * <A NAME="SD_RUN"></A>
 * <EM>run</EM>
 */

    public void
    run() {
        if (SocketThread_Debug) {
            debug("SocketThread: run.");
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

                if (!running) {
                    continue;
                }

                if (!getSocketMessage()) {
                    syncInput();
                    continue;
                }

                if (SocketThread_Debug) {
                    debug("SocketThread: run:" +
                          " got a message: " + message);
                }

                found = false;
                synchronized (waitValueLock) {
                    if (waitValue != 0) {
                        long s = ((long) message.id << 32) +
                                 (message.sessionNo << 16) +
                                 (message.type      <<  8) + message.action;

                        if (SocketThread_Debug) {
                            debug("SocketThread: run:" +
                                  " comparing wait value: " + waitValue +
                                  " against: " + s);
                        }

                        if (waitValue == s) {
                            if (SocketThread_Debug) {
                                debug("SocketThread: run:" +
                                      " found a match.");
                            }

                            found = true;
                        }
                    }
                }

                if (!running) {
                    continue;
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
        } catch (IOException ioe) {
            cleanupConnection();
        } catch (Exception e) {
            error("SocketThread: run: ", e);
        }
    }
}
