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

package org.tigr.util.awt;

import java.awt.*;

public final class GBA
	{
	public static final int B = GridBagConstraints.BOTH;
	public static final int C = GridBagConstraints.CENTER;
	public static final int E = GridBagConstraints.EAST;
	public static final int H = GridBagConstraints.HORIZONTAL;
	public static final int NONE = GridBagConstraints.NONE;
	public static final int N = GridBagConstraints.NORTH;
	public static final int NE = GridBagConstraints.NORTHEAST;
	public static final int NW = GridBagConstraints.NORTHWEST;
	public static final int RELATIVE = GridBagConstraints.RELATIVE;
	public static final int REMAINDER = GridBagConstraints.REMAINDER;
	public static final int S = GridBagConstraints.SOUTH;
	public static final int SE = GridBagConstraints.SOUTHEAST;
	public static final int SW= GridBagConstraints.SOUTHWEST;
	public static final int V = GridBagConstraints.VERTICAL;
	public static final int W = GridBagConstraints.WEST;
	
	private static GridBagConstraints c = new GridBagConstraints();
	
	public void add(Container container, Component component, int x, int y, int width, int height)
		{
		c.gridx = x;
		c.gridy = y;
		c.gridwidth = width;
		c.gridheight = height;
		c.weightx = 0;
		c.weighty = 0;
		c.fill = GBA.NONE;
		c.anchor = GBA.C;
		c.insets = new Insets(0, 0, 0, 0);
		c.ipadx = 0;
		c.ipady = 0;
		container.add(component, c);
		}
	
	public void add(Container container, Component component, int x, int y, int width, int height,
					int weightx, int weighty, int fill, int anchor)
		{
		c.gridx = x;
		c.gridy = y;
		c.gridwidth = width;
		c.gridheight = height;
		c.weightx = weightx;
		c.weighty = weighty;
		c.fill = fill;
		c.anchor = anchor;
		c.insets = new Insets(0, 0, 0, 0);
		c.ipadx = 0;
		c.ipady = 0;
		container.add(component, c);
		}
	
	public void add(Container container, Component component, int x, int y, int width, int height,
					int weightx, int weighty, int fill, int anchor, Insets insets, int ipadx, int ipady)
		{
		c.gridx = x;
		c.gridy = y;
		c.gridwidth = width;
		c.gridheight = height;
		c.weightx = weightx;
		c.weighty = weighty;
		c.fill = fill;
		c.anchor = anchor;
		c.insets = insets;
		c.ipadx = ipadx;
		c.ipady = ipady;
		container.add(component, c);
		}

	public void add(Container container, Component component, int x, int y, int width, int height,
					int weightx, int weighty, int fill, int anchor, int top, int left, int bottom, int right)
		{
		c.gridx = x;
		c.gridy = y;
		c.gridwidth = width;
		c.gridheight = height;
		c.weightx = weightx;
		c.weighty = weighty;
		c.fill = fill;
		c.anchor = anchor;
		c.insets = new Insets(top, left, bottom, right);
		c.ipadx = 0;
		c.ipady = 0;
		container.add(component, c);
		}

	public void add(Container container, Component component, int x, int y, int width, int height,
					int weightx, int weighty, int fill, int anchor, int top, int left, int bottom, int right, 
					int ipadx, int ipady)
		{
		c.gridx = x;
		c.gridy = y;
		c.gridwidth = width;
		c.gridheight = height;
		c.weightx = weightx;
		c.weighty = weighty;
		c.fill = fill;
		c.anchor = anchor;
		c.insets = new Insets(top, left, bottom, right);
		c.ipadx = ipadx;
		c.ipady = ipady;
		container.add(component, c);
		}
	}