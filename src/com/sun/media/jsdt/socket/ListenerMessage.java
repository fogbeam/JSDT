
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
import com.sun.media.jsdt.event.*;
import java.util.EventListener;

/**
 * JSDT observer message thread class.
 *
 * @version     2.3 - 20th November 2017
 * @author      Rich Burridge
 */

final class
ListenerMessage extends JSDTMessage implements Runnable {

    // The event listener to be notified.
    private final EventListener eventListener;

    // The name of the resource that this event is about.
    private final String resourceName;

    // The client name to use in the listener event.
    private final String clientName;

    // The type of manageable object to raise the event on.
    private final Manageable manageable;

    // The listener event type.
    private final int type;


/**
 * <A NAME="SD_LISTENERMESSAGE"></A>
 * <EM>ListenerMessage</EM>
 *
 * @param eventListener
 * @param session
 * @param resourceName
 * @param clientName
 * @param manageable
 * @param type
 */

    public
    ListenerMessage(EventListener eventListener, SessionImpl session,
                    String resourceName, String clientName,
                    Manageable manageable, int type) {
        if (ListenerMessage_Debug) {
            debug("ListenerMessage: constructor:" +
                  " event listener: "  + eventListener +
                  " session name: "    + session.getName() +
                  " resource name: "   + resourceName +
                  " client name: "     + clientName +
                  " manageable name: " + manageable.getName() +
                  " type: "            + type);
        }

        this.eventListener = eventListener;
        this.session      = session;
        this.resourceName = resourceName;
        this.clientName   = clientName;
        this.manageable   = manageable;
        this.type         = type;
    }


/**
 * <A NAME="SD_HANDLEMESSAGE"></A>
 * <EM>handleMessage</EM>
 *
 * @param message
 */

    protected void
    handleMessage(Message message) {
        if (ListenerMessage_Debug) {
            debug("ListenerMessage: handleMessage:" +
                  " message: " + message);
        }

        try {
            if (manageable instanceof ByteArrayImpl) {
                sendByteArrayEvent((ByteArrayListener) eventListener);
            } else if (manageable instanceof ChannelImpl) {
                sendChannelEvent((ChannelListener) eventListener);
            } else if (manageable instanceof SessionImpl) {
                sendSessionEvent((SessionListener) eventListener);
            } else if (manageable instanceof TokenImpl) {
                sendTokenEvent((TokenListener) eventListener);
            }
        } catch (Throwable th) {
            error("ListenerMessage: handleMessage: ",
                  "impl.thrown", th + " by listener.");
        }
    }


/**
 * <A NAME="SD_SENDBYTEARRAYEVENT"></A>
 * <EM>sendByteArrayEvent</EM>
 *
 * @param eventListener
 */

    private void
    sendByteArrayEvent(ByteArrayListener eventListener) {
        ByteArrayEvent event = new ByteArrayEvent(session, clientName,
                                                  (ByteArray) manageable, type);

        if (ListenerMessage_Debug) {
            debug("ListenerMessage: sendByteArrayEvent:" +
                  " event listener: " + eventListener);
        }

        switch (type ) {
            case ByteArrayEvent.JOINED:
                eventListener.byteArrayJoined(event);
                break;

            case ByteArrayEvent.LEFT:
                eventListener.byteArrayLeft(event);
                break;

            case ByteArrayEvent.VALUE_CHANGED:
                eventListener.byteArrayValueChanged(event);
                break;

            case ByteArrayEvent.INVITED:
                eventListener.byteArrayInvited(event);
                break;

            case ByteArrayEvent.EXPELLED:
                eventListener.byteArrayExpelled(event);
                break;
        }
    }


/**
 * <A NAME="SD_SENDCHANNELEVENT"></A>
 * <EM>sendChannelEvent</EM>
 *
 * @param eventListener
 */

    private void
    sendChannelEvent(ChannelListener eventListener) {
        ChannelEvent event = new ChannelEvent(session, clientName,
                                              (Channel) manageable, type);

        if (ListenerMessage_Debug) {
            debug("ListenerMessage: sendChannelEvent:" +
                  " event listener: " + eventListener);
        }

        switch (type) {
            case ChannelEvent.JOINED:
                eventListener.channelJoined(event);
                break;

            case ChannelEvent.LEFT:
                eventListener.channelLeft(event);
                break;

            case ChannelEvent.INVITED:
                eventListener.channelInvited(event);
                break;

            case ChannelEvent.EXPELLED:
                eventListener.channelExpelled(event);
                break;

            case ChannelEvent.CONSUMER_ADDED:
                eventListener.channelConsumerAdded(event);
                break;

            case ChannelEvent.CONSUMER_REMOVED:
                eventListener.channelConsumerRemoved(event);
                break;
        }
    }


/**
 * <A NAME="SD_SENDSESSIONEVENT"></A>
 * <EM>sendSessionEvent</EM>
 *
 * @param eventListener
 */

    private void
    sendSessionEvent(SessionListener eventListener) {
        SessionEvent event = new SessionEvent(session, clientName,
                                              resourceName, type);
        SessionProxy sp = (SessionProxy) session.po.getProxy();

        if (ListenerMessage_Debug) {
            debug("ListenerMessage: sendSessionEvent:" +
                  " event listener: " + eventListener);
        }

        switch (type) {
            case SessionEvent.BYTEARRAY_CREATED:
                eventListener.byteArrayCreated(event);
                break;

            case SessionEvent.BYTEARRAY_DESTROYED:
                eventListener.byteArrayDestroyed(event);
                sp.removeByteArray(resourceName);
                break;


            case SessionEvent.CHANNEL_CREATED:
                eventListener.channelCreated(event);
                break;

            case SessionEvent.CHANNEL_DESTROYED:
                eventListener.channelDestroyed(event);
                sp.removeChannel(resourceName);
                break;


            case SessionEvent.DESTROYED:
                eventListener.sessionDestroyed(event);
                break;

            case SessionEvent.JOINED:
                eventListener.sessionJoined(event);
                break;

            case SessionEvent.LEFT:
                eventListener.sessionLeft(event);
                break;

            case SessionEvent.INVITED:
                eventListener.sessionInvited(event);
                break;

            case SessionEvent.EXPELLED:
                eventListener.sessionExpelled(event);
                break;


            case SessionEvent.TOKEN_CREATED:
                eventListener.tokenCreated(event);
                break;

            case SessionEvent.TOKEN_DESTROYED:
                eventListener.tokenDestroyed(event);
                sp.removeToken(resourceName);
                break;
        }
    }


/**
 * <A NAME="SD_SENDTOKENEVENT"></A>
 * <EM>sendTokenEvent</EM>
 *
 * @param eventListener
 */

    private void
    sendTokenEvent(TokenListener eventListener) {
        TokenEvent event = new TokenEvent(session, clientName,
                                          (Token) manageable, type);

        if (ListenerMessage_Debug) {
            debug("ListenerMessage: sendTokenEvent:" +
                  " event listener: " + eventListener);
        }

        switch (type) {
            case TokenEvent.JOINED:
                eventListener.tokenJoined(event);
                break;

            case TokenEvent.LEFT:
                eventListener.tokenLeft(event);
                break;

            case TokenEvent.GIVEN:
                eventListener.tokenGiven(event);
                break;

            case TokenEvent.REQUESTED:
                eventListener.tokenRequested(event);
                break;

            case TokenEvent.GRABBED:
                eventListener.tokenGrabbed(event);
                break;

            case TokenEvent.INHIBITED:
                eventListener.tokenGrabbed(event);
                break;

            case TokenEvent.RELEASED:
                eventListener.tokenReleased(event);
                break;

            case TokenEvent.INVITED:
                eventListener.tokenInvited(event);
                break;

            case TokenEvent.EXPELLED:
                eventListener.tokenExpelled(event);
                break;
        }
    }


/**
 * <A NAME="SD_RUN"></A>
 * <EM>run</EM>
 */

    public void
    run() {
        if (ListenerMessage_Debug) {
            debug("ListenerMessage: run.");
        }

        handleMessage(null);
    }
}
