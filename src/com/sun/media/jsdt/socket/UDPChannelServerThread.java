
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
 * JSDT UDP Channel Server Thread class.
 *
 * @version     2.3 - 6th November 2017
 * @author      Rich Burridge
 */

final class
UDPChannelServerThread extends UDPSocketThread {

    /** The UDP Channel server message associated with this server thread. */
    private UDPChannelServerMessage csm = null;


/**
 * <A NAME="SD_UDPCHANNELSERVERTHREAD"></A>
 * <EM>UDPChannelServerThread</EM>
 *
 * @param session
 * @param channel
 * @param port
 */

    public
    UDPChannelServerThread(SessionImpl session, Channel channel, int port) {
        super(port);

        if (UDPChannelServerThread_Debug) {
            debug("UDPChannelServerThread: constructor:" +
                  " session: " + session +
                  " channel: " + channel +
                  " port: "    + port);
        }

        csm = new UDPChannelServerMessage(session, channel);
        message = new Message();
    }


/**
 * <A NAME="SD_CLEANUPCONNECTION"></A>
 * <EM>cleanupConnection</EM>
 */

    public void
    cleanupConnection() {
        super.cleanupConnection();

        if (UDPChannelServerThread_Debug) {
            debug("UDPChannelServerThread: cleanupConnection.");
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
        if (UDPChannelServerThread_Debug) {
            debug("UDPChannelServerThread: handleMessage:" +
                  " message: " + message);
        }

        csm.setPacketInfo(packet.getAddress(), packet.getPort());
        csm.handleMessage(message);
    }
}
