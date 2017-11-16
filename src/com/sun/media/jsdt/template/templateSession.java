
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

package com.sun.media.jsdt.template;

import com.sun.media.jsdt.*;
import com.sun.media.jsdt.impl.SessionImpl;

/**
 * JSDT implementation specific Session class.
 *
 * @version     2.3 - 28th October 2017
 * @author      Rich Burridge
 */

public class
templateSession extends SessionImpl implements templateDebugFlags {

/**
 * <A NAME="SD_TEMPLATESESSION"></A>
 * <EM>templateSession</EM> the default constructor for the templateSession
 * class. Note that the session name is supplied by _createProxy or
 * _createServer.
 */

    public
    templateSession() {
        if (templateSession_Debug) {
            debug("templateSession: constructor.");
        }
    }


/**
 * <A NAME="SD__CREATEPROXY"></A>
 * <EM>_createProxy</EM> create a proxy-side connection for this Session.
 *
 * @param namingProxy the naming proxy that created this session.
 * @param name the name of the session being constructed.
 * @param sessionNo the unique session number for this session name.
 * @param connectionType the type of this connection.
 * @param host the host of the server-side connection.
 * @param port the port number to use for the connection.
 *
 * @exception NoSuchHostException if the host given doesn't exist.
 */

    public final synchronized void
    _createProxy(NamingProxy namingProxy, String name, short sessionNo,
                 String connectionType, String host, int port)
        throws NoSuchHostException {
        if (templateSession_Debug) {
            debug("templateSession: _createProxy:" +
                  " naming proxy: "    + namingProxy +
                  " object name: "     + name +
                  " session #: "       + sessionNo +
                  " connection type: " + connectionType +
                  " host: "            + host +
                  " port: "            + port);
        }
    }


/**
 * <A NAME="SD__CREATESERVER"></A>
 * <EM>_createServer</EM> create a server-side connection for this Session.
 *
 * @param name the name of the session being constructed.
 * @param sessionNo the unique session number for this session name.
 * @param connectionType the type of this connection.
 * @param url the url associated with this object.
 * @param port the port number to use for the connection.
 *
 * @exception PortInUseException if this port is being used by another
 * application.
 */

    public final synchronized void
    _createServer(String name, short sessionNo, String connectionType,
                  String url, int port)
        throws PortInUseException {
        if (templateSession_Debug) {
            debug("templateSession: _createServer:" +
                  " object name: "     + name +
                  " session #: "       + sessionNo +
                  " connection type: " + connectionType +
                  " url: "             + url +
                  " port: "            + port);
        }
    }
}
