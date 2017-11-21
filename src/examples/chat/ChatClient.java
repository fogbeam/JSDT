
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

package examples.chat;

import com.sun.media.jsdt.*;

/**
 * This is just a simple server/client based proof-of-concept
 * implementation to make sure that the Shared Data classes have
 * all the required API in them.
 *
 * It's based on the chat demonstration which is part of the
 * west-coast rmi distribution.
 *
 * @version     2.3 - 20th November 2017
 * @author      Rich Burridge
 */

public class
ChatClient implements Client {

    protected final String name;

    public
    ChatClient(String name) {
        this.name = name;
    }


    public Object
    authenticate(AuthenticationInfo info) {
        System.err.println("ChatClient: authenticate.");
        return(null);
    }


    public String
    getName() {
        return(name);
    }
}
