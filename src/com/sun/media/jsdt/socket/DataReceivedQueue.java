
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

import com.sun.media.jsdt.Data;
import com.sun.media.jsdt.impl.JSDTObject;
import com.sun.media.jsdt.impl.Util;
import java.util.Vector;

/**
 * The queue of client-side data received messages.
 *
 * @version     2.3 - 5th November 2017
 * @author      Rich Burridge
 */

public final class
DataReceivedQueue extends JSDTObject implements socketDebugFlags {

    // Vector of incoming data messages received from the channel server.
    private Vector<DataReceivedMessage> messages = null;

    // Indicates whether we have finished with the data received queue.
    boolean finished = false;


/**
 * <A NAME="SD_DATARECEIVEDQUEUE"></A>
 * <EM>DataReceivedQueue</EM> the constructor for the DataReceivedQueue class.
 */

    DataReceivedQueue() {
        if (DataReceivedQueue_Debug) {
            debug("DataReceivedQueue: constructor.");
        }

        messages = new Vector<>();
    }


/**
 * <A NAME="SD_GETMESSAGE"></A>
 * <EM>getMessage</EM> gets the next message from the queue of incoming
 * messages. If the message queue is currently empty, it will wait until a
 * new message arrives.
 *
 * @return the next message to be processed.
 */

    synchronized DataReceivedMessage
    getMessage() {
        DataReceivedMessage message;

        if (DataReceivedQueue_Debug) {
            debug("DataReceivedQueue: getMessage.");
        }

        notifyAll();
        while (messages.size() == 0) {
            try {
                if (!finished) {
                    wait();
                } else {
                    return(null);
                }
            } catch (InterruptedException ie) {
            }
        }
        message = messages.firstElement();
        messages.removeElement(message);
        return(message);
    }


/**
 * <A NAME="SD_PUTMESSAGE"></A>
 * <EM>putMessage</EM> put a message in the queue of messages to be processed.
 * This message will contain all the information needed, to send this message
 * on to its rightful consumer.
 *
 * @param channelProxy the channel proxy associated with this message.
 * @param receiverName the name of the receiver of the message.
 * @param isOrdered indicates if the channel this message was received on,
 * is ordered.
 * @param data the messages bytes (converted to a Data object).
 */

    synchronized void
    putMessage(ChannelProxy channelProxy, String receiverName,
               boolean isOrdered, Data data) {
        int                 queueSize = Util.getIntProperty("maxQueueSize",
                                                            maxQueueSize);
        DataReceivedMessage message   = new DataReceivedMessage();

        if (DataReceivedQueue_Debug) {
            debug("DataReceivedQueue: putMessage:" +
                  " channel proxy: " + channelProxy +
                  " receiver name: " + receiverName +
                  " isOrdered? "     + isOrdered +
                  " data: "          + data);
        }

        message.setMessageInfo(channelProxy, receiverName, isOrdered, data);
        while (messages.size() == queueSize) {
            try {
                wait();
            } catch (InterruptedException ie) {
            }
        }
        messages.addElement(message);
        notifyAll();
    }


/**
 * <A NAME="SD_TERMINATE"></A>
 * <EM>terminate</EM>
 */

    final synchronized void
    terminate() {
        if (DataReceivedQueue_Debug) {
            debug("DataReceivedQueue: terminate.");
        }

        finished = true;
        notifyAll();
    }
}
