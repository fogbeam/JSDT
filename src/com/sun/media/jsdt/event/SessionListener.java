
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
 * The listener interface for receiving Session events.
 *
 * @version     2.3 - 27th October 2017
 * @author      Rich Burridge
 * @since       JSDT 1.0
 */

public interface
SessionListener extends EventListener, Serializable {

/**
 * <A NAME="SD_BYTEARRAYCREATED"></A>
 * invoked when a ByteArray has been created.
 *
 * @param event the Session event containing more information.
 */

    void
    byteArrayCreated(SessionEvent event);


/**
 * <A NAME="SD_BYTEARRAYDESTROYED"></A>
 * invoked when a ByteArray has been destroyed.
 *
 * @param event the Session event containing more information.
 */

    void
    byteArrayDestroyed(SessionEvent event);


/**
 * <A NAME="SD_CHANNELCREATED"></A>
 * invoked when a Channel has been created.
 *
 * @param event the Session event containing more information.
 */

    void
    channelCreated(SessionEvent event);


/**
 * <A NAME="SD_CHANNELDESTROYED"></A>
 * invoked when a Channel has been destroyed.
 *
 * @param event the Session event containing more information.
 */

    void
    channelDestroyed(SessionEvent event);


/**
 * <A NAME="SD_SESSIONDESTROYED"></A>
 * invoked when a Client has destroyed a Session.
 *
 * @param event the Session event containing more information.
 *
 * @since        JSDT 1.3
 */

    void
    sessionDestroyed(SessionEvent event);


/**
 * <A NAME="SD_SESSIONJOINED"></A>
 * invoked when a Client has joined a Session.
 *
 * @param event the Session event containing more information.
 */

    void
    sessionJoined(SessionEvent event);


/**
 * <A NAME="SD_SESSIONLEFT"></A>
 * invoked when a Client has left a Session.
 *
 * @param event the Session event containing more information.
 */

    void
    sessionLeft(SessionEvent event);


/**
 * <A NAME="SD_SESSIONINVITED"></A>
 * invoked when a Client has been invited to join a Session.
 *
 * @param event the Session event containing more information.
 */

    void
    sessionInvited(SessionEvent event);


/**
 * <A NAME="SD_SESSIONEXPELLED"></A>
 * invoked when a Client has been expelled from a Session.
 *
 * @param event the Session event containing more information.
 */

    void
    sessionExpelled(SessionEvent event);


/**
 * <A NAME="SD_TOKENCREATED"></A>
 * invoked when a Token has been created.
 *
 * @param event the Session event containing more information.
 */

    void
    tokenCreated(SessionEvent event);


/**
 * <A NAME="SD_TOKENDESTROYED"></A>
 * invoked when a Token has been destroyed.
 *
 * @param event the Session event containing more information.
 */

    void
    tokenDestroyed(SessionEvent event);
}
