package telephony;

import home.HomeScreen;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import nr.app.R;
import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import data.ByteArray;

public class StockListDownloader extends AsyncTask<Void, Integer, String>{
	private ProgressDialog mProgressDialog;
	private final int NUM_BYTES_PER_DOWNLOAD = 1000;
	private boolean goOnline;
	private Context context;
	
	private final String nyse_link="http://www.nasdaq.com/screening/companies-by-industry.aspx?exchange=NYSE&render=download";
	private final String nasdaq_link="http://www.nasdaq.com/screening/companies-by-industry.aspx?exchange=NASDAQ&render=download";
	private final String amex_link="http://www.nasdaq.com/screening/companies-by-industry.aspx?exchange=AMEX&render=download";
	
	public StockListDownloader(Context context, ProgressDialog d, boolean goOnline)
	{
		mProgressDialog = d;
		this.goOnline=goOnline;
		this.context=context;
	}
	
	@Override
	protected String doInBackground(Void... v)
	{
		if(goOnline)
			mProgressDialog.setMessage("Downloading...");

		try {
			String nyse_data;
			String nasdaq_data;
			String amex_data;
			
			if(goOnline)
			{
				
			URL url = new URL(nyse_link);
			
			ByteArray stringBytes = new ByteArray(500000); //conn.getContentLength()
			InputStream stream = new BufferedInputStream(url.openStream());
			
			int downloadedBytes=0;
			int temp=0;
			while(true)
			{
				byte[] b = new byte[NUM_BYTES_PER_DOWNLOAD];
				temp = stream.read(b, 0, b.length);
				
				if(temp == -1)
					break;
				
				if(temp != b.length) //trim
				{
					byte[] b2 = new byte[temp];
					for(int i=0; i<temp; i++)
					{
						b2[i] = b[i];
					}
					b = b2;
				}
				
				
				stringBytes.add(b);
				downloadedBytes = downloadedBytes + temp;
				publishProgress(new Integer(Math.min(39, (int)(((double)downloadedBytes / 457728)*(80.0*(457728/928768.0))) )  ));
			}
			nyse_data = new String(stringBytes.getArray());
			
			//
			url = new URL(nasdaq_link);
			
			stringBytes = new ByteArray(500000); //conn.getContentLength()
			stream = new BufferedInputStream(url.openStream());
			
			downloadedBytes=0;
			temp=0;
			while(true)
			{
				byte[] b = new byte[NUM_BYTES_PER_DOWNLOAD];
				temp = stream.read(b, 0, b.length);
				
				if(temp == -1)
					break;
				
				if(temp != b.length) //trim
				{
					byte[] b2 = new byte[temp];
					for(int i=0; i<temp; i++)
					{
						b2[i] = b[i];
					}
					b = b2;
				}
				
				
				stringBytes.add(b);
				downloadedBytes = downloadedBytes + temp;
				publishProgress(new Integer(Math.min(74, 40 + (int)(((double)downloadedBytes / 402432)*(80.0*(402432/928768.0))) )  ));
			}
			nasdaq_data = new String(stringBytes.getArray());
			
			
			//
			url = new URL(amex_link);
			
			stringBytes = new ByteArray(100000); //conn.getContentLength()
			stream = new BufferedInputStream(url.openStream());
			
			downloadedBytes=0;
			temp=0;
			while(true)
			{
				byte[] b = new byte[NUM_BYTES_PER_DOWNLOAD];
				temp = stream.read(b, 0, b.length);
				
				if(temp == -1)
					break;
				
				if(temp != b.length) //trim
				{
					byte[] b2 = new byte[temp];
					for(int i=0; i<temp; i++)
					{
						b2[i] = b[i];
					}
					b = b2;
				}
				
				stringBytes.add(b);
				downloadedBytes = downloadedBytes + temp;
				publishProgress(new Integer(Math.min(80, 74 +  (int)(((double)downloadedBytes / 68608)*(80.0*(68608/928768.0))) )  ));
			}
			amex_data = new String(stringBytes.getArray());
			
			}
			else
			{
			InputStream nyseStream = context.getResources().openRawResource(R.raw.nyse_list);
			InputStream nasdaqStream = context.getResources().openRawResource(R.raw.nasdaq_list);
			InputStream amexStream = context.getResources().openRawResource(R.raw.amex_list);
			

			InputStreamReader is = new InputStreamReader(nyseStream);
			StringBuilder sb=new StringBuilder();
			BufferedReader br = new BufferedReader(is);
			String read = br.readLine();

			while(read != null) {
				read=read+"\n";
			    sb.append(read);
			    read =br.readLine();

			}
			nyse_data=sb.toString();
			
			is = new InputStreamReader(nasdaqStream);
			sb=new StringBuilder();
			br = new BufferedReader(is);
			read = br.readLine();

			while(read != null) {
				read=read+"\n";
			    sb.append(read);
			    read =br.readLine();

			}
			nasdaq_data=sb.toString();
			
			is = new InputStreamReader(amexStream);
			sb=new StringBuilder();
			br = new BufferedReader(is);
			read = br.readLine();

			while(read != null) {
				read=read+"\n";
			    sb.append(read);
			    read =br.readLine();

			}
			amex_data=sb.toString();
			
			}
			
			
			
			Reader reader = new StringReader(nyse_data);
			CSVReader csvReader=new CSVReader((Reader)reader);
			List<String[]> nyse_list = csvReader.readAll();
			csvReader.close();
			
			reader = new StringReader(nasdaq_data);
			csvReader=new CSVReader((Reader)reader);
			List<String[]> nasdaq_list = csvReader.readAll();
			csvReader.close();
			
			reader = new StringReader(amex_data);
			csvReader=new CSVReader((Reader)reader);
			List<String[]> amex_list = csvReader.readAll();
			csvReader.close();
			
			
			if(goOnline)
			{
				publishProgress(82);
			}
			
			HomeScreen.stocklistDatabaseStatic.beginTransaction();
			for(int i=1; i<nyse_list.size(); i++)
			{
				String[] stock = nyse_list.get(i);
				publishProgress(82 + Math.min(9, (int)(((double)i/nyse_list.size())*9.0) ));
				
				String statement = "INSERT OR IGNORE INTO stocklist (_id, name, market_cap, sector, industry, searches) VALUES" +
						" ('"+stock[0]+"','"+stock[1]+"',"+stock[3]+",'"+stock[6]+"','"+stock[7]+"'"+",0"+");";
				HomeScreen.stocklistDatabaseStatic.execSQL(statement);
			}
			for(int i=1; i<nasdaq_list.size(); i++)
			{
				String[] stock = nasdaq_list.get(i);
				publishProgress(91 + Math.min(8, (int)(((double)i/nyse_list.size())*8.0) ));
				String statement = "INSERT OR IGNORE INTO stocklist (_id, name, market_cap, sector, industry, searches) VALUES" +
						" ('"+stock[0]+"','"+stock[1]+"',"+stock[3]+",'"+stock[6]+"','"+stock[7]+"'"+",0"+");";
				HomeScreen.stocklistDatabaseStatic.execSQL(statement);
			}
			for(int i=1; i<amex_list.size(); i++)
			{
				String[] stock = amex_list.get(i);
				String statement = "INSERT OR IGNORE INTO stocklist (_id, name, market_cap, sector, industry, searches) VALUES" +
						" ('"+stock[0]+"','"+stock[1]+"',"+stock[3]+",'"+stock[6]+"','"+stock[7]+"'"+",0"+");";
				HomeScreen.stocklistDatabaseStatic.execSQL(statement);
			}
			
			if(goOnline)
			{
				publishProgress(100);
			}
			
			HomeScreen.stocklistDatabaseStatic.setTransactionSuccessful();
			HomeScreen.stocklistDatabaseStatic.endTransaction();
			
			Cursor c = HomeScreen.stocklistDatabaseStatic.rawQuery("select * from stocklist", null);
			c.close();

			if(!goOnline)
			{
				mProgressDialog.cancel();
			}
			
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
		
		
	}
	
	@Override
	protected void onPostExecute(String s)
	{
		mProgressDialog.cancel();
		
	}
	
	@Override
	protected void onProgressUpdate(Integer... i)
	{
		mProgressDialog.setProgress(i[0].intValue());
	}

}
