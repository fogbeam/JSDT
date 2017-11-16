
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
 * The listener interface for receiving Connection events.
 *
 * @version     2.3 - 27th October 2017
 * @author      Rich Burridge
 * @since       JSDT 2.0
 */

public interface
ConnectionListener extends EventListener, Serializable {

/**
 * <A NAME="SD_CONNECTIONFAILED"></A>
 * invoked if a problem is detected with (one of) this applications
 * underlying connection(s).
 *
 * <P>This listener is automatically removed as a listener on the connection
 * that reported the failure, to prevent continual failure notifications.
 *
 * @param event the Connection event containing more information.
 */

    void
    connectionFailed(ConnectionEvent event);
}
