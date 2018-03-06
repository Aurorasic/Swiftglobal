package cn.primeledger.cas.global.common.entity;

        import lombok.Data;

/**
 * @author baizhengwen
 * @date 2018/2/28
 */
@Data
public abstract class BaseEntity<T> {
    private short type;
    private short version;
    private T data;
}
