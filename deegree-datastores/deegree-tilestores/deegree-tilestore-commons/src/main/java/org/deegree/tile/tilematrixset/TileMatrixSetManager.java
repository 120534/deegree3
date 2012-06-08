//$HeadURL$
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

 Occam Labs UG (haftungsbeschränkt)
 Godesberger Allee 139, 53175 Bonn
 Germany
 http://www.occamlabs.de/

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/

package org.deegree.tile.tilematrixset;

import static org.deegree.commons.config.ResourceState.StateType.created;
import static org.deegree.commons.config.ResourceState.StateType.init_error;
import static org.deegree.commons.config.ResourceState.StateType.init_ok;
import static org.slf4j.LoggerFactory.getLogger;

import java.net.URL;

import org.deegree.commons.config.AbstractResourceManager;
import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.config.DefaultResourceManagerMetadata;
import org.deegree.commons.config.ResourceInitException;
import org.deegree.commons.config.ResourceManager;
import org.deegree.commons.config.ResourceProvider;
import org.deegree.commons.config.ResourceState;
import org.deegree.tile.TileMatrixSetMetadata;
import org.slf4j.Logger;

/**
 * <code>TileMatrixSetManager</code>
 * 
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @author last edited by: $Author: mschneider $
 * 
 * @version $Revision: 31882 $, $Date: 2011-09-15 02:05:04 +0200 (Thu, 15 Sep 2011) $
 */

public class TileMatrixSetManager extends AbstractResourceManager<TileMatrixSetMetadata> {

    private static final Logger LOG = getLogger( TileMatrixSetManager.class );

    private TileMatrixSetManagerMetadata metadata;

    @Override
    public void initMetadata( DeegreeWorkspace workspace ) {
        this.metadata = new TileMatrixSetManagerMetadata( workspace );
    }

    @Override
    public TileMatrixSetManagerMetadata getMetadata() {
        return metadata;
    }

    @Override
    public Class<? extends ResourceManager>[] getDependencies() {
        return new Class[] {};
    }

    @Override
    public void startup( DeegreeWorkspace workspace )
                            throws ResourceInitException {
        super.startup( workspace );
        addStandardConfig( "inspirecrs84quad" );
        addStandardConfig( "googlecrs84quad" );
    }

    private void addStandardConfig( String name ) {
        if ( get( name ) != null ) {
            return;
        }
        LOG.info( "Adding standard tile matrix set {}.", name );
        URL url = TileMatrixSetManager.class.getResource( name + ".xml" );
        ResourceProvider provider = nsToProvider.get( "http://www.deegree.org/datasource/tile/tilematrixset" );
        ResourceState<TileMatrixSetMetadata> state = null;
        try {
            TileMatrixSetMetadata resource = create( name, url );
            state = new ResourceState<TileMatrixSetMetadata>( name, null, provider, created, resource, null );
            resource.init( workspace );
            state = new ResourceState<TileMatrixSetMetadata>( name, null, provider, init_ok, resource, null );
            add( resource );
        } catch ( ResourceInitException e ) {
            LOG.error( "Could not create resource {}: {}", name, e.getLocalizedMessage() );
            if ( e.getCause() != null ) {
                LOG.error( "Cause was: {}", e.getCause().getLocalizedMessage() );
            }
            LOG.trace( "Stack trace:", e );
            state = new ResourceState<TileMatrixSetMetadata>( name, null, provider, init_error, null, e );
        } catch ( Throwable t ) {
            LOG.error( "Could not create resource {}: {}", name, t.getLocalizedMessage() );
            if ( t.getCause() != null ) {
                LOG.error( "Cause was: {}", t.getCause().getLocalizedMessage() );
            }
            LOG.trace( "Stack trace:", t );
            state = new ResourceState<TileMatrixSetMetadata>( name, null, provider, init_error, null,
                                                              new ResourceInitException( t.getMessage(), t ) );
        }
        idToState.put( state.getId(), state );
    }

    static class TileMatrixSetManagerMetadata extends DefaultResourceManagerMetadata<TileMatrixSetMetadata> {
        TileMatrixSetManagerMetadata( DeegreeWorkspace workspace ) {
            super( "tile matrix sets", "datasources/tile/tilematrixset", TileMatrixSetProvider.class, workspace );
        }
    }

}
