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
package org.deegree.tools.crs.georeferencing.communication.panel2D;

import static org.deegree.commons.utils.CollectionUtils.map;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Polygon;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.vecmath.Point2d;

import org.deegree.commons.utils.Pair;
import org.deegree.commons.utils.Triple;
import org.deegree.geometry.primitive.Ring;
import org.deegree.tools.crs.georeferencing.application.ApplicationState;
import org.deegree.tools.crs.georeferencing.application.Scene2DValues;
import org.deegree.tools.crs.georeferencing.model.points.AbstractGRPoint;
import org.deegree.tools.crs.georeferencing.model.points.FootprintPoint;
import org.deegree.tools.crs.georeferencing.model.points.Point4Values;
import org.deegree.tools.crs.georeferencing.model.points.PointResidual;

/**
 * 
 * Panel for drawing the footprints of an imported geometry.
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class BuildingFootprintPanel extends AbstractPanel2D {

    private static final long serialVersionUID = 5286409108040388499L;

    public final static String BUILDINGFOOTPRINT_PANEL_NAME = "BuildingFootprintPanel";

    private List<Polygon> polygonList;

    private List<Ring> worldPolygonList;

    private final Insets insets = new Insets( 0, 10, 0, 0 );

    private Map<FootprintPoint, FootprintPoint> pointsPixelToWorld;

    private ArrayList<Polygon> polygonListTranslated;

    private ApplicationState state;

    /**
     * 
     */
    public BuildingFootprintPanel( ApplicationState state ) {
        this.setName( BUILDINGFOOTPRINT_PANEL_NAME );
        this.state = state;
    }

    @Override
    protected void paintComponent( Graphics g ) {
        super.paintComponent( g );

        Graphics2D g2 = (Graphics2D) g;

        if ( polygonList != null ) {
            for ( Polygon polygon : polygonList ) {
                g2.drawPolygon( polygon );
            }
        }

        if ( zoomRect != null ) {
            int x = new Double( zoomRect.x ).intValue();
            int y = new Double( zoomRect.y ).intValue();
            int width = new Double( zoomRect.getWidth() ).intValue();
            int height = new Double( zoomRect.getHeight() ).intValue();

            g2.drawRect( x, y, width, height );
        }

        if ( state.points != null ) {
            for ( Point4Values point : map( state.points.getMappedPoints(),
                                            Triple.<Point4Values, Point4Values, PointResidual> FIRST() ) ) {
                g2.fillOval( new Double( point.getNewValue().x ).intValue() - selectedPointSize,
                             new Double( point.getNewValue().y ).intValue() - selectedPointSize, selectedPointSize * 2,
                             selectedPointSize * 2 );
            }
        }
        if ( lastAbstractPoint != null ) {
            Point2d p = new Point2d( lastAbstractPoint.getNewValue().x - selectedPointSize,
                                     lastAbstractPoint.getNewValue().y - selectedPointSize );

            g2.fillOval( new Double( p.x ).intValue(), new Double( p.y ).intValue(), selectedPointSize * 2,
                         selectedPointSize * 2 );
        }
    }

    @Override
    public Insets getInsets() {
        return insets;
    }

    public List<Polygon> getPolygonList() {
        return polygonList;
    }

    @Override
    public void setPolygonList( List<Ring> polygonRing, Scene2DValues sceneValues ) {

        if ( polygonRing != null ) {
            this.worldPolygonList = polygonRing;
            polygonListTranslated = new ArrayList<Polygon>();
            pointsPixelToWorld = new HashMap<FootprintPoint, FootprintPoint>();

            for ( Ring ring : polygonRing ) {
                int[] x2 = new int[ring.getControlPoints().size()];
                int[] y2 = new int[ring.getControlPoints().size()];
                for ( int i = 0; i < ring.getControlPoints().size(); i++ ) {
                    double x = ring.getControlPoints().getX( i );
                    double y = ring.getControlPoints().getY( i );
                    int[] p = sceneValues.getPixelCoord( new FootprintPoint( x, y ) );
                    x2[i] = p[0];
                    y2[i] = p[1];

                    pointsPixelToWorld.put( new FootprintPoint( x2[i], y2[i] ),
                                            new FootprintPoint( ring.getControlPoints().getX( i ),
                                                                ring.getControlPoints().getY( i ) ) );

                }
                Polygon p = new Polygon( x2, y2, ring.getControlPoints().size() );
                polygonListTranslated.add( p );

            }

            this.polygonList = polygonListTranslated;
        } else {
            this.polygonList = null;
        }

    }

    /**
     * Determines the closest point of the point2d in worldCoordinate.
     * 
     * @param point2d
     *            the specified point
     * @return a Pair of <Code>AbstractPoint</Code> in pixelCoordinates and <Code>FootprintPoint</Code> in
     *         worldCoordinates that is the closest point to point2d
     */
    public Pair<AbstractGRPoint, FootprintPoint> getClosestPoint( AbstractGRPoint point2d ) {
        Pair<AbstractGRPoint, FootprintPoint> closestPoint = new Pair<AbstractGRPoint, FootprintPoint>();

        if ( pointsPixelToWorld.size() != 0 ) {
            double distance = -1.0;

            for ( Point2d point : pointsPixelToWorld.keySet() ) {
                if ( distance == -1.0 ) {
                    distance = point.distance( point2d );
                    if ( point2d instanceof FootprintPoint ) {
                        closestPoint.first = new FootprintPoint( point.x, point.y );
                        closestPoint.second = new FootprintPoint( pointsPixelToWorld.get( point ).x,
                                                                  pointsPixelToWorld.get( point ).y );
                    }

                } else {
                    double distanceTemp = point.distance( point2d );
                    if ( distanceTemp < distance ) {
                        distance = distanceTemp;
                        if ( point2d instanceof FootprintPoint ) {
                            closestPoint.first = new FootprintPoint( point.x, point.y );
                            closestPoint.second = new FootprintPoint( pointsPixelToWorld.get( point ).x,
                                                                      pointsPixelToWorld.get( point ).y );
                        }
                    }
                }
            }
        }

        return closestPoint;
    }

    @Override
    public void updatePoints( Scene2DValues sceneValues ) {
        if ( worldPolygonList != null ) {
            setPolygonList( worldPolygonList, sceneValues );
        }
        state.points.updateFootprintPoints( sceneValues );
    }

}
