
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

import com.sun.media.jsdt.*;
import com.sun.media.jsdt.impl.*;
import java.io.*;

/**
 * JSDT registry authentication client class.
 *
 * @version     2.3 - 7th November 2017
 * @author      Rich Burridge
 */

final class
RegistryClient extends JSDTObject implements Client, Runnable, httpDebugFlags {

    // States that we can be in during client authentication.
    private static final int SENDING_REQUEST = 1;
    private static final int SENT_REQUEST    = 2;
    private static final int NEED_RESPONSE   = 3;
    private static final int GOT_RESPONSE    = 4;

    // The current state of client authentication.
    protected volatile int state = SENDING_REQUEST;

    // The id to use in client/server messages.
    int id = 0;

    // The name of the Client trying to perform the priviledged operation.
    String name = null;

    /* The URL String of the object the priviledged operation is being
     * performed on.
     */
    private String urlString = null;

    // The action the client is trying to perform.
    int action = 0;

    // The message associated with this authentication operation.
    private Message message;

    // The manager for the registry.
    private RegistryManager manager;


/**
 * <A NAME="SD_REGISTRYCLIENT"></A>
 * <EM>RegistryClient</EM> the constructor for the RegistryClient class.
 *
 * @param message the message for this authentication operation.
 * @param manager the registry manager.
 */

    RegistryClient(Message message, RegistryManager manager) {
        DataInputStream in = message.thread.dataIn;

        if (RegistryClient_Debug) {
            debug("RegistryClient: constructor:" +
                  " message: " + message +
                  " manager: " + manager);
        }

        try {
            name      = in.readUTF();
            urlString = in.readUTF();
            action    = in.readInt();
        } catch (IOException e) {
            error("RegistryClient: constructor: ", e);
        }

        this.message = message;
        this.manager = manager;
        id           = message.id;
    }


/**
 * <A NAME="SD_AUTHENTICATE"></A>
 * <EM>authenticate</EM> used to authenticate a client for potentially
 * creating or destroying a Session or a Client reference in the Registry.
 * The Registry Manager will be doing this client validation.
 *
 * The manager sends this proxy client a challenge. The client sends the
 * challenge (via the original socket connection in the message thread) to
 * the real client trying to do the priviledged operation. It responds.
 * The response is sent back to this proxy client, and returned to the manager.
 *
 * It then determines whether the client should be allowed to do this
 * operation or not.
 *
 * @param info the authentication info for this validation.
 *
 * @return the response by the remote client to the managers challenge.
 */

    public Object
    authenticate(AuthenticationInfo info) {
        DataInputStream       in;
        byte[]                cBytes, rBytes;
        ByteArrayInputStream  bis;
        ObjectInputStream     ois;
        ObjectOutputStream    oos;
        int                   length;
        Object                challenge  = info.getChallenge();
        ByteArrayOutputStream bos        = new ByteArrayOutputStream();
        JSDTThread            thread     = message.thread;
        int                   retval     = 0;
        Object                response   = null;

        if (RegistryClient_Debug) {
            debug("RegistryClient: authenticate:" +
                  " info: " + info);
        }

        try {
            thread.writeMessageHeader(thread.dataOut, (short) 1, id,
                                      T_Registry, T_Authenticate, false, true);
            thread.dataOut.writeInt(retval);
            oos = new ObjectOutputStream(bos);
            oos.writeObject(challenge);
            cBytes = bos.toByteArray();
            thread.dataOut.writeInt(cBytes.length);
            thread.dataOut.write(cBytes, 0, cBytes.length);
            thread.flush();
            thread.finishMessage();

            synchronized (this) {
                state = SENT_REQUEST;
                notifyAll();
            }

            synchronized (this) {
                while (state != NEED_RESPONSE) {
                    try {
                        wait();
                    } catch (InterruptedException ie) {
                    }
                }
            }

            in = message.thread.dataIn;
            in.readUTF();                       /* clientName. */
            in.readUTF();                       /* urlString. */
            in.readInt();                       /* action. */
            length   = in.readInt();
            rBytes   = message.thread.getData(length);
            bis      = new ByteArrayInputStream(rBytes);
            ois      = new ObjectInputStream(bis);
            response = ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            error("RegistryClient: authenticate: ", e);
        }

        synchronized (this) {
            state = GOT_RESPONSE;
            notifyAll();
        }

        return(response);
    }


/**
 * <A NAME="SD_CHALLENGE"></A>
 * <EM>challenge</EM> get the name of this client.
 */

    void
    challenge(Message message) {
        if (RegistryClient_Debug) {
            debug("RegistryClient: challenge:" +
                  " message: " + message);
        }

        this.message = message;

        synchronized (this) {
            while (state == SENDING_REQUEST) {
                try {
                    wait();
                } catch (InterruptedException ie) {
                }
            }
        }

        synchronized (this) {
            state = NEED_RESPONSE;
            notifyAll();
        }

        synchronized (this) {
            while (state != GOT_RESPONSE) {
                try {
                    wait();
                } catch (InterruptedException ie) {
                }
            }
        }
    }


/**
 * <A NAME="SD_GETNAME"></A>
 * <EM>getName</EM> get the name of this client.
 *
 * @return the name of the client.
 */

    public String
    getName() {
        if (RegistryClient_Debug) {
            debug("RegistryClient: getName.");
        }

        return(name);
    }


/**
 * <A NAME="SD_RUN"></A>
 * <EM>run</EM>
 */

    public void
    run() {
        int                retval   = 0;
        boolean            admitted = false;
        AuthenticationInfo info;

        if (RegistryClient_Debug) {
            debug("RegistryClient: run.");
        }

        info = new AuthenticationInfo(null, action, urlString,
                                      AuthenticationInfo.REGISTRY);

        try {
            admitted = manager.registryRequest(info, this);
        } catch (Throwable th) {
            error("RegistryClient: run: ",
                  "impl.thrown", th + " by manager.");
        }

        try {
            message.thread.writeMessageHeader(message.thread.dataOut,
                            (short) 1, id, T_Registry, T_Challenge, false, true);
            message.thread.dataOut.writeInt(retval);
            message.thread.dataOut.writeBoolean(admitted);
            message.thread.flush();
            message.thread.finishMessage();
        } catch (IOException e) {
            error("RegistryClient: run: ", e);
        }
    }
}
