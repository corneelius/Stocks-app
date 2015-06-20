package stockpage;

import home.HomeScreen;
import home.HomeScreen.HomeScreenActions;

import java.util.ArrayList;

import layout.Carpet;
import nr.app.R;
import telephony.StockPopulator;
import telephony.StockPopulatorOptions;
import telephony.StockPopulatorResultReceiver;
import widget.ChartWidget;
import widget.HeaderWidget;
import widget.NewsWidget;
import widget.StatsWidget;
import widget.WatchlistAdder;
import actionbar.Action;
import actionbar.ActionBar;
import actionbar.ActionReceiver;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;
import data.Chart;
import data.News;
import data.Stats;
import data.Stock;
import data.StockData;
import data.StockGenerics;
import data.Ticker;


public class StockPage extends Activity implements StockPopulatorResultReceiver, ActionReceiver{
	private Stock stock;
	private StockPopulator mPopulator;
	private StatsWidget mStatsWidget;
	private ChartWidget mChartWidget;
	private HeaderWidget mHeaderWidget;
	private ActionBar mActionBar;
	private Resources r;
	private NewsWidget mNewsWidget;
	private RelativeLayout relativeLayout;
	private boolean statsUpdated=false;
	private boolean newsUpdated=false;
	private Intent overrideIntent;
	private boolean chartUpdated=false;
	private boolean stockGenericsUpdated=false;
	private WatchlistAdder mWatchlistAdder;
	private ArrayList<Action> actionbarActions;
	private boolean updatingData=false;
	private Carpet mCarpet;


	public void onCreate(Bundle onSavedInstanceState)
	{
		super.onCreate(onSavedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		requestWindowFeature(Window.FEATURE_ACTION_BAR);
		setContentView(R.layout.stockpage);
		
		mChartWidget = (ChartWidget)findViewById(R.id.stockpage_chartwidget);
		mStatsWidget = (StatsWidget)findViewById(R.id.stockpage_statswidget);
		mNewsWidget = new NewsWidget(this);
		mCarpet = new Carpet(getBaseContext());
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.BELOW, R.id.stockpage_actionbar);
		mCarpet.setLayoutParams(params);
		relativeLayout = (RelativeLayout)findViewById(R.id.stockpage_rlayout);

		
		r = getResources();
		
		mActionBar = (ActionBar)findViewById(R.id.stockpage_actionbar);
		mActionBar.setReturnAction(new Action(HomeScreen.HomeScreenActions.BACK_HOMESCREEN, r.getDrawable(R.drawable.previous_item)));
		mActionBar.setIconOn(true);
		actionbarActions = new ArrayList<Action>();
		actionbarActions.add(new Action(HomeScreenActions.REFRESH2B, r.getDrawable(R.drawable.refresh)));
		actionbarActions.add(new Action(HomeScreenActions.NEWS, r.getDrawable(R.drawable.list)));
		actionbarActions.add(new Action(HomeScreenActions.STOCKPAGE_ADD, r.getDrawable(R.drawable.content_new_hololight)));
		mActionBar.setActions(actionbarActions);
		mActionBar.setProgressBarVisibility(View.VISIBLE);
		mActionBar.setParent(this);
		mActionBar.setProgressBarProgress(1);
		
		mPopulator = new StockPopulator(this, StockPopulatorOptions.FILL_STOCK, 100);
		
		stock = new Stock(new Ticker(getIntent().getCharArrayExtra("ticker")));
		mWatchlistAdder = new WatchlistAdder(this, stock.getTicker(), this);
		
		
		mActionBar.setProgressBarProgress(-1);
		mActionBar.setProgressBarVisibility(View.VISIBLE);
		
		mHeaderWidget = (HeaderWidget)findViewById(R.id.header_widget);

	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		menu.add("test");
		return true;
		
	}
	
	@Override
	public void onDestroy()
	{
		super.onDestroy();
	}

	@Override
	public void onBackPressed()
	{
		super.onBackPressed();
	}

