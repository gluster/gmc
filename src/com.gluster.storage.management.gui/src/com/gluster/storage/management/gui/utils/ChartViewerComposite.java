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
package com.gluster.storage.management.gui.utils;

import java.lang.reflect.InvocationTargetException;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.birt.chart.api.ChartEngine;
import org.eclipse.birt.chart.device.IDeviceRenderer;
import org.eclipse.birt.chart.device.IUpdateNotifier;
import org.eclipse.birt.chart.exception.ChartException;
import org.eclipse.birt.chart.factory.GeneratedChartState;
import org.eclipse.birt.chart.factory.Generator;
import org.eclipse.birt.chart.model.Chart;
import org.eclipse.birt.chart.model.ChartWithAxes;
import org.eclipse.birt.chart.model.ChartWithoutAxes;
import org.eclipse.birt.chart.model.attribute.Anchor;
import org.eclipse.birt.chart.model.attribute.AxisType;
import org.eclipse.birt.chart.model.attribute.Bounds;
import org.eclipse.birt.chart.model.attribute.ChartDimension;
import org.eclipse.birt.chart.model.attribute.FontDefinition;
import org.eclipse.birt.chart.model.attribute.LineAttributes;
import org.eclipse.birt.chart.model.attribute.LineStyle;
import org.eclipse.birt.chart.model.attribute.NumberFormatSpecifier;
import org.eclipse.birt.chart.model.attribute.TickStyle;
import org.eclipse.birt.chart.model.attribute.impl.BoundsImpl;
import org.eclipse.birt.chart.model.attribute.impl.ColorDefinitionImpl;
import org.eclipse.birt.chart.model.attribute.impl.FontDefinitionImpl;
import org.eclipse.birt.chart.model.attribute.impl.JavaDateFormatSpecifierImpl;
import org.eclipse.birt.chart.model.attribute.impl.LineAttributesImpl;
import org.eclipse.birt.chart.model.attribute.impl.NumberFormatSpecifierImpl;
import org.eclipse.birt.chart.model.attribute.impl.TextAlignmentImpl;
import org.eclipse.birt.chart.model.component.Axis;
import org.eclipse.birt.chart.model.component.Series;
import org.eclipse.birt.chart.model.component.impl.SeriesImpl;
import org.eclipse.birt.chart.model.data.DateTimeDataElement;
import org.eclipse.birt.chart.model.data.DateTimeDataSet;
import org.eclipse.birt.chart.model.data.NumberDataSet;
import org.eclipse.birt.chart.model.data.SeriesDefinition;
import org.eclipse.birt.chart.model.data.TextDataSet;
import org.eclipse.birt.chart.model.data.impl.DateTimeDataElementImpl;
import org.eclipse.birt.chart.model.data.impl.DateTimeDataSetImpl;
import org.eclipse.birt.chart.model.data.impl.NumberDataElementImpl;
import org.eclipse.birt.chart.model.data.impl.NumberDataSetImpl;
import org.eclipse.birt.chart.model.data.impl.SeriesDefinitionImpl;
import org.eclipse.birt.chart.model.data.impl.TextDataSetImpl;
import org.eclipse.birt.chart.model.impl.ChartWithAxesImpl;
import org.eclipse.birt.chart.model.impl.ChartWithoutAxesImpl;
import org.eclipse.birt.chart.model.layout.Legend;
import org.eclipse.birt.chart.model.layout.Plot;
import org.eclipse.birt.chart.model.type.AreaSeries;
import org.eclipse.birt.chart.model.type.PieSeries;
import org.eclipse.birt.chart.model.type.impl.AreaSeriesImpl;
import org.eclipse.birt.chart.model.type.impl.PieSeriesImpl;
import org.eclipse.birt.core.framework.PlatformConfig;
import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;

import com.ibm.icu.util.Calendar;
import com.ibm.icu.util.ULocale;

/**
 * 
 */
public final class ChartViewerComposite extends Composite implements PaintListener, IUpdateNotifier {

	public enum CHART_TYPE {
		PIE, LINE
	};

	private IDeviceRenderer deviceReader = null;
	private Chart chart = null;
	private GeneratedChartState generatedChartState = null;
	private boolean needsGeneration = true;

	private static Logger logger = Logger.getLogger(ChartViewerComposite.class.getName());

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
	public ChartViewerComposite(Composite parent, int style, String[] categories, Double[] values) {
		super(parent, style);
		init();

		chart = createPieChart(categories, values);
		addPaintListener(this);
	}

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
	public ChartViewerComposite(Composite parent, int style, Calendar[] categories, Double[] values, String unit) {
		super(parent, style);
		init();

		chart = createLineChart(categories, values, unit);
		addPaintListener(this);
	}

