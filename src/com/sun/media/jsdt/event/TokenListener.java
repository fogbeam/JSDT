
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
 * The listener interface for receiving Token events.
 *
 * @version     2.3 - 27th October 2017
 * @author      Rich Burridge
 * @since       JSDT 1.0
 */

public interface
TokenListener extends EventListener, Serializable {

/**
 * <A NAME="SD_TOKENJOINED"></A>
 * invoked when a Client has joined a Token.
 *
 * @param event the Token event containing more information.
 */

    void
    tokenJoined(TokenEvent event);


/**
 * <A NAME="SD_TOKENLEFT"></A>
 * invoked when a Client has left a Token.
 *
 * @param event the Token event containing more information.
 */

    void
    tokenLeft(TokenEvent event);


/**
 * <A NAME="SD_TOKENGIVEN"></A>
 * invoked when a Token has been given by one Client to another.
 *
 * @param event the Token event containing more information.
 */

    void
    tokenGiven(TokenEvent event);


/**
 * <A NAME="SD_TOKENREQUESTED"></A>
 * invoked when a Client has requested a Token from one or more Clients
 * that currently have it grabbed/inhibited.
 *
 * @param event the Token event containing more information.
 */

    void
    tokenRequested(TokenEvent event);


/**
 * <A NAME="SD_TOKENGRABBED"></A>
 * invoked when a Client has grabbed/inhibited a Token.
 *
 * @param event the Token event containing more information.
 */

    void
    tokenGrabbed(TokenEvent event);


/**
 * <A NAME="SD_TOKENRELEASED"></A>
 * invoked when a Client has released itself from a previously
 * grabbed/inhibited Token.
 *
 * @param event the Token event containing more information.
 */

    void
    tokenReleased(TokenEvent event);


/**
 * <A NAME="SD_TOKENINVITED"></A>
 * invoked when a Client has been invited to join a Token.
 *
 * @param event the Token event containing more information.
 */

    void
    tokenInvited(TokenEvent event);


/**
 * <A NAME="SD_TOKENEXPELLED"></A>
 * invoked when a Client has been expelled from a Token.
 *
 * @param event the Token event containing more information.
 */

    void
    tokenExpelled(TokenEvent event);
}
