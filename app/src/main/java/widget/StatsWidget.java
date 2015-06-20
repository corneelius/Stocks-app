package widget;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.VelocityTracker;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import animation.TimeAnimator;
import data.Stats;

public class StatsWidget extends SurfaceView implements SurfaceHolder.Callback{
	private Typeface mNameTypeface;
	private Typeface mValueTypeface;
	private StatsListAdapter mStatsListAdapter;
	private SurfaceHolder mHolder;
	private ArrayList<Bitmap> mBitmaps;
	private int backgroundColor;
	private int textColor;
	private StatsWidgetSetupHelper setupHelper;
	private float mDensityMultiplier;
	private int horizontalMargin;
	private int verticalMargin;
	private Display mDisplay;
	private volatile int bitmapIndex;  //the index of the bitmap we are currently showing
	private VelocityTracker mVelocityTracker;
	private volatile boolean touchIsDisabled;
	private Bitmap statusBitmap;
	private int circleColor;
	private float circleRadius;
	private int highlightedCircleColor;
	private int interpolatorFloor;
	private int placeIndicatorHeight;
	private DecelerateInterpolator dInterpolator = new DecelerateInterpolator();
	private int ANIMATION_LENGTH=600;
	
	private Paint recycledPaint;
	
	private float interpolatorFactor=.9f;
	private float interpolatorDivisor=.9f;
	private float interpolatorMultiplier=.9f;
		
	//String name is left justified, and String value is right justified
	

	public StatsWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		setBackgroundColor(Color.WHITE);
		
		mNameTypeface = Typeface.createFromAsset(getContext().getAssets(), "fonts/Roboto-BoldCondensed.ttf");
		mValueTypeface = Typeface.createFromAsset(getContext().getAssets(), "fonts/Roboto-Thin.ttf");
		mBitmaps = new ArrayList<Bitmap>();
		mHolder = getHolder();
		mHolder.addCallback(this);
		backgroundColor = Color.argb(255, 255, 255, 255); //default is white
		circleColor = Color.argb(255, 207, 207, 207);  //go to 333333
		highlightedCircleColor = Color.argb(255, 33, 33, 33);
		textColor = Color.BLACK;
		setupHelper = new StatsWidgetSetupHelper();
		mDensityMultiplier = getContext().getResources().getDisplayMetrics().density;
		mDisplay = (Display) ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		horizontalMargin = (int) (mDisplay.getWidth() * .06);
		verticalMargin = (int) (mDisplay.getHeight() * .02);
		bitmapIndex = -1;
		touchIsDisabled = false;
		circleRadius = 6f;
		interpolatorFloor = 10;
		recycledPaint = new Paint();
		
