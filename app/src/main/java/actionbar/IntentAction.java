package actionbar;

import android.content.Intent;
import android.graphics.drawable.Drawable;

public class IntentAction extends Action{
	private Intent mIntent;

	public IntentAction(Intent intent, Drawable drawable) {
		super(null, drawable);
		mIntent=intent;
		// TODO Auto-generated constructor stub
	}
	
	public Intent getIntentAction()
	{
		return mIntent;
	}

}
