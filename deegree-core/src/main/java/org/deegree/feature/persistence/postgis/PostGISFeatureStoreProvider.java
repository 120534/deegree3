//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
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
package org.deegree.feature.persistence.postgis;

import java.io.File;
import java.net.URL;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.bind.JAXBException;

import org.deegree.commons.xml.NamespaceContext;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.jaxb.JAXBUtils;
import org.deegree.cs.CRS;
import org.deegree.feature.i18n.Messages;
import org.deegree.feature.persistence.FeatureStore;
import org.deegree.feature.persistence.FeatureStoreException;
import org.deegree.feature.persistence.FeatureStoreProvider;
import org.deegree.feature.persistence.mapping.MappedApplicationSchema;
import org.deegree.feature.persistence.postgis.jaxb.GMLVersionType;
import org.deegree.feature.persistence.postgis.jaxb.PostGISFeatureStoreConfig;
import org.deegree.feature.persistence.postgis.jaxb.PostGISFeatureStoreConfig.GMLSchema;
import org.deegree.feature.persistence.postgis.jaxb.PostGISFeatureStoreConfig.NamespaceHint;
import org.deegree.feature.types.ApplicationSchema;
import org.deegree.gml.GMLVersion;
import org.deegree.gml.feature.schema.ApplicationSchemaXSDDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link FeatureStoreProvider} for the {@link PostGISFeatureStore}.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class PostGISFeatureStoreProvider implements FeatureStoreProvider {

    private static final Logger LOG = LoggerFactory.getLogger( PostGISFeatureStoreProvider.class );

    private static final String CONFIG_NS = "http://www.deegree.org/datasource/feature/postgis";
    
    private static final String CONFIG_JAXB_PACKAGE = "org.deegree.feature.persistence.postgis.jaxb";

    private static final String CONFIG_SCHEMA = "/META-INF/schemas/datasource/feature/postgis/0.6.1/postgis.xsd";

    private static final String CONFIG_TEMPLATE = "/META-INF/schemas/datasource/feature/postgis/0.6.1/example.xml";

    @Override
    public String getConfigNamespace() {
        return CONFIG_NS;
    }

    @Override
    public URL getConfigSchema() {
        return PostGISFeatureStoreProvider.class.getResource( CONFIG_SCHEMA );
    }

    @Override
    public URL getConfigTemplate() {
        return PostGISFeatureStoreProvider.class.getResource( CONFIG_TEMPLATE );
    }

    @Override
    public FeatureStore getFeatureStore( URL configURL )
                            throws FeatureStoreException {
        PostGISFeatureStoreConfig config = parseConfig( configURL );
        MappedApplicationSchema schema = getSchema( configURL.toString(), config );
        return new PostGISFeatureStore( schema, config.getJDBCConnId() );
    }

    public String[] getDDL( URL configURL )
                            throws FeatureStoreException {
        PostGISFeatureStoreConfig config = parseConfig( configURL );
        MappedApplicationSchema schema = getSchema( configURL.toString(), config );
        return new PostGISDDLCreator( schema ).getDDL();
    }

    private MappedApplicationSchema getSchema( String configURL, PostGISFeatureStoreConfig config )
                            throws FeatureStoreException {

        ApplicationSchema appSchema = null;
        CRS storageSRS = new CRS( config.getStorageCRS() );
        NamespaceContext nsContext = new NamespaceContext();
        if ( !config.getGMLSchema().isEmpty() ) {
            XMLAdapter resolver = new XMLAdapter();
            resolver.setSystemId( configURL );
            try {
                String[] schemaURLs = new String[config.getGMLSchema().size()];
                int i = 0;
                GMLVersionType gmlVersionType = null;
                for ( GMLSchema jaxbSchemaURL : config.getGMLSchema() ) {
                    schemaURLs[i++] = resolver.resolve( jaxbSchemaURL.getValue().trim() ).toString();
                    // TODO what about different versions at the same time?
                    gmlVersionType = jaxbSchemaURL.getVersion();
                }

                for ( Entry<String, String> nsHint : getHintMap( config.getNamespaceHint() ).entrySet() ) {
                    nsContext.addNamespace( nsHint.getKey(), nsHint.getValue() );
                }

                ApplicationSchemaXSDDecoder decoder = null;
                if ( schemaURLs.length == 1 && schemaURLs[0].startsWith( "file:" ) ) {
                    File file = new File( new URL( schemaURLs[0] ).toURI() );
                    decoder = new ApplicationSchemaXSDDecoder( GMLVersion.valueOf( gmlVersionType.name() ),
                                                               getHintMap( config.getNamespaceHint() ), file );
                } else {
                    decoder = new ApplicationSchemaXSDDecoder( GMLVersion.valueOf( gmlVersionType.name() ),
                                                               getHintMap( config.getNamespaceHint() ), schemaURLs );
                }
                appSchema = decoder.extractFeatureTypeSchema();
            } catch ( Exception e ) {
                String msg = Messages.getMessage( "STORE_MANAGER_STORE_SETUP_ERROR", e.getMessage() );
                LOG.error( msg, e );
                throw new FeatureStoreException( msg, e );
            }
        }

        MappedApplicationSchema schema = null;
        try {

            schema = PostGISApplicationSchemaBuilder.build( appSchema, config.getFeatureType(), config.getJDBCConnId(),
                                                            config.getDBSchemaQualifier(), storageSRS, nsContext );
        } catch ( SQLException e ) {
            String msg = Messages.getMessage( "STORE_MANAGER_STORE_SETUP_ERROR", e.getMessage() );
            LOG.error( msg, e );
            throw new FeatureStoreException( msg, e );
        }
        return schema;
    }

    private PostGISFeatureStoreConfig parseConfig( URL configURL )
                            throws FeatureStoreException {
        try {
            return (PostGISFeatureStoreConfig) JAXBUtils.unmarshall( CONFIG_JAXB_PACKAGE, CONFIG_SCHEMA, configURL );
        } catch ( JAXBException e ) {
            String msg = Messages.getMessage( "STORE_MANAGER_STORE_SETUP_ERROR", e.getMessage() );
            throw new FeatureStoreException( msg, e );
        }
    }

    private static Map<String, String> getHintMap( List<NamespaceHint> hints ) {
        Map<String, String> prefixToNs = new HashMap<String, String>();
        for ( NamespaceHint namespaceHint : hints ) {
            prefixToNs.put( namespaceHint.getPrefix(), namespaceHint.getNamespaceURI() );
        }
        return prefixToNs;
    }
}