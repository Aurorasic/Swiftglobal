package com.higgsblock.global.chain.app.utils;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

/**
 * @author chenjiawei
 * @date 2018-03-28
 */
public class ObjectSizeCounter implements SizeCounter {
    private Unsafe unsafe = getUnsafe();

    private Unsafe getUnsafe() {
        try {
            Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            return (Unsafe) field.get(null);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public long calculateSize(Object o) {
        //TODO: bytes sum of superclass and nested fields。
        return 0;
    }

    private long calculateShallowSize(Object o) {
        Field[] fields = o.getClass().getDeclaredFields();
        if (fields.length == 0) {
            return 0;
        }

        Field last = null;
        long offset = 0;
        long temp;
        for (Field field : fields) {
            temp = unsafe.objectFieldOffset(field);
            if (offset < temp) {
                offset = temp;
                last = field;
            }
        }

        long result = offset + getShallowSize(last.getType());

        return result % 8 == 0 ? result : (result / 8 + 1) * 8;
    }

    private int getShallowSize(Class<?> clazz) {
        if (clazz == boolean.class) {
            return 1;
        } else if (clazz == char.class) {
            return 2;
        } else if (clazz == byte.class) {
            return 1;
        } else if (clazz == short.class) {
            return 2;
        } else if (clazz == int.class) {
            return 4;
        } else if (clazz == long.class) {
            return 8;
        } else if (clazz == float.class) {
            return 4;
        } else if (clazz == double.class) {
            return 8;
        } else {
            return 4;
        }
    }
}
