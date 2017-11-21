
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
import java.awt.*;
import java.awt.event.*;
import java.io.*;

/**
 * Description: PpongCourt does the real work for the game.
 *
 * @version     2.3 - 20th October 2017
 * @author      James "Bo" Begole
 * @author      Rich Burridge
 */

public class
PpongCourt extends Canvas implements ChannelConsumer, ConnectionHandler,
                             Runnable, PpongDebugFlags, MouseMotionListener {

    public Channel channel = null;
    public Client  client  = null;

// Should be based on network delay.

    final private long     updateDelay = 500;

    private long nextUpdateTime = System.currentTimeMillis() + updateDelay;

    private final int       minDelayTests  = 5;
    private final LongStats roundTripDelay = new LongStats(minDelayTests);

    // Indication of whether this thread should be suspend or not.
    private boolean suspend = false;


// Begin ConnectionHandler method implementations:

    public void
    connectionUp(String message, Object who) {
        if (PpongCourt_Debug) {
            System.err.println(message);
        }
    }


    public void
    connectionDown(String message, Object who) {
        if (PpongCourt_Debug) {
            System.err.println(message);
        }
    }


    public void
    connectionMishap(String message, Object who) {
        if (PpongCourt_Debug) {
            System.err.println(message);
        }
    }


    public void
    handleSession(Channel channel, Client client, Object who) {
        boolean delayChecked = false;

        this.channel = channel;
        this.client  = client;

        while (true) {
            if (ballInPlay) {
                moveBall();

                if (!delayChecked) {
                    for (int i = 0; i< minDelayTests; i++) {
                        send(Message.writeTestDelay(
                                        System.currentTimeMillis()));
                    }
                    delayChecked = true;
                }

// Send Racket Position after updateDelay or if Racket has moved
// "significantly" better if send update only when racket has moved
// significantly and when ball is "close" to my racket for precision.

               if ((System.currentTimeMillis() >= nextUpdateTime) ||
                   (Math.abs(oldMyRacket.y-myRacket.y) > myRacket.height / 4)) {
                   nextUpdateTime = System.currentTimeMillis() + updateDelay;

// Convert racket position and send to opponent

                   send(Message.writeRacket(outerCourt.width - myRacket.x,
                                            myRacket.y));
                   oldMyRacket = new Racket(myRacket);
                   send(Message.writeTestDelay(System.currentTimeMillis()));
               }
            }

            repaint();
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                break;
            }
        }
    }


    public void
    changeStatus(String message, Object who) {
        if (PpongCourt_Debug) {
            System.err.println(message);
        }
    }


    public void
    debugLog(String message, Object who) {
        if (PpongCourt_Debug) {
            System.err.println(message);
        }
    }

// End of ConnectionHandler method implementations.


