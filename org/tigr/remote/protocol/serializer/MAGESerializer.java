/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: MAGESerializer.java,v $
 * $Revision: 1.1.1.1 $
 * $Date: 2003-08-21 21:04:23 $
 * $Author: braisted $
 * $State: Exp $
 */
package org.tigr.remote.protocol.serializer;

import java.io.OutputStream;
import java.io.PrintStream;
import java.io.IOException;

import java.util.Iterator;
import java.util.Map;

import org.tigr.util.FloatMatrix;
import org.tigr.microarray.mev.cluster.Cluster;
import org.tigr.microarray.mev.cluster.algorithm.*;

class MAGESerializer {

    private final static int BREAK_AFTER = 10*1024;

    /**
     * Constructs a <code>MAGESerializer</code> with specified print stream.
     */
    public MAGESerializer( PrintStream out  ) {
        m_out = out;
    }

    /**
     * Serialize an <code>AlgorithmData</code> into MAGE format.
     */
    public void serialize( AlgorithmData data ) {
        m_out.println( "<MAGE-ML identifier = \"TIGR:Clustering Request\">");
        m_ind.inc(); m_ind.print( m_out );

        if (( data.getMatrixes().size()   > 0 ) ||
            ( data.getProperties().size() > 0 )) {
            // MATRIXES START here
            m_out.println("<BioAssayData_package>");
            m_ind.inc(); m_ind.print( m_out );
            m_out.println("<BioAssayData_assnlist>");
            m_ind.inc(); m_ind.print( m_out );

            for (Iterator iter = data.getMatrixes().entrySet().iterator(); iter.hasNext();) {
                serializeMatrix( (Map.Entry)iter.next()  );
            }

            serializeParams( data.getParams() );

            m_ind.dec(); m_ind.print( m_out );
            m_out.println("</BioAssayData_assnlist>");
            m_ind.dec(); m_ind.print( m_out );
            m_out.println("</BioAssayData_package>");
            m_ind.dec(); m_ind.print( m_out );
            m_out.println("</MAGE-ML>");
            // MATRIXES END here
        }
    }

    /**
     * Serialize matrix of a microarray values.
     */
    private void serializeMatrix( Map.Entry  s ) {
        FloatMatrix mtx = (FloatMatrix)s.getValue();
        String name     =  Util.escape( (String)s.getKey() );

        m_ind.inc(); m_ind.print( m_out );

        m_out.println("<DerivedBioAssayData identifier =\"" + name + "\" >");
        m_ind.inc(); m_ind.print( m_out );
        m_out.println("<BioDataValues_assn>");
        m_ind.inc(); m_ind.print( m_out );
        m_out.println("<BioDataCube>");
        m_ind.inc(); m_ind.print( m_out );
        m_out.println("<PropertySets_assnlist>");
        m_ind.inc(); m_ind.print( m_out );

        m_out.println("<NameValueType name=\"cols\" value=\"" + mtx.getColumnDimension() + "\" />" );
        m_ind.print( m_out );

        m_out.println("<NameValueType name=\"rows\" value=\"" + mtx.getRowDimension() + "\" />" );
        m_ind.print( m_out );

        m_ind.dec(); m_ind.print( m_out );
        m_out.println("</PropertySets_assnlist>");
        m_out.println(
                     "         <BioAssayDimension_assnref> \n" +
                     "              <BioAssayDimension_ref identifier=\"UNDEFINED\"/> \n" +
                     "          </BioAssayDimension_assnref> \n" +
                     "          <DesignElementDimension_assnref> \n" +
                     "            <DesignElementDimension_ref identifier=\"UNDEFINED\"/> \n" +
                     "          </DesignElementDimension_assnref> \n" +
                     "          <QuantitationTypeDimension_assnref> \n" +
                     "             <QuantitationTypeDimension_ref identifier=\"UNDEFINED\"/> \n" +
                     "          </QuantitationTypeDimension_assnref> \n" +
                     "          <DataInternal_assn> \n" +
                     "             <DataInternal>");

        float[][] arr = mtx.getArray();

        int maxCols = mtx.getColumnDimension();
        int maxRows = mtx.getRowDimension();
        int br = 0;
        float toPrint;

        for (int j = 0; j < maxRows; j++) {
            for (int i = 0; i < maxCols; i ++) {
                toPrint = arr[j][i];
                if (Float.isNaN( toPrint  ))
                    m_out.print("NaN ");
                else {
                    m_out.print( toPrint ); m_out.print(' ');
                }
                br++;
                if (br == BREAK_AFTER) {
                    br = 0;
                    m_out.print("\r\n<?break?>\r\n");
                }
            }
        }

        m_out.println(
                     "             </DataInternal> \n" +
                     "          </DataInternal_assn>"
                     );
        m_ind.dec(); m_ind.print( m_out );
        m_out.println("</BioDataCube>");
        m_ind.dec(); m_ind.print( m_out );
        m_out.println("</BioDataValues_assn>");
        m_ind.dec(); m_ind.print( m_out );
        m_out.println("</DerivedBioAssayData>");
    }

    /**
     * Serialize an algorithm parameters.
     */
    private void serializeParams( AlgorithmParameters params ) {
        m_ind.print( m_out );
        m_out.println("<DerivedBioAssayData identifier=\"parameters\">");
        m_ind.print( m_out );
        m_out.println("   <PropertySets_assnlist>");
        m_ind.print( m_out );
        for (Iterator iter = params.entrySet().iterator(); iter.hasNext();) {
            Map.Entry entry = (Map.Entry)iter.next();
            String name  = Util.escape(  (String)entry.getKey()  );
            String value = Util.escape(  (String)entry.getValue()  );

            m_ind.print( m_out );
            m_out.println("        <NameValueType name=\"" + name + "\" value=\"" + value + "\" />");
        }
        m_ind.print( m_out );
        m_out.println("   </PropertySets_assnlist>");
        m_ind.print( m_out );
        m_out.println("</DerivedBioAssayData>");
    }

    // Not implemented since we are not going to pass clusters to a server
    private void serializeCluster( Cluster s ) {}

    private PrintStream m_out;
    private XMLIndent m_ind = new XMLIndent();
}


