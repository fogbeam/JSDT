
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

import com.sun.media.jsdt.Channel;
import com.sun.media.jsdt.impl.Message;
import com.sun.media.jsdt.impl.SessionImpl;

/**
 * JSDT UDP Channel proxy thread class.
 *
 * @version     2.3 - 20th November 2017
 * @author      Rich Burridge
 */

final class
UDPChannelProxyThread extends UDPSocketThread {

    // The client-side session associated with this channel thread.
    private final SessionImpl session;

    // The client-side channel associated with this channel thread.
    private final Channel channel;

/**
 * <A NAME="SD_UDPCHANNELPROXYTHREAD"></A>
 * <EM>UDPChannelProxyThread</EM> create a thread (and therefore an associated
 * separate socket), to handle data sent over the channel at a specific
 * priority.
 *
 * @param session the session this channel belongs to.
 * @param channel the channel in question.
 * @param host the server host name.
 * @param port the port number on the server to connect to.
 */

    public
    UDPChannelProxyThread(SessionImpl session, Channel channel,
                          String host, int port) {
        super(host, port);

        if (UDPChannelProxyThread_Debug) {
            debug("UDPChannelProxyThread: constructor:" +
                  " session: " + session +
                  " channel: " + channel +
                  " host: "    + host +
                  " port: "    + port);
        }

        this.session = session;
        this.channel = channel;
        message      = new Message();
    }


/**
 * <A NAME="SD_HANDLEMESSAGE"></A>
 * <EM>handleMessage</EM>
 *
 * @param message
 */

    public void
    handleMessage(Message message) {
        ChannelProxyMessage cpm = new ChannelProxyMessage(session, channel);

        if (UDPChannelProxyThread_Debug) {
            debug("UDPChannelProxyThread: handleMessage:" +
                  " message: " + message);
        }

        cpm.handleMessage(message);
    }
}
