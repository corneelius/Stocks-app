package widget;

import java.util.Calendar;
import java.util.Date;

import nr.app.R;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import data.Chart;
import data.Types;

public class ChartWidget extends SurfaceView implements SurfaceHolder.Callback
{
	private boolean surfaceIsCreated;
	private Typeface robotoBold;
	private volatile Chart mChart;
	private float mDensityMultiplier;
	private final float TOGGLE_BAR_HEIGHT_FIX = 32; //dip
	private final float VERTICAL_MARGIN = 5; //dip
	private final float HORIZONTAL_MARGIN = 3; //dip
	
	private float chartHeight;
	private float chartWidth;
	private float chartTop;
	private float chartLeft;
	
	private float toggleButtonWidth;
	private float toggleBarHeight;
	private float toggleBarWidth;
	private float toggleBarTop;
	private float[] toggleBarDivisions;
	private ChartType[] toggles;
	
	private float tiersHeight;
	private float tiersWidth;

	private int numChartTiers;
	private boolean bitmapsCreated=false;
	
	private Typeface tiersFont;
	private Typeface toggleBarIndicatorFont;
	
	
	private volatile Bitmap oneMonthBitmap; //daily
	private volatile Bitmap threeMonthBitmap; //daily
	private volatile Bitmap sixMonthBitmap; //daily
	private volatile Bitmap yearToDateBitmap; //daily
	private volatile Bitmap twoYearBitmap; //monthly
	private volatile Bitmap fiveYearBitmap; //monthly
	private volatile Bitmap tenYearBitmap; //monthly
	
	private volatile Bitmap oneMonthBitmapTierNumbers; //daily
	private volatile Bitmap threeMonthBitmapTierNumbers; //daily
	private volatile Bitmap sixMonthBitmapTierNumbers; //monthly
	private volatile Bitmap yearToDateBitmapTierNumbers; //monthly
	private volatile Bitmap twoYearBitmapTierNumbers;
	private volatile Bitmap fiveYearBitmapTierNumbers; //monthly
	private volatile Bitmap tenYearBitmapTierNumbers; //monthly
	
	private volatile double oneMonthHigh;
	private volatile double oneMonthLow;
	private volatile double threeMonthHigh;
	private volatile double threeMonthLow;
	private volatile double sixMonthHigh;
	private volatile double sixMonthLow;
	private volatile double yearToDateHigh;
	private volatile double yearToDateLow;
	private volatile double twoYearHigh;
	private volatile double twoYearLow;
	private volatile double fiveYearHigh;
	private volatile double fiveYearLow;
	private volatile double tenYearHigh;
	private volatile double tenYearLow;
	
	private volatile Bitmap currentChartBitmap;
	private volatile Bitmap currentTierNumbersBitmap;
	private volatile ChartType selectedToggle;
	private Resources r;
	
	private volatile boolean touchIsDisabled;
	
	private int chartLinesColor;
	private int backgroundColor;

	
	private Object drawingLock;
	
