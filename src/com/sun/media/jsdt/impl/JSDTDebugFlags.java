
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

package com.sun.media.jsdt.impl;

/**
 * JSDT debug flags interface.
 *
 * @version     2.3 - 6th November 2017
 * @author      Rich Burridge
 */

public interface JSDTDebugFlags {

    boolean AuthenticationInfo_Debug         = false;
    boolean ByteArrayImpl_Debug              = false;
    boolean ChannelImpl_Debug                = false;
    boolean ChannelConsumerImpl_Debug        = false;
    boolean ClientImpl_Debug                 = false;
    boolean ClientFactory_Debug              = false;
    boolean Connection_Debug                 = false;
    boolean Data_Debug                       = false;
    boolean JSDTByteArrayInputStream_Debug   = false;
    boolean JSDTI18N_Debug                   = false;
    boolean JSDTListenerImpl_Debug           = false;
    boolean JSDTManagerImpl_Debug            = false;
    boolean JSDTObject_Debug                 = false;
    boolean JSDTSecurity_Debug               = false;
    boolean JSDTThread_Debug                 = false;
    boolean ManageableImpl_Debug             = false;
    boolean Message_Debug                    = false;
    boolean Naming_Debug                     = false;
    boolean RegistryFactory_Debug            = false;
    boolean SessionImpl_Debug                = false;
    boolean SessionFactory_Debug             = false;
    boolean TargetableThread_Debug           = false;
    boolean TokenImpl_Debug                  = false;
    boolean URLString_Debug                  = false;
    boolean Util_Debug                       = false;

    boolean ByteArrayEvent_Debug             = false;
    boolean ChannelEvent_Debug               = false;
    boolean ClientEvent_Debug                = false;
    boolean ConnectionEvent_Debug            = false;
    boolean RegistryEvent_Debug              = false;
    boolean SessionEvent_Debug               = false;
    boolean TokenEvent_Debug                 = false;

    boolean AlreadyBoundException_Debug      = false;
    boolean ClientNotGrabbingException_Debug = false;
    boolean ClientNotReleasedException_Debug = false;
    boolean ConnectionException_Debug        = false;
    boolean InvalidClientException_Debug     = false;
    boolean InvalidURLException_Debug        = false;
    boolean JSDTException_Debug              = false;
    boolean ManagerExistsException_Debug     = false;
    boolean NameInUseException_Debug         = false;
    boolean NoRegistryException_Debug        = false;
    boolean NoSuchByteArrayException_Debug   = false;
    boolean NoSuchChannelException_Debug     = false;
    boolean NoSuchClientException_Debug      = false;
    boolean NoSuchConsumerException_Debug    = false;
    boolean NoSuchHostException_Debug        = false;
    boolean NoSuchListenerException_Debug    = false;
    boolean NoSuchManagerException_Debug     = false;
    boolean NoSuchSessionException_Debug     = false;
    boolean NoSuchTokenException_Debug       = false;
    boolean NotBoundException_Debug          = false;
    boolean PermissionDeniedException_Debug  = false;
    boolean PortInUseException_Debug         = false;
    boolean RegistryExistsException_Debug    = false;
    boolean TimedOutException_Debug          = false;
    boolean UnknownException_Debug           = false;
}
