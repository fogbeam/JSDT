
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

/**
 * Test environment debug flags interface.
 *
 * @version     2.3 - 15th November 2017
 * @author      Rich Burridge
 */

public
interface TestDebugFlags {

    static final boolean ExpelClient_Debug            = false;
    static final boolean ExpelManagerListener_Debug   = false;
    static final boolean ExpelUserListener_Debug      = false;

    static final boolean GiverClient_Debug            = false;

    static final boolean HolderClient_Debug           = false;

    static final boolean InviteClient_Debug           = false;
    static final boolean InviteManagerListener_Debug  = false;
    static final boolean InviteUserListener_Debug     = false;

    static final boolean ReceiveManager_Debug         = false;
    static final boolean ReceiverClient_Debug         = false;
    static final boolean RequesterClient_Debug        = false;

    static final boolean SendData_Debug               = false;

    static final boolean TestChannelConsumer_Debug    = false;
    static final boolean TestClient_Debug             = false;
    static final boolean TestManagerListener_Debug    = false;
    static final boolean TestServer_Debug             = false;
    static final boolean TestUser_Debug               = false;
}
