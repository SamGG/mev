/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: MAGEResponseHandler.java,v $
 * $Revision: 1.2 $
 * $Date: 2005-02-24 20:24:08 $
 * $Author: braistedj $
 * $State: Exp $
 */
package org.tigr.remote.protocol.parser;

import java.util.LinkedList;
import java.util.StringTokenizer;

import org.tigr.microarray.mev.cluster.Cluster;
import org.tigr.microarray.mev.cluster.Node;
import org.tigr.microarray.mev.cluster.NodeValue;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmData;
import org.tigr.util.ConfMap;
import org.tigr.util.FloatMatrix;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

class MAGEResponseHandler extends ResponseHandlerBase {

    // required parameters
    private MatrixData  m_currentMatrix = null;
    private ClusterData m_currentCluster = null;
    private NodeValue   m_nodeValue = null;
    private AlgorithmData m_data = new AlgorithmData();
    private boolean m_parameters;

    /**
     * Constructs a <code>MAGEResponseHandler</code> with specified configuration.
     */
    public MAGEResponseHandler(ConfMap cfg) {
        super( cfg );
    }

    /**
     * Returns the build result.
     */
    public AlgorithmData getResult() { return m_data;}

    /**
     * An element is started.
     */
    public void startElement(String uri, String localName,
                             String name , Attributes attrs) throws SAXException {
        super.startElement(uri, localName, name, attrs);
        try {
            if (name.equals("DerivedBioAssayData")) {
                String matrixID = attrs.getValue("identifier" );
                // If this BioAssayData element has 'parameters' name, when it contains
                // parameters rather than a matrix
                if (! matrixID.equals( "parameters" ))
                    m_currentMatrix = new MatrixData( matrixID );
                else
                    m_parameters = true;
            } else
                if (name.equals( "NameValueType" )) {
                String mdimsPath[] = {"NameValueType", "PropertySets_assnlist", "BioDataCube"};
                String paramsPath[] = {"NameValueType", "PropertySets_assnlist", "DerivedBioAssayData"};
                String clusterNodeDimsPath[] = {"NameValueType", "PropertySets_assnlist", "NodeContents", "NodeContents_assnlist", "Node"};
                if (m_path.checkFromBottom( mdimsPath )) {
                    // That means that we unmarshal matrix dimensions
                    // cols and rows attributes
                    String _name =  attrs.getValue("name");
                    if (_name.equals("cols")) {
                        String val =  attrs.getValue("value") ;
                        int dimX = Integer.parseInt( val ) ;
                        m_currentMatrix.m_dims.setDimX( dimX );
                    } else
                        if (_name.equals("rows")) {
                        String val =  attrs.getValue("value") ;
                        int dimY = Integer.parseInt( val ) ;
                        m_currentMatrix.m_dims.setDimY( dimY );
                    }
                    MatrixDimensions dims = m_currentMatrix.m_dims;
                    if (dims.getDimX() != 0 && dims.getDimY() != 0) {
                        m_currentMatrix.m_matrix = new FloatMatrix( dims.getDimY(), dims.getDimX() );
                    }
                } else
                    if (m_path.checkFromBottom( paramsPath )) {
                    // That means that we unmarshal algorithm parameters
                    String _name = attrs.getValue("name" );
                    String value = attrs.getValue("value");
                    String type  = attrs.getValue("type");
                    if (type != null) {
                        if (type.equals( "vector-of-int" )) {
                            IntVectorParser p = new IntVectorParser();
                            int[] vector = null;
                            try {
                                vector = p.parse( value );
                            } catch (ParserException ee) {
                                throw new ParserException("error parsing parameter " + _name, ee );
                            }
                            m_data.addIntArray( _name, vector );
                        }
                    } else {
                        m_data.addParam(_name, value);
                    }
                } else
                    if (m_path.checkFromBottom( clusterNodeDimsPath )) {
                    // Cluster node contents ( parameters )
                    // 'nvt' means NameValueType MAGE element
                    if (m_currentCluster != null) {
                        String nvtName = attrs.getValue("name");
                        String nvtValue = attrs.getValue("value");
                        if (nvtName != null && nvtValue != null) {
                            if (nvtName.equals("features-indexes")) {
                                IntVectorParser p = new IntVectorParser();
                                try {
                                    m_currentCluster.getCurrentNode().setFeaturesIndexes( p.parse( nvtValue  ) );
                                } catch (ParserException e) {
                                    throw new ParserException("Cannot parse features indexes. " +
                                                              "Cluster: " + m_currentCluster.m_id + ". Node: " +
                                                              m_currentCluster.getCurrentNode() , e );
                                }
                            }
                            if (nvtName.equals("probes-indexes")) {
                                IntVectorParser p = new IntVectorParser();
                                try {
                                    m_currentCluster.getCurrentNode().setProbesIndexes( p.parse( nvtValue  ) );
                                } catch (ParserException e) {
                                    throw new ParserException("Cannot parse probes indexes. " +
                                                              "Cluster: " + m_currentCluster.m_id + ". Node: " +
                                                              m_currentCluster.getCurrentNode() , e );
                                }
                            } else {
                                // user-defined cluster noode content parameter
                                m_currentCluster.getCurrentNode().setProperty( nvtName, nvtValue );
                            }
                        }
                    }
                }
            } else
                if (name.equals("BioAssayDataCluster")) {
                String id = attrs.getValue("identifier");
                m_currentCluster = new ClusterData( id );
            } else
                if (name.equals("Node")) {
                String path1[] = {"Node","Nodes_assnlist","Node"};
                String path2[] = {"Node","Nodes_assnlist","BioAssayDataCluster"};
                if (( m_path.checkFromBottom( path1 ) || m_path.checkFromBottom( path2 ) ) &&
                    m_currentCluster != null) {
                    m_currentCluster.startNode(  new Node() );
                }
            } else
                if (name.equals("NodeValue")) {
                if (m_currentCluster != null) {
                    m_nodeValue = new NodeValue( attrs.getValue("name"), attrs.getValue("value"), null   );
                }
            } else
                if (name.equals("OntologyEntry")) {
                String path[] = {"OntologyEntry", "Type_assn","NodeValue"};
                if (m_path.checkFromBottom( path )) {
                    if (m_nodeValue != null) {
                        m_nodeValue.description = attrs.getValue("value");
                        String category = attrs.getValue("category");
                        if (category != null && category.equals("type")) {
                            adjustValueType( m_nodeValue, attrs.getValue("value") );
                        }
                    }
                }
            }
        } catch (Exception ex) {
            processError( ex );
        }
    }

