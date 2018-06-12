package com.higgsblock.global.chain.app.utils;

import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Model converter.
 *
 *  * @author zhao xiaogang
 *  * @date 2018-05-10
 */
public class ModelConverter {

    public static void  convertToList(List<Object> source, List<Object> target){
        if (null == source||source.size()==0|| null == target) {
            return ;
        }

        target.clear();
        for (Object obj:source){
            Object tar = new Object();
            BeanUtils.copyProperties(obj, tar);
            target.add(tar);
        }

    }

    public static <T> List<T>  convertList(List<?> source,Class<T> clz){
        List<T> retList = new ArrayList<>();
        for (Object obj:source){
            try {
                T t = (T)clz.newInstance();
                BeanUtils.copyProperties(obj, t);
                retList.add(t);
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
                return null;
            }
        }
        return retList;
    }
}
