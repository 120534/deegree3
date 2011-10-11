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
package org.deegree.tools.crs.georeferencing.model.points;

import org.deegree.tools.crs.georeferencing.model.RowColumn;

/**
 * Datastructure for every information needed to manipulate a point.
 * <p>
 * &lt;oldValue, initialValue, newValue, worldCoordinates, rowColumns&gt;
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class Point4Values {

    private AbstractGRPoint initialValue;

    private AbstractGRPoint oldValue;

    private AbstractGRPoint newValue;

    private AbstractGRPoint worldCoords;

    private RowColumn rc;

    /**
     * 
     * @param oldValue
     * @param initialValue
     * @param newValue
     * @param worldCoords
     * @param rc
     */
    public Point4Values( AbstractGRPoint oldValue, AbstractGRPoint initialValue, AbstractGRPoint newValue,
                         AbstractGRPoint worldCoords, RowColumn rc ) {
        this.oldValue = oldValue;
        this.initialValue = initialValue;
        this.newValue = newValue;
        this.worldCoords = worldCoords;
        this.rc = rc;
    }

    /**
     * 
     * @param initialValue
     * @param worldCoords
     * @param rc
     */
    public Point4Values( AbstractGRPoint initialValue, AbstractGRPoint worldCoords, RowColumn rc ) {
        this.oldValue = initialValue;
        this.initialValue = initialValue;
        this.newValue = initialValue;
        this.worldCoords = worldCoords;
        this.rc = rc;
    }

    public AbstractGRPoint getInitialValue() {
        return initialValue;
    }

    public AbstractGRPoint getOldValue() {
        return oldValue;
    }

    public AbstractGRPoint getNewValue() {
        return newValue;
    }

    public AbstractGRPoint getWorldCoords() {
        return worldCoords;
    }

    public RowColumn getRc() {
        return rc;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append( "<OldValue, InitialValue, NewValue, WorldCoordinates, Row, ColX, ColY>\n" );
        sb.append( "<" ).append( oldValue );
        sb.append( ", " ).append( initialValue );
        sb.append( ", " ).append( newValue );
        sb.append( ", " ).append( worldCoords );
        if ( rc != null ) {
            sb.append( ", " ).append( rc.getRow() );
            sb.append( ", " ).append( rc.getColumnX() );
            sb.append( ", " ).append( rc.getColumnY() );
        }
        sb.append( ">" );
        return sb.toString();
    }
}
