
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
 * The receiver client for the test environment for JSDT.
 *
 * @version     2.3 - 21st November 2017
 * @author      Rich Burridge
 */

public class
ReceiverClient extends ClientAdaptor implements Client, TestDebugFlags {

    private   Token  token;

    protected final String name;


    public
    ReceiverClient(String name) {
        if (ReceiverClient_Debug) {
            System.err.println("ReceiverClient: constructor.");
        }

        this.name = name;
    }


    public void
    setToken(Token token) {
        if (ReceiverClient_Debug) {
            System.err.println("ReceiverClient: setToken:" +
                                " token: " + token);
        }

        this.token = token;
    }


    public Object
    authenticate(AuthenticationInfo info) {
        if (ReceiverClient_Debug) {
            System.err.println("ReceiverClient: authenticate:" +
                                " info: " + info);
        }

        return(null);
    }


    public String
    getName() {
        if (ReceiverClient_Debug) {
            System.err.println("ReceiverClient: getName.");
        }

        return(name);
    }


    public void
    tokenGiven(ClientEvent event) {
        String tokenName = event.getResourceName();

        System.out.println("Got a Client TOKEN_GIVEN event for token: " +
                           tokenName);
        if (tokenName.equals(token.getName())) {
            try {
                System.err.println("Client: " + this.getName() +
                                   " grabs the token.");
                token.grab(this, true);
                System.err.println("Client: " + this.getName() +
                                   " leaves the token.");
                token.leave(this);
            } catch (Exception e) {
                System.err.println("ReceiverClient: tokenGiven:" +
                                   " exception " + e);
                e.printStackTrace();
            }
        }
    }
}
