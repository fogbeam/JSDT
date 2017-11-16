
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

import com.sun.media.jsdt.event.ByteArrayListener;
import java.io.StreamCorruptedException;

/**
 * The Byte Array interface. A ByteArray contains an array of bytes that
 * is shared between all Clients that are currently joined to it.
 *
 * @version     2.3 - 25th October 2017
 * @author      Rich Burridge
 * @since        JSDT 1.0
 */

public interface
ByteArray extends Manageable {

/**
 * <A NAME="SD_GETVALUEASBYTES"></A>
 * get the current value for this shared ByteArray, as an array of bytes.
 *
 * @return the current value of this ByteArray object, as an array of bytes.
 *
 * @exception NoSuchByteArrayException if this shared ByteArray doesn't exist.
 *
 * @since       JSDT 1.5
 */

    byte[]
    getValueAsBytes() throws NoSuchByteArrayException;


/**
 * <A NAME="SD_GETVALUEASOBJECT"></A>
 * get the current value for this shared ByteArray, as a Java object.
 *
 * @return the current value of this ByteArray object, as a Java object.
 *
 * @exception ClassNotFoundException if the class for this object cannot
 * be found.
 * @exception NoSuchByteArrayException if this shared ByteArray doesn't exist.
 * @exception StreamCorruptedException if this ByteArray object does not
 * contain a serialized object.
 *
 * @since       JSDT 1.5
 */

    Object
    getValueAsObject()
        throws ClassNotFoundException, NoSuchByteArrayException,
               StreamCorruptedException;


/**
 * <A NAME="SD_GETVALUEASSTRING"></A>
 * get the current value for this shared ByteArray, as a String object.
 *
 * @return the current value of this ByteArray object, as a String object.
 *
 * @exception NoSuchByteArrayException if this shared ByteArray doesn't exist.
 *
 * @since       JSDT 1.5
 */

    String
    getValueAsString() throws NoSuchByteArrayException;


/**
 * <A NAME="SD_SETVALUE"></A>
 * sets a new value for this shared ByteArray using the given byte array value.
 * The new value is sent to all other instances of this shared ByteArray. All
 * ByteArray listeners will have their <CODE>byteArrayValueChanged</CODE>
 * method invoked.
 *
 * @param client the Client wishing to set the value of this ByteArray.
 * @param value the new byte array value for this shared ByteArray.
 *
 * @exception ConnectionException if a connection error occured.
 * @exception InvalidClientException if the Client is invalid is some way (ie.
 * its getName() method returns null).
 * @exception NoSuchByteArrayException if this shared ByteArray doesn't exist.
 * @exception NoSuchClientException if this Client doesn't exist.
 * @exception NoSuchSessionException if this Session doesn't exist.
 * @exception PermissionDeniedException if this Client does not have permission
 * to change the value of this ByteArray.
 * @exception TimedOutException if no reply was received for this operation
 * in the given timeout period.
 */

    void
    setValue(Client client, byte[] value)
        throws ConnectionException, InvalidClientException,
               NoSuchByteArrayException, NoSuchClientException,
               NoSuchSessionException, PermissionDeniedException,
               TimedOutException;


/**
 * <A NAME="SD_SETVALUEOFFLEN"></A>
 * sets a new value for this shared ByteArray using a subset of the given
 * ByteArray. The new value is sent to all other instances of this shared
 * ByteArray. All ByteArray listeners will have their
 * <CODE>byteArrayValueChanged</CODE> method invoked.
 *
 * @param client the Client wishing to set the value of this ByteArray.
 * @param value the byte array that is the source of the new shared
 * ByteArray value.
 * @param offset the initial offset within the byte array.
 * @param length the number of bytes to use.
 *
 * @exception ConnectionException if a connection error occured.
 * @exception InvalidClientException if the Client is invalid is some way (ie.
 * its getName() method returns null).
 * @exception NoSuchByteArrayException if this shared ByteArray doesn't exist.
 * @exception NoSuchClientException if this Client doesn't exist.
 * @exception NoSuchSessionException if this Session doesn't exist.
 * @exception PermissionDeniedException if this Client does not have
 * permission to perform this operation.
 * @exception TimedOutException if no reply was received for this operation
 * in the given timeout period.
 */

    void
    setValue(Client client, byte[] value, int offset, int length)
        throws ConnectionException, InvalidClientException,
               NoSuchByteArrayException, NoSuchClientException,
               NoSuchSessionException, PermissionDeniedException,
               TimedOutException;


/**
 * <A NAME="SD_SETVALUEFROMSTRING"></A>
 * sets a new value for this shared ByteArray using the given String.
 * The new value is sent to all other instances of this shared ByteArray.
 * All ByteArray listeners will have their <CODE>byteArrayValueChanged</CODE>
 * method invoked.
 *
 * @param client the Client wishing to set the value of this ByteArray.
 * @param string the String value from which an array of bytes is set.
 *
 * @exception ConnectionException if a connection error occured.
 * @exception InvalidClientException if the Client is invalid is some way (ie.
 * its getName() method returns null).
 * @exception NoSuchByteArrayException if this shared ByteArray doesn't exist.
 * @exception NoSuchClientException if this Client doesn't exist.
 * @exception NoSuchSessionException if this Session doesn't exist.
 * @exception PermissionDeniedException if this Client does not have permission
 * to change the value of this ByteArray.
 * @exception TimedOutException if no reply was received for this operation
 * in the given timeout period.
 *
 * @since       JSDT 1.5
 */

    void
    setValue(Client client, String string)
        throws ConnectionException, InvalidClientException,
               NoSuchByteArrayException, NoSuchClientException,
               NoSuchSessionException, PermissionDeniedException,
               TimedOutException;


/**
 * <A NAME="SD_SETVALUEFROMOBJECT"></A>
 * sets a new value for this shared ByteArray using the given Java object.
 * The new value is sent to all other instances of this shared ByteArray.
 * All ByteArray listeners will have their <CODE>byteArrayValueChanged</CODE>
 * method invoked.
 *
 * @param client the Client wishing to set the value of this ByteArray.
 * @param object the Java object value which is serialized into an
 * array of bytes.
 *
 * @exception ConnectionException if a connection error occured.
 * @exception InvalidClientException if the Client is invalid is some way (ie.
 * its getName() method returns null).
 * @exception NoSuchByteArrayException if this shared ByteArray doesn't exist.
 * @exception NoSuchClientException if this Client doesn't exist.
 * @exception NoSuchSessionException if this Session doesn't exist.
 * @exception PermissionDeniedException if this Client does not have permission
 * to change the value of this ByteArray.
 * @exception TimedOutException if no reply was received for this operation
 * in the given timeout period.
 *
 * @since       JSDT 1.5
 */

    void
    setValue(Client client, Object object)
        throws ConnectionException, InvalidClientException,
               NoSuchByteArrayException, NoSuchClientException,
               NoSuchSessionException, PermissionDeniedException,
               TimedOutException;


/**
 * <A NAME="SD_ADDBYTEARRAYLISTENER"></A>
 * add the specified ByteArray listener to receive ByteArray events for
 * this ByteArray.
 *
 * @param listener the ByteArray listener.
 *
 * @exception ConnectionException if a connection error occured.
 * @exception NoSuchByteArrayException if this shared ByteArray doesn't exist.
 * @exception NoSuchSessionException if this Session doesn't exist.
 * @exception TimedOutException if no reply was received for this operation
 * in the given timeout period.
 */

    void
    addByteArrayListener(ByteArrayListener listener)
        throws ConnectionException, NoSuchByteArrayException,
               NoSuchSessionException, TimedOutException;


/**
 * <A NAME="SD_REMOVEBYTEARRAYLISTENER"></A>
 * removes the specified ByteArray listener so that it no longer receives
 * ByteArray events for this ByteArray.
 *
 * @param listener the ByteArray listener.
 *
 * @exception ConnectionException if a connection error occured.
 * @exception NoSuchByteArrayException if this shared ByteArray doesn't exist.
 * @exception NoSuchListenerException if this ByteArrayListener doesn't exist.
 * @exception NoSuchSessionException if this Session doesn't exist.
 * @exception TimedOutException if no reply was received for this operation
 * in the given timeout period.
 */

    void
    removeByteArrayListener(ByteArrayListener listener)
        throws ConnectionException, NoSuchByteArrayException,
               NoSuchListenerException, NoSuchSessionException,
               TimedOutException;
}
