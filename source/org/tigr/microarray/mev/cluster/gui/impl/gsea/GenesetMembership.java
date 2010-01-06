package org.tigr.microarray.mev.cluster.gui.impl.gsea;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.beans.Expression;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.tigr.microarray.mev.cluster.algorithm.impl.gsea.Geneset;
import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.GSEAExperiment;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.IDisplayMenu;
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.IViewer;
import org.tigr.util.FloatMatrix;

public class GenesetMembership extends JPanel implements IViewer {

	// First create an Experiment object with number of rows equal to genes
	// present in gene sets and data.
	// Number of columns id equal to number of gene sets +1. This can be
	// obtained form GSEAGUI.

	// Second step is to create a new ExperimentHeader class which would allow
	// painting geneset names instead of sample names
	// override the drawHeader(), updateSizes()

	// Third step is to create an experimentViewer with the experiment object
	// created in step one. GSEAExperimentViewer would need an additional
	// constructor where it overrides exrimentHeader
	// with the new custom experimentHeader object. Clusters would be equal to
	// numer of rows
	// in experiment object
	// ExperimentViewer should have popup menu and rightclick disabled because
	// 1. spot information will not be able to find the correct info. Number of
	// columns is not equal to number of samples but is equal to number
	// of gene sets.
	// 2. Rows are not equal to number of genes so number of rows is lesser than
	// the original experiment object
	// 3. We will be plotting test statistic and not expression values.

	private Vector<String> unique_genes;
	private Vector<String> gene_sets;
	private Geneset[] gSets;
	private GSEAExperiment experimentObject;
	private FloatMatrix experimentMatrix;
	private int[] columns;
	private FontMetrics metrics;
	private Dimension elementSize = new Dimension(20, 5);
	public static Color missingColor = new Color(128, 128, 128);
	public static Color maskColor = new Color(255, 255, 255, 128);
	private int firstSelectedRow = -1;
	private int lastSelectedRow = -1;
	private int firstSelectedColumn = -1;
	private int lastSelectedColumn = -1;
	private Insets insets = new Insets(0, 10, 0, 0);
	private boolean isDrawBorders = true;
	private boolean useDoubleGradient = true;
	public BufferedImage posColorImage = createGradientImage(Color.black,
			Color.red);
	public BufferedImage negColorImage = createGradientImage(Color.green,
			Color.black);
	private static final float INITIAL_MAX_VALUE = 3f;
	private static final float INITIAL_MIN_VALUE = -3f;
	private float maxValue = INITIAL_MAX_VALUE;
	private float minValue = INITIAL_MIN_VALUE;
	private float midValue = 0.0f;

	public GenesetMembership(Vector<String> uniquegenes,
			Vector<String> genesets, Geneset[] gset) {
		this.unique_genes = uniquegenes;
		this.gene_sets = genesets;
		this.experimentMatrix = new FloatMatrix(this.unique_genes.size(),
				this.gene_sets.size() + 1);
		this.gSets = gset;
		createExperimentObject();
		setBackground(Color.white);
	}

	/**
	 * @return createExperimentObject creates an Experiment object with
	 * 
	 *         rows=number of unique genes present in the expression data
	 *         columns=number of gene sets
	 * 
	 *         The zeroth column will contain the sorted test statistic of all
	 *         genes in the data set. Thereafter each cell [row=gene and
	 *         column=geneset] will contain the test statistic corresponding to
	 *         that gene, if it is present in the gene set and zero otherwise
	 * 
	 * 
	 * 
	 * 
	 */
	public void createExperimentObject() {

		// Rowsize is equal to the number of genes
		int rowSize = this.unique_genes.size();
		// System.out.println("row size of Amat:"+rowSize);

		// Colsize is equal to the number of genesets
		int colSize = this.gene_sets.size();
		// System.out.println("col size of Amat:"+colSize);

		int rowIndex = 0;
		int colIndex = 0;
		int matrixColIndex = 1;

		// Loop through the gene sets
		for (colIndex = 0; colIndex < colSize; colIndex++) {
			// Get the names of all genes in the current gene set
			ArrayList<String> _genesinGeneset = new ArrayList<String>();
			_genesinGeneset = gSets[colIndex].getGenesinGeneset();

			// Loop through all genes in the current gene set
			for (int j = 0; j < _genesinGeneset.size(); j++) {
				String Gene = (String) _genesinGeneset.get(j);
				Gene = Gene.trim();

				// Loop through all unique genes in the expression data
				for (int k = 0; k < rowSize; k++) {
					String uniq_genes = (String) this.unique_genes.get(k);
					uniq_genes = uniq_genes.trim();
					// Set the zeroth and current geneset column to test
					// statistic if gene in the gene set also present in data
					// set
					if (uniq_genes.equals(Gene)) {
						this.experimentMatrix.set(k, 0, gSets[colIndex]
								.getGeneSetElement(j).getTestStat());
						this.experimentMatrix.set(k, matrixColIndex,
								gSets[colIndex].getGeneSetElement(j)
										.getTestStat());
					}

				}

			}

			matrixColIndex = matrixColIndex + 1;

		}

		experimentObject = new GSEAExperiment(this.experimentMatrix,
				createColumns(this.gene_sets.size() + 1));

	}

