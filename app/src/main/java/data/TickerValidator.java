package data;

public class TickerValidator implements android.widget.AutoCompleteTextView.Validator{

	@Override
	public boolean isValid(CharSequence text) {
		return false;
	}

	@Override
	public CharSequence fixText(CharSequence invalidText) {
		//upper case, 
		return null;
	}

}
