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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.birt.chart.util.CDateTime;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;

import com.gluster.storage.management.client.GlusterServersClient;
import com.gluster.storage.management.core.constants.GlusterConstants;
import com.gluster.storage.management.core.model.Cluster;
import com.gluster.storage.management.core.model.ServerStats;
import com.gluster.storage.management.core.model.ServerStatsRow;
import com.gluster.storage.management.gui.Activator;
import com.gluster.storage.management.gui.preferences.PreferenceConstants;
import com.gluster.storage.management.gui.views.ClusterSummaryView.ChartPeriodLinkListener;
import com.ibm.icu.util.Calendar;

/**
 *
 */
public class ChartUtil {
	private static final ChartUtil instance = new ChartUtil();
	private static final GUIHelper guiHelper = GUIHelper.getInstance();
	private static final int CHART_WIDTH = 350;

	private ChartUtil() {
	}

	public static ChartUtil getInstance() {
		return instance;
	}

	public Composite createAreaChartSection(FormToolkit toolkit, Composite section, ServerStats stats,
			int dataColumnIndex, String unit, String timestampFormat, ChartPeriodLinkListener listener, double maxValue) {
		List<Calendar> timestamps = new ArrayList<Calendar>();
		List<Double> data = new ArrayList<Double>();

		extractChartData(stats, timestamps, data, dataColumnIndex);

		if (timestamps.size() == 0) {
			toolkit.createLabel(section,
					"Server statistics not available!\n Check if all services are running properly on the cluster servers.");
			return null;
		}

		createAreaChart(section, timestamps.toArray(new Calendar[0]), data.toArray(new Double[0]), unit,
				timestampFormat, maxValue);

		// Calendar[] timestamps = new Calendar[] { new CDateTime(1000l*1310468100), new CDateTime(1000l*1310468400),
		// new CDateTime(1000l*1310468700),
		// new CDateTime(1000l*1310469000), new CDateTime(1000l*1310469300), new CDateTime(1000l*1310469600), new
		// CDateTime(1000l*1310469900),
		// new CDateTime(1000l*1310470200), new CDateTime(1000l*1310470500), new CDateTime(1000l*1310470800), new
		// CDateTime(1000l*1310471100),
		// new CDateTime(1000l*1310471400), new CDateTime(1000l*1310471700), new CDateTime(1000l*1310472000), new
		// CDateTime(1000l*1310472300),
		// new CDateTime(1000l*1310472600), new CDateTime(1000l*1310472900), new CDateTime(1000l*1310473200), new
		// CDateTime(1000l*1310473500),
		// new CDateTime(1000l*1310473800) };
		//
		// Double[] values = new Double[] { 10d, 11.23d, 17.92d, 18.69d, 78.62d, 89.11d, 92.43d, 89.31d, 57.39d, 18.46d,
		// 10.44d, 16.28d, 13.51d, 17.53d, 12.21, 20d, 21.43d, 16.45d, 14.86d, 15.27d };
		// createLineChart(section, timestamps, values, "%");
		createChartLinks(toolkit, section, 4, listener);
		return section;
	}

	private ChartViewerComposite createAreaChart(Composite section, Calendar timestamps[], Double values[],
			String unit, String timestampFormat, double maxValue) {
		ChartViewerComposite chartViewerComposite = new ChartViewerComposite(section, SWT.NONE, timestamps, values,
				unit, timestampFormat, maxValue);
		GridData data = new GridData(SWT.FILL, SWT.FILL, false, false);
		data.widthHint = CHART_WIDTH;
		data.heightHint = 250;
		data.verticalAlignment = SWT.CENTER;
		chartViewerComposite.setLayoutData(data);
		return chartViewerComposite;
	}

	private void extractChartData(ServerStats stats, List<Calendar> timestamps, List<Double> data, int dataColumnIndex) {
		for (ServerStatsRow row : stats.getRows()) {
			Double cpuUsage = row.getUsageData().get(dataColumnIndex);
			if (!cpuUsage.isNaN()) {
				timestamps.add(new CDateTime(row.getTimestamp() * 1000));
				data.add(cpuUsage);
			}
		}
	}

