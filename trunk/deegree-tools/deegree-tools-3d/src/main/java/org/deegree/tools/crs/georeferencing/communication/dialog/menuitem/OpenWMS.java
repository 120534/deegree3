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
package org.deegree.tools.crs.georeferencing.communication.dialog.menuitem;

import static org.deegree.tools.crs.georeferencing.i18n.Messages.get;

import java.awt.Component;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JTextField;

import org.deegree.tools.crs.georeferencing.communication.GUIConstants;
import org.deegree.tools.crs.georeferencing.communication.dialog.AbstractGRDialog;

/**
 * Dialog that provides a textfield to request to a WMS.
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class OpenWMS extends AbstractGRDialog {

    private static final long serialVersionUID = 727805039160135631L;

    private JTextField textField;

    public OpenWMS( Component parent ) {
        super( parent, GUIConstants.DIM_OPEN_WMS );

        setTitle( get( "WMS_ADDRESS" ) );

        textField = new JTextField( 50 );

        textField.setText( "http://www.wms.nrw.de/geobasis/adv_dtk?SERVICE=WMS&REQUEST=GetCapabilities&VERSION=1.1.1" );
        JLabel label = new JLabel( "Please insert a valid WMS Capabilities URL: " );
        JLabel spaceB = new JLabel( " " );
        JLabel spaceA = new JLabel( " " );
        Font font = new Font( "Serif", Font.BOLD, 15 );
        label.setFont( font );
        spaceB.setFont( font );
        spaceA.setFont( font );
        this.getPanel().add( spaceB );
        this.getPanel().add( label );
        this.getPanel().add( spaceA );
        this.getPanel().add( textField );
    }

    public JTextField getTextField() {
        return textField;
    }

}
