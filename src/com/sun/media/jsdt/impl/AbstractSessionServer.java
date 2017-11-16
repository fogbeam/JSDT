
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


/**
 * JSDT abstract server-side Session interface.
 *
 * @version     2.3 - 28th October 2017
 * @author      Rich Burridge
 */

public interface
AbstractSessionServer extends AbstractManageableServer {

/**
 * <A NAME="SD_INITSERVER"></A>
 * <EM>initServer</EM> initialise the "server-side" for this Session.
 *
 * @param name the name of the server Session being constructed.
 * @param session the server-side Session.
 * @param object the server-side Session (same as session).
 */

    void
    initServer(String name, SessionImpl session, Object object);


/**
 * <A NAME="SD_GETSERVER"></A>
 * <EM>getServer</EM> get a handle to the "server-side" for this Session.
 *
 * @return the "server-side" for this Session.
 */

    Object
    getServer();
}
