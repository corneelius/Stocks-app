package telephony;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringBufferInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.client.ClientProtocolException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import data.ByteArray;
import data.Chart;
import data.News;
import data.NewsArticle;
import data.Stats;
import data.Stock;
import data.StockData;
import data.StockGenerics;
import data.Types;

public class StockPopulatorHelper {
	
	private StockPopulator mCreator;
	private StockPopulatorOptions mSettings;
	private final int NUM_BYTES_PER_DOWNLOAD = 1000;
	
	
	//generics
	private final String LAST_TRADE = "l1"; //http://finance.yahoo.com/d/quotes.csv?s=STX&f=l1vc6k2nb2b3
	private final String VOLUME = "v";
	private final String NOMINAL_CHANGE = "c6";
	private final String PERCENT_CHANGE = "k2";
	private final String NAME = "n";
	
	//stats page one
	private final String ASK = "b2"; 
	private final String BID = "b3";
	private final String OPEN = "o";
	private final String PREVIOUS_CLOSE = "p";
	private final String DAY_HIGH = "g";
	private final String DAY_LOW = "h";
	private final String FIFTY_TWO_WEEK_RANGE = "w";
	//stats page two
	private final String DIVIDEND_PER_SHARE = "d";
	private final String DIVIDEND_YIELD = "y";
	private final String MARKET_CAP = "j1";
	private final String AVERAGE_DAILY_VOLUME = "a2";
	//stats page three
	private final String PE = "r";
	private final String PEG = "r5";
	private final String EPS = "e";
	private final String EPS_NEXT_YEAR = "e8";
	private final String EBITDA = "j4";
	private final String PRICE_TO_SALES = "p5";
	private final String PRICE_TO_BOOK = "p6";
	private final String SHORT_RATIO = "s7";
	
