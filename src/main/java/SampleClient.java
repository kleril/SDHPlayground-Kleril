import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.CacheControlDirective;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.interceptor.LoggingInterceptor;
import ca.uhn.fhir.util.BundleUtil;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.Patient;

public class SampleClient {
	static String fileName = "LastNames.txt";
	
	static FhirContext fhirContext;
	static IGenericClient client;

    public static void main(String[] theArgs) {
    	ArrayList<String> searchNames = loadSearchNames();
    	ArrayList<Double> queryAverages = new ArrayList<Double>();
    	StopWatchInterceptor stopwatch = new StopWatchInterceptor();
        // Create a FHIR client
        fhirContext = FhirContext.forR4();
        client = fhirContext.newRestfulGenericClient("http://hapi.fhir.org/baseR4");
        client.registerInterceptor(new LoggingInterceptor(false));
        client.registerInterceptor(stopwatch);

        // TEST 1
        for (String surname : searchNames)
        {
        	ArrayList<Patient> patients = searchForPatients(surname, false);
	   	     for (Patient p : patients)
	   	     {
	   	    	 printPatientInfo(p);
	   	     }
        }
    	queryAverages.add(stopwatch.getAverageRequestTime());
    	stopwatch.resetTimeHistory();

    	// TEST 2
        for (String surname : searchNames)
        {
        	ArrayList<Patient> patients = searchForPatients(surname, false);
	   	     for (Patient p : patients)
	   	     {
	   	    	 printPatientInfo(p);
	   	     }
        }
    	queryAverages.add(stopwatch.getAverageRequestTime());
    	stopwatch.resetTimeHistory();
    	
    	// TEST 3
        for (String surname : searchNames)
        {
        	ArrayList<Patient> patients = searchForPatients(surname, true);
	   	     for (Patient p : patients)
	   	     {
	   	    	 printPatientInfo(p);
	   	     }
        }
    	queryAverages.add(stopwatch.getAverageRequestTime());
    	stopwatch.resetTimeHistory();
    	

    	System.out.println("Test 1 avg response time (ms): " + queryAverages.get(0));
    	System.out.println("Test 2 avg response time (ms): " + queryAverages.get(1));
    	System.out.println("Test 3 avg response time (ms): " + queryAverages.get(2));
    }
    
    //
    protected static ArrayList<Patient> searchForPatients(String surname, boolean disableCache)
    {
    	// Search for Patient resources
        Bundle response = client
                .search()
                .forResource("Patient")
                .where(Patient.FAMILY.matches().value(surname))
                .returnBundle(Bundle.class)
                .cacheControl(new CacheControlDirective().setNoCache(disableCache))
                .execute();


    	ArrayList<Patient> patients = new ArrayList<>();
	     //Load Patient entries
	     patients.addAll(BundleUtil.toListOfResourcesOfType(fhirContext, response, Patient.class));
	     /*
	     while (response.getLink(IBaseBundle.LINK_NEXT) != null) {
	    	 response = client.loadPage().next(response).execute();
	        patients.addAll(BundleUtil.toListOfResourcesOfType(fhirContext, response, Patient.class));
	     }
	     */

	     patients.sort(PatientComparator);
	     return patients;
    }
    
    public static Comparator<Patient> PatientComparator = new Comparator<Patient>()
    {
		@Override
		public int compare(Patient p1, Patient p2)
		{
			String f1 = null;
			String f2 = null;
			if (!p1.getName().isEmpty() && p1.getName().get(0).hasGiven()) f1 = p1.getName().get(0).getGiven().get(0).getValue();
			if (!p2.getName().isEmpty() && p2.getName().get(0).hasGiven()) f2 = p2.getName().get(0).getGiven().get(0).getValue();
			 
			if (f1 == null && f2 == null) return 0;
			if (f1 != null && f2 == null) return 1;
			if (f1 == null && f2 != null) return -1;
			return f1.compareToIgnoreCase(f2);
		}
    };
    
    public static ArrayList<String> loadSearchNames()
    {
    	ArrayList<String> names = new ArrayList<>();
    	try {
			BufferedReader br = new BufferedReader(new FileReader(fileName));
			String nextLine = br.readLine();
			while (nextLine != null)
			{
				names.add(nextLine);
				nextLine = br.readLine();
			}
		    br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
    	return names;
    }
    
    protected static void printPatientInfo(Patient p)
    {
    	String firstName = "N/A";
    	String lastName = "N/A";
    	if (!p.getName().isEmpty())
		{
    		HumanName hn = p.getName().get(0); //ASSUMPTION: The first entry for a patient's name is 'correct'
    		if (hn.hasGiven())
    		{
    			firstName = hn.getGiven().get(0).getValue(); //First name is the first entry in the set of given names (see: http://hl7.org/fhir/datatypes-definitions.html#HumanName.given)
    		}
    		if (hn.hasFamily())
    		{
        		lastName = hn.getFamily();
    		}
		}
    	String dob = p.getBirthDate() == null ? "N/A" : p.getBirthDate().toString();
    	System.out.println(firstName + ", " + lastName + " DOB: " + dob);
    }
}
