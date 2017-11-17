
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
import java.util.*;

/**
 * JSDT Channel client-side data received thread class.
 *
 * @version     2.3 - 16th November 2017
 * @author      Rich Burridge
 * @author      Andrea Colpo
 */

public final class
DataReceivedThread extends JSDTObject implements Runnable, httpDebugFlags {

    /** The queue of client-side data received messages. */
    private DataReceivedQueue queue = null;

    /** The ChannelProxy which has created this DataReceivedThread */
    private ChannelProxy channelProxy = null;


/**
 * <A NAME="SD_DATARECEIVEDTHREAD"></A>
 * <EM>DataReceivedThread</EM> the constructor for the DataReceivedThread class.
 *
 * @param queue
 * @param channelProxy
 */

    DataReceivedThread(DataReceivedQueue queue, ChannelProxy channelProxy) {
        if (DataReceivedThread_Debug) {
            debug("DataReceivedThread: constructor:" +
                  "\n   queue: "         + queue +
                  "\n   channel proxy: " + channelProxy);
        }

        this.queue = queue;
        this.channelProxy = channelProxy;
    }


/**
 * <A NAME="SD_HANDLEMESSAGE"></A>
 * <EM>handleMessage</EM> handle the next message in the message vector.
 * The data is passed onto all the consumers of the receiving client.
 *
 * @param message the data received message to process.
 */

    private void
    handleMessage(DataReceivedMessage message) {
        String       receiverName = message.getReceiverName();
        Data         data         = message.getData();
        ChannelProxy cp           = message.getChannelProxy();
        Client       client       = cp.clients.get(receiverName);

        if (DataReceivedThread_Debug) {
            debug("DataReceivedThread: handleMessage:" +
                  " message: " + message);
        }

        if (client != null) {
            ClientImpl c = (ClientImpl) cp.clientConsumers.get(client);

            if (c != null) {
                Hashtable consumers = c.getConsumers();

                for (Enumeration e = consumers.elements();
                                 e.hasMoreElements();) {
                    ChannelConsumer consumer =
                                        (ChannelConsumer) e.nextElement();

                    if (consumer != null) {
                        try {
                            if (message.isOrdered()) {
                                consumer.dataReceived(data);
                            } else {
                                ConsumerMessage cm = new ConsumerMessage();

                                cm.setConsumerInfo(consumer, data);
                                Util.startThread(cm, "ConsumerMessageThread:" +
                                                     c.getName(), true);
                            }
                        } catch (Throwable th) {
                            error("DataReceivedThread: handleMessage: ",
                                  "impl.thrown", th + " by consumer.");
                        }
                    }
                }
            }
        }
    }


/**
 * <A NAME="SD_RUN"></A>
 * <EM>run</EM> handle incoming data messages as they are received from the
 * channel server. The messages will be inserting into the messages vector
 * by the channel proxy.
 */

    public void
    run() {
        if (DataReceivedThread_Debug) {
            debug("DataReceivedThread: run.");
        }

        try {
            while (!queue.finished) {
                DataReceivedMessage message;

                if ((message = queue.getMessage()) != null) {
                    handleMessage(message);
                }
            }
        } catch (Exception e) {
            error("DataReceivedThread: run: ", e);
        }

        if (DataReceivedThread_Debug) {
            debug("DataReceivedThread: run : messages handled.");
        }

        /* Decrement thread counter of creator ChannelProxy instance. */
        this.channelProxy.decrementNoThreads();
    }
}
