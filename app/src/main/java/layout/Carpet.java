package layout;

import actionbar.ActionBar;
import android.content.Context;
import android.graphics.Color;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;

public class Carpet extends FrameLayout{
//stateless
	public Carpet(Context context) {
		super(context);
		this.setBackgroundColor(Color.argb(225, 241, 241, 241));
		
		
	}
	public void removeAllContent()
	{
		removeAllViews();
	}
	
	public void setContent(View v)
	{
		this.removeView(v);
		FrameLayout.LayoutParams params= new FrameLayout.LayoutParams(FrameLayout.LayoutParams.FILL_PARENT, FrameLayout.LayoutParams.FILL_PARENT);
		v.setLayoutParams(params);
		addView(v);
		
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent e)
	{
		super.onTouchEvent(e);
		return true;
	}
	
	public AnimationListenerMessenger open()
	{
		clearAnimation();
		ScaleAnimation sAnim = new ScaleAnimation(1, 1, 1, (((ViewGroup) getParent()).getHeight()-getActionBar((ViewGroup) getParent()).getHeight())/1, 0, 0 );
		sAnim.setInterpolator(new DecelerateInterpolator());
		sAnim.setDuration(300);
		sAnim.setFillAfter(true);
		AnimationListenerMessenger list = new AnimationListenerMessenger();
		sAnim.setAnimationListener(list);
		startAnimation(sAnim);
		
		
		
		return list;
	}
	public AnimationListenerMessenger close()
	{
		clearAnimation();
		float f = (1.0f/((float)((ViewGroup) getParent()).getHeight()-(float)getActionBar((ViewGroup) getParent()).getHeight()));
		ScaleAnimation sAnim = new ScaleAnimation(1, 1, 1, f, Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0 );
		sAnim.setInterpolator(new AccelerateInterpolator());
		sAnim.setDuration(350);
		sAnim.setFillAfter(true);
		AnimationListenerMessenger list = new AnimationListenerMessenger();
		sAnim.setAnimationListener(list);
		startAnimation(sAnim);
		
		
		return list;
	}
	
	private ActionBar getActionBar(ViewGroup parent)
	{
		for(int i=0; i<parent.getChildCount(); i++)
		{
			if(parent.getChildAt(i) instanceof ActionBar)
				return (ActionBar) parent.getChildAt(i);
		}
		return null;
	} 
	
	public void removeAllViews()
	{
		super.removeAllViews();
	}

}
