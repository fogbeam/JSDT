
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

/**
 * JSDT debug flags interface.
 *
 * @version     2.3 - 28th October 2017
 * @author      Rich Burridge
 */

interface templateDebugFlags {

    boolean ByteArrayProxy_Debug     = true;
    boolean ByteArrayServer_Debug    = true;
    boolean ChannelProxy_Debug       = true;
    boolean ChannelServer_Debug      = true;
    boolean ManageableProxy_Debug    = true;
    boolean ManageableServer_Debug   = true;
    boolean NamingProxy_Debug        = true;
    boolean Registry_Debug           = true;
    boolean SessionProxy_Debug       = true;
    boolean SessionServer_Debug      = true;
    boolean TokenProxy_Debug         = true;
    boolean TokenServer_Debug        = true;

    boolean templateClient_Debug     = true;
    boolean templateSession_Debug    = true;
}
