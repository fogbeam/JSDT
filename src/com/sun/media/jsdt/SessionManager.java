
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

package com.sun.media.jsdt;

/**
 * The Session Manager interface.
 *
 * @version     2.3 - 26th October 2017
 * @author      Rich Burridge
 * @since       JSDT 1.0
 */

public interface
SessionManager extends JSDTManager {

/**
 * <A NAME="SD_SESSIONREQUEST"></A>
 * called when there is a Client interested in performing a priviledged
 * operation on a managed Session.
 *
 * <PRE>
 * The following priviledged operations could occur:
 *
 * CREATE a ByteArray within the managed Session.
 * DESTROY a ByteArray within the managed Session.
 * CREATE a Channel within the managed Session.
 * DESTROY a Channel within the managed Session.
 * CREATE a Token within the managed Session.
 * DESTROY a Token within the managed Session.
 * JOIN a managed Session.
 * </PRE>
 *
 * @param session the Session the Client is interested in.
 * @param info the authentication information.
 * @param client the Client.
 *
 * @see AuthenticationInfo
 *
 * @return true if the client is allowed to perform the priviledged
 * operation; false if not.
 */

    boolean
    sessionRequest(Session session, AuthenticationInfo info, Client client);
}
