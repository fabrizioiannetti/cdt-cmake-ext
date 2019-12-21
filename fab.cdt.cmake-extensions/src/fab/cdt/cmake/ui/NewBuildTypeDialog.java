package fab.cdt.cmake.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ListDialog;

public class NewBuildTypeDialog extends ListDialog {

	private Text configText;
	private String configName;

	public NewBuildTypeDialog(Shell parent) {
		super(parent);
		setTitle("Add Build Type");
		setMessage("Select a new Configuration to add");
		List<String> buildTypes = new ArrayList<>(Arrays.asList("Default", "Debug", "Release"));
		// remove build types that are already created and add the custom placeholder
//		store.execute((CMakeOptions options) -> {
//			for (CMakeBuildTypeOptions buildTypeOptions : options.buildTypes) {
//				if (buildTypes.contains(buildTypeOptions.buildType))
//					buildTypes.remove(buildTypeOptions.buildType);
//			}
//		});
		buildTypes.add("-- Custom --");
		
		setContentProvider(new ArrayContentProvider());
		setLabelProvider(new LabelProvider());
		setInput(buildTypes);
	}
	
	public String getConfigName() {
		return configName;
	}

	@Override
	protected Control createDialogArea(Composite container) {
		Composite dialogArea = (Composite) super.createDialogArea(container);
		Label label = new Label(dialogArea, SWT.NONE);
		label.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false));
		label.setText("Config name:");
		configText = new Text(dialogArea, SWT.SINGLE | SWT.BORDER);
		configText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		return dialogArea;
	}

	@Override
	protected void okPressed() {
		configName = configText.getText().trim();
		super.okPressed();
		if (configName.isEmpty())
			setReturnCode(CANCEL);
	}
	
	@Override
	protected Control createButtonBar(Composite parent) {
		return super.createButtonBar(parent);
	}
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		// create OK and Cancel buttons by default
		createButton(parent, IDialogConstants.OK_ID, "Create", true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}
}
