
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

import com.sun.media.jsdt.*;

/**
 * The Registry contains a transient database that maps names to JSDT
 * objects. There are two types of JSDT object that can be stored in the
 * Registry; a Session and a Client. When the Registry is first started, it's
 * database is empty. The names stored in the Registry are pure and are not
 * parsed.  A collaborative service storing itself in the Registry may want
 * to prefix the name of the service by a package name (although this is not
 * required), to reduce name collisions in the Registry.
 *
 * @version     2.3 - 16th November 2017
 * @author      Rich Burridge
 */

public interface
AbstractRegistry {

/**
 * <A NAME="SD_STARTREGISTRY"></A>
 * <EM>startRegistry</EM> is a class method that will start a Registry of
 * the appropriate type. The Registry is started in a separate thread.
 * It can be stopped with the stopRegistry method. If the process that
 * started it terminates, then the Registry thread is terminated too.
 *
 * @param registryType the type of Registry to start.
 * @param port the port number that the Registry should run on.
 *
 * @exception RegistryExistsException if a Registry (or some other process)
 * is already running on the port used by the Registry on this machine.
 * @exception NoRegistryException if an invalid registry type was given.
 *
 * @since       JSDT 1.5
 */

    void
    startRegistry(String registryType, int port)
                throws RegistryExistsException, NoRegistryException;


/**
 * <A NAME="SD_STOPREGISTRY"></A>
 * <EM>stopRegistry</EM> is a class method that will stop a Registry of
 * the appropriate type. The Registry was started in a separate thread.
 * This method simply destroys that thread if it exists.
 *
 * @param registryType the type of Registry to start.
 * @param port the port number that the Registry is running on.
 *
 * @exception NoRegistryException if an invalid registry type was given,
 * or the Registry is not running, or wasn't started by the startRegistry
 * method.
 *
 * @since       JSDT 1.5
 */

    void
    stopRegistry(String registryType, int port)
                throws NoRegistryException;


/**
 * <A NAME="SD_ATTACHMANAGER"></A>
 * <EM>attachManager</EM> attachs a manager to this Registry. This manager
 * will authenticate clients trying to create or destroy Sessions or Clients
 * in this Registry.
 *
 * @param manager the registry manager to attach.
 *
 * @exception ManagerExistsException if there is already a manager associated
 * with this manageable object.
 *
 * @since       JSDT 2.0
 */

    void
    attachManager(RegistryManager manager)
                throws ManagerExistsException;


/**
 * <A NAME="SD_REGISTRYEXISTS"></A>
 * <EM>registryExists</EM> is a class method that checks if a Registry,
 * of the given registry type, is already running on the given port.
 *
 * @param registryType the type of Registry to check on.
 * @param port the port number that the Registry is running on.
 *
 * @return true if a Registry is already running; false if it isn't.
 *
 * @exception NoRegistryException if an invalid registry type was given.
 *
 * @since       JSDT 1.5
 */

    boolean
    registryExists(String registryType, int port)
                throws NoRegistryException;
}
