
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
 * The invite client for the test environment for JSDT.
 *
 * @version     2.3 - 21st November 2017
 * @author      Rich Burridge
 */

public class
InviteClient extends ClientAdaptor implements Client, TestDebugFlags {

    private final String name;

    public
    InviteClient(String name) {
        if (InviteClient_Debug) {
            System.err.println("InviteClient: constructor: " +
                                " name: " + name);
        }

        this.name = name;
    }


    public Object
    authenticate(AuthenticationInfo info) {
        char   type      = info.getType();
        String name      = info.getName();
        int    action    = info.getAction();
        String challenge = (String) info.getChallenge();
        String response  = null;

        if (InviteClient_Debug) {
            System.err.println("InviteClient: authenticate:" +
                                " type: "      + type +
                                " name: "      + name +
                                " action: "    + action +
                                " challenge: " + challenge);
        }

        if (type == AuthenticationInfo.SESSION) {
            response = "abcdef";
        } else if (type == AuthenticationInfo.CHANNEL) {
            response = "ghijkl";
        } else if (type == AuthenticationInfo.TOKEN) {
            response = "mnopqr";
        } else if (type == AuthenticationInfo.BYTEARRAY) {
            response = "stuvwx";
        }
        return(response);
    }


    public String
    getName() {
        if (InviteClient_Debug) {
            System.err.println("InviteClient: getName.");
        }

        return(name);
    }


    public void
    channelInvited(ClientEvent event) {
        String  resourceName = event.getResourceName();
        Session session;
        Channel channel;

        if (InviteClient_Debug) {
            System.err.println("InviteClient: channelInvited:" +
                               " event: " + event);
        }

        try {
            System.err.println("InviteClient: channelInvited:" +
                               " getting a session for: " + resourceName);
            session = event.getSession();
            System.err.println("InviteClient: channelInvited:" +
                               " getting a channel for: " + resourceName);
            channel = session.createChannel(this, resourceName,
                                            true, true, false);
            System.err.println("InviteClient: channelInvited:" +
                               " joining channel: " + resourceName);
            channel.join(this);
        } catch (Exception e) {
            System.err.println("InviteClient: channelInvited:" +
                                " exception " + e);
            e.printStackTrace();
        }
    }


    public void
    sessionInvited(ClientEvent event) {
        String  resourceName = event.getResourceName();
        Session session;

        if (InviteClient_Debug) {
            System.err.println("InviteClient: sessionInvited:" +
                               " event: " + event);
        }

        try {
            System.err.println("InviteClient: sessionInvited:" +
                               " getting a session for: " + resourceName);
            session = event.getSession();
            System.err.println("InviteClient: sessionInvited:" +
                               " joining session: " + session.getName());
            session.join(this);
        } catch (Exception e) {
            System.err.println("InviteClient: sessionInvited:" +
                                " exception " + e);
            e.printStackTrace();
        }
    }


    public void
    tokenInvited(ClientEvent event) {
        String  resourceName = event.getResourceName();
        Session session;
        Token   token;

        if (InviteClient_Debug) {
            System.err.println("InviteClient: tokenInvited:" +
                               " event: " + event);
        }

        try {
            System.err.println("InviteClient: tokenInvited:" +
                               " getting a session for: " + resourceName);
            session = event.getSession();
            System.err.println("InviteClient: tokenInvited:" +
                               " getting a token for: " + resourceName);
            token = session.createToken(this, resourceName, false);
            System.err.println("InviteClient: tokenInvited:" +
                               " joining token: " + resourceName);
            token.join(this);
        } catch (Exception e) {
            System.err.println("InviteClient: tokenInvited:" +
                                " exception " + e);
            e.printStackTrace();
        }
    }
}