	private enum ChartType
	{
		ONE_MONTH, THREE_MONTH, SIX_MONTH, YEAR_TO_DATE, TWO_YEAR, FIVE_YEAR, TEN_YEAR   //may not use all
	}
	
	
	
	
	public ChartWidget(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init();
	}
	public ChartWidget(Context c)
	{
		super(c);
		init();
	}
	
	
	
	
	private void init()
	{
		touchIsDisabled = true;
		robotoBold = Typeface.createFromAsset(getContext().getAssets(), "fonts/Roboto-Bold.ttf");
		backgroundColor = Color.argb(255, 255, 255, 255);
		surfaceIsCreated = false;
		getHolder().addCallback(this);
		r = getResources();
		mDensityMultiplier = getContext().getResources().getDisplayMetrics().density;
		numChartTiers = 4; //from 6
		toggleBarIndicatorFont = Typeface.createFromAsset(getContext().getAssets(), "fonts/Roboto-BlackItalic.ttf");
		
		chartLinesColor = Color.argb(255, 33, 181, 229);
		drawingLock = new Object(); //object to synchronize drawing calls
		tiersFont = Typeface.createFromAsset(getContext().getAssets(), "fonts/Roboto-ThinItalic.ttf");
	}
	private void createBitmaps(ChartType...types)
	{
		for(int index = 0; index < types.length; index++)
		{
			double[] data = getClosingDataFor(types[index]);
			Date[] dateData = getDateDataFor(types[index]);
			//must be called after getClosingDataFor()
			double highestValue = getChartHighestValue(types[index]);
			double lowestValue = getChartLowestValue(types[index]);
		
			double spreadBetweenPairLines = (highestValue-lowestValue)/(numChartTiers-1);  //difference between highest and lowest values divided by the number of lines
		
			
			//initialize tierNumbers bitmap
			//this includes margins before and after bitmap
			Bitmap tierNumbersBitmap = Bitmap.createBitmap((int)(tiersWidth), (int)tiersHeight, Bitmap.Config.ARGB_8888);
			Canvas tierNumbersCanvas = new Canvas(tierNumbersBitmap);
			tierNumbersCanvas.drawColor(backgroundColor);
			
			//initialize chart bitmap
			Bitmap chartBitmap = Bitmap.createBitmap((int)(chartWidth), (int)chartHeight, Bitmap.Config.ARGB_8888);
			Canvas chartCanvas = new Canvas(chartBitmap);
			chartCanvas.drawColor(backgroundColor);
			
			//LAYOUT
			float tierTextX = toPixels(4); 
			float linesStartingX = tierTextX;
			float chartRightLimit = chartCanvas.getWidth()-toPixels(5); //margin to the right
			//LAYOUT
			
			//draw text and lines
			Paint tiersTextPaint = getTiersTextPaint(Types.trauncateToStringWithZeros(highestValue), tiersWidth-tierTextX);
			Paint chartLinesPaint = getChartLinesPaint();
			
			//LAYOUT
			float timelineHeight = toPixels(20);
			float lowestLineY = chartHeight-timelineHeight;  //plug in lowest possible value
			float highestLineY = 0+tiersTextPaint.getTextSize(); //plug in highest possible value
			float timelineTop=lowestLineY;
			float heightBetweenLines = lowestLineY-highestLineY;
			float spaceBetweenLines = (heightBetweenLines)/(numChartTiers-1);  //measure spaces, not lines
			float pixelsPerDollar = (float) (heightBetweenLines/(highestValue-lowestValue));
			float Xdelta = chartRightLimit - linesStartingX;
			//LAYOUT
			
			for(int i=0;i<numChartTiers;i++)
			{
					chartCanvas.drawLine(linesStartingX, lowestLineY-(spaceBetweenLines*i), chartRightLimit, lowestLineY-(spaceBetweenLines*i), chartLinesPaint);
					tierNumbersCanvas.drawText(Types.trauncateToStringWithZeros((spreadBetweenPairLines*i)+lowestValue), tierTextX, lowestLineY-(spaceBetweenLines*i)+(0.5f*tiersTextPaint.getTextSize()), tiersTextPaint);
			}
		
			float xs = Xdelta / (data.length-1);
		
			//Paint chartPaint = getChartPaint(255);
			Paint timelinePaint = getTimelinePaint(0.5f*timelineHeight);
			
			Path chartPath = new Path();
			chartPath.moveTo(linesStartingX, lowestLineY-((float)((data[data.length-1]-lowestValue)*pixelsPerDollar)));
			//draw chart
			
			Paint chartGraphPaint = new Paint();
			chartGraphPaint.setAntiAlias(true);
			chartGraphPaint.setStrokeWidth(3);
			chartGraphPaint.setColor(Color.rgb(51,181,229));
			
			chartPath.lineTo(linesStartingX+xs, lowestLineY-((float)((data[data.length-2]-lowestValue)*pixelsPerDollar)));	
			chartCanvas.drawLine(linesStartingX, lowestLineY-((float) ((data[(data.length-1)-0]-lowestValue)*pixelsPerDollar)), 
					linesStartingX+xs, lowestLineY-((float) ((data[(data.length-2)-0]-lowestValue)*pixelsPerDollar)), chartGraphPaint);
			for(int i=data.length-3; i>=0; i--)
			{
				float x1 = linesStartingX + (xs*(data.length-2-i));
				float x2 = linesStartingX + (xs*(data.length-1-i));
				chartCanvas.drawLine(x1, lowestLineY-((float) ((data[i+1]-lowestValue)*pixelsPerDollar)), 
						x2, lowestLineY-((float)((data[i]-lowestValue)*pixelsPerDollar)), chartGraphPaint);
				
				chartPath.lineTo(x2, lowestLineY-((float)((data[i]-lowestValue)*pixelsPerDollar)));		
			}
						
			chartPath.lineTo(chartRightLimit, lowestLineY);
			chartPath.lineTo(linesStartingX, lowestLineY);
			chartPath.lineTo(linesStartingX + (xs*0), lowestLineY-((float) ((data[(data.length-1)-0]-lowestValue)*pixelsPerDollar)));
		float maxSpace = chartRightLimit-linesStartingX;
		float spacing=maxSpace/6f;
			switch(types[index])
			{
			case ONE_MONTH: 
				{
					for(int i=1; i<=5; i++)
					{
						chartCanvas.drawLine(linesStartingX+(i*spacing), timelineTop, linesStartingX+(i*spacing), timelineTop+(0.3f*timelineHeight), timelinePaint);
						int date = dateData[dateData.length-((int) ((i*spacing)/xs))-1].getDate();
						chartCanvas.drawText(""+date, linesStartingX+(i*spacing)-(timelinePaint.measureText(""+date)/2f), timelineTop+(0.75f*timelineHeight), timelinePaint);
					}
					oneMonthBitmap = chartBitmap;
					oneMonthBitmapTierNumbers = tierNumbersBitmap;
					break;
				}
			case THREE_MONTH:
				{
					for(int i=1; i<=5; i++)
					{
						chartCanvas.drawLine(linesStartingX+(i*spacing), timelineTop, linesStartingX+(i*spacing), timelineTop+(0.3f*timelineHeight), timelinePaint);
						int date = dateData[dateData.length-((int) ((i*spacing)/xs))-1].getDate();
						chartCanvas.drawText(""+date, linesStartingX+(i*spacing)-(timelinePaint.measureText(""+date)/2f), timelineTop+(0.75f*timelineHeight), timelinePaint);
					}
					threeMonthBitmap = chartBitmap;
					threeMonthBitmapTierNumbers = tierNumbersBitmap;
					break;
				}
			case SIX_MONTH: 
				{
					for(int i=1; i<=5; i++)
					{
						chartCanvas.drawLine(linesStartingX+(i*spacing), timelineTop, linesStartingX+(i*spacing), timelineTop+(0.3f*timelineHeight), timelinePaint);
						int date = dateData[dateData.length-((int) ((i*spacing)/xs))-1].getMonth()+1;
						chartCanvas.drawText(""+date, linesStartingX+(i*spacing)-(timelinePaint.measureText(""+date)/2f), timelineTop+(0.75f*timelineHeight), timelinePaint);
					}
					sixMonthBitmap = chartBitmap;
					sixMonthBitmapTierNumbers = tierNumbersBitmap;
					break;
				}
			case YEAR_TO_DATE: 
				{
					for(int i=1; i<=5; i++)
					{
						chartCanvas.drawLine(linesStartingX+(i*spacing), timelineTop, linesStartingX+(i*spacing), timelineTop+(0.3f*timelineHeight), timelinePaint);
						int date = dateData[dateData.length-((int) ((i*spacing)/xs))-1].getMonth()+1;
						chartCanvas.drawText(""+date, linesStartingX+(i*spacing)-(timelinePaint.measureText(""+date)/2f), timelineTop+(0.75f*timelineHeight), timelinePaint);
					}
					yearToDateBitmap = chartBitmap;
					yearToDateBitmapTierNumbers = tierNumbersBitmap;
					break;
				}
			case TWO_YEAR: 
				{
					for(int i=1; i<=5; i++)
					{
						chartCanvas.drawLine(linesStartingX+(i*spacing), timelineTop, linesStartingX+(i*spacing), timelineTop+(0.3f*timelineHeight), timelinePaint);
						int date = dateData[dateData.length-((int) ((i*spacing)/xs))-1].getMonth()+1;
						chartCanvas.drawText(""+date, linesStartingX+(i*spacing)-(timelinePaint.measureText(""+date)/2f), timelineTop+(0.75f*timelineHeight), timelinePaint);
					}
					twoYearBitmap = chartBitmap;
					twoYearBitmapTierNumbers = tierNumbersBitmap;
					break;
				}
			case FIVE_YEAR: 
				{
					for(int i=1; i<=5; i++)
					{
						chartCanvas.drawLine(linesStartingX+(i*spacing), timelineTop, linesStartingX+(i*spacing), timelineTop+(0.3f*timelineHeight), timelinePaint);
						String date = "'"+(""+(dateData[dateData.length-((int) ((i*spacing)/xs))-1].getYear())).substring(1);
						chartCanvas.drawText(""+date, linesStartingX+(i*spacing)-(timelinePaint.measureText(""+date)/2f), timelineTop+(0.75f*timelineHeight), timelinePaint);
					}
				fiveYearBitmap = chartBitmap;
				fiveYearBitmapTierNumbers = tierNumbersBitmap;
				break;
				}
			case TEN_YEAR: 
				{
					for(int i=1; i<=5; i++)
					{
						chartCanvas.drawLine(linesStartingX+(i*spacing), timelineTop, linesStartingX+(i*spacing), timelineTop+(0.3f*timelineHeight), timelinePaint);
						String date = "'"+(""+(dateData[dateData.length-((int) ((i*spacing)/xs))-1].getYear())).substring(1);
						chartCanvas.drawText(""+date, linesStartingX+(i*spacing)-(timelinePaint.measureText(""+date)/2f), timelineTop+(0.75f*timelineHeight), timelinePaint);
					}
				tenYearBitmap = chartBitmap;
				tenYearBitmapTierNumbers = tierNumbersBitmap;
				break;
				}
			}
			chartCanvas.clipPath(chartPath);
			Paint iPaint = new Paint();
			iPaint.setAntiAlias(true);
			iPaint.setShader(new LinearGradient(linesStartingX, lowestLineY, linesStartingX, highestLineY, new int[] {Color.argb(135, 51, 181, 229), Color.argb(90, 51, 181, 229), Color.argb(60, 51, 181, 229) }, null, Shader.TileMode.MIRROR));
			chartCanvas.drawRect(linesStartingX, highestLineY, chartRightLimit, lowestLineY, iPaint);
			
			
		}
		
		bitmapsCreated=true;
	}

	
	
