
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
 * The HttpSendInputStream class is used by the HttpSendSocket class as
 * a layer on the top of the InputStream it returns so that it can be
 * notified of attempts to read from it.  This allows the HttpSendSocket
 * to know when it should push across its output message.
 *
 * Based on the sun.rmi.transport.proxy.HttpSendInputStream class.
 *
 * @version     2.3 - 21st November 2017
 * @author      Rich Burridge
 */

class
HttpSendInputStream extends FilterInputStream implements httpDebugFlags {

    // The HttpSendSocket object that is providing this stream.
    private final HttpSendSocket owner;


/**
 * Create new filter on a given input stream.
 *
 * @param in the InputStream to filter from
 * @param owner the HttpSendSocket that is providing this stream
 */

    public
    HttpSendInputStream(InputStream in,
                        HttpSendSocket owner) throws IOException {
        super(in);

        if (HttpSendInputStream_Debug) {
            JSDTObject.Debug("HttpSendInputStream: constructor:" +
                             " in: "    + in +
                             " owner: " + owner);
        }

        this.owner = owner;
    }


/**
 * Mark this stream as inactive for its owner socket, so the next time
 * a read is attempted, the owner will be notified and a new underlying
 * input stream obtained.
 */

    public void
    deactivate() {
        if (HttpSendInputStream_Debug) {
            JSDTObject.Debug("HttpSendInputStream: deactivate.");
        }

        in = null;
    }


/**
 * Read a byte of data from the stream.
 */

    public int
    read() throws IOException {
        if (in == null) {
            in = owner.readNotify();
        }

        return(in.read());
    }


/**
 * Read into an array of bytes.
 *
 * @param b the buffer into which the data is to be read
 * @param off the start offset of the data
 * @param len the maximum number of bytes to read
 */

    public int
    read(byte b[], int off, int len) throws IOException {
        if (HttpSendInputStream_Debug) {
            JSDTObject.Debug("HttpSendInputStream: read:" +
                             " b: "   + b +
                             " off: " + off +
                             " len: " + len);
        }

        if (len == 0) {
            return(0);
        }
        if (in == null) {
            in = owner.readNotify();
        }

        return(in.read(b, off, len));
    }


/**
 * Skip bytes of input.
 *
 * @param n the number of bytes to be skipped
 */

    public long
    skip(long n) throws IOException {
        if (HttpSendInputStream_Debug) {
            JSDTObject.Debug("HttpSendInputStream: skip:" +
                             " n: " + n);
        }

        if (n == 0) {
            return(0);
        }
        if (in == null) {
            in = owner.readNotify();
        }

        return(in.skip(n));
    }


/**
 * Return the number of bytes that can be read without blocking.
 */

    public int
    available() throws IOException {
        if (HttpSendInputStream_Debug) {
            JSDTObject.Debug("HttpSendInputStream: available.");
        }

        if (in == null) {
            in = owner.readNotify();
        }

        return(in.available());
    }


/**
 * Close the stream.
 */

    public void
    close() throws IOException {
        if (HttpSendInputStream_Debug) {
            JSDTObject.Debug("HttpSendInputStream: close.");
        }

        owner.close();
    }


/**
 * Mark the current position in the stream.
 *
 * @param readlimit how many bytes can be read before mark becomes invalid
 */

    public synchronized void
    mark(int readlimit) {
        if (HttpSendInputStream_Debug) {
            JSDTObject.Debug("HttpSendInputStream: mark:" +
                             " read limit: " + readlimit);
        }

        if (in == null) {
            try {
                in = owner.readNotify();
            } catch (IOException e) {
                return;
            }
        }
        in.mark(readlimit);
    }


/**
 * Reposition the stream to the last marked position.
 */

    public synchronized void
    reset() throws IOException {
        if (HttpSendInputStream_Debug) {
            JSDTObject.Debug("HttpSendInputStream reset.");
        }

        if (in == null) {
            in = owner.readNotify();
        }
        in.reset();
    }


/**
 * Return true if this stream type supports mark/reset.
 */

    public boolean
    markSupported() {
        if (HttpSendInputStream_Debug) {
            JSDTObject.Debug("HttpSendInputStream: markSupported.");
        }

        if (in == null) {
            try {
                in = owner.readNotify();
            } catch (IOException e) {
                return(false);
            }
        }

        return(in.markSupported());
    }
}
