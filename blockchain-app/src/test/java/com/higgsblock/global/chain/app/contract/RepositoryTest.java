package com.higgsblock.global.chain.app.contract;

import com.higgsblock.global.chain.vm.DataWord;
import com.higgsblock.global.chain.vm.core.Repository;
import com.higgsblock.global.chain.vm.datasource.HashMapDB;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

import static org.junit.Assert.assertEquals;

public class RepositoryTest {

    @Test
    public void test4() {

        RepositoryRoot repository = new RepositoryRoot("");
        Repository track = repository.startTracking();

        byte[] cow = Hex.decode("CD2A3D9F938E13CD947EC05ABC7FE734DF8DD826");
        byte[] horse = Hex.decode("13978AEE95F38490E9769C39B2773ED763D9CD5F");

        byte[] cowKey = Hex.decode("A1A2A3");
        byte[] cowValue = Hex.decode("A4A5A6");

        byte[] horseKey = Hex.decode("B1B2B3");
        byte[] horseValue = Hex.decode("B4B5B6");

        track.addStorageRow(cow, new DataWord(cowKey), new DataWord(cowValue));
        track.addStorageRow(horse, new DataWord(horseKey), new DataWord(horseValue));
        track.commit();

        assertEquals(new DataWord(cowValue), repository.getStorageValue(cow, new DataWord(cowKey)));
        assertEquals(new DataWord(horseValue), repository.getStorageValue(horse, new DataWord(horseKey)));

        repository.close();
    }
}
