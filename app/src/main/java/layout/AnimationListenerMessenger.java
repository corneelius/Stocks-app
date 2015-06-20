package layout;

import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;

public class AnimationListenerMessenger implements AnimationListener{
	private AnimationListener mListener;
	
	public void setAnimationListener(AnimationListener list)
	{
		mListener=list;
	}

	@Override
	public void onAnimationStart(Animation animation) {
		if(mListener!=null)
			mListener.onAnimationStart(animation);
		
	}

	@Override
	public void onAnimationEnd(Animation animation) {
		if(mListener!=null)
			mListener.onAnimationEnd(animation);
		
	}

	@Override
	public void onAnimationRepeat(Animation animation) {
		if(mListener!=null)
			mListener.onAnimationRepeat(animation);
		
	}

}
