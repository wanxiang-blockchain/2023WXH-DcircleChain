package com.base.baseui.widget.others.radar;

/**
 * @author yangfei
 * @time 2023/5/19
 * @desc
 */
public enum RadarType {
    Article("article"),
    User("user");

    private final String type;
    RadarType(String type){
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
