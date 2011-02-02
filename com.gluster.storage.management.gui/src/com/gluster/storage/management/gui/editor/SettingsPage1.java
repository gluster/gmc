package com.gluster.storage.management.gui.editor;

import org.eclipse.jface.fieldassist.ComboContentAdapter;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.SimpleContentProposalProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ColumnLayout;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;

public class SettingsPage1 extends FormPage {

	public static final String ID = "com.gluster.storage.management.gui.editor.ClusterSummaryPage";

	/**
	 * Create the form page.
	 * 
	 * @param id
	 * @param title
	 */
	public SettingsPage1(String title) {
		super(ID, title);
	}

	/**
	 * Create the form page.
	 * 
	 * @param editor
	 * @param id
	 * @param title
	 * @wbp.parser.constructor
	 * @wbp.eval.method.parameter title "Some title"
	 */
	public SettingsPage1(FormEditor editor, String title) {
		super(editor, ID, title);
	}

	private void setupForm(FormToolkit toolkit, final ScrolledForm form) {
		form.setText("Settings");
		toolkit.decorateFormHeading(form.getForm());

		ColumnLayout layout = new ColumnLayout();

		// layout.topMargin = 0;
		// layout.bottomMargin = 5;
		// layout.leftMargin = 10;
		// layout.rightMargin = 10;
		// layout.horizontalSpacing = 10;
		// layout.verticalSpacing = 10;
		// layout.maxNumColumns = 4;
		// layout.minNumColumns = 1;

		form.getBody().setLayout(layout);
	}

	/**
	 * Create contents of the form.
	 * 
	 * @param managedForm
	 */
	@Override
	protected void createFormContent(IManagedForm managedForm) {
		FormToolkit toolkit = managedForm.getToolkit();
		final ScrolledForm form = managedForm.getForm();

		setupForm(toolkit, form);
		createClusterSettingsSection(toolkit, form);
		createAppSettingsSection(toolkit, form);
	}

	private Composite createSection(final ScrolledForm form,
			FormToolkit toolkit, String title, String desc, int numColumns,
			boolean collapsible) {
		int style = Section.TITLE_BAR | Section.EXPANDED;
		if (desc != null && !desc.isEmpty()) {
			style |= Section.DESCRIPTION;
		}
		if (collapsible) {
			style |= Section.TWISTIE;
		}

		Section section = toolkit.createSection(form.getBody(), style);
		section.setText(title);
		section.setDescription(desc);

		//toolkit.createCompositeSeparator(section);
		Composite client = toolkit.createComposite(section);
		GridLayout layout = new GridLayout();
		layout.marginWidth = layout.marginHeight = 10;
		layout.numColumns = numColumns;
		layout.verticalSpacing = 20;
		client.setLayout(layout);
		section.setClient(client);
		section.addExpansionListener(new ExpansionAdapter() {
			public void expansionStateChanged(ExpansionEvent e) {
				form.reflow(false);
			}
		});
		return client;
	}
	
	private void createAppSettingsSection(FormToolkit toolkit,
			final ScrolledForm form) {
		GridData layoutData = createDefaultLayoutData();

		Composite sectionClient = createSection(form, toolkit, "Application Settings", null,
				2, false);

		toolkit.createButton(sectionClient, "Enable Remote CLI?", SWT.CHECK | SWT.FLAT);
		toolkit.paintBordersFor(sectionClient);
	}
	
	private void createClusterSettingsSection(FormToolkit toolkit,
			final ScrolledForm form) {
		GridData layoutData = createDefaultLayoutData();

		Composite sectionClient = createSection(form, toolkit, "Cluster Settings", null,
				2, false);

		Label lblTimeZone = toolkit.createLabel(sectionClient,
				"Time Zone: ", SWT.NONE);
		Combo cboTimeZone = createTimeZoneCombo(sectionClient, layoutData);
		
		Label lblTimeServer = toolkit.createLabel(sectionClient,
				"Network Time GlusterServer: ", SWT.NONE);
		Text txtTimeServer = toolkit.createText(sectionClient, "pool.ntp.org", SWT.BORDER_SOLID);
		txtTimeServer.setLayoutData(layoutData);

		toolkit.paintBordersFor(sectionClient);
	}

	private GridData createDefaultLayoutData() {
		GridData layoutData = new GridData();
		layoutData.grabExcessHorizontalSpace=true;
		layoutData.minimumWidth=200;
		return layoutData;
	}

	private Combo createTimeZoneCombo(Composite sectionClient, GridData layoutData) {
		Combo cboTimeZone = new Combo(sectionClient, SWT.FLAT);
		cboTimeZone.setData(FormToolkit.KEY_DRAW_BORDER,
				FormToolkit.TEXT_BORDER);
		cboTimeZone.setLayoutData(layoutData);

		for(String timeZone : TimeZones.timeZones) {
			cboTimeZone.add(timeZone);
		}
		cboTimeZone.setText("Asia/Calcutta");
		
		SimpleContentProposalProvider proposalProvider = new SimpleContentProposalProvider(
				TimeZones.timeZones);
		proposalProvider.setFiltering(true);
		ContentProposalAdapter proposalAdapter = new ContentProposalAdapter(
				cboTimeZone, new ComboContentAdapter(), proposalProvider, null,
				null);
		proposalAdapter.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_REPLACE);

		return cboTimeZone;
	}
}
