
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

package examples.stock;

import com.sun.media.jsdt.*;
import com.sun.media.jsdt.event.*;

/**
 * Stock quote listener. Uses yahoo to obtain stock quote information.
 *
 * @version     2.3 - 21st November 2017
 * @author      Amith Yamasani
 * @author      Rich Burridge
 */

public class
StockListener implements ByteArrayListener, StockDebugFlags {

    // Set true if a new value has arrived.
    boolean hasChanged = false;

    // The new shared byte array value.
    byte newValue[] = null;


    public
    StockListener(String name) {
        if (StockListener_Debug) {
            System.err.println("StockListener: constructor:" +
                               " name: " + name);
        }
    }


    public void
    byteArrayJoined(ByteArrayEvent event) {
        if (StockListener_Debug) {
            System.err.println("StockListener: byteArrayJoined:" +
                                " event: " + event);
        }
    }


    public void
    byteArrayLeft(ByteArrayEvent event) {
        if (StockListener_Debug) {
            System.err.println("StockListener: byteArrayLeft:" +
                                " event: " + event);
        }
    }


    public synchronized void
    byteArrayValueChanged(ByteArrayEvent event) {
        if (StockListener_Debug) {
            System.err.println("StockListener: byteArrayValueChanged: " +
                               " event: " + event);
        }

        try {
            newValue = event.getByteArray().getValueAsBytes();
        } catch (NoSuchByteArrayException noe) {
            if (StockListener_Debug) {
                System.err.println("StockListener: byteArrayValueChanged:" +
                                   " exception " + noe);
                noe.printStackTrace();
            }
        }

        hasChanged = true;
    }


    public void
    byteArrayInvited(ByteArrayEvent event) {
        if (StockListener_Debug) {
            System.err.println("StockListener: byteArrayInvited:" +
                                " event: " + event);
        }
    }


    public void
    byteArrayExpelled(ByteArrayEvent event) {
        if (StockListener_Debug) {
            System.err.println("StockListener: byteArrayExpelled:" +
                                " event: " + event);
        }
    }
}
