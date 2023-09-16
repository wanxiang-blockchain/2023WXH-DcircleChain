package com.base.foundation.db.enums;

import com.base.foundation.utils.IIntEnum;

public enum Status implements IIntEnum {
    HasReadAck( 0x01),
    Deleted (0x1 << 16);

    private final int value;

    Status(int value) {
        this.value = value;
    }

    @Override
    public Integer getValue() {
        return value;
    }
}
