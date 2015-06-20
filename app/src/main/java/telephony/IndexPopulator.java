package telephony;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import nr.app.R;


import android.content.Context;
import android.os.AsyncTask;
import data.ByteArray;
import data.Index;
import data.IndexEntry;

public class IndexPopulator extends AsyncTask<Void, Double, Index> {
		private IndexPopulatorResultReceiver caller;
		private final String RASPBERRY_URL="http://50.56.177.77:8181/RaspberryDB/GetIndicies";
		private final int NUM_BYTES_PER_DOWNLOAD = 1000;
		private int maxProgressPointsAffected;
		private Context mContext;
		
	public IndexPopulator(IndexPopulatorResultReceiver caller, Context context, int maxProgressPointsAffected)
	{
		super();
		this.caller = caller;
		this.maxProgressPointsAffected = maxProgressPointsAffected;
		mContext=context;
	}
	
	private String getOldData() throws IOException
	{
		InputStream is = mContext.getResources().openRawResource(R.raw.index);
		InputStreamReader ir = new InputStreamReader(is);
		StringBuilder sb=new StringBuilder();
		BufferedReader br = new BufferedReader(ir);
		String read = br.readLine();

		while(read != null) {
			read=read+"\n";
		    sb.append(read);
		    read =br.readLine();

		}
		return sb.toString();
	}
	
	private double progressLeftOver=0.0;
	@Override
	protected void onProgressUpdate(Double...i)
	{
		double weightedProgress = i[0].doubleValue() * ((double)maxProgressPointsAffected/100.0);
		weightedProgress += progressLeftOver;
		
		if((int)weightedProgress == 0)
		{
			progressLeftOver += weightedProgress;
			return;
		}
		
		int progress2 =  (int)weightedProgress;
		caller.updateProgress(progress2);
		
		progressLeftOver = weightedProgress % (int)weightedProgress;
		
	}

