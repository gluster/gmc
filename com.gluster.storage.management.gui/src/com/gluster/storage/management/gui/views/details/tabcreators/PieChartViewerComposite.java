/*******************************************************************************
 * Copyright (c) 2011 Gluster, Inc. <http://www.gluster.com>
 * This file is part of Gluster Management Console.
 *
 * Gluster Management Console is free software; you can redistribute it and/or 
 * modify it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *  
 * Gluster Management Console is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License
 * for more details.
 *  
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 *******************************************************************************/
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
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;

/**
 * 
 */
public final class PieChartViewerComposite extends Composite implements
		PaintListener, IUpdateNotifier {

	private IDeviceRenderer deviceReader = null;
	private Chart chart = null;
	private GeneratedChartState generatedChartState = null;
	private boolean needsGeneration = true;

	private static Logger logger = Logger
			.getLogger(PieChartViewerComposite.class.getName());

	/**
	 * @param parent
	 *            Parent composite of this pie chart viewer composite
	 * @param style
	 *            SWT style to be used
	 * @param categories
	 *            Categories of the pie chart
	 * @param values
	 *            Values of each category in the pie chart Constructs a pie
	 *            chart viewer composite for given categories and values
	 */
	public PieChartViewerComposite(Composite parent, int style, String[] categories,
			Double[] values) {
		super(parent, style);
		try {
			PlatformConfig config = new PlatformConfig();
			config.setBIRTHome(Platform.getInstallLocation().getURL().getPath());
			// Get the connection with SWT device to render the graphics.
			deviceReader = ChartEngine.instance(config).getRenderer("dv.SWT");//$NON-NLS-1$
		} catch (ChartException ex) {
			logger.log(Level.SEVERE, "Could not create Chart Renderer for SWT",
					ex);
		}

		addControlListener(new ControlListener() {

			public void controlMoved(ControlEvent e) {
				needsGeneration = true;
			}

			public void controlResized(ControlEvent e) {
				needsGeneration = true;
			}
		});

		chart = createPieChart(categories, values);
		addPaintListener(this);
	}

	/**
	 * @param categories
	 *            Categories of the pie chart
	 * @param values
	 *            Values of each category in the pie chart
	 * @return The chart object created for given categories and values
	 */
	public static final Chart createPieChart(String[] categories,
			Double[] values) {
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
	 * @see
	 * org.eclipse.swt.events.PaintListener#paintControl(org.eclipse.swt.events
	 * .PaintEvent)
	 */
	public final void paintControl(PaintEvent e) {
		Rectangle d = ((Composite) e.getSource()).getBounds();
		Image imgChart = new Image(this.getDisplay(), d);
		GC gcImage = new GC(imgChart);
		deviceReader.setProperty(IDeviceRenderer.GRAPHICS_CONTEXT, gcImage);
		deviceReader.setProperty(IDeviceRenderer.UPDATE_NOTIFIER, this);

		Bounds bo = BoundsImpl.create(0, 0, d.width, d.height);
		bo.scale(58d / deviceReader.getDisplayServer().getDpiResolution());

		Generator gr = Generator.instance();
		if (needsGeneration) {
			needsGeneration = false;
			try {
				generatedChartState = gr.build(deviceReader.getDisplayServer(),
						chart, bo, null, null, null);
			} catch (ChartException ce) {
				ce.printStackTrace();
			}
		}

		try {
			gr.render(deviceReader, generatedChartState);
			GC gc = e.gc;
			gc.drawImage(imgChart, d.x, d.y);
		} catch (ChartException gex) {
			logger.log(Level.SEVERE, "Exception while rendering pie chart ["
					+ gex.getMessage() + "]", gex);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.birt.chart.device.IUpdateNotifier#getDesignTimeModel()
	 */
	public Chart getDesignTimeModel() {
		return chart;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.birt.chart.device.IUpdateNotifier#getRunTimeModel()
	 */
	public Chart getRunTimeModel() {
		return generatedChartState.getChartModel();
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