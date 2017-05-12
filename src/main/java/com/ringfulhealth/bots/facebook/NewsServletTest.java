package com.ringfulhealth.bots.facebook;

import static org.junit.Assert.*;

import org.junit.Test;

public class NewsServletTest {

	NewsServlet newsServlet = new NewsServlet(); 
	@Test
	public void testRepresentatives() {
		String address = "580 Portola Plaza, Los Angeles, CA 90095";
		String humanRequest = "Reps: " + address;
		String expected = "Your representatives are Ted Lieu (Democratic) and Elan Carr (Republican).";
		assertEquals(newsServlet.converse(humanRequest, null), expected);
	}
	@Test
	public void testHelp() {
		String humanRequest = "help";
		String expected = "I'm here to help you get involved in local politics.";
		assertEquals(newsServlet.converse(humanRequest, null), expected);
	}
	@Test
	public void testValidPollingLocation() {
		String address = "1234 Westwood Blvd, Los Angeles, CA 90024";
		String humanRequest = "Polling Location: " + address;
		String expected = "Your polling location is WESTWOOD RECREATION COMPLEX, located on 1350 S SEPULVEDA BLVD, LOS ANGELES, CA 90025. The hours are 7:00am-8:00pm. Note: CLUBROOM B.";
		assertEquals(newsServlet.converse(humanRequest, null), expected);	
	}
	@Test
	public void testInvalidPollingLocation() {
		String address = "15 Furshtatskaya St., St. Petersburg 191028, Russian Federation";
		String humanRequest = "Polling Location: " + address;
		String expected = "Sorry! It appears that was an invalid request!";
		assertEquals(newsServlet.converse(humanRequest, null), expected);	
	}
	@Test
	public void testContactRepresentatives() {
		String humanRequest = "Contact Reps: 1234 Westwood Blvd, Los Angeles, CA 90024";
		String expected = "Whoops! I don't know what you just said";
		assert(((String) newsServlet.converse(humanRequest, null)).startsWith("Karen Bass"));	
	}
	@Test
	public void testBogusInput() {
		String humanRequest = "ak;djlkfajsdjf2j39kl";
		String expected = "Whoops! I don't know what you just said";
		assertEquals(newsServlet.converse(humanRequest, null), expected);	
	}
}