	public void init() {
		try {
			PlatformConfig config = new PlatformConfig();
			config.setBIRTHome(Platform.getInstallLocation().getURL().getPath());
			// Get the connection with SWT device to render the graphics.
			deviceReader = ChartEngine.instance(config).getRenderer("dv.SWT");//$NON-NLS-1$
		} catch (ChartException ex) {
			logger.log(Level.SEVERE, "Could not create Chart Renderer for SWT", ex);
		}

		addControlListener(new ControlListener() {

			public void controlMoved(ControlEvent e) {
				needsGeneration = true;
			}

			public void controlResized(ControlEvent e) {
				needsGeneration = true;
			}
		});
	}

	private Chart createLineChart(Calendar[] timestamps, Double[] values, String unit) {
		return createAreaChart(timestamps, new Double[][] {values}, unit);
	}

	/**
	 * Creates a line chart model as a reference implementation
	 * 
	 * @return An instance of the simulated runtime chart model (containing
	 *         filled datasets)
	 */
	public static final Chart createAreaChart(Calendar[] timestamps, Double[][] values, final String unit) {
		ChartWithAxes cwaLine = ChartWithAxesImpl.create();
		// Plot
		cwaLine.getBlock().setBackground(ColorDefinitionImpl.WHITE());
		Plot p = cwaLine.getPlot();
		p.getClientArea().setBackground(ColorDefinitionImpl.WHITE());
		p.setBackground(ColorDefinitionImpl.WHITE());

		// Title
		cwaLine.getTitle().getLabel().getCaption().setValue("Line Chart");//$NON-NLS-1$
		cwaLine.getTitle().setVisible(false);
		cwaLine.getTitle().getLabel().setVisible(true);
		cwaLine.getTitle().getInsets().set(0, 10, 0, 0);
		cwaLine.getTitle().setAnchor(Anchor.SOUTH_LITERAL);

		// Legend
		Legend lg = cwaLine.getLegend();
		lg.setVisible(false);
		LineAttributes lia = lg.getOutline( );
		lia.setStyle( LineStyle.SOLID_LITERAL );
		lg.getText( ).getFont( ).setSize( 10 );
		//lg.getInsets( ).set( 10, 5, 0, 0 );
		lg.getInsets( ).set( 0, 0, 0, 0 );
		lg.getOutline( ).setVisible( false );
		lg.setAnchor( Anchor.NORTH_LITERAL );

		// X-Axis
		Axis xAxisPrimary = cwaLine.getPrimaryBaseAxes()[0];
		xAxisPrimary.setType(AxisType.TEXT_LITERAL);
		DateTimeDataElement dtde = DateTimeDataElementImpl.create(timestamps[timestamps.length-1]);
		DateTimeDataElement dtde1 = DateTimeDataElementImpl.create(timestamps[0]);
		xAxisPrimary.getScale().setMax(dtde);
		xAxisPrimary.getScale().setStep((dtde.getValue() - dtde1.getValue())/ 10);
		xAxisPrimary.getScale().setMajorGridsStepNumber(timestamps.length > 10 ? timestamps.length / 10 : 1);
		//xAxisPrimary.getMajorGrid().setTickStyle(TickStyle.ABOVE_LITERAL);
		xAxisPrimary.getMajorGrid().getTickAttributes().setVisible(false);
		xAxisPrimary.getMajorGrid().setLineAttributes(LineAttributesImpl.create(ColorDefinitionImpl.GREY(), LineStyle.SOLID_LITERAL, 1));
		xAxisPrimary.getTitle().setVisible(false);
		xAxisPrimary.getTitle().getInsets().set(1, 1, 1, 1);
		xAxisPrimary.getLabel().getInsets().set(1, 1, 1, 1);
		xAxisPrimary.getLabel().getCaption().setFont(createChartFont());
		xAxisPrimary.setFormatSpecifier( JavaDateFormatSpecifierImpl.create( "HH:mm" ) );

		// Y-Axis
		Axis yAxisPrimary = cwaLine.getPrimaryOrthogonalAxis(xAxisPrimary);
		yAxisPrimary.getScale().setMax(NumberDataElementImpl.create(100));
		yAxisPrimary.getScale().setMin(NumberDataElementImpl.create(0));
		yAxisPrimary.setGapWidth(0);
		yAxisPrimary.getScale().setStep(20);
		yAxisPrimary.getScale().setMajorGridsStepNumber(1);
		yAxisPrimary.getMajorGrid().setTickStyle(TickStyle.LEFT_LITERAL);
		yAxisPrimary.getMajorGrid().setLineAttributes(LineAttributesImpl.create(ColorDefinitionImpl.GREY(), LineStyle.SOLID_LITERAL, 1));
		yAxisPrimary.getLabel().setVisible(true);
		yAxisPrimary.getLabel().getCaption().setFont(createChartFont());
		yAxisPrimary.setFormatSpecifier(new NumberFormatSpecifierImpl() {
			@Override
			public String getSuffix() {
				return " " + unit;
			}
		});
		
		// Data Set
		DateTimeDataSet categoryValues = DateTimeDataSetImpl.create(timestamps);

		// X-Series
		Series seCategory = SeriesImpl.create();
		seCategory.setDataSet(categoryValues);
		SeriesDefinition sdX = SeriesDefinitionImpl.create();

		xAxisPrimary.getSeriesDefinitions().add(sdX);
		sdX.getSeries().add(seCategory);

		SeriesDefinition sdY = SeriesDefinitionImpl.create();
		sdY.getSeriesPalette().shift(-3);
		yAxisPrimary.getSeriesDefinitions().add(sdY);

		for (int i = 0; i < values.length; i++) {
			// Y-Sereis
			AreaSeries ls = (AreaSeries) AreaSeriesImpl.create();
			// LineSeries ls = (LineSeries) LineSeriesImpl.create();
			
			NumberDataSet orthoValues = NumberDataSetImpl.create(values[i]);
			ls.setDataSet(orthoValues);
			ls.getLineAttributes().setColor(ColorDefinitionImpl.create(50, 50, 255));
//			for (int j = 0; j < ls.getMarkers().size(); j++) {
//				( (Marker) ls.getMarkers( ).get( j ) ).setType( MarkerType.CIRCLE_LITERAL);
//				((Marker) ls.getMarkers().get(j)).setVisible(true);
//			}
			ls.setTranslucent(true);
			// don't show values on each point on the line chart
			ls.getLabel().setVisible(false);
			sdY.getSeries().add(ls);
		}
		return cwaLine;
	}

