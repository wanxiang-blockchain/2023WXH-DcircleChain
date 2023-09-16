package com.base.foundation.utils;

import java.io.Serializable;

public interface IEnum<T> extends Serializable {
    T getValue();
}
