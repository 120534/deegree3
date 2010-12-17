//$HeadURL: svn+ssh://lbuesching@svn.wald.intevation.de/deegree/base/trunk/resources/eclipse/files_template.xml $
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

 This library is free software; you can redistribute it and/or modify it under
 the terms of the GNU Lesser General Public License as published by the Free
 Software Foundation; either version 2.1 of the License, or (at your option)
 any later version.
 This library is distributed in the hope that it will be useful, but WITHOUT
 ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 details.
 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation, Inc.,
 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

 Contact information:

 lat/lon GmbH
 Aennchenstr. 19, 53177 Bonn
 Germany
 http://lat-lon.de/

 Department of Geography, University of Bonn
 Prof. Dr. Klaus Greve
 Postfach 1147, 53001 Bonn
 Germany
 http://www.geographie.uni-bonn.de/deegree/

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.client.generic;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;

import lombok.Getter;

import org.deegree.commons.jdbc.ConnectionManager;
import org.deegree.commons.utils.DeegreeAALogoUtils;
import org.deegree.commons.version.DeegreeModuleInfo;
import org.deegree.console.featurestore.FeatureStoreConfigManager;
import org.deegree.console.jdbc.ConnectionConfigManager;
import org.deegree.console.services.ServiceConfigManager;
import org.deegree.feature.persistence.FeatureStoreManager;
import org.deegree.services.controller.OGCFrontController;

/**
 * Encapsulates informations about the status of the deegree web services.
 * 
 * @author <a href="mailto:buesching@lat-lon.de">Lyn Buesching</a>
 * @author last edited by: $Author: lyn $
 * 
 * @version $Revision: $, $Date: $
 */
@ManagedBean
@RequestScoped
public class ApplicationBean implements Serializable {

    private static final long serialVersionUID = 147824864885285227L;

    private String logo = DeegreeAALogoUtils.getAsString();

    private List<String> moduleInfos = new ArrayList<String>();

    private List<String> nameToController = new ArrayList<String>();

    private String baseVersion;

    public ApplicationBean() {
        for ( DeegreeModuleInfo info : DeegreeModuleInfo.getRegisteredModules() ) {
            if ( baseVersion == null ) {
                baseVersion = info.getVersion().getVersionNumber();
            }
            moduleInfos.add( info.toString() );
        }
        for ( String key : OGCFrontController.getServiceConfiguration().getServiceControllers().keySet() ) {
            nameToController.add( key );
        }
    }

    public String getBaseVersion() {
        return baseVersion;
    }

    public String getLogo() {
        return logo;
    }

    public List<String> getModuleInfos() {
        return moduleInfos;
    }

    public List<String> getNameToController() {
        return nameToController;
    }

    public String getWorkspaceName() {
        return OGCFrontController.getServiceWorkspace().getName();
    }

    public String getWorkspaceDirectory() {
        return OGCFrontController.getServiceWorkspace().getLocation().getAbsolutePath();
    }

    public List<Connection> getConnections() {
        List<Connection> connections = new ArrayList<Connection>();
        for ( String connId : ConnectionManager.getConnectionIds() ) {
            connections.add( new Connection( connId ) );
        }
        return connections;
    }

    public Collection<String> getFeatureStores() {
        return FeatureStoreManager.getFeatureStoreIds();
    }

    public boolean getPendingChanges() {
        return true;
    }

    public void applyChanges()
                            throws Exception {
        try {
            OGCFrontController.getInstance().reload();
        } catch ( Exception e ) {
            e.printStackTrace();
        }

        ServiceConfigManager serviceConfigManager = (ServiceConfigManager) FacesContext.getCurrentInstance().getExternalContext().getApplicationMap().get(
                                                                                                                                                           "serviceConfigManager" );
        if ( serviceConfigManager != null ) {
            serviceConfigManager.scan();
        }

        FeatureStoreConfigManager fsConfigManager = (FeatureStoreConfigManager) FacesContext.getCurrentInstance().getExternalContext().getApplicationMap().get(
                                                                                                                                                                "featureStoreConfigManager" );
        if ( fsConfigManager != null ) {
            fsConfigManager.scan();
        }

        ConnectionConfigManager connConfigManager = (ConnectionConfigManager) FacesContext.getCurrentInstance().getExternalContext().getApplicationMap().get(
                                                                                                                                                              "connectionConfigManager" );
        if ( connConfigManager != null ) {
            connConfigManager.scan();
        }
    }
}
