package com.base.baseui.widget.others;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.base.baseui.R;
import com.blankj.utilcode.util.ActivityUtils;

/**
 * @author yangfei
 * @time 2023/4/14
 * @desc
 */
public class TitleView extends FrameLayout {

    private TextView tvTitle;
    private ImageView ivBack;

    private ImageView ivRight;
    private ImageView ivRightSecond;
    private View viewDivider;
    private ConstraintLayout clContent;
    public TitleView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TitleView(@NonNull Context context) {
        this(context,null);
    }

    public TitleView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.TitleView);
        String titleText = typedArray.getString(R.styleable.TitleView_title);
        boolean showDivider = typedArray.getBoolean(R.styleable.TitleView_dividerShow,true);
        inflate(context, R.layout.title_view, this);
        init(titleText,showDivider);
    }

    private void init(String title, boolean showDivider){
        tvTitle = findViewById(R.id.titleview_title);
        ivBack = findViewById(R.id.iv_back);
        ivRight = findViewById(R.id.iv_right_image);
        clContent = findViewById(R.id.cl_content);

        ivRightSecond = findViewById(R.id.iv_right_second_image);
        viewDivider = findViewById(R.id.iv_did);
        if(!TextUtils.isEmpty(title)){
            tvTitle.setText(title);
        }
        ivBack.setOnClickListener(v -> {
            if (ActivityUtils.getTopActivity()!=null){
                ActivityUtils.getTopActivity().finish();//无需外部设置点击事件
            }
        });
        if (showDivider){
            viewDivider.setVisibility(View.VISIBLE);
        }else {
            viewDivider.setVisibility(View.GONE);
        }

    }

    public void setOnBackListener(OnClickListener listener){
        if(listener != null){
            ivBack.setOnClickListener(listener);
        }
    }

    public void setRightSecondImageViewVisibility(int visibility){
        ivRightSecond.setVisibility(visibility);
    }

    public void setRightSecondImageView(@DrawableRes int icon, OnClickListener listener){
        ivRightSecond.setImageResource(icon);
        ivRightSecond.setOnClickListener(listener);
    }

    public void setRightImageView(@DrawableRes int icon, OnClickListener listener){
        ivRight.setImageResource(icon);
        ivRight.setOnClickListener(listener);
    }

    public void setRightImageViewVisibility(int visibility){
        ivRight.setVisibility(visibility);
    }

    public void setLeftImage(@DrawableRes int icon){
        ivBack.setImageResource(icon);
    }

    public void setTitle(String title){
        if(!TextUtils.isEmpty(title)){
            tvTitle.setText(title);
        }
    }

    public void setTitleColor(Integer color){
        tvTitle.setTextColor(color);
    }

    public void setContentBackgroundColor(Integer color){
        clContent.setBackgroundColor(color);
    }
}
