
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
 * The receive bytearray/channel/session/token manager for the
 * test environment for JSDT.
 *
 * @version     2.3 - 15th November 2017
 * @author      Rich Burridge
 */

public class
ReceiveManager implements ByteArrayManager, ChannelManager, SessionManager,
                          TestDebugFlags, TokenManager {

    // The name of this channel/session/token manager.
    protected String name;


    public
    ReceiveManager(String name) {
        if (ReceiveManager_Debug) {
            System.err.println("ReceiveManager: constructor:" +
                               " name: " + name);
        }

        this.name = name;
    }


    public String
    getName() {
        if (ReceiveManager_Debug) {
            System.err.println("ReceiveManager: getName: " +
                               " name: " + name);
        }
        return(name);
    }


    public boolean
    byteArrayRequest(ByteArray byteArray,
                     AuthenticationInfo info, Client client) {
        String reply;

        if (ReceiveManager_Debug) {
            System.err.println("ReceiveManager: byteArrayRequest:");
        }

        info.setChallenge("STUVWX");
        reply = (String) client.authenticate(info);
        return(reply.equals("stuvwx"));
    }


    public boolean
    channelRequest(Channel channel, AuthenticationInfo info, Client client) {
        String reply;

        if (ReceiveManager_Debug) {
            System.err.println("ReceiveManager: channelRequest:");
        }

        info.setChallenge("GHIJKL");
        reply = (String) client.authenticate(info);
        return(reply.equals("ghijkl"));
    }


    public boolean
    sessionRequest(Session session, AuthenticationInfo info, Client client) {
        String reply;

        if (ReceiveManager_Debug) {
            System.err.println("ReceiveManager: sessionRequest:");
        }

        info.setChallenge("ABCDEF");
        reply = (String) client.authenticate(info);
        return(reply.equals("abcdef"));
    }


    public boolean
    tokenRequest(Token token, AuthenticationInfo info, Client client) {
        String reply;

        if (ReceiveManager_Debug) {
            System.err.println("ReceiveManager: tokenRequest:");
        }

        info.setChallenge("MNOPQR");
        reply = (String) client.authenticate(info);
        return(reply.equals("mnopqr"));
    }
}
