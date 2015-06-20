package data;

public class ByteArray {
	private byte[] mBytes;
	private byte[] mBytes2;
	private int index;
	
	public ByteArray(int len)
	{
		mBytes = new byte[len]; //empty
	}
	
	public void add(byte b)
	{
		if(mBytes != null && mBytes.length > index)
		{
			mBytes[index] = b;
			index++;
		}
		else if(mBytes2 != null && mBytes2.length > index)
		{
			mBytes2[index] = b;
			index++;
		}
		else
		{
			if(mBytes2 == null)
			{
				mBytes2 = new byte[mBytes.length + 1000];
				copy(mBytes, mBytes2);
				mBytes = null;
			}
			else if(mBytes == null)
			{
				mBytes = new byte[mBytes2.length + 1000];
				copy(mBytes2, mBytes);
				mBytes2 = null;
			}
			add(b);
		}
	}
	public void add(byte[] b)
	{
		for(int i=0; i<b.length; i++)
		{
			add(b[i]);
		}
	}
	
	public byte[] getArray()
	{
		byte[] r = new byte[index];
		
		if(mBytes != null)
		{
			for(int i=0; i<r.length; i++)
			{
				r[i] = mBytes[i];
			}
		}
		else if(mBytes2 != null)
		{
			for(int i=0; i<r.length; i++)
			{
				r[i] = mBytes2[i];
			}
		}
		return r;
	}
	
	private void copy(byte[] from, byte[] to)
	{
		if(to.length >= from.length)
		{
			for(int i=0; i<from.length; i++)
			{
				to[i] = from[i];
			}
		}
		else
		{
			throw new IllegalArgumentException("new byte[] must be larger or equal to the size of old byte[]");
		}
	}
	
	
	
	

}
