package pl.maciejczekanski.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.Element;
import android.support.v8.renderscript.RenderScript;
import android.support.v8.renderscript.ScriptIntrinsicBlur;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

public class BlurFrameLayout extends FrameLayout {

    private static final int DEFAULT_BLUR_RADIUS = 10;
    private static final int DEFAULT_DOWNSAMPLE = 3;

    private RenderScript renderScript;
    private ScriptIntrinsicBlur blurIntrinsic;

    private Bitmap originalBackground;
    private Bitmap blurredBackground;

    private boolean parentDrawn = false;
    private Canvas blurCanvas;
    private Allocation in;
    private Allocation out;

    private int blurRadius = DEFAULT_BLUR_RADIUS;
    private float downsample = DEFAULT_DOWNSAMPLE;
    private float xScaleFactor = 1.0f;
    private float yScaleFactor = 1.0f;

    public BlurFrameLayout(Context context) {
        super(context);
        init(null, 0, 0);
    }

    public BlurFrameLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        init(attrs, 0, 0);
    }

    public BlurFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, defStyleAttr, 0);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public BlurFrameLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs, defStyleAttr, defStyleRes);
    }

    private void init(AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        initAttributes(attrs, defStyleAttr, defStyleRes);
        initRenderScript();
        setWillNotDraw(false);
    }

    private void initAttributes(AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        TypedArray a = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.BlurFrameLayout, defStyleAttr, defStyleRes);
        try {
            blurRadius = a.getInteger(R.styleable.BlurFrameLayout_blurRadius, DEFAULT_BLUR_RADIUS);
            downsample = a.getFloat(R.styleable.BlurFrameLayout_downsample, DEFAULT_DOWNSAMPLE);
        } finally {
            a.recycle();
        }
    }

    private void initRenderScript() {
        if (!isInEditMode()) {
            renderScript = RenderScript.create(getContext());
            blurIntrinsic = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript));
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        createBitmaps();
        invalidate();
    }

    private void createBitmaps() {
        final int width = roundToNextMultipleOf16(getMeasuredWidth()/downsample);
        final int height = roundToNextMultipleOf16(getMeasuredHeight()/downsample);
        xScaleFactor = getMeasuredWidth()/(float)width;
        yScaleFactor = getMeasuredHeight()/(float)height;
        originalBackground = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        blurredBackground = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        in = Allocation.createFromBitmap(renderScript, originalBackground, Allocation.MipmapControl.MIPMAP_NONE, Allocation.USAGE_SHARED | Allocation.USAGE_GRAPHICS_TEXTURE | Allocation.USAGE_SCRIPT);
        out = Allocation.createTyped(renderScript, in.getType());
        blurCanvas = new Canvas(originalBackground);
    }

    private void blur() {
        blurIntrinsic.setRadius(blurRadius);
        blurIntrinsic.setInput(in);
        blurIntrinsic.forEach(out);
        out.copyTo(blurredBackground);
    }

    @Override
    public void draw(Canvas canvas) {
        View parentView = (View) getParent();

        if (parentDrawn) {
            return;
        }

        parentDrawn = true;
        drawViewOnCanvas(parentView);

        blur();
        canvas.save();
        canvas.scale(xScaleFactor, yScaleFactor);
        canvas.drawBitmap(blurredBackground, 0, 0, null);
        canvas.restore();

        super.draw(canvas);
        parentDrawn = false;
    }

    private int roundToNextMultipleOf16(float x) {
        return (int) (Math.ceil(x/16)*16);
    }

    private void drawViewOnCanvas(View v) {
        blurCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        blurCanvas.save();
        blurCanvas.scale(1f/xScaleFactor, 1f/yScaleFactor);
        blurCanvas.translate(-getLeft(), -getTop());
        v.draw(blurCanvas);
        blurCanvas.restore();
    }

    public void setBlurRadius(int blurRadius) {
        this.blurRadius = blurRadius;
        invalidate();
    }

    public int getBlurRadius() {
        return blurRadius;
    }

    public float getDownsample() {
        return downsample;
    }
}
