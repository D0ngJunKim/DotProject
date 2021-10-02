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
import kdj.dotp.widget.value.DotOpenAnimValue;

/**
 * Created by 180842 on 2019-06-18.
 */
public class DotOuterLayerOpenAnimation {
    private volatile static DotOuterLayerOpenAnimation sInstance;

    public static DotOuterLayerOpenAnimation getInstance() {
        if (sInstance == null) {
            synchronized (DotOuterLayerOpenAnimation.class) {
                if (sInstance == null) {
                    sInstance = new DotOuterLayerOpenAnimation();
                }
            }
        }
        return sInstance;
    }

    public void openLayer(final View layer, final View title, final View pager, OnDotAnimCheckListener listener) {
        if (layer == null || title == null || pager == null)
            return;
        AnimatorSet openAnimSet = new AnimatorSet();
        openAnimSet.playTogether(
                getLayerAnim(layer),
                getTitleAnim(title),
                getPagerAnim(pager)
        );
        openAnimSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                listener.onLayerAnimFinished(true);
            }
        });
        openAnimSet.start();
    }

    private AnimatorSet getLayerAnim(View layer) {
        final ObjectAnimator translateYAnim = ObjectAnimator.ofFloat(layer, "translationY", ExDimensionKt.toPx(layer.getContext().getResources().getConfiguration().screenHeightDp, layer.getContext()), 0);
        final ValueAnimator fadeInAnim = ObjectAnimator.ofFloat(layer, "alpha", 0f, 1f);

        AnimatorSet animatorSet = new AnimatorSet();

        fadeInAnim.setDuration(0);
        translateYAnim.setDuration(DotOpenAnimValue.OUTER_LAYER.getDuration());
        translateYAnim.setStartDelay(DotOpenAnimValue.OUTER_LAYER.getDelay());
        translateYAnim.setInterpolator(new DecelerateInterpolator());
        animatorSet.playTogether(translateYAnim, fadeInAnim);

        return animatorSet;
    }

    private AnimatorSet getTitleAnim(View title) {
        final ObjectAnimator translateYAnim = ObjectAnimator.ofFloat(title, "translationY", 50, 0);
        final ValueAnimator fadeInAnim = ObjectAnimator.ofFloat(title, "alpha", 0f, 1f);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setInterpolator(new DecelerateInterpolator());
        animatorSet.setDuration(DotOpenAnimValue.OUTER_TITLE.getDuration());
        animatorSet.setStartDelay(DotOpenAnimValue.OUTER_TITLE.getDelay());
        animatorSet.playTogether(translateYAnim, fadeInAnim);

        return animatorSet;
    }

    private AnimatorSet getPagerAnim(View pager) {
        final ObjectAnimator translateYAnim = ObjectAnimator.ofFloat(pager, "translationY", 50, 0);
        final ValueAnimator fadeInAnim = ObjectAnimator.ofFloat(pager, "alpha", 0f, 1f);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setInterpolator(new DecelerateInterpolator());
        animatorSet.setDuration(DotOpenAnimValue.OUTER_PAGER.getDuration());
        animatorSet.setStartDelay(DotOpenAnimValue.OUTER_PAGER.getDelay());
        animatorSet.playTogether(translateYAnim, fadeInAnim);

        return animatorSet;
    }
}
