package com.Table;


import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class TableMainLayout extends RelativeLayout {

	public final String TAG = "MAIN TABLE";
	
	public String mUrl;
	
	public ArrayList<String> musicSelections; 
	
	// set the header titles
	private String headers[] = {
		" Date & \n Time ",
		" Name & Venue ",
		" Styles & Tags ",
		" Price & Age ",
		" Organizers ",
		" Facebook ",
		" Tix / Page "
	};
	
	TableLayout dateAndTimeHeaderTable;
	TableLayout eventInfoHeaderTable;
	TableLayout dateAndTimeTable;
	TableLayout eventInfoTable;
	
	HorizontalScrollView eventInfoHeaderHorizontalScrollView;
	HorizontalScrollView eventInfoTableHorizontalScrollView;

	ScrollView dateAndTimeTableScrollView;
	ScrollView eventInfoTableScrollView;
	
	Context mContext;
	Activity mActivity;
	
	List<EventEntry> eventEntries = new ArrayList<EventEntry>();
	
	int headerCellsWidth[] = new int[headers.length];
	
	public TableMainLayout(Context context, String url, Activity activity) {
		
		super(context);
		
		mContext = context;
		mActivity = activity;
		
		musicSelections = new ArrayList<String>(
				PreferenceManager.getDefaultSharedPreferences(
						context).getStringSet("MUSICPREFS", new HashSet<String>()));
		
		mUrl = url;
		
		//TODO: This is an expensive chore, we need to fetch this from localDB
		new FetchTask().execute();
		
		initComponents();
		setComponentIds();
		setAllScrollViewTags();
		
		eventInfoHeaderHorizontalScrollView.addView(eventInfoHeaderTable);
		dateAndTimeTableScrollView.addView(dateAndTimeTable);
		eventInfoTableScrollView.addView(eventInfoTableHorizontalScrollView);
		eventInfoTableHorizontalScrollView.addView(eventInfoTable);
		
		addComponentsToMainLayout();
		setBackgroundColor(Color.WHITE);
		
		addDateAndTimeHeader();
		addEventInfoHeader();	
		resizeHeaderHeight();
		
		getTableRowHeaderCellWidth();
			
		generateEventEntryTables();	
		resizeBodyTableRowHeight();
	}
	
	//TODO: Sync this to server updates
	public void refetchTable()
	{
		new FetchTask().execute();
	}
	
	private void initComponents(){
		
		dateAndTimeHeaderTable = new TableLayout(mContext); 
		eventInfoHeaderTable = new TableLayout(mContext); 
		dateAndTimeTable = new TableLayout(mContext); 
		eventInfoTable = new TableLayout(mContext);
		
		eventInfoHeaderHorizontalScrollView = new CustomHorizontalScrollView(mContext);
		eventInfoTableHorizontalScrollView = new CustomHorizontalScrollView(mContext);
		
		dateAndTimeTableScrollView = new CustomScrollView(mContext);
		eventInfoTableScrollView = new CustomScrollView(mContext);
		
		dateAndTimeHeaderTable.setBackgroundColor(Color.GREEN);
		eventInfoHeaderHorizontalScrollView.setBackgroundColor(Color.LTGRAY);
		
	}
	
	private void setComponentIds(){
		dateAndTimeHeaderTable.setId(1);
		eventInfoHeaderHorizontalScrollView.setId(2);
		dateAndTimeTableScrollView.setId(3);
		eventInfoTableScrollView.setId(4);
	}
	
	// set tags for some horizontal and vertical scroll view
	private void setAllScrollViewTags(){
		
		eventInfoHeaderHorizontalScrollView.setTag("eventInfoHeaderHorizontalScrollView");
		eventInfoTableHorizontalScrollView.setTag("eventInfoTableHorizontalScrollView");		
		dateAndTimeTableScrollView.setTag("dateAndTimeTableScrollView");
		eventInfoTableScrollView.setTag("eventInfoTableScrollView");
	}
	
	// Relative Layout magic
	private void addComponentsToMainLayout(){
		
		// RelativeLayout params were very useful here
		// the addRule method is the key to arrange the components properly
		RelativeLayout.LayoutParams eventInfoHeaderParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		eventInfoHeaderParams.addRule(RelativeLayout.RIGHT_OF, dateAndTimeHeaderTable.getId());
		
		RelativeLayout.LayoutParams dateAndTimeTableParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		dateAndTimeTableParams.addRule(RelativeLayout.BELOW, dateAndTimeHeaderTable.getId());
		
		RelativeLayout.LayoutParams eventInfoTableParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		eventInfoTableParams.addRule(RelativeLayout.RIGHT_OF, dateAndTimeTableScrollView.getId());
		eventInfoTableParams.addRule(RelativeLayout.BELOW, eventInfoHeaderHorizontalScrollView.getId());
		
		addView(dateAndTimeHeaderTable);
		addView(eventInfoHeaderHorizontalScrollView, eventInfoHeaderParams);
		addView(dateAndTimeTableScrollView, dateAndTimeTableParams);
		addView(eventInfoTableScrollView, eventInfoTableParams);
			
	}
	

	
	private void addDateAndTimeHeader(){
		dateAndTimeHeaderTable.addView(dateAndTimeHeader());
	}
	
	private void addEventInfoHeader(){
		eventInfoHeaderTable.addView(eventInfoHeader());
	}
	
	// generate table row of table A
	TableRow dateAndTimeHeader(){
		
		TableRow dateAndTimeHeader = new TableRow(mContext);
		TextView textView = headerTextView(headers[0]);
		dateAndTimeHeader.addView(textView);
		
		return dateAndTimeHeader;
	}
	
	// generate table row of table B
	TableRow eventInfoHeader(){
		
		TableRow eventInfoHeader = new TableRow(mContext);
		int headerFieldCount = headers.length;
		
		TableRow.LayoutParams params = new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.MATCH_PARENT);
		params.setMargins(2, 0, 0, 0);
		
		for(int x=1; x<(headerFieldCount); x++){
			TextView textView = headerTextView(headers[x]);
			textView.setLayoutParams(params);
			eventInfoHeader.addView(textView);
		}
		
		return eventInfoHeader;
	}
	
	public void refreshTables()
	{
		generateEventEntryTables();	
		resizeBodyTableRowHeight();
	}
	
	private void generateEventEntryTables(){
		
		dateAndTimeTable.removeAllViews();
		eventInfoTable.removeAllViews();
		
		for(EventEntry event : eventEntries){
						
			TableRow tableRowForDateAndTime = tableRowForDateAndTime(event);
			TableRow tableRowForEventInfo = tableRowForEventInfo(event);
			
			tableRowForDateAndTime.setBackgroundColor(Color.LTGRAY);
			tableRowForEventInfo.setBackgroundColor(Color.LTGRAY);
			
			dateAndTimeTable.addView(tableRowForDateAndTime);
			eventInfoTable.addView(tableRowForEventInfo);
			
		}
	}
	
	TableRow tableRowForDateAndTime(EventEntry event){
		
		TableRow.LayoutParams params = new TableRow.LayoutParams(headerCellsWidth[0],LayoutParams.MATCH_PARENT);
		params.setMargins(0, 2, 0, 0);
		
		TableRow tableRowForDateAndTime = new TableRow(mContext);
		TextView textView = bodyTextView(event.mDateAndTime);
		tableRowForDateAndTime.addView(textView,params);
		
		return tableRowForDateAndTime;
	}
	
	TableRow tableRowForEventInfo(EventEntry event){

		TableRow tableRowForEventInfo = new TableRow(mContext);
		
		int loopCount = ((TableRow)eventInfoHeaderTable.getChildAt(0)).getChildCount();
		final String info[] = {
			event.mEventTitleAtVenue,
			event.mTags,
			event.mPriceAndAge,
			event.mOrganizers,
			event.mFacebookLink,
			event.mTixPageLink
		};
		
		for(int x=0 ; x<loopCount; x++){
			TableRow.LayoutParams params = new TableRow.LayoutParams(headerCellsWidth[x+1],LayoutParams.MATCH_PARENT);
			params.setMargins(2, 2, 0, 0);
			
			TextView textView = bodyTextView(info[x]);
			
			//Links to Facebook page, and Tickets/Other page
			if (x == 4 || x == 5)
			{
				final int linkIndex = x;
				
				textView.setOnClickListener(new OnClickListener() {
					
                    @Override
                    public void onClick(View view)
                    {
                        Uri address= Uri.parse(info[linkIndex]);  
                        Intent browser= new Intent(Intent.ACTION_VIEW, address);  
                        mActivity.startActivity(browser);  
                    }

                });
			}
			
			tableRowForEventInfo.addView(textView,params);
		}
		
		return tableRowForEventInfo;
		
	}
	
	// table cell standard TextView
	TextView bodyTextView(String label){
		
		TextView bodyTextView = new TextView(mContext);
		bodyTextView.setBackgroundColor(Color.WHITE);
		bodyTextView.setText(label);
		bodyTextView.setGravity(Gravity.CENTER);
		bodyTextView.setPadding(5, 5, 5, 5);
		
		return bodyTextView;
	}
	
	// header standard TextView
	TextView headerTextView(String label){
		
		TextView headerTextView = new TextView(mContext);
		headerTextView.setBackgroundColor(Color.WHITE);
		headerTextView.setText(label);
		headerTextView.setGravity(Gravity.CENTER);
		headerTextView.setPadding(5, 5, 5, 5);
		headerTextView.setTextSize(24);
		return headerTextView;
	}
	
	// resizing TableRow height starts here
	void resizeHeaderHeight() {
		
		TableRow productNameHeaderTableRow = (TableRow) dateAndTimeHeaderTable.getChildAt(0);
		TableRow productInfoTableRow = (TableRow)  eventInfoHeaderTable.getChildAt(0);

		int rowAHeight = viewHeight(productNameHeaderTableRow);
		int rowBHeight = viewHeight(productInfoTableRow);

		TableRow tableRow = rowAHeight < rowBHeight ? productNameHeaderTableRow : productInfoTableRow;
		int finalHeight = rowAHeight > rowBHeight ? rowAHeight : rowBHeight;

		matchLayoutHeight(tableRow, finalHeight);
	}
	
	void getTableRowHeaderCellWidth(){
		
		int dateAndTimeHeaderCount = ((TableRow)dateAndTimeHeaderTable.getChildAt(0)).getChildCount();
		int eventInfoHeaderCount = ((TableRow)eventInfoHeaderTable.getChildAt(0)).getChildCount();;
		
		for(int x = 0; x < (dateAndTimeHeaderCount+eventInfoHeaderCount); x++){
			
			if (x == 0)
			{
				headerCellsWidth[x] = viewWidth(((TableRow)dateAndTimeHeaderTable.getChildAt(0)).getChildAt(x));
			}
			else
			{
				headerCellsWidth[x] = viewWidth(((TableRow)eventInfoHeaderTable.getChildAt(0)).getChildAt(x-1));
			}
			
		}
	}
	
	void resizeBodyTableRowHeight(){
		
		int dateAndTimeTableCount = dateAndTimeTable.getChildCount();
		
		for (int x = 0; x < dateAndTimeTableCount; x++)
		{		
			TableRow productNameHeaderTableRow = (TableRow) dateAndTimeTable.getChildAt(x);
			TableRow productInfoTableRow = (TableRow) eventInfoTable.getChildAt(x);
	
			int rowAHeight = viewHeight(productNameHeaderTableRow);
			int rowBHeight = viewHeight(productInfoTableRow);
	
			TableRow tableRow = rowAHeight < rowBHeight ? productNameHeaderTableRow : productInfoTableRow;
			int finalHeight = rowAHeight > rowBHeight ? rowAHeight : rowBHeight;

			matchLayoutHeight(tableRow, finalHeight);		
		}
		
	}
	
	// make a standard TableRow height
	private void matchLayoutHeight(TableRow tableRow, int height) {
		
		int tableRowChildCount = tableRow.getChildCount();
		
		// if a TableRow has only 1 child
		if (tableRow.getChildCount() == 1){
			
			View view = tableRow.getChildAt(0);
			TableRow.LayoutParams params = (TableRow.LayoutParams) view.getLayoutParams();
			params.height = height - (params.bottomMargin + params.topMargin);
			
			return ;
		}
		
		// if a TableRow has more than 1 child
		for (int x = 0; x < tableRowChildCount; x++) {
			
			View view = tableRow.getChildAt(x);
			
			TableRow.LayoutParams params = (TableRow.LayoutParams) view.getLayoutParams();

			if (!isHighestLayout(tableRow, x)) {
				params.height = height - (params.bottomMargin + params.topMargin);
				return;
			}
		}
	}

	// check if the view has the highest height in a TableRow
	private boolean isHighestLayout(TableRow tableRow, int layoutPosition) {
		
		int tableRowChildCount = tableRow.getChildCount();
		int heighestViewPosition = -1;
		int viewHeight = 0;

		for (int x = 0; x < tableRowChildCount; x++) {
			View view = tableRow.getChildAt(x);
			int height = viewHeight(view);

			if (viewHeight < height) {
				heighestViewPosition = x;
				viewHeight = height;
			}
		}

		return heighestViewPosition == layoutPosition;
	}

	// read a view's height
	private int viewHeight(View view) {
		view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
		return view.getMeasuredHeight();
	}

	// read a view's width
	private int viewWidth(View view) {
		view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
		return view.getMeasuredWidth();
	}

	class CustomHorizontalScrollView extends HorizontalScrollView{

		public CustomHorizontalScrollView(Context context) {
			super(context);
		}
		
		@Override
		protected void onScrollChanged(int l, int t, int oldl, int oldt) {
			String tag = (String) this.getTag();
			
			//TODO: String matching is evil, fix this
			if (tag.equalsIgnoreCase("eventInfoHeaderHorizontalScrollView"))
			{
				eventInfoTableHorizontalScrollView.scrollTo(l, 0);
			}
			else
			{
				eventInfoHeaderHorizontalScrollView.scrollTo(l, 0);
			}
		}
		
	}

	class CustomScrollView extends ScrollView{

		public CustomScrollView(Context context) {
			super(context);
		}
		
		@Override
		protected void onScrollChanged(int l, int t, int oldl, int oldt) {
			
			String tag = (String) this.getTag();
			
			//TODO: String matching is evil, fix this
			if (tag.equalsIgnoreCase("dateAndTimeTableScrollView"))
			{
				eventInfoTableScrollView.scrollTo(0, t);
			}
			else
			{
				dateAndTimeTableScrollView.scrollTo(0 , t);
			}
		}
	}

    private class FetchTask extends AsyncTask<Void, Void, String> {
	  	
    	Document doc;
    	
  	  	@Override
  	  	protected String doInBackground(Void... params) {
  	  		String title = "";
  	  		try 
  	  		{    	    	
  	  			doc = Jsoup.connect(mUrl).get();
  	  		} 
  	  		catch (IOException e) 
  	  		{
  	  			e.printStackTrace();
  	  		}
  	  		return title;   
  	  	} 

  	  	@SuppressLint("NewApi") @Override
  	  	protected void onPostExecute(String result) {        
		    if (doc != null)
		    {		    	  		    	
		    	eventEntries.clear();
		    	
		    	Element table = doc.select("table.table").first();
		    			    	
		        Elements oddEvents = table.select("tr.odd");
		        Elements evenEvents = table.select("tr.even");
		        
		        Iterator<Element> oddIterator = oddEvents.iterator();
		        Iterator<Element> evenIterator = evenEvents.iterator();
		        
		        while (oddIterator.hasNext() || evenIterator.hasNext())
		        {		
		        	//Even needs to be first to generate table correctly
		        	if (evenIterator.hasNext())
		        	{
		        		Elements evenEventElements = evenIterator.next().getElementsByTag("td");
		        		if (evenEventElements.first()!= null && !evenEventElements.first().text().isEmpty())
		        		{
				        	String dateAndTime = "";
				        	String eventTitleAtVenue = "";
				        	String tags = "";
				        	String priceAndAge = "";
				        	String organizers = "";
				        	String facebookLink = "";
				        	String tixPageLink = "";
				        	boolean hadLinkInTitle = false;
		        			int i = 0;
		        			
	  		  		    	for (Element evenEventElement : evenEventElements)
	  		  		    	{	
	  		  		    		if (i == 0)
  		  		    				dateAndTime = evenEventElement.text(); 
  		  		    			if (i == 1)
  		  		    			{
  		  		    				eventTitleAtVenue = evenEventElement.text();
  		  		    				//Assume this is the facebook link for now
  		  		    				facebookLink = evenEventElement.select("a").first().attr("href");
  		  		    				hadLinkInTitle = true;
  		  		    			}
  		  		    			if (i == 2)
  		  		    				tags = evenEventElement.text(); 
  		  		    			if (i == 3)
  		  		    				priceAndAge = evenEventElement.text();
  		  		    			if (i == 4)
  		  		    				organizers = evenEventElement.text(); 
  		  		    			if (i == 5 && evenEventElement.select("a").first() != null)
  		  		    				if (hadLinkInTitle)
  		  		    				{
  		  		    					//If there are two links, first one was tixPageLink
  		  		    					tixPageLink = facebookLink;
  		  		    					//This one is facebook link
  		  		    					facebookLink = evenEventElement.select("a").first().attr("href");
  		  		    				}
	  		  		    		
	  		  		    		i++;
	  		  		    	}
				        	
	  		  		    	boolean wouldEnjoyEvent = true;
	  		  		    	
	  		  		    	for (String musicSelection : musicSelections)
	  		  		    	{
	  		  		    		if (tags.contains(musicSelection))
	  		  		    			break;
	  		  		    		wouldEnjoyEvent = false;
	  		  		    	}
	  		  		    	
	  		  		    	if (wouldEnjoyEvent)
	  		  		    	{
	  		  		    		EventEntry event = new EventEntry(
	  		  		    				dateAndTime, eventTitleAtVenue, tags,
	  		  		    				priceAndAge, organizers, facebookLink, tixPageLink);		        		
	  		  		    		eventEntries.add(event);
	  		  		    	}
		        		}
		        	}
		        	if (oddIterator.hasNext())
		        	{
		        		Elements oddEventElements = oddIterator.next().getElementsByTag("td");
		        		if (oddEventElements.first()!= null && !oddEventElements.first().text().isEmpty())
		        		{
				        	String dateAndTime = "";
				        	String eventTitleAtVenue = "";
				        	String tags = "";
				        	String priceAndAge = "";
				        	String organizers = "";
				        	String facebookLink = "";
				        	String tixPageLink = "";
				        	boolean hadLinkInTitle = false;
							int j = 0;
		        			
	  		  		    	for (Element oddEventElement : oddEventElements)
	  		  		    	{	
	  		  		    		if (j == 0)
  		  		    				dateAndTime = oddEventElement.text(); 
  		  		    			if (j == 1)
  		  		    			{
  		  		    				eventTitleAtVenue = oddEventElement.text();
  		  		    				facebookLink = oddEventElement.select("a").first().attr("href");
  		  		    				hadLinkInTitle = true;
  		  		    			}
  		  		    			if (j == 2)
  		  		    				tags = oddEventElement.text();  
  		  		    			if (j == 3)
  		  		    				priceAndAge = oddEventElement.text();
  		  		    			if (j == 4)
  		  		    				organizers = oddEventElement.text();  
  		  		    			if (j == 5 && oddEventElement.select("a").first() != null)
  		  		    				if (hadLinkInTitle)
  		  		    				{
  		  		    					//If there are two links, first one was tixPageLink
  		  		    					tixPageLink = facebookLink;
  		  		    					//This one is facebook link
  		  		    					facebookLink = oddEventElement.select("a").first().attr("href");
  		  		    				}
	  		  		    			  		    		
	  		  		    		j++;
	  		  		    	}
	  		  		    	
	  		  		    	boolean wouldEnjoyEvent = true;
	  		  		    	
	  		  		    	for (String musicSelection : musicSelections)
	  		  		    	{
	  		  		    		if (tags.contains(musicSelection))
	  		  		    			break;
	  		  		    		wouldEnjoyEvent = false;
	  		  		    	}
	  		  		    	
	  		  		    	if (wouldEnjoyEvent)
	  		  		    	{
	  		  		    		EventEntry event = new EventEntry(
			  		  	        	dateAndTime,
			  			        	eventTitleAtVenue,
			  			        	tags,
			  			        	priceAndAge,
			  			        	organizers,
			  			        	facebookLink, tixPageLink);		        		
				        		eventEntries.add(event);
	  		  		    	}
		        		}
		        	}  		        	
		        }
		        refreshTables();
		    }
		    else Log.e("JSOUP", "DOC WAS NULL");
  	  	}
  	}
}
