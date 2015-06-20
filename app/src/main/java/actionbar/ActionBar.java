package actionbar;

import home.HomeScreen.HomeScreenActions;

import java.util.ArrayList;

import nr.app.R;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

public class ActionBar extends RelativeLayout implements OnTouchListener{
	private ArrayList<Action> mActions;
	private int backgroundColor;
	private Resources r;
	private Drawable actionPressed;
	private Drawable mIcon;
	private boolean iconOn=false;
	private boolean[] buttonFlips;
	private ActionReceiver parent;
	private Action returnAction;
	private ProgressBar mProgressBar;
	private int iconMargin;
	private float mDensityMultiplier;
	private boolean refreshProgressBarRunning=false;
	private ProgressBar mIndeterminateProgressBar;
	private ProgressBar mIndeterminateProgressBarRight;
	private boolean touchEnabled=true;

	private ButtonView mButtonView;
	
	public ActionBar(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		r = context.getResources();
		mDensityMultiplier = getResources().getDisplayMetrics().density;
		backgroundColor = Color.rgb(51, 181, 229);
		actionPressed = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[] {Color.rgb(51, 181, 229), Color.rgb(81, 211, 255), Color.rgb(140, 210, 255) });
		mIcon = r.getDrawable(R.drawable.ic_launcher);
		mIndeterminateProgressBar = new ProgressBar(getContext())
		{
			@Override
			public boolean onTouchEvent(MotionEvent e)
			{
				return true;
			}
		};
		mIndeterminateProgressBar.setIndeterminate(true);
		RelativeLayout.LayoutParams params5 = new RelativeLayout.LayoutParams((int)(r.getDimension(R.dimen.actionbar_height)/1.5f), (int)(r.getDimension(R.dimen.actionbar_height)/1.5f));       
		params5.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		params5.addRule(RelativeLayout.CENTER_VERTICAL);
		params5.leftMargin=(int) (10*mDensityMultiplier);
		mIndeterminateProgressBar.setLayoutParams(params5);
		mIndeterminateProgressBar.setVisibility(View.INVISIBLE);
		addView(mIndeterminateProgressBar);
		
		
			
		
		mProgressBar = (ProgressBar)(((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.actionbar_progressbar2, null));
			mProgressBar.setId(R.id.actionbar_progressbar2);
			mProgressBar.setMax(100);
			mProgressBar.setInterpolator(new AccelerateInterpolator());
			mProgressBar.setVisibility(View.INVISIBLE); //never GONE
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, 5);
			params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
			mProgressBar.setLayoutParams(params);
			mProgressBar.setIndeterminate(false);
			addView(mProgressBar);
			
		mButtonView = new ButtonView(getContext());
			mButtonView.setVisibility(View.VISIBLE); //never GONE
			RelativeLayout.LayoutParams params2 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			params2.addRule(RelativeLayout.ABOVE, R.id.actionbar_progressbar2);
			params2.addRule(RelativeLayout.ALIGN_PARENT_TOP);
			mButtonView.setLayoutParams(params2);
			addView(mButtonView);
		
