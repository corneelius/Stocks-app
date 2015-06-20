package data;

import java.math.BigDecimal;
import java.util.Date;

public class IndexEntry implements Comparable<IndexEntry>{
	private BigDecimal mDecimal;
	private Date mDate;
	
	public IndexEntry(Date date, BigDecimal d)
	{
		this.mDate = date;
		this.mDecimal = d;
	}
	public BigDecimal getBigDecimal()
	{
		return mDecimal;
	}
	public Date getDate()
	{
		return mDate;
	}
	@Override
	public int compareTo(IndexEntry another) {
		return mDecimal.compareTo(another.getBigDecimal());
	}

}
