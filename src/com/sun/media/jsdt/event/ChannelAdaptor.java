
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

/**
 * The adaptor which receives Channel events.
 * The methods in this class are empty; this class is provided as a
 * convenience for easily creating listeners by extending this class
 * and overriding only the methods of interest.
 *
 * @version     2.3 - 27th October 2017
 * @author      Rich Burridge
 * @since       JSDT 1.0
 */

public abstract class
ChannelAdaptor implements ChannelListener {

/**
 * <A NAME="SD_CHANNELJOINED"></A>
 * invoked when a Client has joined a Channel.
 *
 * @param event the Channel event containing more information.
 */

    public void
    channelJoined(ChannelEvent event) { }


/**
 * <A NAME="SD_CHANNELLEFT"></A>
 * invoked when a Client has left a Channel.
 *
 * @param event the Channel event containing more information.
 */

    public void
    channelLeft(ChannelEvent event) { }


/**
 * <A NAME="SD_CHANNELINVITED"></A>
 * invoked when a Client has been invited to join a Channel.
 *
 * @param event the Channel event containing more information.
 */

    public void
    channelInvited(ChannelEvent event) { }


/**
 * <A NAME="SD_CHANNELEXPELLED"></A>
 * invoked when a Client has been expelled from a Channel.
 *
 * @param event the Channel event containing more information.
 */

    public void
    channelExpelled(ChannelEvent event) { }


/**
 * <A NAME="SD_CHANNELCONSUMERADDED"></A>
 * invoked when a Client has added a Consumer to this Channel.
 *
 * @param event the Channel event containing more information.
 *
 * @since        JSDT 1.2
 */

    public void
    channelConsumerAdded(ChannelEvent event) { }


/**
 * <A NAME="SD_CHANNELCONSUMERREMOVED"></A>
 * invoked when a Client has removed a Consumer from this Channel.
 *
 * @param event the Channel event containing more information.
 *
 * @since        JSDT 1.2
 */

    public void
    channelConsumerRemoved(ChannelEvent event) { }
}