	@Override
	protected Index doInBackground(Void... params) {
		
		Index index = null;
		int timeOut=0;
		do
		{
			timeOut++;
			index = fetch(timeOut > 5 ? true : false);
		}while(index==null);
		return index;
	}
	private Index fetch(boolean forceLocal)
	{
		List<String[]> convertedCSV=null;
		boolean internetReachable=true;
		if(!Internet.isReachable() || forceLocal)
		{
			internetReachable=false;
			String data;
			try {
				data = new String(getOldData());
				Reader reader = new StringReader(data);
				CSVReader csvReader=new CSVReader((Reader)reader);
				convertedCSV = csvReader.readAll();
				csvReader.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else
		{

			try {

				URL url = new URL(RASPBERRY_URL);
				URLConnection conn = url.openConnection();
				int dataLength = conn.getContentLength();

				ByteArray stringBytes = new ByteArray(dataLength == -1 ? 1000 : dataLength);
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
					double progress = (((double)status/dataLength)) * 100.0;
					publishProgress(progress); //max 100
					stringBytes.add(b);
				}

				String data = new String(stringBytes.getArray());
				Reader reader = new StringReader(data);
				CSVReader csvReader=new CSVReader((Reader)reader);
				convertedCSV = csvReader.readAll();
				csvReader.close();

			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		ArrayList<IndexEntry> dji = null;
		BigDecimal dji_previous_close = null;
		BigDecimal dji_todays_close = null;
		ArrayList<IndexEntry> sandp = null;
		BigDecimal sandp_previous_close = null;
		BigDecimal sandp_todays_close = null;
		ArrayList<IndexEntry> nasdaq = null;
		BigDecimal nasdaq_previous_close = null;
		BigDecimal nasdaq_todays_close = null;
		boolean hasMarketClose=false;
		
		Date indexDate=null;
		if(convertedCSV == null)
		{
			return null;
		}
		else
		{
			indexDate = createDate(convertedCSV.get(0)[0]);
		}

		
		if(convertedCSV.size() > 1)  // more than just the date
		{
		if(indexDate.getDay()==0 || indexDate.getDay()==1 || indexDate.getDay()==6
				|| (indexDate.getHours() > 15 || (indexDate.getHours() == 15 && indexDate.getMinutes() > 5)) 
				||  indexDate.getHours() < 8  || (indexDate.getHours() == 8  && indexDate.getMinutes() < 31))
			//we are outside of market hours
		{
			dji = new ArrayList<IndexEntry>(convertedCSV.get(1).length-3); //put previous_close in its own variable
			dji_previous_close = new BigDecimal(convertedCSV.get(1)[1].substring(convertedCSV.get(1)[1].indexOf("$")+1));
			dji_todays_close = new BigDecimal(convertedCSV.get(1)[convertedCSV.get(1).length-1].substring(convertedCSV.get(1)[convertedCSV.get(1).length-1].indexOf("$")+1));
			for(int i=2;i<convertedCSV.get(1).length-1;i++)
			{
				BigDecimal d1 = new BigDecimal(convertedCSV.get(1)[i].substring(convertedCSV.get(1)[i].indexOf("$")+1));
				Date dd1 = createDate(convertedCSV.get(1)[i].substring(0, convertedCSV.get(1)[i].indexOf("$")));
				dji.add(i-2, new IndexEntry(dd1, d1));
			}
			sandp = new ArrayList<IndexEntry>(convertedCSV.get(2).length-3);
			sandp_previous_close = new BigDecimal(convertedCSV.get(2)[1].substring(convertedCSV.get(2)[1].indexOf("$")+1));
			sandp_todays_close = new BigDecimal(convertedCSV.get(2)[convertedCSV.get(2).length-1].substring(convertedCSV.get(2)[convertedCSV.get(2).length-1].indexOf("$")+1));
			for(int i=2;i<convertedCSV.get(2).length-1;i++)
			{
				BigDecimal d2 = new BigDecimal(convertedCSV.get(2)[i].substring(convertedCSV.get(2)[i].indexOf("$")+1));
				Date dd2 = createDate(convertedCSV.get(2)[i].substring(0, convertedCSV.get(2)[i].indexOf("$")));
				sandp.add(i-2, new IndexEntry(dd2, d2));
			}
			nasdaq = new ArrayList<IndexEntry>(convertedCSV.get(3).length-3);
			nasdaq_previous_close = new BigDecimal(convertedCSV.get(3)[1].substring(convertedCSV.get(3)[1].indexOf("$")+1));
			nasdaq_todays_close = new BigDecimal(convertedCSV.get(3)[convertedCSV.get(3).length-1].substring(convertedCSV.get(3)[convertedCSV.get(3).length-1].indexOf("$")+1));
			for(int i=2;i<convertedCSV.get(3).length-1;i++)
			{
				BigDecimal d3 = new BigDecimal(convertedCSV.get(3)[i].substring(convertedCSV.get(3)[i].indexOf("$")+1));
				Date dd3 = createDate(convertedCSV.get(3)[i].substring(0, convertedCSV.get(3)[i].indexOf("$")));
				nasdaq.add(i-2, new IndexEntry(dd3, d3));
			}
			hasMarketClose=true;
		}
		else
		{
			publishProgress(5.0);
		dji = new ArrayList<IndexEntry>(convertedCSV.get(1).length-2); //put previous_close in its own variable
		dji_previous_close = new BigDecimal(convertedCSV.get(1)[1].substring(convertedCSV.get(1)[1].indexOf("$")+1));
		for(int i=2;i<convertedCSV.get(1).length;i++)
		{
			BigDecimal d1 = new BigDecimal(convertedCSV.get(1)[i].substring(convertedCSV.get(1)[i].indexOf("$")+1));
			Date dd1 = createDate(convertedCSV.get(1)[i].substring(0, convertedCSV.get(1)[i].indexOf("$")));
			dji.add(i-2, new IndexEntry(dd1, d1));
		}
		sandp = new ArrayList<IndexEntry>(convertedCSV.get(2).length-2);
		sandp_previous_close = new BigDecimal(convertedCSV.get(2)[1].substring(convertedCSV.get(2)[1].indexOf("$")+1));
		for(int i=2;i<convertedCSV.get(2).length;i++)
		{
			BigDecimal d2 = new BigDecimal(convertedCSV.get(2)[i].substring(convertedCSV.get(2)[i].indexOf("$")+1));
			Date dd2 = createDate(convertedCSV.get(2)[i].substring(0, convertedCSV.get(2)[i].indexOf("$")));
			sandp.add(i-2, new IndexEntry(dd2, d2));
		}
		nasdaq = new ArrayList<IndexEntry>(convertedCSV.get(3).length-2);
		nasdaq_previous_close = new BigDecimal(convertedCSV.get(3)[1].substring(convertedCSV.get(3)[1].indexOf("$")+1));
		for(int i=2;i<convertedCSV.get(3).length;i++)
		{
			BigDecimal d3 = new BigDecimal(convertedCSV.get(3)[i].substring(convertedCSV.get(3)[i].indexOf("$")+1));
			Date dd3 = createDate(convertedCSV.get(3)[i].substring(0, convertedCSV.get(3)[i].indexOf("$")));
			nasdaq.add(i-2, new IndexEntry(dd3, d3));
		}
		}
		}
		publishProgress(10.0);
		
		Index index = new Index(indexDate, dji, sandp, nasdaq, 
				dji_previous_close, sandp_previous_close, nasdaq_previous_close, dji_todays_close, sandp_todays_close, nasdaq_todays_close, hasMarketClose);
		index.setDate(Calendar.getInstance().getTime());
		
		if(internetReachable)
		{
			fromInternet=true;
			return index;
		}
		else
		{
			fromInternet=false;
			return index;
		}
		
	}
	private boolean fromInternet;
	private Date createDate(String string)
	{
		int year = Integer.parseInt(string.substring(0, string.indexOf('-')));
		string = string.substring(string.indexOf("-")+1);
		int month = Integer.parseInt(string.substring(0, string.indexOf('-')));
		string = string.substring(string.indexOf("-")+1);
		int day = Integer.parseInt(string.substring(0, string.indexOf("@")));
		string = string.substring(string.indexOf("@")+1);
		int hour = Integer.parseInt(string.substring(0, string.indexOf(":")));
		string = string.substring(string.indexOf(":")+1);
		int min = Integer.parseInt(string.substring(0, string.indexOf(":")));
		string = string.substring(string.indexOf(":")+1);
		int sec = Integer.parseInt(string.substring(0));
		
		Date date = new Date(year, month-1, day, hour, min, sec);
		return date;
		
	}

	@Override
	protected void onPostExecute(Index index)
	{
		if(fromInternet)
			caller.updateIncicies(index);
		else
			caller.noDataNotifyIndex(index);
	}

}
