package com.yhtech.image_preview.ui.widget;

import android.animation.TimeInterpolator;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.view.animation.AccelerateInterpolator;


public class QMUISwipeAction {
   public static String TYPE_DELETE="delect";
    public static String TYPE_NOTIFY = "notify";
    public  static String TYPE_MOVE="typemove";
    private String type;
    private final String mText;
    Drawable mIcon;
    int mTextSize;
    Typeface mTypeface;
    int mSwipeDirectionMiniSize;
    int mIconTextGap;
    int mTextColor;
    int mTextColorAttr;
    int mBackgroundColor;
    int mBackgroundColorAttr;
    int mIconAttr;
    boolean mUseIconTint;
    int mPaddingStartEnd;
    int mOrientation;
    boolean mReverseDrawOrder;
    TimeInterpolator mSwipeMoveInterpolator;
    int mSwipePxPerMS;


    // inner use for layout and draw
    Paint paint;
    float contentWidth;
    float contentHeight;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }



    private QMUISwipeAction(ActionBuilder builder) {
        mText = builder.mText != null && builder.mText.length() > 0 ? builder.mText : null;
        mTextColor = builder.mTextColor;
        mTextSize = builder.mTextSize;
        mTypeface = builder.mTypeface;
        mTextColorAttr = builder.mTextColorAttr;
        mIcon = builder.mIcon;
        mIconAttr = builder.mIconAttr;
        mUseIconTint = builder.mUseIconTint;
        mIconTextGap = builder.mIconTextGap;
        mBackgroundColor = builder.mBackgroundColor;
        mBackgroundColorAttr = builder.mBackgroundColorAttr;
        mPaddingStartEnd = builder.mPaddingStartEnd;
        mSwipeDirectionMiniSize = builder.mSwipeDirectionMiniSize;
        mOrientation = builder.mOrientation;
        mReverseDrawOrder = builder.mReverseDrawOrder;
        mSwipeMoveInterpolator = builder.mSwipeMoveInterpolator;
        mSwipePxPerMS = builder.mSwipePxPerMS;

        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setTypeface(mTypeface);
        paint.setTextSize(mTextSize);
        Paint.FontMetrics fontMetrics = paint.getFontMetrics();
        if (mIcon != null && mText != null) {
            mIcon.setBounds(0, 0, mIcon.getIntrinsicWidth(), mIcon.getIntrinsicHeight());
            if (mOrientation == ActionBuilder.HORIZONTAL) {
                contentWidth = mIcon.getIntrinsicWidth() + mIconTextGap + paint.measureText(mText);
                contentHeight = Math.max(fontMetrics.descent - fontMetrics.ascent, mIcon.getIntrinsicHeight());
            } else {
                contentWidth = Math.max(mIcon.getIntrinsicWidth(), paint.measureText(mText));
                contentHeight = fontMetrics.descent - fontMetrics.ascent + mIconTextGap + mIcon.getIntrinsicHeight();
            }
        } else if (mIcon != null) {
            mIcon.setBounds(0, 0, mIcon.getIntrinsicWidth(), mIcon.getIntrinsicHeight());
            contentWidth = mIcon.getIntrinsicWidth();
            contentHeight = mIcon.getIntrinsicHeight();
        } else if (mText != null) {
            contentWidth = paint.measureText(mText);
            contentHeight = fontMetrics.descent - fontMetrics.ascent;
        }
    }

    public String getText() {
        return mText;
    }

    public int getTextColor() {
        return mTextColor;
    }

    public int getTextSize() {
        return mTextSize;
    }


    protected void draw(Canvas canvas) {
        if (mText != null && mIcon != null) {
            if (mOrientation == ActionBuilder.HORIZONTAL) {
                if (mReverseDrawOrder) {
                    canvas.drawText(mText, 0,
                            (contentHeight - paint.descent() + paint.ascent()) / 2 - paint.ascent(),
                            paint);
                    canvas.save();
                    canvas.translate(contentWidth - mIcon.getIntrinsicWidth(), (contentHeight - mIcon.getIntrinsicHeight()) / 2);
                    mIcon.draw(canvas);
                    canvas.restore();
                } else {
                    canvas.save();
                    canvas.translate(0, (contentHeight - mIcon.getIntrinsicHeight()) / 2);
                    mIcon.draw(canvas);
                    canvas.restore();
                    canvas.drawText(mText,
                            mIcon.getIntrinsicWidth() + mIconTextGap,
                            (contentHeight - paint.descent() + paint.ascent()) / 2 - paint.ascent(),
                            paint);
                }

            } else {
                float textWidth = paint.measureText(mText);
                if (mReverseDrawOrder) {
                    canvas.drawText(mText, (contentWidth - textWidth) / 2, -paint.ascent(), paint);
                    canvas.save();
                    canvas.translate(
                            (contentWidth - mIcon.getIntrinsicWidth()) / 2,
                            contentHeight - mIcon.getIntrinsicHeight());
                    mIcon.draw(canvas);
                    canvas.restore();
                } else {
                    canvas.save();
                    canvas.translate((contentWidth - mIcon.getIntrinsicWidth()) / 2, 0);
                    mIcon.draw(canvas);
                    canvas.restore();
                    canvas.drawText(mText, (contentWidth - textWidth) / 2, contentHeight - paint.descent(), paint);
                }
            }
        } else if (mIcon != null) {
            mIcon.draw(canvas);
        } else if (mText != null) {
            canvas.drawText(mText, 0, -paint.ascent(), paint);
        }

    }

    public static class ActionBuilder {
        public static final int VERTICAL = 1;
        public static final int HORIZONTAL = 2;
        String mText;
        Drawable mIcon;
        int mTextSize;
        Typeface mTypeface;
        int mSwipeDirectionMiniSize;
        int mIconTextGap;
        int mTextColor;
        int mTextColorAttr = 0;
        int mBackgroundColor;
        int mBackgroundColorAttr = 0;
        int mIconAttr = 0;
        boolean mUseIconTint = false;
        int mPaddingStartEnd = 0;
        int mOrientation = VERTICAL;
        boolean mReverseDrawOrder = false;
        TimeInterpolator mSwipeMoveInterpolator = new AccelerateInterpolator();
        int mSwipePxPerMS = 2;

        public ActionBuilder text(String text) {
            mText = text;
            return this;
        }

        public ActionBuilder textSize(int textSize) {
            mTextSize = textSize;
            return this;
        }

        public ActionBuilder textColor(int textColor) {
            mTextColor = textColor;
            return this;
        }

        public QMUISwipeAction build() {
            return new QMUISwipeAction(this);
        }
    }
}