	@Override
	public void onStart()
	{
		super.onStart();
		updateData();
	}
	@Override
	public void onStop()
	{
		super.onStop();
	}
	private void updateData()
	{
		if(!updatingData)
		{
			mActionBar.setProgressBarVisibility(View.VISIBLE);
			mActionBar.setProgressBarProgress(0);
			updatingData=true;
			mPopulator = new StockPopulator(this, StockPopulatorOptions.FILL_STOCK, 100);
			mPopulator.execute(new Stock(stock.getTicker()));
		}
	}
	@Override
	public void onSaveInstanceState(Bundle b)
	{
		super.onSaveInstanceState(b);
		if(chartUpdated && newsUpdated && statsUpdated && stockGenericsUpdated)
		{
			b.putBoolean("chartUpdated", chartUpdated);
			b.putBoolean("newsUpdated", newsUpdated);
			b.putBoolean("statsUpdated", statsUpdated);
			b.putBoolean("stockGenericsUpdated", stockGenericsUpdated);
			b.putBundle("chart", stock.getChart().getBundle());
			b.putBundle("news", stock.getNews().getBundle());
			b.putBundle("stats", stock.getStats().getBundle());
			b.putBundle("stockGenerics", stock.getStockGenerics().getBundle());
			b.putParcelable("ticker", stock.getTicker());
		}
		
	}
	@Override
	public void onRestoreInstanceState(Bundle b)
	{
		super.onRestoreInstanceState(b);
		chartUpdated = b.getBoolean("chartUpdated");
		newsUpdated = b.getBoolean("newsUpdated");
		statsUpdated = b.getBoolean("statsUpdated");
		stockGenericsUpdated = b.getBoolean("stockGenericsUpdated");
		
		if(chartUpdated && newsUpdated && statsUpdated && stockGenericsUpdated)
		{
			stock = new Stock((Ticker)b.getParcelable("ticker"), new StockGenerics(b.getBundle("stockGenerics")), new Stats(b.getBundle("stats")), new Chart(b.getBundle("chart")), new News(b.getBundle("news")));
		}
	}
	@Override
	public void onPause()
	{
		super.onStop();
	}
	@Override
	public void updateChart(Chart[] updatedChart) {
		stock.setChart(updatedChart[0]);
		this.chartUpdated=true;
			mChartWidget.initialize(updatedChart[0]);
		
	}
	@Override
	public void updateNews(News[] updatedNews) {
		stock.setNews(updatedNews[0]);
		this.newsUpdated=true;
		mNewsWidget.initialize(updatedNews[0]);
		
	}
	@Override
	public void onResume()
	{
		super.onResume();
	
	}
	
	@Override
	public void updateStats(Stats[] updatedStats) {
		stock.setStats(updatedStats[0]);
		this.statsUpdated=true;
		mStatsWidget.initialize(updatedStats[0]);
		
	}
	@Override
	public void updateStockGenerics(StockGenerics[] updatedStockGenerics) {
		stock.setStockGenerics(updatedStockGenerics[0]);
		this.stockGenericsUpdated=true;
		mHeaderWidget.initialize(updatedStockGenerics[0]);
		
	}
	@Override
	public void done(StockData[] data, StockPopulatorOptions task) {
		mActionBar.setProgressBarVisibility(View.INVISIBLE);
		updatingData=false;
		stock = (Stock)data[0];
	}
	@Override
	public void updateProgress(int progress) {
		mActionBar.setProgressBarProgress(progress);
		
	}
	@Override
	public void noDataNotify()
	{
		Toast.makeText(this, "No data connection", Toast.LENGTH_SHORT).show();
		onBackPressed();
		
	}
	@Override
	public void updateWatchlists(StockGenerics[] updatedWatchlists) {
		// TODO Auto-generated method stub
		
	}
	private Action getActionWithAction(HomeScreenActions a)
	{
		if(actionbarActions == null)
			return null;
					
		for(int i=0; i<actionbarActions.size(); i++)
		{
			if(actionbarActions.get(i).getAction().equals(a))
				return actionbarActions.get(i);
		}
		return null;
	}
	private boolean carpetOpening;
	private boolean carpetClosing;
	private class CustomOpenAnimationListener implements AnimationListener
	{
		private View content;
		private Runnable finish;
		
		public CustomOpenAnimationListener(View content, Runnable finish)
		{
			this.content=content;
			this.finish = finish;
		}
		@Override
		public void onAnimationStart(Animation animation) {
			carpetOpening=true;
			mActionBar.lock(HomeScreenActions.NEWS);
			mActionBar.lock(HomeScreenActions.STOCKPAGE_ADD);
			
		}
		@Override
		public void onAnimationEnd(Animation animation) {
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			params.addRule(RelativeLayout.BELOW, R.id.stockpage_actionbar);
			params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
			relativeLayout.removeView(mCarpet);
			mCarpet.removeAllViews(); //release views
			mCarpet = new Carpet(StockPage.this);
			mCarpet.setLayoutParams(params);
			mCarpet.setContent(content);
			
			relativeLayout.addView(mCarpet);
			carpetOpening=false;
			if(finish!=null)
				finish.run();
			
			mActionBar.unlockAll();
		}
		@Override
		public void onAnimationRepeat(Animation animation) {
			// TODO Auto-generated method stub
			
		}
	}

