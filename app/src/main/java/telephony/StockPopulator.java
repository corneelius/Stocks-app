package telephony;

import java.io.IOException;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import android.os.AsyncTask;
import data.Chart;
import data.News;
import data.Stats;
import data.Stock;
import data.StockData;
import data.StockGenerics;

public class StockPopulator extends AsyncTask<StockData, StockData, StockData[]>{
	private StockPopulatorResultReceiver mParent;
	private StockPopulatorOptions mOptions;
	private int maxProgressPointsAffected;
	
	public StockPopulator(StockPopulatorResultReceiver parent, StockPopulatorOptions options, int maxProgressPointsAffected)
	{
		this.mParent = parent;
		this.mOptions = options;
		this.maxProgressPointsAffected=maxProgressPointsAffected;
	}
	public void setStockPopulatorOptions(StockPopulatorOptions options)
	{
		mOptions = options;
	}

	private double progressLeftOver=0.0;
	public void updateProgress(double p)
	{
		double weightedProgress = p * (maxProgressPointsAffected/100.0);
		weightedProgress += progressLeftOver;
		
		if((int)weightedProgress == 0)
		{
			progressLeftOver += weightedProgress;
			return;
		}
		
		int progress2 =  (int)weightedProgress;
		mParent.updateProgress(progress2);
		
		progressLeftOver = weightedProgress % (int)weightedProgress;
	}
	
	@Override
	protected StockData[] doInBackground(StockData... params) {
		if(!ensureConsistency(params))
		{
			//params contains objects of multiple classes, fail it
			throw new RuntimeException("StockPopulator error - array of multiple classes");
		}
		if(!verifyData(params))
		{
			//params has objects that don't satisfy options - fail it
			throw new RuntimeException("StockPopulator error - array doesn't satify mOptions");
		}
		
		
		if(params.length==0)
			return params;
		
		StockPopulatorHelper helper = new StockPopulatorHelper(this, mOptions);
		StockData[] result = null;
		try {
			result = helper.populate(params);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			return null;
		} catch (FactoryConfigurationError e) {
			e.printStackTrace();
			return null;
		} catch (SAXException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return result;
	}
	
	@Override
	protected void onPostExecute(StockData[] data)
	{
		if(data==null)
			mParent.noDataNotify();
		else
			mParent.done(data, mOptions);
	}
	private boolean ensureConsistency(StockData[] array)
	{
		if(array.length > 1)
		{
			for(int i=1; i<array.length; i++)
			{
				if(!((array[i].getClass().getName()).equals(array[i-1].getClass().getName())))
				return false;
			}
			return true;
		}
		else
		{
			return true;
		}
	}
	
	@Override
	protected void onProgressUpdate(StockData...data)
	{
		if(data[0] instanceof Chart)
		{
			Chart[] charts = new Chart[data.length];
			for(int i = 0; i<data.length;i++)
			{
				charts[i] = (Chart) data[i];
			}
			mParent.updateChart(charts);
		}
		else if(data[0] instanceof Stats)
		{
			Stats[] stats = new Stats[data.length];
			for(int i=0;i<data.length;i++)
			{
				stats[i] = (Stats) data[i];
			}
			mParent.updateStats(stats);
		}
		else if(data[0] instanceof News)
		{
			News[] news = new News[data.length];
			for(int i=0;i<data.length;i++)
			{
				news[i] = (News) data[i];
			}
			mParent.updateNews(news);
		}
		else if(mOptions == StockPopulatorOptions.FILL_WATCHLISTS && data[0] instanceof StockGenerics)
		{
			StockGenerics[] generics = new StockGenerics[data.length];
			for(int i=0;i<data.length;i++)
			{
				generics[i] = (StockGenerics) data[i];
			}
			mParent.updateWatchlists(generics);
		}
		else if(data[0] instanceof StockGenerics)
		{
			StockGenerics[] generics = new StockGenerics[data.length];
			for(int i=0;i<data.length;i++)
			{
				generics[i] = (StockGenerics) data[i];
			}
			mParent.updateStockGenerics(generics);
		}
	}
	
	public void publishProgressPublic(StockData...data) //makes publishProgress accessible publicly
	{
		publishProgress(data);
	}
	
	private boolean verifyData(StockData[] input)
	{
		switch(mOptions)
		{
		case FILL_STOCK:
		{
			for(int i=0; i<input.length; i++)
			{
			if(!(input[i] instanceof Stock))
				return false;
			}
			break;
		}
		case FILL_NEWS:
		{
			for(int i=0;i<input.length;i++)
			{
			if(!(input[i] instanceof News || input[i] instanceof Stock))
				return false;
			}
			break;
		}
		case FILL_CHART:
		{
			for(int i=0;i<input.length;i++)
			{
			if(!(input[i] instanceof Chart || input[i] instanceof Stock))
				return false;
			}
			break;
		}
		case FILL_STATS:
		{
			for(int i=0;i<input.length;i++)
			{
			if(!(input[i] instanceof Stats || input[i] instanceof Stock))
				return false;
			}
			break;
		}
		case FILL_GENERICS:
		{
			for(int i=0;i<input.length;i++)
			{
			if(!(input[i] instanceof StockGenerics || input[i] instanceof Stock))
				return false;
			}
			break;
		}
		}
		return true;
	}
	
	

}
