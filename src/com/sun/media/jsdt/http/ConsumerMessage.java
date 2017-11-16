
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
import com.sun.media.jsdt.impl.Message;

/**
 * JSDT channel consumer message thread class.
 *
 * @version     2.3 - 28th October 2017
 * @author      Rich Burridge
 */

final class
ConsumerMessage extends JSDTMessage implements Runnable {

    /** The channel consumer to be notified. */
    private ChannelConsumer consumer;

    /** The data to be sent to the consumer. */
    private Data data;


/**
 * <A NAME="SD_SETCONSUMERINFO"></A>
 * <EM>setConsumerInfo</EM> the the fields that have changed for this
 * consumer message.
 *
 * @param consumer
 * @param data
 */

    void
    setConsumerInfo(ChannelConsumer consumer, Data data) {
        if (ConsumerMessage_Debug) {
            debug("ConsumerMessage: setConsumerInfo:" +
                  " consumer: " + consumer +
                  " data: "     + data);
        }

        this.consumer = consumer;
        this.data     = data;
    }


/**
 * <A NAME="SD_HANDLEMESSAGE"></A>
 * <EM>handleMessage</EM>
 *
 * @param message
 */

    protected void
    handleMessage(Message message) {
        if (ConsumerMessage_Debug) {
            debug("ConsumerMessage: handleMessage:" +
                  " message: " + message);
        }

        consumer.dataReceived(data);
    }


/**
 * <A NAME="SD_RUN"></A>
 * <EM>run</EM>
 */

    public void
    run() {
        if (ConsumerMessage_Debug) {
            debug("ConsumerMessage: run.");
        }

        handleMessage(null);
    }
}
