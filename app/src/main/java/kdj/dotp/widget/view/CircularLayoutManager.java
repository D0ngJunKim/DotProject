package kdj.dotp.widget.view;

/**
 * Created by DJKim on 2019-03-28.
 */

import android.content.Context;
import android.graphics.PointF;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

import kdj.dotp.R;
import kdj.dotp.widget.anim.ArcRotateAnimation;
import kdj.dotp.widget.util.ExDimensionKt;


/**
 * Originally Created by Dajavu on 16/4/22.
 * Manipulated By DJKim.
 * 주의 및 필독!
 * 주의 및 필독!
 **/

/** 주의 및 필독! **/

/** 각도의 기준은 다음과 같다.
 * CASE 1:
 *                  0
 *                  |
 *                  |
 *                  |
 * -90 -------------------------- 90
 *                  |
 *                  |
 *                  |
 *              (-)180
 *
 */
public class CircularLayoutManager extends RecyclerView.LayoutManager {
    private static float DISTANCE_RATIO = 11f; // 스와이프 속도 조절 값.

    // Scroll Flag.
    private static int SCROLL_LEFT = 1;
    private static int SCROLL_RIGHT = 2;
    // Context.
    private Context mContext;
    // 아이템이 보여지는 원의 반지름.
    private float mRadius;
    // 아이템 사잇각.
    private int intervalAngle;
    // 스크롤에 의해 변경되는 각도 값.
    private float offsetRotate;
    // RecyclerView 에서 보여지는 최대, 최소 각도 값. 넘어가면 Recycle.
    private int minRemoveDegree;
    private int maxRemoveDegree;
    // RecyclerView 에서 스크롤 되는 최대, 최소 각도 값.
    private int minScrollDegree;
    private int maxScrollDegree;
    // RecyclerView 에서 스크롤 되는 최대, 최소 각도 값.
    private int minScrollDegreeOffsetAngle;
    private int maxScrollDegreeOffsetAngle;
    // 실제 계산에 사용되는 스크롤 가능 최대, 최소 각도 값.
    // 멀티 윈도우, 폴더블 등 가변적인 상황에 대비하기 위해 Define 된 값과 실제 계산에 쓰이는 값을 분리.
    private int calcMinScrollDegree = -1;
    private int calcMaxScrollDegree = -1;
    // 각각의 아이템이 가지는 추가적인 Offset.
    private float contentOffsetX = 0;
    private float contentOffsetY = 0;
    // 중심 점을 맞추기 위한 Y Offset.
    private float centerOffsetY;
    // RecyclerView의 지름이 디바이스의 가로를 넘어갈 때
    // 추가적으로 좌측에 줄 Offset. 사용처 참고.
    private float arcScrollOffset = 0;
    // 아이템이 RecyclerView에 붙었는지 여부, 또 각각의 아이템의 각도에 대한 정보를 가지고 있는 Array
    private SparseBooleanArray itemAttached = new SparseBooleanArray();
    private SparseArray<Float> itemsRotate = new SparseArray<>();
    // 원형의 스크롤 이벤트를 횡,종으로 변환하기위해 받아오는 이벤트 값.
    private MotionEvent mMotionEvent;