	public static FontDefinition createChartFont() {
		return FontDefinitionImpl.create("Serif", 8, false, false, false, false, false, 0d, TextAlignmentImpl.create());
	}

	/**
	 * @param categories
	 *            Categories of the pie chart
	 * @param values
	 *            Values of each category in the pie chart
	 * @return The chart object created for given categories and values
	 */
	public static final Chart createPieChart(String[] categories, Double[] values) {
		ChartWithoutAxes pieChart = ChartWithoutAxesImpl.create();

		// Plot
		pieChart.setSeriesThickness(10);
		pieChart.setDimension(ChartDimension.TWO_DIMENSIONAL_WITH_DEPTH_LITERAL);
		pieChart.getBlock().setBackground(ColorDefinitionImpl.WHITE());
		Plot p = pieChart.getPlot();

		p.getClientArea().setBackground(null);
		p.getClientArea().getOutline().setVisible(false);
		p.getOutline().setVisible(false);

		// Legend
		Legend lg = pieChart.getLegend();
		lg.setMaxPercent(0.7);
		lg.getText().getFont().setSize(9);
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
		sd.setSeriesPalette(new GlusterChartPalette());
		sd.getSeriesPalette().shift(0);
		sd.getSeries().add(seCategory);

		// Orthogonal Series
		PieSeries sePie = (PieSeries) PieSeriesImpl.create();
		sePie.setRatio(0.60);
		sePie.setDataSet(seriesOneValues);
		sePie.setSeriesIdentifier("Chart");//$NON-NLS-1$
		sePie.getTitle().setVisible(false); // no title
		sePie.getLabel().setVisible(false); // no label (values)
		sePie.setExplosion(0); // no gap between the pie slices

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
		bo.scale(71d / deviceReader.getDisplayServer().getDpiResolution());

		Generator gr = Generator.instance();
		if (needsGeneration) {
			needsGeneration = false;
			try {
				generatedChartState = gr.build(deviceReader.getDisplayServer(), chart, bo, null, null, null);
			} catch (ChartException ce) {
				ce.printStackTrace();
			}
		}

		try {
			gr.render(deviceReader, generatedChartState);
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