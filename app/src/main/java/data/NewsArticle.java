package data;

import java.util.Date;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

public class NewsArticle implements Parcelable{
	private String title;
	private String link;
	private String description;
	private String articleText;
	private String source;
	private Date pubDate;
	
	public Bundle getBundle()
	{
		Bundle b = new Bundle();
		b.putString("title", title);
		b.putString("url", link);
		b.putString("description", description);
		b.putString("articleText", articleText);
		b.putString("source", source);
		b.putSerializable("pubDate", pubDate);
		
		return b;
	}
	public NewsArticle() {}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getLink() {
		return link.substring(link.indexOf('*')+1);
	}
	public void setLink(String link) {
		this.link = link;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}

	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
	public String getArticleText() {
		return articleText;
	}
	public void setArticleText(String articleText) {
		this.articleText = articleText;
	}
	public Date getPubDate() {
		return pubDate;
	}
	public void setPubDate(Date pubDate) {
		this.pubDate = pubDate;
	}
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(title);
		dest.writeString(description);
		dest.writeString(articleText);
		dest.writeString(source);
		dest.writeSerializable(pubDate);
		
		
	}
	public NewsArticle(Parcel in)
	{
		title = in.readString();
		description = in.readString();
		articleText = in.readString();
		source = in.readString();
		pubDate = (Date) in.readSerializable();
	}
	 public static final Parcelable.Creator<NewsArticle> CREATOR = new Parcelable.Creator<NewsArticle>() 
		     {
				  public NewsArticle createFromParcel(Parcel in) 
				  {
					 return new NewsArticle(in);
				  }

				@Override
				public NewsArticle[] newArray(int size) {
					return new NewsArticle[size];
				}
		     };

}
