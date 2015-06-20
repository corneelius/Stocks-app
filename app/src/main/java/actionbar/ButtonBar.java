package actionbar;

import home.HomeScreen;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.MotionEvent;
import android.view.View;

public class ButtonBar extends View{
	private HomeScreen parent;
	private ArrayList<Action> mActions;
	private boolean[] mFlips;
	private float[] lefts;
	private float spacing;
	private GradientDrawable background;
	int backgroundColor = Color.rgb(25, 21, 20);
	private int iconMargin;
	public ButtonBar(Context context) {
		super(context);
		mActions = new ArrayList<Action>();
		setBackgroundColor(backgroundColor);
		background = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, new int[] {Color.argb(200, 63, 66, 65), Color.argb(255, 63, 66, 65) });
		iconMargin=15;
	}
	
	public void resetButtons()
	{
		for(int i=0; i<mFlips.length; i++)
		{
			mFlips[i]=false;
		}
		invalidate();
	}
	
	public void resetButtons(HomeScreen.HomeScreenActions otherThan)
	{
		for(int i=0; i<mFlips.length; i++)
		{
			if(!(mActions.get(i).getAction()==otherThan))
				mFlips[i]=false;
		}
		invalidate();
	}
	
	public void setHomeScreen(HomeScreen parent)
	{
		this.parent=parent;
	}
	public void addAction(Action action)
	{
		mActions.add(action);
		mFlips = increaseBooleanArraySize(mFlips, mActions.size());
		lefts = new float[mActions.size()];
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
	
	
	@Override
	public void onDraw(Canvas canvas)
	{
		spacing = (getWidth()-(getHeight()*mActions.size()))/(mActions.size()+1);

		
		for(int i=0; i<lefts.length; i++)
		{
			lefts[i] = spacing + (i*spacing) + (i*getHeight());
		}
	
		canvas.drawColor(backgroundColor);
		for(int i=0; i<mActions.size(); i++)
		{
			if(mFlips[i])
			{
				background.setBounds((int)(lefts[i]) , 0, (int)(lefts[i]+getHeight()) , getHeight());
				background.draw(canvas);
			}
			mActions.get(i).getDrawable().setBounds((int)((lefts[i]) + iconMargin), iconMargin, (int)(lefts[i]+getHeight()-iconMargin) , getHeight()-iconMargin);
			mActions.get(i).getDrawable().draw(canvas);
		}
	}
	
	private Action lastDown;
	private int lastDownIndex;
	@Override
	public boolean onTouchEvent(MotionEvent e)
	{
		int index = getIndex(e.getX());
		if(e.getAction()==MotionEvent.ACTION_DOWN)
		{
			if(index != -1)
			{
				HomeScreen.HomeScreenActions action = mActions.get(index).getAction();
				mFlips[index] = !mFlips[index];
				
				invalidate();
				lastDown = mActions.get(index);
				lastDownIndex = index;
				parent.act(action);
			}
		}
		else if(e.getAction()==MotionEvent.ACTION_UP || e.getAction()==MotionEvent.ACTION_CANCEL)
		{
			String s = lastDown.getAction().toString();
			if( s.substring(s.indexOf('2')+1).equals("B") )
			{
				mFlips[lastDownIndex] = false;
				invalidate();
			}
		}
		return true;
	}
	
	private int getIndex(float x)
	{
		for(int i=0; i<lefts.length; i++)
		{
			if(x > lefts[i] && x < (lefts[i]+getHeight()))
				return i;
		}
		return -1;
	}

}
