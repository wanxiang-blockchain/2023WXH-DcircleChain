package com.base.baseui.widget.others;

import android.content.Context;
import android.content.res.TypedArray;
import android.media.Image;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.base.baseui.R;

/**
 * @author yangfei
 * @time 2023/3/2
 * @desc
 */
public class DialogTitleView extends FrameLayout {

    private TextView tvTitle;
    private ImageView imgInfo;

    private ImageView imgExit;

    public DialogTitleView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DialogTitleView(@NonNull Context context) {
        this(context,null);
    }

    public DialogTitleView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.DialogTitleView);
        String titleText = typedArray.getString(R.styleable.DialogTitleView_titleText);
        boolean vis = typedArray.getBoolean(R.styleable.DialogTitleView_infoVisable,false);
        int iconId = typedArray.getResourceId(R.styleable.DialogTitleView_inifoIcon,R.mipmap.ic_basic_info);
        boolean exitShow = typedArray.getBoolean(R.styleable.DialogTitleView_showExit,false);
        init(context,titleText,vis,iconId,exitShow);
    }

    private void init(Context context, String title, boolean vis, int iconId, boolean exitShow) {
        inflate(context, R.layout.dialog_header, this);
        tvTitle = findViewById(R.id.tv_dialog_title);
        imgInfo = findViewById(R.id.ic_help);
        imgExit = findViewById(R.id.ic_close);

        if(!TextUtils.isEmpty(title)){
            tvTitle.setText(title);
        }
        if (exitShow){
            imgExit.setVisibility(View.VISIBLE);
        }else {
            imgExit.setVisibility(View.GONE);
        }
        if (vis){
            imgInfo.setVisibility(View.VISIBLE);
        }else {
            imgInfo.setVisibility(View.GONE);
        }
        imgInfo.setImageResource(iconId);
    }

    public ImageView getImgExit() {
        return imgExit;
    }

    public ImageView getImgInfo() {
        return imgInfo;
    }

    public void setTitle(String title){
        if(!TextUtils.isEmpty(title)){
            tvTitle.setText(title);
        }
    }
}
