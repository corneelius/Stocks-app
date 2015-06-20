package widget;

import home.CompletionNotificatee;
import home.HomeScreen;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import data.Index;
import data.Types;

public class IndexView extends SurfaceView implements SurfaceHolder.Callback{
	private HomeScreen homeScreen; 
	
	private Index mIndex;
	private float mDensityMultiplier;

	private int dark_grey;
	private int tierLinesColor;
	private Typeface tiersFont;
	private Typeface timeLineFont;
	private Typeface pricesValuesFont;
	private volatile Bitmap genericBitmap;
	private Object drawingLock;
	private final float textScaleX = 1.25f;
	private int backgroundColor;
	private CompletionNotificatee parent;
	private volatile Bitmap pendingDraw;
	private int maxProgressPointsAffected;
	private float pricesHeight;
	private float pricesTop;
	private volatile float timeLineY;
	private String subTitle;
	
	//for non-generic redraw
	private volatile float chartTop;
	private volatile float chartLeft;
	private volatile float chartRight;
	private volatile float chartBottom;
	private volatile float timeLineTimeTop;
	private volatile float timeLineTimeBottom;
	private volatile float[] tiersAboveZeroPositions;
	private volatile float[] tiersBelowZeroPositions;
	private volatile float zeroY;
	private volatile float tiersOffsetForText;
	private volatile float gapBetweenEntries;
	private volatile float tierLinesLeft;
	private volatile Paint timeLineTimePaint;
	private final int dji_color = Color.argb(255, 255, 68, 68);  //red
	private final int sandp_color = Color.argb(255, 102, 153, 0); //green
	private final int nasdaq_color = Color.argb(255, 0, 153, 204); //pink
	private volatile boolean variablesProcessed=false;
	private volatile int chartAlpha;
	private volatile String chartDateString;
	private volatile float screenWidth;
	private volatile float screenHeight;
	private volatile Typeface robotoBold;
	
	private int NUM_DATA_ENTRIES;
	private float HORIZONTAL_MARGIN;
	private float VERTICAL_MARGIN;
	private float tierNumberSpace;
	
	
	private volatile Paint pricesPaint;
	private volatile Paint valuesPaint;
	private volatile Paint percentPaint;
	private volatile Paint changePaint;
	
	//prices global state
		private boolean pricesVarsSet;
		private float PRICES_left;
		private float PRICES_right;
		private float PRICES_top;
		private float PRICES_bottom;
		private float PRICES_nameSpace;
		private float PRICES_circleSpace;
		private float PRICES_valueSpace;
		private float PRICES_percentAndChangeSpace;
		private float PRICES_marginSpace;
		private int PRICES_upGreen;
		private int PRICES_downRed;
		private float PRICES_pricesX;
		private float PRICES_valuesX;
		private float PRICES_changeX;
		private float PRICES_percentX;
		private float PRICES_spaceForEach;
		private float PRICES_dji_y;
		private float PRICES_sandp_y;
		private float PRICES_nasdaq_y;
		

	public IndexView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		screenWidth=display.getWidth();
		screenHeight=display.getHeight();
		
		
		mDensityMultiplier = getContext().getResources().getDisplayMetrics().density;
		dark_grey=Color.rgb(132,  132, 132);
		tierLinesColor=Color.rgb(0, 0, 0); //tierLinesColor=Color.argb(200, 153, 51, 204);
		tiersFont = Typeface.createFromAsset(getContext().getAssets(), "fonts/Roboto-ThinItalic.ttf");
		timeLineFont = Typeface.createFromAsset(getContext().getAssets(), "fonts/Roboto-Condensed.ttf");
		pricesValuesFont = Typeface.createFromAsset(getContext().getAssets(), "fonts/Roboto-Light.ttf");
		robotoBold = Typeface.createFromAsset(getContext().getAssets(), "fonts/Roboto-Bold.ttf");
		getHolder().addCallback(this);
		drawingLock = new Object();
		chartAlpha = 255;
		backgroundColor = Color.WHITE;
		
		//density multiplier must be set
		NUM_DATA_ENTRIES = 383+1;
		HORIZONTAL_MARGIN=toPixels(5); //dip
		VERTICAL_MARGIN=toPixels(5); //dip
		tierNumberSpace = toPixels(40); //dip
		
		float left = HORIZONTAL_MARGIN*2;
		float right = screenWidth-HORIZONTAL_MARGIN; 
		
		pricesPaint = getPricesPaint();
		pricesPaint.setTextScaleX(0.8f);
		pricesPaint.setTextSize(screenWidth*2f);
		pricesPaint.setTypeface(robotoBold);
		pricesPaint.setColor(Color.argb(200, 0, 0, 0));
		
		valuesPaint = getPricesPaint();
		valuesPaint.setTextSize(screenWidth);
		valuesPaint.setTypeface(robotoBold);
		valuesPaint.setColor(Color.argb(200, 0, 0, 0));
		
		changePaint = getPricesPaint();
		changePaint.setTextSize(screenWidth);
		changePaint.setTypeface(robotoBold);
		changePaint.setColor(Color.argb(200, 0, 0, 0));
		
