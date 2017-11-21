
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
 * The channel consumer for the test environment for JSDT.
 *
 * @version     2.3 - 21st November 2017
 * @author      Rich Burridge
 */

public class
TestChannelConsumer implements ChannelConsumer, TestDebugFlags {

    // The name of this channel consumer.
    protected final String name;


    public
    TestChannelConsumer(String name) {
        if (TestChannelConsumer_Debug) {
            System.err.println("TestChannelConsumer: constructor:" +
                               " name: " + name);
        }

        this.name = name;
    }


    public synchronized void
    dataReceived(Data data) {
        int    priority = data.getPriority();
        byte[] theData  = data.getDataAsBytes();
        int    length   = theData.length;

        if (TestChannelConsumer_Debug) {
            System.err.println("TestChannelConsumer: dataReceived:" +
                               " data: "        + data              +
                               " channel: "     + data.getChannel() +
                               " sender name: " + data.getSenderName());
        }

        System.err.println("Consumer received " + length +
                           " bytes of data at priority " + priority);
    }
}
