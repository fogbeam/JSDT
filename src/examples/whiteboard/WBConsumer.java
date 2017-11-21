
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

package examples.whiteboard;

import com.sun.media.jsdt.*;

/**
 * This is just a simple server/client based proof-of-concept
 * implementation to make sure that the Shared Data classes have
 * all the required API in them.
 *
 * It's based on WhiteBoard; a test applet for the game server from
 * daniel@vpro.nl. It's been rewritten to use JSDT and to make it
 * easier to modify and maintain.
 *
 * @version     2.3 - 21st November 2017
 * @author      daniel@vpro.nl
 * @author      Rich Burridge
 */

public class
WBConsumer implements ChannelConsumer, WhiteBoardDebugFlags {

    // The name of this channel consumer.
    protected final String name;

    // The WhiteBoardUser for this consumer.
    private final WhiteBoardUser wbu;


    public
    WBConsumer(String name, WhiteBoardUser wbUser) {
        if (WBConsumer_Debug) {
            System.err.println("WBConsumer: constructor(" + name + ").");
        }

        this.name = name;
        this.wbu  = wbUser;
    }


    public synchronized void
    dataReceived(Data data) {
        if (WBConsumer_Debug) {
            System.err.println("WBConsumer: dataReceived: " +
                               " data " + data);
        }

// Handle the data received.

        wbu.commandLine = data.getDataAsString();
        if (WBConsumer_Debug) {
            System.err.println("Setting commandLine to: " + wbu.commandLine);
        }
        wbu.controls.repaint();
        wbu.drawingArea.repaint();
    }
}
