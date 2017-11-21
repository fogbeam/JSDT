
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
import java.net.*;

/**
 * Description: Game class keeps information about current game
 *              provides game control methods.
 *
 * @version     2.3 - 31st October 2017
 * @author      James "Bo" Begole
 * @author      Rich Burridge
 */

class Game implements PpongDebugFlags {

    private Ppong         owner          = null;
    private PpongCourt    court          = null;
    private GeneralClient client         = null;
    private boolean       inProgress     = false;
    public  boolean       gameInProgress = false;
    public  long          number         = 0;

    private Session session     = null;
    private Channel channel     = null;
    private Client  scoreClient = null;


    public
    Game(Ppong ppong, PpongCourt court,
         String serverHost, int port, String type) {
        owner           = ppong;
        this.court      = court;

        client = new GeneralClient(court);
        client.start(serverHost, port, type);

        court.start();
        court.setMessage("Connecting to Server at: " + serverHost);

// Set up the statistics output.

        try {
            String clientName = null;
            URLString url = URLString.createSessionURL(serverHost, port,
                                                       type, "PpongSession");

            try {
                InetAddress localhost = InetAddress.getLocalHost();
                int n = rrange(20, 100) * rrange(1, 10000);
                String myHost = localhost.getHostName();

                clientName = "ScoreClient@" + myHost + n;
            } catch (UnknownHostException uhe) {
                owner.showStatus("Can not determine local host");
            }

            scoreClient = new PpongClient(clientName);
            session = SessionFactory.createSession(scoreClient, url, true);
            channel = session.createChannel(scoreClient, "ScoreChannel",
                                            true, true, true);
        } catch (JSDTException e) {
            System.out.println("Game: constructor: Could not connect to '" +
                               owner.remoteHost + "'.");
            e.printStackTrace();
        }
    }


/* Generate a random number between low and high. */

    private int
    rrange(int low, int high) {
        return((int)(Math.random() * (high - low + 1) + low));
    }


    public void
    start() {
        if (!inProgress) {
            court.init();
            owner.startButton.toggle();
            owner.playSound(Ppong.S_MUSIC);
            inProgress = true;
        }
    }


    public void
    stop() {
        pause();
        court.stop();
    }


    public void
    end() {
        if (Game_Debug) {
            System.out.println("Ending Game 1: GameInProgress =" +
                               gameInProgress);
            System.out.println("inProgress = " + inProgress);
        }

        if (inProgress) {
            if (Game_Debug) {
                System.out.println("Ending Game");
            }

            client.stop();
            owner.startButton.toggle();
            owner.stopSound(Ppong.S_MUSIC);
            inProgress = false;

            try {
                if (gameInProgress) {    // Report Score to Server.
                    gameInProgress = false;
                    send(Message.writeScore(number, court.client.getName(),
                                             court.myScore, court.opponentHost,
                                            court.opponentScore));
                }
                channel.leave(scoreClient);
                session.leave(scoreClient);
            } catch (JSDTException e) {
                System.out.println("Game: end: exception: " + e);
            }

            try {
                session.close(true);
            } catch (JSDTException e) {
                System.out.println("Game: end: exception: " + e);
            }

            if (owner.pauseButton.getLabel().equals("Resume")) {
                owner.pauseButton.toggle();
            }

            owner.pauseButton.setEnabled(false);
            owner.soundsButton.setEnabled(false);
        }
    }


    public void
    send(byte[] message) {
        try {
            channel.sendToClient(scoreClient, "PpongServer", new Data(message));
        } catch (JSDTException e) {
            System.out.println("Game: send: exception: " + e);
        }
    }


    public void
    pause() {
        if (gameInProgress) {
            court.stopBallInPlay();
        }
    }


    public void
    resume() {
        if (gameInProgress) {
            court.putBallInPlay();
        }
    }
}