		mIndeterminateProgressBar.bringToFront();
		iconMargin = (int) (mDensityMultiplier*7);
	}
	public void setRightIndeterminateProgressBar(boolean b)
	{
		if(mIndeterminateProgressBarRight==null && b)
		{
			mIndeterminateProgressBarRight = new ProgressBar(getContext());
			mIndeterminateProgressBarRight.setIndeterminate(true);
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams((int)(r.getDimension(R.dimen.actionbar_height)/1.6), (int)(r.getDimension(R.dimen.actionbar_height)/1.6));
			params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
			params.addRule(RelativeLayout.CENTER_VERTICAL);
			params.rightMargin=(int)(15*mDensityMultiplier);
			mIndeterminateProgressBarRight.setLayoutParams(params);
		}
		if(b)
		{
			if((mIndeterminateProgressBarRight.getParent())!=null) ((RelativeLayout)(mIndeterminateProgressBarRight.getParent())).removeView(mIndeterminateProgressBarRight);
			addView(mIndeterminateProgressBarRight);
			mIndeterminateProgressBarRight.setVisibility(View.VISIBLE);
		}
		else if(mIndeterminateProgressBarRight!=null)
		{
			mIndeterminateProgressBarRight.setVisibility(View.GONE);
			removeView(mIndeterminateProgressBarRight);
		}
	}
	public void enableTouch()
	{
		touchEnabled=true;
	}
	public void disableTouch()
	{
		touchEnabled=false;
	}
	public void setIconOn(boolean tf)
	{
		iconOn=tf;
		mButtonView.invalidate();
	}
	public void unlockAll()
	{
		for(Action a : mActions)
		{
			a.setLock(false);
		}
	}
	public void lock(HomeScreenActions action)
	{
		for(Action a : mActions)
		{
			if(a.getAction().equals(action))
				a.setLock(true);
		}
	}
	public int getProgressBarProgress()
    {
    	return mProgressBar.getProgress();
    }
	 public void setProgressBarProgress(int progress)
	 {
	    	if(progress>mProgressBar.getMax())
	    		mProgressBar.setProgress(mProgressBar.getMax());
	    	else if(progress<0)
	    		mProgressBar.setProgress(0);
	    	else
	    		mProgressBar.setProgress(progress);
	 }
	 public void getCache(int width, int height, int x, Bitmap bitmap)
	 {
		 Bitmap b = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		 Canvas c = new Canvas(b);
		 mButtonView.drawBar(c, width, height, true);
		 
		 Canvas returnCanvas = new Canvas(bitmap);
		 returnCanvas.drawBitmap(b, x, 0, new Paint());
	 }
	public void setReturnAction(Action action) 
    {
        returnAction = action;
        if(buttonFlips==null)
        {
        	buttonFlips=new boolean[1];
        }

        mButtonView.invalidate();
        
    }
	 public void disableReturnButton()
	 {
	    returnAction=null;
	    mButtonView.invalidate();
	 }
	 public void setProgressBarVisibility(int visibility) {
	    	//default value is View.INVISIBLE
	        mProgressBar.setVisibility(visibility);
	        mProgressBar.setProgress(1);
	    }
	public void setParent(ActionReceiver parent)
	{
		this.parent=parent;
	}
	
	private boolean[] increaseBooleanArraySize(boolean[] b1, int size)
	{
		if(b1==null)
			return new boolean[size];
		
		//only works if size is bigger than b1
		if(!(size >= b1.length))
			return null;
		
		boolean[] b2 = new boolean[size];
		for(int i=0; i<b1.length; i++)
		{
			b2[i] = b1[i];
		}
		return b2;
	}
	
	public void setActions(ArrayList<Action> actions) 
	{
		if(actions.size() > 3) //can only have three buttons
			return;
		mActions = actions;
		buttonFlips = increaseBooleanArraySize(buttonFlips, actions.size()+1); //extra for return button
	}

	
	public class ButtonView extends View
	{
	public ButtonView(Context context) {
			super(context);
			// TODO Auto-generated constructor stub
		}

	@Override
	public void onDraw(Canvas canvas)
	{
		drawBar(canvas, this.getWidth(), this.getHeight(), false);
		
	}
	private void drawBar(Canvas canvas, int width, int height, boolean forceDormant)
	{
		Paint paint = new Paint();
		paint.setColor(backgroundColor);
		canvas.drawRect(0, 0, canvas.getWidth(), height, paint);
		GradientDrawable d = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[] {Color.rgb(70, 70, 70), Color.rgb(235, 235, 235)});
		d.setBounds(0, height-5, width, height);
		d.draw(canvas);
		
		//return button
		if(returnAction !=null && (!refreshProgressBarRunning || forceDormant) && buttonFlips[0] && iconOn)
		{
			actionPressed.setBounds(0, 0, (int)((3.0/2)*height), height);
			actionPressed.draw(canvas);
			mIcon.setBounds(0, (int)((1.0/8)*height), (int)((3.0/4)*height), (int)((7.0/8)*height));
			mIcon.draw(canvas);
			returnAction.getDrawable().setBounds((int)((1.0/2)*height), 0, (int)((3.0/2)*height), height);
			returnAction.getDrawable().draw(canvas);
		}
		else if(returnAction !=null && (!refreshProgressBarRunning || forceDormant) && buttonFlips[0])
		{
			actionPressed.setBounds(0, 0, height, height);
			actionPressed.draw(canvas);
			returnAction.getDrawable().setBounds(0, 0, height, height);
			returnAction.getDrawable().draw(canvas);
		}
		else if(returnAction !=null && (!refreshProgressBarRunning || forceDormant) && iconOn)
		{
			mIcon.setBounds(0, (int)((1.0/8)*height), (int)((3.0/4)*height), (int)((7.0/8)*height));
			mIcon.draw(canvas);
			returnAction.getDrawable().setBounds((int)((1.0/2)*height), 0, (int)((3.0/2)*height), height);
			returnAction.getDrawable().draw(canvas);
		} 
		else if(returnAction !=null && (!refreshProgressBarRunning || forceDormant))
		{
			returnAction.getDrawable().setBounds(0, 0, height, height);
			returnAction.getDrawable().draw(canvas);
		} 
		
		else if(returnAction == null) //return action is square
		{
			actionPressed.setBounds(0, 0, height, height);
			actionPressed.draw(canvas);
		}
		
		//actions
		if(mActions != null && mActions.size()!=0)
		{
		
				//draw selected background's for icons
			
			for(int i=1; i<buttonFlips.length; i++)
			{
				if(buttonFlips[i])
				{
					actionPressed.setBounds((int) (width - (height*i)), 0, (int) (width - (height*(i-1))), height);
					actionPressed.draw(canvas);
				}
			}
			
			//draw icons
			for(int i=0; i<mActions.size(); i++)
			{
				int left=(int) (width-(height*(i+1)));
				int right=(int) (width-(height*i));
				int diff = right-left;
				int gap = (diff - height)/2;
				mActions.get(i).getDrawable().setBounds( left+gap+iconMargin, 0+iconMargin, right-gap-iconMargin, height-iconMargin);
				mActions.get(i).getDrawable().draw(canvas);
			}
		}
		
		
	}
	}
	public void setRefreshProgressBarRunning(boolean tf)
	{
		refreshProgressBarRunning=tf;
		mButtonView.invalidate();
		if(tf)
			mIndeterminateProgressBar.setVisibility(View.VISIBLE);
		else
			mIndeterminateProgressBar.setVisibility(View.INVISIBLE);
			
	}
	public boolean getRefreshProgressBarRunning()
	{
		return refreshProgressBarRunning;
	}
	

	public void setFlip(int id, boolean tf)
	{
		if(id >= 0 && id <= buttonFlips.length)
		{
			buttonFlips[id]=tf;
			invalidate();
		}
	}
	public void turnActionOff(HomeScreenActions action)
	{
		for(int i=0; i<mActions.size(); i++)
		{
			if(action==mActions.get(i).getAction())
			{
				buttonFlips[i+1]=false;
			}
		}
	}
	
	private Action lastDown;
	private int lastDownIndex;
	
	public void reset()
	{
		for(int i=0; i<buttonFlips.length; i++)
		{
			buttonFlips[i] = false;
		}
		mButtonView.invalidate();
	}
	private boolean actionIsNotLocked(HomeScreenActions action)
	{
		for(Action a : mActions)
		{
			if(a.getAction().equals(action) && !a.getLock())
				return true;
		}
		return false;
	}
	@Override
	public boolean onTouchEvent(MotionEvent e)
	{
		if(!touchEnabled)
			return true;
		
		if(e.getAction() == MotionEvent.ACTION_DOWN)
		{
			if(mActions!=null && e.getX() > getHeight() && (int) (((getWidth() - e.getX())/getHeight())) < (mActions==null ? 0 : mActions.size()) )
			{
				lastDown = mActions.get((int) ((getWidth() - e.getX())/getHeight()));
				lastDownIndex = (int) ((getWidth() - e.getX())/getHeight())+1;
				Action action = mActions.get((int) ((getWidth() - e.getX())/getHeight()));
				if(mActions.get((int) ((getWidth() - e.getX())/getHeight())).getActivate())
				{
					if(actionIsNotLocked(action.getAction())) 
					{
						parent.act(action.getAction());
						buttonFlips[(int) ((getWidth() - e.getX())/getHeight())+1]=false;
					}
					else
					{
						//action.setActivate(false);
					}
				}
				else
				{
					if(actionIsNotLocked(action.getAction())) 
					{
						parent.act(action.getAction());
						buttonFlips[(int) ((getWidth() - e.getX())/getHeight())+1]=true;
					}
					else
					{
						//action.setActivate(true);
					}
				}
			}

			else if(returnAction!=null && e.getX() < (iconOn ? this.getHeight()*1.5 : this.getHeight()) ) //return button pressed
			{
				lastDown = returnAction;
				lastDownIndex=0;
				buttonFlips[0]=true;
				mButtonView.invalidate();
				if(returnAction instanceof IntentAction)
				{
					this.getContext().startActivity(((IntentAction) returnAction).getIntentAction());
				}
				else
				{
					parent.act(returnAction.getAction());
				}
			}
		}
		if(e.getAction() == MotionEvent.ACTION_UP || e.getAction() == MotionEvent.ACTION_CANCEL && (int) (((getWidth() - e.getX())/getHeight())) < (mActions==null ? 0 : mActions.size()) )
		{
			buttonFlips[0]=false;
			String s=null;

			if(lastDown!=null && lastDown.getAction()==null)
			{
				getContext().startActivity(((IntentAction)(lastDown)).getIntentAction());
				buttonFlips[lastDownIndex] = false;
			}
			else if(lastDown!=null)
			{
				s = lastDown.getAction().toString();

				if( s.substring(s.indexOf('2')+1).equals("B") )
				{
					buttonFlips[lastDownIndex] = false;
				}
			}
		}
		mButtonView.invalidate();
		return true;
	}
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		return false;
	}


}
