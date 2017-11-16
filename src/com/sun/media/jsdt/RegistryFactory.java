
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

package com.sun.media.jsdt;

import com.sun.media.jsdt.impl.*;
import com.sun.media.jsdt.event.RegistryListener;

/**
 * This is a factory class that starts and stops a Registry of the
 * appropriate type.  Valid types are "socket", "http" or "multicast".
 *
 * <P>As with all types of collaborative environments, there needs to be some
 * way for each application to initially rendezvous, so that data can be
 * shared. The JSDT group rendezvous point is a Session object, and a special
 * URL String is used to describe that Session. This Session information
 * needs to be kept somewhere that is easily accessable to all applications.
 * This is where the Registry fits in.
 *
 * <P>The Registry contains a transient database that maps these URL Strings to
 * JSDT objects. When the Registry is first started, it's database is empty.
 * The names stored in the Registry are pure and are not parsed. A
 * collaborative service storing itself in the Registry may want to prefix
 * the name of the service by a package name (although this is not required),
 * to reduce name collisions.
 *
 * <P>The Registry can also be used to store special Clients. This Client is
 * capable of being invited to join a JSDT Session.
 *
 * <P>If the Registry is started by the RegistryFactory.startRegistry method,
 * then it runs in its own thread. This is not a deamon thread, so
 * it should either be stopped with the stopRegistry method, or it will be
 * terminated when the application that started it, exits.
 *
 * <P>The Registry needs to be started on a "well-known" port. This is
 * defined by the <CODE>registryPort</CODE> property, which by default, is
 * 4561, irrespective of the implementation type.
 *
 * @version     2.3 - 16th November 2017
 * @author      Rich Burridge
 * @since       JSDT 1.1
 *
 * @see         com.sun.media.jsdt.SessionFactory
 * @see         com.sun.media.jsdt.ClientFactory
 */

