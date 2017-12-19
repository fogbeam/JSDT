
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

import com.sun.media.jsdt.*;

/**
 * JSDT Channel Consumer interface.
 *
 * @version     2.3 - 20th November 2017
 * @author      Rich Burridge
 */

@SuppressWarnings("unused")
public class
ChannelConsumerImpl extends JSDTObject implements ChannelConsumer {

    /** The name of this channel consumer.
     *
     *  @serial
     */
    protected final String name;


/**
 * <A NAME="SD_CHANNELCONSUMERIMPL"></A>
 * <EM>ChannelConsumerImpl</EM> the constructor for the
 *  ChannelConsumerImpl class.
 *
 * @param name the name of the channel consumer being constructed.
 */

    public
    ChannelConsumerImpl(String name, Client client) {
        if (ChannelConsumerImpl_Debug) {
            debug("ChannelConsumerImpl: constructor:" +
                  " name: "    + name +
                  " client: " + client);
        }

        this.name = name;
    }


/**
 * <A NAME="SD_DATARECEIVED"></A>
 * <EM>dataReceived</EM> is called when data is received for this client on
 * the given channel. This interface is used for normal data and for
 * uniformly sequenced data.
 *
 * The data object received is to a copy of the client data which this
 * consumer can do with as they require.
 *
 * @param data the data which can be of unlimited size.
 */


    public void
    dataReceived(Data data) {
        if (ChannelConsumerImpl_Debug) {
            debug("ChannelConsumerImpl: dataReceived:" +
                  " data: " + data);
        }
    }
}
