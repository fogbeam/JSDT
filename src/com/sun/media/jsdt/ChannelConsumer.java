
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

import java.io.Serializable;

/**
 * The Channel Consumer interface.
 *
 * @version     2.3 - 25th October 2017
 * @author      Rich Burridge
 * @since       JSDT 1.0
 */

public interface
ChannelConsumer extends Serializable {

/**
 * <A NAME="SD_DATARECEIVED"></A>
 * is called when Data is received for this Client on the given Channel.
 *
 * <P>The Data object received is to a copy of the Client Data which this
 * consumer can do with as they require.
 *
 * <P><EM>IMPORTANT NOTE:</EM>
 *
 * <P>This method can potentially be called multiple times concurrently. To
 * protect again this, classes which implement the ChannelConsumer interface
 * should make sure they use the <EM>synchronized</EM> keyword. Ie:
 *
 * <PRE>
 *     public synchronized void
 *     dataReceived(Data data);
 * </PRE>
 *
 * @param data the Data which can be of unlimited size.
 */

    void
    dataReceived(Data data);
}
