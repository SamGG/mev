/*
Copyright @ 2001-2002, The Institute for Genomic Research (TIGR).  
All rights reserved.

This software is provided "AS IS".  TIGR makes no warranties, express
or implied, including no representation or warranty with respect to
the performance of the software and derivatives or their safety,
effectiveness, or commercial viability.  TIGR does not warrant the
merchantability or fitness of the software and derivatives for any
particular purpose, or that they may be exploited without infringing
the copyrights, patent rights or property rights of others. TIGR shall
not be liable for any claim, demand or action for any loss, harm,
illness or other damage or injury arising from access to or use of the
software or associated information, including without limitation any
direct, indirect, incidental, exemplary, special or consequential
damages.

This software program may not be sold, leased, transferred, exported
or otherwise disclaimed to anyone, in whole or in part, without the
prior written consent of TIGR.
*/

package org.tigr.util;

import java.util.Vector;

public class StringSplitter
	{
	private String string, delimiter;
	private int nextTokenIndex;
	private Vector tokens;
	
	public StringSplitter(String str, String delim)
		{
		try
			{
			string = new String(str);
			tokens = new Vector();
			delimiter = delim;
			int first = 0, last;
			nextTokenIndex = 0;
			
			while ((last = str.indexOf(delim, first)) != -1)
				{
				tokens.addElement(str.substring(first, last));
				first = last + 1;
				}
			tokens.addElement(str.substring(first));
			}
		catch (NullPointerException npe) {;}
		catch (Exception e) {System.out.println("Exception (StringSplitter.const()): " + e);}
		}
	
	public Vector getTokens() {return tokens;}
	public int countTokens() {return tokens.size();}

	public boolean hasMoreTokens()
		{
		if (nextTokenIndex < 0 || nextTokenIndex + 1 > tokens.size()) return false;
		else return true;
		}

	public String nextToken()
		{
		if (hasMoreTokens())
			{
			String token = (String) tokens.elementAt(nextTokenIndex);
			nextTokenIndex++;
			return token;
			}
		else throw new ArrayIndexOutOfBoundsException();
		}
	}