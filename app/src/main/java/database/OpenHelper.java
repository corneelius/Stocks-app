package database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class OpenHelper extends SQLiteOpenHelper {

	public OpenHelper(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.e("nr.app", "creating database");
		String statement = "CREATE TABLE stocklist (_id TEXT PRIMARY KEY, name TEXT, " + //_id is symbol
				"market_cap INTEGER, sector TEXT, industry TEXT, searches INTEGER, date INTEGER);";
		db.execSQL(statement);
		
	}
	
	@Override
	public void onOpen(SQLiteDatabase db)
	{
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		
	}
	

}
