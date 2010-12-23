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
package org.deegree.tools.crs.georeferencing.application;

import java.awt.Rectangle;
import java.util.List;

import javax.vecmath.Point2d;

import jj2000.j2k.NotImplementedError;

import org.deegree.cs.CRS;
import org.deegree.cs.coordinatesystems.CoordinateSystem;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryFactory;
import org.deegree.tools.crs.georeferencing.model.datastructures.GREnvelopeParameterCalculator;
import org.deegree.tools.crs.georeferencing.model.points.AbstractGRPoint;
import org.deegree.tools.crs.georeferencing.model.points.FootprintPoint;
import org.deegree.tools.crs.georeferencing.model.points.GeoReferencedPoint;
import org.deegree.tools.crs.georeferencing.model.points.AbstractGRPoint.PointType;

/**
 * Helper for the {@link Controller} to handle operations in painting the scenes.
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class Scene2DValues {

    private Rectangle dimensionGeoreference;

    private Rectangle dimensionFootprint;

    private CRS crs;

    private Envelope envelopeFootprint;

    private Envelope envelopeGeoref;

    private GeometryFactory geom;

    private double ratioGeoref;

    private double ratioFoot;

    private List<String> selectedLayers;

    private GREnvelopeParameterCalculator grEnv;

    /**
     * Creates a new instance of <Code>Scene2DValues</Code>
     * 
     * @param geom
     */
    public Scene2DValues( GeometryFactory geom ) {
        this.geom = geom;

    }

    /**
     * Converts the pixelPoint to a point with world coordinates.
     * 
     * @param pixelPoint
     *            , not <Code>null</Code>
     * @return an AbstractGRPoint in worldCoordinates
     */
    public AbstractGRPoint getWorldPoint( AbstractGRPoint pixelPoint ) {
        switch ( pixelPoint.getPointType() ) {

        case GeoreferencedPoint:

            if ( this.envelopeGeoref != null ) {

                // determine the minX and the maxY position of the subRaster-envelope
                double getMinX = envelopeGeoref.getMin().get0();
                double getMaxY = envelopeGeoref.getMax().get1();

                // determine the percentage of the requested point
                double percentX = getWorldDimension( pixelPoint ).x;
                double percentY = getWorldDimension( pixelPoint ).y;

                return new GeoReferencedPoint( getMinX + percentX, getMaxY - percentY );
            }

        case FootprintPoint:

            // determine the minX and the maxY position of the envelope
            double getMinX = this.envelopeFootprint.getMin().get0();
            double getMaxY = this.envelopeFootprint.getMax().get1();

            // determine the percentage of the requested point
            double percentX = getWorldDimension( pixelPoint ).x;
            double percentY = getWorldDimension( pixelPoint ).y;

            return new FootprintPoint( getMinX + percentX, getMaxY - percentY );
        case ResidualPoint:
            break;

        }

        return null;
    }

    /**
     * Converts the pixelDimension to a dimension with world coordinates.
     * 
     * @param dimension
     *            , not <Code>null</Code>.
     * @return an AbstractGRPoint in worldCoordinates.
     */
    public AbstractGRPoint getWorldDimension( AbstractGRPoint dimension ) {
        switch ( dimension.getPointType() ) {

        case GeoreferencedPoint:
            if ( this.envelopeGeoref != null ) {

                // determine the span of the envelope
                double spanX = this.envelopeGeoref.getSpan0();
                double spanY = this.envelopeGeoref.getSpan1();

                // determine the percentage of the requested point
                double percentX = ( dimension.x / dimensionGeoreference.width ) * spanX;
                double percentY = ( dimension.y / dimensionGeoreference.height ) * spanY;

                return new GeoReferencedPoint( percentX, percentY );

            }
        case FootprintPoint:

            // determine the span of the envelope
            double spanX = this.envelopeFootprint.getSpan0();
            double spanY = this.envelopeFootprint.getSpan1();

            // determine the percentage of the requested point
            double percentX = ( dimension.x / dimensionFootprint.width ) * spanX;
            double percentY = ( dimension.y / dimensionFootprint.height ) * spanY;

            return new FootprintPoint( percentX, percentY );
        case ResidualPoint:
            break;

        }

        return null;
    }

    /**
     * In this method firstly there is a computation from the span of the raster-envelope, after that the point is
     * calculated relative to the min-point of the raster-envelope, after that the percent is computed and finally
     * multiplicated with the imagedimension.
     * 
     * @param abstractGRPoint
     *            the worldCoordinatePoint which should be translated back to pixelCoordinates, not be <Code>null</Code>
     * @return an integer array with x, y - coordinates, z is not implemented yet.
     */
    public int[] getPixelCoord( AbstractGRPoint abstractGRPoint ) {

        Point2d percentPoint;

        int pixelPointX;
        int pixelPointY;

        switch ( abstractGRPoint.getPointType() ) {

        case GeoreferencedPoint:

            percentPoint = computePercentWorld( envelopeGeoref, abstractGRPoint );

            pixelPointX = Math.round( (float) ( ( percentPoint.x * dimensionGeoreference.width ) ) );
            pixelPointY = Math.round( (float) ( ( ( 1 - percentPoint.y ) * dimensionGeoreference.height ) ) );
            return new int[] { pixelPointX, pixelPointY };
        case FootprintPoint:

            percentPoint = computePercentWorld( this.envelopeFootprint, abstractGRPoint );
            pixelPointX = new Double( ( percentPoint.x * dimensionFootprint.width ) ).intValue();
            pixelPointY = new Double( ( 1 - percentPoint.y ) * dimensionFootprint.height ).intValue();
            return new int[] { pixelPointX, pixelPointY };
        case ResidualPoint:
            break;
        }

        return null;
    }

    /**
     * Computes the percent of the AbstractGRPoint relative to the Envelope in worldCoordinates.
     * 
     * @param env
     *            of the scene
     * @param abstractGRPoint
     *            to compute the percentage of
     * @return a Point2d which holds the percentage of the abstractGRPoint
     */
    private Point2d computePercentWorld( Envelope env, AbstractGRPoint abstractGRPoint ) {
        double spanX = env.getSpan0();
        double spanY = env.getSpan1();
        double mathX = -env.getMin().get0();
        double mathY = -env.getMin().get1();
        double deltaX = mathX + abstractGRPoint.x;
        double deltaY = mathY + abstractGRPoint.y;
        return new Point2d( deltaX / spanX, deltaY / spanY );

    }

    /**
     * Computes the percent of the AbstractGRPoint relative to the sceneDimension in PixelCoordinates.
     * 
     * @param dimension
     *            of the scene
     * @param abstractGRPoint
     *            to compute the percentage of
     * @return a Point2d which holds the percentage of the abstractGRPoint
     */
    private Point2d computePercentPixel( Rectangle dimension, AbstractGRPoint abstractGRPoint ) {
        double spanX = dimension.width;
        double spanY = dimension.height;
        return new Point2d( abstractGRPoint.x / spanX, abstractGRPoint.y / spanY );
    }

    public Rectangle getGeorefDimension() {
        return dimensionGeoreference;
    }

    public void setGeorefDimension( Rectangle georefDimension ) {
        this.dimensionGeoreference = georefDimension;
    }

    public void setDimensionFootpanel( Rectangle dimension ) {

        this.dimensionFootprint = dimension;
        if ( envelopeFootprint != null ) {
            transformProportionPartialOrientationFoot( envelopeFootprint );
        }
    }

    /**
     * Sets the points <i>xCoord</i> and <i>yCoord</i> as the centroid of the envelope with specified spans in x- and
     * y-direction. These parameters will be corrected if there is a mismatch regarding to the proportion of the
     * requested envelope.
     * 
     * @param xCoord
     *            x-coordiante in worldCoordinate-representation, not be <Code>null</Code>.
     * @param yCoord
     *            y-coordiante in worldCoordinate-representation, not be <Code>null</Code>.
     * @param spanX
     *            x-dimension that should be the width of the envelope, if not specified use -1 instead.
     * @param spanY
     *            y-dimension that should be the height of the envelope, if not specified use -1 instead.
     */
    public void setCentroidWorldEnvelopePosition( double xCoord, double yCoord, double spanX, double spanY,
                                                  PointType type ) {

        double halfSpanXWorld;
        double halfSpanYWorld;

        switch ( type ) {
        case GeoreferencedPoint:
            if ( spanX != -1 && spanY != -1 ) {
                if ( ratioGeoref < 1 ) {
                    halfSpanYWorld = spanY / 2;
                    halfSpanXWorld = halfSpanYWorld * ratioGeoref;

                } else {

                    halfSpanXWorld = spanX / 2;
                    halfSpanYWorld = halfSpanXWorld * 1 / ratioGeoref;

                }

            } else {
                halfSpanXWorld = this.envelopeGeoref.getSpan0() / 2;
                halfSpanYWorld = this.envelopeGeoref.getSpan1() / 2;

            }

            double minX = xCoord - halfSpanXWorld;
            double minY = yCoord - halfSpanYWorld;
            double maxX = xCoord + halfSpanXWorld;
            double maxY = yCoord + halfSpanYWorld;
            envelopeGeoref = geom.createEnvelope( minX, minY, maxX, maxY, crs );

            System.out.println( "[Scene2DValues] newCenteredEnvelope: " + envelopeGeoref );
            break;
        case FootprintPoint:
            // halfSpanXWorld = spanX / 2;
            // halfSpanYWorld = spanY / 2;
            // double minXF = xCoord - halfSpanXWorld;
            // double minYF = yCoord - halfSpanYWorld;
            // double maxYF = yCoord + halfSpanYWorld;
            // double maxXF = xCoord + halfSpanXWorld;
            // Envelope e = geom.createEnvelope( minXF, minYF, maxXF, maxYF, envelopeFootprint.getCoordinateSystem() );
            // transformProportionFootprint( e );
            // break;
            throw new NotImplementedError();
        case ResidualPoint:
            break;
        }
    }

    /**
     * 
     * Sets the point as the centroid of the envelope.
     * 
     * @param xCoord
     *            x-coordiante in worldCoordinate-representation, not be <Code>null</Code>.
     * @param yCoord
     *            y-coordiante in worldCoordinate-representation, not be <Code>null</Code>.
     * 
     */
    public void setCentroidWorldEnvelopePosition( double xCoord, double yCoord, PointType type ) {
        this.setCentroidWorldEnvelopePosition( xCoord, yCoord, -1, -1, type );
    }

    /**
     * Computes the translation of the envelope for the georeferencing scene or the footprint scene.
     * 
     * @param mouseChange
     *            the translation of the scene in pixelCoordinates, not <Code>null</Code>
     */
    public void moveEnvelope( AbstractGRPoint mouseChange ) {
        Point2d percent;
        switch ( mouseChange.getPointType() ) {
        case GeoreferencedPoint:
            percent = computePercentPixel( dimensionGeoreference, mouseChange );
            this.envelopeGeoref = createTranslatedEnv( this.envelopeGeoref, percent );
            break;
        case FootprintPoint:

            percent = computePercentPixel( dimensionFootprint, mouseChange );
            this.envelopeFootprint = createTranslatedEnv( envelopeFootprint, percent );
            break;
        case ResidualPoint:
            break;
        }

    }

    /**
     * Creates the envelope for translation.
     * 
     * @param env
     *            to be translated, not <Code>null</Code>
     * @param percent
     *            the percent of the point regarding to the envelope, not <Code>null</Code>
     * @return the envelope for translation.
     */
    private Envelope createTranslatedEnv( Envelope env, Point2d percent ) {
        double changeX = env.getSpan0() * percent.x;
        double changeY = env.getSpan1() * percent.y;

        return geom.createEnvelope( env.getMin().get0() + changeX, env.getMin().get1() - changeY, env.getMax().get0()
                                                                                                  + changeX,
                                    env.getMax().get1() - changeY, env.getCoordinateSystem() );
    }

    /**
     * Computes the zoom of the envelope for the georeferencing scene or the footprint scene.
     * 
     * @param isZoomedIn
     *            <i>true</i>, if one zoom in, otherwise <i>false</i>
     * @param resizing
     *            the factor the scene should be resized &rarr; <i>0&lt;resizing&lt;1</i>, could be <Code>null</Code>
     * @param mousePosition
     *            where the zoom should orient on, not <Code>null</Code>
     */
    public void computeZoomedEnvelope( boolean isZoomedIn, double resizing, AbstractGRPoint mousePosition ) {

        double newSize = ( 1 - resizing );
        if ( isZoomedIn == false ) {

            newSize = ( 1 / ( 1 - resizing ) );

        }
        AbstractGRPoint center;
        switch ( mousePosition.getPointType() ) {
        case GeoreferencedPoint:
            center = getWorldPoint( mousePosition );
            envelopeGeoref = createZoomedEnv( this.envelopeGeoref, newSize, center );
            System.out.println( "[Scene2DValues] Subrasterzoomed " + envelopeGeoref + " SPAN: "
                                + envelopeGeoref.getSpan0() + ", " + envelopeGeoref.getSpan1() );
            break;
        case FootprintPoint:
            center = getWorldPoint( mousePosition );
            this.envelopeFootprint = createZoomedEnv( envelopeFootprint, newSize, center );
            System.out.println( "[Scene2DValues] envZoomed " + envelopeFootprint );
            break;
        case ResidualPoint:
            break;
        }
    }

    /**
     * Creates the envelope for zoom in worldCoordinates.
     * 
     * @param env
     *            to be zoomed, not <Code>null</Code>
     * @param newSize
     *            the absolute value to be resized, not <Code>null<Code>.
     * @param center
     *            in worldCoordinates where the zoom should orient on, not <Code>null</Code>
     * @return the zoomed envelope.
     */
    public Envelope createZoomedEnv( Envelope env, double newSize, AbstractGRPoint center ) {

        Point2d percentP = computePercentWorld( env, center );
        double spanX = env.getSpan0() * newSize;
        double spanY = env.getSpan1() * newSize;

        double percentSpanX = spanX * percentP.x;
        double percentSpanY = spanY * percentP.y;
        double percentSpanXPos = spanX - percentSpanX;
        double percentSpanYPos = spanY - percentSpanY;

        double minPointX = center.x - percentSpanX;
        double minPointY = center.y - percentSpanY;
        double maxPointX = center.x + percentSpanXPos;
        double maxPointY = center.y + percentSpanYPos;
        Envelope e = geom.createEnvelope( minPointX, minPointY, maxPointX, maxPointY, env.getCoordinateSystem() );
        System.out.println( "[Scene2DValues] createdZoomedEnv " + e );
        return e;
    }

    /**
     * Creates a new envelope from an envelope and a centerPoint and translates it to the relative bounds.
     * 
     */
    public void createZoomedEnvWithMinPoint( PointType type, Rectangle rect ) {
        double minXRaster = rect.getMinX();
        double minYRaster = rect.getMinY();
        double width = rect.getWidth();
        double height = rect.getHeight();
        AbstractGRPoint minPoint = null;
        AbstractGRPoint dim = null;

        switch ( type ) {
        case GeoreferencedPoint:
            minPoint = getWorldPoint( new GeoReferencedPoint( minXRaster, minYRaster ) );
            dim = getWorldDimension( new GeoReferencedPoint( width, height ) );

            transformAspectRatioGeorefPartial( geom.createEnvelope( minPoint.x, minPoint.y - dim.y, minPoint.x + dim.x,
                                                                    minPoint.y, crs ) );
            break;

        case FootprintPoint:
            minPoint = getWorldPoint( new FootprintPoint( minXRaster, minYRaster ) );
            dim = getWorldDimension( new FootprintPoint( width, height ) );

            transformProportionPartialOrientationFoot( geom.createEnvelope( minPoint.x, minPoint.y - dim.y, minPoint.x
                                                                                                            + dim.x,
                                                                            minPoint.y, null ) );

            break;
        case ResidualPoint:
            break;
        }

    }

    /**
     * 
     * 
     * Determines the ratio the boundingbox has to orient on. If there is a mismatch between the width and height this
     * should influence the display of the image returned by the WMS to prevent any deformation. <li>pos - orientation
     * on width because width is larger</li> <li>neg - orientation on hight because hight is larger</li> <li>other -
     * orientation on width/hight because they are even</li>
     * <p>
     * The partial orientation orients on the minimal span dimension.
     * 
     * @param envelope
     *            to be transformed, mustn't be <Code>null</Code>.
     */
    public void transformAspectRatioGeorefPartial( Envelope envelope ) {

        this.envelopeGeoref = transformProportionGeorefPartialOrientation( envelope );

    }

    private Envelope transformProportionGeorefPartialOrientation( Envelope envelope ) {

        double w = dimensionGeoreference.width;
        double h = dimensionGeoreference.height;
        System.out.println( "[Scene2DValues] spanned rectangle: " + envelope );

        ratioGeoref = w / h;

        if ( ratioGeoref < 1 ) {
            double newWidth = envelope.getSpan1() * ratioGeoref;
            grEnv = GREnvelopeParameterCalculator.newInstance( envelope.getMin().get0(), envelope.getMax().get1(),
                                                               newWidth, envelope.getSpan1() );
        } else if ( ratioGeoref > 1 ) {
            double newHeight = envelope.getSpan0() / ratioGeoref;
            grEnv = GREnvelopeParameterCalculator.newInstance( envelope.getMin().get0(), envelope.getMax().get1(),
                                                               envelope.getSpan0(), newHeight );
        }
        if ( envelope.getCoordinateSystem().equals( new CRS( "EPSG:4326" ) )
             || envelope.getCoordinateSystem().equals( new CRS( "CRS:84" ) ) ) {
            return geom.createEnvelope( grEnv.getMinX(), grEnv.getMinY(), grEnv.getMaxX(), grEnv.getMaxY(),
                                        envelope.getCoordinateSystem() );

        }
        return geom.createEnvelope( grEnv.getMinX(), grEnv.getMinY(), grEnv.getMaxX(), grEnv.getMaxY(),
                                    envelope.getCoordinateSystem() );
    }

    /**
     * 
     * The full orientation orients on the maximal span dimension.
     * 
     * @param envelope
     *            to be transformed, must be not <Code>null</Code>.
     */
    public void transformProportionGeorefFullOrientation( Envelope envelope ) {
        double w = dimensionGeoreference.width;
        double h = dimensionGeoreference.height;
        ratioGeoref = w / h;

        if ( ratioGeoref < 1 ) {
            double newHeight = envelope.getSpan0() / ratioGeoref;
            grEnv = GREnvelopeParameterCalculator.newInstance( envelope.getMin().get0(), envelope.getMax().get1(),
                                                               envelope.getSpan0(), newHeight );

        } else if ( ratioGeoref > 1 ) {
            double newWidth = envelope.getSpan1() * ratioGeoref;
            grEnv = GREnvelopeParameterCalculator.newInstance( envelope.getMin().get0(), envelope.getMax().get1(),
                                                               newWidth, envelope.getSpan1() );
        }
        this.envelopeGeoref = geom.createEnvelope( grEnv.getMinX(), grEnv.getMinY(), grEnv.getMaxX(), grEnv.getMaxY(),
                                                   crs );
        System.out.println( "[Scene2DValues] transformed FULL " + envelopeGeoref );
    }

    /**
     * Transforms the ratio of the dimensions of the footprintPanel. <br>
     * It takes the ratio between the dimension -width and -height and the ratio between the envelope -width and
     * -height.
     * 
     * @param envelope
     *            the footprint envelope that has to be transformed, must not be <Code>null</Code>.
     */
    public void transformProportionPartialOrientationFoot( Envelope envelope ) {
        double w = dimensionFootprint.width;
        double h = dimensionFootprint.height;

        ratioFoot = w / h;
        if ( ratioFoot < 1 ) {
            double newWidth = envelope.getSpan1() * ratioFoot;
            grEnv = GREnvelopeParameterCalculator.newInstance( envelope.getMin().get0(), envelope.getMax().get1(),
                                                               newWidth, envelope.getSpan1() );
        } else if ( ratioFoot > 1 ) {
            double newHeight = envelope.getSpan0() / ratioFoot;
            grEnv = GREnvelopeParameterCalculator.newInstance( envelope.getMin().get0(), envelope.getMax().get1(),
                                                               envelope.getSpan0(), newHeight );

        }

        this.envelopeFootprint = geom.createEnvelope( grEnv.getMinX(), grEnv.getMinY(), grEnv.getMaxX(),
                                                      grEnv.getMaxY(), null );
    }

    public void transformProportionFullOrientationFoot( Envelope envelope ) {
        double w = dimensionFootprint.width;
        double h = dimensionFootprint.height;

        ratioFoot = w / h;

        if ( ratioFoot < 1 ) {
            double newHeight = envelope.getSpan0() / ratioFoot;
            grEnv = GREnvelopeParameterCalculator.newInstance( envelope.getMin().get0(), envelope.getMax().get1(),
                                                               envelope.getSpan0(), newHeight );
        } else if ( ratioFoot > 1 ) {

            double newWidth = envelope.getSpan1() * ratioFoot;
            grEnv = GREnvelopeParameterCalculator.newInstance( envelope.getMin().get0(), envelope.getMax().get1(),
                                                               newWidth, envelope.getSpan1() );
        }

        this.envelopeFootprint = geom.createEnvelope( grEnv.getMinX(), grEnv.getMinY(), grEnv.getMaxX(),
                                                      grEnv.getMaxY(), crs );

    }

    public CRS getCrs() {
        return crs;
    }

    public void setEnvelopeFootprint( Envelope createEnvelope ) {
        this.envelopeFootprint = createEnvelope;
    }

    public void setEnvelopeGeoref( Envelope envelopeGeoref ) {
        this.envelopeGeoref = envelopeGeoref;
        this.crs = envelopeGeoref.getCoordinateSystem();
    }

    public void setEnvelopeGeoref( double[] c, CoordinateSystem crs ) {
        this.envelopeGeoref = geom.createEnvelope( c[0], c[1], c[2], c[3], new CRS( crs ) );
        this.crs = envelopeGeoref.getCoordinateSystem();
    }

    public List<String> getSelectedLayers() {

        return this.selectedLayers;
    }

    public Envelope getEnvelopeFootprint() {
        return envelopeFootprint;
    }

    public Envelope getEnvelopeGeoref() {
        return envelopeGeoref;
    }

}
