
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
 * The HttpOutputStream class assists the HttpSendSocket and HttpReceiveSocket
 * classes by providing an output stream that buffers its entire input until
 * closed, and then it sends the complete transmission prefixed by the end of
 * an HTTP header that specifies the content length.
 *
 * Based on the sun.rmi.transport.proxy.HttpOutputStream class.
 *
 * @version     2.3 - 28th October 2017
 * @author      Rich Burridge
 */

class
HttpOutputStream extends ByteArrayOutputStream implements httpDebugFlags {

    // Data to send if the response would otherwise be empty.
    private static byte[] emptyData = { 0 };

    // The output stream to send response to.
    protected OutputStream out;

    // The HttpReceiveSocket object that is providing this stream.
    private HttpReceiveSocket owner;

    // True if HTTP response has been sent.
    private boolean responseSent = false;

    // Set true after sending infinite content length.
    private boolean doneInfinite = false;


    /**
     * Begin buffering new HTTP response to be sent to a given stream.
     *
     * @param out the OutputStream to send response to.
     */

    public
    HttpOutputStream(OutputStream out, HttpReceiveSocket owner) {
        super();

        if (HttpOutputStream_Debug) {
            JSDTObject.Debug("HttpOutputStream: constructor:" +
                             " out: "   + out +
                             " owner: " + owner);
        }

        this.out   = out;
        this.owner = owner;
    }


/**
 * On close, send HTTP-packaged response.
 */

    public synchronized void
    close() throws IOException {
        if (HttpOutputStream_Debug) {
            JSDTObject.Debug("HttpOutputStream: close:" +
                             " size: "          + size() +
                             " response sent: " + responseSent);
        }

        if (!responseSent || (owner.isReusable() && doneInfinite)) {

/*
 * If response would have zero content length, then make it
 * have some arbitrary data so that certain clients will not
 * fail because the "document contains no data".
 */

            if (size() == 0) {
                write(emptyData);
            }

            DataOutputStream dos = new DataOutputStream(out);

            if (owner.isReusable()) {
                if (!doneInfinite) {
                    dos.writeBytes("Content-type: application/octet-stream\r\n");
                    dos.writeBytes("\r\n");
                    doneInfinite = true;
                }
            } else {
                dos.writeBytes("Content-type: application/octet-stream\r\n");
                dos.writeBytes("Content-length: " + size() + "\r\n");
                dos.writeBytes("\r\n");
            }

            writeTo(dos);
            dos.flush();

/*
 * Do not close the underlying stream here, because that would
 * close the underlying socket and prevent reading a response.
 */

            reset();              // Reset byte array.
            responseSent = true;
        }
    }
}