	//helpers
	private double getChartHighestValue(ChartType type)
	{
		switch(type)
		{
		case ONE_MONTH: return oneMonthHigh;
		case THREE_MONTH: return threeMonthHigh;
		case SIX_MONTH: return sixMonthHigh;
		case YEAR_TO_DATE: return yearToDateHigh;
		case TWO_YEAR: return twoYearHigh;
		case FIVE_YEAR: return fiveYearHigh;
		case TEN_YEAR: return tenYearHigh;
		}
		return 0;
	}
	private double getChartLowestValue(ChartType type)
	{
		switch(type)
		{
		case ONE_MONTH: return oneMonthLow;
		case THREE_MONTH: return threeMonthLow;
		case SIX_MONTH: return sixMonthLow;
		case YEAR_TO_DATE: return yearToDateLow;
		case TWO_YEAR: return twoYearLow;
		case FIVE_YEAR: return fiveYearLow;
		case TEN_YEAR: return tenYearLow;
		}
		return 0;
	}
	private double[] getClosingDataFor(ChartType type)
	{
		Date[] monthly = mChart.getTenYearMonthlyDates();
		double[] monthlyValues = mChart.getTenYearMonthlyValues();
		Date[] daily = mChart.getFiveYearDailyDates();
		double[] dailyValues = mChart.getFiveYearDailyValues();
		
		Date today = Calendar.getInstance().getTime();
		switch(type)
		{
		case ONE_MONTH: 
		{
			double high = Double.MIN_VALUE;
			double low = Double.MAX_VALUE;
			Date oneMonthAgo;
			if(today.getMonth()==0)
			{
				oneMonthAgo = new Date(today.getYear()-1, 11, today.getDate());
			}
			else
			{
				oneMonthAgo = new Date(today.getYear(), today.getMonth()-1, today.getDate());
			}
			int i=0;
			for(i=0; (i<daily.length && oneMonthAgo.before(daily[i])); i++)
			{
				if(dailyValues[i] > high)
					high = dailyValues[i];
				if(dailyValues[i] < low)
					low = dailyValues[i];
			}
			oneMonthHigh=high;
			oneMonthLow=low;
			
			return Types.copyOfRangeDouble(mChart.getFiveYearDailyValues(), 0, i);
		}
		case THREE_MONTH: 
		{
			double high = Double.MIN_VALUE;
			double low = Double.MAX_VALUE;
			Date threeMonthAgo;
			if(today.getMonth()<3)
			{
				threeMonthAgo = new Date(today.getYear()-1, 12+(today.getMonth()-3) , today.getDate());
			}
			else
			{
				threeMonthAgo = new Date(today.getYear(), today.getMonth()-3, today.getDate());
			}
			int i=0;
			for(i=0; (i<daily.length && threeMonthAgo.before(daily[i])); i++)
			{
				if(dailyValues[i] > high)
					high = dailyValues[i];
				if(dailyValues[i] < low)
					low = dailyValues[i];
			}
			threeMonthHigh=high;
			threeMonthLow=low;
			
			return Types.copyOfRangeDouble(mChart.getFiveYearDailyValues(), 0, i);
		}
		case SIX_MONTH: 
		{
			double high = Double.MIN_VALUE;
			double low = Double.MAX_VALUE;
			Date sixMonthAgo;
			if(today.getMonth()<6)
			{
				sixMonthAgo = new Date(today.getYear()-1, 12+(today.getMonth()-6) , today.getDate());
			}
			else
			{
				sixMonthAgo = new Date(today.getYear(), today.getMonth()-6, today.getDate());
			}
			int i=0;
			for(i=0; (i<daily.length && sixMonthAgo.before(daily[i])); i++)
			{
				if(dailyValues[i] > high)
					high = dailyValues[i];
				if(dailyValues[i] < low)
					low = dailyValues[i];
			}
			sixMonthHigh=high;
			sixMonthLow=low;
			
			return Types.copyOfRangeDouble(mChart.getFiveYearDailyValues(), 0, i);
		}
		case YEAR_TO_DATE: 
		{
			double high = Double.MIN_VALUE;
			double low = Double.MAX_VALUE;
			Date twelveMonthAgo = new Date(today.getYear()-1, today.getMonth(), today.getDate());
			int i=0;
			for(i=0; (i<daily.length && twelveMonthAgo.before(daily[i])); i++)
			{
				if(dailyValues[i] > high)
					high = dailyValues[i];
				if(dailyValues[i] < low)
					low = dailyValues[i];
			}
			yearToDateHigh=high;
			yearToDateLow=low;
			
			return Types.copyOfRangeDouble(mChart.getFiveYearDailyValues(), 0, i);
		}
		case TWO_YEAR :
		{
			double high = Double.MIN_VALUE;
			double low = Double.MAX_VALUE;
			Date twoYearsAgo = new Date(today.getYear()-2, today.getMonth(), today.getDate());
			int i=0;
			for(i=0; (i<daily.length && twoYearsAgo.before(daily[i])); i++)
			{
				if(dailyValues[i] > high)
					high = dailyValues[i];
				if(dailyValues[i] < low)
					low = dailyValues[i];
			}
			twoYearHigh=high;
			twoYearLow=low;
			
			return Types.copyOfRangeDouble(mChart.getFiveYearDailyValues(), 0, i);
		}
		case FIVE_YEAR: 
		{
			double high = Double.MIN_VALUE;
			double low = Double.MAX_VALUE;
			Date fiveYearsAgo = new Date(today.getYear()-5, today.getMonth(), today.getDate());
			int i=0;
			for(i=0; (i<daily.length && fiveYearsAgo.before(daily[i])); i++)
			{
				if(dailyValues[i] > high)
					high = dailyValues[i];
				if(dailyValues[i] < low)
					low = dailyValues[i];
			}
			fiveYearHigh=high;
			fiveYearLow=low;
			
			return Types.copyOfRangeDouble(mChart.getFiveYearDailyValues(), 0, i);
		}
		case TEN_YEAR: 
		{
			double high = Double.MIN_VALUE;
			double low = Double.MAX_VALUE;
			Date tenYearsAgo = new Date(today.getYear()-10, today.getMonth(), today.getDate());
			int i=0;
			for(i=0; (i<monthly.length && tenYearsAgo.before(monthly[i])); i++)
			{
				if(monthlyValues[i] > high)
					high = monthlyValues[i];
				if(monthlyValues[i] < low)
					low = monthlyValues[i];
			}
			tenYearHigh=high;
			tenYearLow=low;
			
			return Types.copyOfRangeDouble(mChart.getTenYearMonthlyValues(), 0, i);
		}
		default: return null;
		}
	}
	private Date[] getDateDataFor(ChartType type)
	{
		Date[] monthly = mChart.getTenYearMonthlyDates();
		Date[] daily = mChart.getFiveYearDailyDates();
		
		Date today = Calendar.getInstance().getTime();
		switch(type)
		{
		case ONE_MONTH: 
		{
			Date oneMonthAgo;
			if(today.getMonth()==0)
			{
				oneMonthAgo = new Date(today.getYear()-1, 11, today.getDate());
			}
			else
			{
				oneMonthAgo = new Date(today.getYear(), today.getMonth()-1, today.getDate());
			}
			int i=0;
			for(i=0; (i<daily.length && oneMonthAgo.before(daily[i])); i++)
			{

			}

			
			return Types.copyOfRangeDate(mChart.getFiveYearDailyDates(), 0, i);
		}
		case THREE_MONTH: 
		{
			Date threeMonthAgo;
			if(today.getMonth()<3)
			{
				threeMonthAgo = new Date(today.getYear()-1, 12+(today.getMonth()-3) , today.getDate());
			}
			else
			{
				threeMonthAgo = new Date(today.getYear(), today.getMonth()-3, today.getDate());
			}
			int i=0;
			for(i=0; (i<daily.length && threeMonthAgo.before(daily[i])); i++)
			{

			}
			
			return Types.copyOfRangeDate(mChart.getFiveYearDailyDates(), 0, i);
		}
		case SIX_MONTH: 
		{
			Date sixMonthAgo;
			if(today.getMonth()<6)
			{
				sixMonthAgo = new Date(today.getYear()-1, 12+(today.getMonth()-6) , today.getDate());
			}
			else
			{
				sixMonthAgo = new Date(today.getYear(), today.getMonth()-6, today.getDate());
			}
			int i=0;
			for(i=0; (i<daily.length && sixMonthAgo.before(daily[i])); i++)
			{
				
			}

			
			return Types.copyOfRangeDate(mChart.getFiveYearDailyDates(), 0, i);
		}
		case YEAR_TO_DATE: 
		{
			Date twelveMonthAgo = new Date(today.getYear()-1, today.getMonth(), today.getDate());
			int i=0;
			for(i=0; (i<daily.length && twelveMonthAgo.before(daily[i])); i++)
			{

			}

			
			return Types.copyOfRangeDate(mChart.getFiveYearDailyDates(), 0, i);
		}
		case TWO_YEAR :
		{

			Date twoYearsAgo = new Date(today.getYear()-2, today.getMonth(), today.getDate());
			int i=0;
			for(i=0; (i<daily.length && twoYearsAgo.before(daily[i])); i++)
			{

			}

			
			return Types.copyOfRangeDate(mChart.getFiveYearDailyDates(), 0, i);
		}
		case FIVE_YEAR: 
		{
			Date fiveYearsAgo = new Date(today.getYear()-5, today.getMonth(), today.getDate());
			int i=0;
			for(i=0; (i<daily.length && fiveYearsAgo.before(daily[i])); i++)
			{

			}

			
			return Types.copyOfRangeDate(mChart.getFiveYearDailyDates(), 0, i);
		}
		case TEN_YEAR: 
		{

			Date tenYearsAgo = new Date(today.getYear()-10, today.getMonth(), today.getDate());
			int i=0;
			for(i=0; (i<monthly.length && tenYearsAgo.before(monthly[i])); i++)
			{

			}

			
			return Types.copyOfRangeDate(mChart.getTenYearMonthlyDates(), 0, i);
		}
		default: return null;
		}
	}
	private Bitmap getChartBitmap(ChartType type)
	{
		switch(type)
		{
		case ONE_MONTH: return oneMonthBitmap;
		case THREE_MONTH: return threeMonthBitmap;
		case SIX_MONTH: return sixMonthBitmap;
		case YEAR_TO_DATE: return yearToDateBitmap;
		case TWO_YEAR: return twoYearBitmap;
		case FIVE_YEAR: return fiveYearBitmap;
		case TEN_YEAR: return tenYearBitmap;
		
		}
		return null;
	}
	
