
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

package com.sun.media.jsdt.impl;

import java.io.IOException;

/**
 * JSDT Simple Message class.
 *
 * @version     2.3 - 28th October 2017
 * @author      Rich Burridge
 */

public final class
Message extends JSDTObject implements JSDTDebugFlags {

    /** The thread associated with this message. */
    public JSDTThread thread;

    /** The session number associated with this message. */
    public short sessionNo = 0;

    /** The id associated with this message. */
    public int id;

    /** The version of this message. */
    char version = '\u0000';

    /** The type of this message. */
    public char type = '\u0000';

    /** The action of this message. */
    public char action = '\u0000';


/**
 * <A NAME="SD_MESSAGE"></A>
 * <EM>Message</EM> is the constructor for the Message class.
 */

    public
    Message() {
        if (Message_Debug) {
            debug("Message: constructor.");
        }
    }


/**
 * <A NAME="SD_GETMESSAGEHEADER"></A>
 * <EM>getMessageHeader</EM> reads the header portion of the incoming message
 * off the socket, and the version number, session name, id, type and action
 * are extracted for later use. This method blocks if there is no message to
 * read on the socket.
 *
 * @param thread the thread the message is for.
 */

    public void
    getMessageHeader(JSDTThread thread) throws IOException {
        if (Message_Debug) {
            debug("Message: getMessageHeader:" +
                  " thread: " + thread);
        }

        this.thread = thread;
        try {
                        thread.dataIn.readChar();  /* Skip T_Version. */
            version   = thread.dataIn.readChar();
                        thread.dataIn.readChar();  /* Skip T_Session_No. */
            sessionNo = thread.dataIn.readShort();
            id        = thread.dataIn.readInt();
            type      = thread.dataIn.readChar();
            action    = thread.dataIn.readChar();
        } catch (IOException ioe) {
            throw ioe;
        } catch (Exception e) {
            error("Message: getMessageHeader: ", e);
        }
    }


/**
 * <A NAME="SD_SETMESSAGEHEADER"></A>
 * <EM>setMessageHeader</EM> set the variables in this message from the given
 * message.
 *
 * @param message the message to copy the variables from.
 */

    public void
    setMessageHeader(Message message) {
        if (Message_Debug) {
            debug("Message: setMessageHeader:" +
                  " message: " + message);
        }

        thread    = message.thread;
        version   = message.version;
        sessionNo = message.sessionNo;
        id        = message.id;
        type      = message.type;
        action    = message.action;
    }


/**
 * <A NAME="SD_TOSTRING"></A>
 * <EM>toString</EM> print a short description of this Message object.
 *
 * @return a String containing a description of this Message.
 */

    public String
    toString() {
        return("Message:" +
                " session #: " + sessionNo +
                " id: "        + id +
                " type: "      + typeToString(type) +
                " action: "    + actionToString(action) + "\n");
    }


/**
 * <A NAME="SD_VALIDMESSAGEHEADER"></A>
 * <EM>validMessageHeader</EM> checks to see if this message contains valid
 * version, sessionNo, type and action fields.
 *
 * Note that more stringent tests could be applied here.
 *
 * @return a boolean indicating whether the message is valid.
 */

    public boolean
    validMessageHeader() {
        if (Message_Debug) {
            debug("Message: validMessageHeader.");
        }

        return(version != '\u0000' && sessionNo != 0 &&
               type    != '\u0000' && action    != '\u0000');
    }
}