    public CircularLayoutManager(Context context, float parentRadiusInDp, float childRadiusInDp) {
        mContext = context;
        // Default Setting.
        intervalAngle = 30;
        offsetRotate = 0;
        minRemoveDegree = -90;
        maxRemoveDegree = 90;
        minScrollDegree = -90;
        maxScrollDegree = 90;
        minScrollDegreeOffsetAngle = 0;
        maxScrollDegreeOffsetAngle = 0;
        centerOffsetY = ExDimensionKt.toPx((parentRadiusInDp - childRadiusInDp), context);
        mRadius = ExDimensionKt.toPx(childRadiusInDp, context);
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (getItemCount() == 0) {
            detachAndScrapAttachedViews(recycler);
            offsetRotate = 0;
            return;
        }

        if (getChildCount() == 0) {
            View scrap = recycler.getViewForPosition(0);
            addView(scrap);
            measureChildWithMargins(scrap, 0, 0);
            detachAndScrapView(scrap, recycler);
        }

        int displayWidth = mContext.getResources().getDisplayMetrics().widthPixels;
        // Define Angle
        if (getWidth() <= displayWidth) {
            calcMinScrollDegree = minScrollDegree;
            calcMaxScrollDegree = maxScrollDegree;
        } else {
            // 닮음을 이용하여 원하는 가변의 각도를 구한다.
            // CASE : 반지름이 디바이스 Width보다 커져서 스크롤이 불가능한 영역이 생길 시
            // RecyclerView의 반지름과 현재 디바이스의 가로를 기준으로 Item이 "스크롤 될" 최대, 최소 각도를 자동으로 계산.
            // 여기에 scrollLeftOffset을 통해 간극을 벌릴 수 있다.
            // EX) arcScrollOffset = 0 -> 40도
            // EX) arcScrollOffset = 30 -> 30도
            float x = displayWidth / 2f;
            float r = mRadius + centerOffsetY;
            float y = (float) Math.sqrt((r * r) - (x * x));
            float similarityX = arcScrollOffset;
            float similarityY;
            if (similarityX != 0) {
                similarityY = (similarityX * y) / (x - similarityX);
            } else {
                similarityX = x;
                similarityY = y;
            }

            int degree = 90 - (int) Math.toDegrees(Math.atan2(similarityY, similarityX));
            if (degree > 90) {
                degree = 90;
            }
            calcMinScrollDegree = -degree;
            calcMaxScrollDegree = degree;
        }

        // Offset 추가.
        calcMinScrollDegree -= minScrollDegreeOffsetAngle;
        calcMaxScrollDegree += maxScrollDegreeOffsetAngle;

        // 각각의 Item이 가질 각도를 리스트에 저장.
        float rotate = calcMinScrollDegree;
        for (int i = 0; i < getItemCount(); i++) {
            itemsRotate.put(i, rotate);
            itemAttached.put(i, false);
            rotate += intervalAngle;
        }

        detachAndScrapAttachedViews(recycler);
        fixRotateOffset();
        layoutItems(recycler, state);
    }

    private void layoutItems(RecyclerView.Recycler recycler, RecyclerView.State state) {
        layoutItems(recycler, state, SCROLL_RIGHT);
    }

    private void layoutItems(RecyclerView.Recycler recycler,
                             RecyclerView.State state, int orientation) {
        if (state.isPreLayout()) return;

        // Item의 현재 각도가 Min, Max를 넘어가면 Item을 때어낸다.
        for (int i = 0; i < getChildCount(); i++) {
            View view = getChildAt(i);
            int position = getPosition(view);
            if (itemsRotate.get(position) - offsetRotate > maxRemoveDegree
                    || itemsRotate.get(position) - offsetRotate < minRemoveDegree) {
                itemAttached.put(position, false);
                removeAndRecycleView(view, recycler);
            }
        }

        // Item의 현재 각도가 Min, Max 범위 안에 있는 경우 Item을 붙인다.
        for (int i = 0; i < getItemCount(); i++) {
            if (itemsRotate.get(i) - offsetRotate <= maxRemoveDegree
                    && itemsRotate.get(i) - offsetRotate >= minRemoveDegree) {
                if (!itemAttached.get(i)) {
                    View scrap = recycler.getViewForPosition(i);
                    measureChildWithMargins(scrap, 0, 0);
                    if (orientation == SCROLL_LEFT) {
                        addView(scrap, 0);
                    } else {
                        addView(scrap);
                    }
                    float rotate = itemsRotate.get(i) - offsetRotate;

                    int left = calLeftPosition(rotate, getDecoratedMeasuredWidth(scrap));
                    int top = calTopPosition(rotate, getDecoratedMeasuredHeight(scrap));

                    scrap.setTag(R.id.key_angle, rotate);

                    layoutDecorated(scrap, left, top, left + getDecoratedMeasuredWidth(scrap), top + getDecoratedMeasuredHeight(scrap));

                    itemAttached.put(i, true);
                }
            }
        }
    }