	private Composite createChartLinks(FormToolkit toolkit, Composite section, int columnCount,
			ChartPeriodLinkListener listener) {
		GridLayout layout = new org.eclipse.swt.layout.GridLayout(columnCount, false);
		layout.marginBottom = 0;
		layout.marginTop = 0;
		layout.marginLeft = (CHART_WIDTH - (50 * columnCount)) / 2;
		Composite graphComposite = toolkit.createComposite(section, SWT.NONE);
		graphComposite.setLayout(layout);
		GridData data = new GridData(SWT.FILL, SWT.FILL, false, false);
		data.widthHint = CHART_WIDTH;
		graphComposite.setLayoutData(data);

		createStatsLink(toolkit, listener, graphComposite, "1 day", GlusterConstants.STATS_PERIOD_1DAY);
		createStatsLink(toolkit, listener, graphComposite, "1 week", GlusterConstants.STATS_PERIOD_1WEEK);
		createStatsLink(toolkit, listener, graphComposite, "1 month", GlusterConstants.STATS_PERIOD_1MONTH);
		createStatsLink(toolkit, listener, graphComposite, "1 year", GlusterConstants.STATS_PERIOD_1YEAR);

		return graphComposite;
	}

	private void createStatsLink(FormToolkit toolkit, ChartPeriodLinkListener listener, Composite parent, String label,
			String statsPeriod) {
		Hyperlink link1 = toolkit.createHyperlink(parent, label, SWT.NONE);
		link1.addHyperlinkListener(listener.getInstance(statsPeriod));
		if (listener.getStatsPeriod().equals(statsPeriod)) {
			link1.setEnabled(false);
		}
	}

	public abstract class ChartPeriodLinkListener extends HyperlinkAdapter {
		protected String statsPeriod;
		protected String unit;
		protected int columnCount;
		protected double maxValue;
		protected Cluster cluster;
		protected FormToolkit toolkit;
		protected String serverName;

		public String getStatsPeriod() {
			return this.statsPeriod;
		}

		public ChartPeriodLinkListener(Cluster cluster, String serverName, String statsPeriod, String unit,
				double maxValue, int columnCount, FormToolkit toolkit) {
			this.cluster = cluster;
			this.serverName = serverName;
			this.statsPeriod = statsPeriod;
			this.unit = unit;
			this.columnCount = columnCount;
			this.maxValue = maxValue;
			this.cluster = cluster;
			this.toolkit = toolkit;
		}

		public ChartPeriodLinkListener(String serverName, String statsPeriod, FormToolkit toolkit) {
			this.statsPeriod = statsPeriod;
			this.serverName = serverName;
			this.toolkit = toolkit;
		}

		@Override
		public void linkActivated(HyperlinkEvent e) {
			super.linkActivated(e);
			// GlusterDataModelManager.getInstance().initializeAlerts(cluster);
			Composite section = ((Hyperlink) e.getSource()).getParent().getParent();
			ServerStats stats = fetchStats(serverName);
			refreshChartSection(toolkit, section, stats, statsPeriod, unit, maxValue, columnCount, this);
		}

		public abstract ChartPeriodLinkListener getInstance(String statsPeriod);

		protected abstract ServerStats fetchStats(String serverName);
	}

	public class CpuChartPeriodLinkListener extends ChartPeriodLinkListener {
		private CpuChartPeriodLinkListener(Cluster cluster, String serverName, String statsPeriod, String unit,
				double maxValue, int columnCount, FormToolkit toolkit) {
			super(cluster, serverName, statsPeriod, unit, maxValue, columnCount, toolkit);
		}

