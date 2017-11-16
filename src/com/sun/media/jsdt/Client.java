
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

import java.io.Serializable;

/**
 * The Client interface.
 *
 * @version     2.3 - 26th October 2017
 * @author      Rich Burridge
 * @since       JSDT 1.0
 */

public interface Client extends Serializable {

/**
 * <A NAME="SD_AUTHENTICATE"></A>
 * used to authenticate a Client for potentially joining a managed object,
 * or creating or destroying a ByteArray, Channel or Token within a managed
 * Session. The ByteArray, Channel, Session or Token manager will be doing
 * this Client validation.
 *
 * <P>The manager sends the Client an authentication request. Within this
 * request is a challenge. The Client replies with a response.
 * This response is validated by the manager and determines if the
 * Client will be allowed to join the ByteArray/Channel/Session/Token
 * or create/destroy the ByteArray/Channel/Token.
 *
 * <P>The challenge given by the manager and the response provided by the
 * Client are both Java objects. There must be some agreed policy between
 * the manager and the Client with regards to these objects. In other words
 * the Client needs to know what to do with the challenge and how to respond
 * to it, and the manager needs to know how to handle that response.
 *
 * @param info the authentication information. This object needs to be
 * serializable.
 *
 * @return the response by the Client to the managers challenge.
 */

    Object
    authenticate(AuthenticationInfo info);


/**
 * <A NAME="SD_GETNAME"></A>
 * get the name of this Client.
 *
 * @return the name of the Client.
 */

    String
    getName();
}
