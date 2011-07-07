package eu.udig.catalog.neo4j.viewer;

import java.util.Iterator;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.neo4j.gis.spatial.DynamicLayer;
import org.neo4j.gis.spatial.Layer;
import org.neo4j.gis.spatial.osm.OSMLayer;
import org.neo4j.gis.spatial.DynamicLayer.LayerConfig;

import eu.udig.catalog.neo4j.ConfigureDynamicLayers;

/**
 * GUI for dynamic layers configuration
 * 
 * @author Mirco Franzago for GSoC 2011
 */

public class DynamicLayersViewer extends TitleAreaDialog {

	List list;
	Layer targetLayer; 
	java.util.List<Layer> layers;
	//ConfigureDynamicLayers configureDynamicLayers;
	boolean isConfigLayer;

	Button addButtonOSM;
	Button addButtonCQL;
	Button editButton;
	Button removeButton;
	Button exitButton;

	public DynamicLayersViewer(Shell parentShell, ConfigureDynamicLayers configureDynamicLayers, Layer targetLayer) {
		super(parentShell);
		//this.configureDynamicLayers = configureDynamicLayers;
		this.targetLayer = targetLayer;
		this.isConfigLayer = targetLayer instanceof LayerConfig;
		if (isConfigLayer)  
			this.layers = ((DynamicLayer)((LayerConfig)targetLayer).getParent()).getLayers();
		else 
			this.layers = ((DynamicLayer)targetLayer).getLayers();
	}

	public void create() {
		super.create();
		setTitle("Dynamic Layer Configuration");
		setMessage("You can add/edit/remove LayerConfig from the layer");
		validate();
	}

	protected Control createDialogArea(Composite parent) {
		// Create new composite as container
		final Composite area = new Composite(parent, SWT.NULL);
		// We use a grid layout and set the size of the margins
		final GridLayout gridLayout = new GridLayout(2, false);
		gridLayout.marginWidth = 15;
		gridLayout.marginHeight = 10;
		area.setLayout(gridLayout);
		// Now we create the list widget
		list = new List(area, SWT.BORDER | SWT.MULTI);
		// We define a minimum width for the list
		final GridData gridData = new GridData();
		gridData.widthHint = 200;
		list.setLayoutData(gridData);
		// We add a SelectionListener
		list.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				//When the selection changes, we re-validate the list
				validate();
			}
		});
		// We add the initial layers list
		fillLayersList();

		addButtons(area);

		return area;
	}

	protected void createButtonsForButtonBar(Composite parent) {
		//in this case we want to leave this empty
	}

	protected void addButtons(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		FillLayout fillLayout = new FillLayout(SWT.VERTICAL);
		fillLayout.spacing = 6;
		fillLayout.marginWidth = 15;
		composite.setLayout(fillLayout);

		// Create Add OSM tags filter button
		addButtonOSM = new Button(composite, SWT.PUSH);
		addButtonOSM.setText("Add OSM tags filter");

		// Initially deactivate it
		addButtonOSM.setEnabled(true);
		// Add a SelectionListener
		addButtonOSM.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				// Retrieve selected entries from list
				//		        itemsToOpen = list.getSelection();
				//		        // Set return code
				//		        setReturnCode(OPEN);
				//		        // Close dialog
				//		        close();
			}
		});

		// Create Add CQL button
		addButtonCQL = new Button(composite, SWT.PUSH);
		addButtonCQL.setText("Add CQL");
		// Initially deactivate it
		addButtonCQL.setEnabled(true);
		// Add a SelectionListener
		addButtonCQL.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				// Retrieve selected entries from list
				//		        itemsToOpen = list.getSelection();
				//		        // Set return code
				//		        setReturnCode(OPEN);
				//		        // Close dialog
				//		        close();
			}
		});

		// Create Delete button
		editButton = new Button(composite, SWT.PUSH);
		editButton.setText("Edit");
		editButton.setEnabled(false);
		// Add a SelectionListener
		editButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				// Get the indices of the selected entries
				//		        int selectedItems[] = list.getSelectionIndices();
				//		        // Remove all these entries
				//		        list.remove(selectedItems);
				//		        // Now re-validate the list because it has changed
				//		        validate();
			}
		});

		// Create Remove button
		removeButton = new Button(composite, SWT.PUSH);
		removeButton.setText("Remove");
		removeButton.setEnabled(false);
		// Add a SelectionListener
		removeButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				// Get the indices of the selected entries
				int selectedItems[] = list.getSelectionIndices();
				// Remove all these entries
				if (MessageDialog.openConfirm(getShell(), "Remove item", "Do you really want to remove selected item(s)?")) {
					for(int i = 0; i< selectedItems.length; i++) {
						String layerToBeRemoveName = layers.get(selectedItems[i]).getName();
						removeLayerConfig(layerToBeRemoveName);
					}
					// Now re-validate the list because it has changed
					validate();
				}
			}
		});

		// Create Exit button
		exitButton = new Button(composite, SWT.PUSH);
		exitButton.setText("EXIT");
		// Add a SelectionListener
		exitButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				setReturnCode(CANCEL);
				close();
			}
		});
	}

	//fill in the viewer the dynamic layers list
	private void fillLayersList () {
		for (int i = 0; i < layers.size(); i++) {
			list.add(layers.get(i).getName());
		}

		//pre-selection of the LayerConfig
		if (isConfigLayer && layers.size() > 0) {
			int cont = 0;
			Iterator<Layer> iterator = layers.iterator();
			while (iterator.hasNext())  {
				if (iterator.next().getName().equals(targetLayer.getName())) {
					list.select(cont);
				}
				else 
					cont++;
			}
		}
	}

	private void removeLayerConfig (String layerToBeRemoveName) {
		if (isConfigLayer) {
			//TODO why in DynamicLayer the remove method is protected???
			((OSMLayer)((LayerConfig)targetLayer).getParent()).removeDynamicLayer(layerToBeRemoveName);
		}
		else 
			((OSMLayer)targetLayer).removeDynamicLayer(layerToBeRemoveName);
		list.remove(layerToBeRemoveName);
	}

	private void validate() {
		// We select the number of selected list entries
		boolean selected = (list.getSelectionCount()  > 0);

		editButton.setEnabled(selected);
		removeButton.setEnabled(selected);
		
		if (!selected)
			// If nothing was selected, we set an error message
			setErrorMessage("Select a Layer Config");
		else
			// Otherwise we set the error message to null
			// to show the intial content of the message area
			setErrorMessage(null);
	}

}
