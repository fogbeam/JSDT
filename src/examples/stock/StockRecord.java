
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
import java.io.*;

/**
 * Stock quote records on the server.
 *
 * @version     2.3 - 21st November 2017
 * @author      Amith Yamasani
 * @author      Rich Burridge
 */

public class
StockRecord implements StockDebugFlags {

    // The server-side client associated with this stock symbol.
    private final Client client;

    // The Shared byte array associated with this stock symbol.
    private final ByteArray sba;

    private boolean isValid;

    private String symbol, time, value, change, percentChange;


    public
    StockRecord(Client client, ByteArray sba) {
        if (StockRecord_Debug) {
            System.err.println("StockRecord: constructor:" +
                                " client: "            + client +
                                " shared byte array: " + sba);
        }

        this.client = client;
        this.sba    = sba;

        symbol = sba.getName();
    }


    public ByteArray
    getByteArray() {
        if (StockRecord_Debug) {
            System.err.println("StockRecord: getByteArray.");
        }

        return(sba);
    }


    public void
    setPercentChange(String change) {
        if (StockRecord_Debug) {
            System.err.println("StockRecord: setChange:" +
                                " percent change: " + change);
        }

        this.percentChange = change;
    }


    public void
    setChange(String change) {
        if (StockRecord_Debug) {
            System.err.println("StockRecord: setChange:" +
                                " change: " + change);
        }

        this.change = change;
    }


    public void
    setTime(String time)  {
        if (StockRecord_Debug) {
            System.err.println("StockRecord: setTime:" +
                                " time: " + time);
        }

        this.time = time;
    }


    public void
    setValid(boolean isValid) {
        if (StockRecord_Debug) {
            System.err.println("StockRecord: setValid:" +
                                " valid?: " + isValid);
        }

        this.isValid = isValid;
    }


    public void
    setValue(String value)  {
        if (StockRecord_Debug) {
            System.err.println("StockRecord: setValue:" +
                                " value: " + value);
        }

        this.value = value;
    }


    public void
    update() {
        ByteArrayOutputStream baos;
        DataOutputStream dos;

        if (StockRecord_Debug) {
            System.err.println("StockRecord: update.");
        }

        baos = new ByteArrayOutputStream();
        dos  = new DataOutputStream(baos);

        try {
            dos.writeUTF(symbol);
            dos.writeBoolean(isValid);
            if (isValid) {
                dos.writeUTF(time);
                dos.writeUTF(value);
                dos.writeUTF(change);
                dos.writeUTF(percentChange);
            }
            dos.flush();
        } catch (IOException e) {
            System.err.println("StockRecord: update: exception: " + e);
            if (StockRecord_Debug) {
                e.printStackTrace();
            }
        }

        try {
            sba.setValue(client, baos.toByteArray());
        } catch (JSDTException je) {
            System.err.println("StockRecord: update: exception: " + je);
            if (StockRecord_Debug) {
                je.printStackTrace();
            }
        }
    }
}
