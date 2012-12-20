package org.semweb.assign5;

import java.io.IOException;
import java.io.InputStream;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.reasoner.Reasoner;
import com.hp.hpl.jena.reasoner.ReasonerRegistry;
import com.hp.hpl.jena.reasoner.rulesys.GenericRuleReasoner;
import com.hp.hpl.jena.reasoner.rulesys.Rule;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;


public class FriendTracker
{
	static String defaultNameSpace ="http://www.semanticweb.org/ontologies/2012/11/gmail.owl#";
	static String eventsDefaultNameSpace = "http://www.semanticweb.org/ontologies/2012/11/events.owl#";
	Model _friends = null;
	Model schema = null;
	Model _events = null;
	Model schema1 = null;
	InfModel inferredFriends = null;
	InfModel inferredEvents = null;

	static String list[] = new String[600];
	static String profileInfo[] = new String[20];
	static String events[] = new String[30];

	public String[] getFriends(){
		return list;
	}
	public String[] getMyProfileInfo(){
		return profileInfo;
	}
	public String[] getEvents(){
		return events;
	}

	public void collectFbGmailEventsData() throws IOException
	{
		FriendTracker hello = new FriendTracker();
		//Load my Facebook friends, Gmail contacts and events
		hello.populateFBFriends();
		hello.populateGmailContacts();
		hello.populateEvents();

		//Add the ontologies
		hello.populateFacebookFriendsSchema();
		hello.populateGmailContactsSchema();
		hello.populateEventsSchema();

		hello.addAlignment();
		hello.bindReasoner();

		//Run SPARQL queries to store required data in class member variables
		hello.profileInfo(hello.inferredFriends);
		hello.myFriendsList(hello.inferredFriends);
		hello.eventsList(hello.inferredEvents);
	}

	/*populate the rdf data for facebook information of myself and my friends*/
	private void populateFBFriends() throws IOException
	{
		_friends = ModelFactory.createOntologyModel();
		InputStream inFoafInstance = FileManager.get().open("Ontologies/facebook.rdf");
		_friends.read(inFoafInstance,defaultNameSpace);
		inFoafInstance.close();
	}

	/*populate the owl/rdf data for gmail information of myself and a few celebrity contacts*/
	private void populateGmailContacts() throws IOException
	{
		InputStream inFoafInstance = FileManager.get().open("Ontologies/gmail.owl");
		_friends.read(inFoafInstance,defaultNameSpace);
		inFoafInstance.close();
	}

	/*populate the owl/rdf data of few of my google calendar events*/
	private void populateEvents() throws IOException
	{
		_events = ModelFactory.createOntologyModel();
		InputStream inFoafInstance = FileManager.get().open("Ontologies/events.rdf");
		_events.read(inFoafInstance,eventsDefaultNameSpace);
		inFoafInstance.close();
	}

	private void populateFacebookFriendsSchema() throws IOException
	{
		InputStream inFoaf = FileManager.get().open("Ontologies/facebook.rdf");
		schema = ModelFactory.createOntologyModel();
		schema.read(inFoaf, defaultNameSpace);
		inFoaf.close();
	}
	private void populateGmailContactsSchema() throws IOException
	{
		InputStream inFoafInstance =FileManager.get().open("Ontologies/gmail.owl");
		schema.read(inFoafInstance,defaultNameSpace);
		inFoafInstance.close();
	}
	private void populateEventsSchema() throws IOException
	{
		InputStream inFoaf = FileManager.get().open("Ontologies/events.rdf");
		schema1 = ModelFactory.createOntologyModel();
		schema1.read(inFoaf,eventsDefaultNameSpace);
		inFoaf.close();
	}

	/*SPARQL Query to get profile information*/
	private void profileInfo(Model model)
	{
		runQuery1("select ?name ?location ?fbID ?bday ?hometown " +
				"where {?person friends:name ?name . FILTER(?name='Shruti') . " +
				"?person friends:location ?location ; friends:facebookID ?fbID ;" +
				"friends:birthday ?bday ; friends:hometown ?hometown}", model);
	}

	/*SPARQL Query to get facebook friends list, exclude my data from this list*/
	private void myFriendsList(Model model)
	{
		runQuery("select ?name where {?person friends:name ?name . " +
				"FILTER(?name!='Shruti')}", model);
	}

	private void eventsList(Model model)
	{
		runQuery2("select ?title ?date ?time where {?event events:title ?title;" +
				"events:date ?date ; events:time ?time}", model);
	}
	/*Query logic for my profile information*/
	private void runQuery1(String queryRequest, Model model)
	{
		StringBuffer queryStr = new StringBuffer();
		// Establish Prefixes
		queryStr.append("PREFIX rdfs" + ": <" + "http://www.w3.org/2000/01/rdf-schema#" + "> ");
		queryStr.append("PREFIX rdf" + ": <" + "http://www.w3.org/1999/02/22-rdf-syntax-ns#" + "> ");
		queryStr.append("PREFIX friends" + ": <" + "http://xmlns.com/friends/0.1/"+ "> ");
		//Now add query
		queryStr.append(queryRequest);
		Query query = QueryFactory.create(queryStr.toString());
		QueryExecution qexec = QueryExecutionFactory.create(query, model);
		int i= 0;
		try
		{
			ResultSet response = qexec.execSelect();
			while( response.hasNext())
			{
				QuerySolution soln = response.nextSolution();
				RDFNode name = soln.get("?name");
				RDFNode location = soln.get("?location");
				RDFNode fbID = soln.get("?fbID");
				RDFNode bday = soln.get("?bday");
				RDFNode hometown = soln.get("?hometown");
				if( name != null )
				{
					profileInfo[i]="Name: " + name.toString();
					i++;
				}
				else
					System.out.println("No Friends found!");
				if (location != null){
					profileInfo[i] = "Location: " + location.toString();
					i++;
				}
				else
					System.out.println("No location found!");
				if (fbID != null){
					profileInfo[i] = "Facebook ID: " + fbID.toString();
					i++;
				}
				else
					System.out.println("No fbID found!");
				if (bday != null){
					profileInfo[i] = "Birthday: " + bday.toString();
					i++;
				}
				else
					System.out.println("No bday found!");
				if (hometown != null){
					profileInfo[i] = "Hometown: " + hometown.toString();
					i++;
				}
				else
					System.out.println("No hometown found!");
			}
		}
		finally { qexec.close();}
	}

