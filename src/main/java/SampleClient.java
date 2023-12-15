import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.interceptor.LoggingInterceptor;
import ca.uhn.fhir.util.BundleUtil;

import java.util.ArrayList;
import java.util.Comparator;

import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.Patient;

public class SampleClient {
	static ArrayList<Patient> patients = new ArrayList<>();

    public static void main(String[] theArgs) {
        // Create a FHIR client
        FhirContext fhirContext = FhirContext.forR4();
        IGenericClient client = fhirContext.newRestfulGenericClient("http://hapi.fhir.org/baseR4");
        client.registerInterceptor(new LoggingInterceptor(false));

        // Search for Patient resources
        Bundle response = client
                .search()
                .forResource("Patient")
                .where(Patient.FAMILY.matches().value("SMITH"))
                .returnBundle(Bundle.class)
                .execute();

     //Load Patient entries
     patients.addAll(BundleUtil.toListOfResourcesOfType(fhirContext, response, Patient.class));
     while (response.getLink(IBaseBundle.LINK_NEXT) != null) {
    	 response = client.loadPage().next(response).execute();
        patients.addAll(BundleUtil.toListOfResourcesOfType(fhirContext, response, Patient.class));
     }

     patients.sort(new Comparator<Patient>() {
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
     });
     for (Patient p : patients)
     {
    	 printPatientInfo(p);
     }
    }
    
    protected static void printPatientInfo(Patient p)
    {
    	String firstName = "N/A";
    	String lastName = "N/A";
    	if (!p.getName().isEmpty())
		{
    		HumanName hn = p.getName().get(0); //Patient name object must either be empty or of length 1
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
