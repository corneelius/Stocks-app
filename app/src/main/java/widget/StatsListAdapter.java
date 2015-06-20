package widget;

import java.util.ArrayList;

import data.Stats;
import data.Types;

public class StatsListAdapter {
	private Stats mStats;
	private ArrayList<String[]> returnArrays;
	private String longestString;
	public final int NUM_FIELDS = 17; //52-week range and day's range is combined
	
	
	public StatsListAdapter(Stats stats)
	{
		mStats = stats;
		returnArrays = new ArrayList<String[]>();
		initReturnArray(returnArrays);
		longestString = findLongestString();
	}
	public String[] getStatAt(int index)
	{
		return returnArrays.get(index);
	}
	public String getLongestString()
	{
		return longestString;
	}
	public int getSize()
	{
		return NUM_FIELDS;
	}
	
	
	private String findLongestString()
	{
		 //asterik between name and value
		
		String longestName = "";
		String longestValue = "";
		for(String[] array : returnArrays)
		{
			if(array[0].length() > longestName.length())
			{
				longestName = array[0];
			}
			if(array[1].length() > longestValue.length())
			{
				longestValue = array[1];
			}
		}
		return longestName+"*"+longestValue;
	}
	private void initReturnArray(ArrayList<String[]> input)
	{
		for(int i=0;i<NUM_FIELDS; i++)
		{
			input.add(getNextStat(i));
		}
	}
	private String[] getNextStat(int index) throws IndexOutOfBoundsException
	{
		String[] returnArray;
		//returns an array - first value is String name, second value is double value
		switch(index)
		{
		case 0:
		{
			returnArray = new String[2];
			returnArray[0] = new String("Open");
			returnArray[1] = Types.trauncateToStringWithZeros(mStats.getOpen());
			return returnArray;
		}
		case 1: 
		{
			returnArray = new String[2];
			returnArray[0] = new String("Previous Close");
			returnArray[1] = Types.trauncateToStringWithZeros(mStats.getPreviousClose());
			return returnArray;
		}
		case 2: 
		{
			returnArray = new String[2];
			returnArray[0] = new String("Day's Range");
			returnArray[1] = Types.trauncateToStringWithZeros(mStats.getDayLow())+"-"+Types.trauncateToStringWithZeros(mStats.getDayHigh());
			return returnArray;
			
		}
		case 3: 
		{
			returnArray = new String[2];
			returnArray[0] = new String("52-week Range");
			returnArray[1] = Types.trauncateToStringWithZeros(mStats.getFtWeekLow())+"-"+Types.trauncateToStringWithZeros(mStats.getFtWeekHigh());
			return returnArray;
		}
		case 4: 
		{
			returnArray = new String[2];
			returnArray[0] = new String("P/E");
			returnArray[1] = Types.trauncateToStringWithoutZeros(mStats.getPE());
			return returnArray;
		}
		case 5: 
		{
			returnArray = new String[2];
			returnArray[0] = new String("PEG");
			returnArray[1] = Types.trauncateToStringWithoutZeros(mStats.getPEG());
			return returnArray;
		}
		case 6: 
		{
			returnArray = new String[2];
			returnArray[0] = new String("EPS");
			returnArray[1] = Types.trauncateToStringWithoutZeros(mStats.getEPS());
			return returnArray;
		}
		case 7: 
		{
			returnArray = new String[2];
			returnArray[0] = new String("Next Year's EPS");
			returnArray[1] = Types.trauncateToStringWithoutZeros(mStats.getNextYearEPS());
			return returnArray;
		}
		case 8: 
		{
			returnArray = new String[2];
			returnArray[0] = new String("Volume");
			returnArray[1] = Types.trauncateToStringWithoutZeros(mStats.getVolume());
			return returnArray;
		}
		case 9: 
		{
			returnArray = new String[2];
			returnArray[0] = new String("Avg Daily Volume");
			returnArray[1] = Types.trauncateToStringWithoutZeros(mStats.getAverageDailyVolume());
			return returnArray;
		}
		case 10: 
		{
			returnArray = new String[2];
			returnArray[0] = new String("Dividend $");
			returnArray[1] = Types.trauncateToStringWithoutZeros(mStats.getDividendPerShare());
			return returnArray;
		}
		case 11: 
		{
			returnArray = new String[2];
			returnArray[0] = new String("Dividend %");
			returnArray[1] = Types.trauncateToStringWithoutZeros(mStats.getDividendYield());
			return returnArray;
		}
		case 12: 
		{
			returnArray = new String[2];
			returnArray[0] = new String("Market Cap");
			returnArray[1] = Types.trauncateToStringWithoutZeros(mStats.getMarketCap());
			return returnArray;
		}
		case 13: 
		{
			returnArray = new String[2];
			returnArray[0] = new String("EBITDA");
			returnArray[1] = Types.trauncateToStringWithoutZeros(mStats.getEBITDA());
			return returnArray;
		}
		case 14: 
		{
			returnArray = new String[2];
			returnArray[0] = new String("Price/Sales");
			returnArray[1] = Types.trauncateToStringWithoutZeros(mStats.getPriceToSales());
			return returnArray;
		}
		case 15: 
		{
			returnArray = new String[2];
			returnArray[0] = new String("Price/Book");
			returnArray[1] = Types.trauncateToStringWithoutZeros(mStats.getPriceToBook());
			return returnArray;
		}
		case 16: 
		{
			returnArray = new String[2];
			returnArray[0] = new String("Short Ratio");
			returnArray[1] = Types.trauncateToStringWithZeros(mStats.getShortRatio());
			return returnArray;
		}
		
		}
		throw new IndexOutOfBoundsException();
	}
	

}
