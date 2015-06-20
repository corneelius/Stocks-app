package graphics;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.EmbossMaskFilter;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;

public class WDrawable extends Drawable
{
	private int mColor;

	private boolean selected=false;
	public WDrawable(int color)
	{
		mColor=color;
	}
	public void setSelected(boolean tf)
	{
		selected=tf;
	}

	private Paint getPaint()
	{
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setColor(mColor);
		return paint;
	}
	@Override
	public void draw(Canvas canvas) {
		Rect rect = getBounds();
		int centerX = (rect.left+rect.right)/2;
		int centerY = (rect.top+rect.bottom)/2;
		int radius = (rect.left+rect.right)/3;
		
		Paint paint = getPaint();
		paint.setMaskFilter(new EmbossMaskFilter(new float[]{1, 1, 1}, 0.5f, 8, 3));
		if(selected)
			paint.setShader(new RadialGradient(centerX, centerY, radius, new int[] {Color.argb(120, Color.red(mColor), Color.green(mColor), Color.blue(mColor)), Color.argb(255, Color.red(mColor), Color.green(mColor), Color.blue(mColor))}, null, Shader.TileMode.CLAMP));
		else
			paint.setShader(new RadialGradient(centerX, centerY, radius, new int[] {Color.argb(255, Color.red(mColor), Color.green(mColor), Color.blue(mColor)), Color.argb(120, Color.red(mColor), Color.green(mColor), Color.blue(mColor))}, null, Shader.TileMode.CLAMP));
		
		canvas.drawCircle(centerX, centerY, radius, paint);
		
		
	}

	@Override
	public void setAlpha(int alpha) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setColorFilter(ColorFilter cf) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getOpacity() {
		// TODO Auto-generated method stub
		return 0;
	}
	
}
