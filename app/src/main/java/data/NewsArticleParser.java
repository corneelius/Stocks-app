package data;

import home.CompletionNotificatee;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import android.os.Message;
import android.util.Log;

public class NewsArticleParser {
	private CompletionNotificatee caller;
	private String toParse;
	private Message mMessage;
	
	private Runnable processor = new Runnable()
	{
		@Override
		public void run()
		{
			try {
				
				int numThreads = toParse.length()/5000;
				int charsPerThread = toParse.length()/numThreads;
				CountDownLatch latch = new CountDownLatch(numThreads);
				ArrayList<Callable<String>> jobs = new ArrayList<Callable<String>>();
			
				for(int i=0; i<numThreads; i++)
				{
					if(i==numThreads-1)
					{
						jobs.add(new Job(toParse.substring(charsPerThread*i, toParse.length()), latch));
					}
					else
						jobs.add(new Job(toParse.substring(charsPerThread*i, charsPerThread*(i+1)), latch));
				}
				ExecutorService executor = Executors.newCachedThreadPool();
				ArrayList<Future<String>> futures;
			
				futures = (ArrayList<Future<String>>) executor.invokeAll(jobs);
				Log.d("nr.app", "created "+futures.size()+"threads in NewsArticleParser");
			
			
				latch.await();
			
				String toReturn="";
				for(Future<String> f : futures)
				{
					toReturn = toReturn + (String)f.get();
				}
				
				caller.notification(toReturn); //send pointer
				
				mMessage.sendToTarget();
			
				
				
				} catch (InterruptedException e) 
				{
				e.printStackTrace();
				} catch (ExecutionException e) {
					e.printStackTrace();
				}
				
		}
	};
	
	private class Job implements Callable<String> 
	{
		
		private String s;
		private CountDownLatch finishLatch;
		
		public Job(String s, CountDownLatch latch)
		{
			this.s=s;
			finishLatch=latch;
		}
		@Override
		public String call()
		{
			String r=s;
			boolean waitingForEnd = false;
			int lastStartingMarker = 0;
			r.replaceAll(System.getProperty("line.separator"), "");
			for(int i=0; i<r.length(); i++)
			{
				if(r.charAt(i) == '<')
				{
					waitingForEnd = true;
					lastStartingMarker = i;
				}
				else if(r.charAt(i) == '>')
				{
					if(waitingForEnd)
					{
						r = r.substring(0, lastStartingMarker) + r.substring(i+1, r.length());
						waitingForEnd = false;
						i = i-(i-lastStartingMarker);
					}
				}
			}
			
			finishLatch.countDown();
			return r;
		
		}
	}
	
	public NewsArticleParser(CompletionNotificatee caller, Message m)
	{
		this.caller = caller;
		mMessage = m;
	}
	
	public void parse(String toParse)
	{
		this.toParse=toParse;
		new Thread(processor).run();
		
		
	}
	
	
}
