
/*
 *  Copyright (c) 1996-2005 James Begole.
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

package examples.ppong;

import com.sun.media.jsdt.*;
import com.sun.media.jsdt.event.*;
import java.io.*;
import java.util.*;

/**
 * Description: The server for a two player network game.
 *              Based on classic Pong.
 *
 * @version     2.3 - 20th November 2017
 * @author      James "Bo" Begole
 * @author      Rich Burridge
 *
 */

public class
PpongServer {

    private static final String version = "1.5";


    private static int
    getPort(String args[]) {
        int defPort = 4462;   // Default port number for connections.

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-port")) {
                if (++i < args.length) {
                    try {
                        return(Integer.parseInt(args[i]));
                    } catch (Exception e) {
                        System.out.println("Default port: " + defPort);
                    }
                }
            }
        }
        return(defPort);
    }


    private static String
    getServer(String args[]) {
        String defHost = "localhost";  // Default host name for connections.

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-server")) {
                if (++i < args.length) {
                    return(args[i]);
                }
            }
        }
        return(defHost);
    }


    private static String
    getType(String args[]) {
        String defType = "socket";   // Default Session type.
        int length = args.length;

        for (int i = 0; i < length; i++) {
            if (args[i].equals("-type")) {
                if (++i < length) {
                    return(args[i]);
                }
            }
        }
        return(defType);
    }


    public static void
    main(String args[]) {
        String    server  = getServer(args);
        String    type    = getType(args);
        int       port    = getPort(args);
        URLString url     = URLString.createSessionURL(server, port,
                                                       type, "PpongSession");
        Client    client  = new PpongClient("PpongServer");
        Session   session = null;

        System.out.println("Initializing Ppong! Server.  Version " + version);
        System.out.println("\tInitializing port " + port +
                           ": " + new Date());

        try {
            if (!RegistryFactory.registryExists(type)) {
                RegistryFactory.startRegistry(type);
            }

            session = SessionFactory.createSession(client, url, true);
        } catch (JSDTException e) {
            System.out.println("PpongServer: main: exception: " + e);
        }

        new PortListener(session, client, server);
        new ScoreListener(session, client);
    }
}


class
ScoreListener implements ChannelConsumer {

    public
    ScoreListener(Session session, Client client) {
        System.out.println("ScoreListener: constructor: " + new Date());

        try {
            Channel channel = session.createChannel(client, "ScoreChannel",
                                            true, true, true);
            channel.addConsumer(client, this);
        } catch (JSDTException e) {
            System.err.println("ScoreListener: constructor:" +
                               " exception: " + e + ": " + new Date());
        }
    }


    public void
    dataReceived(Data data) {
        ByteArrayInputStream bais =
                        new ByteArrayInputStream(data.getDataAsBytes());
        DataInputStream      dis  = new DataInputStream(bais);

        try {
            char type = dis.readChar();

            switch (type) {
                case Message.M_SCORE:
                    System.out.println("ScoreListener:Sender " +
                                       data.getSenderName() + ":" +
                                       "Score: Game Number: " +
                                       dis.readLong() + " between " +
                                       dis.readUTF() + ": " + dis.readInt() +
                                       " vs. " +
                                       dis.readUTF() + ": " + dis.readInt());
            }
        } catch (IOException ioe) {
            System.err.println("ScoreListener: dataReceived:" +
                               " exception: " + ioe);
        }
    }
}


class
PortListener extends ChannelAdaptor implements ChannelManager {

    // Size of 5 initially, grow by 2.
    private static final Vector<GameHandler> currentGames = new Vector<>(5, 2);

    private Channel channel;

    private final Client  client;
    private final String server;

    private boolean player1Found = false;

    private Player player1;


    public
    PortListener(Session session, Client client, String server) {
        System.out.println("PortListener: constructor: " + new Date());

        this.client  = client;
        this.server  = server;

        try {
            channel = session.createChannel(client, "GameChannel",
                                            true, true, true, this);
            channel.addChannelListener(this);
        } catch (JSDTException e) {
            System.err.println("PortListener: run : constructor:" +
                               " exception: " + e + ": " + new Date());
        }
    }


    public boolean
    channelRequest(Channel channel, AuthenticationInfo info, Client client) {
        final int MAXNUMGAMES = 5;
        boolean allowClient = true;

        System.out.println("Num Games:"  + currentGames.size() +
                           ": " + new Date());

// Clean up the currentGames vector if needed.

        for (int i = 0; i < currentGames.size(); i++) {
            if (!currentGames.elementAt(i).isAlive()) {
                currentGames.removeElementAt(i);
            }
        }

// If we have the maximum allowed connections.

        if (currentGames.size() >= MAXNUMGAMES) {
            allowClient = false;
        }

        return(allowClient);
    }


    public synchronized void
    channelConsumerAdded(ChannelEvent event) {
        System.err.println("channelConsumerAdded called.");
        if (!player1Found) {
            System.out.println("Player1: " + event.getClientName() +
                               ": " + new Date());
            player1 = new Player(channel, event.getClientName(),
                                 client, server);
            player1Found = true;
            player1.send(Message.writeServerMessage(
                         "Waiting for a second player..."));
        } else {
            Player player2;

            System.out.println("Player 2: " + event.getClientName() +
                               ": " + new Date());
            player2 = new Player(channel, event.getClientName(),
                                 client, server);
            player1.send(Message.writeOpponent(player2.getName()));
            player2.send(Message.writeOpponent(player1.getName()));

            GameHandler game = new GameHandler(channel, player1, player2);
            currentGames.addElement(game);

            player1Found = false;
        }
    }


    public synchronized void
    channelLeft(ChannelEvent event) {
        if (player1 != null) {
            if (event.getClientName().equals(player1.getName())) {
                player1Found = false;
            }
        }
    }
}


