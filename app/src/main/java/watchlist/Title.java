package watchlist;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;
import data.StockGenerics;
import data.Types;

public class Title extends View{
	private Typeface robotoBold;
	private StockGenerics[] mGenerics;
	private boolean hasBeenInitialized;
	private String mName;
	private Typeface robotoRegular;

	public Title(Context context, AttributeSet attrs) {
		super(context, attrs);
		robotoBold = Typeface.createFromAsset(getContext().getAssets(), "fonts/Roboto-Bold.ttf");
		robotoRegular = Typeface.createFromAsset(getContext().getAssets(), "fonts/Roboto-Regular.ttf");
		mName="";
	}
	public void initialize(StockGenerics[] generics)
	{
		mGenerics = generics;
		hasBeenInitialized = true;
		invalidate();
	}
	public void initializeAppend(StockGenerics[] generics)
	{
		StockGenerics[] array = new StockGenerics[mGenerics.length+generics.length];
		for(int i=0; i<mGenerics.length; i++)
		{
			array[i] = mGenerics[i];
		}
		for(int i=0; i<generics.length; i++)
		{
			array[mGenerics.length+i] = generics[i];
		}
		mGenerics=array;
		hasBeenInitialized = true;
		invalidate();
	}
	public void removeStock(String ticker)
	{
		ArrayList<StockGenerics> gen = new ArrayList<StockGenerics>(mGenerics.length-1);
		for(int i=0; i<mGenerics.length; i++)
		{
			if(!mGenerics[i].getTicker().toString().equals(ticker))
			{
				gen.add(mGenerics[i]);
			}
		}
		mGenerics = gen.toArray(new StockGenerics[gen.size()]);
		invalidate();
	}
	private double getPriceChangeAverage()
	{
		double total = 0;
		for(int i=0; i<mGenerics.length; i++)
		{
			total+=mGenerics[i].getPercentChange();
		}
		return(total/mGenerics.length);
	}


	public void setName(String name)
	{
		mName=name;
	}
	@Override
	public void onDraw(Canvas canvas) {
			float padding = getHeight()*(0.2f/3f);
			float titleHeight = (getHeight()*0.6f);
			float changeHeight = (getHeight()*0.2f);
			
			//draw name String
			Paint paint = new Paint();
				paint.setTypeface(robotoBold);
				paint.setTextSize(titleHeight);
				paint.setAntiAlias(true);
				
			float nameSize=0f;
			while((nameSize=paint.measureText(mName)) > (9/10.0)*getWidth())
				paint.setTextSize(paint.getTextSize()-5);
			
			canvas.drawText(mName, (getWidth() - nameSize)/2f, padding+titleHeight, paint); //if name is too big, sideMargin may be negative
			
		if(hasBeenInitialized)
		{
			//draw price
				paint.setTypeface(robotoRegular);
				paint.setTextSize(changeHeight);
			double change = getPriceChangeAverage();
			String changeString = Types.trauncateToStringWithZeros(change)+"%";
			float changeSize = paint.measureText(changeString);
			
			if(change > 0)
				paint.setColor(Color.rgb(102, 153, 0));
			else
				paint.setColor(Color.rgb(255, 68, 68));
			
			canvas.drawText(changeString, (getWidth() - changeSize) / 2f, padding+titleHeight+padding+changeHeight, paint);
			
			paint.setColor(Color.BLACK);
			canvas.drawLine(0, getHeight(), getWidth(), getHeight(), paint);
			canvas.drawLine(0, getHeight()-1, getWidth(), getHeight()-1, paint);
			canvas.drawLine(0, getHeight()-2, getWidth(), getHeight()-2, paint);
		
		}
		

	}
	
}
