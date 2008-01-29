/* This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
/* Constants.java
 * Copyright (C) 2005 Amira Djebbari
 */
package org.tigr.microarray.mev.cluster.gui.impl.bn;
/**
 * The class <code>Constants</code> defines some constants such as the column numbers of
 * various fields in a tab-delimited Resourcerer file
 *
 * @author <a href="mailto:amira@jimmy.harvard.edu"></a>
 */
public class Constants {
    /**
     * The variable <code>SYMBOLS_COLUMN_NUMBER</code> denotes the official gene symbols
     * column number in the tab-delimited Resourcerer file.
     */
    public static int SYMBOLS_COLUMN_NUMBER = 5;
    /**
     * The variable <code>PROBE_IDS_COLUMN_NUMBER</code> denotes the Probe ID column number
     * in the tab-delimited Resourcerer file.
     */
    public static int PROBE_ID_COLUMN_NUMBER = 0;
    /**
     * The variable <code>GB_COLUMN_NUMBER</code> denotes the GenBank accession column number
     * in the tab-delimited Resourcerer file.
     */
    public static int GB_COLUMN_NUMBER = 2;
    /**
     * The variable <code>GO_COLUMN_NUMBER</code> denotes the GO terms column number
     * in the tab-delimited Resourcerer file.
     */
    public static int GO_COLUMN_NUMBER = 9;
    /**
     * The variable <code>RESOURCERER_ARTICLES_COLUMN_NUMBER</code> denotes the articles column number 
     * in the tab-delimited Resourcerer file.
     */
    public static int RESOURCERER_ARTICLES_COLUMN_NUMBER = 8;
    /**
     * The variable <code>RANDOM_NUMBER_GENERATOR_SEED</code> corresponds to the seed of the random number generator.
     */
    public static int RANDOM_NUMBER_GENERATOR_SEED = 2;
}


