
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

package test;

import com.sun.media.jsdt.*;
import com.sun.media.jsdt.event.*;

/**
 * The requester client for the test environment for JSDT.
 *
 * @version     2.3 - 15th November 2017
 * @author      Rich Burridge
 */

public class
RequesterClient extends TokenAdaptor implements Client, TestDebugFlags {

    protected String name;


    public
    RequesterClient(String name) {
        if (RequesterClient_Debug) {
            System.err.println("RequesterClient: constructor.");
        }

        this.name = name;
    }


    public Object
    authenticate(AuthenticationInfo info) {
        if (RequesterClient_Debug) {
            System.err.println("RequesterClient: authenticate:" +
                                " info: " + info);
        }

        return(null);
    }


    public String
    getName() {
        if (RequesterClient_Debug) {
            System.err.println("RequesterClient: getName.");
        }

        return(name);
    }


    public void
    tokenLeft(TokenEvent event) {
        Session session    = event.getSession();
        String  clientName = event.getClientName();
        Token   token      = event.getToken();

        if (RequesterClient_Debug) {
            System.err.println("RequesterClient: tokenLeft:" +
                               " event: " + event);

            System.err.println("RequesterClient: tokenLeft:" +
                               " client name "  + clientName +
                               " from session " + session +
                               " token "        + token);
        }

        if (clientName.equals("Holder")) {
            try {
                System.err.println("Client: " + this.getName() +
                                   " grabs the token.");
                token.grab(this, true);
            } catch (Exception e) {
                System.err.println("RequesterClient: tokenLeft:" +
                                   " exception " + e);
                e.printStackTrace();
            }
        }
    }
}
