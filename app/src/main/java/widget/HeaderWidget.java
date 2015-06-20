package widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;
import data.StockGenerics;
import data.Types;

public class HeaderWidget extends View{
	private StockGenerics mGenerics;
	private boolean hasBeenInitialized;
	private Typeface robotoBold;
	private Typeface robotoRegular;
	private Bitmap mBitmap;
	private Paint recycledPaint;
	private int red = Color.rgb(255, 68, 68);
	private int green = Color.rgb(102, 153, 0);
	

	public HeaderWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
		hasBeenInitialized = false;
		setBackgroundColor(Color.WHITE);
		robotoBold = Typeface.createFromAsset(getContext().getAssets(), "fonts/Roboto-Bold.ttf");
		robotoRegular = Typeface.createFromAsset(getContext().getAssets(), "fonts/Roboto-Regular.ttf");
		recycledPaint = new Paint();
		setBackgroundColor(Color.WHITE);
	}
	public void onMeasure(int width, int height)
	{
		setMeasuredDimension(MeasureSpec.getSize(width), MeasureSpec.getSize(height));
	}
	public void initialize(StockGenerics generics)
	{
		mGenerics = generics;
		hasBeenInitialized = true;
		invalidate();
	}
	public void onDraw(Canvas canvas)
	{
		
		//display StockGenerics
		if(hasBeenInitialized)
		{
			if(mBitmap==null)
				initCanvas();
			canvas.drawBitmap(mBitmap, 0,  0, recycledPaint);
			
		}
	}
	private void initCanvas()
	{
		Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		
		float padding = bitmap.getHeight()/20;
		float titleHeight = (bitmap.getHeight()*0.3f);
		float priceHeight = (bitmap.getHeight()*0.35f);
		float changeHeight = (bitmap.getHeight()*0.15f);
		float bidaskHeight = (bitmap.getHeight()*0.15f);
		
		//draw name String
		Paint paint = new Paint();
			paint.setTypeface(robotoBold);
			paint.setTextSize(titleHeight);
			paint.setAntiAlias(true);
		String name = mGenerics.getName();
		float nameSize = paint.measureText(name);
		canvas.drawText(name, (getWidth() - nameSize) / 2f, padding+titleHeight, paint); //if name is too big, sideMargin may be negative
		
		//draw price
			paint.setTypeface(robotoRegular);
			paint.setTextSize(priceHeight);
		String priceString = Types.trauncateToStringWithZeros(mGenerics.getLastTrade());
		float priceSize = paint.measureText(priceString);
		//wait to draw
		
		
		//draw bid, ask
			paint.setTypeface(robotoRegular);
			paint.setTextSize(bidaskHeight);
		String bid = "BID " + Types.trauncateToStringWithZeros(mGenerics.getBid());
		String ask = "ASK " + Types.trauncateToStringWithZeros(mGenerics.getAsk());
		
		float bidaskSize = paint.measureText((bid.length() > ask.length() ? bid : ask));
		
		
		//change paint back to draw price
		paint.setTypeface(robotoRegular);
		paint.setTextSize(priceHeight);
		
		canvas.drawText(priceString, (getWidth()-(priceSize+bidaskSize+5) )/2f, padding+titleHeight+padding+priceHeight, paint);
		
		//change paint back to draw bidask
		paint.setTypeface(robotoRegular);
		paint.setTextSize(bidaskHeight);
		canvas.drawText(bid, ((getWidth()-(priceSize+bidaskSize+5) )/2f)+priceSize+5, padding+titleHeight+padding+padding+bidaskHeight, paint);
		canvas.drawText(ask, ((getWidth()-(priceSize+bidaskSize+5) )/2f)+priceSize+5, padding+titleHeight+padding+bidaskHeight+padding+bidaskHeight, paint);
				


				
		//draw nominal change
		String nominalChange;
		if(mGenerics.getNominalChange() > 0)
		{
			nominalChange = (String) ("+"+Types.trauncateToStringWithZeros(Types.toPositive(mGenerics.getNominalChange()))+"" );
			paint.setColor(green);
		}
		else
		{
			nominalChange = (String) ("-"+Types.trauncateToStringWithZeros(Types.toPositive(mGenerics.getNominalChange()))+"" );
			paint.setColor(red);
		}
		
		String percentChange = ""+Types.trauncateToStringWithZeros(Types.toPositive(mGenerics.getPercentChange()))+'%'; //no negative sign for percent
		
		

		//draw changes
		paint.setTypeface(robotoRegular);
		paint.setTextSize(changeHeight);
		String changeString = nominalChange + "  " + percentChange;
		float changeSize = paint.measureText(changeString); 
		float changeSideMargin = (getWidth() - changeSize) / 2f;
		canvas.drawText(changeString, changeSideMargin, padding+titleHeight+padding+bidaskHeight+padding+bidaskHeight+padding+changeHeight, paint);
		
		paint.setColor(Color.rgb(255, 136, 0));
		canvas.drawLine(0, getHeight(), getWidth(), getHeight(), paint);
		canvas.drawLine(0, getHeight()-1, getWidth(), getHeight()-1, paint);
		canvas.drawLine(0, getHeight()-2, getWidth(), getHeight()-2, paint);
		mBitmap = bitmap;
		
		
	}

	
}
