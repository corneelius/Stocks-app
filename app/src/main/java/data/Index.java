package data;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

import android.os.Bundle;
import android.util.Log;

//do not rely on device for time, use server
public class Index{
	private Date timeStamp;
	private ArrayList<IndexEntry> SandPTicks;
	private ArrayList<IndexEntry> DowJonesTicks;
	private ArrayList<IndexEntry> NasdaqTicks;
	private BigDecimal DJIpreviousClose; //guaranteed to have previous closes
	private BigDecimal SandPpreviousClose;
	private BigDecimal NasdaqpreviousClose;
	private BigDecimal DJItodaysClose; //not guaranteed to have today's closes
	private BigDecimal SandPtodaysClose;
	private BigDecimal NasdaqtodaysClose;
	private boolean isEmpty = false;
	private Date updated;
	private boolean hasMarketClose;
	
	private String[] DowJonesChanges;
	private String[] SandPChanges;
	private String[] NasdaqChanges;
	private String[] DowJonesPercents;
	private String[] SandPPercents;
	private String[] NasdaqPercents;
	//not guaranteed to have todayCloses
	


		public Index(Date timeStamp, ArrayList<IndexEntry> DJ, ArrayList<IndexEntry> SP, ArrayList<IndexEntry> NQ, BigDecimal DJIpreviousClose,
				BigDecimal SandPpreviousClose, BigDecimal NasdaqpreviousClose, BigDecimal DJItodaysClose,
				BigDecimal SandPtodaysClose, BigDecimal NasdaqtodaysClose, boolean hasMarketClose)
		{
			this.SandPTicks=SP;
			this.DowJonesTicks=DJ;
			this.NasdaqTicks=NQ;
			this.DJIpreviousClose=DJIpreviousClose;
			this.SandPpreviousClose=SandPpreviousClose;
			this.NasdaqpreviousClose=NasdaqpreviousClose;
			this.timeStamp=timeStamp;
			this.DJItodaysClose=DJItodaysClose;
			this.SandPtodaysClose=SandPtodaysClose;
			this.NasdaqtodaysClose=NasdaqtodaysClose;
			this.hasMarketClose=hasMarketClose;
			
			
			if(
					SandPTicks == null ||
					DowJonesTicks == null ||
					NasdaqTicks == null ||
					DJIpreviousClose == null ||
					SandPpreviousClose == null ||
					NasdaqpreviousClose == null)
				isEmpty = true;
				
			calcChanges();
		}
		private String verifyPriceString(String string, boolean addPlus)
		{
			int dot = string.indexOf(".");
			String s=string;
			if(dot+3 <= string.length())
			{
				s = string.substring(0, dot+3);
			}
			else if(dot+1==string.length())
			{
				s = s+" 00";
			}
			else if(dot==-1)
			{
				s = s+".00";
			}
			else if(dot+2==string.length())
			{
				s = s+"0";
			}
			
			
			if(addPlus && !(s.charAt(0)=='-' || s.charAt(0)=='+' ))
				s = "+"+s;
			
			return s;
		}
		private void calcChanges()
		{
			DowJonesChanges = new String[getDowJonesSize()];
			SandPChanges = new String[getDowJonesSize()];
			NasdaqChanges = new String[getDowJonesSize()];
			DowJonesPercents = new String[getDowJonesSize()];
			SandPPercents = new String[getDowJonesSize()];
			NasdaqPercents = new String[getDowJonesSize()];
			for(int i=0; i<getDowJonesSize(); i++)
			{
				DowJonesChanges[i] = verifyPriceString(getDowJonesAt(i).getBigDecimal().subtract(getDJIPreviousClose()).toPlainString(), true);
				SandPChanges[i] = verifyPriceString(getSandpAt(i).getBigDecimal().subtract(getSandpPreviousClose()).toPlainString(), true);
				NasdaqChanges[i] = verifyPriceString(getNasdaqAt(i).getBigDecimal().subtract(getNasdaqPreviousClose()).toPlainString(), true);
				
				DowJonesPercents[i] = verifyPriceString( Types.toPercentString((getDowJonesAt(i).getBigDecimal().subtract(getDJIPreviousClose())).divide(getDJIPreviousClose(), 4, RoundingMode.HALF_UP)), true )+"%";
				SandPPercents[i] = verifyPriceString( Types.toPercentString((getSandpAt(i).getBigDecimal().subtract(getSandpPreviousClose())).divide(getSandpPreviousClose(), 4, RoundingMode.HALF_UP)), true )+"%";
				NasdaqPercents[i] = verifyPriceString( Types.toPercentString((getSandpAt(i).getBigDecimal().subtract(getSandpPreviousClose())).divide(getSandpPreviousClose(), 4, RoundingMode.HALF_UP)), true )+"%";
			
			}
		}
		public String getDowJonesChangeAt(int index)
		{
			return DowJonesChanges[index];
		}
		public String getSandpChangeAt(int index)
		{
			return SandPChanges[index];
		}
		public String getNasdaqChangeAt(int index)
		{
			return NasdaqChanges[index];
		}
		public String getDowJonesPercentChangeAt(int index)
		{
			return DowJonesPercents[index];
		}
		public String getSandpPercentChangeAt(int index)
		{
			return SandPPercents[index];
		}
		public String getNasdaqPercentChangeAt(int index)
		{
			return NasdaqPercents[index];
		}
		public boolean hasMarketClose()
		{
			return hasMarketClose;
		}
		public Index(Bundle bundle)
		{
			
			this((Date) bundle.getSerializable("time_stamp"), 
					new ArrayList<IndexEntry>(Arrays.asList(Types.convertStringArrays(
							new Object[] {bundle.getStringArray("dji_dates"),bundle.getStringArray("dji_bigDecimal")} ))),
					new ArrayList<IndexEntry>(Arrays.asList(Types.convertStringArrays(
							new Object[] {bundle.getStringArray("sandp_dates"),bundle.getStringArray("sandp_bigDecimal")} ))), 
					new ArrayList<IndexEntry>(Arrays.asList(Types.convertStringArrays(
							new Object[] {bundle.getStringArray("nasdaq_dates"),bundle.getStringArray("nasdaq_bigDecimal")} ))), 
			new BigDecimal(bundle.getString("dji_previousClose")), 
			new BigDecimal(bundle.getString("sandp_previousClose")),
			new BigDecimal(bundle.getString("nasdaq_previousClose")), 
			bundle.getString("dji_todaysClose") == null ? null : new BigDecimal(bundle.getString("dji_todaysClose")), 
			bundle.getString("sandp_todaysClose") == null ? null : new BigDecimal(bundle.getString("sandp_todaysClose")), 
			bundle.getString("nasdaq_todaysClose") == null ? null : new BigDecimal(bundle.getString("nasdaq_todaysClose")) , bundle.getBoolean("hasMarketClose"));
		calcChanges();
		}
		public Bundle getBundle()
		{
			final Bundle bundle = new Bundle();
			Thread thread = new Thread()
			{
				@Override
				public void run()
				{
					
					bundle.putSerializable("timeStamp", timeStamp);
					
					Object[] dji = Types.convertIndexEntryArray(DowJonesTicks.toArray(new IndexEntry[DowJonesTicks.size()]));
					Object[] sandp = Types.convertIndexEntryArray(SandPTicks.toArray(new IndexEntry[SandPTicks.size()]));
					Object[] nasdaq = Types.convertIndexEntryArray(NasdaqTicks.toArray(new IndexEntry[NasdaqTicks.size()]));
					bundle.putStringArray("dji_dates", (String[])dji[0]);
					bundle.putStringArray("dji_bigDecimal", (String[])dji[1]);
					bundle.putStringArray("sandp_dates", (String[])sandp[0]);
					bundle.putStringArray("sandp_bigDecimal", (String[])sandp[1]);
					bundle.putStringArray("nasdaq_dates", (String[])nasdaq[0]);
					bundle.putStringArray("nasdaq_bigDecimal", (String[])nasdaq[1]);
					
					bundle.putString("dji_previousClose", DJIpreviousClose.toPlainString());
					bundle.putString("sandp_previousClose", SandPpreviousClose.toPlainString());
					bundle.putString("nasdaq_previousClose", NasdaqpreviousClose.toPlainString());
					
					bundle.putBoolean("hasMarketClose", hasMarketClose);					
					try{
					bundle.putString("dji_todaysClose", DJItodaysClose.toPlainString());
					bundle.putString("sandp_todaysClose", SandPtodaysClose.toPlainString());
					bundle.putString("nasdaq_todaysClose", NasdaqtodaysClose.toPlainString());
					} catch(NullPointerException e)
					{
						Log.i("nr.app", "the U.S. stock market is open");
					}
				}
			};
			thread.start();
			
			return bundle;
			
		}
	