	private Bitmap getCorrespondingBitmap(Bitmap bitmap)
	{
		if(bitmap == oneMonthBitmap)
			return oneMonthBitmapTierNumbers;
		else if(bitmap == threeMonthBitmap)
			return threeMonthBitmapTierNumbers;
		else if(bitmap == sixMonthBitmap)
			return sixMonthBitmapTierNumbers;
		else if(bitmap == yearToDateBitmap)
			return yearToDateBitmapTierNumbers;
		else if(bitmap == twoYearBitmap)
			return twoYearBitmapTierNumbers;
		else if(bitmap == fiveYearBitmap)
			return fiveYearBitmapTierNumbers;
		else if(bitmap == tenYearBitmap)
			return tenYearBitmapTierNumbers;
		
		else if(bitmap == oneMonthBitmapTierNumbers)
			return oneMonthBitmap;
		else if(bitmap == threeMonthBitmapTierNumbers)
			return threeMonthBitmap;
		else if(bitmap == sixMonthBitmapTierNumbers)
			return sixMonthBitmap;
		else if(bitmap == yearToDateBitmapTierNumbers)
			return yearToDateBitmap;
		else if(bitmap == twoYearBitmapTierNumbers)
			return twoYearBitmap;
		else if(bitmap == fiveYearBitmapTierNumbers)
			return fiveYearBitmap;
		else if(bitmap == tenYearBitmapTierNumbers)
			return tenYearBitmap;
		
		return null;
		
	}
	
	
	
	
	
	
	
	
	
	
	
