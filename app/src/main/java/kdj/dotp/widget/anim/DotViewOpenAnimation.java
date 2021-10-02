package kdj.dotp.widget.anim;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;


import java.util.ArrayList;
import java.util.Collection;

import kdj.dotp.widget.listener.OnDotAnimCheckListener;
import kdj.dotp.widget.util.ExDimensionKt;
import kdj.dotp.widget.value.DotOpenAnimValue;

public class DotViewOpenAnimation {
    private volatile static DotViewOpenAnimation sInstance;
    private Collection<Animator> mAnimationList = new ArrayList<Animator>();
    private boolean bIdling = false;


    public static DotViewOpenAnimation getInstance() {
        if (sInstance == null) {
            synchronized (DotViewOpenAnimation.class) {
                if (sInstance == null) {
                    sInstance = new DotViewOpenAnimation();
                }
            }
        }
        return sInstance;
    }


    /**
     * tab -> open -> show ssg dot view
     *
     * @param dotIconBg
     * @param icon
     */
    public void openDotView(final Context context, final View dotIconParentLayout, final View dotIconBg, final View icon, final View close, View shadow, float dotWidthInPx, OnDotAnimCheckListener listener) {
        if (dotIconParentLayout == null || dotIconBg == null || icon == null || close == null)
            return;
        AnimatorSet openAnimSet = new AnimatorSet();
        openAnimSet.playTogether(
                getHideDotIconAnim(icon),
                getMoveUpDotIconParentLayoutAnim(dotIconParentLayout, -Math.round(ExDimensionKt.toPx(18, context))),
                getShowDotCloseBtnAnim(close),
                getSizeUpDotIconBgAnim(context, dotIconBg, shadow, dotWidthInPx,-Math.round(ExDimensionKt.toPx(18, context)))
        );
        openAnimSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                listener.onDotAnimFinished(true);
            }
        });
        openAnimSet.start();
    }

    /**
     * tab -> open -> move up
     *
     * @param view
     * @param distance
     */
    private Animator getMoveUpDotIconParentLayoutAnim(final View view, int distance) {
        final ObjectAnimator translateYAnim = ObjectAnimator.ofFloat(view, "translationY", distance);
        translateYAnim.setDuration(DotOpenAnimValue.DOT_BG_TRANS.getDuration());
        translateYAnim.setStartDelay(DotOpenAnimValue.DOT_BG_TRANS.getDelay());
        translateYAnim.setInterpolator(new AccelerateDecelerateInterpolator());
        return translateYAnim;
    }

    /**
     * Ssg Dot 아이콘 탭 -> 오픈 -> Ssg Dot 아이콘이 사라지는 애니메이션
     *
     * @param view
     */
    private Animator getHideDotIconAnim(final View view) {
        //scal anim
        final ObjectAnimator scaleAnim = ObjectAnimator.ofPropertyValuesHolder(
                view,
                PropertyValuesHolder.ofFloat("scaleX", 0.037f),
                PropertyValuesHolder.ofFloat("scaleY", 0.037f));

        //fadeout anim
        final ValueAnimator fadeoutAnim = ObjectAnimator.ofFloat(view, "alpha", 1f, 0f);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(scaleAnim, fadeoutAnim);
        animatorSet.setDuration(DotOpenAnimValue.DOT_LOGO.getDuration());
        animatorSet.setStartDelay(DotOpenAnimValue.DOT_LOGO.getDelay());
        animatorSet.setInterpolator(new DecelerateInterpolator());
        return animatorSet;
    }


    /**
     * Ssg Dot 아이콘 탭 -> 오픈 -> X 버튼이 보여지는 애니메이션
     *
     * @param view
     */
    private Animator getShowDotCloseBtnAnim(final View view) {
        AnimatorSet animatorSet = new AnimatorSet();

        final ValueAnimator fadeinAnim = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f);
        final ObjectAnimator scaleAnim = ObjectAnimator.ofPropertyValuesHolder(
                view,
                PropertyValuesHolder.ofFloat("scaleX", 0f, 1f),
                PropertyValuesHolder.ofFloat("scaleY", 0f, 1f));

        fadeinAnim.setDuration(0);

        scaleAnim.setDuration(DotOpenAnimValue.DOT_CLOSE.getDuration());
        scaleAnim.setStartDelay(DotOpenAnimValue.DOT_CLOSE.getDelay());
        scaleAnim.setInterpolator(new DecelerateInterpolator());
        animatorSet.playTogether(scaleAnim, fadeinAnim);

        return animatorSet;
    }

    /**
     * Ssg Dot 아이콘 탭 -> 오픈 -> Ssg Dot Bg가 점점 커지는 애니메이션
     *
     * @param view
     */
    private Animator getSizeUpDotIconBgAnim(Context context, final View view, final View shadow, float targetWidth, float distance) {
        float divider = Math.round(ExDimensionKt.toPx(54, context));
        float firstTarget = targetWidth / divider; //54px -> 100px
        float secondTarget = (targetWidth * 1.11f) / divider; //100px -> 111% scale up

        final ObjectAnimator scaleAnim = ObjectAnimator.ofPropertyValuesHolder(
                view,
                PropertyValuesHolder.ofFloat("scaleX", firstTarget),    //54px -> 100px
                PropertyValuesHolder.ofFloat("scaleY", firstTarget));   //54px -> 100px
        final ValueAnimator fadeAnim = ObjectAnimator.ofFloat(shadow, "alpha", 0f, 1f);
        final ObjectAnimator translateYAnim = ObjectAnimator.ofFloat(shadow, "translationY", distance);

        AnimatorSet animSet = new AnimatorSet();
        animSet.playTogether(scaleAnim, fadeAnim, translateYAnim);
        animSet.setDuration(DotOpenAnimValue.DOT_BG_SCALE.getDuration());
        animSet.setStartDelay(DotOpenAnimValue.DOT_BG_SCALE.getDelay());
        animSet.setInterpolator(new AccelerateDecelerateInterpolator());
        animSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                breathDotIconBg(view, secondTarget);
            }
        });

        return animSet;
    }

    /**
     * Ssg Dot 아이콘 탭 -> 오픈 -> Ssg Dot Bg가 숨쉬는 애니메이션
     *
     * @param view
     * @param targetWidth
     */
    private void breathDotIconBg(final View view, float targetWidth) {
        if (view == null) return;

        final ObjectAnimator scaleAnim = ObjectAnimator.ofPropertyValuesHolder(
                view,
                PropertyValuesHolder.ofFloat("scaleX", targetWidth),  //100px -> 111% scale up
                PropertyValuesHolder.ofFloat("scaleY", targetWidth)); //100px -> 111% scale up
        scaleAnim.setDuration(800);
        scaleAnim.setStartDelay(2000);
        scaleAnim.setRepeatCount(1);
        scaleAnim.setInterpolator(new AccelerateDecelerateInterpolator());
        scaleAnim.setRepeatMode(ObjectAnimator.REVERSE);
        scaleAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (!bIdling) {
                    scaleAnim.start();
                }
            }
        });

        scaleAnim.start();
        bIdling = false;

        mAnimationList.add(scaleAnim);
    }

    /**
     * Foldable Device 대응을 위한 기능
     *
     * @param view
     * @param targetWidth
     */
    public void resizeDotIconBg(Context context, final View view, float targetWidth) {
        if (view == null) return;

        releaseBreathingDotIconBgAnimation();

        float divider = Math.round(ExDimensionKt.toPx(54, context));
        float firstTarget = targetWidth / divider;
        float secondTarget = (targetWidth * 1.11f) / divider;

        view.setScaleX(firstTarget);
        view.setScaleY(firstTarget);

        breathDotIconBg(view, secondTarget);
    }


    /**
     * 숨쉬는 애니메이션 종료
     */
    public void releaseBreathingDotIconBgAnimation() {
        bIdling = true;

        for (Animator animator : mAnimationList) {
            if (animator != null) {
                animator.removeAllListeners();
                animator.end();
                animator.cancel();
            }
        }

        mAnimationList.clear();
    }

}
