package kdj.dotp.widget.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import kdj.dotp.R;
import kdj.dotp.widget.util.ExDimensionKt;
import kdj.dotp.widget.value.DotCloseAnimValue;
import kdj.dotp.widget.value.DotOpenAnimValue;


/**
 * Created by raymonkim on 2019-03-28.
 */
public class CircularRecyclerView extends RecyclerView {
    private static final String CIRCLE_RADIUS_ANIMATOR_FIELD_NAME = "animationCircleRadius";
    private float mCircleRadius = 0;
    private float mAnimRadius = 0;
    private Paint mCirclePaint;
    private int mCircleBgColor;
    private CircularLayoutManager mLayoutManager;
    private OnCompleteCallback mCallback;
    private CircularGradationDecoration mShadowDecoration;

    public CircularRecyclerView(@NonNull Context context) {
        super(context);
        init(context, null);
    }

    public CircularRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);

    }

    public CircularRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();

        if (attrs == null) {
            mCircleRadius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, mAnimRadius, metrics);
        } else {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CircularRecyclerView);
            mCircleRadius = typedArray.getDimensionPixelSize(R.styleable.CircularRecyclerView_circle_radius, (int) mAnimRadius);
            mCircleBgColor = typedArray.getColor(R.styleable.CircularRecyclerView_circle_bg, Color.WHITE);
            typedArray.recycle();
        }

        setOverScrollMode(OVER_SCROLL_NEVER);

        mCirclePaint = new Paint();
        mCirclePaint.setAntiAlias(true);
        mCirclePaint.setColor(mCircleBgColor);
        mCirclePaint.setStyle(Paint.Style.FILL);
    }

    @Override
    public void onDraw(Canvas c) {
        super.onDraw(c);
        int circleCenterX = (int) (getWidth() / 2f);
        int circleCenterY = (int) (getHeight() / 2f);

        c.drawCircle(circleCenterX, circleCenterY, mAnimRadius, mCirclePaint);
    }

    @Override
    public void setLayoutManager(@Nullable LayoutManager layout) {
        if (layout instanceof CircularLayoutManager) {
            mLayoutManager = (CircularLayoutManager) layout;
        }
        super.setLayoutManager(layout);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        float eventX = e.getX();
        float eventY = e.getY();
        float dx = (getWidth() / 2f) - eventX;
        float dy = (getHeight() / 2f) - eventY;
        double r = Math.sqrt((dx * dx) + (dy * dy));
        if (e.getAction() == MotionEvent.ACTION_DOWN) {
            if (r > mCircleRadius) {
                return false;
            }
        }

        if (mLayoutManager != null) {
            mLayoutManager.setMotionEvent(e);
        }
        return super.onTouchEvent(e);
    }

    public void setShadowDecoration(CircularGradationDecoration decoration) {
        mShadowDecoration = decoration;
    }

    public void setAnimationCallBack(OnCompleteCallback callBack) {
        mCallback = callBack;
    }

    public void setCircleRadius(float circleRadius) {
        mCircleRadius = ExDimensionKt.toPx(circleRadius, getContext());
        int diameter = (int) (mCircleRadius * 2);
        getLayoutParams().width = diameter;
        getLayoutParams().height = diameter;
    }

    public void rearrangeRadius(float circleRadius) {
        mAnimRadius = ExDimensionKt.toPx(circleRadius, getContext());
        setCircleRadius(circleRadius);
    }

    public void beginEnterAction() {
        beginEnterAnimation(DotOpenAnimValue.INNER_SCALE.getDuration(), DotOpenAnimValue.INNER_SCALE.getDelay(), DotOpenAnimValue.INNER_ROTATE.getDuration(), DotOpenAnimValue.INNER_ROTATE.getDelay());
    }

    public void beginExitAction() {
        beginExitAnimation(DotCloseAnimValue.INNER_SCALE.getDuration(), DotCloseAnimValue.INNER_SCALE.getDelay(), DotCloseAnimValue.INNER_ROTATE.getDuration(), DotCloseAnimValue.INNER_ROTATE.getDelay());
    }

    private void beginEnterAnimation(long scaleDuration, long scaleStartDelay, long rollingDuration, long rollingStartDelay) {
        if (mShadowDecoration != null) {
            addItemDecoration(mShadowDecoration);
        }
        ObjectAnimator scaleAnim = ObjectAnimator.ofInt(this, CIRCLE_RADIUS_ANIMATOR_FIELD_NAME, 0, (int) mCircleRadius);
        ValueAnimator alphaAnim = ObjectAnimator.ofFloat(this, "alpha", 0f, 1f);

        AnimatorSet enterAnimSet = new AnimatorSet();
        enterAnimSet.playTogether(scaleAnim, alphaAnim);
        enterAnimSet.setDuration(scaleDuration);
        enterAnimSet.setStartDelay(scaleStartDelay);
        enterAnimSet.setInterpolator(new DecelerateInterpolator());
        enterAnimSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (mCallback != null) {
                    mCallback.onCompleteScaleExpand();
                }
            }
        });

        if (mLayoutManager != null) {
            if (rollingStartDelay != 0) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mLayoutManager.rollInItemsWithAnimation(rollingDuration, mCallback);
                    }
                }, rollingStartDelay);
            } else {
                mLayoutManager.rollInItemsWithAnimation(rollingDuration, mCallback);
            }
        }
        enterAnimSet.start();
    }

    private void beginExitAnimation(long scaleDuration, long scaleStartDelay, long rollingDuration, long rollingStartDelay) {
        ObjectAnimator scaleAnim = ObjectAnimator.ofInt(this, CIRCLE_RADIUS_ANIMATOR_FIELD_NAME, (int) mCircleRadius, 0);
        ValueAnimator alphaAnim = ObjectAnimator.ofFloat(this, "alpha", 1f, 0f);

        AnimatorSet exitAnimSet = new AnimatorSet();
        exitAnimSet.playTogether(scaleAnim, alphaAnim);
        exitAnimSet.setDuration(scaleDuration);
        exitAnimSet.setInterpolator(new DecelerateInterpolator());
        exitAnimSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (mCallback != null) {
                    mCallback.onCompleteScaleCollapse();
                }
            }
        });

        exitAnimSet.setStartDelay(scaleStartDelay);
        if (mLayoutManager != null) {
            if (rollingStartDelay != 0) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mLayoutManager.rollOutItemsWithAnimation(rollingDuration, mCallback);
                    }
                }, rollingStartDelay);
            } else {
                mLayoutManager.rollOutItemsWithAnimation(rollingDuration, mCallback);
            }
        }
        exitAnimSet.start();
    }

    @SuppressWarnings("unused")
    private void setAnimationCircleRadius(int animationCircleRadius) {
        mAnimRadius = animationCircleRadius;
        invalidate();
    }


    public interface OnCompleteCallback {
        void onCompleteItemAnimIn();

        void onCompleteItemAnimOut();

        void onCompleteScaleExpand();

        void onCompleteScaleCollapse();
    }
}
