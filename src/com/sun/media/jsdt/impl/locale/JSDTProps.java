
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

package com.sun.media.jsdt.impl.locale;

import java.util.ListResourceBundle;

/**
 * JSDT properties for C locale.
 *
 * @version     2.3 - 6th November 2017
 * @author      Rich Burridge
 */

public class
JSDTProps extends ListResourceBundle {

    private static final Object[][] contents = {
        { "exception.no.such.byte.array",     "no such byte array"        },
        { "exception.no.such.channel",        "no such channel"           },
        { "exception.no.such.client",         "no such client"            },
        { "exception.no.such.consumer",       "no such consumer"          },
        { "exception.no.such.host",           "no such host"              },
        { "exception.no.such.listener",       "no such listener"          },
        { "exception.no.such.manager",        "no such manager"           },
        { "exception.no.such.session",        "no such session"           },
        { "exception.no.such.token",          "no such token"             },
        { "exception.consumer.exists",        "consumer exists"           },
        { "exception.manager.exists",         "manager exists"            },
        { "exception.permission.denied",      "permission.denied"         },
        { "exception.client.not.grabbing",    "client not grabbing"       },
        { "exception.client.not.released",    "client not released"       },
        { "exception.invalid.client",         "invalid client"            },
        { "exception.invalid.url",            "invalid url"               },
        { "exception.no.registry",            "no registry"               },
        { "exception.already.bound",          "already bound"             },
        { "exception.not.bound",              "not bound"                 },
        { "exception.name.in.use",            "name in use"               },
        { "exception.connection.error",       "connection error"          },
        { "exception.port.in.use",            "port in use"               },
        { "exception.timed.out",              "timed out"                 },
        { "exception.unknown",                "unknown"                   },

        { "impl.invalid.response",            " invalid response."        },
        { "impl.unknown.exception.type",      " unknown exception type: " },
        { "impl.unknown.type",                " unknown type: "           },
        { "impl.unknown.action",              " unknown action: "         },
        { "impl.action",                      " action: "                 },
        { "impl.thrown",                      " thrown: "                 },
        { "impl.subclass",      "Should be over-ridden by it's subclass." },
        { "impl.cannot.destroy.client",       " cannot destroy client: "  },
        { "impl.cannot.invite.client",        " cannot invite client: "   },
        { "impl.cannot.find",                 " cannot find: "            },
        { "impl.should.have.waited",  " - client should have waited for this reply." },
        { "impl.registry",  "Unable to start Registry; perhaps there is already one running..." },
        { "impl.read",                        " read: "                   },
        { "impl.expected",                    " expected: "               },
        { "impl.reason",                      " reason: "                 },
        };


    public Object[][]
    getContents() {
        return(contents);
    }
}
