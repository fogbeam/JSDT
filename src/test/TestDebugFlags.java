
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
 * @version     2.3 - 16th November 2017
 * @author      Rich Burridge
 */

public
interface TestDebugFlags {

    boolean ExpelClient_Debug            = false;
    boolean ExpelManagerListener_Debug   = false;
    boolean ExpelUserListener_Debug      = false;

    boolean GiverClient_Debug            = false;

    boolean HolderClient_Debug           = false;

    boolean InviteClient_Debug           = false;
    boolean InviteManagerListener_Debug  = false;
    boolean InviteUserListener_Debug     = false;

    boolean ReceiveManager_Debug         = false;
    boolean ReceiverClient_Debug         = false;
    boolean RequesterClient_Debug        = false;

    boolean SendData_Debug               = false;

    boolean TestChannelConsumer_Debug    = false;
    boolean TestClient_Debug             = false;
    boolean TestManagerListener_Debug    = false;
    boolean TestServer_Debug             = false;
    boolean TestUser_Debug               = false;
}
