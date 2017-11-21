
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

package com.sun.media.jsdt.http;

import com.sun.media.jsdt.impl.JSDTObject;
import java.io.*;

/**
 * The HttpSendOutputStream class is used by the HttpSendSocket class as
 * a layer on the top of the OutputStream it returns so that it can be
 * notified of attempts to write to it.  This allows the HttpSendSocket
 * to know when it should construct a new message.
 *
 * Based on the sun.rmi.transport.proxy.HttpSendOutputStream class.
 *
 * @version     2.3 - 21st November 2017
 * @author      Rich Burridge
 */

class
HttpSendOutputStream extends FilterOutputStream implements httpDebugFlags {

    // The HttpSendSocket object that is providing this stream.
    private final HttpSendSocket owner;


/**
 * Create new filter on a given output stream.
 *
 * @param out the OutputStream to filter from
 * @param owner the HttpSendSocket that is providing this stream
 */

    public
    HttpSendOutputStream(OutputStream out,
                         HttpSendSocket owner) throws IOException {
        super(out);

        if (HttpSendOutputStream_Debug) {
            JSDTObject.Debug("HttpSendOutputStream: constructor:" +
                             " out: "   + out +
                             " owner: " + owner);
        }

        this.owner = owner;
    }


/**
 * Mark this stream as inactive for its owner socket, so the next time
 * a write is attempted, the owner will be notified and a new underlying
 * output stream obtained.
 */

    public void
    deactivate() {
        if (HttpSendOutputStream_Debug) {
            JSDTObject.Debug("HttpSendOutputStream: deactivate.");
        }

        out = null;
    }


/**
 * Write a byte of data to the stream.
 */

    public void
    write(int b) throws IOException {
        if (HttpSendOutputStream_Debug) {
            JSDTObject.Debug("HttpSendOutputStream: write:" +
                             " b: " + b);
        }

        if (out == null) {
            out = owner.writeNotify();
        }
        out.write(b);
    }


/**
 * Write a subarray of bytes.
 *
 * @param b the buffer from which the data is to be written
 * @param off the start offset of the data
 * @param len the number of bytes to be written
 */

    public void
    write(byte b[], int off, int len) throws IOException {
        if (HttpSendOutputStream_Debug) {
            JSDTObject.Debug("HttpSendOutputStream: write:" +
                             " b: "   + b +
                             " off: " + off +
                             " len: " + len);
        }

        if (len == 0) {
            return;
        }
        if (out == null) {
            out = owner.writeNotify();
        }
        out.write(b, off, len);
    }


/**
 * Flush the stream.
 */

    public void
    flush() throws IOException {
        if (HttpSendOutputStream_Debug) {
            JSDTObject.Debug("HttpSendOutputStream: flush.");
        }

        if (out != null) {
            out.flush();
        }
    }


/**
 * Close the stream.
 */

    public void
    close() throws IOException {
        if (HttpSendOutputStream_Debug) {
            JSDTObject.Debug("HttpSendOutputStream: close.");
        }

        flush();
        owner.close();
    }
}
