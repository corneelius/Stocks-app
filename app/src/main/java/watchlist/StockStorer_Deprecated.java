package watchlist;

import java.io.File;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import telephony.StockPopulator;
import telephony.StockPopulatorOptions;
import telephony.StockPopulatorResultReceiver;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import data.Chart;
import data.News;
import data.Stats;
import data.StockData;
import data.StockGenerics;
import data.Ticker;

public class StockStorer_Deprecated extends Service implements StockPopulatorResultReceiver{
	private String name;
	private WatchlistManager mWatchlistManager;
	private StockPopulator mStockPopulator;
	private String tempFileName;
	private ObjectOutputStream oos;
	
	@Override
	public void onCreate()
	{
		mWatchlistManager = new WatchlistManager(this);
		mStockPopulator = new StockPopulator(this, StockPopulatorOptions.FILL_GENERICS, 100);
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startID)
	{
		name = intent.getStringExtra("name");
			ArrayList<String>  array = mWatchlistManager.getWatchlist(name);
			StockGenerics[] gen = new StockGenerics[array.size()-1];
			for(int i=1; i<array.size(); i++)
			{
				gen[i] = new StockGenerics(new Ticker(array.get(i)));
			}
			
		mStockPopulator.execute(gen);
		return Service.START_REDELIVER_INTENT;
	}
	
	
	
	
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	@Override
	public void updateChart(Chart[] updatedChart) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void updateNews(News[] updatedNews) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void updateStats(Stats[] updatedStats) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void updateStockGenerics(StockGenerics[] updatedStockGenerics) {
		ArrayList<StockGenerics> array = new ArrayList<StockGenerics>(updatedStockGenerics.length);
		for(int i=0; i<updatedStockGenerics.length; i++)
		{
			array.add(updatedStockGenerics[i]);
		}
		File temp = mWatchlistManager.writeStockGenerics(array);
		temp.renameTo(new File(getFilesDir(), name));
	}
	@Override
	public void done(StockData[] data, StockPopulatorOptions task) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void updateWatchlists(StockGenerics[] updatedWatchlists) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void updateProgress(int progress) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void noDataNotify() {
		// TODO Auto-generated method stub
		
	}

}
