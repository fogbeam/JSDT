
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

package com.sun.media.jsdt.event;

/**
 * The adaptor which receives ByteArray events.
 * The methods in this class are empty; this class is provided as a
 * convenience for easily creating listeners by extending this class
 * and overriding only the methods of interest.
 *
 * @version     2.3 - 27th October 2017
 * @author      Rich Burridge
 * @since       JSDT 1.0
 */

public abstract class
ByteArrayAdaptor implements ByteArrayListener {

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
