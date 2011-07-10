package eu.udig.catalog.neo4j.dynamiclayerconf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.neo4j.gis.spatial.DynamicLayer;
import org.neo4j.gis.spatial.Layer;
import org.neo4j.gis.spatial.osm.OSMLayer;
import org.neo4j.gis.spatial.DynamicLayer.LayerConfig;

/**
 * GUI for dynamic layers configuration
 * 
 * @author Mirco Franzago for GSoC 2011
 */

public class DynamicLayersViewer extends TitleAreaDialog {

	List list;
	Layer targetLayer;
	// ConfigureDynamicLayers configureDynamicLayers;
	boolean isConfigLayer;
	boolean isOSMLayer;

	Button addButtonOSM;
	Button addButtonCQL;
	Button editButton;
	Button removeButton;
	Button exitButton;

	public DynamicLayersViewer(Shell parentShell,
			ConfigureDynamicLayers configureDynamicLayers, Layer targetLayer) {
		super(parentShell);
		// this.configureDynamicLayers = configureDynamicLayers;
		this.targetLayer = targetLayer;
		this.isConfigLayer = targetLayer instanceof LayerConfig;
		this.isOSMLayer = targetLayer instanceof OSMLayer;
	}

	public void create() {
		super.create();
		setTitle("Dynamic Layers Configuration");
		Layer mainLayer;
		if (isConfigLayer) mainLayer = ((LayerConfig)targetLayer).getParent();
		else mainLayer = targetLayer;
		setMessage("You can add/edit/remove Dynamic Layers of the layer:\n"+ mainLayer.getName());
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
		
		Composite listArea =new Composite(area, SWT.NULL);
		GridLayout gridLayout2 = new GridLayout(1, false);
		listArea.setLayout(gridLayout2);
		
		Label label = new Label(listArea, SWT.NULL);
		label.setText("Dynamic Layers list:");
		
		// Now we create the list widget
		list = new List(listArea, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
		final GridData gridData = new GridData();
		gridData.widthHint = 200;
		gridData.heightHint = 150;
		list.setLayoutData(gridData);
		// We add a SelectionListener
		list.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				// When the selection changes, we re-validate the list
				validate();
			}
		});
		// We add the initial layers list
		fillLayersList();

		addButtons(area);

		return area;
	}

	protected void createButtonsForButtonBar(Composite parent) {
		// in this case we want to leave this empty
	}

	protected void addButtons(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		FillLayout fillLayout = new FillLayout(SWT.VERTICAL);
		fillLayout.spacing = 6;
		fillLayout.marginWidth = 15;
		composite.setLayout(fillLayout);

		// Create Add CQL button
		addButtonCQL = new Button(composite, SWT.PUSH);
		addButtonCQL.setText("Add CQL");
		addButtonCQL.setEnabled(true);
		// Add a SelectionListener
		addButtonCQL.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				LayerConfigEditingDialog textEditorDialog = new LayerConfigEditingDialog(getShell(), true);
				if (textEditorDialog.open() == Window.OK) {
					addCQLLayerConfig(textEditorDialog);
					// Now re-validate the list because it has changed
					validate();
				}
			}
		});
		
		// Create Add OSM tags filter button
		addButtonOSM = new Button(composite, SWT.PUSH);
		addButtonOSM.setText("Add OSM tags filter");

		addButtonOSM.setEnabled(false);
		// Add a SelectionListener
		addButtonOSM.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				LayerConfigEditingDialog textEditorDialog = new LayerConfigEditingDialog(getShell(),false);
				if (textEditorDialog.open() == Window.OK) {
					addOSMLayerConfig(textEditorDialog);
					// Now re-validate the list because it has changed
					validate();
				}
			}
		});
		//the "add osm tags filter" button have to be enable only if the layer is an OSMLayer
		if (isConfigLayer) {
			if (((LayerConfig)targetLayer).getParent() instanceof OSMLayer)
				addButtonOSM.setEnabled(true);
		} else if (isOSMLayer)
			addButtonOSM.setEnabled(true);
		
		// Create Delete button
		editButton = new Button(composite, SWT.PUSH);
		editButton.setText("Edit");
		editButton.setEnabled(false);
		// Add a SelectionListener
		editButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				// Get the indice of the selected entries
				String selectedItem = list.getSelection()[0];
				LayerConfigEditingDialog textEditorDialog = createLayerConfigEditingDialog(selectedItem);
				if (textEditorDialog.open() == Window.OK) {
					LayerConfig conf = getLayerConfig().get(selectedItem);
					if ( addCQLLayerConfig(textEditorDialog))
						removeLayerConfig(conf);
					// Now re-validate the list because it has changed
					validate();
				}
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
				String[] selectedItems = list.getSelection();
				// Remove all these entries
				if (MessageDialog.openConfirm(getShell(), "Remove item",
				"Do you really want to remove selected item(s)?")) {
					for (int i = 0; i < selectedItems.length; i++) {
						LayerConfig layerToBeRemove = getLayerConfig().get(selectedItems[i]);
						removeLayerConfig(layerToBeRemove);
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

	// fill in the viewer the dynamic layers list
	private void fillLayersList() {
		HashMap<String, LayerConfig> layers = getLayerConfig();
		Iterator<LayerConfig> itr = layers.values().iterator();
		while (itr.hasNext()) {
			list.add(itr.next().getName());
		}

		// pre-selection of the LayerConfig
		if (isConfigLayer && layers.size() > 0) {
			list.setSelection(new String[] { targetLayer.getName() });
		}
	}

	private void removeLayerConfig(LayerConfig layerToBeRemove) {
		String layerToBeRemoveName = layerToBeRemove.getName();
		if (((DynamicLayer) (layerToBeRemove.getParent()))
				.removeLayerConfig(layerToBeRemoveName))
			list.remove(layerToBeRemoveName);
	}

	// add a simple dynamic layer to a OSMLayer
	private void addOSMLayerConfig(LayerConfigEditingDialog dialog) {
		LayerConfig newLayerConfig = null;
		if (isConfigLayer) {
			newLayerConfig = ((OSMLayer) (((LayerConfig) targetLayer)
					.getParent())).addSimpleDynamicLayer(dialog.getName(), dialog.getType(),	dialog.getQuery());
		} else {
			newLayerConfig = ((OSMLayer) targetLayer).addSimpleDynamicLayer(dialog.getName(),	dialog.getType(), dialog.getQuery());
		}

		if (newLayerConfig != null) {
			list.add(newLayerConfig.getName());
		}
	}

	// add a CQL-based LayerConfig to a Dynamic Layer
	private boolean addCQLLayerConfig(LayerConfigEditingDialog dialog) {
		LayerConfig newLayerConfig = null;
		try{
		if (isConfigLayer) {
			newLayerConfig = ((DynamicLayer) (((LayerConfig) targetLayer)
					.getParent())).addLayerConfig(dialog.getName(),
							dialog.getType(), dialog.getQuery());
		} else {
			newLayerConfig = ((DynamicLayer) targetLayer).addLayerConfig(
					dialog.getName(), dialog.getType(), dialog.getQuery());
		}
		}
		catch (Exception e) {
			MessageDialog.openError(getShell(), "New Dynamic Layer", e.getMessage());
			return false;
		}
		
		if (newLayerConfig != null) {
			//check the non-existence of the new layer
			String[]items = list.getItems();
			for (int i=0; i<items.length; i++) {
				if (newLayerConfig.getName().equals(items[i])) {
					MessageDialog.openError(getShell(), "New Dynamic Layer", "Problem encountered adding the new dynamic layer:\nthe layer "+ newLayerConfig.getName() +" already exists!");
					return false;
				}
			}
			list.add(newLayerConfig.getName());
			MessageDialog.openInformation(getShell(), "New Dynamic Layer", "The new dynamic layer "+newLayerConfig.getName()+" was created");
			return true;
		}
		else {
			MessageDialog.openError(getShell(), "New Dynamic Layer", "Problem encountered adding the new dynamic layer: choose a different name.");
			return false;
		}
	}

	private LayerConfigEditingDialog createLayerConfigEditingDialog(String layerConfigToBeEditName) {
		LayerConfigEditingDialog textEditorDialog;
		LayerConfig layerConfigToBeEdit = getLayerConfig().get(layerConfigToBeEditName);

		textEditorDialog = new LayerConfigEditingDialog(getShell(), true, layerConfigToBeEdit.getName(), 
				layerConfigToBeEdit.getGeometryType(), layerConfigToBeEdit.getQuery());

		return textEditorDialog;
	}

	// to get the LayerConfigs of the targetLayer
	private HashMap<String, LayerConfig> getLayerConfig() {
		java.util.List<Layer> tempList;
		HashMap<String, LayerConfig> layerConfigList = new HashMap<String, LayerConfig>();
		// if the selected layer is a LayerConfig, we take the LayerConfigs of
		// the parent
		if (isConfigLayer)
			tempList = ((DynamicLayer) ((LayerConfig) targetLayer).getParent())
			.getLayers();
		else
			tempList = ((DynamicLayer) targetLayer).getLayers();

		// we want to take only the LayerConfigs
		for (int i = 0; i < tempList.size(); i++) {
			if (tempList.get(i) instanceof LayerConfig) {
				layerConfigList.put(((LayerConfig) tempList.get(i)).getName(),
						(LayerConfig) tempList.get(i));
			}
		}

		return layerConfigList;
	}

	private void validate() {
		// We select the number of selected list entries
		boolean selected = (list.getSelectionCount() > 0);

		editButton.setEnabled(selected);
		removeButton.setEnabled(selected);

		if (!selected)
			// If nothing was selected, we set an error message
			setErrorMessage("Select a Dynamic Layer from the list below or add a new one");
		else
			// Otherwise we set the error message to null
			// to show the intial content of the message area
			setErrorMessage(null);
	}

}