	/*Query logic for friends' names*/
	private void runQuery(String queryRequest, Model model)
	{
		StringBuffer queryStr = new StringBuffer();
		// Establish Prefixes
		queryStr.append("PREFIX rdfs" + ": <" + "http://www.w3.org/2000/01/rdf-schema#" + "> ");
		queryStr.append("PREFIX rdf" + ": <" + "http://www.w3.org/1999/02/22-rdf-syntax-ns#" + "> ");
		queryStr.append("PREFIX friends" + ": <" + "http://xmlns.com/friends/0.1/"+ "> ");
		//Now add query
		queryStr.append(queryRequest);
		Query query = QueryFactory.create(queryStr.toString());
		QueryExecution qexec = QueryExecutionFactory.create(query, model);
		int i= 0;
		list[i] = "Facebook & Gmail Friends";
		i++;
		list[i] = "------------------------------------";
		i++;
		try
		{
			ResultSet response = qexec.execSelect();
			while( response.hasNext())
			{
				QuerySolution soln = response.nextSolution();
				RDFNode name = soln.get("?name");
				if( name != null )
				{
					list[i]=name.toString();
					i++;}
				else
					System.out.println("No Friends found!");
			}
		}
		finally { qexec.close();}
		}
	/*Query logic for events*/
	private void runQuery2(String queryRequest, Model model)
	{
		StringBuffer queryStr = new StringBuffer();
		// Establish Prefixes
		queryStr.append("PREFIX rdfs" + ": <" + "http://www.w3.org/2000/01/rdf-schema#" + "> ");
		queryStr.append("PREFIX rdf" + ": <" + "http://www.w3.org/1999/02/22-rdf-syntax-ns#" + "> ");
		queryStr.append("PREFIX events" + ": <" + "http://xmlns.com/events/0.1/"+ "> ");
		//Now add query
		queryStr.append(queryRequest);
		Query query = QueryFactory.create(queryStr.toString());
		QueryExecution qexec = QueryExecutionFactory.create(query, model);
		int i= 0;
		events[i] = "Events List";
		i++;
		try
		{
			ResultSet response = qexec.execSelect();
			while( response.hasNext())
			{
				QuerySolution soln = response.nextSolution();
				RDFNode title = soln.get("?title");
				RDFNode date = soln.get("?date");
				RDFNode time = soln.get("?time");
				events[i] = "------------------------------------";
				i++;
				if( title != null )
				{
					events[i]="Title: " + title.toString();
					i++;}
				else
					System.out.println("No Title found!");

				if (date != null){
					events[i] = "Date: " +date.toString();
					i++;
				}
				else
					System.out.println("No date found!");
				if (time != null){
					events[i] = "Time: " + time.toString();
					i++;
				}
				else
					System.out.println("No time found!");
			}
		}
		finally { qexec.close();}
	}

	public void addAlignment()
	{
		// State that :Person is equivalentClass of friends:Person
		Resource resource = schema.createResource(defaultNameSpace + "Person");
		Property prop = schema.createProperty("http://www.w3.org/2002/07/owl#equivalentClass");
		Resource obj = schema.createResource("http://xmlns.com/friends/0.1/Person");
		schema.add(resource,prop,obj);

		//State that :name is an equivalentProperty of friends:name
		resource = schema.createResource(defaultNameSpace + "name");
		prop = schema.createProperty("http://www.w3.org/2002/07/owl#equivalentProperty");
		obj = schema.createResource("http://xmlns.com/friends/0.1/name");
		schema.add(resource,prop,obj);
		//State that :hasFriend is an equivalent property of friends:knows
		resource = schema.createResource(defaultNameSpace + "hasFriend");
		prop = schema.createProperty("http://www.w3.org/2002/07/owl#equivalentProperty");
		obj = schema.createResource("http://xmlns.com/friends/0.1/knows");
		schema.add(resource,prop,obj);
	}

	private void bindReasoner()
	{
		//creating an inference model for friends
		Reasoner reasoner = ReasonerRegistry.getOWLReasoner();
		reasoner = reasoner.bindSchema(schema);
		inferredFriends = ModelFactory.createInfModel(reasoner, _friends);

		//creating an inference model for events
		Reasoner reasoner1 =  ReasonerRegistry.getOWLReasoner();
		reasoner1 = reasoner1.bindSchema(schema1);
		inferredEvents = ModelFactory.createInfModel(reasoner1, _events);
	}
}
