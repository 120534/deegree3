//$HeadURL: svn+ssh://aschmitz@wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2011 by:
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
package org.deegree.layer.config;

import static org.deegree.layer.dims.Dimension.parseTyped;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.StringReader;
import java.text.ParseException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import java_cup.runtime.Symbol;

import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.utils.Pair;
import org.deegree.layer.dims.Dimension;
import org.deegree.layer.dims.DimensionLexer;
import org.deegree.layer.dims.parser;
import org.deegree.layer.persistence.base.jaxb.DimensionType;
import org.deegree.layer.persistence.base.jaxb.StyleRefType;
import org.deegree.layer.persistence.base.jaxb.StyleRefType.Style.LegendGraphic;
import org.deegree.layer.persistence.base.jaxb.StyleRefType.Style.LegendStyle;
import org.deegree.style.persistence.StyleStore;
import org.deegree.style.persistence.StyleStoreManager;
import org.deegree.style.se.unevaluated.Style;
import org.slf4j.Logger;

/**
 * Some methods to work with the jaxb beans from the base layer schema.
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author: stranger $
 * 
 * @version $Revision: $, $Date: $
 */
public class ConfigUtils {

    private static final Logger LOG = getLogger( ConfigUtils.class );

    public static Pair<Map<String, Style>, Map<String, Style>> parseStyles( DeegreeWorkspace workspace,
                                                                            String layerName, List<StyleRefType> styles ) {
        // hail java 7 to finally be able to do some really complicated type inference
        Map<String, Style> styleMap = new LinkedHashMap<String, Style>();
        Map<String, Style> legendStyleMap = new LinkedHashMap<String, Style>();

        StyleStoreManager mgr = workspace.getSubsystemManager( StyleStoreManager.class );
        for ( StyleRefType srt : styles ) {
            String id = srt.getStyleStoreId();
            StyleStore store = mgr.get( id );
            if ( store == null ) {
                LOG.warn( "Style store with id {} was not available, ignoring style definition.", id );
                continue;
            }
            if ( srt.getStyle() == null || srt.getStyle().isEmpty() ) {
                for ( Style s : store.getAll( layerName ) ) {
                    styleMap.put( s.getName(), s );
                    legendStyleMap.put( s.getName(), s );
                }
                continue;
            }
            // else use selected styles only
            for ( org.deegree.layer.persistence.base.jaxb.StyleRefType.Style s : srt.getStyle() ) {
                String name = s.getStyleName();
                String nameRef = s.getStyleNameRef();
                String layerRef = s.getLayerNameRef();
                Style st = store.getStyle( layerRef, nameRef );
                if ( st == null ) {
                    LOG.warn( "The combination of layer {} and style {} from store {} is not available.",
                              new Object[] { layerRef, nameRef, id } );
                    continue;
                }
                styleMap.put( name, st );
                if ( s.getLegendGraphic() != null ) {
                    LegendGraphic g = s.getLegendGraphic();
                    // TODO properly handle this
                    // st.setLegendFile( null )
                } else {
                    LegendStyle ls = s.getLegendStyle();
                    if ( ls != null ) {
                        st = store.getStyle( ls.getLayerNameRef(), ls.getStyleNameRef() );
                    }
                    legendStyleMap.put( name, st );
                }
            }
        }
        return new Pair<Map<String, Style>, Map<String, Style>>( styleMap, legendStyleMap );
    }

    public static Map<String, Dimension<Object>> parseDimensions( String layerName, List<DimensionType> dimensions ) {
        Map<String, Dimension<Object>> map = new LinkedHashMap<String, Dimension<Object>>();
        for ( DimensionType type : dimensions ) {
            parser parser = new parser( new DimensionLexer( new StringReader( type.getExtent() ) ) );
            parser defaultParser = null;
            if ( type.getDefaultValue() != null ) {
                defaultParser = new parser( new DimensionLexer( new StringReader( type.getDefaultValue() ) ) );
            }

            LinkedList<?> list;
            LinkedList<?> defaultList;

            try {
                Symbol sym = parser.parse();
                if ( sym.value instanceof Exception ) {
                    final String msg = ( (Exception) sym.value ).getMessage();
                    LOG.warn( "The dimension '{}' has not been added for layer '{}' because the error"
                                                      + " '{}' occurred while parsing the extent/default values.",
                              new Object[] { type.getName(), layerName, msg } );
                    continue;
                }

                list = (LinkedList<?>) sym.value;

                if ( defaultParser != null ) {
                    sym = defaultParser.parse();
                    if ( sym.value instanceof Exception ) {
                        final String msg = ( (Exception) sym.value ).getMessage();
                        LOG.warn( "The dimension '{}' has not been added for layer '{}' because the error"
                                                          + " '{}' occurred while parsing the extent/default values.",
                                  new Object[] { type.getName(), layerName, msg } );
                        continue;
                    }
                }

                defaultList = (LinkedList<?>) sym.value;
            } catch ( Exception e ) {
                LOG.warn( "The dimension '{}' has not been added for layer '{}' because the error"
                                                  + " '{}' occurred while parsing the extent/default values.",
                          new Object[] { type.getName(), layerName, e.getLocalizedMessage() } );
                continue;
            }

            if ( type.isIsTime() ) {
                try {
                    boolean current = ( type.isCurrent() != null ) && type.isCurrent();
                    boolean nearest = ( type.isNearestValue() != null ) && type.isNearestValue();
                    boolean multiple = ( type.isMultipleValues() != null ) && type.isMultipleValues();
                    map.put( "time", new Dimension<Object>( "time", (List<?>) parseTyped( defaultList, true ), current,
                                                            nearest, multiple, "ISO8601", null, type.getSource(),
                                                            (List<?>) parseTyped( list, true ) ) );
                } catch ( ParseException e ) {
                    LOG.warn( "The TIME dimension has not been added for layer {} because the error"
                                                      + " '{}' occurred while parsing the extent/default values.",
                              layerName,
                              e.getLocalizedMessage() );
                }
            } else if ( type.isIsElevation() ) {
                try {
                    boolean nearest = ( type.isNearestValue() != null ) && type.isNearestValue();
                    boolean multiple = ( type.isMultipleValues() != null ) && type.isMultipleValues();
                    map.put( "elevation",
                             new Dimension<Object>( "elevation", (List<?>) parseTyped( defaultList, false ), false,
                                                    nearest, multiple, type.getUnits(),
                                                    type.getUnitSymbol() == null ? "m" : type.getUnitSymbol(),
                                                    type.getSource(), (List<?>) parseTyped( list, false ) ) );
                } catch ( ParseException e ) {
                    // does not happen, as we're not parsing with time == true
                }
            } else {
                try {
                    boolean nearest = ( type.isNearestValue() != null ) && type.isNearestValue();
                    boolean multiple = ( type.isMultipleValues() != null ) && type.isMultipleValues();
                    Dimension<Object> dim;
                    dim = new Dimension<Object>( type.getName(), (List<?>) parseTyped( type.getDefaultValue(), false ),
                                                 false, nearest, multiple, type.getUnits(), type.getUnitSymbol(),
                                                 type.getSource(), (List<?>) parseTyped( list, false ) );
                    map.put( type.getName(), dim );
                } catch ( ParseException e ) {
                    // does not happen, as we're not parsing with time == true
                }
            }
        }
        return map;
    }

}