		percentPaint = getPricesPaint();
		percentPaint.setTextSize(screenWidth);
		percentPaint.setTypeface(pricesValuesFont);
		percentPaint.setColor(Color.argb(200, 0, 0, 0));
		while( pricesPaint.measureText("NASDAQ") + valuesPaint.measureText("99,999,999.99") + percentPaint.measureText("(+10.00%)") + changePaint.measureText("+999.99") > (right-left)*(8/9f) )
		{
			pricesPaint.setTextSize(pricesPaint.getTextSize()*0.9f);
			valuesPaint.setTextSize(valuesPaint.getTextSize()*0.9f);
			percentPaint.setTextSize(percentPaint.getTextSize()*0.9f);
			changePaint.setTextSize(percentPaint.getTextSize()*0.9f);
		}
		
				
	}
	public void setHomeScreen(HomeScreen parent)
	{
		homeScreen=parent;
	}
	private void drawPrices(Canvas canvas, int index)
	{
		
		//assign vars
		if(!pricesVarsSet)
		{
			//guaranteed to be called after variables have been set in ImageCreator
			PRICES_left = HORIZONTAL_MARGIN*2;
			PRICES_right = getWidth()-HORIZONTAL_MARGIN;
			PRICES_top = pricesTop + VERTICAL_MARGIN;
			PRICES_bottom = getHeight()-VERTICAL_MARGIN;
			//size ratio: circles take .5/9, name takes 2/9, value takes 2/5, change and percent takes 3.5/9, margins take 1/9
			PRICES_nameSpace = getWidth()*(2/9f);
			PRICES_circleSpace = getWidth()*(0.5f/9f);
			PRICES_valueSpace = getWidth()*(2/5f);
			PRICES_percentAndChangeSpace = getWidth()*(3.5f/9f);
			PRICES_marginSpace = getWidth()*(1/9f);
		
			PRICES_upGreen = sandp_color;
			PRICES_downRed = dji_color;
		
		
			PRICES_pricesX = PRICES_left+PRICES_circleSpace;
			PRICES_valuesX = PRICES_pricesX + pricesPaint.measureText("NASDAQ") + PRICES_marginSpace*.5f;
			PRICES_changeX = PRICES_valuesX + valuesPaint.measureText("99,999,999.99") + PRICES_marginSpace*.5f;
			PRICES_percentX = PRICES_changeX + changePaint.measureText("+999.99");
		
		
			PRICES_spaceForEach = ((PRICES_bottom-PRICES_top)/7f)+pricesPaint.getTextSize();
			PRICES_dji_y = PRICES_top + PRICES_spaceForEach*1f-(0.5f*PRICES_spaceForEach);
			PRICES_sandp_y = PRICES_top + PRICES_spaceForEach*2f-(0.5f*PRICES_spaceForEach);
			PRICES_nasdaq_y = PRICES_top + PRICES_spaceForEach*3f-(0.5f*PRICES_spaceForEach);
			
			pricesVarsSet=true;
		}
		BigDecimal dji_now;
		BigDecimal sandp_now;
		BigDecimal nasdaq_now;
		String dji_change;
		String dji_percent;
		String sandp_change;
		String sandp_percent;
		String nasdaq_change;
		String nasdaq_percent;
		if(index==-1)
		{
			dji_now=mIndex.getDJItodaysClose();
			sandp_now=mIndex.getSandPtodaysClose();
			nasdaq_now=mIndex.getNasdaqtodaysClose();
			
			dji_change = verifyPriceString(dji_now.subtract(mIndex.getDJIPreviousClose()).toPlainString(), true);
			dji_percent = verifyPriceString( Types.toPercentString((dji_now.subtract(mIndex.getDJIPreviousClose())).divide(mIndex.getDJIPreviousClose(), 4, RoundingMode.HALF_UP)), true )+"%";
			
			sandp_change = verifyPriceString(sandp_now.subtract(mIndex.getSandpPreviousClose()).toPlainString(), true);
			sandp_percent = verifyPriceString( Types.toPercentString((sandp_now.subtract(mIndex.getSandpPreviousClose())).divide(mIndex.getSandpPreviousClose(), 4, RoundingMode.HALF_UP)), true )+"%";
			
			nasdaq_change = verifyPriceString(nasdaq_now.subtract(mIndex.getNasdaqPreviousClose()).toPlainString(), true);
			nasdaq_percent = verifyPriceString( Types.toPercentString((nasdaq_now.subtract(mIndex.getNasdaqPreviousClose())).divide(mIndex.getNasdaqPreviousClose(), 4, RoundingMode.HALF_UP)), true )+"%";
		}
		else
		{
			dji_now=mIndex.getDowJonesAt(index).getBigDecimal();
			sandp_now=mIndex.getSandpAt(index).getBigDecimal();
			nasdaq_now=mIndex.getNasdaqAt(index).getBigDecimal();
			
			dji_change = mIndex.getDowJonesChangeAt(index);
			dji_percent = mIndex.getDowJonesPercentChangeAt(index);
			
			sandp_change = mIndex.getSandpChangeAt(index);
			sandp_percent = mIndex.getSandpPercentChangeAt(index);
			
			nasdaq_change = mIndex.getNasdaqChangeAt(index);
			nasdaq_percent = mIndex.getNasdaqPercentChangeAt(index);
		}
		//draw
		canvas.drawCircle(PRICES_pricesX/2f, PRICES_dji_y-(valuesPaint.getTextSize()*0.5f), (PRICES_pricesX/2f)*0.5f, getRecycledPaintWithColor(dji_color, new RadialGradient(PRICES_pricesX/2f, PRICES_dji_y-(valuesPaint.getTextSize()*0.5f), (PRICES_pricesX/2f)*0.5f, new int[] {getColorWithAlpha(dji_color, 120), dji_color}, null, Shader.TileMode.CLAMP) ));
		canvas.drawText("DJIA", PRICES_pricesX, PRICES_dji_y, pricesPaint);
		canvas.drawText(verifyPriceString(dji_now.toPlainString(), false), PRICES_valuesX, PRICES_dji_y, valuesPaint);
		
		String s = dji_percent;
		if(s.charAt(0)=='+')
		{
			percentPaint.setColor(PRICES_upGreen);
			changePaint.setColor(PRICES_upGreen);
		}
		else
		{
			percentPaint.setColor(PRICES_downRed);
			changePaint.setColor(PRICES_downRed);
		}
		s = "("+s+")";
		canvas.drawText(dji_change, PRICES_changeX, PRICES_dji_y, changePaint);
		canvas.drawText(s, PRICES_percentX, PRICES_dji_y, percentPaint);

		
		
		canvas.drawCircle(PRICES_pricesX/2f, PRICES_sandp_y-(valuesPaint.getTextSize()*0.5f), (PRICES_pricesX/2f)*0.5f, getRecycledPaintWithColor(sandp_color, new RadialGradient(PRICES_pricesX/2f, PRICES_sandp_y-(valuesPaint.getTextSize()*0.5f), (PRICES_pricesX/2f)*0.5f, new int[] {getColorWithAlpha(sandp_color, 120), sandp_color}, null, Shader.TileMode.CLAMP)));
		canvas.drawText("S&P 500", PRICES_pricesX, PRICES_sandp_y, pricesPaint);
		canvas.drawText(verifyPriceString(sandp_now.toPlainString(), false), PRICES_valuesX, PRICES_sandp_y, valuesPaint);
		
		s = sandp_percent;
		if(s.charAt(0)=='+')
		{
			percentPaint.setColor(PRICES_upGreen);
			changePaint.setColor(PRICES_upGreen);
		}
		else
		{
			percentPaint.setColor(PRICES_downRed);
			changePaint.setColor(PRICES_downRed);
		}
		s = "("+s+")";
		canvas.drawText(sandp_change, PRICES_changeX, PRICES_sandp_y, changePaint);
		canvas.drawText(s, PRICES_percentX, PRICES_sandp_y, percentPaint);

		
		
		canvas.drawCircle(PRICES_pricesX/2f, PRICES_nasdaq_y-(valuesPaint.getTextSize()*0.5f), (PRICES_pricesX/2f)*0.5f, getRecycledPaintWithColor(nasdaq_color, new RadialGradient(PRICES_pricesX/2f, PRICES_nasdaq_y-(valuesPaint.getTextSize()*0.5f), (PRICES_pricesX/2f)*0.5f, new int[] {getColorWithAlpha(nasdaq_color, 120), nasdaq_color}, null, Shader.TileMode.CLAMP)));
		canvas.drawText("NASDAQ", PRICES_pricesX, PRICES_nasdaq_y, pricesPaint);
		canvas.drawText(verifyPriceString(nasdaq_now.toPlainString(), false), PRICES_valuesX, PRICES_nasdaq_y, valuesPaint);
		
		s = nasdaq_percent;
		if(s.charAt(0)=='+')
		{
			percentPaint.setColor(PRICES_upGreen);
			changePaint.setColor(PRICES_upGreen);
		}
		else
		{
			percentPaint.setColor(PRICES_downRed);
			changePaint.setColor(PRICES_downRed);
		}
		s = "("+s+")";
		canvas.drawText(nasdaq_change, PRICES_changeX, PRICES_nasdaq_y, changePaint);
		canvas.drawText(s, PRICES_percentX, PRICES_nasdaq_y, percentPaint);
	
		
	}
	
	private Paint recyclePaint=new Paint();
	private Paint getRecycledPaintWithColor(int color, Shader shader)
	{
		recyclePaint.reset();
		recyclePaint.setAntiAlias(true);
		recyclePaint.setColor(color);
		recyclePaint.setShader(shader);
		return recyclePaint;
	}
	
	private class ImageCreator extends Thread
	{
		private volatile Index index;

		
		
		public ImageCreator(Index index)
		{
			this.index=index;
		}
		@Override
		public void run()
		{
			
			publishProgress(10);
			while(getWidth() <= 0 || getHeight() <= 0) { /*wait*/ }
			
			Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
			Canvas canvas = new Canvas(bitmap);
			canvas.drawColor(Color.WHITE);
			
			final float timeLineHeight = toPixels(25); //dip
			pricesHeight = screenHeight*0.35f;
			final float lastUpdatedIndicatorHeight = toPixels(15);
			final float chartHeight = (canvas.getHeight()-((2*VERTICAL_MARGIN)+timeLineHeight+pricesHeight+lastUpdatedIndicatorHeight))-(VERTICAL_MARGIN);
			final float tierDelta = chartHeight/6f; //space between tiers
			final float minimumChartPadding = tierDelta*0.3f;

			
			
			//create scale
			BigDecimal dji_pcu = (index.getLargestDji().getBigDecimal().subtract(
					index.getDJIPreviousClose())).divide(index.getDJIPreviousClose(), 4, RoundingMode.HALF_UP);
			
			BigDecimal sandp_pcu = (index.getLargestSandp().getBigDecimal().subtract(
					index.getSandpPreviousClose())).divide(index.getSandpPreviousClose(), 4, RoundingMode.HALF_UP);
			
			BigDecimal nasdaq_pcu = (index.getLargestNasdaq().getBigDecimal().subtract(
					index.getNasdaqPreviousClose())).divide(index.getNasdaqPreviousClose(), 4, RoundingMode.HALF_UP);
			//
			BigDecimal dji_pcl = (index.getSmallestDji().getBigDecimal().subtract(
					index.getDJIPreviousClose())).divide(index.getDJIPreviousClose(), 4, RoundingMode.HALF_UP);
			
			BigDecimal sandp_pcl = (index.getSmallestSandp().getBigDecimal().subtract(
					index.getSandpPreviousClose())).divide(index.getSandpPreviousClose(), 4, RoundingMode.HALF_UP);
			
			BigDecimal nasdaq_pcl = (index.getSmallestNasdaq().getBigDecimal().subtract(
					index.getNasdaqPreviousClose())).divide(index.getNasdaqPreviousClose(), 4, RoundingMode.HALF_UP);
			
			BigDecimal[] pcuArray = {dji_pcu, sandp_pcu, nasdaq_pcu};
			BigDecimal[] pclArray = {dji_pcl, sandp_pcl, nasdaq_pcl};
			Types.sortBigDecimalArray(pcuArray);
			Types.sortBigDecimalArray(pclArray);
			BigDecimal wu = pcuArray[pcuArray.length-1]; //greatest
			BigDecimal wl = pclArray[0]; //smallest
			
			publishProgress(10);
			
			BigDecimal ulSum = wu.abs().add(wl.abs()); //absolute value - should not be negative
			
			float ulRatio = 0.5f;
			if(!(Math.abs(ulSum.floatValue()) < .001))	
			{
				ulRatio = (wu.divide(ulSum, 15, RoundingMode.HALF_UP)).floatValue(); //proportion of total change that is upside; 0-noUpside, 1-noDownside
			}

			//LAYOUT
			final float left = HORIZONTAL_MARGIN; //where number tiers are drawn
			final float top = 2*VERTICAL_MARGIN;
			final float right = canvas.getWidth()-HORIZONTAL_MARGIN;
			final float chartBottom = canvas.getHeight()-((2*VERTICAL_MARGIN)+timeLineHeight+pricesHeight+lastUpdatedIndicatorHeight); //need to account for timeLine's margin
			//LAYOUT
			
			
			float zeroY = top + (ulRatio * chartHeight);
			if(Math.abs(zeroY - top) < 5f)
			{
				zeroY = zeroY + 10f;
			}
			else if(Math.abs(chartBottom - zeroY) < 5f)
			{
				zeroY = zeroY - 10f;
			}
			
			float spaceAboveZero = zeroY - top;
			float spaceBelowZero = chartBottom - zeroY;
			
			final int aboveZeroTierCount = (int)((spaceAboveZero - minimumChartPadding) / tierDelta);
			final int belowZeroTierCount = (int) ((spaceBelowZero- minimumChartPadding) / tierDelta);
			
			float[] tiersAboveZeroPositions = new float[aboveZeroTierCount];
			float[] tiersBelowZeroPositions = new float[belowZeroTierCount];
			
			publishProgress(10);
			
			for(int i=1; i<=aboveZeroTierCount; i++)
			{
				tiersAboveZeroPositions[i-1] = zeroY-(tierDelta*(i));
			}
			for(int i=1; i<=belowZeroTierCount; i++)
			{
				tiersBelowZeroPositions[i-1] = zeroY+(tierDelta*(i));
			}
			
			publishProgress(10);
			
			BigDecimal[] tiersAboveZeroValues = new BigDecimal[tiersAboveZeroPositions.length];
			BigDecimal[] tiersBelowZeroValues = new BigDecimal[tiersBelowZeroPositions.length];
			
			BigDecimal aboveZeroUnroundedTierDelta=null;
			BigDecimal aboveZeroRoundedTierDelta=null;
			BigDecimal belowZeroUnroundedTierDelta=null;
			BigDecimal belowZeroRoundedTierDelta=null;
			if(aboveZeroTierCount != 0)
			{
				//aboveZeroTierCount
				//wu
				aboveZeroUnroundedTierDelta = wu.divide(new BigDecimal(aboveZeroTierCount), 10, RoundingMode.HALF_UP);
				aboveZeroRoundedTierDelta = roundRoughTierDelta(aboveZeroUnroundedTierDelta.multiply(new BigDecimal(100))); //in percent
			}
			else
				aboveZeroRoundedTierDelta = new BigDecimal(0); //prevent NullPointerExceptions
			if(belowZeroTierCount != 0)
			{
				//belowZeroTierCount
				//wl
				belowZeroUnroundedTierDelta = wl.divide(new BigDecimal(belowZeroTierCount), 10, RoundingMode.HALF_UP);
				belowZeroRoundedTierDelta = roundRoughTierDelta(belowZeroUnroundedTierDelta.multiply(new BigDecimal(100))); //in percent
			}
			else
				belowZeroRoundedTierDelta = new BigDecimal(0); //prevent NullPointerExceptions
			
			BigDecimal[] array2 = {aboveZeroRoundedTierDelta.abs(), belowZeroRoundedTierDelta.abs()};
			Types.sortBigDecimalArray(array2);
			
			BigDecimal tierValueDelta = array2[1]; //larger
			
			
			for(int i=1; i <= tiersAboveZeroPositions.length; i++)
			{
				tiersAboveZeroValues[i-1] = tierValueDelta.multiply(new BigDecimal(i));
			}
			
			for (int i=1; i <= tiersBelowZeroPositions.length; i++)
			{
				tiersBelowZeroValues[i-1] = (tierValueDelta.multiply(new BigDecimal(i))).negate();
			}
			publishProgress(10);

			
			//draw
			Paint tierLinesPaint = getTierLinesPaint();
			Paint tiersTextPaint = getTierTextPaint(tiersAboveZeroValues, tiersBelowZeroValues); //problem - fixed (infinite loop)
			final float tiersTextOffset = (tiersTextPaint.getTextSize()/2f) - toPixels(1);
			tierLinesLeft = left+tierNumberSpace+5;
		
			
				canvas.drawCircle(tierLinesLeft, zeroY-tiersTextOffset, toPixels(3), tierLinesPaint); //starting point
				canvas.drawText("0%", left, zeroY, tiersTextPaint);
				for(int i=1; i<=tiersAboveZeroPositions.length; i++)
				{
					canvas.drawText("+"+tiersAboveZeroValues[i-1].toPlainString()+"%", left, tiersAboveZeroPositions[i-1], tiersTextPaint);
				}
				for(int i=1; i<=tiersBelowZeroPositions.length; i++)
				{
					canvas.drawText(tiersBelowZeroValues[i-1].toPlainString()+"%", left, tiersBelowZeroPositions[i-1], tiersTextPaint);
				}
				
				publishProgress(10);


			float pixelsPerPercent = tierDelta / tierValueDelta.floatValue();

			Paint chartPaint_dji = getChartPaint(dji_color, chartAlpha);
			Paint chartPaint_sandp = getChartPaint(sandp_color, chartAlpha);
			Paint chartPaint_nasdaq = getChartPaint(nasdaq_color, chartAlpha);
			
			BigDecimal dji_zero_percent = index.getDJIPreviousClose();
			BigDecimal sandp_zero_percent = index.getSandpPreviousClose();
			BigDecimal nasdaq_zero_percent = index.getNasdaqPreviousClose();
			
			gapBetweenEntries = (right-tierLinesLeft) / (float)NUM_DATA_ENTRIES; // 390 entries per day, with open at zero percent
			

				canvas.drawLine(
					tierLinesLeft+(gapBetweenEntries*(0)),
					zeroY,
					tierLinesLeft+(gapBetweenEntries*(1)), 
					(float) (zeroY - (((((index.getDowJonesAt(0).getBigDecimal().subtract(dji_zero_percent))
					.divide(dji_zero_percent, 10, RoundingMode.HALF_UP)).multiply(new BigDecimal(100)).doubleValue())) * pixelsPerPercent)), 
					chartPaint_dji);
				canvas.drawLine(
					tierLinesLeft+(gapBetweenEntries*(0)),
					zeroY-tiersTextOffset,
					tierLinesLeft+(gapBetweenEntries*(1)), 
					(float) (zeroY - (((((index.getSandpAt(0).getBigDecimal().subtract(sandp_zero_percent))
					.divide(sandp_zero_percent, 10, RoundingMode.HALF_UP)).multiply(new BigDecimal(100)).doubleValue())) * pixelsPerPercent)), 
					chartPaint_sandp);
				canvas.drawLine(
					tierLinesLeft+(gapBetweenEntries*(0)),
					zeroY-tiersTextOffset,
					tierLinesLeft+(gapBetweenEntries*(1)), 
					(float) (zeroY - (((((index.getNasdaqAt(0).getBigDecimal().subtract(nasdaq_zero_percent))
					.divide(nasdaq_zero_percent, 10, RoundingMode.HALF_UP)).multiply(new BigDecimal(100)).doubleValue())) * pixelsPerPercent)), 
					chartPaint_nasdaq);
				
				for(int i=1; i<index.getDowJonesSize(); i++)
				{	
							
					canvas.drawLine(
						tierLinesLeft+(gapBetweenEntries*(i)),
						((int) (zeroY - (((((index.getDowJonesAt(i-1).getBigDecimal().subtract(dji_zero_percent))
								.divide(dji_zero_percent, 10, RoundingMode.HALF_UP)).multiply(new BigDecimal(100)).doubleValue())) * pixelsPerPercent))), 
						tierLinesLeft+(gapBetweenEntries*(i+1)), 
						((int) (zeroY - (((((index.getDowJonesAt(i).getBigDecimal().subtract(dji_zero_percent))
						.divide(dji_zero_percent, 10, RoundingMode.HALF_UP)).multiply(new BigDecimal(100)).doubleValue())) * pixelsPerPercent))), 
						chartPaint_dji);
				}
				for(int i=1; i<index.getSandpSize(); i++)
				{

					canvas.drawLine(
						tierLinesLeft+(gapBetweenEntries*(i)),
						((int) (zeroY - (((((index.getSandpAt(i-1).getBigDecimal().subtract(sandp_zero_percent))
								.divide(sandp_zero_percent, 10, RoundingMode.HALF_UP)).multiply(new BigDecimal(100)).doubleValue())) * pixelsPerPercent))), 
						tierLinesLeft+(gapBetweenEntries*(i+1)), 
						((int) (zeroY - (((((index.getSandpAt(i).getBigDecimal().subtract(sandp_zero_percent))
						.divide(sandp_zero_percent, 10, RoundingMode.HALF_UP)).multiply(new BigDecimal(100)).doubleValue())) * pixelsPerPercent))), 
						chartPaint_sandp);
					
				}
				for(int i=1; i<index.getNasdaqSize(); i++)
				{
					canvas.drawLine(
						tierLinesLeft+(gapBetweenEntries*(i)),
						((int) (zeroY - (((((index.getNasdaqAt(i-1).getBigDecimal().subtract(nasdaq_zero_percent))
								.divide(nasdaq_zero_percent, 10, RoundingMode.HALF_UP)).multiply(new BigDecimal(100)).doubleValue())) * pixelsPerPercent))), 
						tierLinesLeft+(gapBetweenEntries*(i+1)), 
						((int) (zeroY - (((((index.getNasdaqAt(i).getBigDecimal().subtract(nasdaq_zero_percent))
						.divide(nasdaq_zero_percent, 10, RoundingMode.HALF_UP)).multiply(new BigDecimal(100)).doubleValue())) * pixelsPerPercent))), 
						chartPaint_nasdaq);
				
				}
				
			publishProgress(30);
			
			//LAYOUT
			final float timeLineTop = chartBottom+VERTICAL_MARGIN;
			final float timeLineBottom = getHeight()-VERTICAL_MARGIN-pricesHeight-lastUpdatedIndicatorHeight;
			//LAYOUT
			
			
			float xDelta = right-tierLinesLeft;
			float ten = tierLinesLeft+(xDelta*(.5f/6.5f));
			float twelve = tierLinesLeft+(xDelta*(2.5f/6.5f));
			float two = tierLinesLeft+(xDelta*(4.5f/6.5f));
			
			float timeLineY = timeLineTop+((timeLineBottom-timeLineTop)/2);
			canvas.drawLine(tierLinesLeft, timeLineY, right, timeLineY, tiersTextPaint);
			canvas.drawLine(ten, timeLineY, ten, timeLineY+toPixels(5), tiersTextPaint);
			canvas.drawLine(twelve, timeLineY, twelve, timeLineY+toPixels(5), tiersTextPaint);
			canvas.drawLine(two, timeLineY, two, timeLineY+toPixels(5), tiersTextPaint);
			
			
			Paint timeLinePaint2 = getTimeLinePaint((timeLineBottom-timeLineTop)/2.5f);
			float timeLineNumbersY = ((timeLineTop+timeLineBottom)/2f)+(timeLinePaint2.getTextSize()+toPixels(3));
			canvas.drawText("10am", ten-(timeLinePaint2.measureText("9am")/2f), timeLineNumbersY, timeLinePaint2);
			canvas.drawText("12am", twelve-(timeLinePaint2.measureText("11am")/2f), timeLineNumbersY, timeLinePaint2);
			canvas.drawText("2pm", two-(timeLinePaint2.measureText("1pm")/2f), timeLineNumbersY, timeLinePaint2);
			
			//LAYOUT
			final float lastUpdatedIndicatorTop = timeLineBottom+VERTICAL_MARGIN+pricesHeight;
			final float lastUpdatedIndicatorBottom = lastUpdatedIndicatorTop+lastUpdatedIndicatorHeight;
			//LAYOUT
			String update=subTitle;
			if(subTitle==null)
				update = getUpdateString();
			Paint updatePaint = getLastUpdatePaint(lastUpdatedIndicatorHeight-5);
			float margin = (getWidth()-updatePaint.measureText(update))/2.0f;
			canvas.drawText(update, margin, (lastUpdatedIndicatorTop+lastUpdatedIndicatorBottom)/2.0f, updatePaint);
			
			
			genericBitmap = bitmap;
			
			
			//store variables for non-generic redraw
			chartLeft = tierLinesLeft;
			chartRight = right;
			chartTop = top;
			IndexView.this.chartBottom = chartBottom;
			timeLineTimeTop = timeLineTop;
			timeLineTimeBottom = timeLineY;
			IndexView.this.tiersAboveZeroPositions = tiersAboveZeroPositions;
			IndexView.this.tiersBelowZeroPositions = tiersBelowZeroPositions;
			IndexView.this.zeroY = zeroY;
			tiersOffsetForText = tiersTextOffset;
			IndexView.this.gapBetweenEntries = gapBetweenEntries;
			pricesTop = timeLineBottom+VERTICAL_MARGIN;
			IndexView.this.timeLineY=timeLineY;
			
			variablesProcessed = true;
			
			publishProgress(10);
			
			
			//non-generics
			timeLineTimePaint = getTimeLinePaint((timeLineTimeBottom-timeLineTimeTop));
			Date d = index.getDowJonesAt(0).getDate();
			chartDateString = getDateString(d); 

				
			if(getParent().getParent() != null)
			{
		synchronized(drawingLock)
			{
			Canvas surface;
				surface = getHolder().lockCanvas();
				while(surface == null) {surface = getHolder().lockCanvas();}
				surface.drawBitmap(genericBitmap, 0, 0, new Paint());
				drawTimeLineTime(surface, chartDateString, tierLinesLeft, chartRight, timeLineY, timeLineTimePaint);
				drawPrices(surface, mIndex.hasMarketClose() ? -1 : mIndex.getDowJonesSize()-1);
				drawChartLines(surface, backgroundColor, IndexView.this.tiersAboveZeroPositions, IndexView.this.tiersBelowZeroPositions, tierLinesLeft, chartRight, IndexView.this.zeroY, tierLinesPaint, tiersOffsetForText);
				getHolder().unlockCanvasAndPost(surface);
			}
			}
			parent.notification(null);
			
		}
		private String getUpdateString()
		{
			Date date = mIndex.getUpdatedDate();
			String day=null;
			if(date==null)
				day = "day";
			else
			{
				switch(date.getDay())
				{
				case 0: day = "Sunday"; break;
				case 1: day = "Monday"; break;
				case 2: day = "Tuesday"; break;
				case 3: day = "Wednesday"; break;
				case 4: day = "Thursday"; break;
				case 5: day = "Friday"; break;
				case 6: day = "Saturday"; break;
				default:day =  "day";

				}
			}
			String minutes;
			if(date.getMinutes()<10)
				minutes = "0"+date.getMinutes();
			else
				minutes = ""+date.getMinutes();
			
			return "Last updated on "+day+" at "+toMilitaryTime(date.getHours())+":"+minutes+(date.getHours() >= 12 ? " p.m." : " a.m.");
		}
		private Paint getLastUpdatePaint(float textSize)
		{
			if(recyclePaint==null)
				recyclePaint = new Paint();
			Paint paint = recyclePaint;
			paint.reset();
			paint.setTypeface(tiersFont);
			paint.setTextSize(textSize);
			paint.setAntiAlias(true);
			return paint;
		}
		private int toMilitaryTime(int hours)
		{
			if(hours==12)
			{
				return 12;
			}
			return hours%12;
		}
		
		private Paint getTierTextPaint(BigDecimal[] tiersAboveZeroValues, BigDecimal[] tiersBelowZeroValues)
		{
			Paint paint = new Paint();
			paint.setAntiAlias(true);
			paint.setColor(dark_grey);
			paint.setTextScaleX(textScaleX);
			paint.setTypeface(tiersFont);
			paint.setAlpha(255);
			paint.setTextSize(40); //largest possible
			
			float largestTextSize=0;
			String largestText="";
			for(int i=0; i<(tiersAboveZeroValues.length+tiersBelowZeroValues.length); i++)
			{
				if(i < tiersAboveZeroValues.length)
				{
					float temp = paint.measureText(tiersAboveZeroValues[i].toPlainString()+"%");
					if(temp > largestTextSize)
					{
						largestTextSize = temp;	
						largestText = tiersAboveZeroValues[i].toPlainString()+"%";
					}
				}
				else if(i >= tiersAboveZeroValues.length)
				{
					float temp = paint.measureText(tiersBelowZeroValues[i-tiersAboveZeroValues.length].toPlainString()+"%");
					if(temp > largestTextSize)
					{
						largestTextSize = temp;	
						largestText = tiersBelowZeroValues[i-tiersAboveZeroValues.length].toPlainString()+"%";
					}
				}
			}
			
			while(paint.measureText(largestText) > tierNumberSpace)
			{
				paint.setTextSize(paint.getTextSize()-1);
			}
			
			return paint;
		}
		
		private BigDecimal roundRoughTierDelta(BigDecimal rough)
		{
			boolean isNegative = (rough.signum() == -1);
			BigDecimal absRough = rough.abs();
			double temp = absRough.doubleValue();
			BigDecimal answer=null;
			
			if(temp < 0.1)
				answer = new BigDecimal(0.10);
			else if(temp < 0.2)
				answer = new BigDecimal(0.20);
			else if(temp < 0.25)
				answer = new BigDecimal(0.25);
			else if(temp < 0.3)
				answer = new BigDecimal(0.30);
			else if(temp < 0.4)
				answer = new BigDecimal(0.40);
			else if(temp < 0.5)
				answer = new BigDecimal(0.50);
			else if(temp < 0.6)
				answer = new BigDecimal(0.60);
			else if(temp < 0.7)
				answer = new BigDecimal(0.70);
			else if(temp < 0.75)
				answer = new BigDecimal(0.75);
			else if(temp < 0.8)
				answer = new BigDecimal(0.80);
			else if(temp < 0.9)
				answer = new BigDecimal(0.90);
			else if(temp < 1.0)
				answer = new BigDecimal(1.00);
			else if (temp < 1.25)
				answer = new BigDecimal(1.25);
			else if (temp < 1.5)
				answer = new BigDecimal(1.50);
			else if (temp < 1.75)
				answer = new BigDecimal(1.75);
			else if (temp < 2.0)
				answer = new BigDecimal(2.00);
			else if (temp < 2.5)
				answer = new BigDecimal(2.50);
			else if (temp < 3.0)
				answer = new BigDecimal(3.00);
			else if (temp < 4.0)
				answer = new BigDecimal(4.00);
			else
				answer = absRough.add(new BigDecimal(1));
			
			answer = answer.setScale(2, BigDecimal.ROUND_HALF_UP); //may need fixing
			
			if(isNegative)
				answer = answer.negate();
				
			return answer;
		}
		
	}
		
		private float toPixels(float dip)
		{
			return  (dip * mDensityMultiplier); 
		}

	private int getColorWithAlpha(int color, int alpha)
	{
		return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color));
	}
	
	public void initialize(Index index, CompletionNotificatee parent, int maxProgressPointsAffected, String subTitle)
	{
		this.maxProgressPointsAffected = maxProgressPointsAffected;
		this.parent = parent;
		this.subTitle=subTitle;
		if(index.isEmpty())
		{
			Canvas canvas = getHolder().lockCanvas();
			canvas.drawColor(Color.RED);
			getHolder().unlockCanvasAndPost(canvas);
		}
		else
		{
			mIndex = index;
			ImageCreator iCreate = new ImageCreator(index);
			iCreate.start();
		}
	
	}
	
	private void publishProgress(int progress)
	{
		int added = (int) (((double)progress/100.0) * maxProgressPointsAffected);
		if(added >= maxProgressPointsAffected)
			parent.updateProgress(maxProgressPointsAffected);
		else
			parent.updateProgress(added);
	}
	protected synchronized Bundle getBdundle()
	{
		Bundle bundle=null;
		if(variablesProcessed)
		{
		bundle = new Bundle();
		bundle.putString("chartDateString", chartDateString);
		bundle.putParcelable("genericBitmap", genericBitmap);
		bundle.putFloat("chartLeft", chartLeft);
		bundle.putFloat("chartTop", chartTop);
		bundle.putFloat("chartRight", chartRight);
		bundle.putFloat("chartBottom", chartBottom);
		bundle.putFloat("timeLineTimeTop", timeLineTimeTop);
		bundle.putFloat("timeLineTimeBottom", timeLineTimeBottom);
		bundle.putFloat("tierLinesLeft", tierLinesLeft);
		bundle.putFloat("tiersOffsetForText", tiersOffsetForText);
		bundle.putFloat("zeroY", zeroY);
		bundle.putFloat("timeLineY", timeLineY);
		bundle.putString("subTitle", subTitle);
		bundle.putInt("backgroundColor", backgroundColor);
		bundle.putInt("maxProgressPointsAffected", maxProgressPointsAffected);
		bundle.putFloatArray("tiersAboveZeroPositions", tiersAboveZeroPositions);
		bundle.putFloatArray("tiersBelowZeroPositions", tiersBelowZeroPositions);
		
		if(pricesVarsSet)
		{
			bundle.putBoolean("pricesVarsSet", pricesVarsSet);
			bundle.putFloat("PRICES_left", PRICES_left);
			bundle.putFloat("PRICES_right", PRICES_right);
			bundle.putFloat("PRICES_top", PRICES_top);
			bundle.putFloat("PRICES_bottom", PRICES_bottom);
			bundle.putFloat("PRICES_nameSpace", PRICES_nameSpace);
			bundle.putFloat("PRICES_circleSpace", PRICES_circleSpace);
			bundle.putFloat("PRICES_valueSpace", PRICES_valueSpace);
			bundle.putFloat("PRICES_percentAndChange", PRICES_percentAndChangeSpace);
			bundle.putFloat("PRICES_marginSpace", PRICES_marginSpace);
			bundle.putInt("PRICES_upGreen", PRICES_upGreen);
			bundle.putInt("PRICES_downRed", PRICES_downRed);
			bundle.putFloat("PRICES_pricesX", PRICES_pricesX);
			bundle.putFloat("PRICES_valuesX", PRICES_valuesX);
			bundle.putFloat("PRICES_changeX", PRICES_changeX);
			bundle.putFloat("PRICES_percentX", PRICES_percentX);
			bundle.putFloat("PRICES_spaceForEach", PRICES_spaceForEach);
			bundle.putFloat("PRICES_dji_y", PRICES_dji_y);
			bundle.putFloat("PRICES_sandp_y", PRICES_sandp_y);
			bundle.putFloat("PRICES_nasdaq_y", PRICES_nasdaq_y);
							
		}
		
		
		
		
		
		
		}
		else
			 bundle = null;
		
		return bundle;
	}

	public void initialize(CompletionNotificatee parent, Index index, Bundle bundle)
	{
		this.mIndex = index;
		if(bundle == null) //safety-net, called if Bundle was too big to save
		{
			initialize(index, parent, maxProgressPointsAffected, null);
			return;
		}
		this.maxProgressPointsAffected = bundle.getInt("maxProgressPointsAffected");
		this.chartDateString = bundle.getString("chartDateString");
		this.genericBitmap = (Bitmap)bundle.getParcelable("genericBitmap");
		this.chartLeft = bundle.getFloat("chartLeft");
		this.chartTop = bundle.getFloat("chartTop");
		this.chartRight = bundle.getFloat("chartRight");
		this.chartBottom = bundle.getFloat("chartBottom");
		this.timeLineTimeTop = bundle.getFloat("timeLineTimeTop");
		this.subTitle = bundle.getString("subTitle");
		this.timeLineTimeBottom = bundle.getFloat("timeLineTimeBottom");
		this.tierLinesLeft = bundle.getFloat("tierLinesLeft");
		this.tiersOffsetForText = bundle.getFloat("tiersOffsetForText");
		this.zeroY = bundle.getFloat("zeroY");
		this.backgroundColor = bundle.getInt("backgroundColor");
		this.tiersAboveZeroPositions = bundle.getFloatArray("tiersAboveZeroPositions");
		this.tiersBelowZeroPositions = bundle.getFloatArray("tiersBelowZeroPositions");
		this.timeLineY = bundle.getFloat("timeLineY");
		
		if(bundle.getBoolean("pricesVarsSet", false))
		{
			this.pricesVarsSet = bundle.getBoolean("pricesVarsSet");
			this.PRICES_left = bundle.getFloat("PRICES_left");
			this.PRICES_right = bundle.getFloat("PRICES_right");
			this.PRICES_top = bundle.getFloat("PRICES_top");
			this.PRICES_bottom = bundle.getFloat("PRICES_bottom");
			this.PRICES_nameSpace = bundle.getFloat("PRICES_nameSpace");
			this.PRICES_circleSpace = bundle.getFloat("PRICES_circleSpace");
			this.PRICES_valueSpace = bundle.getFloat("PRICES_valueSpace");
			this.PRICES_percentAndChangeSpace = bundle.getFloat("PRICES_percentAndChangeSpace");
			this.PRICES_marginSpace = bundle.getFloat("PRICES_marginSpace");
			this.PRICES_upGreen = bundle.getInt("PRICES_upGreen");
			this.PRICES_downRed = bundle.getInt("PRICES_downRed");
			this.PRICES_pricesX = bundle.getFloat("PRICES_pricesX");
			this.PRICES_valuesX = bundle.getFloat("PRICES_valuesX");
			this.PRICES_changeX = bundle.getFloat("PRICES_changeX");
			this.PRICES_percentX = bundle.getFloat("PRICES_percentX");
			this.PRICES_spaceForEach = bundle.getFloat("PRICES_spaceForEach");
			this.PRICES_dji_y = bundle.getFloat("PRICES_dji_y");
			this.PRICES_sandp_y = bundle.getFloat("PRICES_sandp_y");
			this.PRICES_nasdaq_y = bundle.getFloat("PRICES_nasdaq_y");
		}

		
		Bitmap bitmap = genericBitmap.copy(Bitmap.Config.ARGB_8888, true);
		Canvas canvas = new Canvas(bitmap);
		
		//non-generics
		timeLineTimePaint = getTimeLinePaint((timeLineTimeBottom-timeLineTimeTop));
		Paint tierLinesPaint = getTierLinesPaint();

			drawTimeLineTime(canvas, chartDateString, tierLinesLeft, chartRight, timeLineY, timeLineTimePaint);
			drawPrices(canvas, mIndex.getDowJonesSize()-1);
			drawChartLines(canvas, backgroundColor, IndexView.this.tiersAboveZeroPositions, IndexView.this.tiersBelowZeroPositions, tierLinesLeft, chartRight, IndexView.this.zeroY, tierLinesPaint, tiersOffsetForText);
			
		pendingDraw = bitmap;
	}
	private Paint getChartPaint(int color, int alpha)
	{
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setStrokeWidth(3);
		paint.setColor(getColorWithAlpha(color, alpha));
		
		return paint;
	}
	private void drawHorizontalUnderlappingLine(Canvas canvas, Bitmap bitmap, int colorToOnlyBe, float x, float y, float x2, float y2, Paint paint)
	{
		//guaranteed that x < x2 and y==y2
		for(float i=x; i <= x2; i++)
		{
			if(bitmap.getPixel((int)i, (int)y) == colorToOnlyBe)
			{
				canvas.drawPoint(i, y, paint);
			}
		}
	}

	private void drawChartLines(Canvas canvas, int colorToOnlyBe, float[] tiersAboveZeroPositions, float[] tiersBelowZeroPositions, float tierLinesLeft, float right, float zeroY, Paint tierLinesPaint, float tiersTextOffset)
	{
		int temp = tierLinesPaint.getColor();
		tierLinesPaint.setColor(getColorOnBackground(temp, Color.BLACK, 70));
		drawHorizontalUnderlappingLine(canvas, genericBitmap, colorToOnlyBe, tierLinesLeft, zeroY-tiersTextOffset, right, zeroY-tiersTextOffset, tierLinesPaint);
		tierLinesPaint.setColor(temp);
		for(int i=1; i<=tiersAboveZeroPositions.length; i++)
		{
			drawHorizontalUnderlappingLine(canvas, genericBitmap, colorToOnlyBe, tierLinesLeft, tiersAboveZeroPositions[i-1]-tiersTextOffset, right, tiersAboveZeroPositions[i-1]-tiersTextOffset, tierLinesPaint); //may need fixing
		}
		for(int i=1; i<=tiersBelowZeroPositions.length; i++)
		{
			drawHorizontalUnderlappingLine(canvas, genericBitmap, colorToOnlyBe, tierLinesLeft, tiersBelowZeroPositions[i-1]-tiersTextOffset, right, tiersBelowZeroPositions[i-1]-tiersTextOffset, tierLinesPaint); //may need fixing
		}
	}
	private void drawTimeLineTime(Canvas canvas, String date, float left, float right, float timeLineY, Paint timeLinePaint)
	{
		if(!(subTitle==null))
		{
			date = "A very old day";
		}
		canvas.drawText(date, ((left+right)/2f)-
			(timeLinePaint.measureText(date)/2f), timeLineY-(timeLinePaint.getTextSize()/2.0f), timeLinePaint);
		
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {		
		synchronized(drawingLock)
		{
		Canvas surface = holder.lockCanvas();
		if(pendingDraw != null)
		{
				surface.drawBitmap(pendingDraw, 0, 0, new Paint());
		}
		else if(variablesProcessed)
		{
			surface.drawBitmap(genericBitmap, 0, 0, new Paint());
			drawTimeLineTime(surface, chartDateString, chartLeft, chartRight, timeLineY, timeLineTimePaint);
			drawPrices(surface, (mIndex.hasMarketClose() ? -1 : mIndex.getDowJonesSize()-1) );
			drawChartLines(surface, backgroundColor, tiersAboveZeroPositions, tiersBelowZeroPositions, chartLeft, chartRight, zeroY, getTierLinesPaint(), tiersOffsetForText);
		}
		else
		{
			surface.drawColor(Color.WHITE);
		}
		holder.unlockCanvasAndPost(surface);
		}
		
	}
	private Paint getTimeLinePaint(float tx)
	{
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setColor(dark_grey);
		paint.setTextScaleX(textScaleX);
		paint.setTextSize(tx);
		paint.setTypeface(timeLineFont);
		paint.setAlpha(200);
		
		return paint;
	}
	private String verifyHour(int hour)
	{
		String string = "";
		if(hour == 0)
		{
			string = "12";
		}
		else
			string = ""+hour;
		
		return string;
	}
	private String verifyMinute(int minute)
	{
		String string = "";
		if(minute < 10)
		{
			string = "0"+minute;
		}
		else
			string = ""+minute;
		
		return string;
	}
	private int getColorOnBackground(int color, int backgroundColor, int percentAlpha)
	{
		double percent = percentAlpha/100.0;
		return Color.rgb((int)((Color.red(color)*percent)+(Color.red(backgroundColor)*(1-percent))), (int)((Color.green(color)*percent)+(Color.green(backgroundColor)*(1-percent))), 
				(int)((Color.blue(color)*percent)+(Color.blue(backgroundColor)*(1-percent))));
	}
	
	public void forceDraw()
	{
		if(genericBitmap != null && tiersAboveZeroPositions != null)
		{

			touchedDownWithinBounds=false;
			synchronized(drawingLock)
			{
				Canvas surface = getHolder().lockCanvas();
				surface.drawBitmap(genericBitmap, 0, 0, new Paint());
				drawTimeLineTime(surface, chartDateString, chartLeft, chartRight, timeLineY, timeLineTimePaint);
				drawPrices(surface, (mIndex.hasMarketClose() ? -1 : mIndex.getDowJonesSize()-1) );
				drawChartLines(surface, backgroundColor, tiersAboveZeroPositions, tiersBelowZeroPositions, chartLeft, chartRight, zeroY, getTierLinesPaint(), tiersOffsetForText);
				getHolder().unlockCanvasAndPost(surface);
			}
		}
		else if(mIndex != null)
		{
			ImageCreator iCreate = new ImageCreator(mIndex);
			iCreate.start();
		}
		
	}
	
	private boolean touchedDownWithinBounds=false;
	private boolean isBeingInteractedWith=false;
	@Override
	public boolean onTouchEvent(MotionEvent e)
	{

		if( e.getY() < timeLineTimeTop && e.getY() > chartTop && e.getX() >= tierLinesLeft && e.getX() <= (chartLeft+(mIndex.getDowJonesSize()-1)*gapBetweenEntries))
		{
			if(e.getAction()==MotionEvent.ACTION_DOWN || e.getAction() == MotionEvent.ACTION_MOVE)
			{
				if(e.getAction()==MotionEvent.ACTION_DOWN)
				{
					touchedDownWithinBounds=true;
					isBeingInteractedWith=true;
				}
				
				if(e.getAction() == MotionEvent.ACTION_MOVE && !touchedDownWithinBounds)
					homeScreen.onIndexTouched(e, true);
				
				if(variablesProcessed && !(e.getAction() == MotionEvent.ACTION_MOVE && !touchedDownWithinBounds))
				{
				
				Date time = mIndex.getDowJonesAt((int)((e.getX() - chartLeft) / gapBetweenEntries)).getDate();				
				
				Calendar calendar = Calendar.getInstance(Locale.US);
				calendar.setTime(time);
				calendar.setTimeZone(TimeZone.getTimeZone("America/New_York"));
				String string = verifyHour(calendar.get(Calendar.HOUR))+":"+verifyMinute(calendar.get(Calendar.MINUTE))+(calendar.get(Calendar.AM_PM)==0 ? " A.M." : " P.M.");
				
				synchronized(drawingLock)
				{
					Canvas surface = getHolder().lockCanvas();
					surface.drawBitmap(genericBitmap, 0, 0, new Paint());
					drawPrices(surface, (int) ((e.getX()-chartLeft)/gapBetweenEntries) );
					drawTimeLineTime(surface, string, chartLeft, chartRight, timeLineY, timeLineTimePaint);
					drawChartLines(surface, backgroundColor, tiersAboveZeroPositions, tiersBelowZeroPositions, chartLeft, chartRight, zeroY, getTierLinesPaint(), tiersOffsetForText);
					
					surface.drawLine(e.getX(), chartTop, e.getX(), chartBottom, timeLineTimePaint);
					getHolder().unlockCanvasAndPost(surface);
				}
				}
			}
			else if(touchedDownWithinBounds && e.getAction() == MotionEvent.ACTION_UP)
			{
				touchedDownWithinBounds=false;
				synchronized(drawingLock)
				{
					Canvas surface = getHolder().lockCanvas();
					surface.drawBitmap(genericBitmap, 0, 0, new Paint());
					drawTimeLineTime(surface, chartDateString, chartLeft, chartRight, timeLineY, timeLineTimePaint);
					drawPrices(surface, (mIndex.hasMarketClose() ? -1 : mIndex.getDowJonesSize()-1) );
					drawChartLines(surface, backgroundColor, tiersAboveZeroPositions, tiersBelowZeroPositions, chartLeft, chartRight, zeroY, getTierLinesPaint(), tiersOffsetForText);
					
					getHolder().unlockCanvasAndPost(surface);
				}
				isBeingInteractedWith=false;
			}
			else if(!touchedDownWithinBounds && e.getAction() == MotionEvent.ACTION_UP)
			{
				homeScreen.onIndexTouched(e, true);
			}
		}
		else
		{
			if(e.getAction()==MotionEvent.ACTION_DOWN)
				touchedDownWithinBounds=false;
			
			if(touchedDownWithinBounds)
			{
				synchronized(drawingLock)
				{
					isBeingInteractedWith=false;
					Canvas surface = getHolder().lockCanvas();
					surface.drawBitmap(genericBitmap, 0, 0, new Paint());
					drawTimeLineTime(surface, chartDateString, chartLeft, chartRight, timeLineY, timeLineTimePaint);
					drawPrices(surface, (mIndex.hasMarketClose() ? -1 : mIndex.getDowJonesSize()-1) );
					drawChartLines(surface, backgroundColor, tiersAboveZeroPositions, tiersBelowZeroPositions, chartLeft, chartRight, zeroY, getTierLinesPaint(), tiersOffsetForText);
					getHolder().unlockCanvasAndPost(surface);
				}
			}
			else
			{
				homeScreen.onIndexTouched(e, true);
			}
		}
		return true;
	}
	public boolean isBeingInteractedWith()
	{
		return isBeingInteractedWith;
	}
	private Paint getPricesPaint()
	{
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setAlpha(255);
		
		return paint;
	}
	
	public void getCache(int top, int right, Bitmap bitmap)
	{
		Bitmap tempBitmap = genericBitmap.copy(Bitmap.Config.ARGB_8888, true);
		Canvas tempCanvas = new Canvas(tempBitmap);
		drawTimeLineTime(tempCanvas, chartDateString, chartLeft, chartRight, timeLineY, timeLineTimePaint);
		drawPrices(tempCanvas, (mIndex.hasMarketClose() ? -1 : mIndex.getDowJonesSize()-1) );
		drawChartLines(tempCanvas, backgroundColor, tiersAboveZeroPositions, tiersBelowZeroPositions, chartLeft, chartRight, zeroY, getTierLinesPaint(), tiersOffsetForText);
		
		Canvas returnCanvas = new Canvas(bitmap);
		returnCanvas.drawBitmap(tempBitmap, right, top, new Paint());
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

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		if(variablesProcessed)
		{
				Canvas surface = holder.lockCanvas();
				surface.drawBitmap(genericBitmap, 0, 0, new Paint());
				drawTimeLineTime(surface, chartDateString, chartLeft, chartRight, timeLineY, timeLineTimePaint);
				drawPrices(surface, (mIndex.hasMarketClose() ? -1 : mIndex.getDowJonesSize()-1) );
				drawChartLines(surface, backgroundColor, tiersAboveZeroPositions, tiersBelowZeroPositions, chartLeft, chartRight, zeroY, getTierLinesPaint(), tiersOffsetForText);
				holder.unlockCanvasAndPost(surface);
			
		}
		else
		{
			Canvas canvas = holder.lockCanvas();
			canvas.drawColor(Color.WHITE);
			holder.unlockCanvasAndPost(canvas);
		}
		
	}
	
	@Override
	public void onDraw(Canvas canvas)
	{
		super.onDraw(canvas);
	}
	private Paint getTierLinesPaint()
	{
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setColor(getColorOnBackground(tierLinesColor, Color.WHITE, 45));
		return paint;
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		
	}
	private String getDateString(Date date)
	{
		String day_of_week="";
		String month="";
		
		switch(date.getDay())
		{
		case 1:
		{
			day_of_week="Sunday";
			break;
		}
		case 2:
		{
			day_of_week="Monday";
			break;
		}
		case 3:
		{
			day_of_week="Tuesday";
			break;
		}
		case 4:
		{
			day_of_week="Wednesday";
			break;
		}
		case 5:
		{
			day_of_week="Thursday";
			break;
		}
		case 6:
		{
			day_of_week="Friday";
			break;
		}
		case 7:
		{
			day_of_week="Saturday";
			break;
		}
		
		}
		
		switch(date.getMonth())
		{
		case 0:
		{
			month="January";
			break;
		}
		case 1:
		{
			month="February";
			break;
		}
		case 2:
		{
			month="March";
			break;
		}
		case 3:
		{
			month="April";
			break;
		}
		case 4:
		{
			month="May";
			break;
		}
		case 5:
		{
			month="June";
			break;
		}
		case 6:
		{
			month="July";
			break;
		}
		case 7:
		{
			month="August";
			break;
		}
		case 8:
		{
			month="September";
			break;
		}
		case 9:
		{
			month="October";
			break;
		}
		case 10:
		{
			month="November";
			break;
		}
		case 11:
		{
			month="December";
			break;
		}
		}
		
		String string = day_of_week+", "+month+" "+date.getDate();
		return string;
	}

}