    /**
     * An element is finished.
     */
    public void endElement(String uri, String localName, String name) throws SAXException {
        try {
            if (name.equals("DerivedBioAssayData")) {
                if (m_currentMatrix != null) {
                    // Matrix finished
                    // parse the most recent chunk
                    if (!"".equals(m_currentMatrix.m_prevChunk))
                        parseFloats( m_currentMatrix.m_prevChunk );
                    m_currentMatrix.checkFinalState();
                    m_data.addMatrix( m_currentMatrix.m_name, m_currentMatrix.m_matrix );
                    m_currentMatrix = null;
                } else {
                    // Parameters finished
                    m_parameters = false;
                }
            } else
                if (name.equals("BioAssayDataCluster")) {
                m_data.addCluster( m_currentCluster.m_id, m_currentCluster.m_cluster   );
                m_currentCluster = null;
            } else
                if (name.equals("Node")) {
                if (m_currentCluster != null)
                    m_currentCluster.endNode();
            } else
                if (name.equals("NodeValue")) {
                if (m_nodeValue != null) {
                    m_currentCluster.getCurrentNode().getValues().addNodeValue( m_nodeValue );
                    m_nodeValue  = null;
                }
            }

        } catch (Exception ex) {
            processError( ex );
        } finally {
            super.endElement( uri, localName, name );
        }
    }

