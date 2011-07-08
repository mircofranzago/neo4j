package eu.udig.catalog.neo4j.dynamiclayerconf;

import java.util.Iterator;

import net.refractions.udig.ui.operations.IOp;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.neo4j.gis.spatial.DynamicLayer;
import org.neo4j.gis.spatial.DynamicLayer.LayerConfig;
import org.neo4j.gis.spatial.EditableLayer;
import org.neo4j.gis.spatial.Layer;
import org.neo4j.gis.spatial.SpatialDatabaseService;
import org.neo4j.gis.spatial.geotools.data.Neo4jSpatialDataStore;
import org.neo4j.gis.spatial.osm.OSMLayer;

import eu.udig.catalog.neo4j.Neo4jSpatialGeoResource;


/**
 * IOp implementation to configure dynamic layers
 * This class gives the possibility to add/edit/remove Layer Config from a selected Dynamic Layer
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


		//		String[] s = spatialDatabase.getLayerNames();
		//		for (int i= 0; i<s.length; i++) {
		//			//			System.out.println(spatialDatabase.getLayer(s[i]).getClass());
		//			//			System.out.println(spatialDatabase.getDynamicLayer(s[i]).getClass());
		//			if (spatialDatabase.getDynamicLayer(s[i]) instanceof OSMLayer) {
		//				final OSMLayer osm = (OSMLayer) spatialDatabase.getDynamicLayer(s[i]);
		//				osm.addSimpleDynamicLayer("highway", "primary");
		//				osm.addSimpleDynamicLayer("highway", "secondary");
		//				osm.addSimpleDynamicLayer("railway", null);
		//				osm.addSimpleDynamicLayer(2, "highway=*");
		//				this.layerTarget = osm;
		//				Iterator iter = osm.getLayers().iterator();
		//				while (iter.hasNext()) {
		//					Layer layer = (Layer)iter.next();
		//					System.out.println(layer.getClass() + " -----  "+ layer.getName());
		//				}



		//Iterator<Layer> iter = dyn.getLayers().iterator();
		/*
				while (iter.hasNext()) {
					OSMLayer layer = (OSMLayer)iter.next();
//					layer.addLayerConfig("CQL1-highway", 2, "highway is not null and geometryType(the_geom) = 'LineString'");
//					layer.addLayerConfig("CQL2-residential", 2, "highway = 'residential' and geometryType(the_geom) = 'LineString'");
//					layer.addLayerConfig("CQL3-natural", 2, "natural is not null and geometryType(the_geom) = 'Polygon'");		
					layer.addSimpleDynamicLayer("highway", "primary");
					layer.addSimpleDynamicLayer("highway", "secondary");
					layer.addSimpleDynamicLayer("highway", "terrrrrr");

					Iterator it = layer.getLayers().iterator();
					while (it.hasNext()) {
						Layer l = (Layer)it.next();
						if (l instanceof LayerConfig)
							System.out.println(((LayerConfig) l).getQuery());
					}
				}
		 */

		//			}
		//		}
		//		System.out.println(geoResource.getTypeName());
	}

//	public void deleteLayer (Layer layerToBeDeleted) {
//		spatialDatabase.deleteLayer(layerToBeDeleted.getName(), 
//				new ProgressMonitorWrapper("Deleting Layer " + layerToBeDeleted.getName(), monitor));
//	}

}
