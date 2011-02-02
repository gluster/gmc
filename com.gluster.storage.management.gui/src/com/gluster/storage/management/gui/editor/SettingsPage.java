package com.gluster.storage.management.gui.editor;

import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.bindings.keys.ParseException;
import org.eclipse.jface.fieldassist.ComboContentAdapter;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.SimpleContentProposalProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ColumnLayout;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;

import com.swtdesigner.SWTResourceManager;

public class SettingsPage extends FormPage {

	public static final String ID = "com.gluster.storage.management.gui.editor.ClusterSummaryPage";

	/**
	 * Create the form page.
	 * 
	 * @param id
	 * @param title
	 */
	public SettingsPage(String title) {
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
	public SettingsPage(FormEditor editor, String title) {
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

		// toolkit.createCompositeSeparator(section);
		Composite client = toolkit.createComposite(section);
		GridLayout layout = new GridLayout();
		layout.marginWidth = layout.marginHeight = 0;
		layout.numColumns = numColumns;
		client.setLayout(layout);
		section.setClient(client);
		new Label(client, SWT.NONE);
		section.addExpansionListener(new ExpansionAdapter() {
			public void expansionStateChanged(ExpansionEvent e) {
				form.reflow(false);
			}
		});
		return client;
	}

	private void createClusterSettingsSection(FormToolkit toolkit,
			final ScrolledForm form) {
		Composite sectionClient = createSection(form, toolkit, "Volumes", null,
				2, false);

		Label lblNumberOfVolumes = toolkit.createLabel(sectionClient,
				"Number of Volumes: ", SWT.NONE);
		Label lblVolumeCount = toolkit.createLabel(sectionClient, "12",
				SWT.NONE);
		Label lblOnline = toolkit.createLabel(sectionClient, "Online: ",
				SWT.NONE);
		Label labelOnlineCount = toolkit.createLabel(sectionClient, "9",
				SWT.NONE);
		labelOnlineCount.setForeground(SWTResourceManager
				.getColor(SWT.COLOR_DARK_GREEN));
		Label lblOffline = toolkit.createLabel(sectionClient, "Offline: ",
				SWT.NONE);
		Label lblOfflineCount = toolkit.createLabel(sectionClient, "3",
				SWT.NONE);
		lblOfflineCount.setForeground(SWTResourceManager
				.getColor(SWT.COLOR_RED));
		/*CCombo cboTimeZone = new CCombo(sectionClient, SWT.FLAT);
		cboTimeZone.setData(FormToolkit.KEY_DRAW_BORDER,
				FormToolkit.TEXT_BORDER);*/
		/*toolkit.paintBordersFor(sectionClient);

		cboTimeZone.add("Asia/Calcutta");
		try {
			ContentProposalAdapter proposalAdapter = new ContentProposalAdapter(
					cboTimeZone, new ComboContentAdapter(),
					new SimpleContentProposalProvider(
							new String[] { "Asia/Calcutta" }),
					KeyStroke.getInstance("Ctrl+Space"), null);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/

		/*Label lblTimeServer = toolkit.createLabel(sectionClient, "Network Time GlusterServer",
				SWT.NONE);*/
	}
}
