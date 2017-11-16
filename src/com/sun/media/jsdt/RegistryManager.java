
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
 * The Registry Manager interface.
 *
 * @version     2.3 - 26th October 2017
 * @author      Rich Burridge
 * @since       JSDT 1.6
 */

public interface
RegistryManager extends JSDTManager {

/**
 * <A NAME="SD_REGISTRYREQUEST"></A>
 * called when there is a Client interested in performing a priviledged
 * operation on a managed Registry.
 *
 * <PRE>
 * The following priviledged operations could occur:
 *
 * CREATE a Session within the managed Registry.
 * DESTROY a Session within the managed Registry.
 * CREATE a Client within the managed Registry.
 * DESTROY a Client within the managed Registry.
 * </PRE>
 *
 * @param info the authentication information.
 * @param client the Client.
 *
 * @see AuthenticationInfo
 *
 * @return true if the client is allowed to perform the priviledged
 * operation; false if not.
 */

    boolean
    registryRequest(AuthenticationInfo info, Client client);
}
