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
package org.deegree.tools.crs.georeferencing.communication.dialog.option;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;

/**
 * <Code>GeneralPanel</Code>.
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class GeneralPanel extends GenericSettingsPanel {

    private static final long serialVersionUID = 1977689265593878932L;

    public static final String SNAPPING = "snapping";

    public static final String SNAPPING_TEXT = "snapping ON/OFF";

    public static final String ZOOM = "zoom";

    public static final String ZOOM_TEXT = "ZoomValue";

    private static final int SH = 100;

    private static final int ZH = 100;

    private JPanel snapping;

    private JPanel zoom;

    private JCheckBox snappingOnOff;

    private JSpinner zoomValue;

    private SpinnerModel zoomModel;

    private double initialZoomValue;

    private JFormattedTextField ftfZoom;

    /**
     * Creates a new instance of <Code>GeneralPanel</Code>.
     * 
     * @param parent
     */
    public GeneralPanel( Component parent ) {
        this.setLayout( new BoxLayout( this, BoxLayout.Y_AXIS ) );
        this.setBounds( parent.getBounds() );

        snapping = new JPanel();
        snapping.setBorder( BorderFactory.createTitledBorder( SNAPPING ) );
        snapping.setBounds( new Rectangle( parent.getBounds().width, SH ) );
        snappingOnOff = new JCheckBox( SNAPPING_TEXT );
        snapping.add( snappingOnOff );
        snappingOnOff.setToolTipText( getSnappingTooltipText() );
        snappingOnOff.setName( SNAPPING );

        zoom = new JPanel();
        zoom.setLayout( new FlowLayout() );
        JLabel labelText = new JLabel( ZOOM_TEXT );

        zoomModel = new SpinnerNumberModel( initialZoomValue, 0.00, 1.0, 0.01 );
        zoom.setBorder( BorderFactory.createTitledBorder( "zoom" ) );
        zoom.setBounds( new Rectangle( parent.getBounds().width, ZH ) );
        zoomValue = new JSpinner( zoomModel );
        zoom.add( zoomValue );
        ftfZoom = getTextField( zoomValue );
        if ( ftfZoom != null ) {
            ftfZoom.setColumns( 5 );
            ftfZoom.setHorizontalAlignment( SwingConstants.RIGHT );
        }
        zoom.setName( ZOOM );
        zoom.add( labelText );
        zoom.add( zoomValue );

        this.add( snapping, this );
        this.add( zoom, this );

    }

    /**
     * Adds the ActionListener to the checkboxes.
     * 
     * @param c
     */
    public void addCheckboxListener( ActionListener c ) {

        snappingOnOff.addActionListener( c );

    }

    /**
     * 
     * @return the tooltip of the snapping.
     */
    private String getSnappingTooltipText() {
        StringBuilder sb = new StringBuilder();
        sb.append( "Snapping is activated if the checkmark is set." );
        return sb.toString();
    }

    /**
     * 
     * @return true if the snapping is on.
     */
    public boolean getSnappingOnOff() {
        return snappingOnOff.isSelected();
    }

    /**
     * Sets the snapping on or off.
     * 
     * @param setSnapping
     *            true for <i>on</i>, false for <i>off</i>
     */
    public void setSnappingOnOff( boolean setSnapping ) {
        this.snappingOnOff.setSelected( setSnapping );
    }

    public void setInitialZoomValue( double initialZoomValue ) {
        this.initialZoomValue = initialZoomValue;
        this.zoomModel.setValue( initialZoomValue );
    }

    public JSpinner getZoomValue() {
        return zoomValue;
    }

    @Override
    public PanelType getType() {

        return PanelType.GeneralPanel;
    }

    /**
     * 
     * @param spinner
     * @return the textfield that is used by the jSpinner.
     */
    public JFormattedTextField getTextField( JSpinner spinner ) {
        JComponent editor = spinner.getEditor();
        if ( editor instanceof JSpinner.DefaultEditor ) {
            return ( (JSpinner.DefaultEditor) editor ).getTextField();
        }
        return null;
    }

}
