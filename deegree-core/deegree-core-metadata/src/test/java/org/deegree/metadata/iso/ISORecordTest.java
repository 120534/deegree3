//$HeadURL: svn+ssh://lbuesching@svn.wald.intevation.de/deegree/base/trunk/resources/eclipse/files_template.xml $
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2012 by:
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
package org.deegree.metadata.iso;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import java.io.InputStream;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.junit.Test;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 * @author last edited by: $Author: lyn $
 * 
 * @version $Revision: $, $Date: $
 */
public class ISORecordTest {

    @Test
    public void testInstantiationFromXMLStream()
                            throws XMLStreamException, FactoryConfigurationError {
        InputStream is = ISORecordTest.class.getResourceAsStream( "datasetRecord.xml" );
        XMLStreamReader xmlStream = XMLInputFactory.newInstance().createXMLStreamReader( is );
        ISORecord record = new ISORecord( xmlStream );
        assertEquals( "5E50884F-5549-2A7A-99E3-334234A887C81", record.getIdentifier() );
    }

    @Test
    public void testInstantiationFromXMLStreamOfBrokenRecord()
                            throws XMLStreamException, FactoryConfigurationError {
        InputStream is = ISORecordTest.class.getResourceAsStream( "datasetRecord_invalidDate.xml" );
        XMLStreamReader xmlStream = XMLInputFactory.newInstance().createXMLStreamReader( is );
        ISORecord record = new ISORecord( xmlStream );
        boolean exception = false;
        try {
            String fileIdentifier = record.getIdentifier();
        } catch ( Exception e ) {
            exception = true;
        }
        assertTrue( exception );
    }
}
