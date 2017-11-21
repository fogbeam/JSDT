
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

package examples.sound;

import com.sun.media.jsdt.*;

/**
 * This is just a simple server/client based proof-of-concept
 * implementation to test UDP channels.
 *
 * The server continuously sends a sound file over a UDP channel
 * to each client joined to the channel. These in turn, play the sound on
 * the workstation speaker.
 *
 * @version     2.3 - 20th November 2017
 * @author      Rich Burridge
 */

public class
SoundClient implements Client, SoundDebugFlags {

    protected final String name;

    public
    SoundClient(String name) {
        if (SoundClient_Debug) {
            System.err.println("SoundClient: constructor.");
        }

        this.name = name;
    }


    public Object
    authenticate(AuthenticationInfo info) {
        if (SoundClient_Debug) {
            System.err.println("SoundClient: authenticate.");
        }

        return(null);
    }


    public String
    getName() {
        if (SoundClient_Debug) {
            System.err.println("SoundClient: getName.");
        }

        return(name);
    }
}
