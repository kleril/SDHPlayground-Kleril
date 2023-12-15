import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;

import org.hl7.fhir.r4.model.Patient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestCases
{
	/*
	 * Test cases for SampleClient.java
	 */
	@Test
	public void loadNames()
	{
		ArrayList<String> names = SampleClient.loadSearchNames();
		assertEquals(names.size(), 20);
	}
	
	@Test
	public void comparePatients()
	{
		Patient p1 = new Patient();
		p1.addName().setFamily("Grumbo").addGiven("Strimbo");
		Patient p2 = new Patient();
		p2.addName().setFamily("Argulus").addGiven("Clumbo");
		Patient p3 = new Patient();
		p3.addName().setFamily("Trimby").addGiven("Clumbo");
		assertTrue(SampleClient.PatientComparator.compare(p1, p2) > 1); //Strimbo comes after Clumbo
		assertTrue(SampleClient.PatientComparator.compare(p2, p3) == 0); //Clumbo == Clumbo
	}
	
	/*
	 * Test cases for StopWatchInterceptor.java
	 */
	//Long wait
	long time1Before;
	long time1After;
	
	//Short wait
	long time2Before;
	long time2After;
	
	Double averageOf0;
	Double averageOf2;

	StopWatchInterceptor stopwatch;
	
	@Before
	public void setupTimerTests()
	{
		stopwatch = new StopWatchInterceptor();
		averageOf0 = stopwatch.getAverageRequestTime();
		
		stopwatch.interceptRequest(null);
		time1Before = stopwatch.timer.getMillis();
		try {Thread.sleep(200);}					catch (InterruptedException e) {e.printStackTrace();}
		try {stopwatch.interceptResponse(null);}	catch (IOException e) {e.printStackTrace();}
		time1After = stopwatch.timer.getMillis();

		stopwatch.interceptRequest(null);
		time2Before = stopwatch.timer.getMillis();


		try {Thread.sleep(50);}					catch (InterruptedException e) {e.printStackTrace();}
		try {stopwatch.interceptResponse(null);}	catch (IOException e) {e.printStackTrace();}
		time2After = stopwatch.timer.getMillis();
		averageOf2 = stopwatch.getAverageRequestTime();
	}
	
	@Test
	public void callForAverageBeforeAnyRequestsComplete()
	{
		assertEquals(averageOf0, null);
	}

	@Test
	public void timer()
	{
		assertTrue(time1Before < time1After);
		
		assertTrue(time2Before < time2After);

		assertTrue(time2After < time1After); //Shorter sleep, should result in a lower time
		
		assertEquals(stopwatch.requestTimes.size(), 2); //Setup should have placed two entries in this array
		
		assertTrue(averageOf2 > 0); //Average time should be positive
	}
	
	@Test
	public void timerAverage()
	{
		long elapsed1 = time1After - time1Before;
		long elapsed2 = time2After - time2Before;
		
		Double result = (double)(elapsed1 + elapsed2)/2;
		assertEquals(averageOf2, result);
	}
	
	@After
	public void timerReset()
	{
		Double averageBeforeReset = stopwatch.getAverageRequestTime();
		assertTrue(averageBeforeReset != null);
		stopwatch.resetTimeHistory();
		Double averageAfterReset = stopwatch.getAverageRequestTime();
		assertEquals(averageAfterReset, null);
	}
}
