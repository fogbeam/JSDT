
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
 * JSDT proxy authentication client class.
 *
 * @version     2.3 - 6th November 2017
 * @author      Rich Burridge
 */

final class
AuthenticateClient extends JSDTObject
                   implements Client, Runnable, httpDebugFlags {

    // The client-side session associated with this authentication client.
    private SessionImpl session;

    /* The kind of managed object, the client is trying to perform the
     * priviledged operation on: Session, ByteArray, Channel or Token.
     */
    char objectType;

    /* The name of the managed object, the client is trying to perform the
     * priviledged operation on.
     */
    String objectName = null;

    // The action the client is trying to perform on the managed object.
    private int objectAction;

    // The id to use in client/server messages.
    int id = 0;

    // The name of this client.
    private String name;

    // The client-side proxy associated with the Session for this object.
    private SessionProxy sp;

    // The client-side managable object.
    private Manageable manageable;

    // The client-side manager associated with this manageable object.
    private JSDTManager manager;


/**
 * <A NAME="SD_AUTHENTICATECLIENT"></A>
 * <EM>AuthenticateClient</EM> the constructor for the AuthenticateClient
 * class.
 *
 * @param message
 * @param session
 * @param manager the client-side manager.
 * @param manageable the client-side manageable object.
 */

    AuthenticateClient(Message message, SessionImpl session,
                       JSDTManager manager, Manageable manageable) {
        DataInputStream in = message.thread.dataIn;

        if (AuthenticateClient_Debug) {
            debug("AuthenticateClient: constructor:" +
                  " message: "    + message +
                  " session: "    + session +
                  " manager: "    + manager +
                  " manageable: " + manageable);
        }

        try {
            objectType   = in.readChar();
            objectName   = in.readUTF();
            name         = in.readUTF();
            objectAction = in.readInt();
        } catch (IOException e) {
            error("AuthenticateClient: constructor: ", e);
        }

        this.session    = session;
        this.manager    = manager;
        this.manageable = manageable;

        id = message.id;
        sp = (SessionProxy) session.po.getProxy();
    }


/**
 * <A NAME="SD_AUTHENTICATE"></A>
 * <EM>authenticate</EM> used to authenticate a client for potentially
 * joining a managed object. The Session, Channel or Token Manager will be
 * doing this client validation.
 *
 * The manager sends the proxy client a challenge. The client sends the
 * challenge (via the session-server) to the real client trying to join
 * the session/channel/token or create/destroy a channel/token in a managed
 * session. It responds. The response is sent back (via the session-server)
 * to this proxy client, and returned to the manager.
 *
 * It then determines whether the client should be admitted or not.
 *
 * @param info the authentication info for this validation.
 *
 * @return the response by the remote client to the managers challenge.
 */

    public Object
    authenticate(AuthenticationInfo info) {
        char                  authType   = info.getType();
        char                  objectType = 0;
        String                objectName = info.getName();
        Object                      challenge  = info.getChallenge();
        ByteArrayOutputStream bos        = new ByteArrayOutputStream();
        ObjectOutputStream    oos;
        byte[]                cBytes;
        ByteArrayInputStream  bis;
        ObjectInputStream     ois;
        byte[]                rBytes;
        int                   length;
        int                   action     = info.getAction();
        ManageableProxy       mp = (ManageableProxy)
                                 ((ManageableImpl) manageable).mpo.getProxy();
        HttpThread            thread     = mp.getManagerThread();
        DataInputStream       in;
        short                 sessionNo  = sp.getSessionNo();
        Message               message;
        int                   retval;
        Object                response   = null;

        if (AuthenticateClient_Debug) {
            debug("AuthenticateClient: authenticate:" +
                  " info: " + info);
        }

        switch (authType) {
            case AuthenticationInfo.BYTEARRAY:
                objectType = ByteArrayImpl.M_ByteArray;
                break;
            case AuthenticationInfo.CHANNEL:
                objectType = ChannelImpl.M_Channel;
                break;
            case AuthenticationInfo.SESSION:
                objectType = SessionImpl.M_Session;
                break;
            case AuthenticationInfo.TOKEN:
                objectType = TokenImpl.M_Token;
                break;
        }

        try {
            thread.writeMessageHeader(thread.dataOut,
                             sessionNo, thread.getId(),
                             ClientImpl.M_Client, T_Authenticate, true, true);
            thread.dataOut.writeChar(objectType);
            thread.dataOut.writeUTF(objectName);
            thread.dataOut.writeUTF(name);
            thread.dataOut.writeInt(action);

            oos = new ObjectOutputStream(bos);
            oos.writeObject(challenge);
            cBytes = bos.toByteArray();
            thread.dataOut.writeInt(cBytes.length);
            thread.dataOut.write(cBytes, 0, cBytes.length);

            thread.flush();

            message = thread.waitForClientResponse(id);

            in = message.thread.dataIn;
            in.readChar();                      /* objectType. */
            in.readUTF();                       /* objectName. */
            in.readUTF();                       /* clientName. */
            in.readInt();                       /* action. */
            retval   = in.readInt();
            length   = in.readInt();
            rBytes   = message.thread.getData(length);
            bis      = new ByteArrayInputStream(rBytes);
            ois      = new ObjectInputStream(bis);
            response = ois.readObject();
            thread.finishReply();

            if (retval != 0) {
                error("AuthenticateClient: authenticate: ",
                      "impl.invalid.response");
            }
        } catch (IOException | ClassNotFoundException e) {
            error("AuthenticateClient: authenticate: ", e);
        }

        return(response);
    }


/**
 * <A NAME="SD_GETNAME"></A>
 * <EM>getName</EM> get the name of this client.
 *
 * @return the name of the client.
 */

    public String
    getName() {
        if (AuthenticateClient_Debug) {
            debug("AuthenticateClient: getName.");
        }
        return(name);
    }


/**
 * <A NAME="SD_RUN"></A>
 * <EM>run</EM>
 */

    public void
    run() {
        short              sessionNo = sp.getSessionNo();
        char               type      = ClientImpl.M_Client;
        char               authType  = 0;
        boolean            admitted  = false;
        AuthenticationInfo info;

        if (AuthenticateClient_Debug) {
            debug("AuthenticateClient: run.");
        }

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

        info = new AuthenticationInfo(session, objectAction,
                                      objectName, authType);

        try {
            switch (objectType) {
                case ByteArrayImpl.M_ByteArray:
                    admitted = ((ByteArrayManager) manager).byteArrayRequest(
                                    (ByteArray) manageable, info, this);
                    break;
                case ChannelImpl.M_Channel:
                    admitted = ((ChannelManager) manager).channelRequest(
                                    (Channel) manageable, info, this);
                    break;
                case SessionImpl.M_Session:
                    admitted = ((SessionManager) manager).sessionRequest(
                                    (Session) manageable, info, this);
                    break;
                case TokenImpl.M_Token:
                    admitted = ((TokenManager) manager).tokenRequest(
                                    (Token) manageable, info, this);
            }
        } catch (Throwable th) {
            error("AuthenticateClient: run: ",
                  "impl.thrown", th + " by manager.");
        }

        try {
            sp.proxyThread.writeMessageHeader(sp.proxyThread.dataOut,
                                    sessionNo, 0, type, T_Join, false, true);
            sp.proxyThread.dataOut.writeChar(objectType);
            sp.proxyThread.dataOut.writeUTF(objectName);
            sp.proxyThread.dataOut.writeUTF(name);
            sp.proxyThread.dataOut.writeBoolean(admitted);
            sp.proxyThread.flush();
            sp.proxyThread.finishMessage();
        } catch (IOException e) {
            error("AuthenticateClient: run: ", e);
        }
    }
}
