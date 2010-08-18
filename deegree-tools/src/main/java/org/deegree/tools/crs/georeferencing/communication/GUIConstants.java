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
package org.deegree.tools.crs.georeferencing.communication;

/**
 * Constants used to control the gui elements.
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public interface GUIConstants {

    public final static String WINDOW_TITLE = " deegree3 Georeferencing Client ";

    /*
     * JMenu
     */
    public final static String MENU_EDIT = "Edit";

    public final static String MENU_FILE = "File";

    public final static String MENU_TRANSFORMATION = "Transformation";

    /*
     * JMenuItem
     */
    public final static String MENUITEM_GETMAP = "Import 2D Map";

    public final static String MENUITEM_GET_3DOBJECT = "Import 3D Object";

    public final static String MENUITEM_TRANS_POLYNOM_FIRST = "Polynomial 1";

    public final static String MENUITEM_TRANS_POLYNOM_SECOND = "Polynomial 2";

    public final static String MENUITEM_TRANS_POLYNOM_THIRD = "Polynomial 3";

    public final static String MENUITEM_TRANS_HELMERT = "Helmert";

    public final static String MENUITEM_EDIT_OPTIONS = "Options";

    /*
     * JTextField
     */
    public final static String JTEXTFIELD_COORDINATE_JUMPER = "CoordinateJumper";

    /*
     * JButton
     */
    public final static String JBUTTON_PAN = "Pan";

    public final static String JBUTTON_ZOOM_IN = "Zoom in";

    public final static String JBUTTON_ZOOM_OUT = "Zoom out";

    public final static String JBUTTON_ZOOM_COORD = "Zoom coord";

}
