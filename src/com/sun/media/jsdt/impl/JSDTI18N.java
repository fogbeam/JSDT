
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

import java.util.*;

/**
 * JSDT internationization class.
 *
 * @version     2.3 - 28th October 2017
 * @author      Rich Burridge
 */

public class
JSDTI18N extends JSDTObject implements JSDTDebugFlags {

    private static ResourceBundle bundle = null;

/**
 * <A NAME="SD_GETRESOURCE"></A>
 * gets a localized String resource from the set of Strings associated with
 * the JSDT toolkit.
 *
 * @param key the String key to use to lookup the localized String value.
 *
 * @return the string value for this key.
 */

    public static synchronized String
    getResource(String key) {
        String value         = null;
        Locale currentLocale = Locale.getDefault();

        if (JSDTI18N_Debug) {
            Debug("JSDTI18N: getResource:" +
                  " key: " + key);
        }

        if (bundle == null) {
            try {
                bundle = ResourceBundle.getBundle(
                         "com.sun.media.jsdt.impl.locale.JSDTProps",
                         currentLocale);
            } catch (MissingResourceException e) {
                bundle = new com.sun.media.jsdt.impl.locale.JSDTProps();
            }
        }

        try {
            value = (String) bundle.getObject(key);
        } catch (MissingResourceException e) {
            Error("JSDTI18N: getResource: ", e);
        }

        return(value);
    }
}
