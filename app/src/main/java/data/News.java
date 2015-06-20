package data;

import java.util.ArrayList;

import android.os.Bundle;
import android.os.Parcelable;

public class News implements StockData{

	private Ticker mTicker;
	private ArrayList<NewsArticle> mArticles;
	private String largestTitle;
	
	
	public Bundle getBundle()
	{
		Bundle b = new Bundle();
		b.putParcelable("ticker", mTicker);
		b.putParcelableArray("articles", mArticles.toArray(new NewsArticle[mArticles.size()]));
		b.putString("largestTitle", largestTitle);
		return b;
	}
	public News(Bundle b)
	{
		mTicker = b.getParcelable("ticker");
		Parcelable[] articles = b.getParcelableArray("articles");
		for(int i=0; i<articles.length; i++)
		{
			mArticles.add((NewsArticle) articles[i]);
		}
		largestTitle = b.getString("largestTitle");
	}
	public void addArticle(NewsArticle article)
	{
		mArticles.add(article);
	}
	public News(Ticker ticker)
	{
		this.mTicker = ticker;
		mArticles = new ArrayList<NewsArticle>();
	}
	public News()
	{
		mArticles = new ArrayList<NewsArticle>();
	}
	
	public int getNumArticles()
	{
		return mArticles.size();
	}
	public NewsArticle get(int index)
	{
		return mArticles.get(index);
	}
	public Ticker getTicker()
	{
		return mTicker;
	}
	public void clear()
	{
		mArticles.clear();
	}
	@Override
	public String summarizeAsString() {
		String output = "";
		for(NewsArticle article : mArticles)
		{
			output = output + article.getTitle()+"-"+article.getLink()+"\n";
		}
		return output;
	}
	public String getLargestTitle() {
		return largestTitle;
	}
	public void setLargestTitle(String largestTitle) {
		this.largestTitle = largestTitle;
	}

}
