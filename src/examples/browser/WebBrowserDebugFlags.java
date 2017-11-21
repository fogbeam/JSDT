
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

package examples.browser;

/**
 * Web Browser example debug flags interface.
 *
 * @version     2.3 - 3rd November 2017
 * @author      Rich Burridge
 */

public
interface WebBrowserDebugFlags {

    boolean Browser_Debug      = false;
    boolean BrowserFrame_Debug = false;
    boolean LocPanel_Debug     = false;
    boolean NavPanel_Debug     = false;
    boolean Student_Debug      = false;
    boolean Teacher_Debug      = false;
    boolean WebBrowser_Debug   = false;
    boolean WebClient_Debug    = false;
    boolean WebConsumer_Debug  = false;
    boolean WebListener_Debug  = false;
}
