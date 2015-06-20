package watchlist;

import java.util.ArrayList;

import graphics.WDrawable;
import home.HomeScreen;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.view.MotionEvent;
import android.view.View;
import data.StockGenerics;

public class WatchlistItem extends View {
	private String name;
	private StockGenerics[] stocks;
	private ArrayList<String> tickers;
	private boolean isSkeleton;
	private int titleColor = Color.argb(255, 51, 181, 229);
	private HomeScreen parent;
	public enum Modes {NORMAL, DELETE, EDIT};
	private Modes mMode;
	private Typeface robotoBold;
	private Typeface robotoRegular;
	private float mDensityMultiplier;
	private int red = Color.rgb(255, 68, 68);
	private int green = Color.rgb(102, 153, 0);
	
	public WatchlistItem(Context context, String name, StockGenerics[] stocks, HomeScreen parent)
	{
		super(context);
		this.name=name;
		this.stocks=stocks;
		this.parent=parent;
		mMode = Modes.NORMAL;
		robotoBold = Typeface.createFromAsset(getContext().getAssets(), "fonts/Roboto-Bold.ttf");
		robotoRegular = Typeface.createFromAsset(getContext().getAssets(), "fonts/Roboto-Regular.ttf");
		mDensityMultiplier = getContext().getResources().getDisplayMetrics().density;
		

	}
	
	public WatchlistItem(Context context, String name, ArrayList<String> stocks, HomeScreen parent)
	{
		super(context);
		isSkeleton=true;
		this.name=name;
		this.tickers=stocks;
		this.parent=parent;
		mMode = Modes.NORMAL;
		robotoBold = Typeface.createFromAsset(getContext().getAssets(), "fonts/Roboto-Bold.ttf");
		robotoRegular = Typeface.createFromAsset(getContext().getAssets(), "fonts/Roboto-Regular.ttf");
		mDensityMultiplier = getContext().getResources().getDisplayMetrics().density;
	}
	

	
	public String getName()
	{
		return name;
	}
	public void setMode(Modes mode)
	{
		mMode = mode;
		invalidate();
	}
	public Modes getMode()
	{
		return mMode;
	}
	private double getPercentChangeAverage()
	{
		double sum=0;
		for(StockGenerics s : stocks)
		{
			sum+=s.getPercentChange();
		}
		return (sum/stocks.length);
	}
	