// Begin Runnable method implementations

    public void
    run() {
        while (true) {
            repaint();

            synchronized(this) {
                while (suspend) {
                    try {
                        wait();
                    } catch (InterruptedException ie) {
                    }
                }
            }

            try {
                kicker.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

// End of Runnable method implementations

// Begin ChannelConsumer method implementations


    public void
    dataReceived(Data data) {
        ByteArrayInputStream bais =
                        new ByteArrayInputStream(data.getDataAsBytes());
        DataInputStream      dis  = new DataInputStream(bais);

        try {
            char type = dis.readChar();

            switch (type) {
                case Message.M_RACKET:
                    opponentRacket.x = dis.readInt();
                    opponentRacket.y = dis.readInt();
                    break;

                case Message.M_TAKEBALL:

// Send stats.

                    parent.game.send(Message.writeRetard(ball.getRetard()));

// I now control the ball.

                    synchronized (ball) {
                        int tempX      = outerCourt.width - dis.readInt();
                        int tempY      = dis.readInt();
                        int tempXSpeed = -(dis.readInt());
                        int tempYSpeed = dis.readInt();

                        ball.reset(tempX, tempY, tempXSpeed, tempYSpeed,
                                   System.currentTimeMillis());
                    }

                    parent.playSound(Ppong.S_HITRACKET);

                    if (PpongCourt_Debug) {
                        System.err.println("I think the ball is:" +
                          ball.x + ":" + ball.y +
                          ":" + ball.XSpeed + ":" + ball.YSpeed + ":");
                        System.err.println("Court Width: " + outerCourt.width);
                    }
                    break;

                case Message.M_OPPONENT:
                    opponentHost = dis.readUTF();

// Acknowledge to Server that I still want to play.

                    send(Message.writeMessage(Message.M_ACK));

// Get the player's attention, since there is now an opponent
// Want to ring the standard bell here, too, but System.out.print('\u0007');
// and variations won't handle it.

                    parent.playSound(Ppong.S_ATTENTION);
                    break;

                case Message.M_MISS:

// Send stats.

                    parent.game.send(Message.writeRetard(ball.getRetard()));

                    if (PpongCourt_Debug) {
                        System.err.println("Miss: before Ball=" + ball);
                    }
                    myScore++;

                    ball.reset(ball.x, ball.y,
                               ball.originalXSpeed, ball.originalYSpeed);
                    opponentMissed = true;
                    parent.playSound(Ppong.S_OMISSED);

                    if (myScore >= gameOverScore) {
                        gameOverString = "Game Over. You Win!";
                        send(Message.writeMessage(Message.M_END));
                        parent.game.end();
                        parent.playSound(Ppong.S_ATTENTION);
                    }
                    if (PpongCourt_Debug) {
                        System.err.println("Miss: after Ball=" + ball);
                    }
                    break;

                case Message.M_SERVER:
                    setMessage(dis.readUTF());
                    break;

                case Message.M_WALL:
                    parent.playSound(Ppong.S_HITWALL);
                    break;

                case Message.M_START:
                    if (PpongCourt_Debug) {
                        System.err.println("ball = " + ball);
                    }

                    synchronized (ball) {
                        long thisTime = System.currentTimeMillis();

                        ball.XSpeed       *= dis.readInt();
                        ball.YSpeed       *= dis.readInt();
                        parent.game.number = dis.readLong();
                        ball.xStartTime    = thisTime;
                        ball.yStartTime    = thisTime;
                    }

                    parent.game.gameInProgress = true;
                    parent.pauseButton.setEnabled(true);
                    if (!parent.isApplication) {
                        parent.musicButton.setEnabled(true);
                        parent.soundsButton.setEnabled(true);
                    }
                    parent.game.resume();

                    if (PpongCourt_Debug) {
                        System.err.println("ball = " + ball);
                    }
                    break;

                case Message.M_RETARD:
                    ball.setRetard(dis.readBoolean());
                    break;

                case Message.M_PAUSE:
                    parent.game.pause();
                    parent.pauseButton.toggle();
                    break;

                case Message.M_RESUME:
                    parent.game.resume();
                    parent.pauseButton.toggle();
                    break;

                case Message.M_END:
                    parent.game.end();
                    break;

                case Message.M_TESTDELAY:

// Send immediately back.

                    send(Message.writeReturnTestDelay(dis.readLong()));
                    break;

                case Message.M_RETURN:
                    long newDatum = System.currentTimeMillis() - dis.readLong();

// Add this datum to stats.

                    roundTripDelay.add(newDatum);
                    if (PpongCourt_Debug) {
                        System.err.println("Current mean RTD: " +
                                           roundTripDelay.getMean());
                    }
                    break;

                case Message.M_ACK:
                    break;                      // Ignore it.

                default:
                    System.out.println("PpongCourt: dataReceived: " +
                                       " unknown message type: " + type);
            }
        } catch (IOException ioe) {
            System.err.println("PpongCourt: dataReceived: exception: " + ioe);
        }
    }

// End of ChannelConsumer method implementations

// Begin PpongCourt specific methods
// (i.e, paint, move-ball, move-racket, etc.)

    private final int gameOverScore = 7;

    private boolean   ballInPlay          = false;
    private boolean   missed              = false;
    private boolean   opponentMissed      = false;
    private Dimension outerCourt;
    private Rectangle innerCourt          = null;
    private String    opponentScoreString = null;

    public  int       myScore             = 0;
    public  int       opponentScore       = 0;

    public  String    opponentHost        = null;
    private String    gameOverString      = null;

    public  Ppong     parent              = null;
    private Thread    kicker              = null;
    private Racket    myRacket            = null;
    private Racket    oldMyRacket         = null;
    private Racket    opponentRacket      = null;
    public  Ball      ball                = null;


// Initializations.

    public
    PpongCourt(Ppong parent) {
        this.parent = parent;
        addMouseMotionListener(this);
        repaint();
    }


    public void
    init() {

// Final (constant) values.
// Edit these to change properties of the court.

        final int courtBorder  = 5;      // in pixels

        // Pixel distance of racket from it's back wall.
        final int racketOffset = 30;

        final int racketWidth = 5;       // in pixels
        final int racketHeight = 30;     // in pixels

// Size the court to use the max available
// Note: There is a potential problem if two players use a different size
// applet. If they load the applet off the same HTML page, this shouldn't
// happen

        setBounds(0, parent.p.getSize().height, parent.width,
                  parent.height - parent.p.getSize().height);

        outerCourt = getSize();
        innerCourt = new Rectangle(courtBorder, courtBorder,
                                   outerCourt.width - 2 * courtBorder - 1,
                                   outerCourt.height - 2 * courtBorder - 1);

        ballInPlay = false;
        myScore = 0;
        opponentScore = 0;

        ball = new Ball(innerCourt.x +
                        innerCourt.width / 2 - ball.diameter / 2,
                        innerCourt.y +
                        innerCourt.height / 2 - ball.diameter / 2,
                        ball.defaultSpeed, ball.defaultSpeed);
        myRacket = new Racket(racketOffset, innerCourt.height/2,
                              racketWidth, racketHeight);
        oldMyRacket = new Racket(myRacket.x, myRacket.y,
                                 myRacket.width, myRacket.height);
        opponentRacket = new Racket(innerCourt.x +
                                    innerCourt.width - racketOffset,
                                    innerCourt.height / 2,
                                    racketWidth, racketHeight);
        opponentScoreString = "Opponent Score: " + opponentScore;
        opponentHost = "Opponent Location: No Opponent Yet";
        gameOverString = null;
        repaint();
    }


    public void
    send(byte[] message) {
        try {
            channel.sendToClient(client, opponentHost, new Data(message));
        } catch (JSDTException e) {
            System.out.println("PpongCourt: send: exception: " + e);
        }
    }


/** The player toward whom the ball is moving will control the movement of
 *  the ball.  That way, there won't be any problem of missing a ball due
 *  to the balls position being updated late.
 */

    private void
    moveBall() {
        long thisTime = System.currentTimeMillis();

// Move in the x direction.

        long diffTime    = thisTime - ball.xStartTime;

// Seconds since start of movement.

        float numSeconds = (float) (diffTime / 1000.0);

        ball.x = ball.startX + Math.round(ball.XSpeed * numSeconds);

// Move in the y direction.

        diffTime = thisTime - ball.yStartTime;

        numSeconds = (float) (diffTime/1000.0);
        ball.y = ball.startY + Math.round(ball.YSpeed * numSeconds);

        if (ball.x <= innerCourt.x) {    // if ball has gone past me,
            if (PpongCourt_Debug) {
                System.err.println("Ball past court boundary: ball = " + ball +
                                   " innerCourt = " + innerCourt);
            }
            if (!missed) {

// If ball got this far, and isn't flagged missed, see if I hit it.

                if (checkMissed()) {

// Reset the ball, moving toward me.

                    ball.reset(innerCourt.x +
                               innerCourt.width / 2 - ball.diameter / 2,
                               innerCourt.y +
                               innerCourt.height / 2 - ball.diameter / 2,
                               (-ball.defaultSpeed),
                               ball.defaultSpeed, thisTime);
                }
                missed = false;
            } else {

// Otherwise, ball was checked earlier and flagged missed.

                ball.reset(innerCourt.x +
                           innerCourt.width / 2 - ball.diameter / 2,
                           innerCourt.y +
                           innerCourt.height / 2 - ball.diameter / 2,
                           (-ball.defaultSpeed),
                           ball.defaultSpeed, thisTime);
                missed = false;
            }
        } else if (!missed) {    // otherwise, if it wasn't already missed,
            if (ball.x < myRacket.x) {
                checkMissed();
            }

            if (opponentMissed) {

// Opponent has missed
// If the ball has gone past opponent end of court, reset the ball

                if (ball.x >= (innerCourt.x + innerCourt.width)) {
                    ball.reset(innerCourt.x +
                               innerCourt.width / 2 - ball.diameter / 2,
                               innerCourt.y +
                               innerCourt.height / 2 - ball.diameter / 2,
                               ball.defaultSpeed,
                               ball.defaultSpeed, thisTime);
                    ball.adjustSpeed(opponentRacket.x - ball.x,
                                     roundTripDelay.getMean());

// Slow ball here based on distance from center of court to opponent.

                    opponentMissed = false;
                }
            }
        }

// Check to see if the ball has bounced off a side wall
// if so, reverse direction.

        int courtTotal = innerCourt.y + innerCourt.height;
        int ballTotal  = ball.y + ball.diameter;

        if (ball.y < innerCourt.y) {

// Hit the top wall.

            ball.y = Math.abs(ball.y);
            ball.startY = ball.y;
            ball.YSpeed = -ball.YSpeed;
            ball.yStartTime = thisTime;
        } else if (ballTotal > courtTotal) {

// Hit the bottom wall.

            ball.y -= 2 * (ballTotal - courtTotal);
            ball.startY = ball.y;
            ball.YSpeed = -ball.YSpeed;
            ball.yStartTime = thisTime;
        }
    }


    private boolean
    checkMissed() {

// See if the ball hit my racket.

        if (ball.hit(myRacket)) {

// The ball hit my Racket, turn it around and give control to other player.

            ball.reverse(myRacket);
            send(Message.writeTakeBall(ball.x, ball.y,
                                       ball.XSpeed, ball.YSpeed));

// Slow ball here.

            ball.adjustSpeed(opponentRacket.x - myRacket.x,
                             roundTripDelay.getMean());

            parent.playSound(Ppong.S_HITRACKET);
            return(false);
        }

// The ball slipped past my Racket.

        missed = true;
        opponentScore++;
        opponentScoreString = "Opponent Score: " + opponentScore;
        send(Message.writeMessage(Message.M_MISS));
        parent.playSound(Ppong.S_IMISSED);

        if (opponentScore >= gameOverScore) {

// Game Over, other person wins.

            gameOverString = "Game Over. Opponent Wins.";
            if (PpongCourt_Debug) {
                System.err.println("Calling parent.game.end().");
            }
            parent.game.end();
            parent.playSound(Ppong.S_ATTENTION);
        }
        return(true);
    }


    private Image     offscreen;
    private Dimension offscreensize;
    private Graphics  offgraphics;

/** Update is called to refresh the screen. The default update clears the
 *  whole screen and calls paint. So, to eliminate flashing, reimplement
 *  update to not clear the screen.
 *  Paint still gets called directly in some cases, so it needs to be
 *  reimplemented, too.
 */

    public synchronized void
    update(Graphics g) {
        // Pixels from top of screen to write text.
        final int baseCharY = 20;

        Dimension d = getSize();

        if ((offscreen == null) ||
            (d.width != offscreensize.width) ||
            (d.height != offscreensize.height)) {
            offscreen = createImage(d.width, d.height);
            offscreensize = d;
            offgraphics = offscreen.getGraphics();
            offgraphics.setFont(getFont());
        }

// Clear the court area.

        offgraphics.setColor(Color.black);
        offgraphics.fillRect(0, 0, outerCourt.width, outerCourt.height);

// Draw the inner court.

        offgraphics.setColor(Color.green);
        offgraphics.drawRect(innerCourt.x, innerCourt.y,
                             innerCourt.width, innerCourt.height);
        offgraphics.drawLine(innerCourt.x + innerCourt.width / 2,
                             innerCourt.y, innerCourt.x + innerCourt.width / 2,
                             innerCourt.y + innerCourt.height);

// Draw the ball.

        ball.paint(offgraphics);

// Draw both rackets.

        myRacket.paint(offgraphics);
        opponentRacket.paint(offgraphics);

// Output Game info.

        offgraphics.setColor(Color.white);
        offgraphics.drawString("Your Score: " + myScore,
                               innerCourt.x+10, innerCourt.y+baseCharY);
        offgraphics.drawString(opponentHost,
                innerCourt.x + innerCourt.width-10 -
                offgraphics.getFontMetrics().stringWidth(opponentHost),
                innerCourt.y + baseCharY +
                offgraphics.getFontMetrics().getHeight());
        offgraphics.drawString(opponentScoreString,
                innerCourt.x + innerCourt.width - 10 -
                offgraphics.getFontMetrics().stringWidth(opponentScoreString),
                innerCourt.y + baseCharY);

        if (gameOverString != null) {
            offgraphics.setColor(Color.white);
            Font tempFont = offgraphics.getFont();
            offgraphics.setFont(new Font(tempFont.getName(), Font.BOLD,
                                tempFont.getSize()+8));
            offgraphics.drawString(gameOverString,
                 (outerCourt.width -
                 offgraphics.getFontMetrics().stringWidth(gameOverString)) / 2,
                 (outerCourt.height -
                 offgraphics.getFontMetrics().getHeight()) / 2);

            offgraphics.setFont(tempFont);
        }

// Put the offscreen image on the screen.

        g.drawImage(offscreen, 0, 0, null);
    }


// Handle mouse movement (i.e., move the racket).

    public void
    mouseMoved(MouseEvent e) {
        myRacket.updatePosition(e.getY(), innerCourt.y,
                                innerCourt.y + innerCourt.height);
    }


    public void
    mouseDragged(MouseEvent e) {
    }


    private void
    suspendThread(boolean suspend) {
        this.suspend = suspend;
        if (!suspend) {
            synchronized (this) {
                notifyAll();
            }
        }
    }


    public void
    start() {
        if (kicker == null) {
            kicker = new Thread(this);
            kicker.start();
        } else {
            suspendThread(false);
        }
    }


    public void
    stop() {
        suspendThread(true);
    }


    public void
    putBallInPlay() {
        ballInPlay     = true;
        nextUpdateTime = System.currentTimeMillis() + 1000;
        parent.playSound(Ppong.S_MUSIC);
    }


    public void
    stopBallInPlay() {
        ballInPlay = false;
        parent.stopSound(Ppong.S_MUSIC);
    }


    public void
    setMessage(String message) {
        parent.messageText.setText(message);
    }


    public Dimension
    getMinimumSize() {
        return(new Dimension(50, 50));
    }


    public Dimension
    getPreferredSize() {
        return(getMinimumSize());
    }


/** Paint is the routine that the default update method calls after clearing
 *  the screen to refresh the screen.  Update is overridden above.
 *  Paint is still called directly when the applet is exposed, so we'll just
 *  have it call update.
 */

    public void
    paint(Graphics g) {
        update(g);
    }

// End of PpongCourt specific methods.

}
