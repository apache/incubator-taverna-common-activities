package net.sf.taverna.t2.activities.spreadsheet;

import static org.junit.Assert.*;

import org.junit.Test;

public class SpreadsheetReadExceptionTest {

	@Test
	public void testSpreadsheetReadException() {
		SpreadsheetReadException spreadsheetReadException = new SpreadsheetReadException();
		assertNull(spreadsheetReadException.getMessage());
		assertNull(spreadsheetReadException.getCause());
	}

	@Test
	public void testSpreadsheetReadExceptionString() {
		SpreadsheetReadException spreadsheetReadException = new SpreadsheetReadException("test exception");
		assertEquals("test exception", spreadsheetReadException.getMessage());
		assertNull(spreadsheetReadException.getCause());
	}

	@Test
	public void testSpreadsheetReadExceptionThrowable() {
		Exception exception = new Exception();
		SpreadsheetReadException spreadsheetReadException = new SpreadsheetReadException(exception);
		assertEquals(exception, spreadsheetReadException.getCause());
	}

	@Test
	public void testSpreadsheetReadExceptionStringThrowable() {
		Exception exception = new Exception();
		SpreadsheetReadException spreadsheetReadException = new SpreadsheetReadException("test exception", exception);
		assertEquals("test exception", spreadsheetReadException.getMessage());
		assertEquals(exception, spreadsheetReadException.getCause());
	}

}
