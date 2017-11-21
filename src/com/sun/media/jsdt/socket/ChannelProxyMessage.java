
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
import com.sun.media.jsdt.impl.*;
import java.io.*;

/**
 * JSDT channel proxy message class.
 *
 * @version     2.3 - 20th November 2017
 * @author      Rich Burridge
 */

final class
ChannelProxyMessage extends JSDTMessage {

    // The client-side session associated with this channel thread.
    private final SessionImpl session;


/**
 * <A NAME="SD_CHANNELPROXYMESSAGE"></A>
 * <EM>ChannelProxyMessage</EM>
 *
 * @param session
 * @param channel
 */

    public
    ChannelProxyMessage(SessionImpl session, Channel channel) {
        if (ChannelProxyMessage_Debug) {
            debug("ChannelProxyMessage: constructor:" +
                  " session: " + session +
                  " channel: " + channel);
        }

        this.session = session;
    }


/**
 * <A NAME="SD_HANDLEMESSAGE"></A>
 * <EM>handleMessage</EM>
 *
 * @param message
 */

    protected void
    handleMessage(Message message) {
        if (ChannelProxyMessage_Debug) {
            debug("ChannelProxyMessage: handleMessage:" +
                  " message: " + message);
        }

        if (message.type == ChannelImpl.M_Channel) {
            parseChannelMessage(message);
        } else {
            error("ChannelProxyMessage: handleMessage: ",
                  "impl.unknown.type", message);
        }
    }


/**
 * <A NAME="SD_PARSECHANNELMESSAGE"></A>
 * <EM>parseChannelMessage</EM>
 *
 * @param message
 */

    private void
    parseChannelMessage(Message message) {
        DataInputStream in          = message.thread.dataIn;
        String          channelName = null;
        SessionProxy    sp          = (SessionProxy) session.po.getProxy();
        ChannelImpl     channel     = null;

        if (ChannelProxyMessage_Debug) {
            debug("ChannelProxyMessage: parseChannelMessage:" +
                  " message: " + message);
        }

        try {
            channelName = in.readUTF();
            channel     = sp.getChannelByName(channelName);
        } catch (IOException e) {
            error("ChannelProxyMessage: parseChannelMessage: ", e);
        }

        if (message.action == T_DataReceived) {          /* DATARECEIVED. */
            ChannelProxy cp = (ChannelProxy) channel.po.getProxy();

            if (cp != null) {
                cp.dataReceived(message, channelName);
            }
        }
    }
}
