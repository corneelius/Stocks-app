package graphics;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.widget.RelativeLayout;

public class ShadowView extends View{
	GradientDrawable d;

	public ShadowView(Context context, GradientDrawable.Orientation orientation, RelativeLayout.LayoutParams params) {
		super(context);
		d = new GradientDrawable(orientation, new int[] {Color.argb(50, 0, 0, 0), Color.argb(50, 255, 255, 255)} );
		setLayoutParams(params);
	}
	
	@Override
	public void onDraw(Canvas canvas)
	{
		d.setBounds(0, 0, getWidth(), getHeight());
		d.draw(canvas);
	}

}
