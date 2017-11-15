
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

/**
 * The test client for the test environment for JSDT.
 *
 * @version     2.3 - 15th November 2017
 * @author      Rich Burridge
 */

public class
TestClient implements Client, TestDebugFlags {

    protected String name;


    public
    TestClient(String name) {
        if (TestClient_Debug) {
            System.err.println("TestClient: constructor.");
        }

        this.name = name;
    }


    public Object
    authenticate(AuthenticationInfo info) {
        char   type      = info.getType();
        String name      = info.getName();
        int    action    = info.getAction();
        String challenge = (String) info.getChallenge();
        String response  = null;

        if (TestClient_Debug) {
            System.err.println("TestClient: authenticate:" +
                                " type: "      + type +
                                " name: "      + name +
                                " action: "    + action +
                                " challenge: " + challenge);
        }

        if (type == AuthenticationInfo.SESSION) {
            response = "abcdef";
        } else if (type == AuthenticationInfo.CHANNEL) {
            response = "ghijkl";
        } else if (type == AuthenticationInfo.TOKEN) {
            response = "mnopqr";
        } else if (type == AuthenticationInfo.BYTEARRAY) {
            response = "stuvwx";
        }
        return(response);
    }


    public String
    getName() {
        if (TestClient_Debug) {
            System.err.println("TestClient: getName.");
        }

        return(name);
    }
}
