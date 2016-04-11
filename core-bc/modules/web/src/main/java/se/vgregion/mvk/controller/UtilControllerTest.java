package se.vgregion.mvk.controller;

import org.junit.Test;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import static org.junit.Assert.*;

/**
 * @author Patrik Björk
 */
public class UtilControllerTest {

    private UtilController utilController = new UtilController();

    @Test
    public void toDate() throws Exception {
        XMLGregorianCalendar xmlGregorianCalendar = null;
        DatatypeFactory datatypeFactory;
        try {
            datatypeFactory = DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException e) {
            throw new RuntimeException(e);
        }

        // Test today
        GregorianCalendar calendar = new GregorianCalendar();

        xmlGregorianCalendar = datatypeFactory.newXMLGregorianCalendar(calendar);

        Date date = utilController.toDate(xmlGregorianCalendar);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        // Verify we get the same day date string.
        assertEquals(sdf.format(new Date()), sdf.format(date));
    }

    @Test
    public void isAfterToday() throws Exception {

        XMLGregorianCalendar xmlGregorianCalendar = null;
        DatatypeFactory datatypeFactory;
        try {
            datatypeFactory = DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException e) {
            throw new RuntimeException(e);
        }

        // Test today
        GregorianCalendar calendar = new GregorianCalendar();
        xmlGregorianCalendar = datatypeFactory.newXMLGregorianCalendar(calendar);

        XMLGregorianCalendar today = xmlGregorianCalendar;

        assertFalse(utilController.isAfterToday(today));

        calendar.add(Calendar.DATE, 1);

        // Test tomorrow
        xmlGregorianCalendar = datatypeFactory.newXMLGregorianCalendar(calendar);

        XMLGregorianCalendar tomorrow = xmlGregorianCalendar;

        assertTrue(utilController.isAfterToday(tomorrow));

    }

}