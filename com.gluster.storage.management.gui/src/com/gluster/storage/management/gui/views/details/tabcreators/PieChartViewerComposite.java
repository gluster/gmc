/***********************************************************************
 * Copyright (c) 2004, 2007 Actuate Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Actuate Corporation - initial API and implementation
 ***********************************************************************/

package com.gluster.storage.management.gui.views.details.tabcreators;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.birt.chart.api.ChartEngine;
import org.eclipse.birt.chart.device.IDeviceRenderer;
import org.eclipse.birt.chart.device.IUpdateNotifier;
import org.eclipse.birt.chart.exception.ChartException;
import org.eclipse.birt.chart.factory.GeneratedChartState;
import org.eclipse.birt.chart.factory.Generator;
import org.eclipse.birt.chart.model.Chart;
import org.eclipse.birt.chart.model.ChartWithoutAxes;
import org.eclipse.birt.chart.model.attribute.Bounds;
import org.eclipse.birt.chart.model.attribute.ChartDimension;
import org.eclipse.birt.chart.model.attribute.impl.BoundsImpl;
import org.eclipse.birt.chart.model.attribute.impl.ColorDefinitionImpl;
import org.eclipse.birt.chart.model.component.Series;
import org.eclipse.birt.chart.model.component.impl.SeriesImpl;
import org.eclipse.birt.chart.model.data.NumberDataSet;
import org.eclipse.birt.chart.model.data.SeriesDefinition;
import org.eclipse.birt.chart.model.data.TextDataSet;
import org.eclipse.birt.chart.model.data.impl.NumberDataSetImpl;
import org.eclipse.birt.chart.model.data.impl.SeriesDefinitionImpl;
import org.eclipse.birt.chart.model.data.impl.TextDataSetImpl;
import org.eclipse.birt.chart.model.impl.ChartWithoutAxesImpl;
import org.eclipse.birt.chart.model.layout.Legend;
import org.eclipse.birt.chart.model.layout.Plot;
import org.eclipse.birt.chart.model.type.PieSeries;
import org.eclipse.birt.chart.model.type.impl.PieSeriesImpl;
import org.eclipse.birt.core.framework.PlatformConfig;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * 
 */
public final class PieChartViewerComposite extends Composite implements PaintListener, IUpdateNotifier {

	private IDeviceRenderer idr = null;
	private Chart cm = null;
	private GeneratedChartState gcs = null;
	private boolean bNeedsGeneration = true;

	private static Logger logger = Logger.getLogger(PieChartViewerComposite.class.getName());

	public static void main(String[] args) {
		Display display = Display.getDefault();
		Shell shell = new Shell(display);
		shell.setSize(800, 600);
		shell.setLayout(new GridLayout());

		// Data Set
		String[] categories = new String[] { "New York", "Boston", "Chicago", "San Francisco", "Dallas" };//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
		Double[] values = new Double[] { 54.65, 21d, 75.95, 91.28, 37.43 };

		PieChartViewerComposite chartViewerComposite = new PieChartViewerComposite(shell, SWT.NO_BACKGROUND,
				categories, values);

		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		data.widthHint = 250;
		data.heightHint = 250;
		chartViewerComposite.setLayoutData(data);

		shell.setText(chartViewerComposite.getClass().getName() + " [device=" //$NON-NLS-1$
				+ chartViewerComposite.idr.getClass().getName() + "]");//$NON-NLS-1$
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}

	/**
	 * Get the connection with SWT device to render the graphics.
	 */
	PieChartViewerComposite(Composite parent, int style, String[] categories, Double[] values) {
		super(parent, style);
		try {
			PlatformConfig config = new PlatformConfig();
			config.setBIRTHome(Platform.getInstallLocation().getURL().getPath());
			idr = ChartEngine.instance(config).getRenderer("dv.SWT");//$NON-NLS-1$
		} catch (ChartException ex) {
			logger.log(Level.SEVERE, "Could not create Chart Renderer for SWT", ex);
		}
		addControlListener(new ControlListener() {

			public void controlMoved(ControlEvent e) {
				bNeedsGeneration = true;
			}

			public void controlResized(ControlEvent e) {
				bNeedsGeneration = true;
			}
		});
		cm = createPieChart(categories, values);
		addPaintListener(this);
	}