	/**
	 * Returns max width of sample names.
	 */
	protected int getNamesWidth(FontMetrics metrics) {
		int maxWidth = 0;
		for (int i = 0; i < this.gene_sets.size(); i++) {
			maxWidth = Math.max(maxWidth, metrics.stringWidth(this.gene_sets
					.get(i)));
		}
		return maxWidth;
	}

	/**
	 * Paints chart into specified graphics.
	 */
	public void paint(Graphics g) {
		FontMetrics metrics = g.getFontMetrics();
		Rectangle rect = g.getClipBounds();

		paint((Graphics2D) g, rect);
	}

	/**
	 * 
	 * 
	 * 
	 */

	public void paint(Graphics2D g, Rectangle rectangle) {
		super.paint(g);
		FontMetrics metrics = g.getFontMetrics();
		setFontMetric(metrics);
		// Paint the expression data points
		if (this.elementSize.getHeight() < 1)
			return;
		final int samples = experimentObject.getNumberOfSamples();

		final int top = getTopIndex(rectangle.y) + getNamesWidth(metrics);
		final int bottom = getBottomIndex(rectangle.y + rectangle.height,
				rectangle.y + rectangle.height);
		final int left = getLeftIndex(rectangle.x);
		final int right = getRightIndex(rectangle.x + rectangle.width, samples);
		int expressionRowIndex = 0;
		int expressionColIndex = 0;

		for (int column = left; column < right; column++) {

			for (int row = top; row < bottom; row++) {
				fillRectAt(g, row, column, expressionRowIndex,
						expressionColIndex);

				if (expressionRowIndex < experimentObject.getNumberOfGenes()) {
					expressionRowIndex = expressionRowIndex + 1;
				}
			}
			if (expressionColIndex < samples) {
				expressionColIndex = expressionColIndex + 1;
			}
		}

		// Paint the gene set names as column names

		// Paint the names of the genes

	}

	/**
	 * Fills rect with specified row and column.
	 */
	private void fillRectAt(Graphics g, int row, int column,
			int expressionRowIndex, int expressionColIndex) {
		if (column > (experimentObject.getNumberOfSamples() - 1))
			return;
		int x = column * elementSize.width + insets.left;
		int y = row * elementSize.height;
		boolean mask = this.firstSelectedRow >= 0 && this.lastSelectedRow >= 0
				&& (row < this.firstSelectedRow || row > this.lastSelectedRow);
		mask = (mask || this.firstSelectedColumn >= 0
				&& this.lastSelectedColumn >= 0
				&& (column < this.firstSelectedColumn || column > this.lastSelectedColumn));

		g.setColor(getColor(this.experimentObject.get(expressionRowIndex,
				expressionColIndex)));
		g.fillRect(x, y, elementSize.width, elementSize.height);
		if (mask) {
			g.setColor(maskColor);
			g.fillRect(x, y, elementSize.width, elementSize.height);
		}
		if (this.isDrawBorders) {
			g.setColor(Color.black);
			g.drawRect(x, y, elementSize.width - 1, elementSize.height - 1);
		}
	}

