
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
 * The giver client for the test environment for JSDT.
 *
 * @version     2.3 - 20th November 2017
 * @author      Rich Burridge
 */

public class
GiverClient extends TokenAdaptor implements Client, TestDebugFlags {

    protected String name;


    public
    GiverClient(String name) {
        if (GiverClient_Debug) {
            System.err.println("GiverClient: constructor.");
        }

        this.name = name;
    }


    public Object
    authenticate(AuthenticationInfo info) {
        if (GiverClient_Debug) {
            System.err.println("GiverClient: authenticate:" +
                                " info: " + info);
        }

        return(null);
    }


    public String
    getName() {
        if (GiverClient_Debug) {
            System.err.println("GiverClient: getName.");
        }

        return(name);
    }


    public void
    tokenJoined(TokenEvent event) {
        Session session    = event.getSession();
        String  clientName = event.getClientName();
        Token   token      = event.getToken();

        if (GiverClient_Debug) {
            System.err.println("GiverClient: tokenJoined:" +
                               " event: " + event);

            System.err.println("GiverClient: tokenJoined:" +
                               " client name "  + clientName +
                               " from session " + session +
                               " token "        + token);
        }

        if (clientName.equals("Receiver")) {
            try {
                TestUser.rsleep(4, 6);
                System.err.println("Client: " + this.getName() +
                                   " gives the token to: " + clientName);
                token.give(this, clientName);

                TestUser.rsleep(4, 6);
                System.err.println("Client: " + this.getName() +
                                   " leaves the token.");
                token.leave(this);

            } catch (Exception e) {
                System.err.println("GiverClient: tokenJoined:" +
                                   " exception " + e);
                e.printStackTrace();
            }
        }
    }
}
