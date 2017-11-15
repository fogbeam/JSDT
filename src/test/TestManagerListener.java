
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
import com.sun.media.jsdt.event.*;

/**
 * The test bytearray/channel/session/token manager/listener for the
 * test environment for JSDT.
 *
 * @version     2.3 - 15th November 2017
 * @author      Rich Burridge
 */

public class
TestManagerListener extends SessionAdaptor
                    implements ByteArrayManager, ByteArrayListener,
                               ChannelManager, ChannelListener,
                               SessionManager, TestDebugFlags,
                               TokenManager,   TokenListener {

    // The name of this channel/session/token manager/listener.
    protected String name;


    public
    TestManagerListener(String name) {
        if (TestManagerListener_Debug) {
            System.err.println("TestManagerListener: constructor: " + name);
        }

        this.name = name;
    }


    public String
    getName() {
        if (TestManagerListener_Debug) {
            System.err.println("TestManagerListener: getName: " + name);
        }
        return(name);
    }


    public void
    byteArrayJoined(ByteArrayEvent event) { }


    public void
    byteArrayLeft(ByteArrayEvent event) { }


    public void
    byteArrayValueChanged(ByteArrayEvent event) { }


    public void
    byteArrayInvited(ByteArrayEvent event) { }


    public void
    byteArrayExpelled(ByteArrayEvent event) { }


    public boolean
    byteArrayRequest(ByteArray byteArray,
                     AuthenticationInfo info, Client client) {
        String reply;

        if (TestManagerListener_Debug) {
            System.err.println("TestManagerListener: byteArrayRequest:");
        }

        info.setChallenge("STUVWX");
        reply = (String) client.authenticate(info);
        return(reply.equals("stuvwx"));
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


    public boolean
    channelRequest(Channel channel, AuthenticationInfo info, Client client) {
        String reply;

        if (TestManagerListener_Debug) {
            System.err.println("TestManagerListener: channelRequest:");
        }

        info.setChallenge("GHIJKL");
        reply = (String) client.authenticate(info);
        return(reply.equals("ghijkl"));
    }


    public boolean
    sessionRequest(Session session, AuthenticationInfo info, Client client) {
        String reply;

        if (TestManagerListener_Debug) {
            System.err.println("TestManagerListener: sessionRequest:");
        }

        info.setChallenge("ABCDEF");
        reply = (String) client.authenticate(info);
        return(reply.equals("abcdef"));
    }


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


    public boolean
    tokenRequest(Token token, AuthenticationInfo info, Client client) {
        String reply;

        if (TestManagerListener_Debug) {
            System.err.println("TestManagerListener: tokenRequest:");
        }

        info.setChallenge("MNOPQR");
        reply = (String) client.authenticate(info);
        return(reply.equals("mnopqr"));
    }
}
