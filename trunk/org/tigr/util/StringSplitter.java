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