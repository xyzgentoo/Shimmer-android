package com.romainpiel.shimmer;

import android.content.res.TypedArray;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

/**
 * Shimmer
 * User: romainpiel
 * Date: 10/03/2014
 * Time: 17:06
 */
public class ShimmerViewHelper {

    public interface AnimationSetupCallback {
        void onSetupAnimation(View target);
    }

    private static final int DEFAULT_REFLECTION_COLOR = 0xFFFFFFFF;
    private static final Shimmer.FLASH_DIRECTION DEFAULT_FLASH_DIRECTION = Shimmer.FLASH_DIRECTION
            .FLASH_X_LEFT_TO_RIGHT;

    private View view;
    private Paint paint;

    // center position of the gradient
    private float gradientX;

    private float gradientY;

    private Shimmer.FLASH_DIRECTION mDirection;

    // shader applied on the text view
    // only null until the first global layout
    private LinearGradient linearGradient;

    // shader's local matrix
    // never null
    private Matrix linearGradientMatrix;

    private int primaryColor;

    // shimmer reflection color
    private int reflectionColor;

    // true when animating
    private boolean isShimmering;

    // true after first global layout
    private boolean isSetUp;

    // callback called after first global layout
    private AnimationSetupCallback callback;

    public ShimmerViewHelper(View view, Paint paint, AttributeSet attributeSet) {
        this.view = view;
        this.paint = paint;
        init(attributeSet);
    }

    public float getGradientX() {
        return gradientX;
    }

    public void setGradientX(float gradientX) {
        this.gradientX = gradientX;
        view.invalidate();
    }

    public float getGradientY() {
        return gradientY;
    }

    public void setGradientY(float gradientY) {
        this.gradientY = gradientY;
        view.invalidate();
    }

    public Shimmer.FLASH_DIRECTION getDirection() {
        return mDirection;
    }

    public void setDirection(Shimmer.FLASH_DIRECTION direction) {
        mDirection = direction;
        if (isSetUp) {
            resetLinearGradient();
        }
    }

    public boolean isShimmering() {
        return isShimmering;
    }

    public void setShimmering(boolean isShimmering) {
        this.isShimmering = isShimmering;
    }

    public boolean isSetUp() {
        return isSetUp;
    }

    public void setAnimationSetupCallback(AnimationSetupCallback callback) {
        this.callback = callback;
    }

    public int getPrimaryColor() {
        return primaryColor;
    }

    public void setPrimaryColor(int primaryColor) {
        this.primaryColor = primaryColor;
        if (isSetUp) {
            resetLinearGradient();
        }
    }

    public int getReflectionColor() {
        return reflectionColor;
    }

    public void setReflectionColor(int reflectionColor) {
        this.reflectionColor = reflectionColor;
        if (isSetUp) {
            resetLinearGradient();
        }
    }

    private void init(AttributeSet attributeSet) {

        reflectionColor = DEFAULT_REFLECTION_COLOR;
        mDirection = DEFAULT_FLASH_DIRECTION;

        if (attributeSet != null) {
            TypedArray a = view.getContext().obtainStyledAttributes(attributeSet, R.styleable.ShimmerView, 0, 0);
            if (a != null) {
                try {
                    reflectionColor = a.getColor(R.styleable.ShimmerView_reflectionColor, DEFAULT_REFLECTION_COLOR);
                } catch (Exception e) {
                    android.util.Log.e("ShimmerTextView", "Error while creating the view:", e);
                } finally {
                    a.recycle();
                }
            }
        }

        linearGradientMatrix = new Matrix();
    }

    private void resetLinearGradient() {

        // our gradient is a simple linear gradient from textColor to reflectionColor. its axis is at the center
        // when it's outside of the view, the outer color (textColor) will be repeated (Shader.TileMode.CLAMP)
        // initially, the linear gradient is positioned on the left side of the view

        switch (mDirection) {
            case FLASH_X_LEFT_TO_RIGHT:
            case FLASH_X_RIGHT_TO_LEFT:
            default:
                linearGradient = new LinearGradient(-view.getWidth(), 0, 0, 0,
                        new int[]{
                                primaryColor,
                                reflectionColor,
                                primaryColor,
                        },
                        new float[]{
                                0,
                                0.5f,
                                1
                        },
                        Shader.TileMode.CLAMP
                );
                break;
            case FLASH_Y_TOP_TO_DOWN:
            case FLASH_Y_DOWN_TO_TOP:
                linearGradient = new LinearGradient(0, -view.getHeight(), 0, 0,
                        new int[] {
                                primaryColor,
                                reflectionColor,
                                primaryColor
                        },
                        new float[] {
                                0,
                                0.5f,
                                1.0f
                        },
                        Shader.TileMode.CLAMP
                );
                break;
        }

        paint.setShader(linearGradient);
    }

    protected void onSizeChanged() {

        resetLinearGradient();

        if (!isSetUp) {
            isSetUp = true;

            if (callback != null) {
                callback.onSetupAnimation(view);
            }
        }
    }

    /**
     * content of the wrapping view's onDraw(Canvas)
     * MUST BE CALLED BEFORE SUPER STATEMENT
     */
    public void onDraw() {

        // only draw the shader gradient over the text while animating
        if (isShimmering) {

            // first onDraw() when shimmering
            if (paint.getShader() == null) {
                paint.setShader(linearGradient);
            }

            //TODO LH Matrix这块我还得看看，有点儿晕

            // translate the shader local matrix
            switch (mDirection) {
                case FLASH_X_LEFT_TO_RIGHT:
                case FLASH_X_RIGHT_TO_LEFT:
                default:
                    linearGradientMatrix.setTranslate(2 * gradientX, 0);
                    break;
                case FLASH_Y_TOP_TO_DOWN:
                case FLASH_Y_DOWN_TO_TOP:
                    linearGradientMatrix.setTranslate(0, 2 * gradientY);
                    break;
            }

            // this is required in order to invalidate the shader's position
            linearGradient.setLocalMatrix(linearGradientMatrix);

        } else {
            // we're not animating, remove the shader from the paint
            paint.setShader(null);
        }
    }
}
