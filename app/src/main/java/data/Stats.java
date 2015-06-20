package data;

import android.os.Bundle;

public class Stats implements StockData{
	public static int NUM_FIELDS = 19; 
	
	private Ticker mTicker;
	
	//page one
	private double open;
	private double previousClose;
	private double dayHigh;
	private double dayLow;
	private double ftWeekHigh;
	private double ftWeekLow;
	//page two
	private double volume;
	private double PE;
	private double PEG;
	private double EPS;
	private double nextYearEPS;
	//page three
	private double dividendPerShare;
	private double dividendYield;
	private double marketCap;
	private double averageDailyVolume;
	//page four
	private double EBITDA;
	private double priceToSales;
	private double priceToBook;
	private double shortRatio;
	
	public Stats(Ticker ticker)
	{
		this.mTicker = ticker;
	}
	
	public Ticker getTicker()
	{
		return mTicker;
	}
	public double getPEG()
	{
		return PEG;
	}
	public void setPEG(double PEG)
	{
		this.PEG = PEG;
	}
	public double getOpen() {
		return open;
	}
	public void setOpen(double open) {
		this.open = open;
	}
	public double getPreviousClose() {
		return previousClose;
	}
	public void setPreviousClose(double previousClose) {
		this.previousClose = previousClose;
	}
	public double getDayHigh() {
		return dayHigh;
	}
	public void setDayHigh(double dayHigh) {
		this.dayHigh = dayHigh;
	}
	public double getDayLow() {
		return dayLow;
	}
	public void setDayLow(double dayLow) {
		this.dayLow = dayLow;
	}
	public double getFtWeekHigh() {
		return ftWeekHigh;
	}
	public void setFtWeekHigh(double ftWeekHigh) {
		this.ftWeekHigh = ftWeekHigh;
	}
	public double getFtWeekLow() {
		return ftWeekLow;
	}
	public void setFtWeekLow(double ftWeekLow) {
		this.ftWeekLow = ftWeekLow;
	}
	public double getDividendPerShare() {
		return dividendPerShare;
	}
	public void setDividendPerShare(double dividendPerShare) {
		this.dividendPerShare = dividendPerShare;
	}
	public double getDividendYield() {
		return dividendYield;
	}
	public void setDividendYield(double dividendYield) {
		this.dividendYield = dividendYield;
	}
	public double getMarketCap() {
		return marketCap;
	}
	public void setMarketCap(double marketCap) {
		this.marketCap = marketCap;
	}
	public double getVolume() {
		return volume;
	}
	public void setVolume(double volume) {
		this.volume = volume;
	}
	public double getAverageDailyVolume() {
		return averageDailyVolume;
	}
	public void setAverageDailyVolume(double averageDailyVolume) {
		this.averageDailyVolume = averageDailyVolume;
	}
	public double getPE() {
		return PE;
	}
	public void setPE(double pE) {
		PE = pE;
	}
	public double getEPS() {
		return EPS;
	}
	public void setEPS(double ePS) {
		EPS = ePS;
	}
	public double getNextYearEPS() {
		return nextYearEPS;
	}
	public void setNextYearEPS(double nextYearEPS) {
		this.nextYearEPS = nextYearEPS;
	}
	public double getEBITDA() {
		return EBITDA;
	}
	public void setEBITDA(double eBITDA) {
		EBITDA = eBITDA;
	}
	public double getPriceToSales() {
		return priceToSales;
	}
	public void setPriceToSales(double priceToSales) {
		this.priceToSales = priceToSales;
	}
	public double getPriceToBook() {
		return priceToBook;
	}
	public void setPriceToBook(double priceToBook) {
		this.priceToBook = priceToBook;
	}
	public double getShortRatio() {
		return shortRatio;
	}
	public void setShortRatio(double shortRatio) {
		this.shortRatio = shortRatio;
	}
	public Bundle getBundle()
	{
		
		Bundle b = new Bundle();
		b.putParcelable("ticker", mTicker);
		b.putDouble("open", open);
		b.putDouble("previousClose", previousClose);
		b.putDouble("dayHigh", dayHigh);
		b.putDouble("dayLow", dayLow);
		b.putDouble("ftWeekHigh", ftWeekHigh);
		b.putDouble("ftWeekLow", ftWeekLow);
		b.putDouble("volume", volume);
		b.putDouble("PE", PE);
		b.putDouble("PEG", PEG);
		b.putDouble("EPS", EPS);
		b.putDouble("nextYearEPS", nextYearEPS);
		b.putDouble("dividendPerShare", dividendPerShare);
		b.putDouble("dividendYield", dividendYield);
		b.putDouble("marketCap", marketCap);
		b.putDouble("averageDailyVolume", averageDailyVolume);
		b.putDouble("EBITDA", EBITDA);
		b.putDouble("priceToSales", priceToSales);
		b.putDouble("priceToBook", priceToBook);
		b.putDouble("shortRatio", shortRatio);
		
		return b;
	}
	public Stats(Bundle b)
	{
		mTicker = b.getParcelable("ticker");
		open = b.getDouble("open");
		previousClose = b.getDouble("previousClose");
		dayHigh = b.getDouble("dayHigh");
		dayLow = b.getDouble("dayLow");
		ftWeekHigh = b.getDouble("ftWeekHigh");
		ftWeekLow = b.getDouble("ftWeekLow");
		volume = b.getDouble("volume");
		PE = b.getDouble("PE");
		PEG = b.getDouble("PEG");
		EPS = b.getDouble("EPS");
		nextYearEPS = b.getDouble("nextYearEPS");
		dividendPerShare = b.getDouble("dividendPerShare");
		dividendYield = b.getDouble("dividendYield");
		marketCap = b.getDouble("marketCap");
		averageDailyVolume = b.getDouble("averageDailyVolume");
		EBITDA = b.getDouble("EBITDA");
		priceToSales = b.getDouble("priceToSales");
		priceToBook = b.getDouble("priceToBook");
		shortRatio = b.getDouble("shortRatio");
	}

	@Override
	public String summarizeAsString() {
		return "Stats [mTicker=" + mTicker
				+ ", open=" + open + ", previousClose=" + previousClose
				+ ", dayHigh=" + dayHigh + ", dayLow=" + dayLow
				+ ", ftWeekHigh=" + ftWeekHigh + ", ftWeekLow=" + ftWeekLow
				+ ", dividendPerShare=" + dividendPerShare + ", dividendYield="
				+ dividendYield + ", marketCap=" + marketCap + ", volume="
				+ volume + ", averageDailyVolume=" + averageDailyVolume
				+ ", PE=" + PE + ", PEG=" + PEG + ", EPS=" + EPS
				+ ", nextYearEPS=" + nextYearEPS + ", EBITDA=" + EBITDA
				+ ", priceToSales=" + priceToSales + ", priceToBook="
				+ priceToBook + ", shortRatio=" + shortRatio + "]";
	}

}
