package watchlist;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.util.ArrayList;

import android.content.Context;
import data.StockGenerics;
import data.Types;

public class WatchlistManager {
	private final String file_name="watchlists";
	private Context context;
	
	public WatchlistManager(Context context)
	{
		this.context=context;
	}
	
	public ArrayList<String> getWatchlist(String name)
	{
		ArrayList<ArrayList<String>> watchlists = getWatchlists();
		for(ArrayList<String> a : watchlists)
		{
			if(a.get(0).equals(name))
				return a;
		}
		return null;
	}
	public ArrayList<ArrayList<String>> getWatchlists()
	{
		FileInputStream in=null;
		StringBuilder builder=null;
		try
		{
			in = context.openFileInput(file_name);
		}
		catch(FileNotFoundException e)
		{
			return null;
		}
		try{
			builder = new StringBuilder();
			byte[] buffer = new byte[1];
			int length;
			while ((length = in.read(buffer)) != -1) {
				builder.append(new String(trimBytes(buffer, length)));
			}
		in.close();
		}catch(IOException e){
			
		}
		
		ArrayList<ArrayList<String>> watchlist = new ArrayList<ArrayList<String>>();
		String data = builder.toString();
		String ticker = "";
		ArrayList<String> w = new ArrayList<String>();
		for(int i=0; i<data.length(); i++)
		{
			if(data.charAt(i)==',')
			{
				w.add(ticker);
				ticker="";
			}
			else if(data.charAt(i)=='?')
			{
				watchlist.add(w);
				w = new ArrayList<String>();
			}
			else
			{
				ticker+=data.charAt(i);
			}
		}
		return watchlist;
		
	}
	private byte[] trimBytes(byte[] b, int len)
	{
		byte[] array = new byte[len];
		for(int i=0; i<array.length; i++)
		{
			array[i] = b[i];
		}
		return array;
	}
	public void removeWatchlist(String name)
	{
		ArrayList<ArrayList<String>> watchlists = getWatchlists();
		for(int i=0; i<watchlists.size(); i++)
		{
			if(watchlists.get(i).get(0).equals(name))
			{
				watchlists.remove(i);
				writeWatchlists(watchlists);
				return;
			}
		}
	}
	public void writeWatchlists(ArrayList<ArrayList<String>> watchlists)
	{
		StringBuilder builder = new StringBuilder();
		for(int i=0; i<watchlists.size(); i++)
		{
			for(int x=0; x<watchlists.get(i).size(); x++)
			{
				builder.append(watchlists.get(i).get(x)+",");
			}
			builder.append("?");
		}
		try
		{
		byte[] bytes = builder.toString().getBytes();
		FileOutputStream out = context.openFileOutput(file_name, Context.MODE_PRIVATE);
		out.write(bytes);
		out.flush();
		out.close();
		}catch(IOException e)
		{
			
		}
	}
	public File writeStockGenerics(ArrayList<StockGenerics> objects) //name is temporary
	{
		String fileName = "temp"+System.currentTimeMillis();
		FileOutputStream in;
		ObjectOutputStream out;
		

			try {
				in = context.openFileOutput(fileName, Context.MODE_PRIVATE);
				out = new ObjectOutputStream(in);
			
			out.writeObject(objects);
			
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return new File(context.getFilesDir(), fileName); //so it can be changed
	}
	public ArrayList<StockGenerics> getStockGenerics(String watchlist)
	{
		File file = new File(context.getFilesDir(), watchlist);
		ArrayList<StockGenerics> array=null;
		if(file.exists())
		{
			FileInputStream fis;
			try {
				fis = context.openFileInput(watchlist);
			ObjectInputStream ois = new ObjectInputStream(fis);
			array = (ArrayList<StockGenerics>) ois.readObject();
			
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (OptionalDataException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return array;
		}
		return null;
	}
	public void addStocks(String list, ArrayList<String> tickers)
	{
		ArrayList<ArrayList<String>> watchlists = getWatchlists();
		if(watchlists==null)
		{
			watchlists = new ArrayList<ArrayList<String>>();
		}
		for(int i=0; i<watchlists.size(); i++)
		{
			if(watchlists.get(i).get(0).equals(list))
			{
				ArrayList<String> theOne = watchlists.get(i);
				watchlistLoop : for(int z = 0; z<tickers.size(); z++)
				{
					String newStock = tickers.get(z);
					for(String s : theOne)
					{
						if(newStock.equals(s))
						{
							continue watchlistLoop;
						}
					}
					watchlists.get(i).add(newStock);
				}
				writeWatchlists(watchlists);
				return;
			}
		}
		//didn't find it - add new watchlist
		tickers.add(0, list);
		Types.checkStringArrayListForDups(tickers, 1);
		watchlists.add(tickers);
		writeWatchlists(watchlists);
	}
	
	public boolean removeStock(String list, String ticker)
	{
		ArrayList<ArrayList<String>> watchlists = getWatchlists();
		for(int i=0; i<watchlists.size(); i++)
		{
			if(watchlists.get(i).get(0).equals(list))
			{
				for(int z = 0; z<watchlists.get(i).size(); z++)
				{
					if(watchlists.get(i).get(z).equals(ticker))
					{
						watchlists.get(i).remove(z);
					}
				}
				//special case
				if(watchlists.get(i).size()==1)
				{
					removeWatchlist(list);
					return true;
				}
				writeWatchlists(watchlists);
				return false;
			}
		}
		return false;
	}

}