class
Player extends ChannelAdaptor {

    public Channel channel    = null;
    public String  clientName = null;
    public Client  client     = null;
    public String  server     = null;
    public boolean connected  = true;


    public
    Player(Channel channel, String clientName, Client client, String server) {
        this.channel    = channel;
        this.clientName = clientName;
        this.client     = client;
        this.server     = server;

        try {
            channel.addChannelListener(this);
        } catch (JSDTException e) {
            System.out.println("PpongServer: send: exception: " + e +
                               ": " + new Date());
        }
        send(Message.writeServerMessage("You are connected to " + server));
    }


    public String
    getName() {
        return(clientName);
    }


    public synchronized void
    channelLeft(ChannelEvent event) {
        String name = event.getClientName();

        if (name.equals(clientName)) {
            connected = false;
        }
    }


    public void
    send(byte[] message) {
        try {
            if (connected) {
                channel.sendToClient(client, clientName, new Data(message));
            }
        } catch (NoSuchClientException nsce) {
        } catch (JSDTException e) {
            System.out.println("PpongServer: send: exception: " + e +
                               ": " + new Date());
        }
    }
}


class
GameHandler extends ChannelAdaptor implements Runnable {

    private final Player player1, player2;

    private boolean gameRunning = true;
    private long    gameNumber  = 0;
    private Thread  thread      = null;

    private static final Random random = new Random();


    public
    GameHandler(Channel channel, Player player1, Player player2) {
        this.player1 = player1;
        this.player2 = player2;
        gameNumber   = System.currentTimeMillis();

        try {
            channel.addChannelListener(this);
        } catch (JSDTException e) {
            System.out.println("GameHandler: constructor: exception: " + e +
                               ": " + new Date());
        }

        thread = new Thread(this);
        thread.setPriority(Thread.NORM_PRIORITY);
        thread.start();
    }


    public boolean
    isAlive() {
        return(thread.isAlive());
    }


    public synchronized void
    channelLeft(ChannelEvent event) {
        String clientName = event.getClientName();

        if (clientName.equals(player1.getName())) {
            System.out.println("Disconnect (Player1: " +
                               player1.getName() + ") : " + new Date());
            player2.send(Message.writeServerMessage("Game complete: " +
                                          "The other player forfeits."));
            player2.send(Message.writeMessage(Message.M_END));
        } else if (clientName.equals(player2.getName())) {
            System.out.println("Disconnect (Player2: " +
                               player2.getName()  + ") : " + new Date());
            player1.send(Message.writeServerMessage("Game complete: " +
                                          "The other player forfeits."));
            player1.send(Message.writeMessage(Message.M_END));
        }

        if (gameRunning) {
            System.out.println("End Game Number " + gameNumber + " between " +
                               player1.getName() + " and " +
                               player2.getName() + ": " + new Date());
            gameRunning = false;
            thread.interrupt();             // Terminate this thread
        }
    }


    public void
    run() {
        System.out.println("Game Number " + gameNumber + " between " +
                           player1.getName() + " and " +
                         player2.getName() + ": " + new Date());
        playGame();
    }


    private void
    playGame() {
        final   String announceStart = "PlayBall!  Game ends at 7.";
        boolean retard = ((random.nextInt() % 2) == 0);

        System.out.println("Retard:" + retard + ":GameNumber:" + gameNumber);
        try {
            Thread.sleep(2000);
            player1.send(Message.writeRetard(retard));
            player2.send(Message.writeRetard(retard));

            player1.send(Message.writeServerMessage("Your opponent is " +
                                                player2.getName() + "."));
            player2.send(Message.writeServerMessage("Your opponent is " +
                                                player1.getName() + "."));
            Thread.sleep(5000);
            for (int seconds = 5; seconds > 1; seconds-- ) {
                player1.send(Message.writeServerMessage(seconds +
                             " seconds to start.  You receive first ball."));
                player2.send(Message.writeServerMessage(seconds +
                             " seconds to start.  Your opponent receives" +
                             " first ball."));
                Thread.sleep(1000);
            }
            player1.send(Message.writeServerMessage("1 second to start." +
                                               "  You receive first ball."));
            player2.send(Message.writeServerMessage("1 second to start."+
                                    "  Your opponent receives first ball."));
            Thread.sleep(1000);
            player1.send(Message.writeServerMessage(announceStart));
            player2.send(Message.writeServerMessage(announceStart));
            player1.send(Message.writeStart(-1, 1, gameNumber));
            player2.send(Message.writeStart( 1, 1, gameNumber));
        } catch (InterruptedException e){
        } catch (Exception e) {
            System.out.println("GameHandler: Error starting game: " + e +
                               ": " + new Date());
        }
    }
}