	private float toPixels(float dip)
	{
		return  (dip * mDensityMultiplier); 
	}



	//draw helpers
	private void drawToggleBar(Canvas canvas, ChartType selected)
	{
		float textY = toggleBarTop+0.7f*toggleBarHeight;
		Paint ovalPaint = getToggleBarOvalPaint();
		Paint textPaint = getToggleBarTextPaint();
		for(int i=0; i<toggles.length; i++)
		{
			float right = toggleBarDivisions[i] + toggleButtonWidth;
			
			if(toggles[i]==selected)
			{
				ovalPaint.setColor(Color.argb(140, 51, 181, 229));
			}
			canvas.drawRect(new RectF(toggleBarDivisions[i], toggleBarTop, right, toggleBarTop+toggleBarHeight), ovalPaint);
			if(toggles[i]==selected)
			{
				ovalPaint.setColor(Color.argb(100, 255, 136, 0));
			}
			
			switch(toggles[i])
			{
			
			case THREE_MONTH:
			{
				float x = toggleBarDivisions[i]+((right-toggleBarDivisions[i])-textPaint.measureText("3m"))/2f;
				canvas.drawText("3m", x, textY, textPaint);
			}break;
			case SIX_MONTH: 
			{
				float x = toggleBarDivisions[i]+((right-toggleBarDivisions[i])-textPaint.measureText("6m"))/2f;
				canvas.drawText("6m", x, textY, textPaint);
			}break;
			case YEAR_TO_DATE: 
			{
				float x = toggleBarDivisions[i]+((right-toggleBarDivisions[i])-textPaint.measureText("12m"))/2f;
				canvas.drawText("12m", x, textY, textPaint);
			}break;
			case TWO_YEAR: 
			{
				float x = toggleBarDivisions[i]+((right-toggleBarDivisions[i])-textPaint.measureText("2y"))/2f;
				canvas.drawText("2y", x, textY, textPaint);
			}break;
			case FIVE_YEAR: 
			{
				float x = toggleBarDivisions[i]+((right-toggleBarDivisions[i])-textPaint.measureText("5y"))/2f;
				canvas.drawText("5y", x, textY, textPaint);
			}break;
			case TEN_YEAR: 
			{
				float x = toggleBarDivisions[i]+((right-toggleBarDivisions[i])-textPaint.measureText("10y"))/2f;
				canvas.drawText("10y", x, textY, textPaint);
			}break;
			default:
			case ONE_MONTH:
			{
				float x = toggleBarDivisions[i]+((right-toggleBarDivisions[i])-textPaint.measureText("1m"))/2f;
				canvas.drawText("1m", x, textY, textPaint);
			}break;
			
			}
		}
	}
	private void drawChart(Canvas canvas, Bitmap chart, int rawPercentage)
	{
		if(chart != null)
		{
			double percentage = rawPercentage / 100.0;
		
			if(percentage > 1)
			{
				percentage=1;
			}
			if(percentage < 0)
			{
				percentage=0;
			}
			
			if(percentage >= 1.0)
			{ //respect tierNumbers bitmap width
				canvas.drawBitmap(currentChartBitmap, getCorrespondingBitmap(currentChartBitmap).getWidth(), chartTop, new Paint());
			}
			else
			{
				Rect target = new Rect((int) chartLeft, (int) (chartTop+(chartHeight*(1-percentage))), (int) (chartLeft+chartWidth), (int) (chartTop+chartHeight));
		
				canvas.drawBitmap(chart, null, target, null);
			}
		}
		
	}
	private void drawTierNumbers(Canvas canvas, Bitmap tierNumbers, int rawPercentage)
	{
		if(tierNumbers != null)
			canvas.drawBitmap(tierNumbers, 0, 0, getTextPaintWithAlpha(rawPercentage));
	}
	

