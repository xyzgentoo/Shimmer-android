package com.romainpiel.shimmer;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.os.Build;
import android.view.View;

/**
 * Shimmer
 * User: romainpiel
 * Date: 06/03/2014
 * Time: 15:42
 */
public class Shimmer {

    /**
     * Flash the shimmering effect over X or Y direction
     */
    public enum FLASH_DIRECTION {
        FLASH_X_LEFT_TO_RIGHT,
        FLASH_X_RIGHT_TO_LEFT,
        FLASH_Y_TOP_TO_DOWN,
        FLASH_Y_DOWN_TO_TOP
    }

    private static final int DEFAULT_REPEAT_COUNT = ValueAnimator.INFINITE;
    private static final long DEFAULT_DURATION = 1000;
    private static final long DEFAULT_START_DELAY = 0;

    private int repeatCount;
    private long duration;
    private long startDelay;
    private FLASH_DIRECTION direction;
    private Animator.AnimatorListener animatorListener;

    private ObjectAnimator animator;

    public Shimmer(FLASH_DIRECTION paramDirection) {
        repeatCount = DEFAULT_REPEAT_COUNT;
        duration = DEFAULT_DURATION;
        startDelay = DEFAULT_START_DELAY;
        direction = paramDirection;
    }

    public int getRepeatCount() {
        return repeatCount;
    }

    public Shimmer setRepeatCount(int repeatCount) {
        this.repeatCount = repeatCount;
        return this;
    }

    public long getDuration() {
        return duration;
    }

    public Shimmer setDuration(long duration) {
        this.duration = duration;
        return this;
    }

    public long getStartDelay() {
        return startDelay;
    }

    public Shimmer setStartDelay(long startDelay) {
        this.startDelay = startDelay;
        return this;
    }

    public FLASH_DIRECTION getDirection() {
        return direction;
    }

    public Shimmer setDirection(FLASH_DIRECTION direction) {
        this.direction = direction;
        return this;
    }

    public Animator.AnimatorListener getAnimatorListener() {
        return animatorListener;
    }

    public Shimmer setAnimatorListener(Animator.AnimatorListener animatorListener) {
        this.animatorListener = animatorListener;
        return this;
    }

    public <V extends View & ShimmerViewBase> void start(final V shimmerView) {

        if (isAnimating()) {
            return;
        }

        final Runnable animate = new Runnable() {
            @Override
            public void run() {

                shimmerView.setShimmering(true);
                shimmerView.setDirection(direction);

                float from, to;
                String property;

                switch (direction) {
                    case FLASH_X_LEFT_TO_RIGHT:
                    default:
                        from = 0;
                        to = shimmerView.getWidth();
                        property = "gradientX";
                        break;
                    case FLASH_X_RIGHT_TO_LEFT:
                        from = shimmerView.getWidth();
                        to = 0;
                        property = "gradientX";
                        break;
                    case FLASH_Y_TOP_TO_DOWN:
                        from = 0;
                        to = shimmerView.getHeight();
                        property = "gradientY";
                        break;
                    case FLASH_Y_DOWN_TO_TOP:
                        from = shimmerView.getHeight();
                        to = 0;
                        property = "gradientY";
                        break;
                }

                animator = ObjectAnimator.ofFloat(shimmerView, property, from, to);

                animator.setRepeatCount(repeatCount);
                animator.setDuration(duration);
                animator.setStartDelay(startDelay);
                animator.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        shimmerView.setShimmering(false);

                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                            shimmerView.postInvalidate();
                        } else {
                            shimmerView.postInvalidateOnAnimation();
                        }

                        animator = null;
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });

                if (animatorListener != null) {
                    animator.addListener(animatorListener);
                }

                animator.start();
            }
        };

        if (!shimmerView.isSetUp()) {
            shimmerView.setAnimationSetupCallback(new ShimmerViewHelper.AnimationSetupCallback() {
                @Override
                public void onSetupAnimation(final View target) {
                    animate.run();
                }
            });
        } else {
            animate.run();
        }
    }

    public void cancel() {
        if (animator != null) {
            animator.cancel();
        }
    }

    public boolean isAnimating() {
        return animator != null && animator.isRunning();
    }
}
