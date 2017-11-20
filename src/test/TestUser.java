
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

import java.applet.Applet;
import com.sun.media.jsdt.*;
import com.sun.media.jsdt.event.*;

/**
 * The user for the test environment for JSDT.
 *
 * @version     2.3 - 15th November 2017
 * @author      Rich Burridge
 */

public class
TestUser extends Applet implements TestDebugFlags {

    // Are we running as an application (as opposed to an applet)?
    private boolean isApplication = false;

    // The maximum number of users joining this session.
    private static final int MAXCLIENTS = 1;

    // The name of this user. */
    protected String name;

    // The machine running the test server application.
    private String serverHost = "localhost";

    // The port number to use on that server machine.
    private static int serverPort = 4467;

    // Machine name of the client to invite to join this session.
    private String clientHost = "localhost";

    // The port number to use on that machine.
    private static int clientPort = 4567;

    // The type of session to use.
    private String sessionType = "socket";


    private void
    getOptions(String args[]) {
        com.sun.media.jsdt.impl.JSDTObject.showMessage = true;
        com.sun.media.jsdt.impl.JSDTObject.showStack   = true;

        if (TestUser_Debug) {
            System.err.println("TestUser: getOptions:");
            for (int i = 0; i < args.length ; i++) {
                System.err.println("args[" + i + "]: " + args[i]);
            }
        }

        if (getArg(args, "serverHost") != null) {
            serverHost = getArg(args, "serverHost");
        }

        if (getArg(args, "clientHost") != null) {
            clientHost = getArg(args, "clientHost");
        }

        if (getArg(args, "serverPort") != null) {
            serverPort = Integer.parseInt(getArg(args, "serverPort"));
        }

        if (getArg(args, "clientPort") != null) {
            clientPort = Integer.parseInt(getArg(args, "clientPort"));
        }

        if (getArg(args, "type") != null) {
            sessionType = getArg(args, "type");
        }
    }


    private String
    getArg(String args[], String arg) {
        if (TestUser_Debug) {
            System.err.println("TestUser: getArg:" +
                                " arg: " + arg);
        }

        if (isApplication) {
            String option = "-" + arg;
            String retval = null;

            for (int i = 0 ; i < args.length ; i++) {
                if (args[i].equals(option)) {
                    if (++i < args.length) {
                        retval = args[i];
                    }
                    break;
                }
            }
            return(retval);
        } else {
            return(getParameter(arg));
        }
    }


    public void
    init() {
        if (TestUser_Debug) {
            System.err.println("TestUser: init.");
        }

        getOptions(null);
        initialize("TestUser");
        run();
    }


    private void
    initialize(String name) {
        int n = rrange(20, 100) * rrange(1, 10000);

        if (TestUser_Debug) {
            System.err.println("TestUser: initialize.");
        }

        this.name = name + n;
    }


/* Generate a random number between low and high. */

    private int
    rrange(int low, int high) {
        return((int)(Math.random() * (high - low + 1) + low));
    }


/* Sleep for a random number of seconds between the low and high values. */

    public static void
    rsleep(int low, int high) {
        if (TestUser_Debug) {
            System.err.println("TestUser:" +
                               "rsleep(" + low + "," + high + ").");
        }

        for (int i = 0; i < (int) (Math.random() * (high-low+1) + low); i++) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                System.err.println("TestUser: rrange: exception " + e);
                e.printStackTrace();
            }
        }
    }


