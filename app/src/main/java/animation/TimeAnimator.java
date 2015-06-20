package animation;

public class TimeAnimator extends Thread{
	
	private static long TIME_DELTA = 1;
	private TimeListener mListener;
	private long startTime;
	private long mDuration = 1000;
	
	public TimeAnimator(TimeListener listener)
	{
		super();
		mListener=listener;
	}
	
	public void setDuration(long duration)
	{
		mDuration = duration;
	}
	
	public void prepare()
	{
		startTime = System.currentTimeMillis();
	}
	
	@Override
	public void run()
	{
		while(true)
		{
			long time = System.currentTimeMillis();
			if((time - startTime) >= mDuration)
			{
				mListener.onTimeUpdate(this, mDuration, mDuration );
				break;
			}
			else if( (time - startTime) % TIME_DELTA == 0 )
			{
				mListener.onTimeUpdate(this, mDuration, (time - startTime) );
			}
				
			
			
		}
	}
	
	public interface TimeListener
	{
		public void onTimeUpdate(TimeAnimator animation, long totalTime, long deltaTime);
	}

}
