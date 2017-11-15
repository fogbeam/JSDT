
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
 * The invite bytearray/channel/session/token manager/listener for the
 * test environment for JSDT.
 *
 * @version     2.3 - 15th November 2017
 * @author      Rich Burridge
 */

public class
InviteManagerListener extends SessionAdaptor
                      implements ByteArrayManager, ByteArrayListener,
                                 ChannelManager, ChannelListener,
                                 SessionManager, TestDebugFlags,
                                 TokenManager,   TokenListener {

    // Set true when client has been invited to join session.
    private boolean sessionInvited = false;

    // Set true when client has been invited to join channel.
    private boolean channelInvited = false;

    // Set true when client has been invited to join token.
    private boolean tokenInvited = false;

    // The name of this channel manager.
    protected String name;

    // Machine name of the client to invite to join this session.
    private String clientHost = null;

    // The port number to use on that machine.
    private static int clientPort = 0;

    // The type of session to use.
    private String sessionType = null;

    // The client that's to be invited to join the channel/session/token.
    private Client inviteClient = null;

    // The channel that is being managed/observed.
    private Channel channel = null;

    // The token that is being managed/observed.
    private Token token = null;


    public
    InviteManagerListener(String name, String clientHost,
                          int clientPort, String sessionType) {
        if (InviteManagerListener_Debug) {
            System.err.println("InviteManagerListener: constructor: " +
                                " name: "         + name +
                                " client host: "  + clientHost +
                                " client port: "  + clientPort +
                                " session type: " + sessionType);
        }

        this.name        = name;
        this.clientHost  = clientHost;
        this.clientPort  = clientPort;
        this.sessionType = sessionType;
    }


    public String
    getName() {
        if (InviteManagerListener_Debug) {
            System.err.println("InviteManagerListener: getName: " +
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

        if (InviteManagerListener_Debug) {
            System.err.println("InviteManagerListener: byteArrayRequest:");
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
        boolean reply;
        String  response;

        if (InviteManagerListener_Debug) {
            System.err.println("InviteManagerListener: channelRequest:");
        }

        info.setChallenge("GHIJKL");
        response = (String) client.authenticate(info);
        reply    = response.equals("ghijkl");

        if (reply) {
            try {
                System.err.println("InviteManagerListener: channelRequest:" +
                                   " adding manager's channel listener.");
                channel.addChannelListener(this);
                this.channel = channel;
            } catch (Exception e) {
                System.err.println("InviteManagerListener: channelRequest:" +
                                   " exception " + e);
                e.printStackTrace();
            }
        }

        return(reply);
    }


    public void
    sessionJoined(SessionEvent event) {
        String  clientName = event.getClientName();
        Session session    = event.getSession();

        if (InviteManagerListener_Debug) {
            System.err.println("InviteManagerListener: sessionJoined:" +
                               " event: " + event);

            System.err.println("InviteManagerListener: sessionJoined:" +
                               " client name "    + clientName +
                               " from session "   + session +
                               " session "        + session);
        }

        if (clientName.equals("InviteClient")) {
            Client clients [] = new Client[1];

/*  InviteClient has now joined the session, so we invite it to join the
 *  channel and token.
 */
            try {
                clients[0] = inviteClient;

                if (!channelInvited) {
                    System.err.println("Inviting client to join the Channel.");
                    channel.invite(clients);
                    channelInvited = true;
                }

                if (!tokenInvited) {
                    System.err.println("Inviting client to join the Token.");
                    token.invite(clients);
                    tokenInvited = true;
                }
            } catch (Exception e) {
                System.err.println("InviteManagerListener:" +
                                   " sessionJoined: exception " + e);
                e.printStackTrace();
            }
        }
    }


    public boolean
    sessionRequest(Session session, AuthenticationInfo info, Client client) {
        boolean reply;
        String  response;

        if (InviteManagerListener_Debug) {
            System.err.println("InviteManagerListener: sessionRequest:");
        }

        info.setChallenge("ABCDEF");
        response = (String) client.authenticate(info);
        reply    = response.equals("abcdef");

        if (reply) {
            try {
                if (client.getName().startsWith("TestUser")) {
                    System.err.println("InviteManagerListener:" +
                        " sessionRequest: adding manager's session listener.");
                    session.addSessionListener(this);
                }
            } catch (Exception e) {
                System.err.println("InviteManagerListener: sessionRequest:" +
                                   " exception " + e);
                e.printStackTrace();
            }
        }

        return(reply);
    }


    public void
    tokenJoined(TokenEvent event) {
        Session session     = event.getSession();
        String  clientName  = event.getClientName();
        Token   token       = event.getToken();

        if (InviteManagerListener_Debug) {
            System.err.println("InviteManagerListener: tokenJoined:" +
                               " event: " + event);

            System.err.println("InviteManagerListener: tokenJoined:" +
                               " client name "    + clientName +
                               " from session "   + session +
                               " token "          + token);
        }

        if (clientName.startsWith("TestUser")) {
            Client clients [] = new Client[1];
            URLString url = URLString.createClientURL(clientHost, clientPort,
                                                 sessionType, "InviteClient");

/*  TestUser has joined the token, so we now invite InviteClient to join
 *  the session.
 */
            try {
                if (!sessionInvited) {
                    inviteClient = ClientFactory.lookupClient(url);
                    clients[0] = inviteClient;

                    System.err.println("Inviting Client to join the session.");
                    session.invite(clients);
                    sessionInvited = true;
                }
            } catch (Exception e) {
                System.err.println("InviteManagerListener:" +
                                   " tokenJoined: exception " + e);
                e.printStackTrace();
            }
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
    tokenExpelled(TokenEvent event) { }


    public boolean
    tokenRequest(Token token, AuthenticationInfo info, Client client) {
        boolean reply;
        String  response;

        if (InviteManagerListener_Debug) {
            System.err.println("InviteManagerListener: tokenRequest:");
        }

        info.setChallenge("MNOPQR");
        response = (String) client.authenticate(info);
        reply    = response.equals("mnopqr");

        if (reply) {
            try {
                System.err.println("InviteManagerListener: tokenRequest:" +
                                   " adding manager's token listener.");
                token.addTokenListener(this);
                this.token = token;
            } catch (Exception e) {
                System.err.println("InviteManagerListener: tokenRequest:" +
                                   " exception " + e);
                e.printStackTrace();
            }
        }

        return(reply);
    }
}
