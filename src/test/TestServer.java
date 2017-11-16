
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
 * The server for the test environment for JSDT.
 *
 * @version     2.3 - 15th November 2017
 * @author      Rich Burridge
 */

public class
TestServer implements TestDebugFlags {

    // The machine running the test server application.
    private String serverHost = null;

    // The port number to use on that server machine.
    private static int serverPort = 0;

    // The machine running the client to invite to join a session.
    private String clientHost = null;

    // The port number to use on that client machine.
    private static int clientPort = 0;

    // The type of session to use.
    private String sessionType = "socket";


    public
    TestServer() {
        if (TestServer_Debug) {
            System.err.println("TestServer: constructor.");
        }
    }


    private void
    create(Client client,
           String sessionName,   SessionManager sessionManager,
           String byteArrayName, ByteArrayManager byteArrayManager,
           String channelName,   ChannelManager channelManager,
           String tokenName,     TokenManager   tokenManager,
           boolean managed) {
        Session session;
        Client  serverClient = new TestClient("Server");
        String  clientName   = client.getName();
        String  m            = (managed) ? "a managed" : "an unmanaged";
        URLString url        = URLString.createSessionURL(serverHost,
                                        serverPort, sessionType, sessionName);

        if (TestServer_Debug) {
            System.err.println("TestServer: create:" +
                                " session name: "      + sessionName +
                                " session manager: "   + sessionManager +
                                " bytearray name: "    + byteArrayName +
                                " bytearray manager: " + byteArrayManager +
                                " channel name: "      + channelName +
                                " channel manager: "   + channelManager +
                                " token name: "        + tokenName +
                                " token manager: "     + tokenManager +
                                " url: "               + url);
        }

        try {
            System.err.println("Creating " + m + " session from the factory.");
            if (managed) {
                session = SessionFactory.createSession(client, url,
                                                       true, sessionManager);
            } else {
                session = SessionFactory.createSession(client, url, true);
            }

            System.err.println("Client: " + clientName + " joined " + m +
                               " session.");

            System.err.println("Session: " + url +
                        " managed: " + SessionFactory.sessionManaged(url));


            System.err.println("Creating " + m + " bytearray: " +
                                byteArrayName);
            if (managed) {
                session.createByteArray(serverClient, byteArrayName,
                                        false, byteArrayManager);
            } else {
                session.createByteArray(serverClient, byteArrayName, false);
            }

            System.err.println("ByteArray: " + byteArrayName +
                        " managed: " + session.byteArrayManaged(byteArrayName));


            System.err.println("Creating " + m + " channel: " + channelName);
            if (managed) {
                session.createChannel(serverClient, channelName,
                                      true, true, false, channelManager);
            } else {
                session.createChannel(serverClient, channelName,
                                      true, true, false);
            }

            System.err.println("Channel: " + channelName +
                        " managed: " + session.channelManaged(channelName));


            System.err.println("Creating " + m + " token: " + tokenName);
            if (managed) {
                session.createToken(serverClient, tokenName,
                                    false, tokenManager);
            } else {
                session.createToken(serverClient, tokenName, false);
            }

            System.err.println("Token: " + tokenName +
                        " managed: " + session.tokenManaged(tokenName));


            String[] byteArrayNames = session.listByteArrayNames();
            System.err.println("Session: " + session.getName() +
                               " contains the following bytearrays:");
            for (int i = 0; i < byteArrayNames.length; i++) {
                System.err.println(byteArrayNames[i]);
            }

            String[] channelNames = session.listChannelNames();
            System.err.println("Session: " + session.getName() +
                               " contains the following channels:");
            for (int i = 0; i < channelNames.length; i++) {
                System.err.println(channelNames[i]);
            }

            String[] tokenNames = session.listTokenNames();
            System.err.println("Session: " + session.getName() +
                               " contains the following tokens:");
            for (int i = 0; i < tokenNames.length; i++) {
                System.err.println(tokenNames[i]);
            }


            System.err.println("Client: " + clientName + " leaving " + m +
                               " session.");
            session.leave(client);
        } catch (Exception e) {
            System.err.println("TestServer: create: exception: " + e);
            e.printStackTrace();
        }
    }


    private static String
    getHost(String args[], boolean isServer) {
        String defHost = "localhost";  // Default host name for connections.
        int length     = args.length;
        String arg     = (isServer) ? "-serverHost" : "-clientHost";

        if (TestServer_Debug) {
            System.err.println("TestServer: getHost.");
        }

        for (int i = 0; i < length; i++) {
            if (args[i].equals(arg)) {
                if (++i < length) {
                    return(args[i]);
                }
            }
        }
        return(defHost);
    }


    private static int
    getPort(String args[], boolean isServer) {
        int defPort = 4465;        // Default port number for connections.
        int length  = args.length;
        String arg  = (isServer) ? "-serverPort" : "-clientPort";

        if (TestServer_Debug) {
            System.err.println("TestServer: getPort.");
        }

        for (int i = 0; i < length; i++) {
            if (args[i].equals(arg)) {
                if (++i < length) {
                    return(Integer.parseInt(args[i]));
                }
            }
        }
        return(defPort);
    }


    private static String
    getType(String args[]) {
        String defType = "socket";   // Default Session type.
        int length = args.length;

        if (TestServer_Debug) {
            System.err.println("TestServer: getType.");
        }

        for (int i = 0; i < length; i++) {
            if (args[i].equals("-type")) {
                if (++i < length) {
                    return(args[i]);
                }
            }
        }
        return(defType);
    }


    private void
    run(TestClient client) {
        System.err.println("******testStart******");
        testStart(client, "TempSession", sessionType);
        System.err.println("******test1******");
        test1(client);
        System.err.println("******test2******");
        test2(client);
        System.err.println("******test3******");
        test3(client);
        System.err.println("******test4******");
        test4(client);
        System.err.println("******test5******");
        test5(client);
        System.err.println("******test6******");
        test6(client);
        System.err.println("******test7******");
        test7(client);

        System.err.println("******testA******");
        testA("DestroySession", "DestroyChannel", "DestroyToken");

        System.err.println("******testB******");
        testB("JoinExistSession", "JoinExistByteArray",
              "JoinExistChannel", "JoinExistToken");

        System.err.println("******testEnd******");
        testEnd();
    }


    private void
    testStart(TestClient client, String sessionName, String sessionType) {
        try {
            URLString url = URLString.createSessionURL(serverHost, serverPort,
                                                   sessionType, sessionName);

            System.err.println("Creating a temporary session.");
            SessionFactory.createSession(client, url, false);

            System.err.println("Destroying the temporary session.");
            SessionFactory.destroySession(client, url);
        } catch (Exception e) {
            System.err.println("TestServer: testStart: exception: " + e);
            e.printStackTrace();
        }
    }


    private void
    test1(TestClient client) {
        create(client, "TestSession", null, "TestByteArray", null,
                       "TestChannel", null, "TestToken",     null, false);

        TestManagerListener tmo = new TestManagerListener("ManagedSession");
        create(client, "ManagedSession", tmo, "ManagedByteArray", tmo,
                       "ManagedChannel", tmo, "ManagedToken",     tmo, true);
    }


    private void
    test2(TestClient client) {
        ExpelManagerListener emo =
                      new ExpelManagerListener("ExpelManagerListener");
        create(client, "ExpelSession", emo, "ExpelByteArray", emo,
                       "ExpelChannel", emo, "ExpelToken",     emo, true);
    }


    private void
    test3(TestClient client) {
        InviteManagerListener imo =
            new InviteManagerListener("InviteManagerListener",
                                      clientHost, (clientPort+1), sessionType);
        create(client, "InviteSession", imo, "InviteByteArray", imo,
                       "InviteChannel", imo, "InviteToken",     imo, true);
    }


    private void
    test4(TestClient client) {
        create(client, "SendSession", null, "SendByteArray", null,
                       "SendChannel", null, "SendToken",     null, false);
    }


    private void
    test5(TestClient client) {
        ReceiveManager rm =
                      new ReceiveManager("ReceiveManager");
        create(client, "ReceiveSession", rm, "ReceiveByteArray", rm,
                       "ReceiveChannel", rm, "ReceiveToken",     rm, true);
    }


    private void
    test6(TestClient client) {
        create(client, "GiveSession", null, "GiveByteArray", null,
                       "GiveChannel", null, "GiveToken",     null, false);
    }


    private void
    test7(TestClient client) {
        create(client, "RequestSession", null, "RequestByteArray", null,
                       "RequestChannel", null, "RequestToken",     null, false);
    }


/* Server-side only test for Channel.destroy(), Token.destroy() and
 * Session.destroy().
 */

    private void
    testA(String sessionName, String channelName, String tokenName) {
        Client  client     = new TestClient("Server");
        String  clientName = client.getName();
        URLString url      = URLString.createSessionURL(serverHost, serverPort,
                                                     sessionType, sessionName);
        Session session;

        if (TestServer_Debug) {
            System.err.println("TestServer: testA:" +
                               " session name: "    + sessionName +
                               " channel name: "    + channelName +
                               " token name: "      + tokenName +
                               " url: "             + url);
        }

        try {
            Channel channel;
            Token   token;

            System.err.println("Creating session: " + sessionName);
            session = SessionFactory.createSession(client, url, true);

            System.err.println("Client: " + clientName + " joined session.");

            System.err.println("Creating channel: " + channelName);
            channel = session.createChannel(client, channelName,
                                            true, true, false);

            System.err.println("Client: " + clientName + " joining channel.");
            channel.join(client);

            System.err.println("Creating token: " + tokenName);
            token = session.createToken(client, tokenName, false);

            System.err.println("Client: " + clientName + " joining token.");
            token.join(client);

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                System.err.println("TestServer: testA: exception " + e);
                e.printStackTrace();
            }

            System.err.println("Destroying channel: " + channelName);
            channel.destroy(client);

            System.err.println("Destroying token: " + tokenName);
            token.destroy(client);

            System.err.println("Destroying session: " + sessionName);
            session.destroy(client);
        } catch (Exception e) {
            System.err.println("TestServer: testA: exception: " + e);
            e.printStackTrace();
        }
    }