/*  Generate some random data (random start number and random length) and
 *  send it over the data channel.
 */

    private void
    writeData(Channel channel, Client client) {
        Data data;         // The data that will be sent over the channel.
        byte[] numbers = new byte[100];
        int dstart     = 0;
        int dlen       = rrange(10, 50);

        for (int i = dstart; i < dstart + dlen; i++) {
            numbers[i] = (byte) i;
        }
        try {
            int priority = rrange(0, 3);

            data = new Data(numbers, dlen);
            data.setPriority(priority);
            System.err.println("Writing " + dlen +
                               " bytes of data at priority " + priority);
            channel.sendToAll(client, data);
        } catch (Exception e) {
            System.err.println("TestUser: writeData: exception " + e);
            e.printStackTrace();
        }
    }


    private void
    run() {
        System.err.println("******testA******");
        testA();

        System.err.println("******test1(unmanaged)******");
        test1(URLString.createSessionURL(serverHost, serverPort,
                                         sessionType, "TestSession"), false);
        System.err.println("******test1(managed)******");
        test1(URLString.createSessionURL(serverHost, serverPort,
                                         sessionType, "ManagedSession"), true);

        System.err.println("******test2******");
        test2(URLString.createSessionURL(serverHost, serverPort,
                                         sessionType, "ExpelSession"));

        System.err.println("******test3******");
        test3(URLString.createSessionURL(serverHost, serverPort,
                                         sessionType, "InviteSession"));

        System.err.println("******test4******");
        test4(URLString.createSessionURL(serverHost, serverPort,
                                         sessionType, "SendSession"));

        System.err.println("******test5******");
        test5(URLString.createSessionURL(serverHost, serverPort,
                                         sessionType, "ReceiveSession"));

        System.err.println("******test6******");
        test6(URLString.createSessionURL(serverHost, serverPort,
                                         sessionType, "GiveSession"));

        System.err.println("******test7******");
        test7(URLString.createSessionURL(serverHost, serverPort,
                                         sessionType, "RequestSession"));

        System.err.println("******test8******");
        test8();
    }


    private void
    testA() {
        URLString goodUrl = URLString.createSessionURL(serverHost, serverPort,
                                                  sessionType, "TestSession");
        URLString badUrl  = URLString.createSessionURL(serverHost, serverPort,
                                                  sessionType, "BogusSession");

        try {
            System.err.println("Testing existing of: " + goodUrl);
            System.err.println("Exists: " +
                                SessionFactory.sessionExists(goodUrl));

            System.err.println("Testing existing of: " + badUrl);
            System.err.println("Exists: " +
                                SessionFactory.sessionExists(badUrl));
        } catch (JSDTException e) {
            System.err.println("TestUser: testA: exception " + e);
            e.printStackTrace();
        }
    }


    private void
    test1(URLString url, boolean managed) {
        Channel             channel;
        ChannelConsumer     channelConsumer;
        Client              client;
        Session             session;
        TestManagerListener tmo;
        Token               token;

        try {
            String m           = (managed) ? "a managed"      : "an unmanaged";
            String channelName = (managed) ? "ManagedChannel" : "TestChannel";
            String tokenName   = (managed) ? "ManagedToken"   : "TestToken";

            System.err.println("Creating a client called: "+ this.name);
            client = new TestClient(this.name);

            System.err.println("Create a test manager/listener.");
            tmo = new TestManagerListener("TestManagerListener");

            System.err.println("Creating a session for: " + url);
            session = SessionFactory.createSession(client, url, false);

            System.err.println("Client: " + this.name + " joining " + m +
                               " session.");
            session.join(client);

            System.err.println("Adding the test listener to the session.");
            session.addSessionListener(tmo);

/*  Check to see if there are MAXCLIENTS clients joined to the session. If
 *  false then we put ourself to sleep for upto two seconds, then continually
 *  retry until it's true.
 */

            System.err.println("Waiting until there are " + MAXCLIENTS +
                               " joined to this session...");
            while (true) {
                String clientNames[] = session.listClientNames();

                if (TestUser_Debug) {
                    int len;

                    if (clientNames == null) {
                        len = 0;
                    } else {
                        len = clientNames.length;
                    }
                    System.err.println("TestUser: main - " +
                         "client check for session [has " + len + " clients].");
                }

                if (clientNames.length == MAXCLIENTS) {
                    break;
                }
                TestUser.rsleep(1, 2);
            }

            System.err.println("Joining " + m + " token called: " + tokenName);
            token = session.createToken(client, tokenName, false);
            token.join(client);

            System.err.println("Inhibiting the token.");
            token.grab(client, false);

            System.err.println("Token status is: " + token.test());
            System.err.println("Status (stringified): " + token.toString());

            System.err.println("Adding the test listener to the token.");
            token.addTokenListener(tmo);

/*  Check to see if there are MAXCLIENTS clients joined to the session. If
 *  false then we put ourself to sleep for upto two seconds, then continually
 *  retry until it's true.
 */

            System.err.println("Waiting until there are " + MAXCLIENTS +
                               " joined to this token...");
            while (true) {
                String clientNames[] = token.listClientNames();

                if (TestUser_Debug) {
                    int len;

                    if (clientNames == null) {
                        len = 0;
                    } else {
                        len = clientNames.length;
                    }
                    System.err.println("TestUser: main - " +
                           "client check for token [has " + len + " clients].");
                }

                if (clientNames.length == MAXCLIENTS) {
                    break;
                }
                TestUser.rsleep(1, 2);
            }

            TestUser.rsleep(2, 5);

            System.err.println("Joining " + m + " channel called: " +
                                channelName);
            channel = session.createChannel(client, channelName,
                                            true, true, false);
            channel.join(client);

            if (channel.isOrdered()) {
                System.err.println("The channel is ordered.");
            } else {
                System.err.println("The channel is not ordered.");
            }

            if (channel.isReliable()) {
                System.err.println("The channel is reliable.");
            } else {
                System.err.println("The channel is not reliable.");
            }

            System.err.println("Creating a channel consumer.");
            channelConsumer = new TestChannelConsumer(client.getName());

            System.err.println("Set the channel consumer.");
            channel.addConsumer(client, channelConsumer);

            System.err.println("Adding the test listener to the channel.");
            channel.addChannelListener(tmo);

/*  The channel has been joined so remove the listener, release and leave
 *  the token.
 */

            System.err.println("Removing the token listener.");
            token.removeTokenListener(tmo);

            System.err.println("Release the token.");
            token.release(client);

            System.err.println("Leaving the token.");
            token.leave(client);

/*  Check to see if the token is not being inhibited by anyone. If this is
 *  true, then everybody should have successfully joined the channel.
 *  If the token is still inhibited, then sleep for upto two seconds, then
 *  continually retry until it's released.
 */

            System.err.println("Wait for the token to be free...");
            while (true) {
                int tokenStatus = token.test();

                if (tokenStatus == Token.NOT_IN_USE) {
                    break;
                }

                if (TestUser_Debug) {
                    System.err.println("TestUser: run: for session " +
                                session + "." + "Token status is: " + token);
                }

                TestUser.rsleep(1, 2);
            }

            TestUser.rsleep(3, 5);

            System.err.println("Write some data over the channel.");
            for (int i = 0; i < 10; i++) {
                writeData(channel, client);
            }

            TestUser.rsleep(3, 5);

// Leave the channel and leave the session, removing their listeners too.

            System.err.println("Remove the channel listener.");
            channel.removeChannelListener(tmo);

            System.err.println("Leave the channel.");
            channel.leave(client);

            System.err.println("Remove the session listener.");
            session.removeSessionListener(tmo);

            System.err.println("Leave the session.");
            session.leave(client);
        } catch (Exception e) {
            System.err.println("TestUser: test1: exception " + e);
            e.printStackTrace();
        }
    }


    private void
    test2(URLString url) {
        Channel           channel;
        Session           session;
        Token             token;
        ExpelClient       expelClient;
        ExpelUserListener euo;

        String    sessionName = "ExpelSession";
        String    channelName = "ExpelChannel";
        String    tokenName   = "ExpelToken";
        URLString clientUrl = URLString.createClientURL(clientHost, clientPort,
                                                  sessionType, "ExpelClient");
        String    clientName  = this.name;

        try {
            System.err.println("Creating client ExpelClient.");
            expelClient = new ExpelClient("ExpelClient");

            System.err.println("Getting the expel session for: " + url);
            session = SessionFactory.createSession(expelClient, url, false);

            System.err.println("Adding to registry via the Client factory.");
            ClientFactory.createClient(expelClient, clientUrl, expelClient);

            System.err.println("Creating a channel/session/token listener.");
            euo = new ExpelUserListener("ExpelUserListener");

            channel = session.createChannel(expelClient, channelName,
                                            true, true, false);
            token = session.createToken(expelClient, tokenName, false);

            System.err.println("Adding the listener to the session.");
            session.addSessionListener(euo);

            System.err.println("Adding the listener to the channel.");
            channel.addChannelListener(euo);

            System.err.println("Adding the listener to the token.");
            token.addTokenListener(euo);

            System.err.println("Client: " + clientName +
                               " joining session: " + sessionName);
            session.join(expelClient);

            System.err.println("Client: " + clientName +
                               " joining channel: " + channelName);
            channel.join(expelClient);

            System.err.println("Client: " + clientName +
                               " joining token: " + tokenName);
            token.join(expelClient);

/*  The channel/session/token manager will now expel the client from the
 *  token, channel and session (in that order). Go to sleep while this happens.
 */
            System.err.println("Waiting for the client to be expelled from" +
                               " the token, channel and session.");
            TestUser.rsleep(10, 12);

        } catch (Exception e) {
            System.err.println("TestUser: test2: exception " + e);
            e.printStackTrace();
        }
    }


    private void
    test3(URLString url) {
        Channel      channel;
        Client       client;
        Session      session;
        Token        token;
        InviteClient inviteClient;

        URLString clientUrl  = URLString.createClientURL(clientHost,
                                    clientPort+1, sessionType, "InviteClient");
        String  sessionName  = "InviteSession";
        String  channelName  = "InviteChannel";
        String  tokenName    = "InviteToken";
        String  clientName   = this.name;

        try {
            System.err.println("Creating a client called: "+ this.name);
            client = new TestClient(clientName);

            System.err.println("Getting the invite session for: " + url);
            session = SessionFactory.createSession(client, url, false);

            System.err.println("Creating client InviteClient.");
            inviteClient = new InviteClient("InviteClient");

            System.err.println("Adding to registry via the Client factory.");
            ClientFactory.createClient(inviteClient, clientUrl, inviteClient);

            System.err.println("Client: " + clientName +
                               " joining session: " + sessionName);
            session.join(client);

            System.err.println("Client: " + clientName +
                               " joining channel: " + channelName);
            channel = session.createChannel(client, channelName,
                                            true, true, false);
            channel.join(client);

            System.err.println("Client: " + clientName +
                               " joining token: " + tokenName);
            token = session.createToken(client, tokenName, false);
            token.join(client);

/*  The channel/session/token manager will now invite client InviteClient to
 *  join the session, channel and token (in that order). Go to sleep while
 *  this happens.
 */
            TestUser.rsleep(20, 22);
            System.err.println("Test3 terminating.");
        } catch (Exception e) {
            System.err.println("TestUser: test3: exception " + e);
            e.printStackTrace();
        }
    }


    private void
    test4(URLString url) {
        Channel         channel1;
        Channel         channel2;
        ChannelConsumer channel2Consumer;
        Client          client1;
        Client          client2;
        Data            data;
        Session         session;
        String  channelName = "SendChannel";
        String  sessionName = "SendSession";
        String  client1Name = "Client1";
        String  client2Name = "Client2";
        int     dlen        = 100;
        byte[]  numbers     = new byte[dlen];

        try {
            System.err.println("Creating a client called: "+ client1Name);
            client1 = new TestClient(client1Name);

            System.err.println("Getting the send session for: " + url);
            session = SessionFactory.createSession(client1, url, false);

            System.err.println("Client: " + client1Name +
                               " joining session: " + sessionName);
            session.join(client1);
            System.err.println("Client: " + client1Name +
                               " joining channel: " + channelName);
            channel1 = session.createChannel(client1, channelName,
                                             true, true, false);
            channel1.join(client1);

            System.err.println("Creating a client called: "+ client2Name);
            client2 = new TestClient(client2Name);
            System.err.println("Client: " + client2Name +
                               " joining session: " + sessionName);
            session.join(client2);
            System.err.println("Client: " + client2Name +
                               " joining channel: " + channelName);
            channel2 = session.createChannel(client2, channelName,
                                             true, true, false);
            channel2.join(client2);

            System.err.println("Creating a channel consumer.");
            channel2Consumer = new TestChannelConsumer(client2Name);

            System.err.println("Setting channel consumer for " + client2Name);
            channel2.addConsumer(client2, channel2Consumer);

            System.err.println("Consumer for channel2 is: " + channel2Consumer);

            for (int count = 0; count < 10; count++) {
                for (int i = 0; i < dlen; i++) {
                    numbers[i] = (byte) i;
                }
                data = new Data(numbers);
                data.setPriority(Channel.HIGH_PRIORITY);
                System.err.println("Sending data from " + client1Name +
                                   " to " + client2Name);
                channel1.sendToClient(client1, client2Name, data);
            }
        } catch (Exception e) {
            System.err.println("TestUser: test4: exception " + e);
            e.printStackTrace();
        }
    }


    private void
    test5(URLString url) {
        Channel channel;
        Client  client;
        Data    data;
        Session session;
        Thread  thread;
        String  channelName = "ReceiveChannel";
        String  clientName  = "Test5Client";
        int     count       = 0;

        try {
            System.err.println("Creating a client called: "+ clientName);
            client = new TestClient(clientName);

            System.err.println("Creating/joining the Receive session for: " +
                                url);
            session = SessionFactory.createSession(client, url, true);

            System.err.println("Client: " + clientName +
                               " joining channel: " + channelName);
            channel = session.createChannel(client, channelName,
                                            true, true, true);

            thread = new SendData(session, channelName);
            thread.start();

            while (count < 10) {
                TestUser.rsleep(1, 2);
                System.err.println("Checking if data is available...");
                if (channel.dataAvailable(client)) {
                    System.err.println("Data IS available.");
                    data = channel.receive(client);
                    System.err.println("Received data: " + data +
                                       " channel: "     + data.getChannel() +
                                       " sender name: " + data.getSenderName());
                    count++;
                }
            }
        } catch (Exception e) {
            System.err.println("TestUser: test5: exception " + e);
            e.printStackTrace();
        }
    }


    private void
    test6(URLString url) {
        Client         giverClient;
        ReceiverClient receiverClient;
        Session        session;
        Token          giverToken;
        Token          receiverToken;

        String         tokenName    = "GiveToken";
        String         sessionName  = "GiveSession";
        String         giverName    = "Giver";
        String         receiverName = "Receiver";

        try {
            System.err.println("Creating a client called: "+ giverName);
            giverClient = new GiverClient(giverName);

            System.err.println("Getting the Give session for: " + url);
            session = SessionFactory.createSession(giverClient, url, false);

            System.err.println("Client: " + giverName +
                               " joining session: " + sessionName);
            session.join(giverClient);

            System.err.println("Client: " + giverName +
                               " joining token: " + tokenName);
            giverToken = session.createToken(giverClient, tokenName, true);

            System.err.println("Adding " + giverName +
                               " as a token listener.");
            giverToken.addTokenListener((TokenListener) giverClient);

            System.err.println(giverName + " grabs the token.");
            giverToken.grab(giverClient, true);


            System.err.println("Creating a client called: "+ receiverName);
            receiverClient = new ReceiverClient(receiverName);
            System.err.println("Client: " + receiverName +
                               " joining session: " + sessionName);
            session.join(receiverClient);
            System.err.println("Client: " + receiverName +
                               " joining token: " + tokenName);
            receiverToken = session.createToken(receiverClient, tokenName, true);
            receiverClient.setToken(receiverToken);

            while (receiverToken.listClientNames().length == 2) {
                System.err.println("Client: " + receiverName +
                               " waiting for giver to leave the token.");
                TestUser.rsleep(1, 2);
            }
            System.err.println("TestUser: test6: completing.");
        } catch (Exception e) {
            System.err.println("TestUser: test6: exception " + e);
            e.printStackTrace();
        }
    }


    private void
    test7(URLString url) {
        Client  holderClient;
        Client  requesterClient;
        Session session;
        Token   holderToken;
        Token   requesterToken;

        String  tokenName     = "RequestToken";
        String  sessionName   = "RequestSession";
        String  holderName    = "Holder";
        String  requesterName = "Requester";

        try {
            System.err.println("Creating a client called: "+ holderName);
            holderClient = new HolderClient(holderName);

            System.err.println("Getting the Request session for: " + url);
            session = SessionFactory.createSession(holderClient, url, false);

            System.err.println("Client: " + holderName +
                               " joining session: " + sessionName);
            session.join(holderClient);

            System.err.println("Client: " + holderName +
                               " joining token: " + tokenName);
            holderToken = session.createToken(holderClient, tokenName, false);
            holderToken.join(holderClient);

            System.err.println("Adding " + holderName +
                               " as a token listener.");
            holderToken.addTokenListener((TokenListener) holderClient);

            System.err.println(holderName + " grabs the token.");
            holderToken.grab(holderClient, true);


            System.err.println("Creating a client called: "+ requesterName);
            requesterClient = new RequesterClient(requesterName);
            System.err.println("Client: " + requesterName +
                               " joining session: " + sessionName);
            session.join(requesterClient);
            System.err.println("Client: " + requesterName +
                               " joining token: " + tokenName);
            requesterToken = session.createToken(requesterClient, tokenName,
                                                 false);
            requesterToken.join(requesterClient);

            System.err.println("Adding " + requesterName +
                               " as a token listener.");
            requesterToken.addTokenListener((TokenListener) requesterClient);

            System.err.println(requesterName + " requests the token.");
            requesterToken.request(requesterClient);

            while (requesterToken.listClientNames().length == 2) {
                TestUser.rsleep(1, 2);
            }
            TestUser.rsleep(5, 10);
        } catch (Exception e) {
            System.err.println("TestUser: test6: exception " + e);
            e.printStackTrace();
        }
    }


    private void
    test8() {
        try {
            System.err.println("Throw shared data exception (no parameters).");
            throw new JSDTException();
        } catch (JSDTException e) {
            System.err.println("Caught shared data exception: " +
                               " type: "        + e.getType() +
                               " stringified: " + e.typeToString(e.getType()));
        }

        try {
            System.err.println("Throw shared data exception (by string).");
            throw new JSDTException("New shared data exception");
        } catch (JSDTException e) {
            System.err.println("Caught shared data exception: " + e);
        }

        try {
            System.err.println("Throw shared data exception (by type).");
            throw new NoSuchSessionException();
        } catch (NoSuchSessionException e) {
            System.err.println("Caught shared data exception: " +
                               " type: "        + e.getType() +
                               " stringified: " + e.typeToString(e.getType()));
        }
    }


    public static void
    main(String args[]) {
        Connection.setProperty("showMessage", "true");
        Connection.setProperty("showStack",   "true");

        TestUser user = new TestUser();

        if (TestUser_Debug) {
            System.err.println("TestUser: main.");
        }

        user.isApplication = true;
        user.getOptions(args);
        user.initialize("TestUser");
        user.run();
        System.exit(0);
    }
}