	/**
	 * Creates a pie chart model as a reference implementation
	 * 
	 * @return An instance of the simulated runtime chart model (containing filled datasets)
	 */
	public static final Chart createPieChart(String[] categories, Double[] values) {
		ChartWithoutAxes pieChart = ChartWithoutAxesImpl.create();

		// Plot
		pieChart.setSeriesThickness(2);
		pieChart.setDimension(ChartDimension.TWO_DIMENSIONAL_WITH_DEPTH_LITERAL);
		pieChart.getBlock().setBackground(ColorDefinitionImpl.WHITE());
		Plot p = pieChart.getPlot();
		p.getClientArea().setBackground(null);
		p.getClientArea().getOutline().setVisible(false);
		p.getOutline().setVisible(false);

		// Legend
		Legend lg = pieChart.getLegend();
		lg.getText().getFont().setSize(8);
		lg.setBackground(null);
		lg.getOutline().setVisible(false);
		lg.setVisible(true);

		// Title
		pieChart.getTitle().getLabel().getCaption().setValue("Pie Chart");//$NON-NLS-1$
		pieChart.getTitle().getOutline().setVisible(false);
		pieChart.getTitle().setVisible(false);

		TextDataSet categoryValues = TextDataSetImpl.create(categories);
		NumberDataSet seriesOneValues = NumberDataSetImpl.create(values);

		// Base Series
		Series seCategory = SeriesImpl.create();
		seCategory.setDataSet(categoryValues);

		SeriesDefinition sd = SeriesDefinitionImpl.create();
		pieChart.getSeriesDefinitions().add(sd);
		sd.getSeriesPalette().shift(0);
		sd.getSeries().add(seCategory);

		// Orthogonal Series
		PieSeries sePie = (PieSeries) PieSeriesImpl.create();
		sePie.setDataSet(seriesOneValues);
		sePie.setSeriesIdentifier("Cities");//$NON-NLS-1$
		sePie.getTitle().setVisible(false);
		sePie.setExplosion(2);

		SeriesDefinition seriesDefinition = SeriesDefinitionImpl.create();
		seriesDefinition.getQuery().setDefinition("query.definition");//$NON-NLS-1$
		sd.getSeriesDefinitions().add(seriesDefinition);
		seriesDefinition.getSeries().add(sePie);

		return pieChart;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.swt.events.PaintListener#paintControl(org.eclipse.swt.events.PaintEvent)
	 */
	public final void paintControl(PaintEvent e) {
		Rectangle d = ((Composite) e.getSource()).getBounds();
		Image imgChart = new Image(this.getDisplay(), d);
		GC gcImage = new GC(imgChart);
		idr.setProperty(IDeviceRenderer.GRAPHICS_CONTEXT, gcImage);
		idr.setProperty(IDeviceRenderer.UPDATE_NOTIFIER, this);

		Bounds bo = BoundsImpl.create(0, 0, d.width, d.height);
		bo.scale(58d / idr.getDisplayServer().getDpiResolution());

		Generator gr = Generator.instance();
		if (bNeedsGeneration) {
			bNeedsGeneration = false;
			try {
				gcs = gr.build(idr.getDisplayServer(), cm, bo, null, null, null);
			} catch (ChartException ce) {
				ce.printStackTrace();
			}
		}

		try {
			gr.render(idr, gcs);
			GC gc = e.gc;
			gc.drawImage(imgChart, d.x, d.y);
		} catch (ChartException gex) {
			logger.log(Level.SEVERE, "Exception while rendering pie chart [" + gex.getMessage() + "]", gex);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.birt.chart.device.IUpdateNotifier#getDesignTimeModel()
	 */
	public Chart getDesignTimeModel() {
		return cm;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.birt.chart.device.IUpdateNotifier#getRunTimeModel()
	 */
	public Chart getRunTimeModel() {
		return gcs.getChartModel();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.birt.chart.device.IUpdateNotifier#peerInstance()
	 */
	public Object peerInstance() {
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.birt.chart.device.IUpdateNotifier#regenerateChart()
	 */
	public void regenerateChart() {
		redraw();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.birt.chart.device.IUpdateNotifier#repaintChart()
	 */
	public void repaintChart() {
		redraw();
	}
}