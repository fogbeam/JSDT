
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

package test;

import com.sun.media.jsdt.*;

/**
 * JSDT test5() sending data thread class.
 *
 * @version     2.3 - 15th November 2017
 * @author      Rich Burridge
 */

class
SendData extends Thread implements TestDebugFlags {

    // The session to join.
    protected final Session session;

    // The name of the channel to join and send data over.
    protected final String channelName;


/**
 * <A NAME="SD_SENDDATA"></A>
 * <EM>SendData</EM>
 *
 * @param session the session to join.
 * @param channelName the name of the channel to join and send data over.
 */

    public
    SendData(Session session, String channelName) {
        if (SendData_Debug) {
            System.err.println("SendData: constructor:" +
                               " session: "      + session +
                               " channel name: " + channelName);
        }

        this.session     = session;
        this.channelName = channelName;
    }


// Generate a random number between low and high.

    private int
    rrange(int low, int high) {
        return((int)(Math.random() * (high - low + 1) + low));
    }


/**
 * <A NAME="SD_RUN"></A>
 * <EM>run</EM>
 */

    public void
    run() {
        Channel channel;
        Data   data;
        int    dstart;
        int    dlen;
        String sessionName       = session.getName();
        String sendingClientName = "Sender";
        Client sendingClient     = new TestClient(sendingClientName);
        byte[] numbers           = new byte[100];

        if (SendData_Debug) {
            System.err.println("SendData: run.");
        }

        try {
            System.err.println("Client: " + sendingClientName +
                               " joining session: " + sessionName);
            session.join(sendingClient);
            System.err.println("Client: " + sendingClientName +
                               " joining channel: " + channelName);
            channel = session.createChannel(sendingClient, channelName,
                                            true, true, false);
            channel.join(sendingClient);
            TestUser.rsleep(4, 6);
            System.err.println("About to start sending data...");
            for (int count = 0; count < 10; count++) {
                dstart = rrange(5, 40);
                dlen   = rrange(10, 50);

                for (int i = dstart; i < dstart + dlen; i++) {
                    numbers[i] = (byte) i;
                }
                data = new Data(numbers, dlen);
                data.setPriority(Channel.HIGH_PRIORITY);
                System.err.println("Client: " + sendingClientName +
                                   " sending data.");
                channel.sendToAll(sendingClient, data);
            }
        } catch (Exception e) {
            System.err.println("SendData: run: exception " + e);
            e.printStackTrace();
        }
    }
}
