package com.base.foundation.db.enums;

import com.base.foundation.utils.IIntEnum;

public enum EncryptedType implements IIntEnum {
    Json(0),
    Aes(1);

    private final int value;

    EncryptedType(int value) {
        this.value = value;
    }

    @Override
    public Integer getValue() {
        return value;
    }
}
