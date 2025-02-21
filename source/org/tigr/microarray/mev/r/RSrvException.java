/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
//
//  RSrvException.java
//  Klimt
//
//  Created by Simon Urbanek on Mon Aug 18 2003.
//  Copyright (c) 2003 __MyCompanyName__. All rights reserved.
//
//  $Id: RSrvException.java,v 1.2 2006-03-07 19:00:35 caliente Exp $
//

package org.tigr.microarray.mev.r;

public class RSrvException extends Exception {
    protected Rconnection conn;
    protected String err;
    protected int reqReturnCode;

    public String getRequestErrorDescription() {
        switch(reqReturnCode) {
            case 0: return "no error";
            case 2: return "R parser: input incomplete";
            case 3: return "R parser: syntax error";
            case Rtalk.ERR_auth_failed: return "authorization failed";
            case Rtalk.ERR_conn_broken: return "connection broken";
            case Rtalk.ERR_inv_cmd: return "invalid command";
            case Rtalk.ERR_inv_par: return "invalid parameter";
            case Rtalk.ERR_IOerror: return "I/O error on the server";
            case Rtalk.ERR_not_open: return "connection is not open";
            case Rtalk.ERR_access_denied: return "access denied (local to the server)";
            case Rtalk.ERR_unsupported_cmd: return "unsupported command";
            case Rtalk.ERR_unknown_cmd: return "unknown command";
            case Rtalk.ERR_data_overflow: return "data overflow, incoming data too big";
            case Rtalk.ERR_object_too_big: return "evaluation successful, but returned object is too big to transport";
            case Rtalk.ERR_out_of_mem: return "FATAL: Rserve ran out of memory, closing connection";
        }
        return "Error ("+reqReturnCode+")";
    }

    public String getMessage() {
        return super.getMessage()+" [request status: "+getRequestErrorDescription()+"]";
    }
    
    public RSrvException(Rconnection c, String msg) {
        this(c,msg,0);
    }

    public RSrvException(Rconnection c, String msg, int requestReturnCode) {
        super(msg);
        conn=c; reqReturnCode=requestReturnCode;
    }

    public int getRequestReturnCode() {
        return reqReturnCode;
    }
}
