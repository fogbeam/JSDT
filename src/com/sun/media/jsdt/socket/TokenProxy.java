
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

package com.sun.media.jsdt.socket;

import com.sun.media.jsdt.*;
import com.sun.media.jsdt.impl.*;
import java.io.*;

/**
 * JSDT Token client-side proxy class.
 *
 * @version     2.3 - 6th November 2017
 * @author      Rich Burridge
 */

public final class
TokenProxy extends ManageableProxy implements AbstractTokenProxy {

/**
 * <A NAME="SD_GETPROXY"></A>
 * <EM>getProxy</EM>
 *
 * @return
 */

    public Object
    getProxy() {
        if (TokenProxy_Debug) {
            debug("TokenProxy: getProxy.");
        }

        return(this);
    }


/**
 * <A NAME="SD_INITPROXY"></A>
 * <EM>initProxy</EM> initialise the "client-side" proxy for this Token.
 *
 * @param name the name of the Token proxy being constructed.
 * @param session the client-side session the Token belongs to.
 * @param object the client-side Token.
 */

    public void
    initProxy(String name, SessionImpl session, Object object) {
        if (TokenProxy_Debug) {
            debug("TokenProxy: initProxy:" +
                  " name: "    + name +
                      " session: " + session);
        }

        this.name    = name;
        this.session = session;
        super.initProxy(name, session, object);
    }


    public int
    give(Client client, String receivingClientName)
        throws ConnectionException, InvalidClientException,
               NoSuchTokenException, NoSuchClientException,
               NoSuchSessionException, PermissionDeniedException,
               TimedOutException {
        DataInputStream in;
        Message         message;
        int             retval;
        int             status;
        SocketThread    thread    = sp.proxyThread;
        int             id        = thread.getId();
        short           sessionNo = sp.getSessionNo();
        char            type      = TokenImpl.M_Token;

        if (TokenProxy_Debug) {
            debug("TokenProxy: give:" +
                  " client: "                + client +
                  " receiving client name: " + receivingClientName);
        }

        if (session == null) {
            throw new NoSuchSessionException();
        }

        try {
            thread.writeMessageHeader(thread.dataOut, sessionNo, id,
                                      type, T_Give, true, true);
            thread.dataOut.writeUTF(name);
            thread.dataOut.writeUTF(Util.getClientName(client));
            thread.dataOut.writeUTF(receivingClientName);
            thread.flush();
            message = thread.waitForReply();

            in     = message.thread.dataIn;
            retval = in.readInt();
            status = in.readInt();
            message.thread.finishReply();

            if (retval != 0) {
                switch (retval) {
                    case JSDTException.NO_SUCH_TOKEN:
                        throw new NoSuchTokenException();
                    case JSDTException.NO_SUCH_CLIENT:
                        throw new NoSuchClientException();
                    case JSDTException.NO_SUCH_SESSION:
                        session = null;
                        throw new NoSuchSessionException();
                    case JSDTException.PERMISSION_DENIED:
                        throw new PermissionDeniedException();
                    default:
                        error("TokenProxy: give: ",
                              "impl.unknown.exception.type", retval);
                }
            }
        } catch (IOException e) {
            thread.finishReply();
            throw new ConnectionException();
        }

        return(status);
    }


    public int
    grab(Client client, boolean exclusive)
        throws ConnectionException, InvalidClientException,
               NoSuchTokenException, NoSuchClientException,
               NoSuchSessionException, PermissionDeniedException,
               TimedOutException {
        DataInputStream in;
        Message         message;
        int             retval;
        int             status;
        SocketThread    thread    = sp.proxyThread;
        int             id        = thread.getId();
        short           sessionNo = sp.getSessionNo();
        char            type      = TokenImpl.M_Token;

        if (TokenProxy_Debug) {
            debug("TokenProxy: grab:" +
                  " client: "    + client  +
                  " exclusive: " + exclusive);
        }

        if (session == null) {
            throw new NoSuchSessionException();
        }

        try {
            thread.writeMessageHeader(thread.dataOut, sessionNo, id,
                                      type, T_Grab, true, true);
            thread.dataOut.writeUTF(name);
            thread.dataOut.writeUTF(Util.getClientName(client));
            thread.dataOut.writeBoolean(exclusive);
            thread.flush();
            message = thread.waitForReply();

            in     = message.thread.dataIn;
            retval = in.readInt();
            status = in.readInt();
            message.thread.finishReply();

            if (retval != 0) {
                switch (retval) {
                    case JSDTException.NO_SUCH_TOKEN:
                        throw new NoSuchTokenException();
                    case JSDTException.NO_SUCH_CLIENT:
                        throw new NoSuchClientException();
                    case JSDTException.NO_SUCH_SESSION:
                        session = null;
                        throw new NoSuchSessionException();
                    case JSDTException.PERMISSION_DENIED:
                        throw new PermissionDeniedException();
                    default:
                        error("TokenProxy: grab: ",
                              "impl.unknown.exception.type", retval);
                }
            }
        } catch (IOException e) {
            thread.finishReply();
            throw new ConnectionException();
        }

        return(status);
    }


/**
 * <A NAME="SD_LISTHOLDERNAMES"></A>
 * list the names of the Clients who are currently holding (grabbing or
 * inhibiting) this Token. This method can be used in conjunction with the
 * <CODE>test</CODE> method to determine if the token is being grabbed or
 * inhibited.
 *
 * @return a sorted array of names of Clients currently holding this Token.
 *
 * @exception ConnectionException if a connection error occured.
 * @exception NoSuchSessionException if this Session doesn't exist.
 * @exception NoSuchTokenException if this Token doesn't exist.
 * @exception TimedOutException if no reply was received for this operation
 * in the given timeout period.
 */

    public String[]
    listHolderNames()
        throws ConnectionException, NoSuchSessionException,
               NoSuchTokenException, TimedOutException {
        DataInputStream in;
        Message         message;
        int             retval;
        int             n;
        SocketThread    thread      = sp.proxyThread;
        int             id          = thread.getId();
        short           sessionNo   = sp.getSessionNo();
        char            type        = TokenImpl.M_Token;
        String[]        clientNames = null;

        if (TokenProxy_Debug) {
            debug("TokenProxy: listHolderNames.");
        }

        if (session == null) {
            throw new NoSuchSessionException();
        }

        try {
            thread.writeMessageHeader(thread.dataOut, sessionNo, id,
                                      type, T_ListHolderNames, true, true);
            thread.dataOut.writeUTF(name);
            thread.flush();
            message = thread.waitForReply();

            in     = message.thread.dataIn;
            retval = in.readInt();
            if (retval == 0) {
                n           = in.readInt();
                clientNames = new String[n];
                for (int i = 0; i < n; i++) {
                    clientNames[i] = in.readUTF();
                }
                Util.sort(clientNames);
            }

            message.thread.finishReply();

            if (retval != 0) {
                switch (retval) {
                    case JSDTException.NO_SUCH_SESSION:
                        session = null;
                        throw new NoSuchSessionException();
                    case JSDTException.NO_SUCH_TOKEN:
                        throw new NoSuchTokenException();
                    default:
                        error("TokenProxy: listHolderNames: ",
                              "impl.unknown.exception.type", retval);
                }
            }
        } catch (IOException e) {
            thread.finishReply();
            throw new ConnectionException();
        }
        return(clientNames);
    }


    public int
    request(Client client)
        throws ConnectionException, InvalidClientException,
               NoSuchTokenException, NoSuchClientException,
               NoSuchSessionException, PermissionDeniedException,
               TimedOutException {
        DataInputStream in;
        Message         message;
        int             retval;
        int             status;
        SocketThread    thread    = sp.proxyThread;
        int             id        = thread.getId();
        short           sessionNo = sp.getSessionNo();
        char            type      = TokenImpl.M_Token;

        if (TokenProxy_Debug) {
            debug("TokenProxy: request:" +
                  " client: " + client);
        }

        if (session == null) {
            throw new NoSuchSessionException();
        }

        try {
            thread.writeMessageHeader(thread.dataOut, sessionNo, id,
                                      type, T_Request, true, true);
            thread.dataOut.writeUTF(name);
            thread.dataOut.writeUTF(Util.getClientName(client));
            thread.flush();
            message = thread.waitForReply();

            in     = message.thread.dataIn;
            retval = in.readInt();
            status = in.readInt();
            message.thread.finishReply();

            if (retval != 0) {
                switch (retval) {
                    case JSDTException.NO_SUCH_TOKEN:
                        throw new NoSuchTokenException();
                    case JSDTException.NO_SUCH_CLIENT:
                        throw new NoSuchClientException();
                    case JSDTException.NO_SUCH_SESSION:
                        session = null;
                        throw new NoSuchSessionException();
                    case JSDTException.PERMISSION_DENIED:
                        throw new PermissionDeniedException();
                    default:
                        error("TokenProxy: request: ",
                              "impl.unknown.exception.type", retval);
                }
            }
        } catch (IOException e) {
            thread.finishReply();
            throw new ConnectionException();
        }
        return(status);
    }


    public int
    release(Client client)
        throws ConnectionException, InvalidClientException,
               NoSuchTokenException, NoSuchClientException,
               ClientNotGrabbingException, ClientNotReleasedException,
               NoSuchSessionException, PermissionDeniedException,
               TimedOutException {
        DataInputStream in;
        Message         message;
        int             retval;
        int             status;
        SocketThread    thread    = sp.proxyThread;
        int             id        = thread.getId();
        short           sessionNo = sp.getSessionNo();
        char            type      = TokenImpl.M_Token;

        if (TokenProxy_Debug) {
            debug("TokenProxy: release:" +
                  " client: "    + client);
        }

        if (session == null) {
            throw new NoSuchSessionException();
        }

        try {
            thread.writeMessageHeader(thread.dataOut, sessionNo, id,
                                      type, T_Release, true, true);
            thread.dataOut.writeUTF(name);
            thread.dataOut.writeUTF(Util.getClientName(client));
            thread.flush();
            message = thread.waitForReply();

            in     = message.thread.dataIn;
            retval = in.readInt();
            status = in.readInt();
            message.thread.finishReply();

            if (retval != 0) {
                switch (retval) {
                    case JSDTException.NO_SUCH_TOKEN:
                        throw new NoSuchTokenException();
                    case JSDTException.NO_SUCH_CLIENT:
                        throw new NoSuchClientException();
                    case JSDTException.NO_SUCH_SESSION:
                        session = null;
                        throw new NoSuchSessionException();
                    case JSDTException.CLIENT_NOT_GRABBING:
                        throw new ClientNotGrabbingException();
                    case JSDTException.CLIENT_NOT_RELEASED:
                        throw new ClientNotReleasedException();
                    case JSDTException.PERMISSION_DENIED:
                        throw new PermissionDeniedException();
                    default:
                        error("TokenProxy: release: ",
                              "impl.unknown.exception.type", retval);
                }
            }
        } catch (IOException e) {
            thread.finishReply();
            throw new ConnectionException();
        }
        return(status);
    }


    public int
    test()
        throws ConnectionException, NoSuchSessionException,
               NoSuchTokenException, TimedOutException {
        DataInputStream in;
        Message         message;
        int             retval;
        int             status;
        SocketThread    thread    = sp.proxyThread;
        int             id        = thread.getId();
        short           sessionNo = sp.getSessionNo();
        char            type      = TokenImpl.M_Token;

        if (TokenProxy_Debug) {
            debug("TokenProxy: test:" +
                  " message: " + message);
        }

        if (session == null) {
            throw new NoSuchSessionException();
        }

        try {
            thread.writeMessageHeader(thread.dataOut, sessionNo, id,
                                      type, T_Test, true, true);
            thread.dataOut.writeUTF(name);
            thread.flush();
            message = thread.waitForReply();

            in     = message.thread.dataIn;
            retval = in.readInt();
            status = in.readInt();
            message.thread.finishReply();

            if (retval != 0) {
                switch (retval) {
                    case JSDTException.NO_SUCH_TOKEN:
                        throw new NoSuchTokenException();
                    case JSDTException.NO_SUCH_SESSION:
                        session = null;
                        throw new NoSuchSessionException();
                    default:
                        error("TokenProxy: test: ",
                              "impl.unknown.exception.type", retval);
                }
            }
        } catch (IOException e) {
            thread.finishReply();
            throw new ConnectionException();
        }
        return(status);
    }
}
