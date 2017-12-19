
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

package com.sun.media.jsdt;

import com.sun.media.jsdt.event.TokenListener;

/**
 * The Token interface.
 *
 * @version     2.3 - 27th October 2017
 * @author      Rich Burridge
 * @since       JSDT 1.0
 */

@SuppressWarnings("UnusedReturnValue")
public interface
Token extends Manageable {

    /** Token not in use state. */
    int NOT_IN_USE        = 100;

    /** Token grabbed state. */
    int GRABBED           = 101;

    /** Token inhibited state. */
    int INHIBITED         = 102;

    /** Token giving state. */
    int GIVING            = 103;

    /** Token already grabbed state. */
    int ALREADY_GRABBED   = 104;

    /** Token already inhibited state. */
    int ALREADY_INHIBITED = 105;


/**
 * <A NAME="SD_ADDTOKENLISTENER"></A>
 * add the specified Token listener to receive Token events for this Token.
 *
 * @param listener the Token listener.
 *
 * @exception ConnectionException if a connection error occured.
 * @exception NoSuchSessionException if this Session doesn't exist.
 * @exception NoSuchTokenException if this Token doesn't exist.
 * @exception TimedOutException if no reply was received for this operation
 * in the given timeout period.
 */

    void
    addTokenListener(TokenListener listener)
        throws ConnectionException, NoSuchSessionException,
               NoSuchTokenException, TimedOutException;


/**
 * <A NAME="SD_REMOVETOKENLISTENER"></A>
 * remove the specified Token listener so that it no longer receives Token
 * events for this Token.
 *
 * @param listener the Token listener.
 *
 * @exception ConnectionException if a connection error occured.
 * @exception NoSuchSessionException if this Session doesn't exist.
 * @exception NoSuchTokenException if this Token doesn't exist.
 * @exception NoSuchListenerException if this TokenListener doesn't exist.
 * @exception TimedOutException if no reply was received for this operation
 * in the given timeout period.
 */

    void
    removeTokenListener(TokenListener listener)
        throws ConnectionException, NoSuchSessionException,
               NoSuchTokenException, NoSuchListenerException,
               TimedOutException;


/**
 * <A NAME="SD_GIVE"></A>
 * used by a Client to surrender a Token to another Client.  It will
 * fail if the requestor has not grabbed the specified Token within a
 * certain period of time.
 *
 * <P>A <EM>GIVEN</EM> TokenEvent is delivered to the Token listener of the
 * Client that this Client would like to give the Token to.
 *
 * <P>The giving Client must be either exclusively grabbing the Token or the
 * only Client non-exclusively inhibiting this Token for the <EM>give</EM>
 * operation to be permissible.
 *
 * <P>A Token being passed between two Clients and whose possession is not yet
 * resolved will appear to any Client requesting <EM>test</EM> to be giving
 * by some other Client and not held by the requestor. <EM>grab</EM> will fail
 * during this interval, even if requested by one of the two Clients involved.
 * <EM>release</EM> requested by the given will succeed, with the result that
 * the Token is released if the offer to the recipient is ultimately rejected.
 * <EM>release</EM> by the recipient will have no effect, just like the
 * attempted release of any other Token that the requester does not yet
 * possess. During the interval that a Token is being passed any
 * <EM>request</EM> indications that are generated will be delivered to both
 * Clients involved.
 *
 * @param client the Client giving this Token.
 * @param receivingClientName the name of the Client to receive the Token.
 *
 * @exception ConnectionException if a connection error occured.
 * @exception InvalidClientException if the Client is invalid is some way (ie.
 * its getName() method returns null).
 * @exception NoSuchTokenException if this Token doesn't exist.
 * @exception NoSuchClientException if the giving or receiving Client doesn't
 * exist.
 * @exception NoSuchSessionException if this Session doesn't exist.
 * @exception PermissionDeniedException if this Client does not have
 * permission to perform this operation.
 * @exception TimedOutException if no reply was received for this operation
 * in the given timeout period.
 *
 * @return the status of the token.
 */

    int
    give(Client client, String receivingClientName)
        throws ConnectionException, InvalidClientException,
               NoSuchTokenException, NoSuchClientException,
               NoSuchSessionException, PermissionDeniedException,
               TimedOutException;


/**
 * <A NAME="SD_GRAB"></A>
 * used by a Client to take exclusive (grab) or non-exclusive (inhibit)
 * control of a specific Token.
 *
 * <P>With an exclusive grab, the grab will succeed if the requestor is the
 * sole inhibitor of the Token.
 *
 * <P>The non-exclusive grab is used to take non-exclusive control of a specific
 * Token. It is used to prevent someone else from exclusively grabbing the
 * Token. Several Clients could inhibit a Token at the same time. This
 * operation will succeed if the requestor has grabbed the Token. The result
 * will be that the Token is no longer grabbed exclusively and is inhibited
 * instead. Therefore it may be inhibited by other Clients too.
 *
 * @param client the Client grabbing/inhibiting this Token.
 * @param exclusive indicates whether the grab should be exclusive.
 *
 * @exception ConnectionException if a connection error occured.
 * @exception InvalidClientException if the Client is invalid is some way (ie.
 * its getName() method returns null).
 * @exception NoSuchTokenException if this Token doesn't exist.
 * @exception NoSuchClientException if this Client doesn't exist.
 * @exception NoSuchSessionException if this Session doesn't exist.
 * @exception PermissionDeniedException if this Client does not have
 * permission to perform this operation.
 * @exception TimedOutException if no reply was received for this operation
 * in the given timeout period.
 *
 * @return the status of the token.
 */

    int
    grab(Client client, boolean exclusive)
        throws ConnectionException, InvalidClientException,
               NoSuchTokenException, NoSuchClientException,
               NoSuchSessionException, PermissionDeniedException,
               TimedOutException;


/**
 * <A NAME="SD_LISTHOLDERNAMES"></A>
 * list the names of the Clients who are currently holding (grabbing or
 * inhibiting) this Token. This method can be used in conjunction with the
 * <CODE>test</CODE> method to determine if the token is being grabbed or
 * inhibited.
 *
 * @return a sorted array of names of Clients currently holding this Token.
 * This array will be of zero length if there are no Clients currently
 * holding this Token.
 *
 * @exception ConnectionException if a connection error occured.
 * @exception NoSuchSessionException if this Session doesn't exist.
 * @exception NoSuchTokenException if this Token doesn't exist.
 * @exception TimedOutException if no reply was received for this operation
 * in the given timeout period.
 *
 * @see       com.sun.media.jsdt.Token#test
 *
 * @since     JSDT 1.3
 */

    String[]
    listHolderNames()
        throws ConnectionException, NoSuchSessionException,
               NoSuchTokenException, TimedOutException;


/**
 * <A NAME="SD_REQUEST"></A>
 * used by a Client to request a Token from the current possessor(s) of the
 * Token. A Token may be inhibited by several Clients, or it may be grabbed
 * by one Client. In any case, a <EM>REQUESTED</EM> TokenEvent is delivered
 * to all the Token listeners listening to this Token, who currently possess
 * the Token.
 *
 * @param client the Client requesting this Token.
 *
 * @exception ConnectionException if a connection error occured.
 * @exception InvalidClientException if the Client is invalid is some way (ie.
 * its getName() method returns null).
 * @exception NoSuchSessionException if this Session doesn't exist.
 * @exception NoSuchTokenException if this Token doesn't exist.
 * @exception NoSuchClientException if this Client doesn't exist.
 * @exception PermissionDeniedException if this Client does not have
 * permission to perform this operation.
 * @exception TimedOutException if no reply was received for this operation
 * in the given timeout period.
 *
 * @return the status of the token.
 */

    int
    request(Client client)
        throws ConnectionException, InvalidClientException,
               NoSuchTokenException, NoSuchClientException,
               NoSuchSessionException, PermissionDeniedException,
               TimedOutException;


/**
 * <A NAME="SD_RELEASE"></A>
 * used to free up a previously grabbed/inhibited Token.
 *
 * @param client the Client releasing this Token.
 *
 * @exception ConnectionException if a connection error occured.
 * @exception InvalidClientException if the Client is invalid is some way (ie.
 * its getName() method returns null).
 * @exception NoSuchTokenException if this Token doesn't exist.
 * @exception NoSuchClientException if this Client doesn't exist.
 * @exception NoSuchSessionException if this Session doesn't exist.
 * @exception ClientNotGrabbingException if this Client is not grabbing this
 * Token.
 * @exception ClientNotReleasedException if the Token was not released
 * successfully.
 * @exception PermissionDeniedException if this Client does not have
 * permission to perform this operation.
 * @exception TimedOutException if no reply was received for this operation
 * in the given timeout period.
 *
 * @return the status of the token.
 */

    int
    release(Client client)
        throws ConnectionException, InvalidClientException,
               NoSuchTokenException, NoSuchClientException,
               NoSuchSessionException, ClientNotGrabbingException,
               ClientNotReleasedException, PermissionDeniedException,
               TimedOutException;


/**
 * <A NAME="SD_TEST"></A>
 * used to check the status of a Token.
 *
 * @exception ConnectionException if a connection error occured.
 * @exception NoSuchSessionException if this Session doesn't exist.
 * @exception NoSuchTokenException if this Token doesn't exist.
 * @exception TimedOutException if no reply was received for this operation
 * in the given timeout period.
 *
 * @return one of the following:
 * <PRE>
 * NOT_IN_USE : not in use
 * GRABBED    : grabbed
 * INHIBITED  : inhibited
 * GIVING     : giving
 * </PRE>
 */

    int
    test()
        throws ConnectionException, NoSuchSessionException,
               NoSuchTokenException, TimedOutException;
}
