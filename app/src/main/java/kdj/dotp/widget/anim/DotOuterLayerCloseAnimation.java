package kdj.dotp.widget.anim;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import kdj.dotp.widget.listener.OnDotAnimCheckListener;
import kdj.dotp.widget.util.ExDimensionKt;
import kdj.dotp.widget.value.DotCloseAnimValue;

/**
 * Created by DJKim on 2019-06-18.
 */
public class DotOuterLayerCloseAnimation {
    private volatile static DotOuterLayerCloseAnimation sInstance;

    public static DotOuterLayerCloseAnimation getInstance() {
        if (sInstance == null) {
            synchronized (DotOuterLayerCloseAnimation.class) {
                if (sInstance == null) {
                    sInstance = new DotOuterLayerCloseAnimation();
                }
            }
        }
        return sInstance;
    }

    public void closeLayer(final View layer, final View title, final View pager, OnDotAnimCheckListener listener) {
        if (layer == null || title == null || pager == null)
            return;
        AnimatorSet closeAnimSet = new AnimatorSet();
        closeAnimSet.playTogether(
                getLayerAnim(layer),
                getTitleAnim(title),
                getPagerAnim(pager)
        );
        closeAnimSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                listener.onLayerAnimFinished(false);
            }
        });
        closeAnimSet.start();
    }

    private Animator getLayerAnim(View layer) {
        final ObjectAnimator translateYAnim = ObjectAnimator.ofFloat(layer, "translationY", 0, ExDimensionKt.toPx(layer.getContext().getResources().getConfiguration().screenHeightDp, layer.getContext()));
        translateYAnim.setDuration(DotCloseAnimValue.OUTER_LAYER.getDuration());
        translateYAnim.setStartDelay(DotCloseAnimValue.OUTER_LAYER.getDelay());
        translateYAnim.setInterpolator(new DecelerateInterpolator());
        return translateYAnim;
    }

    private Animator getTitleAnim(View title) {
        final ObjectAnimator translateYAnim = ObjectAnimator.ofFloat(title, "translationY", 0, ExDimensionKt.toPx(title.getContext().getResources().getConfiguration().screenHeightDp, title.getContext()) + 32);
        final ValueAnimator fadeInAnim = ObjectAnimator.ofFloat(title, "alpha", 1f, 0f);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setInterpolator(new DecelerateInterpolator());
        animatorSet.setDuration(DotCloseAnimValue.OUTER_TITLE.getDuration());
        animatorSet.setStartDelay(DotCloseAnimValue.OUTER_TITLE.getDelay());
        animatorSet.playTogether(translateYAnim, fadeInAnim);

        return animatorSet;
    }

    private Animator getPagerAnim(View pager) {
        final ObjectAnimator translateYAnim = ObjectAnimator.ofFloat(pager, "translationY", 0, ExDimensionKt.toPx(pager.getContext().getResources().getConfiguration().screenHeightDp, pager.getContext()) + 59);
        final ValueAnimator fadeInAnim = ObjectAnimator.ofFloat(pager, "alpha", 1f, 0f);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setInterpolator(new DecelerateInterpolator());
        animatorSet.setDuration(DotCloseAnimValue.OUTER_PAGER.getDuration());
        animatorSet.setStartDelay(DotCloseAnimValue.OUTER_PAGER.getDelay());
        animatorSet.playTogether(translateYAnim, fadeInAnim);

        return animatorSet;
    }
}
