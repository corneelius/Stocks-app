package actionbar;

import home.HomeScreen.HomeScreenActions;
import android.graphics.drawable.Drawable;

public class Action {
	private HomeScreenActions mAction;
	private Drawable mDrawable;
	private boolean activated;
	private boolean lock;
	
	public void setActivate(boolean tf)
	{
		activated=tf;
	}
	public boolean getActivate()
	{
		return activated;
	}
	
	public Action(HomeScreenActions mAction, Drawable drawable)
	{
		this.mAction = mAction;
		mDrawable = drawable;
	}
	public void setDrawable(Drawable drawable)
	{
		mDrawable = drawable;
	}
    public Drawable getDrawable()
    {
    	return mDrawable;
    }
    public HomeScreenActions getAction()
    {
    	return mAction;
    }
    public void setLock(boolean lock)
    {
    	this.lock=lock;
    }
    public boolean getLock()
    {
    	return lock;
    }
}