	AnimationListener closeAnimListener = new AnimationListener()
	{
		@Override
		public void onAnimationStart(Animation animation) {
			carpetClosing=true;
			mActionBar.lock(HomeScreenActions.NEWS);
			mActionBar.lock(HomeScreenActions.STOCKPAGE_ADD);
			
		}
		@Override
		public void onAnimationEnd(Animation animation) {
			relativeLayout.removeView(mCarpet);
			carpetClosing=false;
			mActionBar.unlockAll();
		}
		@Override
		public void onAnimationRepeat(Animation animation) {
			// TODO Auto-generated method stub
			
		}
	};
	
	private int findIndexOfAction(HomeScreen.HomeScreenActions action)
	{
		for(int i=0; i<actionbarActions.size(); i++)
		{
			if(actionbarActions.get(i).getAction()==action)
				return i+1;
		}
		return -1;
	}
	private void closeCarpet(HomeScreen.HomeScreenActions me)
	{
		HomeScreen.HomeScreenActions a = otherActionActivated(me);
		if(a != null)
		{
			getActionWithAction(a).setActivate(false);
			relativeLayout.removeView(mCarpet);
			mCarpet.removeAllViews();
			mActionBar.setFlip(findIndexOfAction(a), false);
			
		}
	}
	
	private HomeScreen.HomeScreenActions otherActionActivated(HomeScreen.HomeScreenActions me)
	{
		if(me != HomeScreenActions.NEWS && getActionWithAction(HomeScreenActions.NEWS).getActivate())
		{
			return HomeScreen.HomeScreenActions.NEWS;
		}
		if(me != HomeScreenActions.STOCKPAGE_ADD && getActionWithAction(HomeScreenActions.STOCKPAGE_ADD).getActivate())
		{
			return HomeScreen.HomeScreenActions.STOCKPAGE_ADD;
		}

		return null;
	}
	
	private Intent getIntent2()
	{
		if(overrideIntent != null)
			return overrideIntent;
		return super.getIntent().getParcelableExtra("intent");
	}
	public void changeIntent(Intent intent)
	{
		overrideIntent=intent;
	}
	
	@Override
	public void act(HomeScreenActions action) {
		switch(action){
		case REFRESH2B:
		{
			updateData();
		}break;
		case BACK_HOMESCREEN:
		{
			//onBackPressed();
			startActivity(getIntent2());
		}break;
		case STOCKPAGE_ADD:
		{
			if(getActionWithAction(HomeScreenActions.STOCKPAGE_ADD).getActivate())
			{
				(mCarpet.close()).setAnimationListener(closeAnimListener);
				
				if(carpetOpening)
				{
					carpetOpening=false;
				}
				getActionWithAction(HomeScreenActions.STOCKPAGE_ADD).setActivate(false);
			}
			else
			{
				closeCarpet(HomeScreen.HomeScreenActions.STOCKPAGE_ADD);
				RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, 1);
				params.addRule(RelativeLayout.BELOW, R.id.stockpage_actionbar);
				mCarpet.setLayoutParams(params);
				
				if(mCarpet.getParent()!=null)
					((ViewGroup) mCarpet.getParent()).removeView(mCarpet); 
				
				relativeLayout.addView(mCarpet);
			
				(mCarpet.open()).setAnimationListener(new CustomOpenAnimationListener(mWatchlistAdder, null));
				
				if(carpetClosing)
				{
					carpetClosing=false;
				}
				getActionWithAction(HomeScreenActions.STOCKPAGE_ADD).setActivate(true);
			}
		}break;
		case NEWS:
		{
			if(getActionWithAction(HomeScreenActions.NEWS).getActivate())
			{
				(mCarpet.close()).setAnimationListener(closeAnimListener);
				
				if(carpetOpening)
				{
					carpetOpening=false;
				}
				getActionWithAction(HomeScreenActions.NEWS).setActivate(false);
			}
			else
			{
				closeCarpet(HomeScreen.HomeScreenActions.NEWS);
				RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, 1);
				params.addRule(RelativeLayout.BELOW, R.id.stockpage_actionbar);
				mCarpet.setLayoutParams(params);
				
				if(mCarpet.getParent()!=null)
					((ViewGroup) mCarpet.getParent()).removeView(mCarpet); 
				
				relativeLayout.addView(mCarpet);
			
				(mCarpet.open()).setAnimationListener(new CustomOpenAnimationListener(mNewsWidget, null));
				
				if(carpetClosing)
				{
					carpetClosing=false;
				}
				getActionWithAction(HomeScreenActions.NEWS).setActivate(true);
			}
		}
		
		}
		
	}
	

}
