package com.base.foundation.utils;

public interface IIntEnum extends IEnum<Integer> {

    static <E extends Enum<?> & IIntEnum> E valueOf(Class<E> enumClass, Integer code) {
        E[] enumConstants = enumClass.getEnumConstants();
        if(enumConstants == null){
            return null;
        }
        for (E enumConstant : enumConstants) {
            if(enumConstant.getValue() != null && enumConstant.getValue().equals(code)){
                return enumConstant;
            }
        }
        for (E enumConstant : enumConstants) {
            if(enumConstant.getValue() == null){
                return enumConstant;
            }
        }
        return null;
    }
}