		public void setDate(Date date)
		{
			this.updated=date;
		}
		public Date getUpdatedDate()
		{
			return updated;
		}
		public boolean isEmpty()
		{
			return isEmpty;
		}
		public IndexEntry getLargestNasdaq()
		{
			return Collections.max(NasdaqTicks);
		}
		public IndexEntry getLargestDji()
		{
			return Collections.max(DowJonesTicks);
		}
		public IndexEntry getLargestSandp()
		{
			return Collections.max(SandPTicks);
		}
		public IndexEntry getSmallestNasdaq()
		{
			return Collections.min(NasdaqTicks);
		}
		public IndexEntry getSmallestDji()
		{
			return Collections.min(DowJonesTicks);
		}
		public IndexEntry getSmallestSandp()
		{
			return Collections.min(SandPTicks);
		}
		public IndexEntry getSandpAt(int index)
		{
			return SandPTicks.get(index);
		}
		public int getSandpSize()
		{
			return SandPTicks.size();
		}
		public IndexEntry getDowJonesAt(int index)
		{
			return DowJonesTicks.get(index);
		}
		public int getDowJonesSize()
		{
			return DowJonesTicks.size();
		}
		public IndexEntry getNasdaqAt(int index)
		{
			return NasdaqTicks.get(index);
		}
		public int getNasdaqSize()
		{
			return NasdaqTicks.size();
		}
		public BigDecimal getDJIPreviousClose()
		{
			return DJIpreviousClose;
		}
		public BigDecimal getSandpPreviousClose()
		{
			return SandPpreviousClose;
		}
		public BigDecimal getNasdaqPreviousClose()
		{
			return NasdaqpreviousClose;
		}

		public Date getTimeStamp() {
			return timeStamp;
		}

		public void setTimeStamp(Date timeStamp) {
			this.timeStamp = timeStamp;
		}

		public BigDecimal getDJItodaysClose() {
			return DJItodaysClose;
		}

		public void setDJItodaysClose(BigDecimal dJItodaysClose) {
			DJItodaysClose = dJItodaysClose;
		}

		public BigDecimal getSandPtodaysClose() {
			return SandPtodaysClose;
		}

		public void setSandPtodaysClose(BigDecimal sandPtodaysClose) {
			SandPtodaysClose = sandPtodaysClose;
		}

		public BigDecimal getNasdaqtodaysClose() {
			return NasdaqtodaysClose;
		}

		public void setNasdaqtodaysClose(BigDecimal nasdaqtodaysClose) {
			NasdaqtodaysClose = nasdaqtodaysClose;
		}


}