    /**
     * Unlike parsing with Xerces C++, we should glue different chunks
     * to unmarshal floats correctly.
     */
    private void parseFloats( String str ) throws ParserException  {
        StringTokenizer st = new StringTokenizer(str);
        while (st.hasMoreTokens()) {
            String s = st.nextToken();
            float value = 0f;
            try {
                if (s.equals("NaN"))
                    value = Float.NaN;
                else
                    value = Float.parseFloat( s );
            } catch (NumberFormatException ex) {
                throw new ParserException("Failed to parser float: " + s + " matrix: " +
                                          m_currentMatrix.m_name + " order: " +
                                          m_currentMatrix.getNumbersRead(), ex );
            }
            m_currentMatrix.floatRead( value );
        }
    }

    /**
     * @return true if specified char is a space.
     */
    private static boolean isSpace( char ch ) {
        return Character.isWhitespace( ch ) || Character.isSpaceChar( ch );
    }

    /**
     * Glue string to be parsed as a string of floats.
     */
    private int doGlue( char ch[], int start, int length ) throws Exception {
        if (!"".equals( m_currentMatrix.m_prevChunk )) {
            StringBuffer toParse = new StringBuffer( m_currentMatrix.m_prevChunk );
            int startPos = start;
            if (!isSpace( ch[start] )) {
                // arrived chunk starts with non-space char
                int i = start;
                while ((!isSpace( ch[i] )) && (( start + length ) > i)) {
                    toParse.append( ch[i] );
                    i++;
                }
                startPos = i;
            }
            parseFloats( toParse.toString() );
            m_currentMatrix.m_prevChunk = "";
            return startPos;
        } else
            return start;
    }

    /**
     * Creates a new rest chunk.
     * @return the corrected length.
     */
    private int leaveChunk( char ch[], int start, int length ) {
        int curPos = start + length - 1;
        if (! isSpace( ch[ curPos] )) {
            int realLength = length;
            StringBuffer sb = new StringBuffer();
            int i = curPos;
            while (!isSpace( ch[i] ) && ( i > start )) {
                sb.append( ch[i] );
                --i;
                --realLength;
            }
            m_currentMatrix.m_prevChunk = sb.reverse().toString();
            return realLength;
        } else {
            m_currentMatrix.m_prevChunk = "";
            return length;
        }
    }

    /**
     * Invoked to handle chunk of an element characters.
     */
    public void characters(char ch[], int start, int length) throws SAXException {
        String prevChunk = "";
        int realStart = start;
        int realLength = length;
        try {
            String mdataPath[] = { "DataInternal", "DataInternal_assn", "BioDataCube"};
            if (m_path.checkFromBottom( mdataPath )) {
                if (m_currentMatrix == null) throw new SAXException("Program error parsing matrix");
                if (m_currentMatrix.m_matrix == null)
                    throw new SAXException("Wrong matrix - no dimensions defined");
                // We are inside DataInternal for the current matrix
                // First, we need to detect, is there a float value cut at the end of a chunk ?
                // If the last chunk character is not, probably the cut has occured
                // There is no cut, if this chunk is the last chunk or the next
                // will start with witespace.
                // But we cannot detect that here
                // So, we will treat this event as a cut.

                realStart = doGlue( ch, start, length );
                realLength = leaveChunk( ch, realStart, length + ( start - realStart) );

                String str = new String( ch, realStart, realLength );
                parseFloats( str );
            }
        } catch (Exception ex) {
            processError( ex );
        }
    }

    /**
     *  Ignore the ignorable.
     */
    public void ignorableWhitespace(char ch[], int start, int length) {}

