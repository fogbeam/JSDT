
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
 * @version     2.3 - 20th November 2017
 * @author      James "Bo" Begole
 * @author      Rich Burridge
 */

class
GeneralClient implements Runnable {

    private final ConnectionHandler owner;
    private String  host;
    private int     port;
    private String  type;
    private Session session      = null;
    private Channel channel      = null;
    private String  clientName   = null;
    private Client  gameClient   = null;

    private Thread  life         = null;
    private boolean runState     = false;
    private boolean connectState = false;


    public
    GeneralClient(ConnectionHandler theOwner) {
        owner = theOwner;
    }


    public void
    start(String host, int port, String type) {
        owner.debugLog("GeneralClient:start", this);

        runState  = true;
        this.host = host;
        this.port = port;
        this.type = type;

        if (life == null) {
            life = new Thread(this);
        }
        life.start();
    }


    public void
    stop() {
        owner.debugLog("GeneralClient:stop", this);

        if (life != null) {
            life.stop();
        }
        shutdown();
    }


    public boolean
    isRunning() {
        owner.debugLog("GeneralClient:isRunning", this);

        return(runState);
    }


    public boolean
    isConnected() {
        owner.debugLog("GeneralClient:isConnected", this);

        return(connectState);
    }


    public void
    run() {
        owner.debugLog("GeneralClient:run", this);

        owner.changeStatus("Connecting to '" + host + "' on port '" +
                           port + "'...", this);

        try {
            URLString url = URLString.createSessionURL(host, port,
                                                       type, "PpongSession");

            try {
                InetAddress localhost = InetAddress.getLocalHost();
                int n = rrange(20, 100) * rrange(1, 10000);
                String myHost = localhost.getHostName();

                clientName = "GameClient@" + myHost + n;
            } catch (UnknownHostException uhe) {
                System.out.println("GeneralClient: run: exception: " + uhe);
            }

            gameClient = new PpongClient(clientName);
            session    = SessionFactory.createSession(gameClient, url, true);
            channel    = session.createChannel(gameClient, "GameChannel",
                                               true, true, true);
            channel.addConsumer(gameClient, (ChannelConsumer) owner);
        } catch (JSDTException e) {
            owner.connectionMishap("GeneralClient: run: Could not connect to '" +
                                   host + "'.", this);
            e.printStackTrace();
            shutdown();
            return;
        }

        owner.connectionUp("Connected.", this);

        connectState = true;
        owner.handleSession(channel, gameClient, this);
        connectState = false;
        owner.connectionDown("Disconnected.", this);
    }


/* Generate a random number between low and high. */

    private int
    rrange(int low, int high) {
        return((int)(Math.random() * (high - low + 1) + low));
    }


    public void
    shutdown() {
        owner.debugLog("GeneralClient:shutdown", this);

        life     = null;
        runState = false;
        if (connectState) {
            try {
                channel.leave(gameClient);
                session.leave(gameClient);
            } catch (JSDTException e) {
                owner.connectionMishap("Could not shutdown '" + "'.", this);
            }

            try {
                session.close(true);
            } catch (JSDTException e) {
                owner.connectionMishap("Could not shutdown '" + "'.", this);
            }

            connectState = false;
        }
    }
}
