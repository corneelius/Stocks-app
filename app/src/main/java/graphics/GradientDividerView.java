package graphics;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.View;

public class GradientDividerView extends View{
	private GradientDrawable d;
	private int softGrey;
	private int surroundingColor;
	
	public GradientDividerView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		surroundingColor = Color.WHITE;
		softGrey = Color.rgb(222,  223, 223);
		
		d = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[] {surroundingColor, softGrey, Color.BLACK} );
		
	}
	
	@Override
	public void onDraw(Canvas canvas)
	{
		d.setBounds(0,  0,  canvas.getWidth(), canvas.getHeight());
		d.draw(canvas);
		
	}

}
