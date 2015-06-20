package telephony;

import data.Chart;
import data.News;
import data.Stats;
import data.StockData;
import data.StockGenerics;

public interface StockPopulatorResultReceiver {
	//defines an activity that can update the contents of its Stock object
	
	//two updates sent: one when recentDataArray is updated, and one when pastDataArray is updated
	public void updateChart(Chart[] updatedChart);
	public void updateNews(News[] updatedNews);
	public void updateStats(Stats[] updatedStats);
	public void updateStockGenerics(StockGenerics[] updatedStockGenerics);
	public void done(StockData[] data, StockPopulatorOptions task);
	public void updateWatchlists(StockGenerics[] updatedWatchlists);
	public void updateProgress(int progress);
	public void noDataNotify();

}
