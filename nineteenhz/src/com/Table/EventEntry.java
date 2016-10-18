package com.Table;

public class EventEntry {
	
	String mDateAndTime;
	String mEventTitleAtVenue;
	String mTags;
	String mPriceAndAge;
	String mOrganizers;
	String mFacebookLink;
	String mTixPageLink;
	
	public EventEntry(String dateAndTime, String eventTitleAtVenue, String tags,
			String priceAndAge, String organizers, String facebookLink, String tixPageLink){
		
		this.mDateAndTime = dateAndTime;
		this.mEventTitleAtVenue = eventTitleAtVenue;
		this.mTags = tags;
		this.mPriceAndAge = priceAndAge;
		this.mOrganizers = organizers;
		this.mFacebookLink = facebookLink;
		this.mTixPageLink = tixPageLink;
		
	}
}
