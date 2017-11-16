
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

import java.lang.reflect.*;
import java.util.*;

/**
 * JSDT security class.
 *
 * @version     2.3 - 30th October 2017
 * @author      Rich Burridge
 */

public class
JSDTSecurity extends JSDTObject implements JSDTDebugFlags {

    public static Method   enablePrivilege  = null;

    public static Object   privilegeManager = null;

    public static Object[] connectArgs      = null;

    public static Object[] multicastArgs    = null;


    static {
        ClassLoader cl;
        String      secManName;
        Class<?>    secMan;

        if (JSDTSecurity_Debug) {
            Debug("JSDTSecurity: static block.");
        }

        try {
            secManName = "com.sun.media.jsdt.impl.JSDTSecurity";
            secMan     = Util.getClassForName(secManName);
            cl         = secMan.getClassLoader();

            if (cl == null) {               // The class was loaded locally.
                Enumeration e, k;
                Hashtable<String, Object> managers = new Hashtable<>();

                managers.put("netscape.security.PrivilegeManager",
                             new NNSecurity());
                managers.put("com.ms.security.PolicyEngine",
                             new IESecurity());

                for (e = managers.elements(), k = managers.keys();
                     e.hasMoreElements();) {
                    try {
                        String          name = (String) k.nextElement();
                        BrowserSecurity bs = (BrowserSecurity) e.nextElement();

                        if (JSDTSecurity_Debug) {
                            Debug("JSDTSecurity: static block:" +
                                  " Trying security manager: " + name);
                        }
                        if ((secMan = Util.getClassForName(name)) != null) {
                            if (JSDTSecurity_Debug) {
                                Debug("JSDTSecurity: static block:" +
                                      " lookup succeeded.");
                            }

                            bs.setupArgs(secMan);
                            break;
                        }
                    } catch (ClassNotFoundException cnfe) {
                        if (JSDTSecurity_Debug) {
                            Debug("JSDTSecurity: static block:" +
                                  " lookup failed.");
                        }
                    }
                }

/* Not one of the security managers on our list. */

                try {
                    secManName       = "java.security.AccessController";
                    secMan           = Util.getClassForName(secManName);

                    privilegeManager = secMan;
                    enablePrivilege  = secMan.getMethod("beginPrivileged",
                                                        (Class<?>) null);
                } catch (Exception e1) {  // JDK 1.1 or other system without AC.
                }
            }
        } catch (Exception e) {
            Error("JSDTSecurity: static block: ", e);
        }
    }
}


interface
BrowserSecurity {

    void
    setupArgs(Class<?> secMan)
                throws ClassNotFoundException, IllegalAccessException,
                       InstantiationException, NoSuchMethodException;

}


class
NNSecurity extends JSDTObject
           implements BrowserSecurity, JSDTDebugFlags {  // Netscape Navigator.

    public void
    setupArgs(Class<?> secMan)
                throws ClassNotFoundException, IllegalAccessException,
                       InstantiationException, NoSuchMethodException {
        Class<?>[] args = new Class[1];

        if (JSDTSecurity_Debug) {
            debug("NNSecurity: setupArgs:" +
                  " security manager: " + secMan);
        }

        JSDTSecurity.privilegeManager = secMan;
        args[0]                       = java.lang.String.class;
        JSDTSecurity.enablePrivilege  = secMan.getMethod("enablePrivilege",
                                                         args);

        JSDTSecurity.connectArgs      = new Object[1];
        JSDTSecurity.connectArgs[0]   = "UniversalConnect";

        JSDTSecurity.multicastArgs    = new Object[1];
        JSDTSecurity.multicastArgs[0] = "UniversalMulticast";
    }
}


class
IESecurity extends JSDTObject
           implements BrowserSecurity, JSDTDebugFlags {   // Internet Explorer.

    public void
    setupArgs(Class<?> secMan)
                throws ClassNotFoundException, IllegalAccessException,
                       InstantiationException, NoSuchMethodException {
        Class<?>[] args      = new Class[1];
        String  pIdName   = "com.ms.security.PermissionID";
        String  netIOName = "com.ms.security.permissions.NetIOPermission";
        Class   pId       = Util.getClassForName(pIdName);
        Class   netIO     = Util.getClassForName(netIOName);

        if (JSDTSecurity_Debug) {
            debug("IESecurity: setupArgs:" +
                  " security manager: " + secMan);
        }

        JSDTSecurity.privilegeManager = secMan;
        args[0]          = pId;
        JSDTSecurity.enablePrivilege  = secMan.getMethod("assertPermission",
                                                         args);

        JSDTSecurity.connectArgs      = new Object[1];
        JSDTSecurity.connectArgs[0]   = netIO.newInstance();

        JSDTSecurity.multicastArgs    = new Object[1];
        JSDTSecurity.multicastArgs[0] = netIO.newInstance();
    }
}
