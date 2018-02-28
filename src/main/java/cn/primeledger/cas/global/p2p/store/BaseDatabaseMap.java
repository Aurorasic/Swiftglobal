package cn.primeledger.cas.global.p2p.store;

import org.mapdb.DB;

import java.util.Map;


/**
 * Base map for the database mapping to different objects.
 *
 * @author zhao xiaogang
 */
public abstract class BaseDatabaseMap<T, U> {
    protected Map<T, U> map;

    public BaseDatabaseMap(DB database) {
        this.map = getMap(database);
    }

    /**
     * Return the db map
     *
     * @param database
     * @return db map
     */
    protected abstract Map<T, U> getMap(DB database);
}