	private void updateSurface(Canvas canvas, Bitmap chartBitmap, Bitmap tierNumbersBitmap, 
			int chartPercentage, int tierNumbersPercentage, ChartType selected)
	{
		if(surfaceIsCreated)
		{
		//updateSurface() doesn't worry about synchronization, only threads do
		boolean canvasWasOriginallyNull = false;
		//must redraw chart, toggleBar, and toggleBar selection
		if(canvas == null)
		{
			canvas = getHolder().lockCanvas();
			canvasWasOriginallyNull = true;
		}
		//start with a blank white canvas
		canvas.drawARGB(Color.alpha(backgroundColor), Color.red(backgroundColor), Color.green(backgroundColor), Color.blue(backgroundColor));
		
		
		if(tierNumbersBitmap != null)
			drawTierNumbers(canvas, tierNumbersBitmap, tierNumbersPercentage);
		
		if(chartBitmap != null)
			drawChart(canvas, chartBitmap, chartPercentage);
		
		drawToggleBar(canvas, selected);
		

		if(canvasWasOriginallyNull)
		{
			getHolder().unlockCanvasAndPost(canvas);
		}
		}
		
		
	}

	private void displayErrorMessage(String string)
	{
		float height = 12*mDensityMultiplier;
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setTextSize(20*mDensityMultiplier);
		paint.setTypeface(robotoBold);
		
		float textSize = paint.measureText(string);
		
		Drawable warning = r.getDrawable(R.drawable.warning);
		Rect rect = new Rect((int)((getWidth()-(textSize+10+4*height))/2), (int)(getHeight()/2 - 2*height), (int)(((getWidth()-(textSize+10+2*height))/2)+4*height), (int)(getHeight()/2 + 2*height));
		warning.setBounds(rect);
		Canvas canvas = getHolder().lockCanvas();
			canvas.drawColor(Color.WHITE);
			warning.draw(canvas);
			canvas.drawText(string, (((getWidth()-(textSize+5+2*height))/2)+2*height)+10, (getHeight()/2), paint);
		getHolder().unlockCanvasAndPost(canvas);
	}

