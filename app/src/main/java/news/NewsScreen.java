package news;

import home.HomeScreen;
import home.HomeScreen.HomeScreenActions;
import nr.app.R;
import actionbar.Action;
import actionbar.ActionBar;
import actionbar.ActionReceiver;
import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class NewsScreen extends Activity implements ActionReceiver{
	private Bundle bundle;
	private WebView mWebView;
	private ActionBar mActionBar;
	private Resources r;
	
	
	@Override
	public void onCreate(Bundle onSavedInstanceState)
	{
		super.onCreate(onSavedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.newsscreen2);
		
		r = getResources();
		
		mActionBar = (ActionBar)findViewById(R.id.newsscreen_actionbar);
		mActionBar.setParent(this);
		mActionBar.setReturnAction(new Action(HomeScreen.HomeScreenActions.BACK_HOMESCREEN, r.getDrawable(R.drawable.previous_item)));
		mActionBar.setRightIndeterminateProgressBar(true);
		mActionBar.setIconOn(true);
		
		bundle = getIntent().getBundleExtra("news_article");
			mActionBar.setProgressBarVisibility(View.VISIBLE);
			mActionBar.setProgressBarProgress(0);
		mWebView = (WebView)findViewById(R.id.newsscreen_webview);
			
		mWebView.setWebChromeClient(new WebChromeClient()
		{
			@Override
			public void onProgressChanged(WebView v, int progress)
			{
				if(progress > 99)
				{
					mActionBar.setProgressBarVisibility(View.INVISIBLE);  //or GONE
				}
				else
				{
					mActionBar.setProgressBarProgress(progress);
				}
			}
		});
		
		mWebView.setWebViewClient(new WebViewClient()
		{
			@Override
			public void onPageFinished(WebView view, String url)
			{
				super.onPageFinished(view, url);
				mActionBar.setRightIndeterminateProgressBar(false);
			}
			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon)
			{
				super.onPageStarted(view, url, favicon);
				mActionBar.setRightIndeterminateProgressBar(true);
			}
			@Override
			public void onReceivedError(WebView view, int errorCode, String description, String failingUrl)
			{
				super.onReceivedError(view, errorCode, description, failingUrl);
				mActionBar.setRightIndeterminateProgressBar(false);
				Toast.makeText(NewsScreen.this, "Website Error", Toast.LENGTH_SHORT).show();
				onBackPressed();
			}
			 @Override
			    public boolean shouldOverrideUrlLoading(WebView view, String url) {
			        view.loadUrl(url);
			        return false;
			    }
		});
		
		mWebView.loadUrl(bundle.getString("url"));
		mWebView.getSettings().setJavaScriptEnabled(true);
	}

	@Override
	public void onBackPressed()
	{
		super.onBackPressed();
	}
	
	@Override
	public void act(HomeScreenActions action) {
		switch(action)
		{
		
		case BACK_HOMESCREEN:
		{
			onBackPressed();
		}break;
		
		}
		
	}


	
}