	@Override
	public void onDraw(Canvas canvas)
	{
		int top = (int) ((getHeight()-(0.5*getHeight()))/2);
		int right = (int) (0.5*getHeight());
		int bottom = (int) (getHeight()-top);
		if(mMode==Modes.DELETE)
		{
			WDrawable d = new WDrawable(Color.argb(255, 255, 68, 68));
			d.setBounds(0, top, right, bottom);
			d.draw(canvas);
		}
		else if(mMode==Modes.EDIT)
		{
			WDrawable d = new WDrawable(Color.argb(255, 0, 153, 204));
			d.setBounds(0, top, right, bottom);
			d.draw(canvas);
		}
		
		//divide into 14ths
		int topMargin = (int) (getHeight()*(1.0/16));
		int spaceBetween = (int) (getHeight()*(1.0/16));
		int titleHeight = (int) (getHeight()*(5.0/16));
		int statsHeight = (int) (getHeight()*(3.0/16));
		int symbolsHeight = (int) (getHeight()*(3.0/16));
		int left = (int) (0.5*getHeight()+5);
		
		Paint textPaint = getTitlePaint(titleHeight-5);
		textPaint.setTypeface(robotoBold);
		textPaint.setUnderlineText(true);
		
		//title
		canvas.drawText(getFittingText(name, (getWidth()-left), textPaint), left, topMargin+titleHeight, textPaint);
		
		//% change
		textPaint.setUnderlineText(false);
		textPaint.setTextSize(statsHeight);
		textPaint.setTypeface(robotoRegular);
		textPaint.setColor(Color.rgb(37, 33, 32));
		if(!isSkeleton)
		{
			double change = getPercentChangeAverage();
			if(change > 0)
				textPaint.setColor(green);
			else
				textPaint.setColor(red);

			String change2 = ""+change;
			int endIndex = change2.indexOf('.')+3;

			if(!(change2.indexOf('.')==-1))
			{
				while(endIndex>change2.length())
					endIndex--;
				change2 = change2.substring(0, endIndex);
			}
			canvas.drawText(change2+"%", left, topMargin+titleHeight+statsHeight+spaceBetween, textPaint);
		
		
		//volume
		textPaint.setColor(Color.rgb(37, 33, 32));
		String volume = "Volume: "+getAverageVolume();
		float volumeSpace = textPaint.measureText(volume);
		canvas.drawText(volume, getWidth()-volumeSpace-(8*mDensityMultiplier), topMargin+topMargin+titleHeight+statsHeight, textPaint);
		}
		else
		{
			canvas.drawText("--", left, topMargin+titleHeight+statsHeight+spaceBetween, textPaint);
			float volumeSpace = textPaint.measureText("--");
			canvas.drawText("--", getWidth()-volumeSpace-(8*mDensityMultiplier), topMargin+titleHeight+statsHeight, textPaint);
		}
		
		
		//symbols
		textPaint.setTextSize(symbolsHeight);
		canvas.drawText(getFittingText(getTickers(isSkeleton ? true : false), (getWidth()-left), textPaint), left, topMargin+titleHeight+statsHeight+symbolsHeight+2*spaceBetween, textPaint);
		
		
		
		
		
		Paint paint = new Paint();
		paint.setColor(Color.argb(50, 37, 33, 32));
		canvas.drawLine(0, getHeight()-1, getWidth(), getHeight()-1, paint);
		
		
	}
	private String getAverageVolume()
	{
		double vol=0;
		for(StockGenerics c : stocks)
		{
			vol+=c.getVolume();
		}
		vol = vol/stocks.length;
		if(vol <= 1000000)
		{
			String s = ""+(int)vol;
			return s;
		}
		else if(vol>1000000)
		{
			vol = vol/1000000;
			String s = ""+vol;
			return (s.substring(0, s.indexOf('.')+3) + "M");
		}
		else
		{
			vol = vol/1000000000;
			String s = ""+vol;
			return (s.substring(0, s.indexOf('.')+3) + "B");
		}
		
	}
	private String getTickers(boolean arrayList)
	{
		if(arrayList)
		{
			String s="";
			for(int i=0; i<tickers.size(); i++)
			{
				if(i==tickers.size()-1)
					s+=tickers.get(i);
				else
					s+=(tickers.get(i)+", ");
			}
			return s;
		}
		else
		{
			String s="";
			for(int i=0; i<stocks.length; i++)
			{
				if(i==stocks.length-1)
					s+=stocks[i].getTicker().toString();
				else
					s+=(stocks[i].getTicker().toString()+", ");
			}
			return s;
		}
	}
	private String getFittingText(String text, float size, Paint paint)
	{
		float space = paint.measureText(text);
		if(space < size)
		{
			return text;
		}

		float dooot = paint.measureText(" ... ");
		float percent = 1f-( ((space+dooot)-size) /space);
		int passedLetters = (int) (percent*text.length());
		
		String trimmed = (text.substring(0, passedLetters)+"...");
		
		return trimmed;
		
	}
	private Paint getTitlePaint(int textSize)
	{
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setColor(titleColor);
		paint.setTextSize(textSize);
		return paint;
	}
	@Override
	public boolean onTouchEvent(MotionEvent e)
	{
		
		if(e.getAction() == MotionEvent.ACTION_UP && mMode==Modes.DELETE && e.getRawX()<getHeight())
		{
			parent.requestRemoveWatchlist(name);
		}
		else if(e.getAction() == MotionEvent.ACTION_UP && mMode==Modes.EDIT && e.getRawX()<getHeight())
		{
			parent.editWatchlist(name);
		}
		else if(e.getAction() == MotionEvent.ACTION_UP)
		{
			Intent intent = new Intent(getContext(), FireScreen.class);
				Intent returnIntent = new Intent(getContext(), HomeScreen.class);
			intent.putExtra("intent", returnIntent);
			intent.putExtra("name", name);
			getContext().startActivity(intent);
			
		}
		return true;
	}
	
}
