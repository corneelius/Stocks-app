package data;

public interface StockData {
	public Ticker getTicker();
	public String summarizeAsString(); //for debugging

}
//interface identifying objects that hold stock data