
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

import java.awt.*;

/**
 * Ball extends Point.
 * Also maintains speed information.
 *
 * @version     2.3 - 29th October 2017
 * @author      James "Bo" Begole
 * @author      Rich Burridge
 */

class
Ball extends Point implements PpongDebugFlags {

    public static final int diameter     = 15;  // in pixels
    public static final int defaultSpeed = 90;  // in pixels/second

    public int XSpeed;
    public int YSpeed;
    public int originalXSpeed;
    public int originalYSpeed;

    public long xStartTime;
    public long yStartTime;

    public int startX;
    public int startY;


    public
    Ball(int x, int y, int xspeed, int yspeed) {
        super(x, y);
        reset(x, y, xspeed, yspeed, System.currentTimeMillis());
    }


    public
    Ball(int x, int y) {
        this(x, y, -defaultSpeed, defaultSpeed);
    }


/** true iff the ball speed should be retarded depending on the
  * network delay time.
  */

    private boolean retard = false;

/**
 * If true, retard the ball speed depending on the network lag.
 * @param value true iff ball speed should be retarded depending on the
 * network delay time
 */

    public void
    setRetard(boolean value) {
        retard = value;
    }


    public boolean
    getRetard() {
        return(retard);
    }


    public void
    reset(int x, int y, int xspeed, int yspeed) {
        this.x = x;
        this.y = y;
        startX = x;
        startY = y;

        this.XSpeed    = xspeed;
        this.YSpeed    = yspeed;
        originalXSpeed = xspeed;
        originalYSpeed = yspeed;
    }


    public void
    reset(int x, int y, int xspeed, int yspeed, long startTime) {
        reset(x, y, xspeed, yspeed);

        xStartTime = startTime;
        yStartTime = startTime;
    }


    public boolean
    hit(Racket racket) {
        int top    = racket.y;
        int bottom = racket.y + racket.height;

        int   xDiff = racket.x - x;
        float slope = YSpeed / XSpeed;
        int   tempY = y + Math.round(xDiff * slope);

        if ((tempY + diameter > top) && (tempY < bottom) ) {

// Put the ball back to the racket x position

            reset(racket.x, tempY, XSpeed, YSpeed, System.currentTimeMillis());
            return(true);
        } else {
            return(false);
        }
    }


    public void
    reverse(Racket racket) {
        int top    = racket.y;
        int bottom = racket.y + racket.height;

        XSpeed = -XSpeed;
        int ballCenter = y + diameter / 2;
        int paddleCenter = top + (bottom - top) / 2;

        int fromCenter = paddleCenter - ballCenter;

// positive, if ball in top half of paddle
// negative, if ball in bottom half of paddle

        if (YSpeed == 0) {
            YSpeed = 1;
        }

        float percentFromCenter = (float) fromCenter /
                                  (float) ((bottom - top) / 2);

        if (Ball_Debug) {
            System.err.println("reverse() YSpeed = " + YSpeed +
                               " percentFromCenter = " + percentFromCenter +
                               " fromCenter = " + fromCenter +
                                     " halfpaddle = " + (bottom - top) / 2);
        }

/* Assert
 * Paddle: ||
 *         ||__ if ballCenter is here or above, then percentFromCenter > 0 (+ve)
 *         ||   if ballCenter is here or below, pFC < 0 (-ve)
 *         ||
 */

// If ball is heading down, want top of paddle to be negative and bottom
// to be positive.

        if (YSpeed > 0) {
            if (percentFromCenter > 0) {
                percentFromCenter = -percentFromCenter;
            }
        }

        if (percentFromCenter > 0) {      // if percentFromCenter is positive
            YSpeed = (int) (YSpeed * (1 + percentFromCenter));
        } else {
            YSpeed = (int) (YSpeed * (-1 + percentFromCenter));
        }

        if (Ball_Debug) {
            System.err.println("reverse() YSpeed=" + YSpeed);
        }

        reset(x, y, XSpeed, YSpeed, System.currentTimeMillis());
    }


    public void
    adjustSpeed(int distance, long netDelay) {

        if (Ball_Debug) {
            System.err.println("adjust()1: distance=" + distance +
                               " netDelay=" + netDelay +
                                     " XSpeed=" + XSpeed +
                               " originalXSpeed=" + originalXSpeed +
                               " YSpeed=" + YSpeed +
                               " originalYSpeed=" + originalYSpeed);
        }

        XSpeed = originalXSpeed;
        YSpeed = originalYSpeed;

// travelTime, netDelayTime, and newTravelTime are in seconds

        float travelTime    = ((float) distance) / (float) XSpeed;
        float netDelayTime  = (float) (netDelay / 1000.0) / 2;
        float newTravelTime = travelTime + netDelayTime;

        if (Ball_Debug) {
            System.err.println("--- retard=" + retard +
                                     " adjustSpeed()1.5 travelTime=" + travelTime +
                                     " netDelayTime=" + netDelayTime +
                                     " newTravelTime =" + newTravelTime);
        }

        long current = System.currentTimeMillis();
        long expectedMessageTime = current +
                 Math.round(retard ? (newTravelTime*1000) : (travelTime));

        if (Ball_Debug) {
            System.err.println("Current time: " + current +
                              " Expected reception time: " + expectedMessageTime);
        }

        if (retard) {
            XSpeed = Math.round(distance / newTravelTime);
            YSpeed = Math.round(YSpeed * ((float) XSpeed /
                                          (float) originalXSpeed));
        }

        if (Ball_Debug) {
            System.err.println("adjust()2: distance=" + distance +
                               " netDelay=" + netDelay +
                                     " XSpeed=" + XSpeed +
                               " originalXSpeed=" + originalXSpeed +
                                     " YSpeed=" + YSpeed +
                               " originalYSpeed=" + originalYSpeed + "\n");
        }
    }


    public void
    paint(Graphics g) {
        g.setColor(Color.yellow);
        g.fillOval(x, y, diameter, diameter);
    }


    public String
    toString() {
        return(super.toString() + " XSpeed=" + XSpeed + " YSpeed=" + YSpeed);
    }
}
