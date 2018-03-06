package cn.primeledger.cas.global.utils;

import com.google.common.collect.Maps;
import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author yuanjiantao
 * @date Created in 2/28/2018
 */
@Slf4j
public class ProtoBufUtil {

    private static final Set<Class<?>> WRAPPER_SET = new HashSet<>();

    private static final Class<SerializeDeserializeWrapper> WRAPPER_CLASS = SerializeDeserializeWrapper.class;

    private static final Schema<SerializeDeserializeWrapper> WRAPPER_SCHEMA = RuntimeSchema.createFrom(WRAPPER_CLASS);

    private static final Map<Class<?>, Schema<?>> CACHE_SCHEMA = Maps.newConcurrentMap();

    static {
        WRAPPER_SET.add(List.class);
        WRAPPER_SET.add(ArrayList.class);
        WRAPPER_SET.add(CopyOnWriteArrayList.class);
        WRAPPER_SET.add(LinkedList.class);
        WRAPPER_SET.add(Stack.class);
        WRAPPER_SET.add(Vector.class);
        WRAPPER_SET.add(Map.class);
        WRAPPER_SET.add(HashMap.class);
        WRAPPER_SET.add(TreeMap.class);
        WRAPPER_SET.add(Hashtable.class);
        WRAPPER_SET.add(SortedMap.class);
        WRAPPER_SET.add(Map.class);
        WRAPPER_SET.add(Object.class);
    }

    public static void registerWrapperClass(Class clazz) {
        WRAPPER_SET.add(clazz);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static <T> Schema<T> getSchema(Class<T> cls) {
        Schema<T> schema = (Schema<T>) CACHE_SCHEMA.get(cls);
        if (schema == null) {
            schema = RuntimeSchema.createFrom(cls);
            CACHE_SCHEMA.put(cls, schema);
        }
        return schema;
    }

    @SuppressWarnings("unchecked")
    public static <T> byte[] serialize(T obj) {
        SerializeDeserializeWrapper wrapper = SerializeDeserializeWrapper.builder(obj);
        Class<T> clazz = (Class<T>) wrapper.getClass();
        LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
        try {
            Object serializeObject = wrapper;
            Schema schema = WRAPPER_SCHEMA;
            if (!WRAPPER_SET.contains(clazz)) {
                schema = getSchema(clazz);
            } else {
                serializeObject = SerializeDeserializeWrapper.builder(wrapper);
            }
            return ProtostuffIOUtil.toByteArray(serializeObject, schema, buffer);
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        } finally {
            buffer.clear();
        }
    }


    @SuppressWarnings("unchecked")
    public static <T> T deserialize(byte[] data) {
        Class<SerializeDeserializeWrapper> clazz = SerializeDeserializeWrapper.class;
        try {
            if (!WRAPPER_SET.contains(clazz)) {
                SerializeDeserializeWrapper message = clazz.newInstance();
                Schema<SerializeDeserializeWrapper> schema = getSchema(clazz);
                ProtostuffIOUtil.mergeFrom(data, message, schema);
                return (T) message.getData();
            } else {
                SerializeDeserializeWrapper wrapper = new SerializeDeserializeWrapper<>();
                ProtostuffIOUtil.mergeFrom(data, wrapper, WRAPPER_SCHEMA);
                return (T) wrapper.getData();
            }
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }
}
