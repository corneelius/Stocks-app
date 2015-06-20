package widget;

import java.util.Calendar;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import data.News;

public class NewsWidget extends ListView implements OnItemClickListener{
	private News mNews;
	private Intent returnIntent;
	
	
	public NewsWidget(Context context)
	{
		super(context);
		init();
		
	}
	
	public NewsWidget(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init();
	}
	private void init()
	{		
		
		setBackgroundColor(Color.argb(199, 241, 241, 241));
		setOnItemClickListener(this);
		
		setDividerHeight(0);
	}
	
	public void initialize(News news)
	{
		setAdapter(new NewsAdapter(getContext(), news, Calendar.getInstance().getTime(), this.getWidth()));
		mNews = news;
	}


	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Intent intent = new Intent(this.getContext().getApplicationContext(), news.NewsScreen.class);
		intent.putExtra("news_article", mNews.get(position).getBundle());
		intent.putExtra("intent", returnIntent);
		getContext().startActivity(intent);
		
	}


}
