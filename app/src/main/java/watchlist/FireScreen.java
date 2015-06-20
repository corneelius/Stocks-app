package watchlist;


import graphics.DrawableView;
import home.HomeScreen;
import home.HomeScreen.HomeScreenActions;

import java.util.ArrayList;

import nr.app.R;
import telephony.StockPopulator;
import telephony.StockPopulatorOptions;
import telephony.StockPopulatorResultReceiver;
import watchlist.FireItem.ColorModes;
import widget.ChartWidget;
import actionbar.Action;
import actionbar.ActionBar;
import actionbar.ActionReceiver;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Toast;
import data.Chart;
import data.News;
import data.Stats;
import data.StockData;
import data.StockGenerics;
import data.Ticker;
import data.Types;

//FireLists are stored in SharedPreferences
//one preference called firelists holds the names of all the WatchLists

public class FireScreen extends Activity implements StockPopulatorResultReceiver, ActionReceiver{
	private ArrayList<StockGenerics> emptyStocks;
	private ArrayList<StockGenerics> filledStocks;
	private ActionBar mActionBar;
	private Resources r;
	private LayoutInflater mLayoutInflater;
	private String name;
	private ArrayList<String> mTickers;
	private WatchlistManager mWatchlistManager;
	private InputMethodManager mInputMethodManager;
	private ScrollView mScrollView;
	private boolean nextUpdateIsRefresh=false;
	private Title mTitle;
	private ArrayList<Action> actionbarActions;
	private ProgressBar chartProgressBar;
	private ChartWidget mChartWidget;
	private boolean updatingChart=false;
	StockPopulator chartPop;
	private float mDensityMultiplier;
	private boolean selectionChanged=false;
	private boolean deleteOn=false;
	private boolean appendStocksOnNextUpdate;
	private DrawableView mDrawableView;
	private boolean hasBeenUpdated=false;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.firelist_layout);

		mLayoutInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mInputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		mWatchlistManager = new WatchlistManager(this);
		chartProgressBar = (ProgressBar)findViewById(R.id.firelist_chart_progressbar);
		mChartWidget = (ChartWidget)findViewById(R.id.firelist_chart);
		mDrawableView = (DrawableView)findViewById(R.id.firelist_drawableview);
		mDensityMultiplier = getResources().getDisplayMetrics().density;


		Drawable d = new ColorDrawable(Color.BLACK);
		mDrawableView.setDrawable(d);
		
		r = getResources();
		mActionBar = (ActionBar)findViewById(R.id.firelist_actionbar);
		mActionBar.setParent(this);
		mActionBar.setIconOn(true);
		actionbarActions = new ArrayList<Action>();
		actionbarActions.add(new Action(HomeScreen.HomeScreenActions.FIRE_ADD2B, r.getDrawable(R.drawable.content_new_hololight)));
		actionbarActions.add(new Action(HomeScreen.HomeScreenActions.FIRE_DELETE, r.getDrawable(R.drawable.content_discard_hololight)));
		actionbarActions.add(new Action(HomeScreen.HomeScreenActions.FIRE_REFRESH2B, r.getDrawable(R.drawable.refresh)));
		mActionBar.setActions(actionbarActions);
		mActionBar.setReturnAction(new Action(HomeScreenActions.BACK_HOMESCREEN, r.getDrawable(R.drawable.previous_item)));

		name = getIntent().getStringExtra("name");
		mTitle = (Title)findViewById(R.id.firelist_title);
		mScrollView = (ScrollView)findViewById(R.id.firelist_contents);
		emptyStocks = new ArrayList<StockGenerics>();
		filledStocks = new ArrayList<StockGenerics>();


		mTickers = mWatchlistManager.getWatchlist(name);
		mTickers.remove(0);
		mTitle.setName(name);
		for(int i=0; i<mTickers.size(); i++)
		{
			emptyStocks.add(new StockGenerics(new Ticker(mTickers.get(i))));
		}

	}

	private void generateLayoutFromFilledStocks()
	{
		LinearLayout.LayoutParams params = getFireItemParams();
		if(appendStocksOnNextUpdate)
		{
			mTitle.initializeAppend(filledStocks.toArray(new StockGenerics[filledStocks.size()]));
			LinearLayout l = (LinearLayout)mScrollView.getChildAt(0);
			int childCount = l.getChildCount();
			ColorModes first = ((FireItem)l.getChildAt(0)).getColorMode();
			ColorModes mode;
			if(l!=null)
			{
				for(int i=0; i<filledStocks.size(); i++)
				{
					if(first==ColorModes.DARK)
					{
						mode = ((childCount+i+1)%2.0==0 ? ColorModes.LIGHT : ColorModes.DARK);
					}
					else
					{
						mode = ((childCount+i+1)%2.0==0 ? ColorModes.DARK : ColorModes.LIGHT);
					}
					FireItem item = new FireItem(this, filledStocks.get(i), this, mode, name);
					item.setLayoutParams(params);
					if(deleteOn) item.setMode(FireItem.Modes.DELETE);
					if(l.getChildCount()+i==0 && !selectionChanged)
						item.setSelected(true);
					l.addView(item);
				}
			}
		}
		else if(nextUpdateIsRefresh)
		{
			mTitle.initialize(filledStocks.toArray(new StockGenerics[filledStocks.size()]));
			LinearLayout l = (LinearLayout)mScrollView.getChildAt(0);
			int filledStocksOffset=0;
			for(int i=0; i<l.getChildCount(); i++)
			{
				FireItem item = (FireItem)l.getChildAt(i);
				if(item.getStockGenerics().getTicker().toString().equals(filledStocks.get(i+filledStocksOffset).getTicker().toString()))
				{
					item.setStockGenerics(filledStocks.get(i+filledStocksOffset));
				}
				else
				{
					filledStocksOffset++;
					i--;
				}
			}
		}
		else
		{
			mTitle.initialize(filledStocks.toArray(new StockGenerics[filledStocks.size()]));
			LinearLayout l = new LinearLayout(this);
			l.setOrientation(LinearLayout.VERTICAL);
			for(int i=0; i<filledStocks.size(); i++)
			{
				FireItem item = new FireItem(this, filledStocks.get(i), this, ((i+1)%2.0==0 ? ColorModes.DARK : ColorModes.LIGHT) , name);
				item.setLayoutParams(params);
				if(deleteOn) item.setMode(FireItem.Modes.DELETE);
				if(i==0 && !selectionChanged)
					item.setSelected(true);
				l.addView(item);
			}
			mScrollView.removeAllViews();
			mScrollView.addView(l);
		}
	}
	public void refreshBut(String ticker)
	{
		selectionChanged=true;
		LinearLayout l=null;
		try{ l = (LinearLayout)mScrollView.getChildAt(0); }
		catch(ClassCastException e) { return; }
		if(l==null) return;
		
		for(int i=0; i<l.getChildCount(); i++)
		{
			FireItem item =(FireItem) l.getChildAt(i);
			if(!item.getStockGenerics().getTicker().toString().equals(ticker))
				item.setSelected(false);
		}
	}
	private LinearLayout.LayoutParams getFireItemParams()
	{
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, (int) (45*mDensityMultiplier));
		return params;
	}
	private void generateLayoutFromEmptyStocks()
	{
		LinearLayout l = new LinearLayout(this);
		l.setOrientation(LinearLayout.VERTICAL);
		LinearLayout.LayoutParams params = getFireItemParams();
		for(int i=0; i<emptyStocks.size(); i++)
		{
			FireItem item = new FireItem(this, emptyStocks.get(i), this, ((i+1)%2.0==0 ? ColorModes.DARK : ColorModes.LIGHT) , name);
			item.setLayoutParams(params);
			if(deleteOn) item.setMode(FireItem.Modes.DELETE);
			if(i==0 && !selectionChanged)
				item.setSelected(true);
			l.addView(item);
		}
		mScrollView.removeAllViews();
		mScrollView.addView(l);
	}
	public void changeChart(String ticker)
	{
		fetchChart(new Chart(new Ticker(ticker)));
	}
	public void removeStock(String ticker)
	{
		FireItem item = getFireItemWithTicker(ticker);
		boolean itemSelected = item.getSelected();
		
		mWatchlistManager.removeStock(name, ticker);
		mTickers = mWatchlistManager.getWatchlist(name);
		if(mTickers==null)
		{
			Toast.makeText(this, name+" deleted", Toast.LENGTH_SHORT).show();
			startActivity((Intent) getIntent().getParcelableExtra("intent"));
			return;
		}
		LinearLayout l = (LinearLayout)mScrollView.getChildAt(0);
		for(int i=0; i<l.getChildCount(); i++)
		{
			if( ((FireItem)l.getChildAt(i)).getStockGenerics().getTicker().toString().equals(ticker) )
				l.removeViewAt(i);
		}
		mTitle.removeStock(ticker);
		
		if(itemSelected)
		{
			fetchChart(new Chart(((FireItem)l.getChildAt(0)).getStockGenerics().getTicker()));
			((FireItem)l.getChildAt(0)).setSelected(true);
		}
		cleanLayout();
	}
	
	private void cleanLayout()
	{
		LinearLayout l = (LinearLayout)mScrollView.getChildAt(0);
		ColorModes first=null;
		for(int i=0; i<l.getChildCount(); i++)
		{
			FireItem item = (FireItem)l.getChildAt(i);
			if(i==0)
			{
				first = item.getColorMode();
				continue;
			}
			if(i%2==0)
			{
				item.setColorMode(first == ColorModes.LIGHT ? ColorModes.LIGHT : ColorModes.DARK);
			}
			else
			{
				item.setColorMode(first == ColorModes.LIGHT ? ColorModes.DARK : ColorModes.LIGHT);
			}
		}
	}
	private FireItem getFireItemWithTicker(String ticker)
	{
		LinearLayout l=null;
		try{ l = (LinearLayout)mScrollView.getChildAt(0); }
		catch(ClassCastException e) { return null; }
		if(l==null) return null;
		
		for(int i=0; i<l.getChildCount(); i++)
		{
			FireItem item = (FireItem) l.getChildAt(i);
			if(item.getStockGenerics().getTicker().toString().equals(ticker))
				return item;
		}
		return null;
		
	}
	@Override
	public void onStart()
	{
		super.onStart();
		selectionChanged=false;
		
		mTickers = mWatchlistManager.getWatchlist(name);
		mTickers.remove(0);
		emptyStocks.clear();
		for(int i=0; i<mTickers.size(); i++)
		{
			emptyStocks.add(new StockGenerics(new Ticker(mTickers.get(i))));
		}
		
		if(!hasBeenUpdated)
			generateLayoutFromEmptyStocks();
		
		fetchData();
		fetchChart(new Chart(emptyStocks.get(0).getTicker()));
	}
	private boolean fetchingData=false;
	private void fetchData()
	{
		if(!fetchingData)
		{
			fetchingData=true;
			mActionBar.setProgressBarVisibility(View.VISIBLE);


			StockPopulator mPop = new StockPopulator(this, StockPopulatorOptions.FILL_GENERICS, 100);
			mPop.execute(emptyStocks.toArray(new StockGenerics[emptyStocks.size()]));
		}
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
	@Override
	public void onStop()
	{
		super.onStop();
	}

	private void fetchChart(Chart stock)
	{
		if(updatingChart)
		{
			chartPop.cancel(true);
		}
		updatingChart=true;
		mChartWidget.turnColor(Color.WHITE);
		chartProgressBar.setVisibility(View.VISIBLE);
		chartProgressBar.bringToFront();
		chartPop = new StockPopulator(this, StockPopulatorOptions.FILL_CHART, -1);
		chartPop.execute(new Chart[] {stock});
	}


	
	@Override
	public void updateChart(Chart[] updatedChart) {
		
	}

	@Override
	public void updateNews(News[] updatedNews) {

	}

	@Override
	public void updateStats(Stats[] updatedStats) {

	}

	@Override
	public void updateStockGenerics(StockGenerics[] updatedStockGenerics) {
			filledStocks.clear();
		for(StockGenerics g : updatedStockGenerics)
		{
			filledStocks.add(g);
		}
		generateLayoutFromFilledStocks();
		fetchingData=false;
		mActionBar.setProgressBarVisibility(View.INVISIBLE);
		mActionBar.setProgressBarProgress(0);
		appendStocksOnNextUpdate=false;
		nextUpdateIsRefresh=false;
		hasBeenUpdated=true;
	}

	@Override
	public void onBackPressed()
	{
		super.onBackPressed();
	}
	
	@Override
	public void done(StockData[] data, StockPopulatorOptions task) {
		if(data[0] instanceof Chart)
		{
			chartProgressBar.setVisibility(View.GONE);
			mChartWidget.setVisibility(View.VISIBLE);
			mChartWidget.initialize((Chart)data[0]);
			mChartWidget.bringToFront();
			updatingChart=false;
		}

	}
	@Override
	public void updateProgress(int progress) {
		mActionBar.setProgressBarProgress(progress);

	}
	@Override
	public void updateWatchlists(StockGenerics[] updatedWatchlists) {
		// TODO Auto-generated method stub

	}
	private ArrayList<String> getAddedStocks(String s)
	{
		String temp="";
		ArrayList<String> stocks = new ArrayList<String>();
		for(int i=0; i<s.length(); i++)
		{
			if(s.charAt(i) == ' ')
			{
				//
			}
			else if(s.charAt(i) == ',')
			{
				stocks.add(temp);
				temp="";
			}
			else
			{
				temp+=Character.toUpperCase(s.charAt(i));
			}
		}
		if(temp.length()>0)
			stocks.add(temp);

		return stocks;
	}
	private void checkListForDups(ArrayList<String> tickers)
	{
		Types.checkStringArrayListForDups(tickers, 0);
		ArrayList<String> existing = mWatchlistManager.getWatchlist(name);
		outer: for(int i=0; i<tickers.size(); i++)
		{
			for(int x=0; x<existing.size(); x++)
			{
				if(tickers.get(i).equals(existing.get(x)))
				{
					tickers.remove(i);
					i--;
					continue outer;
				}
			}
		}
	}
	@Override
	public void act(HomeScreenActions action) {
		switch(action)
		{
		case FIRE_REFRESH2B:
		{
			mTickers = mWatchlistManager.getWatchlist(name);
			mTickers.remove(0);
			nextUpdateIsRefresh=true;
			emptyStocks.clear();
			for(int i=0; i<mTickers.size(); i++)
			{
				emptyStocks.add(new StockGenerics(new Ticker(mTickers.get(i))));
			}
			fetchData();
		}break;
		case FIRE_DELETE:
		{
			if(getActionWithAction(HomeScreenActions.FIRE_DELETE).getActivate())
			{
				getActionWithAction(HomeScreenActions.FIRE_DELETE).setActivate(false);
				deleteOn=false;
			}
			else
			{
				deleteOn=true;
				getActionWithAction(HomeScreenActions.FIRE_DELETE).setActivate(true);
			}
				{
					LinearLayout l=null;
					try{ l = (LinearLayout)mScrollView.getChildAt(0); }
					catch(ClassCastException e) { break; }
					if(l==null) break;
					
					for(int i=0; i<l.getChildCount(); i++)
					{
						FireItem item = (FireItem)l.getChildAt(i);
						if(item.getMode()==FireItem.Modes.DELETE)
						{
							item.setMode(FireItem.Modes.NORMAL);
						}
						else
						{
							item.setMode(FireItem.Modes.DELETE);
						}
					}
				}
		}break;
		case BACK_HOMESCREEN:
		{
			startActivity(new Intent(this, HomeScreen.class));
		}break;
		case FIRE_ADD2B:
		{
			AlertDialog.Builder builder = new AlertDialog.Builder(this);

			builder.setView(mLayoutInflater.inflate(R.layout.backlayout_editwatchlist, null));
			builder.setTitle("Add stocks to "+name+":");	
			builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					mActionBar.turnActionOff(HomeScreenActions.FIRE_ADD2B);
					EditText symbols = (EditText)((Dialog)dialog).findViewById(R.id.backlayout_editwatchlist_newsymbols);
					String s = symbols.getEditableText().toString();
					ArrayList<String> tickers = getAddedStocks(s);
					checkListForDups(tickers);
					mWatchlistManager.addStocks(name, tickers);

					mInputMethodManager.hideSoftInputFromWindow(symbols.getWindowToken(), 0);

					dialog.dismiss();
							
					appendStocksOnNextUpdate=true;
					emptyStocks.clear();
					if(tickers.size()==0) return;
					for(int i=0; i<tickers.size(); i++)
					{
						emptyStocks.add(new StockGenerics(new Ticker(tickers.get(i))));
					}
					mTickers = mWatchlistManager.getWatchlist(name);

					fetchData();
				}
			});
			builder.setNegativeButton("Close", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					EditText symbols = (EditText)((Dialog)dialog).findViewById(R.id.backlayout_editwatchlist_newsymbols);

					mInputMethodManager.hideSoftInputFromWindow(symbols.getWindowToken(), 0);

					dialog.dismiss();


				}
			});

			AlertDialog addStocks = builder.create();
			addStocks.setCanceledOnTouchOutside(false);
			addStocks.show();

			//customizations
			final Button positive = addStocks.getButton(Dialog.BUTTON_POSITIVE);
			positive.setEnabled(false);

			EditText name = (EditText)addStocks.findViewById(R.id.backlayout_editwatchlist_newsymbols);
			name.addTextChangedListener(new TextWatcher()
			{
				@Override
				public void beforeTextChanged(CharSequence s, int start, int count,
						int after) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onTextChanged(CharSequence s, int start, int before,
						int count) {
					// TODO Auto-generated method stub

				}

				@Override
				public void afterTextChanged(Editable s) {
					if(!s.toString().equals(""))
					{
						positive.setEnabled(true);
					}
					else
					{
						positive.setEnabled(false);
					}

				} 	
			});

		}break;
		}


	}
	@Override
	public void noDataNotify() {
		Toast.makeText(this, "No data connection", Toast.LENGTH_SHORT).show();
		onBackPressed();
		
	}

}