		@Override
		protected ServerStats fetchStats(String serverName) {
			IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
			preferenceStore.setValue(PreferenceConstants.P_CPU_CHART_PERIOD, statsPeriod);
			ServerStats stats;
			if (serverName == null) {
				stats = new GlusterServersClient().getAggregatedCpuStats(statsPeriod);
				cluster.setAggregatedCpuStats(stats);
			} else {
				stats = new GlusterServersClient().getCpuStats(serverName, statsPeriod);
			}
			return stats;
		}

		@Override
		public ChartPeriodLinkListener getInstance(String statsPeriod) {
			return new CpuChartPeriodLinkListener(cluster, serverName, statsPeriod, "%", 100, 4, toolkit);
		}
	}

	public class MemoryChartPeriodLinkListener extends ChartPeriodLinkListener {
		public MemoryChartPeriodLinkListener(String serverName, String statsPeriod, FormToolkit toolkit) {
			super(serverName, statsPeriod, toolkit);
		}

		private MemoryChartPeriodLinkListener(Cluster cluster, String serverName, String statsPeriod, String unit,
				double maxValue, int columnCount, FormToolkit toolkit) {
			super(cluster, serverName, statsPeriod, unit, maxValue, columnCount, toolkit);
		}

		@Override
		protected ServerStats fetchStats(String serverName) {
			IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
			preferenceStore.setValue(PreferenceConstants.P_MEM_CHART_PERIOD, statsPeriod);
			ServerStats stats = new GlusterServersClient().getMemoryStats(serverName, statsPeriod);
			return stats;
		}

		@Override
		public ChartPeriodLinkListener getInstance(String statsPeriod) {
			return new MemoryChartPeriodLinkListener(cluster, serverName, statsPeriod, "%", 100, 4, toolkit);
		}
	}

	public class NetworkChartPeriodLinkListener extends ChartPeriodLinkListener {
		private NetworkChartPeriodLinkListener(Cluster cluster, String serverName, String statsPeriod, String unit,
				double maxValue, int columnCount, FormToolkit toolkit) {
			super(cluster, serverName, statsPeriod, unit, maxValue, columnCount, toolkit);
		}

		@Override
		protected ServerStats fetchStats(String serverName) {
			IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
			preferenceStore.setValue(PreferenceConstants.P_NETWORK_CHART_PERIOD, statsPeriod);
			ServerStats stats;
			if (serverName == null) {
				stats = new GlusterServersClient().getAggregatedNetworkStats(statsPeriod);
				cluster.setAggregatedNetworkStats(stats);
			} else {
				stats = new GlusterServersClient().getNetworkStats(serverName, "eth0", statsPeriod);
			}

			return stats;
		}

		@Override
		public ChartPeriodLinkListener getInstance(String statsPeriod) {
			return new NetworkChartPeriodLinkListener(cluster, serverName, statsPeriod, "KiB/s", -1d, 4, toolkit);
		}
	}

	private void refreshChartSection(FormToolkit toolkit, Composite section, ServerStats stats, String statsPeriod,
			String unit, double maxValue, int columnCount, ChartPeriodLinkListener linkListener) {
		for (Control control : section.getChildren()) {
			if (!control.isDisposed()) {
				control.dispose();
			}
		}
		List<Calendar> timestamps = new ArrayList<Calendar>();
		List<Double> data = new ArrayList<Double>();
		extractChartData(stats, timestamps, data, 2);
		createAreaChart(section, timestamps.toArray(new Calendar[0]), data.toArray(new Double[0]), unit,
				getTimestampFormatForPeriod(statsPeriod), maxValue);
		createChartLinks(toolkit, section, columnCount, linkListener);
		section.layout();
	}

	public String getTimestampFormatForPeriod(String statsPeriod) {
		if (statsPeriod.equals(GlusterConstants.STATS_PERIOD_1DAY)) {
			return "HH:mm";
		} else if (statsPeriod.equals(GlusterConstants.STATS_PERIOD_1WEEK)) {
			return "dd-MMM HH:mm";
		} else {
			return "dd-MMM";
		}
	}
}