    @Override
    public int scrollHorizontallyBy(int dx, RecyclerView.Recycler recycler, RecyclerView.State state) {
        return internalScrollBy(dx, recycler, state);
    }

    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
        int delta = dy;
        if (mMotionEvent != null) {
            if (mMotionEvent.getX() < getWidth() / 2f) {
                delta = -dy;
            }
        }
        return internalScrollBy(delta, recycler, state);
    }

    private int internalScrollBy(int delta, RecyclerView.Recycler recycler, RecyclerView.State state) {
        if ((calcMaxScrollDegree - calcMinScrollDegree) >= getMaxOffsetDegree()) return 0;

        int willScroll = delta;

        float theta = delta / DISTANCE_RATIO; // Angle That Every Item Will Rotate For Each dx.
        float targetRotate = offsetRotate + theta;

        // Case When Item Scrolls Out From the Scroll Range.
        if (targetRotate <= 0) {
            willScroll = (int) (-offsetRotate * DISTANCE_RATIO);
        } else if (targetRotate >= getMaxOffsetDegree() - (calcMaxScrollDegree - calcMinScrollDegree)) {
            willScroll = (int) ((getMaxOffsetDegree() - (calcMaxScrollDegree - calcMinScrollDegree) - offsetRotate) * DISTANCE_RATIO);
        }

        theta = willScroll / DISTANCE_RATIO;
        offsetRotate += theta; // Increase The Offset Rotate So When RE-LAYOUT, It Can Recycle The Right Views.

        // Re_CALCULATE X, Y From New Rotate Angle For Each Items.
        for (int i = 0; i < getChildCount(); i++) {
            View view = getChildAt(i);
            float newRotate = (float) view.getTag(R.id.key_angle) - theta;
            int offsetX = calLeftPosition(newRotate, getDecoratedMeasuredWidth(view));
            int offsetY = calTopPosition(newRotate, getDecoratedMeasuredHeight(view));

            layoutDecorated(view, offsetX, offsetY,
                    offsetX + getDecoratedMeasuredWidth(view), offsetY + getDecoratedMeasuredHeight(view));
            view.setTag(R.id.key_angle, newRotate);
        }

        // Add View Differently According To Scroll Direction.
        if (delta < 0) {
            layoutItems(recycler, state, SCROLL_LEFT);
        } else {
            layoutItems(recycler, state, SCROLL_RIGHT);
        }
        return willScroll;
    }

    /** Utility Methods */
    /**
     * @param rotate Current Rotate Of View In Degree.
     * @param width Height Of View.
     *
     * @return X Of View
     */
    private int calLeftPosition(float rotate, int width) {
        float startLeft = (getHorizontalSpace() - width) / 2f + contentOffsetX;
        return (int) ((mRadius * Math.cos(Math.toRadians(90 - rotate))) + startLeft);
    }

    /**
     * @param rotate Current Rotate Of View In Degree.
     * @param height Height Of View.
     *
     * @return Y Of View
     */
    private int calTopPosition(float rotate, int height) {
        float startTop = centerOffsetY + contentOffsetY - (height / 2f);
        return (int) ((mRadius - mRadius * Math.sin(Math.toRadians(90 - rotate))) + startTop);
    }

    private int getHorizontalSpace() {
        return getWidth() - getPaddingRight() - getPaddingLeft();
    }

    private int getVerticalSpace() {
        return getHeight() - getPaddingBottom() - getPaddingTop();
    }

    /**
     * Calibrate Offset Rotate Value For Validity.
     **/
    private void fixRotateOffset() {
        if (offsetRotate < 0) {
            offsetRotate = 0;
        }
        if (offsetRotate > getMaxOffsetDegree()) {
            offsetRotate = getMaxOffsetDegree();
        }
    }

    /**
     * @return Maximum Degrees of View.
     */
    private float getMaxOffsetDegree() {
        return (getItemCount() - 1) * intervalAngle;
    }

    /**
     * @return Get Current Position.
     */
    public int getCurrentPosition() {
        return Math.round(offsetRotate / intervalAngle);
    }

    /**
     * Reset Radius Value In Case Of Multi Window, Foldable Device.
     * @param parentRadiusInDp
     * @param childRadiusInDp
     */
    public void rearrangeItemCircle(float parentRadiusInDp, float childRadiusInDp) {
        this.mRadius = ExDimensionKt.toPx(childRadiusInDp, mContext);
        this.centerOffsetY = ExDimensionKt.toPx((parentRadiusInDp - childRadiusInDp), mContext);
    }

    /**
     * Angle, In Degrees, Between Each Items.
     *
     * @param intervalAngle the interval angle between each items
     */
    public void setIntervalAngle(int intervalAngle) {
        this.intervalAngle = intervalAngle;
    }

    /**
     * X Offset Using When Layouting Children.
     * Default is 0.
     *
     * @param contentOffsetX the content offset of x
     */
    public void setContentOffsetX(float contentOffsetX) {
        this.contentOffsetX = contentOffsetX;
    }

    /**
     * Y Offset Using When Layouting Children.
     * Default is 0.
     *
     * @param contentOffsetY the content offset of y
     */
    public void setContentOffsetY(float contentOffsetY) {
        this.contentOffsetY = contentOffsetY;
    }

    /**
     * Left, Right Offset Using When Layouting Children In Case Of Diameter is Larger Than Display Width.
     * Only Works When Diameter Is Larger Than Display Width.
     * Default is 0.
     */
    public void setArcScrollOffset(float offsetInPx) {
        arcScrollOffset = offsetInPx;
    }

    public void setMotionEvent(MotionEvent e) {
        mMotionEvent = e;
    }

    /**
     * Angle Of Child View In Range [min, max] Will Be Show.
     * If Angle Of Child View Goes Out From The Range, It Will Be Recycled.
     * Default is [-90, 90].
     *
     * @param min Minimum Angle In Degree That Will Be Show
     * @param max Maximum Angle In Degree That Will Be Show
     */
    public void setDegreeRangeWillShow(int min, int max) {
        if (min > max) return;
        minRemoveDegree = min;
        maxRemoveDegree = max;
    }

    /**
     * Angle Of Child View In Range [min, max] Will Be Scrolled.
     * Default is [-90, 90].
     *
     * @param min Minimum Angle In Degree That Will Be Scrolled
     * @param max Maximum Angle In Degree That Will Be Scrolled
     */
    public void setDefaultScrollLimitAngle(int min, int max) {
        if (min > max) return;
        minScrollDegree = min;
        maxScrollDegree = max;
    }

    public void setScrollLimitOffestAngle(int min, int max) {
        minScrollDegreeOffsetAngle = min;
        maxScrollDegreeOffsetAngle = max;
    }

    private PointF computeScrollVectorForPosition(int targetPosition) {
        if (getChildCount() == 0) {
            return null;
        }
        final int firstChildPos = getPosition(getChildAt(0));
        final int direction = targetPosition < firstChildPos ? -1 : 1;
        return new PointF(direction, 0);
    }

    private float getCurrentArcOffset(float theta) {
        float offset = 0;
        int overlapCount = (int) Math.floor(theta / 360f);
        double constant = 2 * Math.PI * mRadius;
        for (int i = 0; i <= overlapCount; i++) {
            if (i == overlapCount) {
                offset += constant * ((theta - (360 * i)) / 360f);
            } else {
                offset += constant;
            }
        }
        return offset;
    }

    @Override
    public int computeHorizontalScrollOffset(@NonNull RecyclerView.State state) {
        if (getItemCount() != 0 && state.getItemCount() != 0) {
            return (int) getCurrentArcOffset(getCurrentPosition() * intervalAngle);
        } else {
            return 0;
        }
    }

    @Override
    public int computeHorizontalScrollRange(@NonNull RecyclerView.State state) {
        if (getItemCount() != 0 && state.getItemCount() != 0) {
            return (int) getCurrentArcOffset((getItemCount() - 1) * intervalAngle);
        } else {
            return 0;
        }
    }

    @Override
    public int computeHorizontalScrollExtent(@NonNull RecyclerView.State state) {
        if (getItemCount() != 0 && state.getItemCount() != 0) {
            return (int) getCurrentArcOffset(calcMaxScrollDegree - calcMinScrollDegree);
        } else {
            return 0;
        }
    }

    @Override
    public boolean canScrollHorizontally() {
        return true;
    }

    @Override
    public boolean canScrollVertically() {
        return true;
    }

    @Override
    public void scrollToPosition(int position) {
        if (position < 0 || position > getItemCount() - 1) return;
        float targetRotate = position * intervalAngle;
        if (targetRotate == offsetRotate) return;
        offsetRotate = targetRotate;
        fixRotateOffset();
        requestLayout();
    }

    @Override
    public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {
        LinearSmoothScroller smoothScroller = new LinearSmoothScroller(mContext) {
            @Override
            public PointF computeScrollVectorForPosition(int targetPosition) {
                return CircularLayoutManager.this.computeScrollVectorForPosition(targetPosition);
            }
        };

        smoothScroller.setTargetPosition(position);
        startSmoothScroll(smoothScroller);
    }

    @Override
    public void onAdapterChanged(RecyclerView.Adapter oldAdapter, RecyclerView.Adapter newAdapter) {
        removeAllViews();
        offsetRotate = 0;
    }

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    /** Animation */
    void rollInItemsWithAnimation(long duration, final CircularRecyclerView.OnCompleteCallback callback) {
        final int childCount = getChildCount();
        if (childCount == 0) {
            if (callback != null) {
                callback.onCompleteItemAnimIn();
            }
            return;
        }

        for (int i = 0; i < childCount; i++) {
            final View view = getChildAt(i);
            if (view != null) {
                view.setAlpha(0);
                float angle = (float) view.getTag(R.id.key_angle);
                float fromDegree = 270;
                float toDegree = 90 - angle;
                float anchorX = (getWidth() / 2f) - view.getX() - (view.getWidth() / 2f) + contentOffsetX;
                float anchorY = (getHeight() / 2f) - view.getY() - (view.getHeight() / 2f) + contentOffsetY;

                AnimationSet animSet = new AnimationSet(true);
                AlphaAnimation alphaAnimation = new AlphaAnimation(0, 1);
                ArcRotateAnimation arcAnimation = new ArcRotateAnimation(mRadius, fromDegree, toDegree, anchorX, anchorY);
                animSet.addAnimation(arcAnimation);
                animSet.addAnimation(alphaAnimation);
                animSet.setDuration(duration);
                animSet.setInterpolator(new DecelerateInterpolator());
                animSet.setFillBefore(true);
                animSet.setFillEnabled(true);

                final int finalI = i;
                animSet.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        view.setAlpha(1);
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        if (finalI == childCount - 1) {
                            if (callback != null) {
                                callback.onCompleteItemAnimIn();
                            }
                        }
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                view.startAnimation(animSet);
            }
        }
    }

    void rollOutItemsWithAnimation(long duration, final CircularRecyclerView.OnCompleteCallback callback) {
        final int childCount = getChildCount();

        if (childCount == 0) {
            callback.onCompleteItemAnimOut();
            return;
        }

        for (int i = 0; i < childCount; i++) {
            final View view = getChildAt(i);
            view.setVisibility(View.VISIBLE);
            float angle = (float) view.getTag(R.id.key_angle);
            float fromDegree = 90 - angle;
            float toDegree = 270;
            float anchorX = (getWidth() / 2f) - view.getX() - (view.getWidth() / 2f) + contentOffsetX;
            float anchorY = (getHeight() / 2f) - view.getY() - (view.getHeight() / 2f) + contentOffsetY;

            ArcRotateAnimation arcAnimation = new ArcRotateAnimation(mRadius, fromDegree, toDegree, anchorX, anchorY);
            AlphaAnimation alphaAnimation = new AlphaAnimation(1, 0);
            AnimationSet animSet = new AnimationSet(true);
            animSet.addAnimation(arcAnimation);
            animSet.addAnimation(alphaAnimation);
            animSet.setDuration(duration);
            animSet.setInterpolator(new DecelerateInterpolator());
            animSet.setFillAfter(true);
            final int finalI = i;
            animSet.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    if (finalI == childCount - 1) {
                        if (callback != null) {
                            callback.onCompleteItemAnimOut();
                        }
                    }
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            view.startAnimation(animSet);
        }
    }
}