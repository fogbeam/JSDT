
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
 * JSDT debug flags interface.
 *
 * @version     2.3 - 6th November 2017
 * @author      Rich Burridge
 */

interface httpDebugFlags {

    boolean AuthenticateClient_Debug        = false;

    boolean ByteArrayProxy_Debug            = false;
    boolean ByteArrayServer_Debug           = false;

    boolean ChannelProxy_Debug              = false;
    boolean ChannelProxyMessage_Debug       = false;
    boolean ChannelServer_Debug             = false;

    boolean CheckToken_Debug                = false;
    boolean CleanupConnections_Debug        = false;

    boolean ClientMessage_Debug             = false;
    boolean ClientProxy_Debug               = false;
    boolean ClientProxyMessage_Debug        = false;
    boolean ClientProxyThread_Debug         = false;
    boolean ClientServer_Debug              = false;
    boolean ClientServerMessage_Debug       = false;
    boolean ClientServerThread_Debug        = false;

    boolean ConnectionMessage_Debug         = false;
    boolean ConnectionThread_Debug          = false;
    boolean ConsumerMessage_Debug           = false;

    boolean DataReceivedMessage_Debug       = false;
    boolean DataReceivedQueue_Debug         = false;
    boolean DataReceivedThread_Debug        = false;

    boolean HttpAwareServerSocket_Debug     = false;
    boolean HttpInputStream_Debug           = false;
    boolean HttpOutputStream_Debug          = false;
    boolean HttpReceiveSocket_Debug         = false;
    boolean HttpSendInputStream_Debug       = false;
    boolean HttpSendOutputStream_Debug      = false;
    boolean HttpSendSocket_Debug            = false;
    boolean HttpThread_Debug                = false;
    boolean HttpToCGISocketFactory_Debug    = false;
    boolean HttpToPortSocketFactory_Debug   = false;

    boolean JSDTMasterSocketFactory_Debug   = false;
    boolean JSDTMessage_Debug               = false;

    boolean ListenerMessage_Debug           = false;

    boolean ManageableProxy_Debug           = false;
    boolean ManageableServer_Debug          = false;
    boolean ManagerPermThread_Debug         = false;
    boolean ManagerProxyThread_Debug        = false;
    boolean ManagerProxyMessage_Debug       = false;

    boolean NamingPermThread_Debug          = false;
    boolean NamingProxy_Debug               = false;
    boolean NamingProxyMessage_Debug        = false;
    boolean NamingProxyThread_Debug         = false;

    boolean PingThread_Debug                = false;

    boolean ReceiveClient_Debug             = false;

    boolean Registry_Debug                  = false;
    boolean RegistryClient_Debug            = false;
    boolean RegistryMessage_Debug           = false;
    boolean RegistryPingThread_Debug        = false;
    boolean RegistryServerMessage_Debug     = false;
    boolean RegistryServerThread_Debug      = false;

    boolean SameVMManagerProxyThread_Debug  = false;
    boolean SameVMSessionProxyThread_Debug  = false;
    boolean SameVMSessionServerThread_Debug = false;
    boolean SameVMThread_Debug              = false;

    boolean ServerListenerId_Debug          = false;

    boolean SessionPermThread_Debug         = false;
    boolean SessionProxy_Debug              = false;
    boolean SessionProxyMessage_Debug       = false;
    boolean SessionProxyThread_Debug        = false;
    boolean SessionServer_Debug             = false;
    boolean SessionServerMessage_Debug      = false;
    boolean SessionServerThread_Debug       = false;

    boolean TCPSocket_Debug                 = false;
    boolean TCPSocketFactory_Debug          = false;
    boolean TCPSocketServer_Debug           = false;
    boolean TokenProxy_Debug                = false;
    boolean TokenServer_Debug               = false;

    boolean WrappedSocket_Debug             = false;

    boolean httpClient_Debug                = false;
    boolean httpSession_Debug               = false;
}
