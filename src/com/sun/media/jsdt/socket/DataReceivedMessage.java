
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

package com.sun.media.jsdt.socket;

import com.sun.media.jsdt.Data;
import com.sun.media.jsdt.impl.JSDTObject;

/**
 * JSDT Channel client-side data received message class.
 *
 * @version     2.3 - 27th October 2017
 * @author      Rich Burridge
 */

public final class
DataReceivedMessage extends JSDTObject implements socketDebugFlags {

    /** The channel proxy associated with this message. */
    private ChannelProxy cp;

    /** The name of the receiver of the message. */
    private String receiverName = null;

    /** Indicates if the channel this message was received on, is ordered. */
    private boolean isOrdered = false;

    /** The messages bytes (converted to a Data object). */
    private Data data = null;


/**
 * <A NAME="SD_SETMESSAGEINFO"></A>
 * <EM>setMessageInfo</EM> sets the fields that are unique to this new
 * message. This object is a data message received from the channel server.
 * It contains everything needed to send it on, to all the channel consumers.
 *
 * @param cp the channel proxy associated with this message.
 * @param receiverName the name of the receiver of the message.
 * @param isOrdered indicates if the channel this message was received on,
 * is ordered.
 * @param data the messages bytes (converted to a Data object).
 */

    void
    setMessageInfo(ChannelProxy cp, String receiverName,
		   boolean isOrdered, Data data) {
        if (DataReceivedMessage_Debug) {
            debug("DataReceivedMessage: setMessageInfo:" +
		  " channel proxy: "   + cp +
		  " receiver name: "   + receiverName +
                  " channel ordered: " + isOrdered +
                  " data: "            + data);
        }

        this.cp           = cp;
	this.receiverName = receiverName;
        this.isOrdered    = isOrdered;
        this.data         = data;
    }


/**
 * <A NAME="SD_GETCHANNELPROXY"></A>
 * <EM>getChannelProxy</EM> get the channel proxy associated with this data
 * message.
 *
 * @return the channel proxy associated with this data message.
 */

    public ChannelProxy
    getChannelProxy() {
        if (DataReceivedMessage_Debug) {
            debug("DataReceivedMessage: getChannelProxy.");
        }

        return(cp);
    }


/**
 * <A NAME="SD_GETDATA"></A>
 * <EM>getData</EM> get the data object associated with this data message.
 *
 * @return the data object associated with this data message.
 */

    public Data
    getData() {
        if (DataReceivedMessage_Debug) {
            debug("DataReceivedMessage: getData.");
        }

        return(data);
    }


/**
 * <A NAME="SD_ISORDERED"></A>
 * <EM>isOrdered</EM> indicates if the channel this message was received on,
 * is ordered.
 *
 * @return true if channel is ordered; false if not.
 */

    public boolean
    isOrdered() {
        if (DataReceivedMessage_Debug) {
            debug("DataReceivedMessage: isOrdered.");
        }

        return(isOrdered);
    }


/**
 * <A NAME="SD_GETRECEIVERNAME"></A>
 * <EM>getReceiverName</EM> get the name of the receiver of this data message.
 *
 * @return the name of the receiver of this data message.
 */

    public String
    getReceiverName() {
        if (DataReceivedMessage_Debug) {
            debug("DataReceivedMessage: getReceiverName.");
        }

        return(receiverName);
    }
}
