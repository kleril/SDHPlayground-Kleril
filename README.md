Assumptions & Considerations:

ASSUMPTION: A partial set of patient results is sufficient
	Fetching every entry matching a surname can take upwards of 30 minutes, and a bundle of 25 should be a representative sample

ASSUMPTION: printing "to the screen" refers to the console

ASSUMPTION: The first entry for a patient's name is 'correct' in regards to the requirement of "Modify SampleClient so that it prints the first and last name, and birth date of each Patient to the screen"
	If this is not the case, what should the approach be? Print out all name entries for a patient? Search through all records to find one(s) which contain the surname being searched for? Is a substring sufficient or do we require an exact match?
	
ASSUMPTION: 'with caching disabled' refers to providing a cache control directive in the request header
	I'm unsure what else this could be referring to that I have control over on the client side, and the results are not significant enough to assure me I am doing it correctly