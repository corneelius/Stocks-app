package data;

import java.util.Date;

import android.os.Bundle;

public class Chart implements StockData{
	private Ticker mTicker;
	
	//hold raw data
	private Date[] tenYearMonthlyDates;
	private double[] tenYearMonthlyValues;
	private Date[] fiveYearDailyDates;
	private double[] fiveYearDailyValues;
	
	public Bundle getBundle()
	{
		Bundle b = new Bundle();
		b.putSerializable("tenYearMonthlyDates", tenYearMonthlyDates);
		b.putDoubleArray("tenYearMonthlyValues", tenYearMonthlyValues);
		b.putSerializable("fiveYearDailyDates", fiveYearDailyDates);
		b.putDoubleArray("fiveYearDailyValues", fiveYearDailyValues);
		return b;
	}
	public Chart(Bundle b)
	{
		tenYearMonthlyDates = (Date[]) b.getSerializable("tenYearMonthlyDates");
		tenYearMonthlyValues = b.getDoubleArray("tenYearMonthlyValues");
		fiveYearDailyDates = (Date[]) b.getSerializable("fiveYearDailyDates");
		fiveYearDailyValues = b.getDoubleArray("fiveYearDailyValues");
	}
	public Chart(Ticker ticker)
	{
		this.mTicker = ticker;
	}

	public Ticker getTicker()
	{
		return mTicker;
	}
	@Override
	public String summarizeAsString() {
		String output="";
		for(double array : tenYearMonthlyValues)
		{
			output = output + array + "\n";
		}
		return output;
	}

	public Ticker getmTicker() {
		return mTicker;
	}

	public void setmTicker(Ticker mTicker) {
		this.mTicker = mTicker;
	}

	public Date[] getTenYearMonthlyDates() {
		return tenYearMonthlyDates;
	}

	public void setTenYearMonthlyDates(Date[] tenYearMonthlyDates) {
		this.tenYearMonthlyDates = tenYearMonthlyDates;
	}

	public double[] getTenYearMonthlyValues() {
		return tenYearMonthlyValues;
	}

	public void setTenYearMonthlyValues(double[] tenYearMonthlyValues) {
		this.tenYearMonthlyValues = tenYearMonthlyValues;
	}

	public Date[] getFiveYearDailyDates() {
		return fiveYearDailyDates;
	}

	public void setFiveYearDailyDates(Date[] fiveYearDailyDates) {
		this.fiveYearDailyDates = fiveYearDailyDates;
	}

	public double[] getFiveYearDailyValues() {
		return fiveYearDailyValues;
	}

	public void setFiveYearDailyValues(double[] fiveYearDailyValues) {
		this.fiveYearDailyValues = fiveYearDailyValues;
	}

}