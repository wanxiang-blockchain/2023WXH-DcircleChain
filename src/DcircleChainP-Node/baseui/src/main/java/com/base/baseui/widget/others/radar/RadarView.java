package com.base.baseui.widget.others.radar;

import static com.base.baseui.widget.others.radar.util.PointRangeKt.getHexagonRadius;
import static com.base.baseui.widget.others.radar.util.PointRangeKt.getLinearGradientColor;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.os.Build;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Scroller;

import com.base.baseui.R;
import com.base.baseui.widget.others.radar.util.RotateUtil;
import com.blankj.utilcode.util.SizeUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author yangfei
 * @time 2023/5/17
 * @desc
 */

public class RadarView extends View {
    private final Context mContext;

    private int mWebMode;
    public static final int WEB_MODE_POLYGON = 1;
    public static final int WEB_MODE_CIRCLE = 2;

    public static final int VERTEX_ICON_POSITION_LEFT = 1;
    public static final int VERTEX_ICON_POSITION_RIGHT = 2;
    public static final int VERTEX_ICON_POSITION_TOP = 3;
    public static final int VERTEX_ICON_POSITION_BOTTOM = 4;
    public static final int VERTEX_ICON_POSITION_CENTER = 5;
    private double mPerimeter;

    private boolean mIsHide = false;

    private float mRadius;
    private PointF mPointCenter;
    private int mLayerLineColor;
    private float mLayerLineWidth;
    private int mVertexLineColor;
    private float mVertexLineWidth;

    private int mLayer;
    private List<Integer> mLayerColor;
    private float mMaxValue;
    private List<Float> mMaxValues;

    private List<String> mVertexText;
    private List<Bitmap> mVertexIcon;
    private int mVertexIconPosition;
    private float mVertexIconSize;
    private float mVertexIconMargin;

    private int mVertexTextColor;
    private float mVertexTextSize;
    private float mVertexTextOffset;
    private int mMaxVertex;
    private float mCenterTextSize;
    private int mCenterTextColor;

    private double mAngle;
    private double mRotateAngle;

    private List<RadarData> mRadarData;

    private RectF mVertexIconRect;

    private Paint mRadarLinePaint;
    private Paint mLayerPaint;
    private TextPaint mVertexTextPaint;
    private Paint mValuePaint;
    private TextPaint mValueTextPaint;
    private Path mRadarPath;
    private TextPaint mCenterTextPaint;

    private GestureDetector mDetector;
    private Scroller mScroller;
    private float mFlingPoint;
    private double mRotateOrientation;
    private boolean mRotationEnable;

    private final String mEmptyHint = "";
    private String mMaxLengthVertexText;
    private String mCenterText;

    private int mRadarWid = 4;

    private final int COLOR_DEFOULT = 0x00FFFFFF;

    private final int COLOR_LEVEL_ONE = 0xFF86E64F;
    private final int COLOR_LEVEL_TWO = 0xFF54EAD6;
    private final int COLOR_LEVEL_THREE = 0xFF3A86FF;
    private final int COLOR_LEVEL_FOUL = 0xFF8F4BEE;
    private final int COLOR_LEVEL_FIVE = 0xFFFF9A00;
    private final int COLOR_LEVEL_SIX = 0xFFE92727;


    public RadarView(Context context) {
        this(context, null);
    }

