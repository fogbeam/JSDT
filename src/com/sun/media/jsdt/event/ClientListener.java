
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

import java.util.EventListener;
import java.io.Serializable;

/**
 * The listener interface for receiving Client events.
 *
 * @version     2.3 - 27th October 2017
 * @author      Rich Burridge
 * @since       JSDT 1.0
 */

public interface
ClientListener extends EventListener, Serializable {

/**
 * <A NAME="SD_BYTEARRAYINVITED"></A>
 * invoked when a Client has been invited to join a ByteArray.
 *
 * @param event the Client event containing more information.
 */

    void
    byteArrayInvited(ClientEvent event);


/**
 * <A NAME="SD_BYTEARRAYEXPELLED"></A>
 * invoked when a Client has been expelled from a ByteArray.
 *
 * @param event the Client event containing more information.
 */

    void
    byteArrayExpelled(ClientEvent event);


/**
 * <A NAME="SD_CHANNELINVITED"></A>
 * invoked when a Client has been invited to join a Channel.
 *
 * @param event the Client event containing more information.
 */

    void
    channelInvited(ClientEvent event);


/**
 * <A NAME="SD_CHANNELEXPELLED"></A>
 * invoked when a Client has been expelled from a Channel.
 *
 * @param event the Client event containing more information.
 */

    void
    channelExpelled(ClientEvent event);


/**
 * <A NAME="SD_SESSIONINVITED"></A>
 * invoked when a Client has been invited to join a Session.
 *
 * @param event the Client event containing more information.
 */

    void
    sessionInvited(ClientEvent event);


/**
 * <A NAME="SD_SESSIONEXPELLED"></A>
 * invoked when a Client has been expelled from a Session.
 *
 * @param event the Client event containing more information.
 */

    void
    sessionExpelled(ClientEvent event);


/**
 * <A NAME="SD_TOKENINVITED"></A>
 * invoked when a Client has been invited to join a Token.
 *
 * @param event the Client event containing more information.
 */

    void
    tokenInvited(ClientEvent event);


/**
 * <A NAME="SD_TOKENEXPELLED"></A>
 * invoked when a Client has been expelled from a Token.
 *
 * @param event the Client event containing more information.
 */

    void
    tokenExpelled(ClientEvent event);


/**
 * <A NAME="SD_TOKENGIVEN"></A>
 * invoked when a Client has been given a Token.
 *
 * @param event the Client event containing more information.
 */

    void
    tokenGiven(ClientEvent event);
}
