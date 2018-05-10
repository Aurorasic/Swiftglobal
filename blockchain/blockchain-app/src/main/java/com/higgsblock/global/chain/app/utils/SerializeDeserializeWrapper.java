package com.higgsblock.global.chain.app.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author yuanjiantao
 * @date Created in 3/1/2018
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SerializeDeserializeWrapper<T> {

    private T data;


    public static <T> SerializeDeserializeWrapper<T> builder(T data) {
        return new SerializeDeserializeWrapper<>(data);
    }

}
