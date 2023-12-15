import java.io.IOException;
import java.util.ArrayList;

import ca.uhn.fhir.rest.client.api.IClientInterceptor;
import ca.uhn.fhir.rest.client.api.IHttpRequest;
import ca.uhn.fhir.rest.client.api.IHttpResponse;
import ca.uhn.fhir.util.StopWatch;

public class StopWatchInterceptor implements IClientInterceptor {
	StopWatch timer;
	ArrayList<Long> requestTimes;
	public StopWatchInterceptor()
	{
		timer = new StopWatch();
		requestTimes = new ArrayList<Long>();
	}

	@Override
	public void interceptRequest(IHttpRequest theRequest) {
		timer.restart();
	}

	@Override
	public void interceptResponse(IHttpResponse theResponse) throws IOException {
		requestTimes.add(timer.getMillis());
	}

	public Double getAverageRequestTime()
	{
		if (requestTimes.isEmpty()) return null; //Prevent division by zero
		long runningTotal = 0;
		for (Long nextTime : requestTimes)
		{
			runningTotal += nextTime;
		}
		return (double)runningTotal / requestTimes.size();
	}
	
	public void resetTimeHistory()
	{
		requestTimes.clear();
	}
}
