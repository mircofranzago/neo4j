package eu.udig.catalog.neo4j.dynamiclayerconf;

import net.refractions.udig.ui.operations.IOp;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.neo4j.gis.spatial.DynamicLayer.LayerConfig;
import org.neo4j.gis.spatial.EditableLayer;
import org.neo4j.gis.spatial.Layer;
import org.neo4j.gis.spatial.SpatialDatabaseService;
import org.neo4j.gis.spatial.geotools.data.Neo4jSpatialDataStore;

import eu.udig.catalog.neo4j.Neo4jSpatialGeoResource;


/**
 * IOp implementation to configure dynamic layers
 * This class gives the possibility to add/edit/remove Layer Config from a selected Dynamic Layer
 * using a useful GUI
 * 
 * @author Mirco Franzago for GSoC 2011
 */
public class ConfigureDynamicLayers implements IOp {

	ConfigureDynamicLayers configureDynamicLayers;
	IProgressMonitor monitor;
	SpatialDatabaseService spatialDatabase;
	String layerTargetName;
	Layer layerTarget;

	public void op(Display display, Object target, IProgressMonitor monitor) throws Exception {
		this.configureDynamicLayers = this;
		this.monitor = monitor;

		Neo4jSpatialGeoResource geoResource = (Neo4jSpatialGeoResource) target;
		layerTargetName = geoResource.getTypeName();
		Neo4jSpatialDataStore dataStore = (Neo4jSpatialDataStore) geoResource.service().getDataStore(monitor);
		spatialDatabase = dataStore.getSpatialDatabaseService();
		layerTarget = spatialDatabase.getLayer(layerTargetName);

		//we can configure only Dynamic Layer and Layer Config
		if (!(layerTarget instanceof EditableLayer) &&  !(layerTarget instanceof LayerConfig)) {
			display.syncExec(new Runnable() {
				public void run() {
					try {
						MessageDialog.openWarning(Display.getCurrent().getActiveShell(),"Dynamic Layer selection", 
						"The selected layer is not an Editable Layer or a Dynamic Layer");
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
			return;
		}

		else {
			display.syncExec(new Runnable() {
				public void run() {
					try {
						if (layerTarget instanceof EditableLayer) {
							layerTarget = spatialDatabase.asDynamicLayer(layerTarget);
						}
						DynamicLayersViewer viewer = new DynamicLayersViewer(Display.getCurrent().getActiveShell(), 
								configureDynamicLayers, layerTarget);
						viewer.open();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});

			geoResource.service().refresh(monitor);
		}
	}

	//	public void deleteLayer (Layer layerToBeDeleted) {
	//		spatialDatabase.deleteLayer(layerToBeDeleted.getName(), 
	//				new ProgressMonitorWrapper("Deleting Layer " + layerToBeDeleted.getName(), monitor));
	//	}
}
