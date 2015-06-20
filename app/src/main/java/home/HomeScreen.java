package home;

import graphics.ShadowView;

import java.util.ArrayList;

import layout.Carpet;
import nr.app.R;
import stockpage.StockPage;
import telephony.IndexPopulator;
import telephony.IndexPopulatorResultReceiver;
import telephony.StockListDownloader;
import telephony.StockPopulator;
import telephony.StockPopulatorOptions;
import telephony.StockPopulatorResultReceiver;
import watchlist.WatchlistItem;
import watchlist.WatchlistManager;
import widget.IndexView;
import widget.NewsWidget;
import widget.SymbolSuggestor;
import actionbar.Action;
import actionbar.ActionBar;
import actionbar.ActionReceiver;
import actionbar.ButtonBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.BounceInterpolator;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsoluteLayout;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import data.Chart;
import data.Index;
import data.News;
import data.Stats;
import data.StockData;
import data.StockGenerics;
import data.Ticker;
import database.OpenHelper;



public class HomeScreen extends Activity implements StockPopulatorResultReceiver, IndexPopulatorResultReceiver, 
CompletionNotificatee, DialogInterface.OnClickListener, ActionReceiver{
	
	private Handler mHandler;
	private ActionBar mActionBar;
	private SymbolSuggestor mSymbolSuggestor;
	private Carpet mCarpet;
	private boolean updatingData;
	private News mNews;
	private NewsWidget mNewsWidget;
	private IndexView mIndexView;
	private long VIBRATION_DURATION=10;
	private int INDEX_VIEW_SHADOW_WIDTH=10;
	private StockPopulator mPop;
	private Vibrator mVibrator;
	private EditText mEditText;
	private Bundle bundledIndex;
	private Resources r;
	private IndexPopulator iPop;
	private SharedPreferences sPrefs;
	private final String STOCK_DATABASE_NAME = "stock_database";
	public static SQLiteDatabase stocklistDatabaseStatic;
	private DialogMode dMode;
	private boolean noInternetConnection=false;
	private ProgressDialog progressDialog;
	private StockListDownloader stockListDownloader;
	private ArrayList<ArrayList<String>> watchlists;
	private WatchlistManager mWatchlistManager;
	private TextView backLayoutText;
	private ProgressBar backLayoutProgressBar;
	private LayoutInflater mLayoutInflater;
	
	private int indexLayoutMinWidth;
	private RelativeLayout indexLayout;
	private RelativeLayout backLayout;
	private AbsoluteLayout container;
	AbsoluteLayout.LayoutParams indexLayoutParams;
	AbsoluteLayout.LayoutParams backLayoutParams;
	AbsoluteLayout.LayoutParams indexCacheViewParams;
	private ImageView indexCacheView;
	private Bitmap homeScreenCoverCache;
	private AlertDialog backLayoutDialog;
	private LinearLayout backLayoutListholder;
	private ButtonBar backLayoutButtonBar;
	private ScrollView backLayoutScrollView;
	private ShadowView indexViewShadowView;
	private boolean updatingWatchlists;
	
	private ArrayList<Action> actionbarActions;
	private float mDensityMultiplier;
	
	private volatile boolean iCreateDone;
	private volatile boolean sCreateDone;
	private InputMethodManager mInputMethodManager;
	private DisplayMetrics mDisplayMetrics;
	
	private boolean nameEditTextInit=false;
	private boolean symbolEditTextInit=false;
	
	private boolean backLayoutEditOn=false;
	private boolean backLayoutDeleteOn=false;
	
	
	private enum DialogMode { ASK, PROCESSING };
	public enum HomeScreenActions { BACK_HOMESCREEN, SEARCH, NEWS, REFRESH, REFRESH2B, STOCKPAGE_ADD, BACK_DISCARD, BACK_NEW2B, BACK_EDIT, BACK_REFRESH2B, FIRE_DELETE, FIRE_ADD2B, FIRE_REFRESH2B};
	@Override
	public void onCreate(Bundle onSavedInstanceState)
	{
		super.onCreate(onSavedInstanceState);
		
		mHandler = new Handler();
		mDensityMultiplier = getResources().getDisplayMetrics().density;
		stocklistDatabaseStatic = new OpenHelper(this, STOCK_DATABASE_NAME, null, 1).getWritableDatabase();
		sPrefs = getSharedPreferences("database log", MODE_WORLD_READABLE);
		mCarpet = new Carpet(getBaseContext());
		mDisplayMetrics = new DisplayMetrics();
		mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		getWindowManager().getDefaultDisplay().getMetrics(mDisplayMetrics);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		r = getResources();
		setContentView(R.layout.homescreen); //NewsWidget created with no data
		mWatchlistManager = new WatchlistManager(this);
		mLayoutInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		sPrefs = getSharedPreferences("recents", Context.MODE_PRIVATE);
		
		mActionBar = (ActionBar)findViewById(R.id.homescreen_actionbar);
		actionbarActions = new ArrayList<Action>();
		actionbarActions.add(new Action(HomeScreenActions.SEARCH, r.getDrawable(R.drawable.search_icon)));
		actionbarActions.add(new Action(HomeScreenActions.NEWS, r.getDrawable(R.drawable.list)));
		mActionBar.setActions(actionbarActions);
		mActionBar.setProgressBarVisibility(View.VISIBLE);
		mActionBar.setParent(this);
		mActionBar.setReturnAction(new Action(HomeScreenActions.REFRESH, r.getDrawable(R.drawable.refresh)));

		mNewsWidget = new NewsWidget(this);
		mIndexView = (IndexView)findViewById(R.id.homescreen_indexview);
		mIndexView.setHomeScreen(this);
		RelativeLayout.LayoutParams par = new RelativeLayout.LayoutParams(mDisplayMetrics.widthPixels, (int) (mDisplayMetrics.heightPixels*(4.0/5.0)));
		par.addRule(RelativeLayout.BELOW, R.id.homescreen_actionbar);
		mIndexView.setLayoutParams(par);
		
		
		 indexLayoutMinWidth = (int) (mDisplayMetrics.widthPixels*0.15f);
		indexLayout = (RelativeLayout)findViewById(R.id.homescreen_indexLayout);
		backLayout = (RelativeLayout)findViewById(R.id.homescreen_backLayout);
		container = (AbsoluteLayout)findViewById(R.id.homescreen_container);
		
		homeScreenCoverCache = Bitmap.createBitmap(mDisplayMetrics.widthPixels+INDEX_VIEW_SHADOW_WIDTH, mDisplayMetrics.heightPixels, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(homeScreenCoverCache); canvas.drawColor(Color.WHITE);
		mActionBar.getCache(mDisplayMetrics.widthPixels, (int) r.getDimension(R.dimen.actionbar_height), INDEX_VIEW_SHADOW_WIDTH, homeScreenCoverCache);
		
		Canvas HCCcanvas = new Canvas(homeScreenCoverCache);
		GradientDrawable d = new GradientDrawable(GradientDrawable.Orientation.RIGHT_LEFT, new int[] {Color.rgb(17, 20, 30), Color.rgb(63, 66, 65)});
		d.setBounds(0, (int) r.getDimension(R.dimen.actionbar_height), INDEX_VIEW_SHADOW_WIDTH, mDisplayMetrics.heightPixels);
		d.draw(HCCcanvas);
		
		GradientDrawable d2 = new GradientDrawable(GradientDrawable.Orientation.RIGHT_LEFT, new int[] {Color.rgb(0, 0, 0), Color.rgb(25, 21, 20)});
		d2.setBounds(0, 0, INDEX_VIEW_SHADOW_WIDTH, (int) r.getDimension(R.dimen.actionbar_height));
		d2.draw(HCCcanvas);
		
		indexCacheView = new ImageView(this);
		indexCacheViewParams = new AbsoluteLayout.LayoutParams(mDisplayMetrics.widthPixels+INDEX_VIEW_SHADOW_WIDTH, mDisplayMetrics.heightPixels, 0, 0);
		indexCacheView.setLayoutParams(indexCacheViewParams);
		indexCacheView.setOnTouchListener(new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				onIndexTouched(event, false);
				return true;
			}
		});
		indexCacheView.setImageBitmap(homeScreenCoverCache);
		initBackLayout();
		
	
		indexLayoutParams = new AbsoluteLayout.LayoutParams(mDisplayMetrics.widthPixels, mDisplayMetrics.heightPixels, 0, 0);
		backLayoutParams = new AbsoluteLayout.LayoutParams(mDisplayMetrics.widthPixels, mDisplayMetrics.heightPixels, 0, 0);
		indexLayout.setLayoutParams(indexLayoutParams);
		backLayout.setLayoutParams(backLayoutParams);

		mIndexView.bringToFront();
		
		indexLayout.bringToFront();
		
		mSymbolSuggestor = new SymbolSuggestor(this, sPrefs);
		mInputMethodManager = (InputMethodManager)getSystemService(
			      Context.INPUT_METHOD_SERVICE);
				
		
		if(onSavedInstanceState != null && onSavedInstanceState.containsKey("index") && onSavedInstanceState.containsKey("chartVariables"))
		{
			bundledIndex = onSavedInstanceState.getBundle("index");
		}
		
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
			mActionBar.lock(HomeScreenActions.SEARCH);
			mActionBar.lock(HomeScreenActions.NEWS);
		}
		@Override
		public void onAnimationEnd(Animation animation) {
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			params.addRule(RelativeLayout.BELOW, R.id.homescreen_actionbar);
			params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);			
			indexLayout.removeView(mCarpet);
			mCarpet.removeAllViews(); //release views
			mCarpet = new Carpet(HomeScreen.this);
			mCarpet.setLayoutParams(params);
			mCarpet.setContent(content);
			
			indexLayout.addView(mCarpet);
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
			mActionBar.lock(HomeScreenActions.SEARCH);
			mActionBar.lock(HomeScreenActions.NEWS);
			
			
		}
		@Override
		public void onAnimationEnd(Animation animation) {
			indexLayout.removeView(mCarpet);
			carpetClosing=false;
			mActionBar.unlockAll();
			carpetIsOpen=false;
			
		}
		@Override
		public void onAnimationRepeat(Animation animation) {
			// TODO Auto-generated method stub
			
		}
	};
	Runnable symbolSuggestorOpenRunnable = new Runnable()
	{
		@Override
		public void run()
		{
		mSymbolSuggestor.getEditText().requestFocus();
		}
	};
	private boolean carpetIsOpen;
	public void act(HomeScreenActions action)
	{
		switch(action)
		{
		case BACK_EDIT:
		{
			if(backLayoutListholder ==  null || backLayoutListholder.getChildCount()==0)
			{
				backLayoutButtonBar.resetButtons();
			}
			else 
			{
			for(int i=0; i<backLayoutListholder.getChildCount(); i++)
			{
				WatchlistItem item;
				try{
				item = (WatchlistItem)backLayoutListholder.getChildAt(i); }
				catch(ClassCastException e) { continue; }
				if(item.getMode()==WatchlistItem.Modes.EDIT)
				{
					item.setMode(WatchlistItem.Modes.NORMAL);
				}
				else
				{
					backLayoutButtonBar.resetButtons(HomeScreenActions.BACK_EDIT);
					item.setMode(WatchlistItem.Modes.EDIT);
				}
			}
			backLayoutDeleteOn=false;
			backLayoutEditOn=!backLayoutEditOn;
			}
		}break;
		case BACK_DISCARD:
		{	
			if(backLayoutListholder ==  null || backLayoutListholder.getChildCount()==0)
			{
				backLayoutButtonBar.resetButtons();
				backLayoutDeleteOn=false;
			}
			else 
			{
			for(int i=0; i<backLayoutListholder.getChildCount(); i++)
			{
				WatchlistItem item;
				try{
				item = (WatchlistItem)backLayoutListholder.getChildAt(i); }
				catch(ClassCastException e) { continue; }
				if(item.getMode()==WatchlistItem.Modes.DELETE)
				{
					item.setMode(WatchlistItem.Modes.NORMAL);
				}
				else
				{
					backLayoutButtonBar.resetButtons(HomeScreenActions.BACK_DISCARD);
					item.setMode(WatchlistItem.Modes.DELETE);
				}
			}
			backLayoutEditOn=false;
			backLayoutDeleteOn=!backLayoutDeleteOn;
			}
			
			
		}break;
		case BACK_NEW2B:
		{
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			
			builder.setView(mLayoutInflater.inflate(R.layout.backlayout_addwatchlist, null));
			builder.setTitle("Create new watchlist:");	
			builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					addWatchlist();
					mInputMethodManager.hideSoftInputFromWindow(((Dialog) dialog).findViewById(R.id.backlayout_entername).getWindowToken(), 0);
					mInputMethodManager.hideSoftInputFromWindow(((Dialog) dialog).findViewById(R.id.backlayout_entersymbols).getWindowToken(), 0);
					nameEditTextInit=false;
					symbolEditTextInit=false;
					dialog.dismiss();
					
				}
			});
			builder.setNegativeButton("Close", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					mInputMethodManager.hideSoftInputFromWindow(((Dialog) dialog).findViewById(R.id.backlayout_entername).getWindowToken(), 0);
					mInputMethodManager.hideSoftInputFromWindow(((Dialog) dialog).findViewById(R.id.backlayout_entersymbols).getWindowToken(), 0);
					nameEditTextInit=false;
					symbolEditTextInit=false;
					dialog.dismiss();

					
				}
			});
			
			backLayoutDialog = builder.create();
			backLayoutDialog.setCanceledOnTouchOutside(false);
			backLayoutDialog.show();
			
			//customizations
			final Button positive = backLayoutDialog.getButton(Dialog.BUTTON_POSITIVE);
			positive.setEnabled(false);
			
			EditText name = (EditText)backLayoutDialog.findViewById(R.id.backlayout_entername);
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
						nameEditTextInit=true;
					if(!s.toString().equals("") && symbolEditTextInit)
					{
						positive.setEnabled(true);
					}
					else
					{
						positive.setEnabled(false);
					}
					
				} 	
			});
			EditText symbols = (EditText)backLayoutDialog.findViewById(R.id.backlayout_entersymbols);
			symbols.addTextChangedListener(new TextWatcher()
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
						symbolEditTextInit=true;
					if(!s.toString().equals("") && nameEditTextInit)
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
		case BACK_REFRESH2B:
		{
			if(!updatingWatchlists){
				updatingWatchlists=true;
			backLayoutText.setText("Updating watchlists...");
			readyBackLayout(true);
			}
		}break;
		case SEARCH:
		{
			
			if(getActionWithAction(HomeScreenActions.SEARCH).getActivate())
			{
				(mCarpet.close()).setAnimationListener(closeAnimListener);
				
				getActionWithAction(HomeScreenActions.SEARCH).setActivate(false);
				mInputMethodManager.hideSoftInputFromWindow(mSymbolSuggestor.getWindowToken(), 0);
				
			}
			
			else
			{
				carpetIsOpen=true;
				closeCarpet(HomeScreen.HomeScreenActions.SEARCH);
				indexLayout.removeView(mCarpet);
				RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, 1);
				params.addRule(RelativeLayout.BELOW, R.id.homescreen_actionbar);
				mCarpet.setLayoutParams(params);
				mCarpet.removeAllViews();
				indexLayout.addView(mCarpet);
			
				(mCarpet.open()).setAnimationListener(new CustomOpenAnimationListener(mSymbolSuggestor, symbolSuggestorOpenRunnable));
				getActionWithAction(HomeScreenActions.SEARCH).setActivate(true);
				
				
			}
			
			
		} break;
		
		case NEWS:
		{
			
			if(getActionWithAction(HomeScreenActions.NEWS).getActivate())
			{
				(mCarpet.close()).setAnimationListener(closeAnimListener);
			
				getActionWithAction(HomeScreenActions.NEWS).setActivate(false);
			}
			else
			{
				carpetIsOpen=true;
				closeCarpet(HomeScreen.HomeScreenActions.NEWS);
				RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, 1);
				mCarpet.removeAllViews();
				params.addRule(RelativeLayout.BELOW, R.id.homescreen_actionbar);
				mCarpet.setLayoutParams(params);
				
				if(mCarpet.getParent()!=null)
					((ViewGroup) mCarpet.getParent()).removeView(mCarpet); 
				
				indexLayout.addView(mCarpet);
			
				(mCarpet.open()).setAnimationListener(new CustomOpenAnimationListener(mNewsWidget, null));
				
				getActionWithAction(HomeScreenActions.NEWS).setActivate(true);
			}
		} break;
		case REFRESH:
		{
			if(!updatingData)
			{
				mActionBar.setRefreshProgressBarRunning(true);
				updateData();
			}
			
			
		} break;
		
		
		}
	}
	public void editWatchlist(final String list)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		
		builder.setView(mLayoutInflater.inflate(R.layout.backlayout_editwatchlist, null));
		builder.setTitle("Add stocks to "+list+":");	
		builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				EditText symbols = (EditText)((Dialog)dialog).findViewById(R.id.backlayout_editwatchlist_newsymbols);
				String s = symbols.getEditableText().toString();
				ArrayList<String> stocks = getAddedStocks(s);
				mWatchlistManager.addStocks(list, stocks);
				readyBackLayout(true);
				
				mInputMethodManager.hideSoftInputFromWindow(symbols.getWindowToken(), 0);
				nameEditTextInit=false;
				symbolEditTextInit=false;
				dialog.dismiss();
				
			}
		});
		builder.setNegativeButton("Close", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				EditText symbols = (EditText)((Dialog)dialog).findViewById(R.id.backlayout_editwatchlist_newsymbols);

				mInputMethodManager.hideSoftInputFromWindow(symbols.getWindowToken(), 0);
				nameEditTextInit=false;
				symbolEditTextInit=false;
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
	public void addWatchlist() //from dialog
	{
		EditText name = (EditText)backLayoutDialog.findViewById(R.id.backlayout_entername);
		EditText symbols = (EditText)backLayoutDialog.findViewById(R.id.backlayout_entersymbols);
		String s = symbols.getEditableText().toString();
		ArrayList<String> stocks = getAddedStocks(s);
		mWatchlistManager.addStocks(name.getEditableText().toString(), stocks);
		
		if(noInternetConnection)
		{
			readySkeletalLayout();
		}
		else
		{
			readyBackLayout(true);
		}
	}
	public void requestRemoveWatchlist(final String name)
	{
		if(!updatingWatchlists)
		{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Are you sure you want to delete \""+name+"\"?");	
		builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				removeWatchlist(name);
				dialog.dismiss();
				
			}
		});
		builder.setNegativeButton("Cancel", null);
		AlertDialog addStocks = builder.create();
		addStocks.setCanceledOnTouchOutside(false);
		addStocks.show();
		}
		else
		{
			Toast.makeText(this, "Please wait for update to finish", Toast.LENGTH_SHORT).show();
		}
		
	}
	
	private void removeWatchlist(String name)
	{
	mWatchlistManager.removeWatchlist(name);
		
		for(int i=0; i<backLayoutListholder.getChildCount(); i++)
		{
			WatchlistItem item;
			try{
			item = (WatchlistItem)backLayoutListholder.getChildAt(i); }
			catch(ClassCastException e) { continue; }
			if(item.getName().equals(name))
			{
				backLayoutListholder.removeViewAt(i);
			}
		}
		if(backLayoutListholder.getChildCount()==1)
		{
			if(!noInternetConnection) backLayoutText.setText("You have no watchlists");
			backLayoutButtonBar.resetButtons();
			backLayoutDeleteOn=false;
		}
		else if(backLayoutListholder.getChildCount()==2)
			if(!noInternetConnection) backLayoutText.setText("1 watchlist");
		else
			if(!noInternetConnection) backLayoutText.setText(backLayoutListholder.getChildCount()+" watchlists");
	}

	private void closeCarpet(HomeScreen.HomeScreenActions me)
	{
		HomeScreen.HomeScreenActions a = otherActionActivated(me);
		if(a != null)
		{
			getActionWithAction(a).setActivate(false);
			indexLayout.removeView(mCarpet);
			mActionBar.setFlip(findIndexOfAction(a), false);
			
			if(a==HomeScreen.HomeScreenActions.SEARCH)
				mInputMethodManager.hideSoftInputFromWindow(mSymbolSuggestor.getWindowToken(), 0);
		}
	}
	private int findIndexOfAction(HomeScreen.HomeScreenActions action)
	{
		for(int i=0; i<actionbarActions.size(); i++)
		{
			if(actionbarActions.get(i).getAction()==action)
				return i+1;
		}
		return -1;
	}
	private HomeScreen.HomeScreenActions otherActionActivated(HomeScreen.HomeScreenActions me)
	{
		if(me != HomeScreenActions.NEWS && getActionWithAction(HomeScreenActions.NEWS).getActivate())
		{
			return HomeScreen.HomeScreenActions.NEWS;
		}
		if(me != HomeScreenActions.SEARCH && getActionWithAction(HomeScreenActions.SEARCH).getActivate())
		{
			return HomeScreen.HomeScreenActions.SEARCH;
		}
		return null;
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
	private void initBackLayout()
	{	
		backLayoutButtonBar = new ButtonBar(this);
		backLayoutButtonBar.setHomeScreen(this);
		RelativeLayout.LayoutParams barP = new RelativeLayout.LayoutParams(mDisplayMetrics.widthPixels-indexLayoutMinWidth, (int) r.getDimension(R.dimen.actionbar_height));
		barP.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		barP.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		backLayoutButtonBar.setLayoutParams(barP);
		backLayoutButtonBar.setId(R.id.homescreen_back_buttonbar);
		backLayoutButtonBar.setDrawingCacheEnabled(true);
		backLayoutButtonBar.addAction(new Action(HomeScreenActions.BACK_DISCARD, r.getDrawable(R.drawable.content_discard)));
		backLayoutButtonBar.addAction(new Action(HomeScreenActions.BACK_NEW2B, r.getDrawable(R.drawable.content_new)));
		backLayoutButtonBar.addAction(new Action(HomeScreenActions.BACK_EDIT, r.getDrawable(R.drawable.content_edit)));
		backLayoutButtonBar.addAction(new Action(HomeScreenActions.BACK_REFRESH2B, r.getDrawable(R.drawable.holo_dark_refresh)));
		backLayout.addView(backLayoutButtonBar);
		

		backLayoutProgressBar = new ProgressBar(this);
		RelativeLayout.LayoutParams barP2 = new RelativeLayout.LayoutParams((int)(mDisplayMetrics.widthPixels*0.1), (int)(mDisplayMetrics.widthPixels*0.1));
		barP2.addRule(RelativeLayout.CENTER_IN_PARENT);
		backLayoutProgressBar.setLayoutParams(barP2);
		backLayoutProgressBar.setIndeterminate(true);
			backLayout.addView(backLayoutProgressBar);
		
		//////////
		
		
		//image
		RelativeLayout.LayoutParams backCacheViewParams = new RelativeLayout.LayoutParams(mDisplayMetrics.widthPixels-indexLayoutMinWidth, ViewGroup.LayoutParams.WRAP_CONTENT);
		backCacheViewParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		backCacheViewParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		backCacheViewParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		
		backLayoutScrollView = new ScrollView(this);
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(mDisplayMetrics.widthPixels-indexLayoutMinWidth, ViewGroup.LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.BELOW, R.id.homescreen_back_buttonbar);
		params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		backLayoutScrollView.setLayoutParams(params);
		backLayout.addView(backLayoutScrollView);
		
		backLayoutListholder = new LinearLayout(this);
		backLayoutListholder.setDrawingCacheEnabled(true);
		backLayoutListholder.setOrientation(LinearLayout.VERTICAL);
		backLayoutScrollView.addView(backLayoutListholder);
		
		backLayoutText = new TextView(this);
		LinearLayout.LayoutParams textP = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		backLayoutText.setLayoutParams(textP);
		backLayoutText.setGravity(Gravity.CENTER);
		backLayoutText.setDrawingCacheEnabled(true);
		backLayoutText.setId(R.id.homescreen_back_textview);
		backLayoutText.setText("Updating watchlists...");
		backLayoutListholder.addView(backLayoutText);
		
	}
	
	private void readyBackLayout(boolean force)
	{
		if(force || watchlists == null)
		{
			backLayoutProgressBar.setVisibility(View.VISIBLE);
			backLayoutProgressBar.bringToFront();
			watchlists = mWatchlistManager.getWatchlists();
			if(watchlists==null || watchlists.size()==0)
			{
				backLayoutProgressBar.setVisibility(View.GONE);
				backLayoutText.setText("You have no watchlists");
			}
			else
			{
				updatingWatchlists = true;
				updateWatchlists();
			}
		}
	}
	@Override
	public void noDataNotify()
	{
		sCreateDone = true;
		noInternetConnection=true;
		if(iCreateDone)
		{
			onUpdateFinished();
		}
		
		backLayoutProgressBar.setVisibility(View.GONE);
		backLayoutText.setText("No data connection");
		readySkeletalLayout();
		
	}
	private void readySkeletalLayout()
	{
		backLayoutListholder.removeAllViews();
		backLayoutText.setText("No data connection");
		backLayoutListholder.addView(backLayoutText);
		ArrayList<ArrayList<String>> matrix = mWatchlistManager.getWatchlists();
		if(matrix==null) return;
		for(int i=0; i<matrix.size(); i++)
		{
			ArrayList<String> shortened = (ArrayList<String>)matrix.get(i).clone();
			shortened.remove(0);
			WatchlistItem item = new WatchlistItem(this, matrix.get(i).get(0), shortened, this);
			item.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, (int) (75*mDensityMultiplier)));
			backLayoutListholder.addView(item);
		}
		updatingWatchlists = false;
		
		if(backLayoutEditOn)
		{
		for(int i=0; i<backLayoutListholder.getChildCount(); i++)
		{
			WatchlistItem item;
			try{
			item = (WatchlistItem)backLayoutListholder.getChildAt(i); }
			catch(ClassCastException e) {continue; }
			item.setMode(WatchlistItem.Modes.EDIT);
		}
		}
		if(backLayoutDeleteOn)
		{
		for(int i=0; i<backLayoutListholder.getChildCount(); i++)
		{
			WatchlistItem item;
			try{
			item = (WatchlistItem)backLayoutListholder.getChildAt(i); }
			catch(ClassCastException e) {continue; }
			item.setMode(WatchlistItem.Modes.DELETE);
		}
		}

	}
	@Override
	public void updateWatchlists(StockGenerics[] updatedWatchlists) {  //called from StockPopulator
		ArrayList<StockGenerics[]> matrix = new ArrayList<StockGenerics[]>();
		int index = 0;
		for(int i=0; i<watchlists.size(); i++)
		{
			StockGenerics[] gen = new StockGenerics[watchlists.get(i).size()-1]; //minus name
			for(int x=1; x<watchlists.get(i).size(); x++)
			{
				gen[x-1] = updatedWatchlists[index];
				index++;
			}
			matrix.add(gen);
		}
		
		backLayoutProgressBar.setVisibility(View.GONE);
		backLayoutText.setText(matrix.size()+(matrix.size()==1 ? " watchlist" : " watchlists") );
		
		backLayoutListholder.removeAllViews();
		backLayoutListholder.addView(backLayoutText);
		for(int i=0; i<matrix.size(); i++)
		{
			WatchlistItem item = new WatchlistItem(this, watchlists.get(i).get(0), matrix.get(i), this);
			item.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, (int) (75*mDensityMultiplier)));
			backLayoutListholder.addView(item);
		}
		updatingWatchlists = false;
		
		if(backLayoutEditOn)
		{
		for(int i=0; i<backLayoutListholder.getChildCount(); i++)
		{
			WatchlistItem item;
			try{
			item = (WatchlistItem)backLayoutListholder.getChildAt(i); }
			catch(ClassCastException e) {continue; }
			item.setMode(WatchlistItem.Modes.EDIT);
		}
		}
		if(backLayoutDeleteOn)
		{
		for(int i=0; i<backLayoutListholder.getChildCount(); i++)
		{
			WatchlistItem item;
			try{
			item = (WatchlistItem)backLayoutListholder.getChildAt(i); }
			catch(ClassCastException e) {continue; }
			item.setMode(WatchlistItem.Modes.DELETE);
		}
		}
		
	}

	private void startStockpage(char[] array)
	{
		Intent intent = new Intent(this, StockPage.class);
		intent.putExtra("ticker", array);
		startActivity(intent);
	}
	public void startStockpage(View view)
	{
		char[] c = mEditText.getText().toString().toCharArray();
		mEditText.setText("");
		startStockpage(c);
	}
	
	private void setDatabase()
	{
		try{
		stocklistDatabaseStatic = new OpenHelper(this, STOCK_DATABASE_NAME, null, 1).getWritableDatabase(); }
		catch(SQLiteException e) //SQLiteDatabaseLockedException
		{
			setDatabase();
		}
	}
	
	@Override
	public void onResume()
	{
		super.onResume();
		//reset ActionBar
		if(getActionWithAction(HomeScreenActions.SEARCH).getActivate())
		{
			mActionBar.reset();
			getActionWithAction(HomeScreenActions.SEARCH).setActivate(false);
			mInputMethodManager.hideSoftInputFromWindow(mSymbolSuggestor.getWindowToken(), 0);
			indexLayout.removeView(mCarpet);
			carpetClosing=false;
			carpetIsOpen=false;
		}
		
		//reset database
		if(stocklistDatabaseStatic != null) stocklistDatabaseStatic.close();
			setDatabase();
			
		if(bundledIndex == null)
		{
			mActionBar.setRefreshProgressBarRunning(true);
			updateData();
			updatingData = true;
		}
		else
		{
			//mIndexView.initialize(this, new Index(bundledIndex), bundledChartVariables);
			updatingData = false;
		}
		//refresh
		if(mSymbolSuggestor!=null)
			mSymbolSuggestor.refresh();
		
		//ready back layout
		backLayoutText.setText("Updating watchlists");
		backLayoutListholder.removeAllViews();
		backLayoutListholder.addView(backLayoutText);
		readyBackLayout(true);
		updatingData=false;
	}

	@Override
	public void onStart()
	{
		super.onStart();

	}
	@Override
	public void onDestroy()
	{
		super.onDestroy();
	}
	@Override
	public void onStop()
	{
		super.onStart();

	}
	
	private float downX = 0f;
	private boolean notAcceptingDowns=false;
	private int pointerCount=0;
	private long openedTime;
	public void onIndexTouched(MotionEvent e, boolean indexTouch)
	{
		if(!slideLock)
		{
			if(e.getAction() == MotionEvent.ACTION_DOWN)
			{
				pointerCount++;
				if(notAcceptingDowns) return;
				
				if(indexTouch)
				{
					openedTime=System.nanoTime();
				}
				
				downX = e.getRawX();
				if(indexTouch)
				{
					indexCacheViewParams.x=-10;
					container.addView(indexCacheView);
					indexLayout.setVisibility(View.GONE);
					readyBackLayout(false);
					notAcceptingDowns=true;
				}

				return;
			}
			int movedRight  = (int) (e.getRawX()-downX);
			if((e.getAction() == MotionEvent.ACTION_UP || e.getAction()==MotionEvent.ACTION_CANCEL))
			{
				pointerCount--;
				if(pointerCount>0) return;
				
				finishViewChange(movedRight);
				return;
			}
			if(indexSlidingOpen && movedRight > 0 && movedRight <= (mDisplayMetrics.widthPixels-indexLayoutMinWidth)) //opening
			{
				indexCacheViewParams.x=movedRight-10;
				container.requestLayout();
			}
			else if(!indexSlidingOpen && movedRight < 0 && ((mDisplayMetrics.widthPixels-indexLayoutMinWidth)+movedRight) >= 0) //closing
			{
				int newX = (mDisplayMetrics.widthPixels-indexLayoutMinWidth)+movedRight;
				indexCacheViewParams.x=newX;
				container.requestLayout();
			}
		}
	}
	private void bounceIndexLayout()
	{
		if(mIndexView.isBeingInteractedWith() || carpetIsOpen) {return;}
		try{
		container.addView(indexCacheView);}
		catch(IllegalStateException e) {return;}
		
		indexLayout.setVisibility(View.GONE);
		notAcceptingDowns=true;
		int overPix = (int)(indexLayoutMinWidth/3.0);
		
		indexCacheViewParams.x=overPix;
		container.requestLayout();
		
		slideLock=true;
		TranslateAnimation anim = new TranslateAnimation(0, -overPix, 0, 0);
		anim.initialize(mDisplayMetrics.widthPixels, mDisplayMetrics.heightPixels, mDisplayMetrics.widthPixels, mDisplayMetrics.heightPixels);
		anim.setDuration(850);
		anim.setFillAfter(true);
		anim.setInterpolator(new BounceInterpolator());
		
		anim.setAnimationListener(new Animation.AnimationListener() {
			
			@Override
			public void onAnimationStart(Animation animation) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				indexCacheView.clearAnimation();
				indexCacheViewParams.x=0;
				container.requestLayout();
				slideLock=false;
				notAcceptingDowns=false;
				indexSlidingOpen=true;
				indexLayout.setVisibility(View.VISIBLE);
				try{
				container.removeView(indexCacheView);}
				catch(NullPointerException e){}
				
			}
		});
		indexCacheView.startAnimation(anim);
	}
	private boolean indexSlidingOpen=true;
	private boolean slideLock=false;
	private void finishViewChange(float movedRight)
	{
		if(indexSlidingOpen && movedRight>0)  //finish open
		{
			if(movedRight >= (mDisplayMetrics.widthPixels-indexLayoutMinWidth))
			{
				slideLock=false;
				indexSlidingOpen=false;
				indexCacheView.clearAnimation();
				notAcceptingDowns=false;
			}
			else
			{
			slideLock=true;
			TranslateAnimation anim = new TranslateAnimation(0, (mDisplayMetrics.widthPixels-indexLayoutMinWidth)-movedRight, 0, 0);
			anim.initialize(mDisplayMetrics.widthPixels, mDisplayMetrics.heightPixels, mDisplayMetrics.widthPixels, mDisplayMetrics.heightPixels);
			anim.setDuration((long) ((((mDisplayMetrics.widthPixels-indexLayoutMinWidth)-movedRight)/(mDisplayMetrics.widthPixels-indexLayoutMinWidth))*(300)));
			anim.setFillAfter(true);
			anim.setInterpolator(new AccelerateDecelerateInterpolator());
			
			anim.setAnimationListener(new Animation.AnimationListener() {
				
				@Override
				public void onAnimationStart(Animation animation) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void onAnimationRepeat(Animation animation) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void onAnimationEnd(Animation animation) {
					mVibrator.vibrate(VIBRATION_DURATION);
					indexCacheViewParams.x=(mDisplayMetrics.widthPixels-indexLayoutMinWidth);
					container.requestLayout();
					slideLock=false;
					indexSlidingOpen=false;
					indexCacheView.clearAnimation();
					notAcceptingDowns=false;
				}
			});
			indexCacheView.startAnimation(anim);
			}
		}
		else if( (!indexSlidingOpen && movedRight<0)) //finish close
		{
			if(movedRight <= -(mDisplayMetrics.widthPixels-indexLayoutMinWidth))
			{
				slideLock=false;
				indexSlidingOpen=true;
				notAcceptingDowns=false;
				indexLayout.setVisibility(View.VISIBLE);
				try{
				container.removeView(indexCacheView);
				}catch(NullPointerException e) 
				{
				}

			}
			else
			{
			slideLock=true;
			TranslateAnimation anim = new TranslateAnimation(0, -((mDisplayMetrics.widthPixels-indexLayoutMinWidth)+movedRight), 0, 0);
			anim.initialize(mDisplayMetrics.widthPixels, mDisplayMetrics.heightPixels, mDisplayMetrics.widthPixels, mDisplayMetrics.heightPixels);
			anim.setDuration((long) ((((mDisplayMetrics.widthPixels-indexLayoutMinWidth)+movedRight)/(mDisplayMetrics.widthPixels-indexLayoutMinWidth))*(350)));
			anim.setFillAfter(true);
			anim.setInterpolator(new AccelerateDecelerateInterpolator());
			
			anim.setAnimationListener(new Animation.AnimationListener() {
				
				@Override
				public void onAnimationStart(Animation animation) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void onAnimationRepeat(Animation animation) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void onAnimationEnd(Animation animation) {
					mVibrator.vibrate(VIBRATION_DURATION);
					indexCacheView.clearAnimation();
					indexCacheViewParams.x=0;
					container.requestLayout();
					slideLock=false;
					notAcceptingDowns=false;
					indexSlidingOpen=true;
					indexLayout.setVisibility(View.VISIBLE);
					try{
					container.removeView(indexCacheView);}
					catch(NullPointerException e){}
				}
			});
			indexCacheView.startAnimation(anim);
			}
		}
		//weird cases
		else if((indexSlidingOpen && movedRight<=0) ) //opening, but it is negative = close it
		{
			indexCacheViewParams.x=0;
			container.requestLayout();
			slideLock=false;
			indexSlidingOpen=true;
			notAcceptingDowns=false;
			indexLayout.setVisibility(View.VISIBLE);
			
			if(indexCacheView!=null && container.indexOfChild(indexCacheView) != -1)
				try{container.removeView(indexCacheView);}catch(NullPointerException e){}
		}
		else if( (!indexSlidingOpen && movedRight>=0) ) //closing, but it is positive = fully open
		{
			indexCacheViewParams.x=(mDisplayMetrics.widthPixels-indexLayoutMinWidth);
			container.requestLayout();
			slideLock=false;
			notAcceptingDowns=false;
			indexSlidingOpen=false;
			backLayoutProgressBar.bringToFront();
		}
	
	}
	private void updateData()
	{
		updatingData = true;
		mActionBar.setProgressBarProgress(0);
		
		iCreateDone = false;
		sCreateDone = false;
		mActionBar.setProgressBarVisibility(View.VISIBLE);
		mNews = new News();  //no ticker
		mPop = new StockPopulator(this, StockPopulatorOptions.FILL_MARKET_NEWS, 45);
		mPop.execute(mNews);
		
		iPop = new IndexPopulator(this, this, 45);
		iPop.execute();
	}
	private void updateWatchlists()
	{
		updatingData = true;

		StockPopulator sPop = new StockPopulator(this, StockPopulatorOptions.FILL_WATCHLISTS, 45);
		int length = 0;
		for(int i=0; i<watchlists.size(); i++)
		{
			if(watchlists.get(i).size()==0)
				continue;
			length += watchlists.get(i).size()-1; //first is name of watchlist
		}
		StockGenerics[] array = new StockGenerics[length];
		int index=0;
		for(int i=0; i<watchlists.size(); i++)
		{
			for(int x=1; x<watchlists.get(i).size(); x++)
			{
				array[index] = new StockGenerics(new Ticker(watchlists.get(i).get(x)));
				index++;
			}
		}
		if(array.length!=0)
			sPop.execute(array);
		else
		{
			backLayoutProgressBar.setVisibility(View.GONE);
			backLayoutText.setText("You have no watchlists");
		}

	}
	@Override
	public void onPause()
	{
		super.onStop();
		mInputMethodManager.hideSoftInputFromWindow(mSymbolSuggestor.getWindowToken(), 0);
		stocklistDatabaseStatic.close();
		mPop.cancel(true);
		iPop.cancel(true);
		updatingData=false;
		mActionBar.setRefreshProgressBarRunning(false);
		mActionBar.setProgressBarProgress(0);
		mActionBar.setProgressBarVisibility(View.GONE);
		
	}
	
	@Override
	public void updateChart(Chart[] updatedChart) {
		// do nothing
	}
	
	
	private boolean databaseEmpty()
	{
		Cursor c;
		try{
		c = stocklistDatabaseStatic.query("stocklist", null, null, null, null, null, null);
		}
		catch(java.lang.IllegalStateException e)
		{
			stocklistDatabaseStatic = new OpenHelper(this, STOCK_DATABASE_NAME, null, 1).getWritableDatabase();
			return databaseEmpty();
		}
		if(c.getCount() < 6450 - 3) {
			c.close();
			return true;
		}
		else
		{
			c.close();
			return false;
		}
	}
	
	private void initiateCheckSequence()
	{
		//check for stuff to prompt user
		if(databaseEmpty())
		{
			ProgressDialog dia = new ProgressDialog(this);
			dia.setTitle("Initializing");
			dia.setIndeterminate(true);
			dia.setCancelable(false);
			dia.setMessage("Processing...");
			dia.setCanceledOnTouchOutside(false);
			dia.show();
			stockListDownloader = new StockListDownloader(this, dia, false);
			stockListDownloader.execute();
			
			
			
		}

			
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		menu.add("Update Stock Database");
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		if(item.getTitle().equals("Update Stock Database"))
		{
			progressDialog = new ProgressDialog(this);
			progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			progressDialog.setMax(100);
			progressDialog.setProgress(0);
			progressDialog.setIndeterminate(false);
			progressDialog.setCanceledOnTouchOutside(false);
			progressDialog.setButton(ProgressDialog.BUTTON_NEGATIVE, "Cancel", this);
			progressDialog.setTitle("Updating data");
			dMode = DialogMode.PROCESSING;
			progressDialog.show();
			
			
			stockListDownloader = new StockListDownloader(this, progressDialog, true);
			stockListDownloader.execute();
		}
		return true;
	}
	@Override
	public void updateNews(News[] updatedNews) {
		sCreateDone = true;
		noInternetConnection=false;
		if(iCreateDone)
		{
			onUpdateFinished();
			mActionBar.unlockAll();
		}

		mNews = updatedNews[0];
		mNewsWidget.initialize(mNews);
		
	}
	
	private void onUpdateFinished()
	{
		updatingData=false;
		mActionBar.setRefreshProgressBarRunning(false);
		mActionBar.setProgressBarProgress(100);
		initiateCheckSequence();
		mActionBar.setProgressBarVisibility(View.GONE);
	}
	
	@Override
	public void updateStats(Stats[] updatedStats) {
		// do nothing
		
	}
	@Override
	public void updateStockGenerics(StockGenerics[] updatedStockGenerics) {
		// do nothing
		
	}
	@Override
	public void done(StockData[] data, StockPopulatorOptions task) {
		updatingData=false;
		
	}
	@Override
	public void updateIncicies(Index index) {
		if(index!=null)
		{
			iCreateDone = true;
			noInternetConnection=false;
			mIndexView.initialize(index, this, 10, null);
			if(sCreateDone)
			{
				onUpdateFinished();
				mActionBar.unlockAll();
			}
			bundledIndex = index.getBundle();
		}
	}
	@Override
	public void noDataNotifyIndex(Index index)
	{
		mActionBar.lock(HomeScreenActions.NEWS);
		iCreateDone = true;
		noInternetConnection=true;
		mIndexView.initialize(index, this, 10, "No internet connection");
		if(sCreateDone)
		{
			onUpdateFinished();
		}
		Toast.makeText(this, "No data connection",  Toast.LENGTH_LONG).show();
	}
	@Override
	public void notification(Object object) {
		mIndexView.getCache(mActionBar.getHeight(),INDEX_VIEW_SHADOW_WIDTH , homeScreenCoverCache);
		mActionBar.getCache(mDisplayMetrics.widthPixels, (int) r.getDimension(R.dimen.actionbar_height), INDEX_VIEW_SHADOW_WIDTH, homeScreenCoverCache);

		Canvas canvas = new Canvas(homeScreenCoverCache);
		GradientDrawable d = new GradientDrawable(GradientDrawable.Orientation.RIGHT_LEFT, new int[] {Color.rgb(17, 20, 30), Color.rgb(63, 66, 65)});
		d.setBounds(0, (int) r.getDimension(R.dimen.actionbar_height), INDEX_VIEW_SHADOW_WIDTH, mDisplayMetrics.heightPixels);
		d.draw(canvas);
		
		GradientDrawable d2 = new GradientDrawable(GradientDrawable.Orientation.RIGHT_LEFT, new int[] {Color.rgb(0, 0, 0), Color.rgb(25, 21, 20)});
		d2.setBounds(0, 0, INDEX_VIEW_SHADOW_WIDTH, (int) r.getDimension(R.dimen.actionbar_height));
		d2.draw(canvas);

		mHandler.post(new Runnable()
		{
			@Override
			public void run()
			{
				indexCacheView.setImageBitmap(homeScreenCoverCache);
				bounceIndexLayout();
			}
		});
		
		
	}


	@Override
	public void onClick(DialogInterface dialog, int which) {
		switch(dMode)
		{
		case PROCESSING:
		{
			//must be cancel
			stockListDownloader.cancel(true);
			
		}
		}
		
	}
	


	@Override
	public void updateProgress(int progress) {
		//  45/45/10
		mActionBar.setProgressBarProgress(mActionBar.getProgressBarProgress()+progress);
		
		
	}
}
