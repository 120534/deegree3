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
package org.deegree.tools.crs.georeferencing.application.transformation;

import java.util.List;

import org.deegree.commons.utils.Triple;
import org.deegree.cs.CRSCodeType;
import org.deegree.cs.CRSIdentifiable;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.cs.transformations.Transformation;
import org.deegree.geometry.primitive.Ring;
import org.deegree.tools.crs.georeferencing.application.Scene2DValues;
import org.deegree.tools.crs.georeferencing.model.Footprint;
import org.deegree.tools.crs.georeferencing.model.points.Point4Values;
import org.deegree.tools.crs.georeferencing.model.points.PointResidual;

/**
 * Abstract base class for all transformation methods.
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public abstract class AbstractTransformation extends Transformation {

    public static enum TransformationType {
        Polynomial,

        Helmert_4,

        Affine
    }

    protected List<Triple<Point4Values, Point4Values, PointResidual>> mappedPoints;

    protected Footprint footPrint;

    protected Scene2DValues sceneValues;

    protected ICRS sourceCRS;

    protected ICRS targetCRS;

    protected final int order;

    private final PointResidual[] residuals;

    private final int arraySize;

    public AbstractTransformation( List<Triple<Point4Values, Point4Values, PointResidual>> mappedPoints,
                                   Footprint footPrint, Scene2DValues sceneValues, ICRS sourceCRS,
                                   ICRS targetCRS, final int order ) throws UnknownCRSException {
        super( sourceCRS, targetCRS, new CRSIdentifiable( new CRSCodeType( "whatever" ) ) );
        this.mappedPoints = mappedPoints;
        this.footPrint = footPrint;
        this.sceneValues = sceneValues;
        this.sourceCRS = sourceCRS;
        this.targetCRS = targetCRS;
        this.order = order;
        arraySize = mappedPoints.size();
        residuals = new PointResidual[arraySize];
    }

    public PointResidual[] getResiduals() {
        return residuals;
    }

    public int getArraySize() {
        return arraySize;
    }

    public CRSIdentifiable getIdentifiable() {

        return new CRSIdentifiable( getCRSCodeType() );
    }

    public CRSCodeType[] getCRSCodeType() {
        CRSCodeType[] s = null;
        CRSCodeType[] t = null;
        // try {
        s = sourceCRS.getCodes();
        t = targetCRS.getCodes();
        int size = s.length + t.length;
        int countT = 0;
        CRSCodeType[] codeTypes = new CRSCodeType[size];
        for ( int i = 0; i < s.length; i++ ) {
            codeTypes[i] = s[i];
        }
        for ( int i = s.length; i < size; i++ ) {
            codeTypes[i] = t[countT];
            countT++;
        }
        return codeTypes;
        // } catch ( UnknownCRSException e1 ) {
        // // TODO Auto-generated catch block
        // e1.printStackTrace();
        // }
//        return null;
    }

    /**
     * 
     * @return the TransformationType for this transformationmethod.
     */
    public abstract TransformationType getType();

    /**
     * Calculates the Polygons for the transformation.
     * 
     * @return a list of RingPolygons, can be <Code>null</Code>.
     */
    public abstract List<Ring> computeRingList();

    /**
     * Calculates the Residuals for the mappedPoints
     * 
     * @return a PointResidual object array, can be <Code>null</Code>.
     */
    public abstract PointResidual[] calculateResiduals();

}
