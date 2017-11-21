
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

package com.sun.media.jsdt.event;

import com.sun.media.jsdt.Channel;
import com.sun.media.jsdt.Session;
import com.sun.media.jsdt.impl.*;
import java.util.EventObject;

/**
 * The Channel event. Channel events are created for the following actions:
 * <PRE>
 * - when a Client has joined a Channel.
 * - when a Client has left a Channel.
 * - when a Client has been invited to join a Channel.
 * - when a Client has been expelled from a Channel.
 * - when a Client has added a Consumer to the Channel.
 * - when a Client has removed a Consumer from the Channel.
 * </PRE>
 *
 * @version     2.3 - 20th November 2017
 * @author      Rich Burridge
 * @since       JSDT 1.0
 */

public class
ChannelEvent extends EventObject implements JSDTDebugFlags {

    /** The Channel joined event type. */
    public static final int JOINED           = 1 << 0;

    /** The Channel left event type. */
    public static final int LEFT             = 1 << 1;

    /** The Channel invited event type. */
    public static final int INVITED          = 1 << 2;

    /** The Channel expelled event type. */
    public static final int EXPELLED         = 1 << 3;

    /** The Channel consumer added event type. */
    public static final int CONSUMER_ADDED   = 1 << 4;

    /** The Channel consumer removed event type. */
    public static final int CONSUMER_REMOVED = 1 << 5;

    /** The session associated with this Channel event.
     *
     *  @serial
     */
    private final Session session;

    /** The channel this event occured on.
     *
     *  @serial
     */
    protected final Channel channel;

    /** The name of the client causing this event.
     *
     *  @serial
     */
    private final String clientName;

    /** The type of this Channel event.
     *
     *  @serial
     */
    private final int type;


/**
 * <A NAME="SD_CHANNELEVENT"></A>
 * constructor for the ChannelEvent class. A new channel event is generated
 * for a client action for a specific channel.
 *
 * @param session the session this channel belongs to.
 * @param clientName the name of the client.
 * @param channel the channel.
 * @param type the type of event.
 */

    public
    ChannelEvent(Session session, String clientName,
                 Channel channel, int type) {
        super(channel);

        if (ChannelEvent_Debug) {
            JSDTObject.Debug("ChannelEvent: constructor:" +
                             " session name: " + session.getName() +
                             " client name: "  + clientName +
                             " channel name: " + channel.getName() +
                             " type: "         + type);
        }

        this.session    = session;
        this.clientName = clientName;
        this.channel    = channel;
        this.type       = type;
    }


/**
 * <A NAME="SD_GETSESSION"></A>
 * get the Session associated with this event.
 *
 * @return the Session associated with this event.
 */

    public Session
    getSession() {
        if (ChannelEvent_Debug) {
            JSDTObject.Debug("ChannelEvent: getSession.");
        }

        return(session);
    }


/**
 * <A NAME="SD_GETCLIENTNAME"></A>
 * get the name of the Client that generated this event.
 *
 * @return the name of the Client that generated this event.
 */

    public String
    getClientName() {
        if (ChannelEvent_Debug) {
            JSDTObject.Debug("ChannelEvent: getClientName.");
        }

        return(clientName);
    }


/**
 * <A NAME="SD_GETCHANNEL"></A>
 * get the Channel associated with this event.
 *
 * @return the Channel associated with this event.
 */

    public Channel
    getChannel() {
        if (ChannelEvent_Debug) {
            JSDTObject.Debug("ChannelEvent: getChannel.");
        }

        return(channel);
    }


/**
 * <A NAME="SD_TOSTRING"></A>
 * <EM>toString</EM> print a short description of this Channel event.
 *
 * @return a String containing a description of this Channel event.
 *
 * @since        JSDT 1.3
 */

    public String
    toString() {
        String typeAsString = null;

        switch (type) {
            case JOINED:           typeAsString = "joined";
                                   break;
            case LEFT:             typeAsString = "left";
                                   break;
            case INVITED:          typeAsString = "invited";
                                   break;
            case EXPELLED:         typeAsString = "expelled";
                                   break;
            case CONSUMER_ADDED:   typeAsString = "consumer added";
                                   break;
            case CONSUMER_REMOVED: typeAsString = "consumer removed";
                                   break;
        }

        return("Channel event:" + "\n" +
                " session name: " + session.getName() + "\n" +
                " client name: "  + clientName + "\n" +
                " channel name: " + channel.getName() + "\n" +
                " type: "         + typeAsString + "\n");
    }
}
