
/*
 *  Copyright (c) 1996-2005 James Begole.
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

package examples.ppong;

import com.sun.media.jsdt.Channel;
import com.sun.media.jsdt.Client;

/**
 * @version     2.3 - 31st October 2017
 * @author      James "Bo" Begole
 * @author      Rich Burridge
 */

interface ConnectionHandler {

    void connectionUp(String message, Object who);

    void connectionDown(String message, Object who);

    void connectionMishap(String message, Object who);

    void handleSession(Channel channel, Client client, Object who);

    void changeStatus(String message, Object who);

    void debugLog(String message, Object who);
}
