package widget;

import java.util.Date;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.view.View;
import data.NewsArticle;

public class ArticleView extends View{
		private NewsArticle mArticle;
		private float mDensityMultiplier;
		private int lightGrey;
		private Paint recycledPaint;
		private Typeface titleFont;
		private Typeface dateFont;
		private Date now;
		private float titleTextSize;
		private float dateTextSize;
		private Paint containerPaint;
		
	public ArticleView(Context context, Date now, int width, Paint paint) {
		super(context);
		mDensityMultiplier = getContext().getResources().getDisplayMetrics().density;
		setBackgroundColor(Color.argb(199, 241, 241, 241));
		dateFont = Typeface.createFromAsset(getContext().getAssets(), "fonts/Roboto-Italic.ttf");
		titleFont = Typeface.createFromAsset(getContext().getAssets(), "fonts/Roboto-Bold.ttf");
		this.now = now;
		recycledPaint = paint;
		
		titleTextSize = toPixels(12);
		dateTextSize = toPixels(10);
		lightGrey = Color.rgb(122, 122, 122);
		
		containerPaint = new Paint();
		containerPaint.setAntiAlias(true);
		containerPaint.setColor(Color.argb(200, 3, 10, 55));
		
		

	}
	public NewsArticle getNewsArticle()
	{
		return mArticle;
	}
	private float toPixels(float dip)
	{
		return  (dip * mDensityMultiplier); 
	}
	private Paint getDatePaint()
	{
		Paint paint = recycledPaint;
		paint.reset();
		paint.setTextSize(dateTextSize);
		paint.setTypeface(dateFont);
		paint.setColor(Color.argb(245, 255, 255, 255));
		paint.setAntiAlias(true);
		return paint;
		
	}
	private Paint getTitlePaint()
	{
		Paint paint = recycledPaint;
		paint.reset();
		paint.setAntiAlias(true);
		paint.setTypeface(titleFont);
		paint.setShader(new RadialGradient(0, 0, getWidth(), new int[] { Color.WHITE, lightGrey   }, null, Shader.TileMode.CLAMP));
		paint.setTextSize(titleTextSize);
		
		return paint;
	}

	public void setArticle(NewsArticle a)
	{
		mArticle = a;
	}

	
	@Override
	public void onDraw(Canvas canvas)
	{
		//article better be set by now
		canvas.drawRect(5*mDensityMultiplier, 5*mDensityMultiplier, getWidth()-5*mDensityMultiplier, getHeight()-5*mDensityMultiplier, containerPaint);
		
		canvas.drawText(trimToWidth(mArticle.getTitle()), 5*mDensityMultiplier+10*mDensityMultiplier, (float) (2f*(getHeight()/5f)+(0.5*titleTextSize)), getTitlePaint());
		canvas.drawText(getPubDate(), 5*mDensityMultiplier+10*mDensityMultiplier, (float) (2f*(getHeight()/5f)+(0.5*titleTextSize)+titleTextSize), getDatePaint());

	}
	private String trimToWidth(String s)
	{
		float width = getWidth()-(2*(5*mDensityMultiplier))-12*mDensityMultiplier;
		Paint paint = getTitlePaint();
		float space = paint.measureText(s);
		if(space < width)
		{
			return s;
		}

		float dooot = paint.measureText(" ... ");
		float percent = (1f-(((space-width)/space) + (dooot/space)));
		int passedLetters = (int) (percent*s.length());
		
		int i=passedLetters;
		while(s.charAt(i) != ' ')
		{
			i--;
		}
		String trimmed = (s.substring(0, i)+"...");
		
		return trimmed;
		

	}
	public String getPubDate()
	{
		Date date = mArticle.getPubDate();
		if(date==null || now==null)
			return "";
		
		if(date.after(now))
		{
			return "now";
		}
		if(now.getYear()==date.getYear() && now.getMonth()==date.getMonth() && now.getDate()==date.getDate())
		{
			if(now.getHours()==date.getHours())
				return "now";
			
			int hours = now.getHours()-date.getHours();
			if(hours==0)
				return "now";
			return (hours+(hours > 1 ? " hours ago" : " hour ago"));
		}
		if( ( now.getYear() >= date.getYear() ? now.getMonth()-date.getMonth() : (now.getMonth()+12)-date.getMonth() ) > 2)
		{
			return "old";
		}

		int hours=0;
		
		hours+=(24-date.getHours());
		for(int i=1; i<(date.getMonth()==now.getMonth() ? now.getDate()-date.getDate() : (getNumDays(date.getMonth(), date.getYear()) - date.getDate()) + now.getDate() ); i++)
		{
			hours+=24;
		}
		hours+=now.getHours();
		int days = hours/24;
		if(days==0) days++;
		return days+(days ==1 ? " day ago" : " days ago");
		
	}
	private int getNumDays(int month, int year)
	{
		switch(month)
		{
		case 0 : return 31; 
		case 1 : return (year%4==0 ? 29 : 28);
		case 2 : return 31;
		case 3 : return 30;
		case 4 : return 31;
		case 5 : return 30;
		case 6 : return 31;
		case 7 : return 31;
		case 8 : return 30;
		case 9 : return 31;
		case 10 : return 30;
		case 11 : return 31;
		default : return 30;
		
		
		}
	}

}
