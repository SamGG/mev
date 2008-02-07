package org.tigr.microarray.mev.file.agilent;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
/**
 */
public final class PropertyFactory {
    /**
     */
    public static Properties readProperties( String bundle, Properties props ) throws IOException {
        props = PropertyFactory.getPropertyBundle( bundle );
        return( props );
    }
    /**
     * Retrieves property file.
     * <P>
     * @param String sBundle    - Name (or path) of property file
     * @return Properties       - Properties object
     * @exception IOException   - If file cannot be accessed
     */
    public static Properties getPropertyBundle( String sBundle ) throws IOException {
        Properties p = new Properties();
//        InputStream is = ClassLoader.getSystemResourceAsStream( sBundle );
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(sBundle);
        try {
            p.load( is );
        } finally {
            try {
                if ( is != null ) { is.close(); }
            } catch ( IOException e ) {}
        }
        return( p );
    }
    /**
     */
    public static double getDoubleProperty( Properties pOb, String sKey ) {
        double dVal = 0;
        try {
            String sVal = pOb.getProperty( sKey );
            if ( sVal != null ) {
                dVal = Double.valueOf( sVal ).doubleValue();
            }
        } catch ( Throwable t ) { }
        return( dVal );
    }
    /**
     */
    public static long getLongProperty( Properties pOb, String sKey ) {
        long lVal = 0;
        try {
            String sVal = pOb.getProperty( sKey );
            if ( sVal != null ) {
                lVal = Long.valueOf( sVal ).longValue();
            }
        } catch ( Throwable t ) { }
        return( lVal );
    }
    /**
     */
    public static boolean getBooleanProperty( Properties pOb, String sKey ) {
        String value = pOb.getProperty( sKey );
        return((boolean)( value != null && value.equals( "true" )));
    }
    /**
     */
    public static int getIntProperty( Properties pOb, String sKey ) {
        int lVal = 0;
        try {
            String sVal = pOb.getProperty( sKey );
            if ( sVal != null ) {
                lVal = Integer.valueOf( sVal ).intValue();
            }
        } catch ( Throwable t ) { }
        return( lVal );
    }
}
