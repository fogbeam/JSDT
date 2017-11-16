
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

package com.sun.media.jsdt.http;

/**
 * JSDTSocketInfo is an interface that extensions of the java.net.Socket
 * class may use to provide more information on its capabilities.
 *
 * Based on the sun.rmi.transport.proxy.RMISocketInfo class.
 *
 * @version     2.3 - 28th October 2017
 * @author      Rich Burridge
 */

public interface
JSDTSocketInfo {

    /**
     * Return true if this socket can be used for more than one JSDT call.
     * If a socket does not implement this interface, then it is assumed
     * to be reusable.
     *
     * @return whether this socket is reusable.
     */

    boolean
    isReusable();


    /**
     * Sets an indication of whether this socket is reusable.
     *
     * @param reusable true if socket is reusable.
     */

    void
    setReusable(boolean reusable);
}