/* Server-side only test for:
 *
 * Session.byteArrayExists()
 * Session.channelExists()
 * Session.tokenExists()
 * Session.getByteArraysJoined()
 * Session.getChannelsJoined()
 * Session.getTokensJoined()
 */

    private void
    testB(String sessionName, String byteArrayName,
          String channelName, String tokenName) {
        Client  client     = new TestClient("Server");
        String  clientName = client.getName();
        URLString url      = URLString.createSessionURL(serverHost, serverPort,
                                                     sessionType, sessionName);
        Session session;

        if (TestServer_Debug) {
            System.err.println("TestServer: testB:" +
                                " session name: "    + sessionName +
                                " byte array name: " + byteArrayName +
                                " channel name: "    + channelName +
                                " token name: "      + tokenName +
                                " url: "             + url);
        }

        try {
            ByteArray byteArray, byteArrays[];
            Channel   channel, channels[];
            Token     token, tokens[];

            System.err.println("Creating session: " + sessionName);
            session = SessionFactory.createSession(client, url, true);

            System.err.println("Client: " + clientName + " joined session.");

            System.err.println("Creating byte array: " + byteArrayName);
            byteArray = session.createByteArray(client, byteArrayName, false);

            System.err.println("Client: " + clientName + " joining byte array.");
            byteArray.join(client);

            System.err.println("Creating channel: " + channelName);
            channel = session.createChannel(client, channelName,
                                            true, true, false);

            System.err.println("Client: " + clientName + " joining channel.");
            channel.join(client);

            System.err.println("Creating token: " + tokenName);
            token = session.createToken(client, tokenName, false);

            System.err.println("Client: " + clientName + " joining token.");
            token.join(client);

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                System.err.println("TestServer: testB: exception " + e);
                e.printStackTrace();
            }

            System.err.println("Checking if byte array: " + byteArrayName +
                                "exists: " +
                                session.byteArrayExists(byteArrayName));
            System.err.println("Checking if byte array: " + "BogusByteArray" +
                                "exists: " +
                                session.byteArrayExists("BogusByteArray"));

            System.err.println("Checking if channel: " + channelName +
                                "exists: " +
                                session.channelExists(channelName));
            System.err.println("Checking if channel: " + "BogusChannel" +
                                "exists: " +
                                session.channelExists("BogusChannel"));

            System.err.println("Checking if token: " + tokenName +
                                "exists: " +
                                session.tokenExists(tokenName));
            System.err.println("Checking if token: " + "BogusToken" +
                                "exists: " +
                                session.tokenExists("BogusToken"));

            byteArrays = session.getByteArraysJoined(client);
            System.err.println("Byte arrays joined:");
            for (int i = 0; i < byteArrays.length ; i++) {
                System.err.println("byteArrays[" + i + "]: " + byteArrays[i]);
            }

            channels = session.getChannelsJoined(client);
            System.err.println("Channels joined:");
            for (int i = 0; i < channels.length ; i++) {
                System.err.println("channels[" + i + "]: " + channels[i]);
            }

            tokens = session.getTokensJoined(client);
            System.err.println("Tokens joined:");
            for (int i = 0; i < tokens.length ; i++) {
                System.err.println("tokens[" + i + "]: " + tokens[i]);
            }
        } catch (Exception e) {
            System.err.println("TestServer: testB: exception: " + e);
            e.printStackTrace();
        }
    }


    private void
    testEnd() {
        try {
            URLString[] registryObjects = RegistryFactory.list();
            if (registryObjects.length == 0) {
                System.err.println("The registry is empty.");
            } else {
                System.err.println("The registry contains the following:");
                for (int i = 0; i < registryObjects.length; i++) {
                    System.err.println(registryObjects[i]);
                }
            }
        } catch (Exception e) {
            System.err.println("TestServer: testEnd: exception: " + e);
            e.printStackTrace();
        }
    }


    public static void
    main(String args[]) {
        Connection.setProperty("showMessage", "true");
        Connection.setProperty("showStack",   "true");

        TestServer server = new TestServer();
        TestClient client = new TestClient("Server");

        if (TestServer_Debug) {
            System.err.println("TestServer: main.");
        }

        server.serverHost  = getHost(args, true);
        server.serverPort  = getPort(args, true);
        server.clientHost  = getHost(args, false);
        server.clientPort  = getPort(args, false);
        server.sessionType = getType(args);
        System.err.println("TestServer: main:" +
                           " server host name: "    + server.serverHost +
                           " server port: "         + server.serverPort +
                           " session/client type: " + server.sessionType);

// Registry running?  Start it if it isn't.

        try {
            if (!RegistryFactory.registryExists(server.sessionType)) {
               RegistryFactory.startRegistry(server.sessionType);
            }
        } catch (JSDTException e) {
            System.err.println("TestServer: main: shared data exception: " + e);
            if (TestServer_Debug) {
                e.printStackTrace();
            }
        }

        server.run(client);
        System.err.println("Setup and bound Test server.");
    }
}
