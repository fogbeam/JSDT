
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

import com.sun.media.jsdt.*;
import com.sun.media.jsdt.impl.*;
import java.net.InetAddress;
import java.io.*;

/**
 * JSDT UDP Channel Server Message class.
 *
 * @version     2.3 - 6th November 2017
 * @author      Rich Burridge
 */

final class
UDPChannelServerMessage extends JSDTMessage {

/**
 * <A NAME="SD_UDPCHANNELSERVERMESSAGE"></A>
 * <EM>UDPChannelServerMessage</EM>
 *
 * @param session
 * @param channel
 */

    public
    UDPChannelServerMessage(SessionImpl session, Channel channel) {
        if (UDPChannelServerMessage_Debug) {
            debug("UDPChannelServerMessage: constructor:" +
                  " session: " + session +
                  " channel: " + channel);
        }

        this.session = session;
    }


/**
 * <A NAME="SD_SETPACKETINFO"></A>
 * <EM>setPacketInfo</EM>
 *
 * @param address
 * @param port
 */

    void
    setPacketInfo(InetAddress address, int port) {
        if (UDPChannelServerMessage_Debug) {
            debug("UDPChannelServerMessage: setPacketInfo:" +
                  " address: " + address +
                  " port: "    + port);
        }
    }


/**
 * <A NAME="SD_HANDLEMESSAGE"></A>
 * <EM>handleMessage</EM>
 *
 * @param message
 */

    protected void
    handleMessage(Message message) {
        if (UDPChannelServerMessage_Debug) {
            debug("UDPChannelServerMessage: handleMessage:" +
                  " message: " + message);
        }

        if (message.type == ChannelImpl.M_Channel) {
            parseProxyChannelMessage(message);
        } else {
            error("UDPChannelServerMessage: handleMessage: ",
                  "impl.unknown.type", message);
        }
    }


/**
 * <A NAME="SD_PARSEPROXYCHANNELMESSAGE"></A>
 * <EM>parseProxyChannelMessage</EM>
 *
 * @param message
 */

    private void
    parseProxyChannelMessage(Message message) {
        DataInputStream  in          = message.thread.dataIn;
        SessionServer    ss          = (SessionServer) session.so.getServer();
        String           channelName;
        ChannelImpl      channel;

        if (UDPChannelServerMessage_Debug) {
            debug("UDPChannelServerMessage: parseProxyChannelMessage:" +
                  " message: " + message);
        }

        try {
            channelName = in.readUTF();
            channel     = ss.getChannelByName(channelName);

            if (channel == null) {
                message.thread.writeMessageHeader(message.thread.dataOut,
                                message.sessionNo, message.id,
                                message.type, message.action, false, true);
                message.thread.dataOut.writeInt(JSDTException.NO_SUCH_CHANNEL);
                message.thread.flush();
                message.thread.finishMessage();
            } else {
                ChannelServer cs = (ChannelServer) channel.so.getServer();

                switch (message.action) {
                    case T_AddConsumer:                   /* ADDCONSUMER. */
                        cs.addConsumer(message);
                        break;
                    case T_RemoveConsumer:                /* REMOVECONSUMER. */
                        cs.removeConsumer(message);
                        break;
                    case T_Send:                           /* SEND. */
                        cs.send(message, channelName);
                        break;
                    default:
                        error("UDPChannelServerMessage:" +
                              " parseProxyChannelMessage: ",
                              "impl.unknown.action", message);
                }
            }
        } catch (IOException e) {
            error("UDPChannelServerMessage: parseProxyChannelMessage: ", e);
        }
    }
}
