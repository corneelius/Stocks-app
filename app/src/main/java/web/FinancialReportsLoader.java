package web;
import java.io.IOException;
import java.util.ArrayList;

import web.ReportLoader.Report;

import android.os.AsyncTask;

public class FinancialReportsLoader extends AsyncTask<String, Void, ReportLoader.Report[]> {
	
	
	private static String getIncomeStatementURL(String ticker)
	{
		return "http://finance.yahoo.com/q/is?s="+ticker+"+Income+Statement&annual";
	}
	private static String getBalanceSheetURL(String ticker)
	{
		return "http://finance.yahoo.com/q/bs?s="+ticker+"+Balance+Sheet&annual";
	}
	private static String getCashFlowStatementURL(String ticker)
	{
		return "http://finance.yahoo.com/q/cf?s="+ticker+"+Cash+Flow&annual";
	}

	@Override
	protected Report[] doInBackground(String... stocks) {
		if(!isCancelled())
		{
			return null;
		}
		ReportLoader loader = new ReportLoader();
		try {
			ArrayList<ReportLoader.TagHandler> h1 = new ArrayList<ReportLoader.TagHandler>();
			h1.add(new BalanceSheetHandler());
			ReportLoader.Report[] reports = loader.getReports(getBalanceSheetURL(stocks[0]), h1);
			Report bs = reports[0];
			
			if(!isCancelled())
			{
				return null;
			}
			
			ArrayList<ReportLoader.TagHandler> h2 = new ArrayList<ReportLoader.TagHandler>();
			h2.add(new IncomeStatementHandler());
			reports = loader.getReports(getIncomeStatementURL(stocks[0]), h2);
			Report is = reports[0];
			
			if(!isCancelled())
			{
				return null;
			}
			
			ArrayList<ReportLoader.TagHandler> h3 = new ArrayList<ReportLoader.TagHandler>();
			h3.add(new CashFlowStatementHandler());
			reports = loader.getReports(getCashFlowStatementURL(stocks[0]), h3);
			Report cf = reports[0];
			
			return new Report[] {bs, is, cf};

		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}

}
