package cn.primeledger.cas.global.common.formatter;

import cn.primeledger.cas.global.constants.EntityType;
import com.google.common.base.Preconditions;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * @author baizhengwen
 * @date 2018/3/27
 */
public abstract class BaseEntityFormatter<T> implements IEntityFormatter<T> {

    private static final String SEPARATOR = "|";

    public static EntityType parseType(String data) {
        String type = StringUtils.substringBefore(data, SEPARATOR);
        return EntityType.getByCode(NumberUtils.toShort(type));
    }

    @Override
    public Class<T> getEntityClass() {
        Type type = getClass().getGenericSuperclass();
        if (type instanceof ParameterizedType) {
            Type[] entityClass = ((ParameterizedType) type).getActualTypeArguments();
            if (null != entityClass) {
                return (Class<T>) entityClass[0];
            }
        }
        return null;
    }

    @Override
    public final T parse(String data) {
        String type = StringUtils.substringBefore(data, SEPARATOR);
        Preconditions.checkState(getType().getCode() == NumberUtils.toShort(type), "type invalid");
        String content = StringUtils.substringAfter(data, SEPARATOR);
        return doParse(content);
    }

    @Override
    public final String format(T data) {
        return String.format("%s|%s", getType().getCode(), doFormat(data));
    }

    /**
     * parse object exclude type
     *
     * @param data
     * @return
     */
    protected abstract T doParse(String data);

    /**
     * format object exclude type
     *
     * @param data
     * @return
     */
    protected abstract String doFormat(T data);
}