	public StockPopulatorHelper(StockPopulator creator, StockPopulatorOptions options)
	{
		this.mSettings = options;
		this.mCreator = creator;
		
	}
	//whatever returns through populate indicates that the method is done updating
	public StockData[] populate(StockData... input) throws 
	ParserConfigurationException, FactoryConfigurationError, SAXException, IOException
	{
		//input[] item must all be of the same type
		accumulatedProgress=0.0;
		if(!Internet.isReachable())
		{
			return null;
		}
		
		switch(mSettings)
		{
		case FILL_MARKET_NEWS:
		{
			if(input[0] instanceof News)
			for(int i=0;i<input.length;i++)
				{
					if(fetchMarketNews((News) input[i]) != 0) return null;
				}

			mCreator.publishProgressPublic(input);
			break;
		}
		case FILL_STOCK:
			{
				//updates are only sent for Stock objects
				//sends updates after each step is completed
				//returns an array containing the filled objects

				
				Chart[] charts = new Chart[input.length];
				StockGenerics[] stockGenerics = new StockGenerics[input.length];
				News[] news = new News[input.length];
				Stats[] stats = new Stats[input.length];
				
				for(int i=0; i<input.length; i++)
				{
					charts[i] = ((Stock)input[i]).getChart();
					stockGenerics[i] = ((Stock)input[i]).getStockGenerics();
					news[i] = ((Stock)input[i]).getNews();
					stats[i] = ((Stock)input[i]).getStats();
				}
				
				//POPULATE!!
				//Stock Generics
				if(fetchGenerics(stockGenerics) != 0) return null; //takes arrays of StockGenerics for efficiency
				mCreator.publishProgressPublic(stockGenerics); //return generics to my creator - StockPopulator
				
				//Charts
				for(int i=0;i<charts.length;i++)
				{
					if(fetchChart(charts[i]) != 0) return null;
				}
				mCreator.publishProgressPublic(charts);
				
				//Stats
				if(fetchStats(stats) != 0) return null; //takes arrays of Stats for efficiency
				mCreator.publishProgressPublic(stats);
				
				//News
				for(int i=0;i<news.length;i++)
				{
					if(fetchNews(news[i]) != 0) return null;
				}
				mCreator.publishProgressPublic(news);
				break;
				}
			
			case FILL_NEWS:
				{
					
					if(input[0] instanceof News)
					{
						for(int i=0;i<input.length;i++)
						{
							if(fetchNews((News) input[i]) != 0) return null;
						}
					}
					else
					{
						for(int i=0; i<input.length;i++)
						{
							if(fetchNews(((Stock) input[i]).getNews()) != 0) return null;
						}
					}
					break;
				}
			
			case FILL_CHART:
				{
					
					if(input[0] instanceof Chart)
					{
						for(int i=0;i<input.length;i++)
						{
							if(fetchChart((Chart) input[i]) !=0) return null;
						}
					}
					else
					{
						for(int i=0;i<input.length;i++)
						{
							if(fetchChart(((Stock)input[i]).getChart()) != 0) return null;
						}
					}
					break;
				}
			case FILL_STATS:
				{
					
					if(input[0] instanceof Stats)
					{
						Stats[] array = new Stats[input.length];
						for(int i=0;i<input.length;i++)
						{
							array[i] = (Stats)(input[i]);
						}
						if(fetchStats(array) != 0) return null;
					}
					else
					{
						Stats[] array = new Stats[input.length];
						for(int i=0;i<input.length;i++)
						{
							array[i] = ((Stock)input[i]).getStats();
						}
						if(fetchStats(array) != 0) return null;
					}
					break;
				}
				
			case FILL_WATCHLISTS:
			{
				 //intentionally did not add break;
			}
			
			case FILL_GENERICS:
				{
					
					if(input[0] instanceof StockGenerics)
					{
						StockGenerics[] array = new StockGenerics[input.length];
						for(int i=0;i<input.length;i++) //cast it to StockGenerics
						{
							array[i] = (StockGenerics)input[i];
						}
						if(fetchGenerics(array) != 0) return null;
						mCreator.publishProgressPublic(array);
					}
					else
					{
						StockGenerics[] array = new StockGenerics[input.length];
						for(int i=0;i<input.length;i++)
						{
							array[i] = ((Stock)input[i]).getStockGenerics();
						}
						if(fetchGenerics(array) != 0) return null;
					}
					
				}
				break;
			}
		return input;  //better have returned by now
	}
	private double accumulatedProgress;
	private synchronized String downloadBytes(String link, int dataLength)
	{
		ByteArray stringBytes=null;
		if(mSettings==StockPopulatorOptions.FILL_STOCK)
		{
			dataLength = 77045; //207027
		}
		try {
			URL url = new URL(link);
			URLConnection conn = url.openConnection();
			
			stringBytes = new ByteArray(dataLength); //conn.getContentLength()
			InputStream stream = new BufferedInputStream(conn.getInputStream());
			
			int status=0;
			while(true)
			{
				byte[] b = new byte[NUM_BYTES_PER_DOWNLOAD];
				status = stream.read(b, 0, b.length);
				
				if(status == -1)
					break;
				
				if(status != b.length) //trim
				{
					byte[] b2 = new byte[status];
					for(int i=0; i<status; i++)
					{
						b2[i] = b[i];
					}
					b = b2;
				}
				accumulatedProgress += ( ((double)status/dataLength) * 100);
				mCreator.updateProgress(accumulatedProgress); //max 100
				stringBytes.add(b);
			}
			
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		String string = new String(stringBytes.getArray());
		return new String(string);
	}
	private int fetchMarketNews(News input) throws ParserConfigurationException, FactoryConfigurationError, SAXException, IOException
	{
		DocumentBuilder builder = null;
		Document doc = null;
		builder = DocumentBuilderFactory.newInstance().newDocumentBuilder(); //throws Exceptions

		String data = downloadBytes("http://finance.yahoo.com/news/?format=rss", 88000);
		doc = builder.parse(new ByteArrayInputStream(data.getBytes())); //throws Exceptions


		NodeList list = doc.getElementsByTagName("item");

		String largestArticleTitle="";
		for(int i=0; i<list.getLength(); i++)
		{
			Node articleNode = list.item(i); //an article
			NodeList articleData = articleNode.getChildNodes();

			String title = "";
			String link = "";
			String description = "";
			Date pubDate = null;

			for(int x=0; x<articleData.getLength(); x++)
			{
				//Log.d("nr.app", "Node="+articleData.item(x).getNodeName());
				
				
				if(articleData.item(x).getNodeName().equals("title"))
				{
					title = articleData.item(x).getFirstChild().getNodeValue();
					if(title.length() > largestArticleTitle.length())
					{
						largestArticleTitle = title;
					}
					continue;
				}
				if(articleData.item(x).getNodeName().equals("link"))
				{
					link = articleData.item(x).getFirstChild().getNodeValue();
					continue;
				}
				if(articleData.item(x).getNodeName().equals("description"))
				{
					description = articleData.item(x).getFirstChild().getNodeValue();
					continue;
				}
				if(articleData.item(x).getNodeName().equals("pubDate"))
				{
					pubDate = Types.toDate(articleData.item(x).getFirstChild().getNodeValue());
					continue;
				}

			}
			NewsArticle article = new NewsArticle();
			article.setTitle(title);
			article.setDescription(description);
			article.setLink(link);
			article.setPubDate(pubDate);

			input.addArticle(article);

		}
		input.setLargestTitle(largestArticleTitle);

		return 0;
	
	}
	private int fetchNews(News input) throws ParserConfigurationException, FactoryConfigurationError, SAXException, IOException
	{
		String getMethod = new String("http://feeds.finance.yahoo.com/rss/2.0/headline?s="
											+input.getTicker().toString()+"&region=US&lang=en-US"); 
		String raw = null;
		
		raw = new String(downloadBytes(getMethod, 13312));


		@SuppressWarnings("deprecation")
		StringBufferInputStream inputStream = new StringBufferInputStream(raw);
		
		
		DocumentBuilder builder = null;

			builder = DocumentBuilderFactory.newInstance().newDocumentBuilder(); //throws Exceptions

		Document doc = null;
			
		doc = builder.parse(inputStream); //throws Exceptions

		
		NodeList list = doc.getElementsByTagName("item");
		
		String largestArticleTitle="";
		for(int i=0; i<list.getLength(); i++)
		{
			Node articleNode = list.item(i); //an article
			NodeList articleData = articleNode.getChildNodes();
			
			String title = "";
			String link = "";
			String description = "";
			Date pubDate = null;
			
			for(int x=0; x<articleData.getLength(); x++)
			{
				//make sure the node is not null
				try{
					articleData.item(x).getFirstChild().getNodeValue();
				}
				catch(NullPointerException e)
				{
					continue;
				}
				
				
				if(articleData.item(x).getNodeName().equals("title"))
				{
					title = articleData.item(x).getFirstChild().getNodeValue();
					if(title.length() > largestArticleTitle.length())
					{
						largestArticleTitle = title;
					}
					continue;
				}
				if(articleData.item(x).getNodeName().equals("link"))
				{
					link = articleData.item(x).getFirstChild().getNodeValue();
					continue;
				}
				if(articleData.item(x).getNodeName().equals("description"))
				{
					description = articleData.item(x).getFirstChild().getNodeValue();
					continue;
				}
				if(articleData.item(x).getNodeName().equals("pubDate"))
				{
					pubDate = Types.toDate(articleData.item(x).getFirstChild().getNodeValue());
					continue;
				}
				
			}
			NewsArticle article = new NewsArticle();
			article.setTitle(title);
			article.setDescription(description);
			article.setLink(link);
			article.setPubDate(pubDate);
			
			input.addArticle(article);
			
		}
		input.setLargestTitle(largestArticleTitle);
		
		return 0;
		
	}
	
	
	private int fetchChart(Chart input) throws ClientProtocolException, IOException
	{ 
		try{
		//http://ichart.finance.yahoo.com/table.csv?s=AAPL&a=00&b=01&c=2012&d=06&e=10&f=2012&g=d&ignore=.csv
		Calendar calendar = Calendar.getInstance();
		int day = calendar.get(Calendar.DATE);
		int month = calendar.get(Calendar.MONTH);
		int year = calendar.get(Calendar.YEAR);
		
		String getMethodMonthly = "http://ichart.finance.yahoo.com/table.csv?s=" +
				input.getTicker().toString()+"&a="+month+"&b="+day+"&c="+(year-10)+"&d="+month+"&e="+day+"&f="+(year)+"&g=m&ignore=.csv";  //first trading day of month
		
		
		
		String getMethodDaily = "http://ichart.finance.yahoo.com/table.csv?s=" +
				input.getTicker().toString()+"&a="+month+"&b="+day+"&c="+(year-5)+"&d="+month+"&e="+day+"&f="+year+"&g=d&ignore=.csv";  //daily for last five years
		
		
 		BufferedReader reader = null; 
 		List<String[]> convertedCSV = null;
 		
			reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(downloadBytes(getMethodDaily, 67584).getBytes()))); //throws Exceptions
 		
				CSVReader csvReader=new CSVReader((Reader)reader);
 		
 			convertedCSV = csvReader.readAll(); //throws Exceptions
		csvReader.close();
		//each String array in convertedCSV hold date, open, low, close, volume, adj close
 			
 			//hold raw data
 			ArrayList<Date> tenYearMonthlyDates = new ArrayList<Date>(120);
 			ArrayList<Double> tenYearMonthlyValues = new ArrayList<Double>(120);
 			ArrayList<Date> fiveYearDailyDates = new ArrayList<Date>(1830);
 			ArrayList<Double> fiveYearDailyValues = new ArrayList<Double>(1830);

		

		for(int i=1;i<convertedCSV.size();i++)
		{
			double parsed = Types.parseDouble(convertedCSV.get(i)[4]);
			int dash = convertedCSV.get(i)[0].indexOf('-');
			int dash2 = convertedCSV.get(i)[0].indexOf('-', dash+1);
			
			int itemMonth = Integer.parseInt(convertedCSV.get(i)[0].substring(dash+1, dash2));
			int itemDay = Integer.parseInt(convertedCSV.get(i)[0].substring(dash2+1));
			int itemYear = Integer.parseInt(convertedCSV.get(i)[0].substring(0, dash));
			
			fiveYearDailyValues.add(parsed);
			fiveYearDailyDates.add(new Date(itemYear-1900, itemMonth-1, itemDay));
		}

		input.setFiveYearDailyDates(Types.toDateArray(fiveYearDailyDates.toArray()));
		input.setFiveYearDailyValues(Types.toDoubleArray(fiveYearDailyValues.toArray()));
		
		//do it again		
		reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(downloadBytes(getMethodMonthly, 6144).getBytes()))); //throws Exceptions
 		
		csvReader=new CSVReader((Reader)reader);
	
		convertedCSV = csvReader.readAll(); //throws Exceptions
		
		for(int i=1; i<convertedCSV.size(); i++)
		{
			double parsed = Types.parseDouble(convertedCSV.get(i)[4]);
			int dash = convertedCSV.get(i)[0].indexOf('-');
			int dash2 = convertedCSV.get(i)[0].indexOf('-', dash+1);
			
			int itemMonth = Integer.parseInt(convertedCSV.get(i)[0].substring(dash+1, dash2));
			int itemDay = Integer.parseInt(convertedCSV.get(i)[0].substring(dash2+1));
			int itemYear = Integer.parseInt(convertedCSV.get(i)[0].substring(0, dash));
			
			tenYearMonthlyValues.add(parsed);
			tenYearMonthlyDates.add(new Date(itemYear-1900, itemMonth-1, itemDay));
			
		}
		
		input.setTenYearMonthlyDates(Types.toDateArray(tenYearMonthlyDates.toArray()));
		input.setTenYearMonthlyValues(Types.toDoubleArray(tenYearMonthlyValues.toArray()));
		csvReader.close();
		return 0;
		}
		catch(java.lang.IndexOutOfBoundsException e)
		{
			return 1;
		}
	
	}
	//takes more than one Stats object to increase efficiency
	private int fetchStats(Stats... input) throws ClientProtocolException, IOException //only method that takes an array of 
											//Stats since they can be processed together
	{
		try{
		String symbols = "";
		for(int i = 0; i<input.length; i++)
		{
			if(i==0)
				symbols = input[i].getTicker().toString();
			else
				symbols = symbols + "+" + input[i].getTicker().toString();
		}
		
		String getMethod = new String("http://finance.yahoo.com/d/quotes.csv?s=" + symbols  + "&f="
				+ OPEN 
				+ PREVIOUS_CLOSE 
				+ DAY_HIGH 
				+ DAY_LOW 
				+ FIFTY_TWO_WEEK_RANGE 
				+ DIVIDEND_PER_SHARE 
				+ DIVIDEND_YIELD 
				+ MARKET_CAP 
				+ AVERAGE_DAILY_VOLUME 
				+ PE 
				+ PEG 
				+ EPS 
				+ EPS_NEXT_YEAR 
				+ EBITDA 
				+ PRICE_TO_SALES 
				+ PRICE_TO_BOOK 
				+ SHORT_RATIO
				+ VOLUME);
 		BufferedReader reader = null; 
 		List<String[]> convertedCSV = null;
 		
 		
		reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(downloadBytes(getMethod, 119*symbols.length()).getBytes()))); //throws Exceptions
 		
 		CSVReader csvReader=new CSVReader((Reader)reader); 
 		
			 convertedCSV = csvReader.readAll(); //throws Exceptions
		csvReader.close();
		for(int i = 0; i<input.length; i++)
		{
			String[] results = convertedCSV.get(i);
			
			input[i].setOpen(Types.parseDouble(results[0]));
			input[i].setPreviousClose(Types.parseDouble(results[1]));
			input[i].setDayLow(Types.parseDouble(results[2]));
			input[i].setDayHigh(Types.parseDouble(results[3]));
			
				int dash = results[4].indexOf('-');       //price range will never be negative
			input[i].setFtWeekLow(Types.parseDouble(results[4].substring(0, dash)));
			input[i].setFtWeekHigh(Types.parseDouble(results[4].substring(dash+1, results[4].length())));
			input[i].setDividendPerShare(Types.parseDouble(results[5]));
			input[i].setDividendYield(Types.parseDouble(results[6]));
			input[i].setMarketCap(Types.parseDouble(results[7]));
			input[i].setAverageDailyVolume(Types.parseDouble(results[8]));
			input[i].setPE(Types.parseDouble(results[9]));
			input[i].setPEG(Types.parseDouble(results[10]));
			input[i].setEPS(Types.parseDouble(results[11]));
			input[i].setNextYearEPS(Types.parseDouble(results[12]));
			input[i].setEBITDA(Types.parseDouble(results[13]));
			input[i].setPriceToSales(Types.parseDouble(results[14]));
			input[i].setPriceToBook(Types.parseDouble(results[15]));
			input[i].setShortRatio(Types.parseDouble(results[16]));
			input[i].setVolume(Types.parseDouble(results[17]));
			
		}
		return 0;
		}
		catch(java.lang.IndexOutOfBoundsException e)
		{
			return 1;
		}
	}
	//takes more than one Stats object to increase efficiency
	private int fetchGenerics(StockGenerics[] input) throws ClientProtocolException, IOException
	{
		try{
			String symbols = "";
				for(int i = 0; i<input.length; i++)
				{
					if(i==0)
						symbols = input[i].getTicker().toString();
					else
						symbols = symbols + "+" + input[i].getTicker().toString();
				}
				
				String getMethod = "http://finance.yahoo.com/d/quotes.csv?s=" + symbols  + "&f=" 
							+ LAST_TRADE 
							+ VOLUME 
							+ NOMINAL_CHANGE 
							+ PERCENT_CHANGE 
							+ NAME
							+ BID
							+ ASK ;
		 		BufferedReader reader = null; 
		 		List<String[]> convertedCSV = null;
		 						

				reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(downloadBytes(getMethod, 67*input.length).getBytes()))); //throws Exceptions
		 		
		 		CSVReader csvReader=new CSVReader((Reader)reader); //throws Exceptions
		 		
				convertedCSV = csvReader.readAll(); //throws Exceptions
					csvReader.close();
					for(int i = 0; i<input.length; i++)
					{
						String[] results = convertedCSV.get(i);
						
						input[i].setLastTrade(Types.parseDouble(results[0]));
						input[i].setVolume((long)(Types.parseDouble(results[1])));
						input[i].setNominalChange(Types.parseDouble(results[2]));
						input[i].setPercentChange(Types.parseDouble(results[3]));
						input[i].setName(results[4]);
						input[i].setBid(Types.parseDouble(results[5]));
						input[i].setAsk(Types.parseDouble(results[6]));
						
					}
					return 0;
		}
		catch(java.lang.IndexOutOfBoundsException e)
		{
			return 1;
		}
	}
	
	public StockPopulatorOptions getSettings()
	{
		return mSettings;
	}
	
	

}