	private Color getColor(float value) {
		if (Float.isNaN(value)) {
			return missingColor;
		}

		float maximum;
		int colorIndex, rgb;

		if (useDoubleGradient) {
			maximum = value < midValue ? this.minValue : this.maxValue;
			colorIndex = (int) (255 * (value - midValue) / (maximum - midValue));
			if (colorIndex < 0)
				colorIndex = -colorIndex;
			colorIndex = colorIndex > 255 ? 255 : colorIndex;
			rgb = value < midValue ? negColorImage.getRGB(255 - colorIndex, 0)
					: posColorImage.getRGB(colorIndex, 0);
		} else {
			float span = this.maxValue - this.minValue;
			if (value <= minValue)
				colorIndex = 0;
			else if (value >= maxValue)
				colorIndex = 255;
			else
				colorIndex = (int) (((value - this.minValue) / span) * 255);

			rgb = posColorImage.getRGB(colorIndex, 0);
		}
		return new Color(rgb);
	}

	/**
	 * Creates a gradient image with specified initial colors.
	 */
	public BufferedImage createGradientImage(Color color1, Color color2) {
		BufferedImage image = (BufferedImage) java.awt.GraphicsEnvironment
				.getLocalGraphicsEnvironment().getDefaultScreenDevice()
				.getDefaultConfiguration().createCompatibleImage(256, 1);
		Graphics2D graphics = image.createGraphics();
		GradientPaint gp = new GradientPaint(0, 0, color1, 255, 0, color2);
		graphics.setPaint(gp);
		graphics.drawRect(0, 0, 255, 1);
		return image;
	}

	public int[][] createSpotIndices(int count) {

		int[][] spotindicies = new int[1][count];
		for (int i = 0; i < count; i++) {
			spotindicies[0][i] = i;

		}
		return spotindicies;
	}

	/**
	 * createColumns generates an integer array, that contains the number of
	 * experiments(samples). Needed to populate GSEAExperiment
	 * 
	 * 
	 */
	public int[] createColumns(int count) {
		columns = new int[count];
		for (int i = 0; i < count; i++) {
			columns[i] = i;
		}
		return columns;
	}

	public FontMetrics getFontMetric() {
		return metrics;
	}

	public void setFontMetric(FontMetrics metric) {
		metrics = metric;
	}

	private int getTopIndex(int top) {
		if (top < 0) {
			return 0;
		}
		return top / elementSize.height;
	}

	private int getLeftIndex(int left) {
		if (left < insets.left) {
			return 0;
		}
		return (left - insets.left) / elementSize.width;
	}

	private int getRightIndex(int right, int limit) {
		if (right < 0) {
			return 0;
		}
		int result = right / elementSize.width + 1;
		return result > limit ? limit : result;
	}

	private int getBottomIndex(int bottom, int limit) {
		if (bottom < 0) {
			return 0;
		}
		int result = bottom / elementSize.height + 1;
		return result > limit ? limit : result;
	}
	
	
	public JComponent getContentComponent() {
		// TODO Auto-generated method stub
		return this;
	}
	
	
	public int[][] getClusters() {
		// TODO Auto-generated method stub
		return null;
	}

	

	public JComponent getCornerComponent(int cornerIndex) {
		// TODO Auto-generated method stub
		return null;
	}

	public Experiment getExperiment() {
		// TODO Auto-generated method stub
		return experimentObject;
	}

	public int getExperimentID() {
		// TODO Auto-generated method stub
		return 0;
	}

	public Expression getExpression() {
		// TODO Auto-generated method stub
		return null;
	}

	public JComponent getHeaderComponent() {
		// TODO Auto-generated method stub
		return null;
	}

	public BufferedImage getImage() {
		// TODO Auto-generated method stub
		return null;
	}

	public JComponent getRowHeaderComponent() {
		// TODO Auto-generated method stub
		return null;
	}

	public int getViewerType() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void onClosed() {
		// TODO Auto-generated method stub

	}

	public void onDataChanged(IData data) {
		// TODO Auto-generated method stub

	}

	public void onDeselected() {
		// TODO Auto-generated method stub

	}

	public void onMenuChanged(IDisplayMenu menu) {
		// TODO Auto-generated method stub

	}

	public void onSelected(IFramework framework) {
		// TODO Auto-generated method stub

	}

	public void setExperiment(Experiment e) {
		// TODO Auto-generated method stub

	}

	public void setExperimentID(int id) {
		// TODO Auto-generated method stub

	}

}
