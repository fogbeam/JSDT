
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
 * The expel bytearray/channel/session/token manager/listener for the
 * test environment for JSDT.
 *
 * @version     2.3 - 15th November 2017
 * @author      Rich Burridge
 */

public class
ExpelManagerListener implements ByteArrayManager, ByteArrayListener,
                                ChannelManager, ChannelListener,
                                SessionManager, SessionListener,
                                TestDebugFlags, TokenManager,
                                TokenListener {

    // The name of this channel/session/token listener.
    protected String name;

    // The channel that is being managed/observed.
    private Channel channel = null;

    // The client that is joining the channel.
    private Client channelClient = null;

    // The client that is joining the session.
    private Client sessionClient = null;

    // The token that is being managed/observed.
    private Token token = null;

    // The client that is joining the token.
    private Client tokenClient = null;


    public
    ExpelManagerListener(String name) {
        if (ExpelManagerListener_Debug) {
            System.err.println("ExpelManagerListener: constructor:" +
                               " name: " + name);
        }

        this.name = name;
    }


    public String
    getName() {
        if (ExpelManagerListener_Debug) {
            System.err.println("ExpelManagerListener: getName: " +
                               " name: " + name);
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

        if (ExpelManagerListener_Debug) {
            System.err.println("ExpelManagerListener: byteArrayRequest:");
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
    channelExpelled(ChannelEvent event) {
        Session session   = event.getSession();
        Client  clients[] = new Client[1];

        if (ExpelManagerListener_Debug) {
            System.err.println("ExpelManagerListener: channelExpelled:" +
                               " event: " + event);
        }

// When we get a channel EXPELLED event, expel the client from the session.

        System.err.println("Expel the client from the session.");
        try {
            clients[0] = sessionClient;
            session.expel(clients);
        } catch (Exception e) {
            System.err.println("ExpelManagerListener:" +
                               " channelExpelled: exception " + e);
            e.printStackTrace();
        }
    }


    public void
    channelConsumerAdded(ChannelEvent event) { }


    public void
    channelConsumerRemoved(ChannelEvent event) { }


    public boolean
    channelRequest(Channel channel, AuthenticationInfo info, Client client) {
        boolean reply;
        String  response;

        if (ExpelManagerListener_Debug) {
            System.err.println("ExpelManagerListener: channelRequest:");
        }

        info.setChallenge("GHIJKL");
        response = (String) client.authenticate(info);
        reply    = response.equals("ghijkl");

        if (reply) {
            try {
                System.err.println("ExpelManagerListener: channelRequest:" +
                                   " adding manager's channel listener.");
                channel.addChannelListener(this);
                channelClient = client;
                this.channel  = channel;
            } catch (Exception e) {
                System.err.println("ExpelManagerListener: channelRequest:" +
                                   " exception " + e);
                e.printStackTrace();
            }
        }

        return(reply);
    }


    public void
    byteArrayCreated(SessionEvent event) { }


    public void
    byteArrayDestroyed(SessionEvent event) { }


    public void
    channelCreated(SessionEvent event) { }


    public void
    channelDestroyed(SessionEvent event) { }


    public void
    sessionDestroyed(SessionEvent event) { }


    public void
    sessionJoined(SessionEvent event) { }


    public void
    sessionLeft(SessionEvent event) { }


    public void
    sessionInvited(SessionEvent event) { }


    public void
    sessionExpelled(SessionEvent event) { }


    public void
    tokenCreated(SessionEvent event) { }


    public void
    tokenDestroyed(SessionEvent event) { }


    public boolean
    sessionRequest(Session session, AuthenticationInfo info, Client client) {
        boolean reply;
        String  response;

        if (ExpelManagerListener_Debug) {
            System.err.println("ExpelManagerListener: sessionRequest:" +
                                " session: " + session +
                                " info: "    + info +
                                " client: "  + client);
        }

        info.setChallenge("ABCDEF");
        response = (String) client.authenticate(info);
        reply    = response.equals("abcdef");

        if (info.getAction() == AuthenticationInfo.JOIN && reply) {
            try {
                System.err.println("ExpelManagerListener: sessionRequest:" +
                                   " adding manager's session listener.");
                session.addSessionListener(this);
                sessionClient = client;
            } catch (Exception e) {
                System.err.println("ExpelManagerListener: sessionRequest:" +
                                   " exception " + e);
                e.printStackTrace();
            }
        }

        return(reply);
    }


    public void
    tokenJoined(TokenEvent event) {
        Client clients [] = new Client[1];

        if (ExpelManagerListener_Debug) {
            System.err.println("ExpelManagerListener: tokenJoined:" +
                               " event: " + event);
        }

// When we get a token JOINED event, expel the client from the token.

        System.err.println("Expel the client from the token.");
        try {
            clients[0] = tokenClient;
            token.expel(clients);
        } catch (Exception e) {
            System.err.println("ExpelManagerListener:" +
                               " tokenJoined: exception " + e);
            e.printStackTrace();
        }
    }


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
    tokenExpelled(TokenEvent event) {
        Client clients[] = new Client[1];

        if (ExpelManagerListener_Debug) {
            System.err.println("ExpelManagerListener: tokenExpelled:" +
                               " event: " + event);
        }

// When we get a token EXPELLED event, expel the client from the channel.

        System.err.println("Expel the client from the channel.");
        try {
            clients[0] = channelClient;
            channel.expel(clients);
        } catch (Exception e) {
            System.err.println("ExpelManagerListener:" +
                               " tokenExpelled: exception " + e);
            e.printStackTrace();
        }
    }


    public boolean
    tokenRequest(Token token, AuthenticationInfo info, Client client) {
        boolean reply;
        String  response;

        if (ExpelManagerListener_Debug) {
            System.err.println("ExpelManagerListener: tokenRequest:");
        }

        info.setChallenge("MNOPQR");
        response = (String) client.authenticate(info);
        reply    = response.equals("mnopqr");

        if (reply) {
            try {
                System.err.println("ExpelManagerListener: tokenRequest:" +
                                   " adding manager's token listener.");
                token.addTokenListener(this);
                tokenClient = client;
                this.token  = token;
            } catch (Exception e) {
                System.err.println("ExpelManagerListener: tokenRequest:" +
                                   " exception " + e);
                e.printStackTrace();
            }
        }

        return(reply);
    }
}
