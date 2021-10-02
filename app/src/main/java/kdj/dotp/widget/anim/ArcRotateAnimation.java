package kdj.dotp.widget.anim;

import android.view.animation.Animation;
import android.view.animation.Transformation;

/**
 * Created by DJKim on 2019-04-05.
 */
public class ArcRotateAnimation extends Animation {
    private float mRadius;
    private float mFromDegrees;
    private float mToDegrees;

    private float mPivotX;
    private float mPivotY;

    /**
     * Constructor to use when building a RotateAnimation from code
     *
     * @param fromDegrees Rotation offset to apply at the start of the
     *                    animation.
     * @param toDegrees   Rotation offset to apply at the end of the animation.
     * @param pivotXValue The X coordinate of the point about which the object
     *                    is being rotated, specified as an absolute number where 0 is the
     *                    left edge. This value can either be an absolute number if
     *                    pivotXType is ABSOLUTE, or a percentage (where 1.0 is 100%)
     *                    otherwise.
     * @param pivotYValue The Y coordinate of the point about which the object
     *                    is being rotated, specified as an absolute number where 0 is the
     *                    top edge. This value can either be an absolute number if
     *                    pivotYType is ABSOLUTE, or a percentage (where 1.0 is 100%)
     *                    otherwise.
     */
    public ArcRotateAnimation(float radius,
                              float fromDegrees, float toDegrees,
                              float pivotXValue, float pivotYValue) {
        mRadius = radius;
        mFromDegrees = fromDegrees;
        mToDegrees = toDegrees;

        mPivotX = pivotXValue;
        mPivotY = pivotYValue;
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        float degrees = mFromDegrees + ((mToDegrees - mFromDegrees) * interpolatedTime);
        float scale = getScaleFactor();

        float curX = (float) ((mPivotX * scale) + (mRadius * Math.cos(Math.toRadians(degrees))));
        float curY = (float) ((mPivotY * scale) - (mRadius * Math.sin(Math.toRadians(degrees))));

        t.getMatrix().setTranslate(curX, curY);
    }
}
