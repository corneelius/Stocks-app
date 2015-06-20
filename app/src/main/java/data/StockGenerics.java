package data;

import android.os.Bundle;

public class StockGenerics implements StockData{
	private Ticker mTicker;
	
	private double ask;
	private double bid;
	private double lastTrade;
	private long volume;
	private double nominalChange;
	private double percentChange;
	private String name;
	
	public StockGenerics(Ticker ticker)
	{
		this.mTicker = ticker;
	}
	
	public Ticker getTicker()
	{
		return mTicker;
	}
	
	public double getLastTrade() {
		return Types.roundToHundredths(lastTrade).doubleValue();
	}
	public void setLastTrade(double lastTrade) {
		this.lastTrade = lastTrade;
	}
	public long getVolume() {
		return volume;
	}
	public void setVolume(long volume) {
		this.volume = volume;
	}
	public double getNominalChange() {
		return Types.roundToHundredths(nominalChange).doubleValue();
	}
	public void setNominalChange(double change) {
		this.nominalChange = change;
	}
	public double getPercentChange() {
		return Types.roundToHundredths(percentChange).doubleValue();
	}
	public void setPercentChange(double percentChange) {
		this.percentChange = percentChange;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}


	public double getAsk() {
		return Types.roundToHundredths(ask).doubleValue();
	}

	public void setAsk(double ask) {
		this.ask = ask;
	}

	public double getBid() {
		return Types.roundToHundredths(bid).doubleValue();
	}

	public void setBid(double bid) {
		this.bid = bid;
	}
	public Bundle getBundle()
	{
		Bundle b = new Bundle();
		b.putParcelable("ticker", mTicker);
		b.putDouble("ask", ask);
		b.putDouble("bid", bid);
		b.putDouble("lastTrade", lastTrade);
		b.putLong("volume", volume);
		b.putDouble("nominalChange", nominalChange);
		b.putDouble("percentChange", percentChange);
		b.putString("name", name);
		return b;
		
	}
	public StockGenerics(Bundle b)
	{
		mTicker = b.getParcelable("ticker");
		ask = b.getDouble("ask");
		bid = b.getDouble("bid");
		lastTrade = b.getDouble("lastTrade");
		volume = b.getLong("volume");
		nominalChange = b.getDouble("nominalChange");
		percentChange = b.getDouble("percentChange");
		name = b.getString("name");
	}

	@Override
	public String summarizeAsString() {
		return "StockGenerics [mTicker=" + mTicker + ", ask=" + ask + ", bid="
				+ bid + ", lastTrade=" + lastTrade + ", volume=" + volume
				+ ", nominalChange=" + nominalChange + ", percentChange="
				+ percentChange + ", name=" + name + "]";
	}

}
