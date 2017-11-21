
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
 * The holder client for the test environment for the JSDT.
 *
 * @version     2.3 - 21st November 2017
 * @author      Rich Burridge
 */

public class
HolderClient extends TokenAdaptor implements Client, TestDebugFlags {

    protected final String name;


    public
    HolderClient(String name) {
        if (HolderClient_Debug) {
            System.err.println("HolderClient: constructor.");
        }

        this.name = name;
    }


    public Object
    authenticate(AuthenticationInfo info) {
        if (HolderClient_Debug) {
            System.err.println("HolderClient: authenticate:" +
                                " info: " + info);
        }

        return(null);
    }


    public String
    getName() {
        if (HolderClient_Debug) {
            System.err.println("HolderClient: getName.");
        }

        return(name);
    }


    public void
    tokenRequested(TokenEvent event) {
        Session session    = event.getSession();
        String  clientName = event.getClientName();
        Token   token      = event.getToken();

        if (HolderClient_Debug) {
            System.err.println("HolderClient: tokenRequested:" +
                               " event: " + event);

            System.err.println("HolderClient: tokenRequested:" +
                               " client name "  + clientName +
                               " from session " + session +
                               " token "        + token);
        }

        if (clientName.equals("Requester")) {
            try {
                System.err.println("Client: " + this.getName() +
                                   " releases the token.");
                token.release(this);

                System.err.println("Client: " + this.getName() +
                                   " leaves the token.");
                token.leave(this);
                TestUser.rsleep(2, 3);
                token.removeTokenListener(this);
                TestUser.rsleep(2, 3);
            } catch (Exception e) {
                System.err.println("HolderClient: tokenRequested:" +
                                   " exception " + e);
                e.printStackTrace();
            }
        }
    }
}
