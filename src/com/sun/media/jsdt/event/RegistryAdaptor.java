
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
 * The adaptor which receives Registry events.
 * The methods in this class are empty; this class is provided as a
 * convenience for easily creating listeners by extending this class
 * and overriding only the methods of interest.
 *
 * @version     2.3 - 27th October 2017
 * @author      Rich Burridge
 * @since       JSDT 2.0
 */

public abstract class
RegistryAdaptor implements RegistryListener {

/**
 * <A NAME="SD_SESSIONCREATED"></A>
 * invoked when a Session has been created.
 *
 * @param event the Registry event containing more information.
 */

    public void
    sessionCreated(RegistryEvent event) { }


/**
 * <A NAME="SD_SESSIONDESTROYED"></A>
 * invoked when a Session has been destroyed.
 *
 * @param event the Registry event containing more information.
 */

    public void
    sessionDestroyed(RegistryEvent event)  { }


/**
 * <A NAME="SD_CLIENTCREATED"></A>
 * invoked when a Client has been created.
 *
 * @param event the Registry event containing more information.
 */

    public void
    clientCreated(RegistryEvent event) { }


/**
 * <A NAME="SD_CLIENTDESTROYED"></A>
 * invoked when a Client has been destroyed.
 *
 * @param event the Registry event containing more information.
 */

    public void
    clientDestroyed(RegistryEvent event) { }


/**
 * <A NAME="SD_CONNECTIONFAILED"></A>
 * invoked if a problem is detected with the connection to the Registry.
 *
 * <P>This listener is automatically removed as a listener on the connection
 * that reported the failure, to prevent continual failure notifications.
 *
 * @param event the Registry event containing more information.
 */

    public void
    connectionFailed(RegistryEvent event) { }
}