    /**
     * Could recognize the following types: integer, float.
     */
    private void adjustValueType( NodeValue value, String type ) throws Exception {
        if (!"".equals(type)) {
            String realValue = (String)value.value;
            if (type.equals("integer"))
                value.value = new Integer( realValue );
            else
                if (type.equals("float"))
                value.value = new Float( realValue );
            else
                if (type.equals("int-array"))
                value.value = (new IntVectorParser()).parse(realValue);
            else
                if (type.equals("float-array"))
                value.value = (new FloatVectorParser()).parse(realValue);
            else
                throw new Exception("Unknown value type: " + type);
        }
    }

    /**
     * The structure to store two dimensions of a matrix.
     */
    class MatrixDimensions {

        private int m_dimX;
        private int m_dimY;

        public MatrixDimensions() {
            m_dimX = m_dimY = 0;
        }

        public int getDimX() { return m_dimX;}
        public int getDimY() { return m_dimY;}
        public void setDimX( int X ) { m_dimX = X;}
        public void setDimY( int Y ) { m_dimY = Y;}
    }

    /**
     * The structure to store a matrix data.
     */
    class MatrixData {

        private int m_numbersRead;
        public String m_name;
        public MatrixDimensions m_dims = new MatrixDimensions();
        public FloatMatrix m_matrix;
        public String m_prevChunk = "";

        /**
         * Constructs a <code>MatrixData</code> with specified name.
         */
        public MatrixData( String name ) {
            m_name = name;
            m_matrix = null;
            m_numbersRead = 0;
        }

        /**
         * Accept specified float value.
         */
        public void floatRead( float value ) throws ParserException {
            try {
                checkReadInProgressState();
                int col = m_numbersRead / m_matrix.getRowDimension();
                int row = m_numbersRead % m_matrix.getRowDimension();
                m_matrix.set(row, col, value);
                ++m_numbersRead;
            } catch (Exception e) {
                throw new ParserException("error setting matrix value", e );
            }
        }

        /**
         * Checkes if the matrix is overflow.
         */
        public void checkReadInProgressState() throws Exception {
            if (( m_numbersRead + 1 ) > m_dims.getDimX() * m_dims.getDimY())
                throw new Exception("Error reading matrix data. Has read more data than dims allow: " +
                                    "read: " + m_numbersRead );
        }

        /**
         * Checkes if number of read values is correct.
         */
        public void checkFinalState() throws Exception  {
            if (m_numbersRead != m_dims.getDimX() * m_dims.getDimY()) {
                String str = "Error reading matrix data: \n" +
                             "Dimensions: " + m_dims.getDimX() + "x" + m_dims.getDimY() +
                             " = " + m_dims.getDimX() * m_dims.getDimY() + "\n" +
                             "It has been read: " + m_numbersRead + "\n";
                throw new Exception( str );
            }
        }

        /**
         * Returns number of read values.
         */
        public int getNumbersRead() { return m_numbersRead;}
    }

    /**
     * The structure to accept a cluster data.
     */
    class ClusterData {

        public String m_id;
        public Cluster m_cluster = new Cluster();
        private LinkedList m_nodes = new LinkedList();

        /**
         * Constructs a <code>ClusterData</code> with specified id.
         */
        public ClusterData(String id ) {
            m_id = id;
        }

        /**
         * Accepts a specified node.
         */
        public void startNode( Node node ) {
            assignParentToStarted( node );
            m_nodes.addLast( node );
        }

        public void endNode() {
            m_nodes.removeLast();
        }

        public Node getCurrentNode() {
            if (m_nodes.size() == 0)
                return null;
            else
                return(Node)m_nodes.getLast();
        }

        /**
         * Assignes a parent to the specified node.
         */
        private void assignParentToStarted( Node node ) {
            Node current = getCurrentNode();
            if (current == null) {
                m_cluster.getNodeList().addNode(  node );
            } else
                current.getChildNodes().addNode( node );
        }
    }
}
