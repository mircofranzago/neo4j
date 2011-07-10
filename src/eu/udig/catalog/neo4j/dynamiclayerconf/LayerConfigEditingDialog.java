package eu.udig.catalog.neo4j.dynamiclayerconf;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * GUI for the LayerConfig editing
 * 
 * @author Mirco Franzago for GSoC 2011
 */
class LayerConfigEditingDialog extends TitleAreaDialog {

	Text queryText;
	Text nameText;
	Combo typeCombo;

	boolean cql;
	
	String name = "";
	String query= "";
	int type = 0;

	public LayerConfigEditingDialog(Shell parentShell, boolean cql) {
		super(parentShell);
		this.cql = cql;
	} 

	public LayerConfigEditingDialog(Shell parentShell, boolean cql, String name, int type, String query) {
		this(parentShell, cql);
		this.name = name;
		this.type = type;
		this.query = query;
	} 

	public void create() {
		super.create();
		setTitle("Add/Edit Dynamic Layer");
		if (cql) setMessage("Write the CQL query to add as new dynamic layer");
		else setMessage("Write the tags filter expression: comma-separeted key=value pairs \nFor example: key1=value1 , key2=value2");
	}

	protected Control createDialogArea(Composite parent) {
		final Composite area = new Composite(parent, SWT.NULL);
		final GridLayout gridLayout = new GridLayout(2, false);
		area.setLayout(gridLayout);

		Label labelName = new Label(area, SWT.NULL);
		labelName.setText("Dynamic Layer Name: ");

		nameText = new Text(area, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL);
		final GridData gridData = new GridData();
		gridData.widthHint = 400;
		gridData.heightHint = 20;
		nameText.setLayoutData(gridData);
		nameText.setText(name);

		Label labelType = new Label(area, SWT.NULL);
		labelType.setText("Geometry type: ");

		typeCombo = new Combo(area, SWT.READ_ONLY);
		typeCombo.setItems(new String[]{"Geometry", "Point", "Linestring", "Polygon", "MultiPoint", "MultiLinetring", "MultiPolygon"});
		typeCombo.select(type);

		Label labelEditor = new Label(area, SWT.NULL);
		if (cql)	labelEditor.setText("CQL Query: ");
		else labelEditor.setText("Tags Filter: ");

		queryText = new Text(area, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL);
		queryText.setLayoutData(gridData);
		queryText.setText(query);

		return area;
	}
	
	protected void okPressed() {
		name = nameText.getText();
		query= queryText.getText();
		type = typeCombo.getSelectionIndex();
		close();
	}

	public String getName() {
		return name;
	}

	public String getQuery() {
		return query;
	}

	public int getType() {
		return type;
	}
	
}
