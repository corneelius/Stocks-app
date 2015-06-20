package widget;

import home.HomeScreen;

import java.util.ArrayList;

import stockpage.StockPage;
import watchlist.WatchlistManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import data.Ticker;

public class WatchlistAdder extends LinearLayout{
	private Ticker mTicker;
	private TextView mTextView;
	private WatchlistManager mWatchlistManager;
	private float mDensityMultiplier;
	private ScrollView mScrollView;
	private Typeface robotoBold;
	private StockPage parent;
	
	private ArrayList<ArrayList<String>> watchlists;

	public WatchlistAdder(Context context, Ticker mTicker, StockPage parent) {
		super(context);
		this.mTicker=mTicker;
		mWatchlistManager = new WatchlistManager(getContext());
		watchlists = mWatchlistManager.getWatchlists();
		mTextView = new TextView(getContext());
		mDensityMultiplier = getContext().getResources().getDisplayMetrics().density;
		mScrollView = new ScrollView(getContext());
		robotoBold = Typeface.createFromAsset(getContext().getAssets(), "fonts/Roboto-Bold.ttf");
		this.setOrientation(LinearLayout.VERTICAL);
		this.parent=parent;
		initLayout();
	}
	
	private void initLayout()
	{
		mTextView.setTextSize(10*mDensityMultiplier);
		mTextView.setTextColor(Color.rgb(51, 181, 229));
		mTextView.setTypeface(robotoBold);
		mTextView.setText("  Add "+mTicker.toString()+" to watchlist:");
		this.addView(mTextView);
		
		if(watchlists==null) return;
		
		mScrollView.removeAllViews();
		
		LinearLayout l = new LinearLayout(getContext());
		l.setOrientation(LinearLayout.VERTICAL);
		
		ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, (int) (mDensityMultiplier*40));
		for(int i=0; i<watchlists.size(); i++)
		{
			boolean contained=false;
			for(int x=1; x<watchlists.get(i).size(); x++)
			{
				if(watchlists.get(i).get(x).equals(mTicker.toString()))
				{
					contained=true;
				}
			}
			WatchlistTab tab = new WatchlistTab(getContext(), watchlists.get(i).get(0), contained);
			tab.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					if(((WatchlistTab)v).alreadyContained)
					{
						if(mWatchlistManager.removeStock(((WatchlistTab)v).mName, mTicker.toString()))
						{
							Toast.makeText(getContext(), "Deleted watchlist \""+((WatchlistTab)v).mName+"\"", Toast.LENGTH_SHORT).show();
							LinearLayout l = (LinearLayout)mScrollView.getChildAt(0);
							l.removeView(v);
							
							Intent intent = parent.getIntent().getParcelableExtra("intent");
							String name = intent.getStringExtra("name");
							if(name!=null && name.equals(((WatchlistTab)v).mName))
							{
								parent.changeIntent(new Intent(getContext(), HomeScreen.class));
							}
						}
						else
						{
							((WatchlistTab)v).alreadyContained=false;
							((WatchlistTab)v).invalidate();
						}
					}
					else
					{
						ArrayList<String> a = new ArrayList<String>();
						a.add(mTicker.toString());
						mWatchlistManager.addStocks( ((WatchlistTab)v).mName, a);
						((WatchlistTab)v).alreadyContained=true;
						((WatchlistTab)v).invalidate();
					}
					
				}
			});
			l.addView(tab, params);
		}
		mScrollView.addView(l);
		this.addView(mScrollView);
	}
	
	private class WatchlistTab extends View
	{
		private String mName;
		private boolean alreadyContained=false;
		private int containedBackgroundColor = Color.argb(225, 212, 210, 210);
		private Paint mPaint;
		private Typeface robotoRegular;
		public WatchlistTab(Context context, String name, boolean alreadyContained) {
			super(context);
			this.mName=name;
			this.alreadyContained=alreadyContained;
			robotoRegular = Typeface.createFromAsset(getContext().getAssets(), "fonts/Roboto-Regular.ttf");
			mPaint = getPaint();
		}
		
		@Override
		public void onDraw(Canvas canvas)
		{
			if(alreadyContained)
			{
				canvas.drawColor(containedBackgroundColor);
			}
			else
			{
				canvas.drawColor(Color.argb(225, 255, 255, 255));
			}
			
			if(alreadyContained)
			{
				mPaint.setTypeface(robotoRegular);
				float size = mPaint.measureText("Added  ");
				canvas.drawText("Added  ", getWidth()-size, getHeight()*(2.0f/3), mPaint);
				
				mPaint.setTypeface(robotoBold);
				canvas.drawText(getFittingText(mName, getWidth()-size-10, mPaint), 21*mDensityMultiplier, getHeight()*(2.0f/3), mPaint);
			}
			else
			{
				mPaint.setTypeface(robotoBold);
				canvas.drawText(getFittingText(mName, getWidth()-10*mDensityMultiplier, mPaint), 21*mDensityMultiplier, getHeight()*(2.0f/3), mPaint);
			}
			
			canvas.drawLine(0, getHeight()-1, getWidth(), getHeight()-1, mPaint);
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
		private Paint getPaint()
		{
			Paint paint = new Paint();
			paint.setAntiAlias(true);
			paint.setTextSize(15*mDensityMultiplier);
			return paint;
		}
		
	}
	
	

}
