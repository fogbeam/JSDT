
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

package examples.stock;

import com.sun.media.jsdt.*;

/**
 * Stock quote client. Uses yahoo to obtain stock quote information.
 *
 * @version     2.3 - 20th November 2017
 * @author      Amith Yamasani
 * @author      Rich Burridge
 */

public class
StockClient implements Client, StockDebugFlags {

    protected final String name;

    public
    StockClient(String name) {
        this.name = name;
    }


    public Object
    authenticate(AuthenticationInfo info) {
        if (StockClient_Debug) {
            System.err.println("StockClient: authenticate.");
        }
        return(null);
    }


    public String
    getName() {
        return(name);
    }
}
