
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

import com.sun.media.jsdt.Token;
import com.sun.media.jsdt.impl.*;

/**
 * JSDT Check token status class.
 *
 * @version     2.3 - 6th November 2017
 * @author      Rich Burridge
 */

final class
CheckToken extends JSDTObject implements Runnable, httpDebugFlags {

    /* The token whose status needs checking. */
    private TokenImpl token;

    /* The previous token status value for this token. */
    private int previousStatus;


/**
 * <A NAME="SD_CHECKTOKEN"></A>
 * <EM>CheckToken</EM> is a constructor for the CheckToken class. A token
 * has been given from on client to another. We need to make sure the other
 * client accepted it. If not, then we reset it's status from GIVING back to
 * GRABBED or INHIBITED.
 *
 * @param token the token whose status needs checking.
 * @param previousStatus the previous status for this token.
 */

    public
    CheckToken(TokenImpl token, int previousStatus) {
        if (CheckToken_Debug) {
            debug("CheckToken: constructor:" +
                  " token: "           + token +
                  " previous status: " + previousStatus);
        }

        this.token          = token;
        this.previousStatus = previousStatus;
    }


/**
 * <A NAME="SD_RUN"></A>
 * <EM>run</EM> wait for a certain period of time. If the token's status is
 * still giving, then the client didn't accept the token, so reset the status
 * back to it's previous value.
 */

    public void
    run() {
        TokenServer ts = (TokenServer) token.so.getServer();
        long givePeriod = Util.getLongProperty("giveTime", giveTime);

        if (CheckToken_Debug) {
            debug("CheckToken: run.");
        }

        try {
            Thread.sleep(givePeriod);
        } catch (Exception e) {
            error("CheckToken: run: ", e);
        }

        if (ts.getTokenStatus() == Token.GIVING) {
            ts.setTokenStatus(previousStatus);
        }
    }
}
