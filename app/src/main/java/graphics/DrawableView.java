package graphics;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

public class DrawableView extends View{

	private Drawable mDrawable;
	public DrawableView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	@Override
	public void onDraw(Canvas canvas)
	{
		if(mDrawable!=null)
		{
			mDrawable.setBounds(new Rect(0, 0, canvas.getWidth(), canvas.getHeight()));
			mDrawable.draw(canvas);
		}
	}
	public void setDrawable(Drawable d)
	{
		mDrawable=d;
	}

}
