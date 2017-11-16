
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
import java.io.*;
import java.net.*;
import java.util.Vector;

/**
 * Various JSDT utility methods.
 *
 * @version     2.3 - 6th November 2017
 * @author      Rich Burridge
 */

public class
Util extends JSDTObject implements JSDTDebugFlags, Serializable {

/**
 * <A NAME="SD_ADJUSTURLString"></A>
 * <EM>adjustURLString</EM> replaces the host name portion of the given JSDT
 * URL with the supplied IP address, generating a new JSDT URL which is
 * returned. Note that the host name part of the JSDT URL may already be an
 * IP address before this replacement is done.
 *
 * @param oldUrl the JSDT URL sent to the Registry.
 * @param IPString the IP address of the sending app.
 *
 * @return a JSDT URL String with host name to IP address adjustment.
 */

    public static String
    adjustURLString(String oldUrl, String IPString) {
        URLString   u      = new URLString(oldUrl);
        String      newUrl;

        if (Util_Debug) {
            Debug("Util: adjustURLString:" +
                  " url: "       + oldUrl +
                  " IP string: " + IPString);
        }

        newUrl = "jsdt://" + IPString + ":" + u.getPort() + "/" +
                 u.getConnectionType() + "/" + u.getObjectType() + "/" +
                 u.getObjectName();

        return(newUrl);
    }


/**
 * <A NAME="SD_DEFRAGMENTBUFFER"></A>
 * <EM>DefragmentBuffer</EM> concatenates smaller fragments into entire
 * buffers.
 *
 * Code kindly supplied by Bela Ban <bba@CS.Cornell.EDU>
 *
 * @param fragments a vector of byte buffers (<code>byte[]</code>)
 *
 * @return a concatenated byte buffer
 */

    public static byte[]
    DefragmentBuffer(Vector fragments) {
        byte[]                tmp;
        ByteArrayOutputStream out    = new ByteArrayOutputStream();
        int                   length = fragments.size();

        for (int i = 0; i < length; i++) {
            tmp = (byte[]) fragments.firstElement();
            out.write(tmp, 0, tmp.length);
            fragments.removeElement(tmp);
        }

        return(out.toByteArray());
    }


/**
 * <A NAME="SD_FRAGMENTBUFFER"></A>
 * <EM>FragmentBuffer</EM> fragments a byte buffer into smaller fragments
 * of (max.) frag_size.
 *
 * Example: a byte buffer of 1024 bytes and a frag_size of 248 gives 4
 *          fragments of 248 bytes each and 1 fragment of 32 bytes.
 *
 * Code kindly supplied by Bela Ban <bba@CS.Cornell.EDU>
 *
 * @param buf the byte buffer to fragment.
 * @param fragSize the maximum size for a fragment.
 *
 * @return A vector of byte buffers (<code>byte[]</code>).
 */

    public static Vector
    FragmentBuffer(byte[] buf, int fragSize) {
        byte[]               fragment;
        int                  tmpSize;
        Vector<byte[]>       retval          = new Vector<>();
        long                 totalSize       = buf.length;
        long                 accumulatedSize = 0;
        ByteArrayInputStream in              = new ByteArrayInputStream(buf);

        if (Util_Debug) {
            Debug("Util: FragmentBuffer:" +
                  " buf: "       + buf +
                  " frag size: " + fragSize);
        }

        while (accumulatedSize < totalSize) {
            if (accumulatedSize + fragSize <= totalSize) {
                tmpSize = fragSize;
            } else {
                tmpSize = (int)(totalSize - accumulatedSize);
            }

            if (Util_Debug) {
                Debug("Util: FragmentBuffer:" +
                      " tmp size: " + tmpSize);
            }

            fragment = new byte[tmpSize];

            in.read(fragment, 0, tmpSize);    // bytes read

            retval.addElement(fragment);
            accumulatedSize += tmpSize;
        }

        return(retval);
    }


/**
 * <A NAME="SD_GETCLASSFORNAME"></A>
 * <EM>getClassForName</EM> get the <CODE>Class</CODE> object associated with
 * the class with the given string name.
 *
 * @param className the fully qualified name of the desired class.
 *
 * @return the <CODE>Class</CODE> descriptor for the class with the specified
 * name.
 *
 * @exception ClassNotFoundException if the class could not be found.
 */

    public static Class
    getClassForName(String className) throws ClassNotFoundException {
        if (Util_Debug) {
            Debug("Util: getClassForName:" +
                  " class name: " + className);
        }

        return(Class.forName(className));
    }


/**
 * <A NAME="SD_GETCLIENTNAME"></A>
 * <EM>getClientName</EM> get the client's name, checking that it's valid.
 *
 * @return the client's name.
 *
 * @exception InvalidClientException if the Client is invalid is some way (ie.
 * its getName() method returns null).
 */

    public static String
    getClientName(Client client) throws InvalidClientException {
        String name = null;

        if (Util_Debug) {
            Debug("Util: getClientName:" +
                  " client: " + client);
        }

        try {
            name = client.getName();
        } catch (Throwable th) {
            Error("Util: getClientName: ",
                  "impl.thrown", th + " by client.");
        }

        if (name == null) {
            throw new InvalidClientException();
        }

        return(name);
    }


/**
 * <A NAME="SD_GETIPADDRESS"></A>
 * <EM>getIPAddress</EM> returns the IP address of the given hostname. A
 * check is also made to see if the hostname is "localhost" or "127.0.0.1".
 * If so, then this is replaced with the IP address of the local host.
 *
 * @param hostName the host name to convert.
 *
 * @exception InvalidURLException if the url string given is invalid.
 *
 * @return the IP address of the given host name.
 */

    public static String
    getIPAddress(String hostName) throws InvalidURLException {
        InetAddress address;

        if (Util_Debug) {
            Debug("Util: getIPAddress:" +
                  " host name: " + hostName);
        }

        try {
            if (hostName.equals("localhost") ||
                hostName.equals("127.0.0.1")) {
                address  = InetAddress.getLocalHost();
            } else {
                address = InetAddress.getByName(hostName);
            }
        } catch (UnknownHostException uhe) {
            throw new InvalidURLException();
        }

        return(address.getHostAddress());
    }


/**
 * <A NAME="SD_ISCLIENT"></A>
 * <EM>isClient</EM> returns an indication of whether the given JSDT URL is
 * a valid Client URL.
 *
 * @param url the JSDT URL to test.
 *
 * @return true if this is a valid Client URL, false if not.
 */

    public static boolean
    isClient(URLString url) {
        char c = url.getObjectType().charAt(0);

        if (Util_Debug) {
            Debug("Util: isClient:" +
                  " url: " + url);
        }

        return(url.isValid() && (c == 'c' || c == 'C'));
    }


/**
 * <A NAME="SD_ISSESSION"></A>
 * <EM>isSession</EM> returns an indication of whether the given JSDT URL is
 * a valid Session URL.
 *
 * @param url the JSDT URL to test.
 *
 * @return true if this is a valid Session URL, false if not.
 */

    public static boolean
    isSession(URLString url) {
        char c = url.getObjectType().charAt(0);

        if (Util_Debug) {
            Debug("Util: isSession:" +
                  " url: " + url);
        }

        return(url.isValid() && (c == 's' || c == 'S'));
    }


/**
 * <A NAME="SD_SORT"></A>
 * <EM>sort</EM> sort the array of names using a simple bubble sort.
 *
 * @param names the array of Strings to be sorted.
 *
 * Algorithm taken from "Algorithms in C++" by Robert Sedgewick, p101.
 */

    public static void
    sort(String[] names) {
        int i, j, n;
        String temp;

        if (Util_Debug) {
            Debug("Util: sort:");
            for (int l = 0; l < names.length ; l++) {
                System.err.println("names[" + l + "]: " + names[l]);
            }
        }

        if (names == null || names.length == 0) {
            return;
        }

        n = names.length-1;
        for (i = n; i >= 0; i--) {
            for (j = 1; j <= i; j++) {
                if (names[j-1].compareTo(names[j]) > 0) {
                    temp       = names[j-1];
                    names[j-1] = names[j];
                    names[j]   = temp;
                }
            }
        }
    }


/**
 * <A NAME="SD_STARTTHREAD"></A>
 * <EM>startThread</EM> creates and start a thread with the given name and
 * runnable object.
 *
 * @param target the object whose <code>run</code> method is called.
 * @param name the name of the new thread.
 * @param isDaemon an indication of whether this should be a daemon thread.
 *
 * @return the newly created Thread.
 */

    public static Thread
    startThread(Runnable target, String name, boolean isDaemon) {
        Thread thread = new TargetableThread(target, name);

        if (Util_Debug) {
            Debug("Util: startThread:" +
                  " target: "  + target +
                  " name: "    + name +
                  " daemon?: " + isDaemon);
        }

        thread.setDaemon(isDaemon);
        thread.start();
        return(thread);
    }

/**
 * <A NAME="SD_GETINTPROPERTY"></A>
 * <EM>getIntProperty</EM> returns the requested integer property, or the
 * default value if the property is not set.
 *
 * @param key the key that the property is stored under.
 * @param defval the default value for this property.
 *
 * @return the requested integer property.
 */

    public static int
    getIntProperty(String key, int defval) {
        String value;
        int    retval;

        if (Util_Debug) {
            Debug("Util: getIntProperty:" +
                  " key: "     + key +
                  " default: " + defval);
        }

        if ((value = Connection.getProperty(key)) != null) {
            retval = Integer.valueOf(value);
        } else {
            retval = defval;
        }

        return(retval);
    }


/**
 * <A NAME="SD_GETLONGPROPERTY"></A>
 * <EM>getLongProperty</EM> returns the requested long property, or the
 * default value if the property is not set.
 *
 * @param key the key that the property is stored under.
 * @param defval the default value for this property.
 *
 * @return the requested long property.
 */

    public static long
    getLongProperty(String key, long defval) {
        String value;
        long   retval;

        if (Util_Debug) {
            Debug("Util: getLongProperty:" +
                  " key: "     + key +
                  " default: " + defval);
        }

        if ((value = Connection.getProperty(key)) != null) {
            retval = Long.valueOf(value);
        } else {
            retval = defval;
        }

        return(retval);
    }


/**
 * <A NAME="SD_GETSTRINGPROPERTY"></A>
 * <EM>getStringProperty</EM> returns the requested String property, or the
 * default value if the property is not set.
 *
 * @param key the key that the property is stored under.
 * @param defval the default value for this property.
 *
 * @return the requested String property.
 */

    public static String
    getStringProperty(String key, String defval) {
        String retval;

        if (Util_Debug) {
            Debug("Util: getStringProperty:" +
                  " key: "     + key +
                  " default: " + defval);
        }

        if ((retval = Connection.getProperty(key)) == null) {
            retval = defval;
        }

        return(retval);
    }


/**
 * <A NAME="SD_GETBOOLEANPROPERTY"></A>
 * <EM>getBooleanProperty</EM> returns the requested boolean property, or the
 * default value if the property is not set.
 *
 * @param key the key that the property is stored under.
 * @param defval the default value for this property.
 *
 * @return the requested boolean property.
 */

    public static boolean
    getBooleanProperty(String key, boolean defval) {
        String  value;
        boolean retval;

        if ((value = Connection.getProperty(key)) != null) {
            retval = Boolean.valueOf(value);
        } else {
            retval = defval;
        }

        return(retval);
    }
}
