
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

package examples.chat;

import com.sun.media.jsdt.*;
import java.awt.*;

/**
 * This is just a simple server/client based proof-of-concept
 * implementation to make sure that the Shared Data classes have
 * all the required API in them.
 *
 * It's based on the chat demonstration which is part of the
 * west-coast rmi distribution.
 *
 * @version     2.3 - 20th November 2017
 * @author      Rich Burridge
 */

class
ChatConsumer implements ChannelConsumer, ChatDebugFlags {

    // The name of this channel consumer.
    protected final String name;

    // The location to write all messages received.
    private final TextArea messageArea;


    public
    ChatConsumer(String name, TextArea messageArea) {
        if (ChatConsumer_Debug) {
            System.err.println("ChatConsumer: constructor.");
        }

        this.name = name;
        this.messageArea = messageArea;
    }


    public synchronized void
    dataReceived(Data data) {
        String  message;
        int     position;
        int     priority   = data.getPriority();
        String  senderName = data.getSenderName();
        Channel channel    = data.getChannel();
        String  theData    = data.getDataAsString();

        if (ChatConsumer_Debug) {
            System.err.println("ChatConsumer: dataReceived " +
                               " Priority "    + priority +
                               " Channel "     + channel +
                               " Sender name " + senderName +
                               " Data "        + theData);
        }

// Construct message and output it to the end of the message area.

        message = senderName + ": " + theData + "\n";
        position = messageArea.getText().length();
        messageArea.insert(message, position);
    }
}