		placeIndicatorHeight = verticalMargin;

	}
	private int toPixels(int dip)
	{
		return (int) ((dip * mDensityMultiplier) + .5); //.5 for rounding
	}
	public StatsWidget(Context context)
	{
		this(context, null);
	}
	
	private class StatsWidgetSetupHelper extends Thread
	{
		

		@Override
		public void run() {
			Paint namePaint = getNameTextPaint();
			Paint valuePaint = getValueTextPaint();
			
			String longestString = mStatsListAdapter.getLongestString();
			int asterik = longestString.indexOf("*");
			setTextSize(namePaint, valuePaint, (longestString.substring(0, asterik)),
						(longestString.substring(asterik+1, longestString.length())), getWidth()-(2*horizontalMargin));
			
			createBitmaps(namePaint, valuePaint);
			
			createStatusBitmap(0);
			
			bitmapIndex = 0;
			Canvas surface=null;
			while(surface==null)
				surface = getHolder().lockCanvas();
			surface.drawBitmap(mergedBitmaps(), 0f, 0f, recycledPaint);
			getHolder().unlockCanvasAndPost(surface);
			touchIsDisabled = false;
			
		}
		private void createStatusBitmap(int index)
		{
			if(index >= mBitmaps.size())
			{
				index = mBitmaps.size()-1;
			}
			
			statusBitmap = Bitmap.createBitmap(getWidth(), placeIndicatorHeight, Bitmap.Config.ARGB_8888);
			Canvas canvas = new Canvas(statusBitmap);
			canvas.drawARGB(Color.alpha(backgroundColor), Color.red(backgroundColor), Color.green(backgroundColor), Color.blue(backgroundColor)); //matching background
			float sideMargin = (1f/6f)*getWidth();
			float space = getWidth()-2*(sideMargin);
			float spaceForEach = space / mBitmaps.size();
			float y = verticalMargin / 2f;
			
			for(int i=0;i<mBitmaps.size();i++)
			{
				if(i==index)
				{
					canvas.drawCircle(((sideMargin+(spaceForEach/2f)) + spaceForEach*i), y, circleRadius, getCirclePaint(highlightedCircleColor)); 
				}
				else
				{
				canvas.drawCircle(((sideMargin+(spaceForEach/2f)) + spaceForEach*i), y, circleRadius, getCirclePaint(circleColor)); 
				}
			}
		}
		
		private void setTextSize(Paint namePaint, Paint valuePaint, String name, String value, int mMaximumWidth)
		{
			//find proper text size
			int maximumTextSize = getWidth();
			int padding = 15;
			for(namePaint.setTextSize(maximumTextSize); namePaint.measureText(name) > (mMaximumWidth/2)-padding;)
			{
				namePaint.setTextSize(namePaint.getTextSize()-5);
			}
			for(valuePaint.setTextSize(maximumTextSize);valuePaint.measureText(value) > (mMaximumWidth/2)-padding;)
			{
				valuePaint.setTextSize(valuePaint.getTextSize()-5);
			}

		}
		
		private void createBitmaps(Paint namePaint, Paint valuePaint)
		{
			int itemPadding = toPixels(5);
			
			int largestPaintTextSize = (int) Math.max(namePaint.getTextSize(), valuePaint.getTextSize());
			
			namePaint.setTextSize(namePaint.getTextSize()-itemPadding);
			valuePaint.setTextSize(valuePaint.getTextSize()-itemPadding);
			valuePaint.setTextSize(Math.min(namePaint.getTextSize(), valuePaint.getTextSize()));
			
			int mHeight = getHeight()-(2*verticalMargin);
			
			int numPerPage = (mHeight-largestPaintTextSize)/largestPaintTextSize; //rounded to lower int
			int numPages = (int) ((mStatsListAdapter.getSize() / (double)numPerPage) + .99);
			
			//LAYOUT
			int left = 0+horizontalMargin;
			int top = 0+verticalMargin;
			int right = getWidth()-horizontalMargin;
			//LAYOUT
			
			
			System.gc();
			for(int i=0;i<numPages;i++)
			{
				Bitmap b1 = Bitmap.createBitmap(getWidth(), getHeight()-placeIndicatorHeight, Bitmap.Config.ARGB_8888);
				Canvas canvas = new Canvas(b1);
				canvas.drawARGB(Color.alpha(backgroundColor), Color.red(backgroundColor), Color.green(backgroundColor), Color.blue(backgroundColor)); //matching background
				
				float xOffset=toPixels(10);
				for(int x=0;x<numPerPage;x++)
				{
				canvas.drawText((mStatsListAdapter.getStatAt((numPerPage*i)+x))[0], left+xOffset, top+((x+1)*(largestPaintTextSize)), namePaint);
				canvas.drawText((mStatsListAdapter.getStatAt((numPerPage*i)+x))[1], ((left+right)/2)+xOffset, top+((x+1)*(largestPaintTextSize)), valuePaint);
				
				if((numPerPage*i)+(x+1) >= mStatsListAdapter.getSize()) break;
				}
				mBitmaps.add(b1);
			}
		}	
	}
	
	public void initialize(Stats stats)
	{
		this.mStatsListAdapter = new StatsListAdapter(stats);
		
		//start setting up bitmaps
		mBitmaps.clear();
		setupHelper = new StatsWidgetSetupHelper();
		setupHelper.start();		
	}

	private Paint getCirclePaint(int color1, int color2, int percentageRatio)
	{
		int alpha1 = Color.alpha(color1);
		int red1 = Color.red(color1);
		int green1 = Color.green(color1);
		int blue1 = Color.blue(color1);
		
		int alpha2 = Color.alpha(color2);
		int red2 = Color.red(color2);
		int green2 = Color.green(color2);
		int blue2 = Color.blue(color2);
		
		int alphaR = (int) (alpha1 + ((percentageRatio/100.0)*(alpha2-alpha1)));
		int redR = (int) (red1 + ((percentageRatio/100.0)*(red2-red1)));
		int greenR = (int) (green1 + ((percentageRatio/100.0)*(green2-green1)));
		int blueR = (int) (blue1 + ((percentageRatio/100.0)*(blue2-blue1)));
		
		return getCirclePaint(Color.argb(alphaR, redR, greenR, blueR));
	}
	private Paint getCirclePaint(int color)
	{
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setStyle(Paint.Style.FILL);
		paint.setColor(color);
		
		return paint;
	}

	private Paint getNameTextPaint()
	{
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setStyle(Paint.Style.FILL);
		paint.setColor(textColor);
		paint.setTypeface(mNameTypeface);
		
		return paint;
	}
	private Paint getValueTextPaint()
	{
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setStyle(Paint.Style.FILL);
		paint.setColor(textColor);
		paint.setTypeface(mValueTypeface);
		
		return paint;
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) { 
		touchIsDisabled = true;
		mHolder = holder;
		mVelocityTracker = VelocityTracker.obtain();
		Canvas canvas = holder.lockCanvas();
		canvas.drawARGB(Color.alpha(backgroundColor), Color.red(backgroundColor), Color.green(backgroundColor), Color.blue(backgroundColor));  //draw matching background
		holder.unlockCanvasAndPost(canvas);
		
		if(mBitmaps.size()>0)
		{
			bitmapIndex = 0;
			Canvas surface = getHolder().lockCanvas();
			surface.drawBitmap(mergedBitmaps(), 0f, 0f, recycledPaint);
			getHolder().unlockCanvasAndPost(surface);
			touchIsDisabled = false;
		}
		
		
	}
	private Bitmap mergedBitmaps()
	{
		Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		
		canvas.drawBitmap(mBitmaps.get(0), new Rect(0, 0, getWidth(), getHeight()-placeIndicatorHeight), new Rect(0, placeIndicatorHeight, getWidth(), getHeight()), recycledPaint);
		canvas.drawBitmap(statusBitmap, new Rect(0, 0, getWidth(), placeIndicatorHeight), new Rect(0, 0, getWidth(), placeIndicatorHeight), recycledPaint);
		
		return bitmap;
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		statusBitmap = Bitmap.createBitmap(getWidth(), verticalMargin, Bitmap.Config.ARGB_8888);
		
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		mVelocityTracker.recycle();
		
	}
	@Override
	public void onMeasure(int width, int height)
	{
		setMeasuredDimension(MeasureSpec.getSize(width), MeasureSpec.getSize(height));
	}
	
	private Bitmap createStatusBitmap(int leavingIndex, int newIndex, int percentThere) 
	{
		
		Canvas canvas = new Canvas(statusBitmap);
		canvas.drawARGB(Color.alpha(backgroundColor), Color.red(backgroundColor), Color.green(backgroundColor), Color.blue(backgroundColor)); //matching background
		float sideMargin = (1f/6f)*getWidth();
		float space = getWidth()-2*(sideMargin);
		float spaceForEach = space / mBitmaps.size();
		float y = verticalMargin / 2f;
		
		for(int i=0;i<mBitmaps.size();i++)
		{
			if(i==newIndex)
			{
				canvas.drawCircle(((sideMargin+(spaceForEach/2f)) + spaceForEach*i), y, circleRadius, getCirclePaint(circleColor, highlightedCircleColor, percentThere)); 
			}
			else if(i==leavingIndex)
			{
				canvas.drawCircle(((sideMargin+(spaceForEach/2f)) + spaceForEach*i), y, circleRadius, getCirclePaint(circleColor, highlightedCircleColor, (100 - percentThere)));
			}
			else
			{
			canvas.drawCircle(((sideMargin+(spaceForEach/2f)) + spaceForEach*i), y, circleRadius, getCirclePaint(circleColor)); 
			}
		}
		return statusBitmap;  //convenience
	}
	private class RightShiftCompleter extends Thread implements TimeAnimator.TimeListener
	{
		TimeAnimator mAnimator;
		int switchPoint;
		int pixelsTo;

		public RightShiftCompleter()
		{
			mAnimator = new TimeAnimator(this);
		}

		@Override
		public void run() 
		{
			if(bitmapIndex != 0)
			{
				touchIsDisabled = true;
				switchPoint = (int) intoShift;
				pixelsTo = getWidth() - switchPoint;

				mAnimator.setDuration(ANIMATION_LENGTH);
				mAnimator.prepare();
				mAnimator.start();

			}
		}


		@Override
		public void onTimeUpdate(TimeAnimator animation, long totalTime,
				long deltaTime) {

			if(deltaTime==totalTime && bitmapIndex!=0)
			{
				Canvas canvas = getHolder().lockCanvas();
				if(canvas==null) return;
				canvas.drawBitmap(mBitmaps.get(bitmapIndex-1), new Rect(0, 0, getWidth(), getHeight()-placeIndicatorHeight), new Rect(0, placeIndicatorHeight, getWidth(), getHeight()), recycledPaint);
				canvas.drawBitmap(createStatusBitmap(bitmapIndex, bitmapIndex-1, 100), new Rect(0, 0, getWidth(), verticalMargin), new Rect(0, 0, getWidth(), placeIndicatorHeight), recycledPaint);
				getHolder().unlockCanvasAndPost(canvas);
				bitmapIndex--;
				touchIsDisabled = false;
				return;
			}
			float fr = dInterpolator.getInterpolation((float) ((double)deltaTime/totalTime));
			rightShift(switchPoint+((int)(fr * pixelsTo)));
		}
		
	}
	private class LeftShiftCompleter extends Thread implements TimeAnimator.TimeListener
	{
		TimeAnimator mAnimator;
		int switchPoint;
		int pixelsTo;

		public LeftShiftCompleter()
		{
			mAnimator = new TimeAnimator(this);
		}

		@Override
		public void run() 
		{
			if(bitmapIndex != mBitmaps.size()-1)
			{
				touchIsDisabled = true;
				switchPoint = (int) intoShift;
				pixelsTo = getWidth() - switchPoint;

				mAnimator.setDuration(ANIMATION_LENGTH);
				mAnimator.prepare();
				mAnimator.start();

			}
		}


		@Override
		public void onTimeUpdate(TimeAnimator animation, long totalTime,
				long deltaTime) {
			if(deltaTime==totalTime && bitmapIndex!=mBitmaps.size()-1)
			{
				Canvas canvas = getHolder().lockCanvas();
				if(canvas==null) return;
				canvas.drawBitmap(mBitmaps.get(bitmapIndex+1), new Rect(0, 0, getWidth(), getHeight()-placeIndicatorHeight), new Rect(0, placeIndicatorHeight, getWidth(), getHeight()), recycledPaint);
				canvas.drawBitmap(createStatusBitmap(bitmapIndex, bitmapIndex+1, 100), new Rect(0, 0, getWidth(), verticalMargin), new Rect(0, 0, getWidth(), placeIndicatorHeight), recycledPaint);
				getHolder().unlockCanvasAndPost(canvas);
				bitmapIndex++;
				touchIsDisabled = false;
				return;
			}
			float fr = dInterpolator.getInterpolation((float) ((double)deltaTime/totalTime));
			leftShift(switchPoint+((int)(fr * pixelsTo)));
			
		}
		
	}
	private class ReturnRightShift extends Thread
	{
		@Override
		public void run()
		{
			touchIsDisabled = true;
			returnRightShift();
			touchIsDisabled = false;
		}
		private void returnRightShift()
		{
			DecelerateInterpolator pol = new DecelerateInterpolator(interpolatorFactor);
			int newAtPixel = (int) (getWidth()-(-intoShift)); 
			bitmapIndex+=1;
			
			float f = 1f;
			for(float x = newAtPixel; x < getWidth(); x += (pol.getInterpolation(f)*interpolatorMultiplier)+interpolatorFloor+15)  //faster than finishShift methods
			{
				rightShift((int)x);
				f = (float) (f*f*interpolatorDivisor);
			}
			
			bitmapIndex-=1;
			
			Canvas canvas = getHolder().lockCanvas();
			canvas.drawBitmap(mBitmaps.get(bitmapIndex), new Rect(0, 0, getWidth(), getHeight()-placeIndicatorHeight), new Rect(0, placeIndicatorHeight, getWidth(), getHeight()), recycledPaint);
			canvas.drawBitmap(createStatusBitmap(bitmapIndex-1, bitmapIndex, 100), new Rect(0, 0, getWidth(), verticalMargin), new Rect(0, 0, getWidth(), placeIndicatorHeight), recycledPaint);
			getHolder().unlockCanvasAndPost(canvas);
		}
	}
	private class ReturnLeftShift extends Thread
	{
		@Override
		public void run()
		{
			touchIsDisabled = true;
			returnLeftShift();
			touchIsDisabled = false;
		}
		private void returnLeftShift()
		{
			DecelerateInterpolator pol = new DecelerateInterpolator(interpolatorFactor);
			int newAtPixel = (int) (getWidth()-intoShift);
			bitmapIndex--;
			
			float f = 1f;
			for(float x = newAtPixel; x < getWidth(); x += (pol.getInterpolation(f)*interpolatorMultiplier)+interpolatorFloor+15)
			{
				leftShift((int)x);
				f = (float) (f*f*interpolatorDivisor);
			}
			
			bitmapIndex++;
			
			Canvas canvas = getHolder().lockCanvas();
			canvas.drawBitmap(mBitmaps.get(bitmapIndex), new Rect(0, 0, getWidth(), getHeight()-placeIndicatorHeight), new Rect(0, placeIndicatorHeight, getWidth(), getHeight()), recycledPaint);
			canvas.drawBitmap(createStatusBitmap(bitmapIndex-1, bitmapIndex, 100), new Rect(0, 0, getWidth(), verticalMargin), new Rect(0, 0, getWidth(), placeIndicatorHeight), recycledPaint);
			getHolder().unlockCanvasAndPost(canvas);
		}
	}
	
	private float startingxCoordinate = 0;
	private volatile float intoShift = 0;
	private float endingxCoordinate = 0;
	
	
	
	@Override
	public boolean onTouchEvent(MotionEvent m)
	{
		if(touchIsDisabled) return true;
		
		if(m.getAction()==MotionEvent.ACTION_DOWN)
		{
			startingxCoordinate = m.getX();	
			//start tracking velocity
		}
		else if(m.getAction()==MotionEvent.ACTION_MOVE)
		{
			mVelocityTracker.addMovement(m);
			intoShift = (int) (m.getX() - startingxCoordinate);  //if toShift is negative, shift left
			
			if(intoShift < 0)  //is negative
			{
				leftShift((int) (intoShift + 2*(-intoShift)));  //make it positive
			}
			else if(intoShift > 0)
			{
				rightShift((int) intoShift);
			}
			intoShift = 0;
		}
		else if(m.getAction()==MotionEvent.ACTION_UP)
		{
			
			endingxCoordinate = m.getX();
			intoShift = (int) (endingxCoordinate - startingxCoordinate);  //equals how many pixels we moved over
			
			
			if(Math.abs(intoShift) > getWidth() / 2)  //finish shifting
			{
				if(intoShift > 0) //right shift >>
				{
					RightShiftCompleter r = new RightShiftCompleter();
					r.start();
				}
				else if(intoShift < 0) //left shift <<
				{
					intoShift = intoShift + 2*(-intoShift);  //make it positive
					LeftShiftCompleter l = new LeftShiftCompleter();
					l.start();
				}
			}
			else //check velocity
			{
				mVelocityTracker.computeCurrentVelocity(1000); //calculates in pixels per second
				if(mVelocityTracker.getXVelocity() > 55 || mVelocityTracker.getXVelocity() < -55) //velocity could be negative
				{
					if(intoShift > 0) //right shift >>
					{
						RightShiftCompleter r = new RightShiftCompleter();
						r.start();
					}
					else if(intoShift < 0) //left shift <<
					{
						intoShift = intoShift + 2*(-intoShift);  //make it positive
						LeftShiftCompleter l = new LeftShiftCompleter();
						l.start();
					}
				}
				else
				{
					//otherwise return to previous position
					if(intoShift > 0) //right shift >>
					{
						ReturnLeftShift l = new ReturnLeftShift();
						l.start();
					}
					else if(intoShift < 0) //left shift <<
					{
						ReturnRightShift r = new ReturnRightShift();
						r.start();
					}
				}
			}
			mVelocityTracker.clear(); //empty for next use
			}
		return true;
	}
	
	private void rightShift(int atPixel)
	{
		if(indexIsValid(mBitmaps, bitmapIndex) && indexIsValid(mBitmaps, bitmapIndex-1))
		{
			int top = placeIndicatorHeight;
			int bottom = getHeight();
			int oldBitmapWidth = getWidth() - atPixel;
			
			Canvas surface = getHolder().lockCanvas();
			if(surface==null)return;
			surface.drawBitmap(mBitmaps.get(bitmapIndex), new Rect(0, 0, oldBitmapWidth, bottom-placeIndicatorHeight), new Rect(atPixel, top, getWidth(), bottom), recycledPaint);
			surface.drawBitmap(mBitmaps.get(bitmapIndex-1), new Rect(oldBitmapWidth, 0, getWidth(), bottom-placeIndicatorHeight), new Rect(0, top, atPixel, bottom), recycledPaint);
			surface.drawBitmap(createStatusBitmap(bitmapIndex, bitmapIndex-1, (int) (100*(Math.abs(atPixel)/(double)getWidth()))), new Rect(0, 0, getWidth(), placeIndicatorHeight), new Rect(0, 0, getWidth(), placeIndicatorHeight), recycledPaint);
			getHolder().unlockCanvasAndPost(surface);
		}
	}
	
	private void leftShift(int atPixel) //move all pixels one pixel to the left
	{
		if(indexIsValid(mBitmaps, bitmapIndex) && indexIsValid(mBitmaps, bitmapIndex+1))
		{
			int top = placeIndicatorHeight;
			int bottom = getHeight();
			int oldBitmapWidth = getWidth() - atPixel;
			
			Canvas surface = getHolder().lockCanvas();
			if(surface==null)return;
			surface.drawBitmap(mBitmaps.get(bitmapIndex), new Rect(atPixel, 0, getWidth(), bottom-placeIndicatorHeight), new Rect(0, top, oldBitmapWidth, bottom), recycledPaint);
			surface.drawBitmap(mBitmaps.get(bitmapIndex+1), new Rect(0, 0, atPixel, bottom-placeIndicatorHeight), new Rect(oldBitmapWidth, top, getWidth(), bottom), recycledPaint);
			surface.drawBitmap(createStatusBitmap(bitmapIndex, bitmapIndex+1, (int) (100*(Math.abs(atPixel)/(double)getWidth()))), new Rect(0, 0, getWidth(), verticalMargin), new Rect(0, 0, getWidth(), placeIndicatorHeight), recycledPaint);
			getHolder().unlockCanvasAndPost(surface);
		}
	}
	

	public int getBackgroundColor() {
		return backgroundColor;
	}
	public void setBackgroundColor(int backgroundColor) {
		this.backgroundColor = backgroundColor;
	}
	private boolean indexIsValid(ArrayList array, int index)
	{
		if(index >= array.size())
			return false;
		if(index < 0)
			return false;
		return true;
	}

}