public class
RegistryFactory extends JSDTObject {

/**
 * <A NAME="SD_STARTREGISTRY"></A>
 * <EM>startRegistry</EM> is a class method that will start a Registry of
 * the appropriate type, using the current setting of the
 * <CODE>registryPort</CODE> property as the port number to run it on.
 *
 * <P>The Registry is started in a separate thread. It can be stopped with
 * the stopRegistry method. If the process that started it terminates, then
 * the Registry thread is terminated too.
 *
 * @param registryType the type of Registry to start.
 *
 * @exception RegistryExistsException if a Registry (or some other process)
 * is already running on the port used by the Registry on this machine.
 * @exception NoRegistryException if an invalid registry type was given.
 */

    public static void
    startRegistry(String registryType)
        throws RegistryExistsException, NoRegistryException {
        int port = Util.getIntProperty("registryPort", registryPort);

        if (RegistryFactory_Debug) {
            Debug("RegistryFactory: startRegistry:" +
                  " registry type: " + registryType);
        }

        startRegistry(registryType, port);
    }


/**
 * <A NAME="SD_STARTREGISTRY"></A>
 * <EM>startRegistry</EM> is a class method that will start a Registry of
 * the appropriate type. The Registry is started in a separate thread.
 * It can be stopped with the stopRegistry method. If the process that
 * started it terminates, then the Registry thread is terminated too.
 *
 * <P>The property <CODE>registryPort</CODE> will be set to the given port
 * number.
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

    public static void
    startRegistry(String registryType, int port)
        throws RegistryExistsException, NoRegistryException {
        if (RegistryFactory_Debug) {
            Debug("RegistryFactory: startRegistry:" +
                  " registry type: " + registryType +
                  " port number: "   + port);
        }

        try {
            startRegistry(registryType, port, null);
        } catch (ManagerExistsException | PermissionDeniedException e) {
        }
    }


/**
 * <A NAME="SD_STARTREGISTRYWITHMANAGER"></A>
 * <EM>startRegistry</EM> is a class method that will start a Registry of
 * the appropriate type, using the current setting of the
 * <CODE>registryPort</CODE> property as the port number to run it on. A
 * registry manager will be associated with this Registry.
 *
 * <P>The Registry is started in a separate thread. It can be stopped with
 * the stopRegistry method. If the process that started it terminates, then
 * the Registry thread is terminated too.
 *
 * @param registryType the type of Registry to start.
 * @param registryManager the registry manager to associate with this registry.
 *
 * @exception RegistryExistsException if a Registry (or some other process)
 * is already running on the port used by the Registry on this machine.
 * @exception NoRegistryException if an invalid registry type was given.
 * @exception ManagerExistsException if a manager already exists for this
 * registry.
 * @exception PermissionDeniedException if this Registry was previously
 * created without a manager attached. You should not be able to add a
 * manager afterwards.
 *
 * @since       JSDT 2.0
 */

    public static void
    startRegistry(String registryType, RegistryManager registryManager)
        throws RegistryExistsException, NoRegistryException,
               ManagerExistsException, PermissionDeniedException {
        int port = Util.getIntProperty("registryPort", registryPort);

        if (RegistryFactory_Debug) {
            Debug("RegistryFactory: startRegistry:" +
                  " registry type: " + registryType +
                  " manager: "       + registryManager);
        }

        startRegistry(registryType, port, registryManager);
    }


/**
 * <A NAME="SD_STOPREGISTRY"></A>
 * <EM>stopRegistry</EM> is a class method that will stop a Registry of
 * the appropriate type, using the current setting of the
 * <CODE>registryPort</CODE> property to determine the port number it is
 * running on.
 *
 * <P>If the registry was started in this VM with the startRegistry method,
 * then it will just stop the Registry thread, else it will send a message
 * to the running Registry, which will cause the Registry to terminate.
 *
 * @param registryType the type of Registry to start, (ie. "socket").
 *
 * @exception NoRegistryException if an invalid registry type was given,
 * or the Registry is not running, or wasn't started by the startRegistry
 * method.
 *
 * @since       JSDT 1.5
 */

    public static void
    stopRegistry(String registryType) throws NoRegistryException {
        int port = Util.getIntProperty("registryPort", registryPort);

        if (RegistryFactory_Debug) {
            Debug("RegistryFactory: stopRegistry:" +
                  " registry type: " + registryType);
        }

        stopRegistry(registryType, port);
    }


/**
 * <A NAME="SD_STOPREGISTRY"></A>
 * <EM>stopRegistry</EM> is a class method that will stop a Registry of
 * the appropriate type. The property <CODE>registryPort</CODE> will be
 * set to the given port number.
 *
 * <P>If the registry was started in this VM with the startRegistry method,
 * then it will just stop the Registry thread, else it will send a message
 * to the running Registry, which will cause the Registry to terminate.
 *
 * @param registryType the type of Registry to start, (ie. "socket").
 * @param port the port number that the Registry is running on.
 *
 * @exception NoRegistryException if an invalid registry type was given,
 * or the Registry is not running, or wasn't started by the startRegistry
 * method.
 *
 * @since       JSDT 1.5
 */

    public static void
    stopRegistry(String registryType, int port) throws NoRegistryException {
        AbstractRegistry registry;

        if (RegistryFactory_Debug) {
            Debug("RegistryFactory: stopRegistry:" +
                  " registry type: " + registryType +
                  " port number: "   + port);
        }

        Connection.setProperty("registryPort", Integer.toString(port));
        registry = getRegistry(registryType, port);
        registry.stopRegistry(registryType, port);
    }


/**
 * <A NAME="SD_REGISTRYEXISTS"></A>
 * <EM>registryExists</EM> is a class method that checks if a Registry,
 * of the given registry type, is already running. It uses the current
 * setting of the <CODE>registryPort</CODE> variable to determine the
 * port number it is running on.
 *
 * @param registryType the type of Registry to check on.
 *
 * @return true if a Registry is already running; false if it isn't.
 *
 * @exception NoRegistryException if an invalid registry type was given.
 */

    public static boolean
    registryExists(String registryType) throws NoRegistryException {
        int port = Util.getIntProperty("registryPort", registryPort);

        if (RegistryFactory_Debug) {
            Debug("RegistryFactory: registryExists:" +
                  " registry type: " + registryType);
        }

        return(registryExists(registryType, port));
    }


/**
 * <A NAME="SD_REGISTRYEXISTS"></A>
 * <EMregistryExists></EM> is a class method that checks if a Registry,
 * of the given registry type, is already running on the given port.
 *
 * <P>The property <CODE>registryPort</CODE> will be set to the given port
 * number.
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

    public static boolean
    registryExists(String registryType, int port) throws NoRegistryException {
        AbstractRegistry registry;
        boolean          exists;

        if (RegistryFactory_Debug) {
            Debug("RegistryFactory: registryExists:" +
                  " registry type: " + registryType +
                  " port number: "   + port);
        }

        Connection.setProperty("registryPort", Integer.toString(port));
        registry = getRegistry(registryType, port);
        exists   = registry.registryExists(registryType, port);
        return(exists);
    }


/**
 * <A NAME="SD_ADDREGISTRYLISTENER"></A>
 * add the specified Registry listener to receive Registry events from the
 * Registry of the specified type running on the given host.
 *
 * @param host the machine the Registry is running on.
 * @param type the type of Registry ("socket", "http" ...).
 * @param listener the Registry listener to add.
 *
 * @exception ConnectionException if a connection error occured.
 * @exception NoRegistryException if there is no Registry running of the
 * given type on the given host.
 * @exception NoSuchHostException if the host doesn't exist.
 * @exception TimedOutException if no reply was received for this operation
 * in the given timeout period.
 *
 * @since       JSDT 2.0
 */

    public static void
    addRegistryListener(String host, String type, RegistryListener listener)
        throws ConnectionException, NoRegistryException,
               NoSuchHostException, TimedOutException {
        if (RegistryFactory_Debug) {
            Debug("RegistryFactory: addRegistryListener:" +
                  " host: "     + host +
                  " type: "     + type +
                  " listener: " + listener);
        }

        Naming.addRegistryListener(host, type, listener);
    }


/**
 * <A NAME="SD_REMOVEREGISTRYLISTENER"></A>
 * removes the specified Registry listener so that it no longer receives
 * Registry events from the Registry of the specified type running on the
 * given host.
 *
 * @param host the machine the Registry is running on.
 * @param type the type of Registry ("socket", "http" ...).
 * @param listener the Registry listener to remove.
 *
 * @exception ConnectionException if a connection error occured.
 * @exception NoRegistryException if there is no Registry running of the
 * given type on the given host.
 * @exception NoSuchHostException if the host doesn't exist.
 * @exception NoSuchListenerException if this SessionListener doesn't exist.
 * @exception TimedOutException if no reply was received for this operation
 * in the given timeout period.
 *
 * @since       JSDT 2.0
 */

    public static void
    removeRegistryListener(String host, String type, RegistryListener listener)
        throws ConnectionException, NoRegistryException,
               NoSuchHostException, NoSuchListenerException,
               TimedOutException {
        if (RegistryFactory_Debug) {
            Debug("RegistryFactory: removeRegistryListener:" +
                  " host: "     + host +
                  " type: "     + type +
                  " listener: " + listener);
        }

        Naming.removeRegistryListener(host, type, listener);
    }


/**
 * <A NAME="SD_LIST"></A>
 * lists all the url strings of the known bound JSDT objects.
 *
 * @exception ConnectionException if a connection error occured.
 * @exception NoRegistryException if no Registry process is running.
 * @exception TimedOutException if no reply was received for this operation
 * in the given timeout period.
 *
 * @return an array of URL strings of the names of all the known bound JSDT
 * objects, or a zero length array if there are no bound objects.
 *
 * @since       JSDT 2.0
 */

    public static URLString[]
    list() throws ConnectionException, NoRegistryException, TimedOutException {
        if (RegistryFactory_Debug) {
            Debug("RegistryFactory: list.");
        }

        return(Naming.list());
    }


/**
 * <A NAME="SD_LISTONHOST"></A>
 * lists all the url strings of the known bound JSDT objects on the given
 * host with the given registry type.
 *
 * @param host host name of the machine to look for bound JSDT objects on.
 * @param registryType the type of Registry ("socket", "http" ...).
 *
 * @exception ConnectionException if a connection error occured.
 * @exception NoSuchHostException if the host doesn't exist.
 * @exception NoRegistryException if no Registry process is running.
 * @exception TimedOutException if no reply was received for this operation
 * in the given timeout period.
 *
 * @return an array of URL strings of the names of all the known bound JSDT
 * objects on the given host, or a zero length array if there are no bound
 * objects.
 *
 * @since       JSDT 2.0
 */

    public static URLString[]
    list(String host, String registryType)
        throws ConnectionException, NoSuchHostException,
               NoRegistryException, TimedOutException {
        if (RegistryFactory_Debug) {
            Debug("RegistryFactory: list:" +
                  " host: " + host +
                  " type: " + registryType);
        }

        return(Naming.list(host, registryType));
    }


/**
 * <A NAME="SD_GETREGISTRY"></A>
 * get (or create a new) instance of the Registry class for this
 * implementation type and port number.
 *
 * @param registryType registry implementation type (eg. "socket").
 * @param port port number that the Registry is running on.
 *
 * @return an instance of the Registry class for this type and port.
 *
 * @exception NoRegistryException if an invalid registry type was given.
 */

    static AbstractRegistry
    getRegistry(String registryType, int port) throws NoRegistryException {
        AbstractRegistry registry;
        String className = "com.sun.media.jsdt." + registryType + ".Registry";

        if (RegistryFactory_Debug) {
            Debug("RegistryFactory: getRegistry:" +
                  " registry type: " + registryType +
                  " port number: "   + port);
        }

        try {
            registry = (AbstractRegistry)
                            Util.getClassForName(className).newInstance();
        } catch (Exception ne) {
            throw new NoRegistryException();
        }

        return(registry);
    }


/**
 * <A NAME="SD_STARTREGISTRYWITHMANAGER"></A>
 * <EM>startRegistry</EM> is a class method that will start a Registry of
 * the appropriate type, on the given port. If the registry manager is not
 * null, it will be associated with this Registry.
 *
 * @param registryType the type of Registry to start.
 * @param port port number that the Registry should run on.
 * @param registryManager the registry manager to associate with this registry,
 * or null if there is no manager.
 *
 * @exception RegistryExistsException if a Registry (or some other process)
 * is already running on the port used by the Registry on this machine.
 * @exception NoRegistryException if an invalid registry type was given.
 * @exception ManagerExistsException if a manager already exists for this
 * registry.
 * @exception PermissionDeniedException if this Registry was previously
 * created without a manager attached. You should not be able to add a
 * manager afterwards.
 */

    static void
    startRegistry(String registryType, int port,
                  RegistryManager registryManager)
        throws RegistryExistsException, NoRegistryException,
               ManagerExistsException, PermissionDeniedException {
        AbstractRegistry registry;
        int              period;

        if (RegistryFactory_Debug) {
            Debug("RegistryFactory: startRegistry:" +
                  " registry type: " + registryType +
                  " port number: "   + port +
                  " manager: "       + registryManager);
        }

        Connection.setProperty("registryPort", Integer.toString(port));
        registry = getRegistry(registryType, port);

        if (registry.registryExists(registryType, port)) {
            throw new RegistryExistsException();
        }

        registry.startRegistry(registryType, port);
        if (registryManager != null) {
            registry.attachManager(registryManager);
        }

        try {
            int elapsed = 0;

            period = Util.getIntProperty("registryTime", registryTime);

            while (!RegistryFactory.registryExists(registryType)) {
                Thread.sleep(1000);
                if (elapsed++ > period) {
                    throw new NoRegistryException();
                }
            }
        } catch (Exception e) {
            throw new NoRegistryException();
        }
    }
}
