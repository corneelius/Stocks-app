package widget;

import java.util.Date;

import nr.app.R;
import android.content.Context;
import android.content.res.Resources;
import android.database.DataSetObserver;
import android.graphics.Paint;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListAdapter;
import data.News;

public class NewsAdapter implements ListAdapter {
	private News mNews;
	private Context mContext;
	private Resources r;
	private float articleViewHeight;
	private Date now;
	private int articleViewWidth;
	private volatile Paint recycledPaint;
	
	public NewsAdapter(Context context, News news, Date now, int width)
	{
		mNews = news;
		mContext = context;
		r = context.getResources();
		articleViewHeight = r.getDimension(R.dimen.newswidget_view_height);
		this.now=now;
		this.articleViewWidth=width;
		recycledPaint = new Paint();
	}

	@Override
	public void registerDataSetObserver(DataSetObserver observer) {
		// 
		
	}

	@Override
	public void unregisterDataSetObserver(DataSetObserver observer) {
		// 
		
	}

	@Override
	public int getCount() {
		return mNews.getNumArticles();
	}

	@Override
	public Object getItem(int position) {
		return mNews.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ArticleView view;
		if(convertView == null)
		{
			view = new ArticleView(mContext, now, articleViewWidth, recycledPaint);
			view.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.FILL_PARENT, (int)articleViewHeight));
		}
		else
		{
			view = (ArticleView)convertView;
		}
		view.setEnabled(true);
		view.setArticle(mNews.get(position));
		
		return view;
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
		return mNews.getNumArticles() == 0;
	}

	@Override
	public boolean areAllItemsEnabled() {
		return true;
	}

	@Override
	public boolean isEnabled(int position) {
		return true;
	}

}
