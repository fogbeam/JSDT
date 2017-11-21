
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

import java.io.*;

/**
 * @version     2.3 - 1st November 2017
 * @author      James "Bo" Begole
 * @author      Rich Burridge
 */

class
Message {

    public static final char M_ACK       = '\u00A0';
    public static final char M_END       = '\u00A1';
    public static final char M_MISS      = '\u00A2';
    public static final char M_PAUSE     = '\u00A3';
    public static final char M_OPPONENT  = '\u00A4';
    public static final char M_RACKET    = '\u00A5';
    public static final char M_RESUME    = '\u00A6';
    public static final char M_RETARD    = '\u00A7';
    public static final char M_RETURN    = '\u00A8';
    public static final char M_SCORE     = '\u00A9';
    public static final char M_SERVER    = '\u00AA';
    public static final char M_START     = '\u00AB';
    public static final char M_TAKEBALL  = '\u00AC';
    public static final char M_TESTDELAY = '\u00AD';
    public static final char M_WALL      = '\u00AE';


    public static byte[]
    writeMessage(char type) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream      dos  = new DataOutputStream(baos);

        try {
            dos.writeChar(type);
            dos.flush();
        } catch (IOException ioe) {
            System.err.println("Message: writeMessage: exception: " + ioe);
        }

        return(baos.toByteArray());
    }


    public static byte[]
    writeOpponent(String name) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream      dos  = new DataOutputStream(baos);

        try {
            dos.writeChar(M_OPPONENT);
            dos.writeUTF(name);
            dos.flush();
        } catch (IOException ioe) {
            System.err.println("Message: writeOpponent: exception: " + ioe);
        }

        return(baos.toByteArray());
    }


    public static byte[]
    writeRacket(int x, int y) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream      dos  = new DataOutputStream(baos);

        try {
            dos.writeChar(M_RACKET);
            dos.writeInt(x);
            dos.writeInt(y);
            dos.flush();
        } catch (IOException ioe) {
            System.err.println("Message: writeRacket: exception: " + ioe);
        }

        return(baos.toByteArray());
    }


    public static byte[]
    writeRetard(boolean retard) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream      dos  = new DataOutputStream(baos);

        try {
            dos.writeChar(M_RETARD);
            dos.writeBoolean(retard);
            dos.flush();
        } catch (IOException ioe) {
            System.err.println("Message: writeRetard: exception: " + ioe);
        }

        return(baos.toByteArray());
    }


    public static byte[]
    writeReturnTestDelay(long timeVal) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream      dos  = new DataOutputStream(baos);

        try {
            dos.writeChar(M_RETURN);
            dos.writeLong(timeVal);
            dos.flush();
        } catch (IOException ioe) {
            System.err.println("Message: writeReturnTestDelay: exception: " +
                               ioe);
        }

        return(baos.toByteArray());
    }


    public static byte[]
    writeScore(long gameNumber, String host1, int score1,
                                String host2, int score2) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream      dos  = new DataOutputStream(baos);

        try {
            dos.writeChar(M_SCORE);
            dos.writeLong(gameNumber);
            dos.writeUTF(host1);
            dos.writeInt(score1);
            dos.writeUTF(host2);
            dos.writeInt(score2);
            dos.flush();
        } catch (IOException ioe) {
            System.err.println("Message: writeScore: exception: " + ioe);
        }

        return(baos.toByteArray());
    }


    public static byte[]
    writeServerMessage(String message) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream      dos  = new DataOutputStream(baos);

        try {
            dos.writeChar(M_SERVER);
            dos.writeUTF(message);
            dos.flush();
        } catch (IOException ioe) {
            System.err.println("Message: writeServerMessage: exception: " +
                               ioe);
        }

        return(baos.toByteArray());
    }


    public static byte[]
    writeStart(int XSpeed, int YSpeed, long gameNumber) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream      dos  = new DataOutputStream(baos);

        try {
            dos.writeChar(M_START);
            dos.writeInt(XSpeed);
            dos.writeInt(YSpeed);
            dos.writeLong(gameNumber);
            dos.flush();
        } catch (IOException ioe) {
            System.err.println("Message: writeStart: exception: " + ioe);
        }

        return(baos.toByteArray());
    }


    public static byte[]
    writeTakeBall(int x, int y, int xSpeed, int ySpeed) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream      dos  = new DataOutputStream(baos);

        try {
            dos.writeChar(M_TAKEBALL);
            dos.writeInt(x);
            dos.writeInt(y);
            dos.writeInt(xSpeed);
            dos.writeInt(ySpeed);
            dos.flush();
        } catch (IOException ioe) {
            System.err.println("Message: writeTakeBall: exception: " + ioe);
        }

        return(baos.toByteArray());
    }


    public static byte[]
    writeTestDelay(long timeVal) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream      dos  = new DataOutputStream(baos);

        try {
            dos.writeChar(M_TESTDELAY);
            dos.writeLong(timeVal);
            dos.flush();
        } catch (IOException ioe) {
            System.err.println("Message: writeTestDelay: exception: " + ioe);
        }

        return(baos.toByteArray());
    }
}
