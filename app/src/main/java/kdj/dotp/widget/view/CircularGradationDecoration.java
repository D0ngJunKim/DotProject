package kdj.dotp.widget.view;

import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by 180842 on 2019-04-18.
 */
public class CircularGradationDecoration extends RecyclerView.ItemDecoration {
    private int[] mColorList;
    private float[] mPositionList;
    private float mRadius;

    public CircularGradationDecoration(float parentRadius, @ColorInt int[] colorList, float[] positionList) {
        mRadius = parentRadius;
        mColorList = colorList;
        mPositionList = positionList;
    }

    @Override
    public void onDrawOver(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        int circleCenterX = (int) (parent.getWidth() / 2f);
        int circleCenterY = (int) (parent.getHeight() / 2f);
        float x = parent.getWidth() / 2f;
        float y0 = 0;
        float y1 = parent.getHeight();
        Paint p = new Paint();
        p.setAntiAlias(true);
        p.setShader(new LinearGradient(x, y0, x, y1, mColorList, mPositionList, Shader.TileMode.CLAMP));
        p.setStyle(Paint.Style.FILL);
        c.drawCircle(circleCenterX, circleCenterY, mRadius, p);
    }

    public void rearrangeRadius(float radius) {
        mRadius = radius;
    }
}
