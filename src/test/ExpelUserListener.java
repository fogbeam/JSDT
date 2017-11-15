
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

import com.sun.media.jsdt.event.*;

/**
 * The expel channel/session/token user listener for the test environment
 * for JSDT.
 *
 * @version     2.3 - 15th November 2017
 * @author      Rich Burridge
 */

public class
ExpelUserListener extends SessionAdaptor
                  implements ChannelListener, TestDebugFlags, TokenListener {

    // The name of this channel/session/token listener.
    protected String name;


    public
    ExpelUserListener(String name) {
        if (ExpelUserListener_Debug) {
            System.err.println("ExpelUserListener: constructor: " +
                               " name: " + name);
        }

        this.name = name;
    }


    public String
    getName() {
        if (ExpelUserListener_Debug) {
            System.err.println("ExpelUserListener: getName: " +
                               " name: " + name);
        }
        return(name);
    }


    public void
    channelJoined(ChannelEvent event) { }


    public void
    channelLeft(ChannelEvent event) { }


    public void
    channelInvited(ChannelEvent event) { }


    public void
    channelExpelled(ChannelEvent event) { }


    public void
    channelConsumerAdded(ChannelEvent event) { }


    public void
    channelConsumerRemoved(ChannelEvent event) { }


    public void
    tokenJoined(TokenEvent event) { }


    public void
    tokenLeft(TokenEvent event) { }


    public void
    tokenGiven(TokenEvent event) { }


    public void
    tokenRequested(TokenEvent event) { }


    public void
    tokenGrabbed(TokenEvent event) { }


    public void
    tokenReleased(TokenEvent event) { }


    public void
    tokenInvited(TokenEvent event) { }


    public void
    tokenExpelled(TokenEvent event) { }
}
