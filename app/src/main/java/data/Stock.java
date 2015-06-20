package data;


public class Stock implements StockData{


private Ticker mTicker;
private Stats mStats;
private Chart mChart;
private News mNews;
private StockGenerics mStockGenerics;

public Stock(Ticker ticker)
{
	mTicker = ticker;
	mStats = new Stats(ticker);
	mChart = new Chart(ticker);
	mNews = new News(ticker);
	mStockGenerics = new StockGenerics(ticker);
}
public Stock(Ticker ticker, StockGenerics mStockGenerics, Stats mStats, Chart mChart, News mNews)
{
	mTicker = ticker;
	this.mStats = mStats;
	this.mChart = mChart;
	this.mNews = mNews;
	this.mStockGenerics = mStockGenerics;
	
}
public Ticker getTicker()
{
	return mTicker;
}
public Stats getStats() {
	return mStats;
}
public void setStats(Stats mStats) {
	this.mStats = mStats;
}
public Chart getChart() {
	return mChart;
}
public void setChart(Chart mChart) {
	this.mChart = mChart;
}
public News getNews() {
	return mNews;
}
public void setNews(News mNews) {
	this.mNews = mNews;
}
public StockGenerics getStockGenerics() {
	return mStockGenerics;
}
public void setStockGenerics(StockGenerics mStockGenerics) {
	this.mStockGenerics = mStockGenerics;
}
@Override
public String summarizeAsString() {
	return mStockGenerics.summarizeAsString();
}


}
