package com.higgsblock.global.chain.app.contract;

import com.higgsblock.global.chain.app.dao.IContractRepository;
import com.higgsblock.global.chain.app.dao.entity.ContractEntity;
import com.higgsblock.global.chain.vm.datasource.DbSettings;
import com.higgsblock.global.chain.vm.datasource.DbSource;
import org.spongycastle.util.encoders.Hex;

import java.util.Map;
import java.util.Set;

public class ContractDataSource implements DbSource<byte[]> {
    private IContractRepository iContractRepository;

    public ContractDataSource(IContractRepository iContractRepository) {
        this.iContractRepository = iContractRepository;
    }

    @Override
    public void setName(String name) {

    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public void init() {

    }

    @Override
    public void init(DbSettings settings) {

    }

    @Override
    public boolean isAlive() {
        return false;
    }

    @Override
    public void close() {

    }

    @Override
    public Set<byte[]> keys() throws RuntimeException {
        return null;
    }

    @Override
    public void reset() {

    }

    @Override
    public byte[] prefixLookup(byte[] key, int prefixBytes) {
        return null;
    }


    private void updateBatchInternal(Map<byte[], byte[]> rows)  {
        for (Map.Entry<byte[], byte[]> entry : rows.entrySet()) {
            if (entry.getValue() == null) {
                String strKey = Hex.toHexString(entry.getKey());
                iContractRepository.delete(strKey);
            } else {
                System.out.println("key===" + Hex.toHexString(entry.getKey()) +
                        "--- value : " + Hex.toHexString(entry.getValue()));
                String strKey = Hex.toHexString(entry.getKey());
                String strValue = Hex.toHexString(entry.getValue());
                ContractEntity entity = new ContractEntity(strKey, strValue);
                iContractRepository.save(entity);
            }
        }
    }

    @Override
    public void updateBatch(Map<byte[], byte[]> rows) {
        updateBatchInternal(rows);
    }

    @Override
    public void put(byte[] key, byte[] val) {

    }

    @Override
    public byte[] get(byte[] key) {
        String strKey = Hex.toHexString(key);
        ContractEntity entity = iContractRepository.findOne(strKey);

        if (entity != null) {
            return Hex.decode(entity.getValue());
        }

        return null;
    }

    @Override
    public void delete(byte[] key) {

    }

    @Override
    public boolean flush() {
        return false;
    }
}
