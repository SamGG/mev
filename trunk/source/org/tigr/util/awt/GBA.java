/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/

package org.tigr.util.awt;

import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.io.Serializable;

public final class GBA implements Serializable {
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