	public void initialize(Chart chart)
	{
		mChart = chart;
		if(chart.getTenYearMonthlyDates().length==0)
		{
			displayErrorMessage("Invalid ticker symbol");
		}
		else
		{
			measure();

			int size = mChart.getTenYearMonthlyDates().length;
			if(size <= 1)
			{
				createBitmaps(ChartType.ONE_MONTH);
				toggleBarDivisions = new float[1];
				int spacing = (int) ((toggleBarWidth-(1*toggleButtonWidth))/(1+1));
				int sideMargin = (int) ((getWidth()-toggleBarWidth)/2f);
				for(int i=1; i<=1; i++)
				{
					toggleBarDivisions[i-1] = sideMargin+spacing+((i-1)*toggleButtonWidth)+((i-1)*spacing);
				}
				ChartType[] temp = {ChartType.ONE_MONTH};
				toggles = temp;
			}
			else if(size <= 6)
			{
				createBitmaps(ChartType.ONE_MONTH, ChartType.THREE_MONTH, ChartType.SIX_MONTH);
				toggleBarDivisions = new float[3];
				int spacing = (int) ((toggleBarWidth-(3*toggleButtonWidth))/(3+1));
				int sideMargin = (int) ((getWidth()-toggleBarWidth)/2f);
				for(int i=1; i<=3; i++)
				{
					toggleBarDivisions[i-1] = sideMargin+spacing+((i-1)*toggleButtonWidth)+((i-1)*spacing);
				}
				ChartType[] temp = {ChartType.ONE_MONTH, ChartType.THREE_MONTH, ChartType.SIX_MONTH};
				toggles = temp;
			}
			else if(size <= 12)
			{
				createBitmaps(ChartType.ONE_MONTH, ChartType.THREE_MONTH, ChartType.SIX_MONTH, ChartType.YEAR_TO_DATE);
				toggleBarDivisions = new float[4];
				int spacing = (int) ((toggleBarWidth-(4*toggleButtonWidth))/(4+1));
				int sideMargin = (int) ((getWidth()-toggleBarWidth)/2f);
				for(int i=1; i<=4; i++)
				{
					toggleBarDivisions[i-1] = sideMargin+spacing+((i-1)*toggleButtonWidth)+((i-1)*spacing);
				}
				ChartType[] temp = {ChartType.ONE_MONTH, ChartType.THREE_MONTH, ChartType.SIX_MONTH, ChartType.YEAR_TO_DATE};
				toggles = temp;
			}
			else if(size <= 24)
			{
				createBitmaps(ChartType.ONE_MONTH, ChartType.SIX_MONTH, ChartType.YEAR_TO_DATE, ChartType.TWO_YEAR);
				toggleBarDivisions = new float[4];
				int spacing = (int) ((toggleBarWidth-(4*toggleButtonWidth))/(4+1));
				int sideMargin = (int) ((getWidth()-toggleBarWidth)/2f);
				for(int i=1; i<=4; i++)
				{
					toggleBarDivisions[i-1] = sideMargin+spacing+((i-1)*toggleButtonWidth)+((i-1)*spacing);
				}
				ChartType[] temp = {ChartType.ONE_MONTH, ChartType.SIX_MONTH, ChartType.YEAR_TO_DATE, ChartType.TWO_YEAR};
				toggles = temp;
			}
			else if(size <= 60)
			{
				createBitmaps(ChartType.ONE_MONTH, ChartType.SIX_MONTH, ChartType.YEAR_TO_DATE, ChartType.TWO_YEAR, ChartType.FIVE_YEAR);
				toggleBarDivisions = new float[5];
				int spacing = (int) ((toggleBarWidth-(5*toggleButtonWidth))/(5+1));
				int sideMargin = (int) ((getWidth()-toggleBarWidth)/2f);
				for(int i=1; i<=5; i++)
				{
					toggleBarDivisions[i-1] = sideMargin+spacing+((i-1)*toggleButtonWidth)+((i-1)*spacing);
				}
				ChartType[] temp = {ChartType.ONE_MONTH, ChartType.SIX_MONTH, ChartType.YEAR_TO_DATE, ChartType.TWO_YEAR, ChartType.FIVE_YEAR};
				toggles = temp;
			}
			else //more than five years
			{
				createBitmaps(ChartType.ONE_MONTH, ChartType.SIX_MONTH, ChartType.YEAR_TO_DATE, ChartType.FIVE_YEAR, ChartType.TEN_YEAR);
				toggleBarDivisions = new float[5];
				int spacing = (int) ((toggleBarWidth-(5*toggleButtonWidth))/(5+1));
				int sideMargin = (int) ((getWidth()-toggleBarWidth)/2f);
				for(int i=1; i<=5; i++)
				{
					toggleBarDivisions[i-1] = sideMargin+spacing+((i-1)*toggleButtonWidth)+((i-1)*spacing);
				}
				ChartType[] temp = {ChartType.ONE_MONTH, ChartType.SIX_MONTH, ChartType.YEAR_TO_DATE, ChartType.FIVE_YEAR, ChartType.TEN_YEAR};
				toggles = temp;
			}
			currentChartBitmap = getChartBitmap(ChartType.ONE_MONTH);
			currentTierNumbersBitmap = getCorrespondingBitmap(getChartBitmap(ChartType.ONE_MONTH));
			selectedToggle = ChartType.ONE_MONTH;
			synchronized(drawingLock)
			{
				updateSurface(null, currentChartBitmap, currentTierNumbersBitmap, 100, 100, selectedToggle);
			}
			touchIsDisabled = false;

		}
	}


	@Override
	public boolean onTouchEvent(MotionEvent e)
	{
		if(!touchIsDisabled)
		{		
			if(e.getAction() == MotionEvent.ACTION_UP)
			{
				if(e.getY() <= (toggleBarTop+toggleBarHeight) && e.getY() >= toggleBarTop)
				{
					for(int i=toggleBarDivisions.length-1; i>=0; i--)
					{
						if(e.getX() > toggleBarDivisions[i])
						{
							new ChartChanger().start(getChartBitmap(toggles[i]), getCorrespondingBitmap(getChartBitmap(toggles[i])), toggles[i]);
							touchIsDisabled=true;
							break;
						}
					}
				}
			}
		}
		return true;
	}
	
	//paint
	Paint recycledPaint = new Paint();
	private Paint getTextPaintWithAlpha(int percentage)
	{
		int alpha = 0;
		if(percentage > 100)
			alpha = 255;
		else if(percentage < 0)
			alpha = 0;
		else
			alpha = (int) (255*(percentage/100.0));
		
		recycledPaint.reset();
		recycledPaint.setAntiAlias(true);
		recycledPaint.setColor(Color.argb(alpha, 0, 0, 0)); //Black
		
		return recycledPaint;
	}
	private Paint getTiersTextPaint(String size, float space)
	{
		Paint paint = new Paint();
		paint.reset();
		paint.setTypeface(tiersFont);
		paint.setColor(Color.BLACK);
		
		paint.setTextSize(space);
		while(paint.measureText(size) > space)
		{
			paint.setTextSize(paint.getTextSize()-1);
		}
		paint.setAntiAlias(true);
		
		return paint;
	}
	private Paint getTimelinePaint(float textSize)
	{
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setTypeface(tiersFont);
		paint.setTextSize(textSize);
		return paint;
		
	}

