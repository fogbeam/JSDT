
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

import com.sun.media.jsdt.*;
import com.sun.media.jsdt.impl.JSDTObject;
import java.util.Vector;

/**
 * JSDT proxy receive client class.
 *
 * @version     2.3 - 6th November 2017
 * @author      Rich Burridge
 */

final class
ReceiveClient extends JSDTObject
              implements Client, ChannelConsumer, socketDebugFlags {

    // The Client associated with the receive() operation.
    private Client client;

    // Data message queue (from server).
    private Vector<Data> messages = null;


/**
 * <A NAME="SD_RECEIVECLIENT"></A>
 * <EM>ReceiveClient</EM> the constructor for the ReceiveClient class.
 *
 * @param channel the channel to receive messages on.
 * @param client the client associated with this receive operation.
 */

    public
    ReceiveClient(Channel channel, Client client) {
        if (ReceiveClient_Debug) {
            debug("ReceiveClient: constructor:" +
                  " channel: " + channel +
                  " client: "  + client);
        }

        this.client  = client;
        messages     = new Vector<>();
    }


/**
 * <A NAME="SD_AUTHENTICATE"></A>
 * <EM>authenticate</EM> used to authenticate a client.
 *
 * @param info the authentication info for this validation.
 *
 * @return the response by the remote client to the managers challenge.
 */

    public Object
    authenticate(AuthenticationInfo info) {
        if (ReceiveClient_Debug) {
            debug("ReceiveClient: authenticate:" +
                  " info: " + info);
        }

        return(client.authenticate(info));
    }


/**
 * <A NAME="SD_GETNAME"></A>
 * <EM>getName</EM> get the name of this client.
 *
 * @return the name of the client.
 */

    public String
    getName() {
        if (ReceiveClient_Debug) {
            debug("ReceiveClient: getName.");
        }

        return(client.getName());
    }


/**
 * <A NAME="SD_DATARECEIVED"></A>
 * <EM>dataReceived</EM> is called when data is received for this client on
 * the given channel. This interface is used for normal data and for
 * uniformly sequenced data.
 *
 * The data object received is to a copy of the client data which this
 * consumer can do with as they require.
 *
 * @param data the data which can be of unlimited size.
 */


    public void
    dataReceived(Data data) {
        if (ReceiveClient_Debug) {
            debug("ReceiveClient: dataReceived:" +
                  " data: " + data);
        }

        try {
            putMessage(data);
        } catch (Exception e) {
            error("ReceiveClient: dataReceived: ", e);
        }
    }


/**
 * <A NAME="SD_DATAVAILABLE"></A>
 * <EM>dataAvailable</EM>
 *
 * @return
 */

    boolean
    dataAvailable() {
        if (ReceiveClient_Debug) {
            debug("ReceiveClient: dataAvailable.");
        }

        synchronized (messages) {
            return(messages.size() != 0);
        }
    }


/**
 * <A NAME="SD_GETMESSAGE"></A>
 * <EM>getMessage</EM>
 *
 * @param timeout the maximum time to wait in milliseconds.
 * @param ignoreTimeout ignore the timeout argument, and block until data
 * becomes available.
 *
 * @return
 */

    synchronized Data
    getMessage(long timeout, boolean ignoreTimeout) {
        Data data = null;

        if (ReceiveClient_Debug) {
            debug("ReceiveClient: getMessage:" +
                  " timeout: "        + timeout +
                  " ignore timeout? " + ignoreTimeout);
        }

        notifyAll();
        if (ignoreTimeout) {
            while (messages.size() == 0) {
                try {
                    wait();
                } catch (InterruptedException ie) {
                }
            }
        } else {
            if (timeout != 0) {
                try {
                    wait(timeout);
                } catch (InterruptedException ie) {
                }
            }
        }

        if (messages.size() != 0) {
            data = messages.firstElement();
            messages.removeElement(data);
        }
        return(data);
    }


/**
 * <A NAME="SD_PUTMESSAGE"></A>
 * <EM>putMessage</EM>
 *
 * @param data
 */

    private synchronized void
    putMessage(Data data) {
        if (ReceiveClient_Debug) {
            debug("ReceiveClient: putMessage:" +
                  " data: " + data);
        }

        messages.addElement(data);
        notifyAll();
    }
}