    public RadarView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RadarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        initAttrs(attrs);
        init();
    }

    private void initAttrs(AttributeSet attrs) {
        TypedArray typedArray = mContext.obtainStyledAttributes(attrs, R.styleable.RadarView);
        mLayer = typedArray.getInt(R.styleable.RadarView_radar_layer, 1);
        mRotationEnable = typedArray.getBoolean(R.styleable.RadarView_rotation_enable, true);
        mWebMode = typedArray.getInt(R.styleable.RadarView_web_mode, WEB_MODE_POLYGON);
        mMaxValue = typedArray.getFloat(R.styleable.RadarView_max_value, 0);
        mRadarWid = typedArray.getInt(R.styleable.RadarView_radar_width, mRadarWid);
        mLayerLineColor = typedArray.getColor(R.styleable.RadarView_layer_line_color, 0xFF9E9E9E);
        mLayerLineWidth = typedArray.getDimension(R.styleable.RadarView_layer_line_width, dp2px(1));
        mVertexLineColor = typedArray.getColor(R.styleable.RadarView_vertex_line_color, 0xFF9E9E9E);
        mVertexLineWidth = typedArray.getDimension(R.styleable.RadarView_vertex_line_width, dp2px(1));
        mVertexTextColor = typedArray.getColor(R.styleable.RadarView_vertex_text_color, mVertexLineColor);
        mVertexTextSize = typedArray.getDimension(R.styleable.RadarView_vertex_text_size, dp2px(0));
        mVertexTextOffset = typedArray.getDimension(R.styleable.RadarView_vertex_text_offset, 0);
        mCenterTextColor = typedArray.getColor(R.styleable.RadarView_center_text_color, mVertexLineColor);
        mCenterTextSize = typedArray.getDimension(R.styleable.RadarView_center_text_size, dp2px(0));
        mCenterText = typedArray.getString(R.styleable.RadarView_center_text);
        mVertexIconSize = typedArray.getDimension(R.styleable.RadarView_vertex_icon_size, dp2px(0));
        mVertexIconPosition = typedArray.getInt(R.styleable.RadarView_vertex_icon_position, VERTEX_ICON_POSITION_TOP);
        mVertexIconMargin = typedArray.getDimension(R.styleable.RadarView_vertex_icon_margin, 0);
        int vertexTextResid = typedArray.getResourceId(R.styleable.RadarView_vertex_text, 0);
        typedArray.recycle();
        initVertexText(vertexTextResid);
    }

    private void initVertexText(int vertexTextResid) {
        try {
            String[] stringArray = mContext.getResources().getStringArray(vertexTextResid);
            if (stringArray.length > 0) {
                mVertexText = new ArrayList<>();
                Collections.addAll(mVertexText, stringArray);
            }
        } catch (Exception e) {
        }
    }

    private void init() {
        mRadarPath = new Path();
        mScroller = new Scroller(mContext);
        mDetector = new GestureDetector(mContext, new GestureListener());
        mDetector.setIsLongpressEnabled(false);

        mRadarData = new ArrayList<>();
        mLayerColor = new ArrayList<>();
        initLayerColor();

        mRadarLinePaint = new Paint();
        mLayerPaint = new Paint();
        mValuePaint = new Paint();
        mVertexTextPaint = new TextPaint();
        mValueTextPaint = new TextPaint();
        mCenterTextPaint = new TextPaint();

        mRadarLinePaint.setAntiAlias(true);
        mLayerPaint.setAntiAlias(true);
        mVertexTextPaint.setAntiAlias(true);
        mCenterTextPaint.setAntiAlias(true);
        mValueTextPaint.setAntiAlias(true);
        mValuePaint.setAntiAlias(true);
        mValueTextPaint.setFakeBoldText(true);

        mVertexIconRect = new RectF();
    }

    private void initLayerColor() {
        if (mLayerColor == null) {
            mLayerColor = new ArrayList<>();
        }
        if (mLayerColor.size() < mLayer) {
            int size = mLayer - mLayerColor.size();
            for (int i = 0; i < size; i++) {
                mLayerColor.add(Color.TRANSPARENT);
            }
        }
    }


    private void initMaxValues() {
        if (mMaxValues != null && mMaxValues.size() < mMaxVertex) {
            int size = mMaxVertex - mMaxValues.size();
            for (int i = 0; i < size; i++) {
                mMaxValues.add(0f);
            }
        }
    }

    public void addData(RadarData data) {
        mRadarData.add(data);
        initData(data);
    }

    public void hideMiddle(){
        mIsHide = true;
    }

    public void clearRadarData() {
        mRadarData.clear();
        invalidate();
    }

    private void initData(RadarData data) {
        List<Float> value = data.getValue();
        float max = Collections.max(value);
        if (mMaxValue == 0 || mMaxValue < max) {
            mMaxValue = max;
        }
        int valueSize = value.size();
        if (mMaxVertex < valueSize) {
            mMaxVertex = valueSize;
        }
        mAngle = 2 * Math.PI / mMaxVertex;
        initVertexText();
        initMaxValues();
    }

    private void initVertexText() {
        if (mVertexText == null || mVertexText.size() == 0) {
            mVertexText = new ArrayList<>();
            for (int i = 0; i < mMaxVertex; i++) {
                char text = (char) ('A' + i);
                mVertexText.add(String.valueOf(text));
            }
        } else if (mVertexText.size() < mMaxVertex) {
            int size = mMaxVertex - mVertexText.size();
            for (int i = 0; i < size; i++) {
                mVertexText.add("");
            }
        }
        if (mVertexText.size() == 0) {
            return;
        }
        mMaxLengthVertexText = Collections.max(mVertexText, new Comparator<>() {
            @Override
            public int compare(String lhs, String rhs) {
                return lhs.length() - rhs.length();
            }
        });
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mRadarData.size() == 0) {
            mValueTextPaint.setTextSize(dp2px(0));
            float hintWidth = mValueTextPaint.measureText(mEmptyHint);
            canvas.drawText(mEmptyHint, mPointCenter.x - hintWidth / 2, mPointCenter.y, mValueTextPaint);
        } else {
            initPaint();
            calcRadius();
            drawRadar(canvas);
            //中间区域
            if(!mIsHide){
                drawData(canvas);
                drawCenterText(canvas);
            }
        }
    }

    private void initPaint() {
        mRadarLinePaint.setStyle(Paint.Style.STROKE);
        mVertexTextPaint.setColor(mVertexTextColor);
        mVertexTextPaint.setTextSize(mVertexTextSize);
        mCenterTextPaint.setTextSize(mCenterTextSize);
        mCenterTextPaint.setColor(mCenterTextColor);
    }

    private void drawRadar(Canvas canvas) {
        if (mWebMode == WEB_MODE_POLYGON) {
            drawWeb(canvas);
        } else if (mWebMode == WEB_MODE_CIRCLE) {
            drawCircle(canvas);
        }
        if(!mIsHide){
            drawRadarLine(canvas);
        }
    }

    private void drawWeb(Canvas canvas) {
        for (int i = mLayer; i >= 1; i--) {
            //减4dp是防止角落被裁剪
            float radius = mRadius / mLayer * i - SizeUtils.dp2px(mRadarWid);
            float radius1 = radius * 1/3;
            float radius2 = radius * 2/3;

            for (int j = 1; j <= mMaxVertex; j++) {
                mRadarPath.reset();
                LinearGradient linearGradient = null;
                float data = 0f;
                if (mRadarData.get(0).getValueText().size() > j-1){
                    data = Float.parseFloat(mRadarData.get(0).getValueText().get(j - 1));
                }
                double angleSin = Math.sin(mAngle * j - mRotateAngle);
                double angleCos = Math.cos(mAngle * j - mRotateAngle);
                float x = (float) (mPointCenter.x + angleSin * radius);
                float y = (float) (mPointCenter.y - angleCos * radius);

                mRadarPath.moveTo(x, y);
                double angleSin1 = Math.sin(mAngle * (j + 1) - mRotateAngle);
                double angleCos1 = Math.cos(mAngle * (j + 1) - mRotateAngle);
                float x1 = (float) (mPointCenter.x + angleSin1 * radius);
                float y1 = (float) (mPointCenter.y - angleCos1 * radius);
                mRadarPath.lineTo(x1, y1);
                linearGradient = getLinearGradient(x, y, x1, y1, data);


                //画网线
                if (mLayerLineWidth > 0) {
                    mRadarLinePaint.setStrokeWidth(SizeUtils.dp2px(mRadarWid));
                    mRadarLinePaint.setShader(linearGradient);
                    //圆角直线
                    mRadarLinePaint.setStrokeCap(Paint.Cap.ROUND);
                    canvas.drawPath(mRadarPath, mRadarLinePaint);
                }
                mRadarPath.close();
            }
        }
        mRadarLinePaint.reset();
    }

    public LinearGradient getLinearGradient(float x0, float y0, float x1, float y1, float data) {
        if(data <= 0){
            return new LinearGradient(x0, y0, x1, y1,
                    new int[]{0xFFF2F3F5, 0xFFF2F3F5, 0xFFF2F3F5}, new float[]{0f, 0.3f, 1f},Shader.TileMode.CLAMP);
        }else{
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                return new LinearGradient(x0, y0, x1, y1,
                        new int[]{getLinearGradientColor(data), getLinearGradientColor(data), COLOR_DEFOULT}, new float[]{0f, 0.3f, 1f},Shader.TileMode.CLAMP);
            }
        }
       if (data > 0 && data <= 1) {
            return new LinearGradient(x0, y0, x1, y1,
                    new int[]{COLOR_LEVEL_ONE, COLOR_LEVEL_ONE, COLOR_DEFOULT}, new float[]{0f, 0.3f, 1f},Shader.TileMode.CLAMP);
        } else if (data >1 && data <= 10) {
            return new LinearGradient(x0, y0, x1, y1,
                    new int[]{COLOR_LEVEL_TWO, COLOR_LEVEL_TWO, COLOR_DEFOULT}, new float[]{0f, 0.3f, 1f}, Shader.TileMode.CLAMP);
        } else if (data >10 && data <=50) {
            return new LinearGradient(x0, y0, x1, y1,
                    new int[]{COLOR_LEVEL_THREE, COLOR_LEVEL_THREE, COLOR_DEFOULT}, new float[]{0f, 0.3f, 1f}, Shader.TileMode.CLAMP);
        } else if (data >50 && data <= 200) {
            return new LinearGradient(x0, y0, x1, y1,
                    new int[]{COLOR_LEVEL_FOUL, COLOR_LEVEL_FOUL, COLOR_DEFOULT}, new float[]{0f, 0.3f, 1f}, Shader.TileMode.CLAMP);
        } else if (data > 200 && data <= 500) {
            return new LinearGradient(x0, y0, x1, y1,
                    new int[]{COLOR_LEVEL_FIVE, COLOR_LEVEL_FIVE, COLOR_DEFOULT}, new float[]{0f, 0.3f, 1f}, Shader.TileMode.CLAMP);
        } else {
            return new LinearGradient(x0, y0, x1, y1,
                    new int[]{COLOR_LEVEL_SIX, COLOR_LEVEL_SIX, COLOR_DEFOULT}, new float[]{0f, 0.3f, 1f}, Shader.TileMode.CLAMP);
        }
    }

    private void drawCircle(Canvas canvas) {
        for (int i = mLayer; i >= 1; i--) {
            float radius = mRadius / mLayer * i;
            int layerColor = mLayerColor.get(i - 1);
            if (layerColor != Color.TRANSPARENT) {
                mLayerPaint.setColor(layerColor);
                canvas.drawCircle(mPointCenter.x, mPointCenter.y, radius, mLayerPaint);
            }
            if (mLayerLineWidth > 0) {
                mRadarLinePaint.setColor(mLayerLineColor);
                mRadarLinePaint.setStrokeWidth(mLayerLineWidth);
                canvas.drawCircle(mPointCenter.x, mPointCenter.y, radius, mRadarLinePaint);
            }
        }
    }

    private void drawRadarLine(Canvas canvas) {
        //虚线
        mRadarLinePaint.setPathEffect(new DashPathEffect(new float[]{5, 5}, 0));
        for (int i = 1; i <= mMaxVertex; i++) {
            double angleSin = Math.sin(mAngle * i - mRotateAngle);
            double angleCos = Math.cos(mAngle * i - mRotateAngle);
            //这是外层的图片和文字
            //drawVertex(canvas, i, angleSin, angleCos);
            //这是画对角连线的
            drawVertexLine(canvas, angleSin, angleCos);
        }
        mRadarLinePaint.reset();
    }

    protected void drawVertexImpl(Canvas canvas, String text, Bitmap icon, Paint paint,
                                  float textY, float textX) {
        if (icon != null) {
            canvas.drawBitmap(icon, null, mVertexIconRect, paint);
        }
        if (!TextUtils.isEmpty(text)) {
            canvas.drawText(text, textX, textY, paint);
        }
    }

    private void drawVertexLine(Canvas canvas, double angleSin, double angleCos) {
        if (mVertexLineWidth <= 0) {
            return;
        }
        float x = (float) (mPointCenter.x + angleSin * (mRadius-SizeUtils.dp2px((mRadarWid+3))));
        float y = (float) (mPointCenter.y - angleCos * (mRadius-SizeUtils.dp2px((mRadarWid+3))));
        mRadarLinePaint.setColor(Color.argb(255, 219, 219, 219));
        mRadarLinePaint.setStrokeWidth(SizeUtils.dp2px(1));
        canvas.drawLine(mPointCenter.x, mPointCenter.y, x, y, mRadarLinePaint);
    }

    private void drawData(Canvas canvas) {
        boolean hasEmpty = false;
        boolean allEmpty = true;

        for (RadarData rd : mRadarData) {
            if (rd.getValue() == null || rd.getValue().get(0) == 0.0) {
                hasEmpty = true;
            } else {
                allEmpty = false;
                break;
            }
        }

        if (hasEmpty && !allEmpty) {
            for (RadarData rd : mRadarData) {
                if (rd.getValue() == null || rd.getValue().get(0) == 0.0) {
                    List<Float> list = new ArrayList<>();
                    list.add(1f);
                    rd.setValue(list);
                    break;
                }
            }
        }
        boolean boolColor = true;
        int dataI = 0;
        for (int i = 0; i < mRadarData.size(); i++) {
            RadarData radarData = mRadarData.get(i);
            mValuePaint.setColor(radarData.getColor());
            mValueTextPaint.setTextSize(radarData.getValueTextSize());
            List<Float> values = radarData.getValue();
            mRadarPath.reset();
            PointF[] textPoint = new PointF[mMaxVertex];
            ArrayList<Integer> colors = new ArrayList<>();
            Float percent ;
            for (int j = 1; j <= mMaxVertex; j++) {
                float value = 0;
                if (values.size() >= j) {
                    value = values.get(j - 1);
                    if (value == 0.0f && boolColor){
                        boolColor = false;
                        value = 1.0f;
                        dataI = j;
                        Integer color = getPointColor(0);
                        colors.add(color);
                    }
                }
                if (mMaxValues != null) {
                    percent = value / mMaxValues.get(j - 1);
                } else {
                    //percent = value / mMaxValue;
                    percent = getHexagonRadius(value);
                    if(value>0 && !boolColor){
                        Integer color = getPointColor(value);
                        colors.add(color);
                    }
                }
                if (percent.isInfinite()) {
                    percent = 1f;
                } else if (percent.isNaN()) {
                    percent = 0f;
                }
                if (percent > 1f) {
                    percent = 1f;
                }
                float x = (float) (mPointCenter.x + Math.sin(mAngle * j - mRotateAngle) * mRadius * percent);
                float y = (float) (mPointCenter.y - Math.cos(mAngle * j - mRotateAngle) * mRadius * percent);
                if (j == 1) {
                    mRadarPath.moveTo(x, y);
                } else {
                    mRadarPath.lineTo(x, y);
                }
                Log.i("RadarView","RadarView "+radarData.getValue());
                textPoint[j - 1] = new PointF(x, y);
            }
            mRadarPath.close();
            if(colors.size()>1){
                int[] colorArray = new int[]{};
                if(colors.size() == 6){
                    colorArray = new int[]{colors.get(0),colors.get(1),colors.get(2),colors.get(3),colors.get(4),colors.get(5)};
                }
                if(colors.size() == 5){
                    colorArray = new int[]{colors.get(0),colors.get(1),colors.get(2),colors.get(3),getPointColor(0)};
                }
                if(colors.size() == 4){
                    colorArray = new int[]{colors.get(0),colors.get(1),colors.get(2),getPointColor(0)};
                }
                if(colors.size() == 3){
                    colorArray = new int[]{colors.get(0),colors.get(1),getPointColor(0)};
                }
                if(colors.size() == 2){
                    colorArray = new int[]{colors.get(0),getPointColor(0)};
                }

                SweepGradient mColorShader = new SweepGradient(mPointCenter.x, mPointCenter.y, colorArray, null);

                mValuePaint.setShader(mColorShader);

            }
            mValuePaint.setAlpha(41);
            canvas.drawPath(mRadarPath, mValuePaint);
            canvas.drawCircle(mPointCenter.x, mPointCenter.y, 9f, mValuePaint);

            //画数据点
            boolean found = true;
            for (int k = 0; k < textPoint.length; k++) {
                float data = 0;
                if (mRadarData.get(0).getValueText().size() > k){
                     data = Float.parseFloat(mRadarData.get(0).getValueText().get(k));
                }
                if(data == 0f && data != Float.parseFloat(mRadarData.get(0).getValueText().get(0))) {
                    if (found) {
                        found = false;
                        Paint fillPaint = new Paint();
                        fillPaint.setColor(Color.WHITE);
                        fillPaint.setStyle(Paint.Style.FILL);
                        canvas.drawCircle(textPoint[k].x, textPoint[k].y, SizeUtils.dp2px(1.5f), fillPaint);
                        mValueTextPaint.setStyle(Paint.Style.STROKE);
                        mValueTextPaint.setColor(Color.argb(255, 219, 219, 219));
                        canvas.drawCircle(textPoint[k].x, textPoint[k].y, SizeUtils.dp2px(1.5f), mValueTextPaint);
                        continue;
                    }
                }
                mValueTextPaint.setStyle(Paint.Style.FILL);
                mValueTextPaint.setColor(getPointColor(data));
                canvas.drawCircle(textPoint[k].x, textPoint[k].y, SizeUtils.dp2px(1.5f), mValueTextPaint);
            }

        }
    }


    private int getPointColor(float data){
        if(data<=0){
            return 0x00000000;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return getLinearGradientColor(data);
        }
        if (data > 0 && data <= 1) {
            return COLOR_LEVEL_ONE;
        } else if (data >1 && data <=10) {
            return COLOR_LEVEL_TWO;
        } else if (data > 10 && data <= 50) {
            return COLOR_LEVEL_THREE;
        } else if (data > 50 && data <= 200) {
            return COLOR_LEVEL_FOUL;
        } else if (data > 200 && data <= 500) {
            return COLOR_LEVEL_FIVE;
        }else {
            return COLOR_LEVEL_SIX;
        }
    }

    private void drawCenterText(Canvas canvas) {
        if (!TextUtils.isEmpty(mCenterText)) {
            float textWidth = mCenterTextPaint.measureText(mCenterText);
            Paint.FontMetrics fontMetrics = mCenterTextPaint.getFontMetrics();
            float textHeight = fontMetrics.descent - fontMetrics.ascent;
            canvas.drawText(mCenterText, mPointCenter.x - textWidth / 2, mPointCenter.y + textHeight / 3, mCenterTextPaint);
        }
    }

    private void calcRadius() {
        if (mVertexText == null || mVertexText.size() == 0) {
            mRadius = Math.min(mPointCenter.x, mPointCenter.y) - mVertexTextOffset;
        } else {
            float maxWidth;
            if (mVertexIconPosition == VERTEX_ICON_POSITION_LEFT || mVertexIconPosition == VERTEX_ICON_POSITION_RIGHT) {
                maxWidth = (mVertexTextPaint.measureText(mMaxLengthVertexText) + mVertexIconMargin + mVertexIconSize) / 2;
            } else {
                maxWidth = Math.max(mVertexTextPaint.measureText(mMaxLengthVertexText), mVertexIconSize) / 2;
            }
            mRadius = Math.min(mPointCenter.x, mPointCenter.y) - (maxWidth + mVertexTextOffset);
            mPerimeter = 2 * Math.PI * mRadius;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                getParent().requestDisallowInterceptTouchEvent(mRotationEnable);
                break;
            case MotionEvent.ACTION_UP:
                getParent().requestDisallowInterceptTouchEvent(false);
                break;
        }
        if (!mRotationEnable) return super.onTouchEvent(event);
        return mDetector.onTouchEvent(event);
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDown(MotionEvent e) {
            if (!mScroller.isFinished()) {
                mScroller.forceFinished(true);
            }
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (Math.abs(velocityX) > Math.abs(velocityY)) {
                mFlingPoint = e2.getX();
                mScroller.fling((int) e2.getX(), 0, (int) velocityX, 0, (int) (-mPerimeter + e2.getX()),
                        (int) (mPerimeter + e2.getX()), 0, 0);
            } else if (Math.abs(velocityY) > Math.abs(velocityX)) {
                mFlingPoint = e2.getY();
                mScroller.fling(0, (int) e2.getY(), 0, (int) velocityY, 0, 0, (int) (-mPerimeter + e2.getY()),
                        (int) (mPerimeter + e2.getY()));
            }
            invalidate();
            return super.onFling(e1, e2, velocityX, velocityY);
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            double rotate = mRotateAngle;
            double dis = RotateUtil.getRotateAngle(new PointF(e2.getX() - distanceX, e2.getY() - distanceY)
                    , new PointF(e2.getX(), e2.getY()), mPointCenter);
            rotate += dis;
            handleRotate(rotate);
            mRotateOrientation = dis;
            return super.onScroll(e1, e2, distanceX, distanceY);
        }

    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            int x = mScroller.getCurrX();
            int y = mScroller.getCurrY();
            int max = Math.max(Math.abs(x), Math.abs(y));
            double rotateDis = RotateUtil.CIRCLE_ANGLE * (Math.abs(max - mFlingPoint) / mPerimeter);
            double rotate = mRotateAngle;
            if (mRotateOrientation > 0) {
                rotate += rotateDis;
            } else if (mRotateOrientation < 0) {
                rotate -= rotateDis;
            }
            handleRotate(rotate);
            mFlingPoint = max;
            invalidate();
        }
    }

    private void handleRotate(double rotate) {
        rotate = RotateUtil.getNormalizedAngle(rotate);
        mRotateAngle = rotate;
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mPointCenter = new PointF(w / 2, h / 2);
    }

    private float dp2px(float dpValue) {
        final float scale = mContext.getResources().getDisplayMetrics().density;
        return dpValue * scale + 0.5f;
    }


    @Deprecated
    /**
     use {@link RadarView#getLayerLineColor()} or {@link RadarView#getVertexLineColor()}
     */
    public int getRadarLineColor() {
        return -1;
    }

    @Deprecated
    /**
     use {@link RadarView#setLayerLineColor()} or {@link RadarView#setVertexLineColor()}
     */
    public void setRadarLineColor(int radarLineColor) {
    }

    @Deprecated
    /**
     use {@link RadarView#getLayerLineWidth()} or {@link RadarView#getVertexLineWidth()}
     */
    public float getRadarLineWidth() {
        return -1;
    }

    @Deprecated
    /**
     use {@link RadarView#setLayerLineWidth()} or {@link RadarView#setVertexLineWidth()}
     */
    public void setRadarLineWidth(float radarLineWidth) {
    }

    @Deprecated
    public boolean isRadarLineEnable() {
        return false;
    }

    @Deprecated
    public void setRadarLineEnable(boolean radarLineEnable) {
    }
}