	private synchronized Paint getChartLinesPaint()
	{
		Paint paint = recycledPaint;
		paint.reset();
		paint.setColor(chartLinesColor);
		paint.setStyle(Paint.Style.STROKE);
		paint.setAntiAlias(true);
		
		return paint;
	}

	private Paint getToggleBarTextPaint()
	{
		Paint paint = new Paint();
		paint.reset();
		paint.setColor(Color.WHITE);
		paint.setAntiAlias(true);
		paint.setTextSize(toPixels(toggleButtonWidth));
		while(paint.measureText("12m") > 0.6f*toggleButtonWidth)
		{
			paint.setTextSize(paint.getTextSize()-1);
		}
		paint.setTypeface(toggleBarIndicatorFont);
		
		
		return paint;
	}
	private Paint getToggleBarOvalPaint()
	{
		Paint paint = new Paint();
		paint.reset();
		paint.setColor(Color.argb(100, 255, 136, 0));
		paint.setAntiAlias(true);
		paint.setTextSize(toPixels(25));
		paint.setTypeface(toggleBarIndicatorFont);
		paint.setPathEffect(new CornerPathEffect(25));
		
		
		return paint;
	}
	
	
	public void turnColor(int color)
	{
		Canvas canvas = getHolder().lockCanvas();
		if(canvas!=null)
		{
			canvas.drawColor(color);
			getHolder().unlockCanvasAndPost(canvas);
		}
	}
	
	//surface callbacks
	
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		touchIsDisabled = true;
		surfaceIsCreated = true;
		//prevent user from seeing black screen in surfaceChanged
		
		measure();
		
		synchronized(drawingLock)
		{
			Canvas canvas = getHolder().lockCanvas();
			canvas.drawARGB(255, 255, 255, 255);
			getHolder().unlockCanvasAndPost(canvas);
		}
		if(bitmapsCreated)
		{
			currentChartBitmap = getChartBitmap(ChartType.ONE_MONTH);
			currentTierNumbersBitmap = getCorrespondingBitmap(getChartBitmap(ChartType.ONE_MONTH));
			synchronized(drawingLock)
			{
				updateSurface(null, currentChartBitmap, currentTierNumbersBitmap, 100, 100, ChartType.ONE_MONTH);
			}
	touchIsDisabled = false;
		}
	}
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) 
	{
		Canvas canvas = holder.lockCanvas();
		if(bitmapsCreated)
		{
			updateSurface(canvas, currentChartBitmap, currentTierNumbersBitmap, 100, 100, selectedToggle);
		}
		else{
			canvas.drawColor(Color.WHITE);
		}
		holder.unlockCanvasAndPost(canvas);
		
		
	}

	private void measure()
	{
		toggleBarHeight = toPixels(TOGGLE_BAR_HEIGHT_FIX); //in pixels
		toggleBarWidth = getWidth()-2*toPixels(HORIZONTAL_MARGIN);
		tiersHeight = getHeight()-toggleBarHeight-toPixels(VERTICAL_MARGIN);
		tiersWidth = getWidth()*0.1f;
		chartHeight = getHeight()-toggleBarHeight-toPixels(VERTICAL_MARGIN);
		chartWidth = getWidth()-toPixels(HORIZONTAL_MARGIN)-tiersWidth;
		
		
		toggleBarTop = chartHeight+toPixels(HORIZONTAL_MARGIN);
		chartTop = 0;
		chartLeft = tiersWidth+toPixels(VERTICAL_MARGIN);
		toggleButtonWidth = toggleBarWidth/6.0f; //max 5
	}
	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		surfaceIsCreated = false;
		
	}
	
	//threads
	
	private class ChartChanger extends Thread
	{
		//do not call start()
		
		private Bitmap nextChart;
		private Bitmap nextTierNumbers;
		private ChartType nextToggle;
		protected void start(Bitmap nextChart, Bitmap nextTierNumbers, ChartType selected)
		{
			this.nextChart = nextChart;
			this.nextTierNumbers = nextTierNumbers;
			this.nextToggle=selected;
			super.start();
		}
		//makes start() method from Thread invisible
		public void start()
		{
			throw new RuntimeException("called illegal method in ChartChanger/ChartWidget - start()");
		}
		
		@Override
		public void run()
		{
			synchronized(drawingLock)
			{
				Canvas surface;
				//remove current bitmaps
				for(int p=100; p > 0; p-=10)
				{
					surface = getHolder().lockCanvas();
						updateSurface(surface, currentChartBitmap, currentTierNumbersBitmap, p, p, selectedToggle);
					getHolder().unlockCanvasAndPost(surface);
				}
				updateSurface(null, currentChartBitmap, currentTierNumbersBitmap, 0, 0, selectedToggle);
					
				//animate new bitmaps
				for(int p=0; p < 100; p+=10)
				{
					//move toggleBar selection
					
					surface = getHolder().lockCanvas();
						updateSurface(surface, nextChart, nextTierNumbers, p, p, nextToggle);
					getHolder().unlockCanvasAndPost(surface);
				}

				currentChartBitmap = nextChart;
				currentTierNumbersBitmap = nextTierNumbers;
				selectedToggle = nextToggle;
				
				updateSurface(null, currentChartBitmap, currentTierNumbersBitmap, 100, 100, selectedToggle);
				touchIsDisabled = false;
			}
		}
	}
}
