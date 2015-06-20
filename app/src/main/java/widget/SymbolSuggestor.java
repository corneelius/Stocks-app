package widget;

import home.HomeScreen;

import java.util.Calendar;

import nr.app.R;
import stockpage.StockPage;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import data.Ticker;
import data.Types;

public class SymbolSuggestor extends RelativeLayout{
	private PrettyEditText mEditText;
	private ListView mListView;
	private ListAdapter mListAdapter;
	private float mDensityMultiplier;
	private DBTextWatcher mTextWatcher;
	private InputMethodManager mInputMethodManager;
	private ScrollView recentsView;
	private Typeface robotoBold;
	private Typeface robotoRegular;
	private HomeScreen mHomeScreen;
	
	public SymbolSuggestor(HomeScreen context, SharedPreferences sPrefs) {
		super(context);
		mHomeScreen=context;
		robotoBold = Typeface.createFromAsset(getContext().getAssets(), "fonts/Roboto-Bold.ttf");
		robotoRegular = Typeface.createFromAsset(getContext().getAssets(), "fonts/Roboto-Regular.ttf");
		mDensityMultiplier = getContext().getResources().getDisplayMetrics().density;
		mTextWatcher = new DBTextWatcher();
		mInputMethodManager = (InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		
		initPrettyText();
		addView(mEditText);
		initListView();
		addView(mListView);
		initRecentsView(false);
		addView(recentsView);
		
	}

	public void refresh()
	{
		removeView(recentsView);
		initRecentsView(true);
		addView(recentsView);
		mEditText.setText("");
	}
	private void initListView()
	{
		mListAdapter = new SuggestionAdapter();
		mListView = new ListView(getContext());
		mListView.setAdapter(mListAdapter);
		
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.BELOW, R.id.symbolsuggestor_prettyedittext);
		params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		
		mListView.setLayoutParams(params);
		mListView.setOnItemClickListener(new OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				String ticker = new String(((SuggestionView) view).getTicker().toCharArray());
				mInputMethodManager.hideSoftInputFromWindow(SymbolSuggestor.this.getWindowToken(), 0);
				addStockToPrefs(ticker);
				
				Intent intent = new Intent(getContext(), StockPage.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
				intent.putExtra("ticker", ticker.toCharArray());
					Intent returnIntent = new Intent(getContext(), HomeScreen.class);
				intent.putExtra("intent", returnIntent);
				getContext().startActivity(intent);
			}
		});
	}
	private void initRecentsView(boolean force)
	{
		if(recentsView==null || force)
		{
		ScrollView holder = new ScrollView(this.getContext());
		LinearLayout recents = new LinearLayout(this.getContext());
		
		recents.setOrientation(LinearLayout.VERTICAL);
		
		TextView title = new TextView(getContext());
		title.setText("Recent quotes");
		LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		titleParams.leftMargin=10;
		title.setLayoutParams(titleParams);
		title.setTypeface(robotoBold);
		title.setTextSize(10*mDensityMultiplier);
		title.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG);
		title.setTextColor(Color.rgb(51, 181, 229));
		recents.addView(title);
		
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.BELOW, R.id.symbolsuggestor_prettyedittext);
		params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		holder.setLayoutParams(params);
		
		
		Cursor c = HomeScreen.stocklistDatabaseStatic.query("stocklist", new String[] {"_id", "searches", "date", "name", "market_cap"},
				"searches > 0", new String[] {}, null, null, "date DESC", null); //unlimited
		
		LinearLayout.LayoutParams itemParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, (int) (80*mDensityMultiplier));
		itemParams.leftMargin=(int) (3*mDensityMultiplier);
		itemParams.rightMargin=(int) (3*mDensityMultiplier);
		
		c.moveToFirst();
		while(!c.isAfterLast()) {
			String ticker = c.getString(0);

			int searches = c.getInt(1);
			
			String name = c.getString(3);
			int marketCap = c.getInt(4);
			
			StockItemData data = new StockItemData(ticker, name, marketCap, "", "", searches);
			SuggestionView v = new SuggestionView(this.getContext());
			v.setData(data);
			v.setLayoutParams(itemParams);
			v.setOnClickListener(new View.OnClickListener()
			{
				public void onClick(View view)
				{
					String ticker = new String(((SuggestionView) view).getTicker().toCharArray());
					mInputMethodManager.hideSoftInputFromWindow(SymbolSuggestor.this.getWindowToken(), 0);
					addStockToPrefs(ticker);
					Intent intent = new Intent(getContext(), StockPage.class);
					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
					intent.putExtra("ticker", ticker.toCharArray());
						Intent returnIntent = new Intent(getContext(), HomeScreen.class);
					intent.putExtra("intent", returnIntent);
					getContext().startActivity(intent);
				}
			});
			recents.addView(v);
			c.moveToNext();
		}
		c.close();
		holder.addView(recents, ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT);
		recentsView = holder;
		
		}
		
	}

	private void addStockToPrefs(String stock)
	{
		/*
		HashSet<String> existing = (HashSet<String>) sPrefs.getStringSet("recents", null);
		
		if(existing == null)
		{
			existing = new HashSet<String>();
		}
		existing.add(stock);
		
		SharedPreferences.Editor e = sPrefs.edit();
		e.putStringSet("recents", existing);
		e.apply();
		*/
		
		
		HomeScreen.stocklistDatabaseStatic.execSQL("UPDATE stocklist SET searches=(SELECT searches FROM stocklist WHERE _id='"+stock+"') + 1, date="+Calendar.getInstance().getTimeInMillis()+" WHERE _id = '"+stock+"';");
		
	}

	private void initPrettyText()
	{
		mEditText=new PrettyEditText(this.getContext());
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, (int) (mDensityMultiplier*50));
		params.topMargin=5;
		params.rightMargin=7;
		params.leftMargin=7;
		params.bottomMargin=1;
		params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		mEditText.setLayoutParams(params);
		mEditText.setLines(1);
		mEditText.setHint("Enter symbol or company name");
		mEditText.addTextChangedListener(mTextWatcher);
		mEditText.setId(R.id.symbolsuggestor_prettyedittext);
		mEditText.setSingleLine();
		mEditText.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
		mEditText.setImeActionLabel("Search", EditorInfo.IME_ACTION_SEARCH);
		
		mEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
		    @Override
		    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
		        	if(Ticker.validTicker(mEditText.getEditableText().toString()))
		        	{
		        		String ticker=mEditText.getEditableText().toString();
		        		Intent intent = new Intent(getContext(), StockPage.class);
						intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
						intent.putExtra("ticker", ticker.toCharArray());
							Intent returnIntent = new Intent(getContext(), HomeScreen.class);
						intent.putExtra("intent", returnIntent);
						getContext().startActivity(intent);
		        		mInputMethodManager.hideSoftInputFromWindow(SymbolSuggestor.this.getWindowToken(), 0);
		        	}
		        	else
		        	{
		        		Toast.makeText(mEditText.getContext(), "Invalid ticker: "+mEditText.getText().toString(), Toast.LENGTH_SHORT).show();
		        		mEditText.setText("");
		        	}
		        	return true;
		        }
		        return false;
		    }
		});
	}

	private void changeData(Cursor newCursor)
	{
		((SuggestionAdapter) mListAdapter).setCursor(newCursor);
		mListView.setAdapter(mListAdapter);
	}
	
	
	@Override
	public boolean onTouchEvent(MotionEvent e)
	{
		super.onTouchEvent(e);
		return true;
	}
	public View getEditText()
	{
		return mEditText;
	}
	
	
	
	/*****************************************************/
	
	private class SuggestionAdapter implements ListAdapter
	{
		private Cursor mCursor;
		
		public void setCursor(Cursor c)
		{
			mCursor = c;
			
		}

		@Override
		public void registerDataSetObserver(DataSetObserver observer) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void unregisterDataSetObserver(DataSetObserver observer) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public int getCount() {
			return (mCursor != null ? mCursor.getCount() : 0);
		}

		@Override
		public Object getItem(int position) {
			mCursor.moveToPosition(position);
			StockItemData d = new StockItemData(
					mCursor.getString(0), mCursor.getString(1), mCursor.getInt(2), mCursor.getString(3), mCursor.getString(4), -1);
			return d;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public boolean hasStableIds() {
			return true;
		}

		@Override
		public int getItemViewType(int position) {
			return 0;
		}

		@Override
		public int getViewTypeCount() {
			return 1;
		}

		@Override
		public boolean isEmpty() {
			return (mCursor.getCount() == 0 ? true : false);
		}

		@Override
		public boolean areAllItemsEnabled() {
			return true;
		}

		@Override
		public boolean isEnabled(int position) {
			return true;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if(convertView==null)
			{
				SuggestionView view = new SuggestionView(getContext());
				view.setData((StockItemData) getItem(position));
			
				AbsListView.LayoutParams params = new AbsListView.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, (int) (80*mDensityMultiplier));
				//params.leftMargin=(int) (3*mDensityMultiplier);
				//params.rightMargin=(int) (3*mDensityMultiplier);
				view.setLayoutParams(params);
			
				return view;
			}
			else
			{
				((SuggestionView) convertView).setData((StockItemData) getItem(position));
				return convertView;
			}
		}
		

	}
	
	private class StockItemData
	{
		private String symbol;
		private String name;
		private int market_cap;
		private String sector;
		private String industry;
		private int searches;
		
		public StockItemData(String symbol, String name, int market_cap, String sector, String industry, int searches)
		{
			this.symbol = symbol;
			this.name = name;
			this.market_cap = market_cap;
			this.sector = sector;
			this.industry = industry;
			this.searches=searches;
		}

		public String getSymbol() {
			return symbol;
		}

		public String getName() {
			return name;
		}

		public int getMarket_cap() {
			return market_cap;
		}

		public String getSector() {
			return sector;
		}

		public String getIndustry() {
			return industry;
		}

		public int getSearches() {
			return searches;
		}

		public void setSearches(int searches) {
			this.searches = searches;
		}
		
		
	}
	
	
	
	
	private class PrettyEditText extends EditText
	{
		private char[] approvedCharacters={'-'};

		public PrettyEditText(Context context) {
			super(context);
		}
		
	}
	public void setDatabase(SQLiteDatabase d)
	{
		HomeScreen.stocklistDatabaseStatic=d;
	}
	
	private class DBTextWatcher implements TextWatcher
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

		private Cursor c;
		@Override
		public void afterTextChanged(Editable s) {
			if(mListView != null)
			{
				if(s.toString().equals(""))
				{
					recentsView.setVisibility(View.VISIBLE);
					mListView.setVisibility(View.INVISIBLE);
					return;
				}
				else
				{
					recentsView.setVisibility(View.INVISIBLE);
					mListView.setVisibility(View.VISIBLE);
				}
				mListView.setVisibility(View.VISIBLE);
				if(c!=null)
					c.close();

				boolean var=true;
				int count=0;
				while(var && count <= 10)
				{
					try{
					c = HomeScreen.stocklistDatabaseStatic.query("stocklist", new String[] {"_id", "name", "market_cap", "sector", "industry" },
							"_id LIKE '"+s.toString()+"%' OR name LIKE '"+s.toString()+"%'", new String[] {}, null, null, "market_cap DESC", "25");
					var=false;
					}catch(IllegalStateException e)
					{
						var=true;
					}
					count++;
				}
				if(!var)
				{
					c.move(-1);
					changeData(c);
				}
					
			}
		} 	
	}
	
	private class SuggestionView extends View
	{
		private StockItemData mData;
		private Paint mTitlePaint;
		private Paint mItemPaint;

		public SuggestionView(Context context) {
			super(context);
			this.setPadding((int)(5*mDensityMultiplier), 0, (int)(5*mDensityMultiplier), 0);
			mTitlePaint = new Paint();
			mTitlePaint.setTextSize(16*mDensityMultiplier);
			mTitlePaint.setAntiAlias(true);
			mTitlePaint.setTypeface(robotoBold);
			this.setBackgroundColor(Color.argb(199, 241, 241, 241));
			
			mItemPaint = new Paint();
			mItemPaint.setTypeface(robotoRegular);
			mItemPaint.setTextSize(12*mDensityMultiplier);
			mItemPaint.setAntiAlias(true);
			
		}
		public String getTicker()
		{
			return mData.getSymbol();
		}
		
		public void setData(StockItemData d)
		{
			 mData = d;
		}
		public StockItemData getData()
		{
			return mData;
		}
		
		
		@Override
		public void onDraw(Canvas canvas)
		{
			canvas.drawText(trimToWidth(convertToAscii(mData.getName()), mTitlePaint, (getWidth()-(getWidth()/8.5f)-5*mDensityMultiplier)), getWidth()/8.5f, getHeight()/2.0f, mTitlePaint);
			if(mData.getSearches() == -1)
				canvas.drawText(trimToWidth(mData.getSector()+", "+mData.getIndustry(), mItemPaint, (getWidth()-(getWidth()/8.5f)-8*mDensityMultiplier)), (getWidth()/8.5f), (getHeight()/3.0f) + 2*mTitlePaint.getTextSize(), mItemPaint);
			else
				canvas.drawText(trimToWidth("Searched for "+mData.getSearches()+(mData.getSearches() ==1 ? " time" : " times"), mItemPaint, (getWidth()-(getWidth()/8.5f)-8*mDensityMultiplier)), (getWidth()/8.5f), (getHeight()/3.0f) + 2*mTitlePaint.getTextSize(), mItemPaint);

			
			
		}
		
		public String convertToAscii(String s)
		{
			int index = s.indexOf("&");
			if(index >= 0)
			{
				int index2 = s.indexOf(";");
				if(index2 >= 0)
				{
					return s.substring(0, index) + Types.convertCodes(s.substring(index, index2+1)) + s.substring(index2+1);
				}
			}
				return s;
		}
		private String trimToWidth(String s, Paint paint, float width)
		{
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
		
		
	}
	
	
		
	

	

}
