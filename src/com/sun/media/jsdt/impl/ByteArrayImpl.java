
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
import com.sun.media.jsdt.event.ByteArrayListener;
import com.sun.media.jsdt.event.ByteArrayEvent;
import java.io.*;

/**
 * JSDT Byte Array (implementation) class.
 *
 * @version     2.3 - 6th November 2017
 * @author      Rich Burridge
 */

public class
ByteArrayImpl extends ManageableImpl
              implements ByteArray, ByteArrayListener {

    // To signify a manageable object of type ByteArray.
    public static final char M_ByteArray = '\u00F6';

    /** The value of this shared byte array.
     *
     *  @serial
     */
    private byte[] value;

    /** Handle to client-side proxy.
     *
     *  @serial
     */
    public AbstractByteArrayProxy po;

    /** Handle to server-side object.
     *
     *  @serial
     */
    public AbstractByteArrayServer so;


/**
 * <A NAME="SD_BYTEARRAYIMPL"></A>
 * <EM>ByteArrayImpl</EM> the constructor for the ByteArray (implementation)
 * class.
 *
 * @param isServer set if are we creating object on the server-side.
 * @param name the name of the byte array being constructed.
 * @param session the session this byte array belongs to.
 * @param value the initial value to associate with this ByteArray.
 */

    public
    ByteArrayImpl(boolean isServer, String name, SessionImpl session,
                  byte[] value) {
        String className;

        if (ByteArrayImpl_Debug) {
            debug("ByteArrayImpl: constructor:" +
                  " server? "  + isServer +
                  " name: "    + name +
                  " session: " + session +
                  " value: "   + value);
        }

        this.name  = name;
        this.value = value;

        try {
            if (!isServer) {
                className = "com.sun.media.jsdt." + session.getConnectionType() +
                            ".ByteArrayProxy";
                po = (AbstractByteArrayProxy)
                                Util.getClassForName(className).newInstance();
                po.initProxy(name, session, this);
            } else {
                className = "com.sun.media.jsdt." + session.getConnectionType() +
                            ".ByteArrayServer";
                so = (AbstractByteArrayServer)
                                Util.getClassForName(className).newInstance();
                so.initServer(name, session, this);
            }
        } catch (Exception e) {
            error("ByteArrayImpl: constructor: ", e);
        }

        objectType = M_ByteArray;
        mpo = po;
    }


/**
 * <A NAME="SD_GETVALUE"></A>
 * get the local value for this shared ByteArray.
 *
 * @deprecated There are now three different methods that can be used to
 * return the current value contained in a ByteArray object. The naming of
 * this method is inconsistent with those three methods. As of JSDT&nbsp;1.5,
 * the preferred way to do this is via the <CODE>getValueAsBytes()</CODE>
 * method.
 *
 * @return the local value for this shared byte array.
 */

    public byte[]
    getValue() throws NoSuchByteArrayException {
        if (ByteArrayImpl_Debug) {
            debug("ByteArrayImpl: getValue.");
        }

        return(value);
    }


/**
 * <A NAME="SD_GETVALUEASBYTES"></A>
 * get the current value for this shared ByteArray, as an array of bytes.
 *
 * @return the current value of this ByteArray object, as an array of bytes.
 *
 * @exception NoSuchByteArrayException if this shared ByteArray doesn't exist.
 */

    public byte[]
    getValueAsBytes() throws NoSuchByteArrayException {
        if (ByteArrayImpl_Debug) {
            debug("ByteArrayImpl: getValueAsBytes.");
        }

        return(value);
    }


/**
 * <A NAME="SD_GETVALUEASOBJECT"></A>
 * get the current value for this shared ByteArray, as a Java object.
 *
 * @return the current value of this ByteArray object, as a Java object.
 *
 * @exception ClassNotFoundException if the class for this object cannot
 * be found.
 * @exception NoSuchByteArrayException if this shared ByteArray doesn't exist.
 * @exception StreamCorruptedException if this Data object does not contain
 * a serialized object.
 */

    public Object
    getValueAsObject()
                throws ClassNotFoundException, NoSuchByteArrayException,
                       StreamCorruptedException {
        ByteArrayInputStream bis    = new ByteArrayInputStream(value);
        ObjectInputStream    ois;
        Object               object = null;

        if (ByteArrayImpl_Debug) {
            debug("ByteArrayImpl: getValueAsObject.");
        }

        try {
            ois    = new ObjectInputStream(bis);
            object = ois.readObject();
            ois.close();
            bis.close();
        } catch (IOException ioe) {
            error("ByteArrayImpl: getValueAsObject: ", ioe);
        }

        return(object);
    }


/**
 * <A NAME="SD_GETVALUEASSTRING"></A>
 * get the current value for this shared ByteArray, as a String object.
 *
 * @return the current value of this ByteArray object, as a String object.
 *
 * @exception NoSuchByteArrayException if this shared ByteArray doesn't exist.
 */

    public String
    getValueAsString() throws NoSuchByteArrayException {
        if (ByteArrayImpl_Debug) {
            debug("ByteArrayImpl: getValueAsString.");
        }

        return(new String(value));
    }


/**
 * <A NAME="SD_SETLOCALVALUE"></A>
 * set the local value for this shared ByteArray.
 *
 * @param value the new local value for this shared ByteArray.
 */

    public void
    setLocalValue(byte[] value) {
        if (ByteArrayImpl_Debug) {
            debug("ByteArrayImpl: setLocalValue:" +
                  " value: " + value);
        }

        this.value = value;
    }


/**
 * <A NAME="SD_SETVALUE"></A>
 * sets a new value for this shared ByteArray. The new value is sent to all
 * other instances of this shared ByteArray. All ByteArray listeners will
 * have their <CODE>byteArrayValueChanged</CODE> method invoked.
 *
 * @param client the Client wishing to set the value of this ByteArray.
 * @param value the new value for this shared ByteArray.
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

    public void
    setValue(Client client, byte[] value)
                throws ConnectionException, InvalidClientException,
                       NoSuchByteArrayException, NoSuchClientException,
                       NoSuchSessionException, PermissionDeniedException,
                       TimedOutException {
        if (ByteArrayImpl_Debug) {
            debug("ByteArrayImpl: setValue:" +
                  " client: " + client +
                  " value: "  + value);
        }

        po.setValue(client, value, 0, value.length);
    }


/**
 * <A NAME="SD_SETVALUEOFFLEN"></A>
 * sets a new value for this shared ByteArray using a subset of the given
 * ByteArray. The new value is sent to all other instances of this shared
 * ByteArray. All ByteArray listeners will have their
 * <CODE>byteArrayValueChanged</CODE> method invoked.
 *
 * @param client the Client wishing to set the value of this ByteArray.
 * @param value the byte array that is the source of the new shared ByteArray
 * value.
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

    public void
    setValue(Client client, byte[] value, int offset, int length)
                throws ConnectionException, InvalidClientException,
                       NoSuchByteArrayException, NoSuchClientException,
                       NoSuchSessionException, PermissionDeniedException,
                       TimedOutException {
        if (ByteArrayImpl_Debug) {
            debug("ByteArrayImpl: setValue:" +
                  " client: " + client +
                  " value: "  + value +
                  " offset: " + offset +
                  " length: " + length);
        }

        po.setValue(client, value, offset, length);
    }


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
 */

    public void
    setValue(Client client, String string)
                throws ConnectionException, InvalidClientException,
                       NoSuchByteArrayException, NoSuchClientException,
                       NoSuchSessionException, PermissionDeniedException,
                       TimedOutException {
        byte[] value;

        if (ByteArrayImpl_Debug) {
            debug("ByteArrayImpl: setValue:" +
                  " client: " + client +
                  " string: " + string);
        }

        value = string.getBytes();
        po.setValue(client, value, 0, value.length);
    }


/**
 * <A NAME="SD_SETVALUEFROMOBJECT"></A>
 * sets a new value for this shared ByteArray using the given Java object.
 * The new value is sent to all other instances of this shared ByteArray.
 * All ByteArray listeners will have their <CODE>byteArrayValueChanged</CODE>
 * method invoked.
 *
 * @param client the Client wishing to set the value of this ByteArray.
 * @param object the Java object value which is serialized into an array of
 * bytes.
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

    public void
    setValue(Client client, Object object)
                throws ConnectionException, InvalidClientException,
                       NoSuchByteArrayException, NoSuchClientException,
                       NoSuchSessionException, PermissionDeniedException,
                       TimedOutException {
        ObjectOutputStream    oos;
        int                   length;
        ByteArrayOutputStream bos   = new ByteArrayOutputStream();
        byte[]                value = null;

        if (ByteArrayImpl_Debug) {
            debug("ByteArrayImpl: setValue:" +
                  " client: " + client +
                  " object: " + object);
        }

        try {
            oos = new ObjectOutputStream(bos);
            oos.writeObject(object);
            length = bos.size();
            value  = new byte[length];
            value  = bos.toByteArray();
        } catch (IOException ioe) {
            error("ByteArrayImpl: setValue: ", ioe);
        }

        po.setValue(client, value, 0, value.length);
    }


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

    public void
    addByteArrayListener(ByteArrayListener listener)
                throws ConnectionException, NoSuchByteArrayException,
                       NoSuchSessionException, TimedOutException {
        if (ByteArrayImpl_Debug) {
            debug("ByteArrayImpl: addByteArrayListener:" +
                  " listener: " + listener);
        }

        try {
            mpo.addListener(listener, objectType);
        } catch (NoSuchByteArrayException | ConnectionException |
                 NoSuchSessionException | TimedOutException nse) {
            throw nse;
        } catch (JSDTException e) {
            error("ByteArrayImpl: addByteArrayListener: ", e);
        }
    }


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

    public void
    removeByteArrayListener(ByteArrayListener listener)
                throws ConnectionException, NoSuchByteArrayException,
                       NoSuchListenerException, NoSuchSessionException,
                       TimedOutException {
        if (ByteArrayImpl_Debug) {
            debug("ByteArrayImpl: removeByteArrayListener:" +
                  " listener: " + listener);
        }

        try {
            mpo.removeListener(listener, objectType);
        } catch (NoSuchByteArrayException | NoSuchListenerException |
                 NoSuchSessionException | ConnectionException |
                 TimedOutException nse) {
            throw nse;
        } catch (JSDTException e) {
            error("ByteArrayImpl: removeByteArrayListener: ", e);
        }
    }


/**
 * <A NAME="SD_BYTEARRAYJOINED"></A>
 * invoked when a Client has joined a ByteArray.
 *
 * @param event the ByteArray event containing more information.
 */

    public void
    byteArrayJoined(ByteArrayEvent event) { }


/**
 * <A NAME="SD_BYTEARRAYLEFT"></A>
 * invoked when a Client has left a ByteArray.
 *
 * @param event the ByteArray event containing more information.
 */

    public void
    byteArrayLeft(ByteArrayEvent event) { }


/**
 * <A NAME="SD_BYTEARRAYVALUECHANGED"></A>
 * invoked when the value of a ByteArray has changed.
 *
 * @param event the ByteArray event containing more information.
 */

    public void
    byteArrayValueChanged(ByteArrayEvent event) { }


/**
 * <A NAME="SD_BYTEARRAYINVITED"></A>
 * invoked when a Client has been invited to join a ByteArray.
 *
 * @param event the ByteArray event containing more information.
 */

    public void
    byteArrayInvited(ByteArrayEvent event) { }


/**
 * <A NAME="SD_BYTEARRAYEXPELLED"></A>
 * invoked when a Client has been expelled from a ByteArray.
 *
 * @param event the ByteArray event containing more information.
 */

    public void
    byteArrayExpelled(ByteArrayEvent event) { }
}
