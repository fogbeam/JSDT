
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
import java.util.*;

/**
 * JSDT message class.
 *
 * @version     2.3 - 6th November 2017
 * @author      Rich Burridge
 */

abstract class
JSDTMessage extends JSDTObject implements httpDebugFlags {

    // The session associated with this message.
    protected SessionImpl session;

    // The name of the session associated with this message.
    protected String sessionName;


/**
 * <A NAME="SD_HANDLEMESSAGE"></A>
 * <EM>handleMessage</EM>
 *
 * @param message
 */

    protected abstract void
    handleMessage(Message message);


/**
 * <A NAME="SD_CLIENTCHALLENGE"></A>
 * <EM>clientChallenge</EM>
 *
 * @param message
 */

    protected final void
    clientChallenge(Message message) {
        int                   length;
        byte[]                cBytes, rBytes;
        ByteArrayInputStream  bis;
        ObjectInputStream     ois;
        ObjectOutputStream    oos;
        SessionProxy          sp  = (SessionProxy) session.po.getProxy();
        DataInputStream       in  = message.thread.dataIn;
        char                  objectType        = 0;
        String                objectName        = null;
        String                clientName        = null;
        int                   action            = 0;
        Object                challenge         = null;
        ByteArrayOutputStream bos               = new ByteArrayOutputStream();
        Object                response          = null;
        Client                priviledgedClient = null;

        if (JSDTMessage_Debug) {
            debug("JSDTMessage: clientChallenge:" +
                  " message: " + message);
        }

        try {
            objectType        = in.readChar();
            objectName        = in.readUTF();
            clientName        = in.readUTF();
            action            = in.readInt();
            length            = in.readInt();
            cBytes            = message.thread.getData(length);
            bis               = new ByteArrayInputStream(cBytes);
            ois               = new ObjectInputStream(bis);
            challenge         = ois.readObject();
            priviledgedClient = sp.getPriviledgedClient(clientName, action,
                                                        objectType);
        } catch (IOException | ClassNotFoundException e) {
            error("JSDTMessage: clientChallenge: ", e);
        }

        if (clientName.equals(priviledgedClient.getName())) {
            try {
                AuthenticationInfo info;
                char               authType = 0;

                switch (objectType) {
                    case ByteArrayImpl.M_ByteArray:
                        authType = AuthenticationInfo.BYTEARRAY;
                        break;
                    case ChannelImpl.M_Channel:
                        authType = AuthenticationInfo.CHANNEL;
                        break;
                    case SessionImpl.M_Session:
                        authType = AuthenticationInfo.SESSION;
                        break;
                    case TokenImpl.M_Token:
                        authType = AuthenticationInfo.TOKEN;
                        break;
                }

                info = new AuthenticationInfo(session, action,
                                              objectName, authType);
                info.setChallenge(challenge);
                sp.proxyThread.writeMessageHeader(sp.proxyThread.dataOut,
                                message.sessionNo, message.id,
                                ClientImpl.M_Client, T_Challenge, false, true);
                sp.proxyThread.dataOut.writeChar(objectType);
                sp.proxyThread.dataOut.writeUTF(objectName);
                sp.proxyThread.dataOut.writeUTF(priviledgedClient.getName());
                sp.proxyThread.dataOut.writeInt(action);

                try {
                    response = priviledgedClient.authenticate(info);
                } catch (Throwable th) {
                    error("JSDTMessage: clientChallenge: ",
                          "impl.thrown", th + " by client.");
                }
                oos = new ObjectOutputStream(bos);
                oos.writeObject(response);
                rBytes = bos.toByteArray();
                sp.proxyThread.dataOut.writeInt(rBytes.length);
                sp.proxyThread.dataOut.write(rBytes, 0, rBytes.length);

                sp.proxyThread.flush();

                try {
                    Message replyMessage = sp.proxyThread.waitForReply();

                    replyMessage.thread.dataIn.readInt();
                    replyMessage.thread.finishReply();
                } catch (TimedOutException toe) {
                }

            } catch (IOException e) {
                error("JSDTMessage: clientChallenge: ", e);
            }
        }
    }


    protected final void
    getAuthenticationResult(Message message) {
        SessionProxy    sp = (SessionProxy) session.po.getProxy();
        DataInputStream in = message.thread.dataIn;

        if (JSDTMessage_Debug) {
            debug("JSDTMessage: getAuthenticationResult:" +
                  " message: " + message);
        }

        try {
            int retval = in.readInt();

            if (retval == 0) {
                String  objectName    = in.readUTF();
                boolean authenticated = in.readBoolean();
                String  key           = objectName + ":" + message.sessionNo +
                                        ":" + message.id;

                sp.proxyThread.resumeClientThread(key, authenticated);
            }
        } catch (IOException e) {
            error("JSDTMessage: getAuthenticationResult: ", e);
        }
    }


/**
 * <A NAME="SD_INFORMLISTENERS"></A>
 * <EM>informListeners</EM>
 *
 * @param message
 * @param m
 * @param resourceName
 */

    final void
    informListeners(Message message, ManageableImpl m, String resourceName) {
        DataInputStream in         = message.thread.dataIn;
        String          clientName = null;
        int             type       = 0;
        ManageableProxy mp         = (ManageableProxy) m.mpo.getProxy();

        if (JSDTMessage_Debug) {
            debug("JSDTMessage: informListeners:" +
                  " message: "       + message +
                  " object name: "   + m.getName() +
                  " resource name: " + resourceName);
        }

        try {
            clientName = in.readUTF();
            type       = in.readInt();
        } catch (IOException e) {
            error("JSDTMessage: informListeners: ", e);
        }

        if (mp.listeners != null) {
            synchronized (mp.listeners) {
                Enumeration e, k;

                for (e = mp.listeners.elements(), k = mp.listeners.keys();
                     e.hasMoreElements();) {
                    EventListener listener = (EventListener) k.nextElement();
                    int mask = (Integer) e.nextElement();

                    if ((mask & type) != 0) {
                        ListenerMessage lm =
                            new ListenerMessage(listener, session,
                                        resourceName, clientName, m, type);

                        Util.startThread(lm, "ListenerMessageThread: " +
                                         clientName + ":" + resourceName, true);
                    }
                }
            }
        }
    }
}
