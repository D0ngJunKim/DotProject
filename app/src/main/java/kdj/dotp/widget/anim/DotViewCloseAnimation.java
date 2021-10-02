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
import android.widget.ImageView;

import kdj.dotp.widget.listener.OnDotAnimCheckListener;
import kdj.dotp.widget.value.DotCloseAnimValue;

public class DotViewCloseAnimation {
    private volatile static DotViewCloseAnimation sInstance;

    public static DotViewCloseAnimation getInstance() {
        if (sInstance == null) {
            synchronized (DotViewCloseAnimation.class) {
                if (sInstance == null) {
                    sInstance = new DotViewCloseAnimation();
                }
            }
        }
        return sInstance;
    }

    /**
     * tab -> close -> hide ssg dot view
     *
     * @param dotIconBg
     * @param close
     */
    public void closeDotView(final Context context, final View parentBg, final View dotIconParentLayout, final ImageView dotIconBg, final View close, final ImageView icon, final View shadow, OnDotAnimCheckListener listener) {
        if (parentBg == null || dotIconParentLayout == null || dotIconBg == null || close == null || icon == null)
            return;
        AnimatorSet closeAnimSet = new AnimatorSet();
        closeAnimSet.playTogether(
                getBgAlphaOutAnim(parentBg),
                getSizeDownDotViewAnim(context, dotIconParentLayout, dotIconBg, close, icon, shadow, listener)
        );
        closeAnimSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                listener.onDotAnimFinished(false);
            }
        });
        closeAnimSet.start();
    }

    private Animator getBgAlphaOutAnim(final View parentBg) {
        ValueAnimator alphaAnim = ObjectAnimator.ofFloat(parentBg, "alpha", 0.8f, 0.0f);
        alphaAnim.setDuration(DotCloseAnimValue.BACKGROUND.getDuration());
        alphaAnim.setStartDelay(DotCloseAnimValue.BACKGROUND.getDelay());
        alphaAnim.setInterpolator(new DecelerateInterpolator());
        return alphaAnim;
    }

    private Animator getSizeDownDotViewAnim(final Context context, final View dotIconParentLayout, final ImageView dotIconBg, final View close, final ImageView icon, final View shadow, OnDotAnimCheckListener listener) {
        final ObjectAnimator translateRootLayoutAnim = ObjectAnimator.ofFloat(dotIconParentLayout, "translationY", 0);

        final ObjectAnimator scaleBgAnim = ObjectAnimator.ofPropertyValuesHolder(
                dotIconBg,
                PropertyValuesHolder.ofFloat("scaleX", 1f),
                PropertyValuesHolder.ofFloat("scaleY", 1f));

        final ObjectAnimator scaleCloseAnim = ObjectAnimator.ofPropertyValuesHolder(
                close,
                PropertyValuesHolder.ofFloat("scaleX", 0f),
                PropertyValuesHolder.ofFloat("scaleY", 0f));

        final ValueAnimator alphaAnim = ObjectAnimator.ofFloat(shadow, "alpha", 1f, 0f);

        final ObjectAnimator translateYAnim = ObjectAnimator.ofFloat(shadow, "translationY", 0);

        scaleBgAnim.setDuration(DotCloseAnimValue.DOT_BG_SCALE.getDuration());
        scaleBgAnim.setStartDelay(DotCloseAnimValue.DOT_BG_SCALE.getDelay());
        scaleBgAnim.setInterpolator(new AccelerateDecelerateInterpolator());

        alphaAnim.setDuration(DotCloseAnimValue.DOT_BG_SCALE.getDuration());
        alphaAnim.setStartDelay(DotCloseAnimValue.DOT_BG_SCALE.getDelay());
        alphaAnim.setInterpolator(new AccelerateDecelerateInterpolator());

        translateYAnim.setDuration(DotCloseAnimValue.DOT_BG_SCALE.getDuration());
        translateYAnim.setStartDelay(DotCloseAnimValue.DOT_BG_SCALE.getDelay());
        translateYAnim.setInterpolator(new AccelerateDecelerateInterpolator());

        translateRootLayoutAnim.setDuration(DotCloseAnimValue.DOT_BG_TRANS.getDuration());
        translateRootLayoutAnim.setStartDelay(DotCloseAnimValue.DOT_BG_TRANS.getDelay());
        translateRootLayoutAnim.setInterpolator(new AccelerateDecelerateInterpolator());

        scaleCloseAnim.setDuration(DotCloseAnimValue.DOT_CLOSE.getDuration());
        scaleCloseAnim.setStartDelay(DotCloseAnimValue.DOT_CLOSE.getDelay());
        scaleCloseAnim.setInterpolator(new DecelerateInterpolator());

        AnimatorSet closeAnimSet = new AnimatorSet();
        closeAnimSet.playTogether(translateRootLayoutAnim, scaleBgAnim, alphaAnim, scaleCloseAnim, recoverDotIcon(icon, listener));
        return closeAnimSet;
    }

    /**
     * Ssg Dot 아이콘 탭 -> 닫기 완료 -> Ssg Dot Bg가 점점 작아지는 애니메이션 -> 원래 Ssg Dot Home 으로 돌아오는 애니메이션
     *
     * @param icon
     */

    private Animator recoverDotIcon(final View icon, OnDotAnimCheckListener listener) {
        final ObjectAnimator scaleIconAnim = ObjectAnimator.ofPropertyValuesHolder(
                icon,
                PropertyValuesHolder.ofFloat("scaleX", 1f),
                PropertyValuesHolder.ofFloat("scaleY", 1f));

        //fadeout anim
        final ValueAnimator fadeInAnim = ObjectAnimator.ofFloat(icon, "alpha", 0f, 1f);

        AnimatorSet iconAnimSet = new AnimatorSet();
        iconAnimSet.setInterpolator(new AccelerateDecelerateInterpolator());
        iconAnimSet.setDuration(DotCloseAnimValue.DOT_LOGO.getDuration());
        iconAnimSet.setStartDelay(DotCloseAnimValue.DOT_LOGO.getDelay());
        iconAnimSet.playTogether(scaleIconAnim, fadeInAnim);

        return iconAnimSet;
    }
}
