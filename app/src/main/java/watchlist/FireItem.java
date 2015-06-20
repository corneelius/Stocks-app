package watchlist;

import graphics.WDrawable;
import stockpage.StockPage;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.view.MotionEvent;
import android.view.View;
import data.StockGenerics;

public class FireItem extends View {
	private StockGenerics stockGenerics;
	private Modes mMode=Modes.NORMAL;
	private FireScreen mParent;
	private Typeface robotoBold;
	private Typeface robotoRegular;
	private ColorModes mColorMode = ColorModes.LIGHT;
	private int red = Color.rgb(255, 68, 68);
	private int green = Color.rgb(102, 153, 0);
	private boolean isSelected=false;
	private float mDensityMultiplier;
	private String mName;
	private Paint recycledPaint = new Paint();
	
	public enum Modes {DELETE, NORMAL};
	public enum ColorModes {LIGHT, DARK};
	
	public FireItem(Context context, StockGenerics stockGenerics, FireScreen parent, ColorModes mode, String name) {
		super(context); 
		this.stockGenerics = stockGenerics;
		mDensityMultiplier = getContext().getResources().getDisplayMetrics().density;
		setBackgroundColor(Color.WHITE);
		mParent=parent;
		this.mName=name;
		mColorMode = mode;
		robotoBold = Typeface.createFromAsset(getContext().getAssets(), "fonts/Roboto-Bold.ttf");
		robotoRegular = Typeface.createFromAsset(getContext().getAssets(), "fonts/Roboto-Regular.ttf");
	}
	public void setSelected(boolean s)
	{
		isSelected=s;
		invalidate();
	}
	public boolean getSelected()
	{
		return isSelected;
	}
	public void setColorMode(ColorModes mode)
	{
		mColorMode = mode;
		invalidate();
	}
	public ColorModes getColorMode()
	{
		return mColorMode;
	}
	public StockGenerics getStockGenerics()
	{
		return stockGenerics;
	}
	public void setStockGenerics(StockGenerics generics)
	{
		this.stockGenerics = generics;
		invalidate();
	}
	public void setMode(Modes mode)
	{
		mMode=mode;
		invalidate();
	}
	public Modes getMode()
	{
		return mMode;
	}

	@Override
	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec)); 
	}

	public boolean onTouchEvent(MotionEvent e)
	{
		
		if(e.getAction() == MotionEvent.ACTION_UP && mMode==Modes.DELETE && e.getRawX()<0.1f*getWidth())
		{
			mParent.removeStock(stockGenerics.getTicker().toString());
		}
		else if(e.getAction() == MotionEvent.ACTION_UP)
		{
			if(isSelected)
			{
				Intent intent = new Intent(getContext(), StockPage.class);
				intent.putExtra("ticker", stockGenerics.getTicker().toString().toCharArray());
					Intent returnIntent = new Intent(getContext(), FireScreen.class);
					returnIntent.putExtra("name", mName);
					intent.putExtra("intent", returnIntent);
				getContext().startActivity(intent);
			}
			else
			{
				isSelected=true;
				mParent.changeChart(stockGenerics.getTicker().toString());
				mParent.refreshBut(stockGenerics.getTicker().toString());
				invalidate();
			}
		}
		return true;
	}

	@Override
	public void onDraw(Canvas canvas)
	{
		if(isSelected)
			canvas.drawColor(Color.rgb(55,  74,  103));
		else if(mColorMode==ColorModes.LIGHT)
			canvas.drawColor(Color.rgb(169, 178, 199));
		else if(mColorMode == ColorModes.DARK)
			canvas.drawColor(Color.rgb(136, 159, 194));

		
		
		float drawableWidth = 0.1f*getWidth();
		float symbolWidth = 0.3f*getWidth();
		float priceWidth = 0.3f*getWidth();
		float changeWidth = 0.3f*getWidth();
		float textSize = getHeight()*0.5f;
		
		if(mMode==Modes.DELETE)
		{
			WDrawable d = new WDrawable(Color.argb(255, 255, 68, 68));
			d.setBounds(0, (int) ((getHeight()-(0.5*getHeight()))/2), (int) drawableWidth, (int) (getHeight()-((int) ((getHeight()-(0.5*getHeight()))/2))));
			d.draw(canvas);
		}
		
		Paint textPaint = getSymbolPaint(textSize);		
		
		//price
		double price = stockGenerics.getLastTrade();
		canvas.drawText(""+price, (drawableWidth+symbolWidth+priceWidth)-textPaint.measureText(""+price)-5, ((getHeight()-textSize)/2)+textSize, textPaint);

		//title
		canvas.drawText(getFittingText(stockGenerics.getTicker().toString(), symbolWidth+priceWidth-textPaint.measureText(price+"..."), textPaint), drawableWidth+5, ((getHeight()-textSize)/2)+textSize, textPaint);
		
		//% change
		textPaint.setTypeface(robotoRegular);
		
		double change = stockGenerics.getPercentChange();
		if(change > 0)
		{
			textPaint.setColor(green);
		}
		else
		{
			textPaint.setColor(red);
		}
				
		canvas.drawRoundRect(new RectF(drawableWidth+symbolWidth+priceWidth, (10+mDensityMultiplier), (drawableWidth+symbolWidth+priceWidth+changeWidth-10), getHeight()-(10+mDensityMultiplier)), 10f, 10f, textPaint);
		
		textPaint.setColor(Color.WHITE);
		String change2 = ""+change;
		int endIndex = change2.indexOf('.')+3;
		
		if(!(change2.indexOf('.')==-1))
		{
			while(endIndex>change2.length())
				endIndex--;
			change2 = change2.substring(0, endIndex);
		}
		canvas.drawText(change2+"%", (drawableWidth+symbolWidth+priceWidth)+((changeWidth-20-(textPaint.measureText(change2+"%")))/2), ((getHeight()-textSize)/2)+textSize, textPaint);
		
		
		
		
		Paint paint2 = recycledPaint;
		paint2.reset();
		paint2.setColor(Color.argb(50, 37, 33, 32));
		canvas.drawLine(0, getHeight()-1, getWidth(), getHeight()-1, paint2);
		
		
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
	private Paint getSymbolPaint(float textSize)
	{
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setColor(Color.WHITE);
		paint.setTextSize(textSize);
		paint.setTypeface(robotoBold);
		return paint;
	}
}
