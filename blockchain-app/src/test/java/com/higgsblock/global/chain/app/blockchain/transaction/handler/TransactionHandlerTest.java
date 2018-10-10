package com.higgsblock.global.chain.app.blockchain.transaction.handler;

import com.higgsblock.global.chain.app.BaseMockTest;
import com.higgsblock.global.chain.app.blockchain.script.LockScript;
import com.higgsblock.global.chain.app.blockchain.script.UnLockScript;
import com.higgsblock.global.chain.app.blockchain.transaction.Transaction;
import com.higgsblock.global.chain.app.blockchain.transaction.TransactionInput;
import com.higgsblock.global.chain.app.blockchain.transaction.TransactionOutPoint;
import com.higgsblock.global.chain.app.blockchain.transaction.TransactionOutput;
import com.higgsblock.global.chain.app.net.message.BizMessage;
import com.higgsblock.global.chain.app.service.ITransactionService;
import com.higgsblock.global.chain.app.utils.GetTransactionTestObj;
import com.higgsblock.global.chain.common.utils.Money;
import com.higgsblock.global.chain.network.socket.message.IMessage;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Su Jiulong
 * @date 2018/9/27
 */
public class TransactionHandlerTest extends BaseMockTest {

    @InjectMocks
    private TransactionHandler transactionHandler;

    @Mock
    private ITransactionService transactionService;

    @Test
    public void validSuccess() {
        Transaction transaction = GetTransactionTestObj.getSingleTransaction();
        IMessage<Transaction> message = new BizMessage<Transaction>("sourceId", transaction);
        Assert.assertTrue(transactionHandler.valid(message));
    }

    @Test
    public void validFailed() {
        Transaction transaction = new Transaction();
        //version invalid
        transaction.setVersion(-1);
        IMessage<Transaction> message = new BizMessage<Transaction>("sourceId", transaction);
        Assert.assertFalse(transactionHandler.valid(message));

        transaction.setVersion(0);
        //lock time invalid
        transaction.setLockTime(-1);
        Assert.assertFalse(transactionHandler.valid(message));

        transaction.setLockTime(0);
        //transaction input invalid
        invalidTxInputs(transaction, message);

        //transaction output invalid
        invalidTxOutputs(transaction, message);

        //invalid transaction size
        invalidTxSize(transaction, message);
    }

    @Test
    public void process() {
        IMessage<Transaction> message = new BizMessage<Transaction>("sourceId", new Transaction());
        PowerMockito.doNothing().when(transactionService).receivedTransaction(message.getData());
        transactionHandler.process(message);
    }

    private void invalidTxInputs(Transaction transaction, IMessage<Transaction> message) {
        List<TransactionInput> transactionInputs = new ArrayList<>(1);
        //The input does not contain preOut and unlock script
        TransactionInput txInput = new TransactionInput();
        transactionInputs.add(txInput);
        transaction.setInputs(transactionInputs);
        Assert.assertFalse(transactionHandler.valid(message));

        //only set TransactionOutPoint
        TransactionOutPoint outPoint = new TransactionOutPoint();
        txInput.setPrevOut(outPoint);
        Assert.assertFalse(transactionHandler.valid(message));
        //add UnLockScript
        UnLockScript unLockScript = new UnLockScript();
        txInput.setUnLockScript(unLockScript);
        Assert.assertFalse(transactionHandler.valid(message));

        //preOut valid success and unlock script invalid
        outPoint.setIndex((short) 0);
        outPoint.setTransactionHash("previous transaction hash");
        Assert.assertFalse(transactionHandler.valid(message));

        //Set the public key list and signature list for the unlock script, signature list size is equal zero.
        List<String> pkList = new ArrayList<>(2);
        pkList.add("pk1");
        pkList.add("pk2");
        unLockScript.setPkList(pkList);
        //signature list size more than public key list size
        List<String> sigList = new ArrayList<>(3);
        sigList.add("signature1");
        sigList.add("signature2");
        sigList.add("signature3");
        unLockScript.setSigList(sigList);
        Assert.assertFalse(transactionHandler.valid(message));
    }

    private void invalidTxOutputs(Transaction transaction, IMessage<Transaction> message) {
        //Ensure input validation passes
        transaction.getInputs().get(0).getUnLockScript().getSigList().remove(2);

        List<TransactionOutput> outputs = new ArrayList<>(1);
        TransactionOutput txOutput = new TransactionOutput();
        outputs.add(txOutput);
        transaction.setOutputs(outputs);

        //money invalid
        Money money = new Money(-1);
        txOutput.setMoney(money);
        Assert.assertFalse(transactionHandler.valid(message));

        money.setValue("1");
        //lock script is null
        Assert.assertFalse(transactionHandler.valid(message));

        //address is null of lock script
        LockScript lockScript = new LockScript();
        txOutput.setLockScript(lockScript);
        Assert.assertFalse(transactionHandler.valid(message));

        String address = "get money address";
        lockScript.setAddress(address);
        Assert.assertTrue(transactionHandler.valid(message));
    }

    private void invalidTxSize(Transaction transaction, IMessage<Transaction> message) {
        final boolean success = assembleExtra(transaction);
        if (success) {
            Assert.assertFalse(transactionHandler.valid(message));
        }
    }

    public static boolean assembleExtra(Transaction transaction) {
        StringBuilder builder = null;
        InputStreamReader reader = null;
        BufferedReader bufferedReader = null;
        try {
            reader = new InputStreamReader(new FileInputStream(new File("C:\\test.pdf")), "utf-8");
            bufferedReader = new BufferedReader(reader, 500 * 1024);
            String string = null;
            builder = new StringBuilder();
            while ((string = bufferedReader.readLine()) != null) {
                builder.append(string);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        if (builder != null && builder.length() > 0) {
            transaction.setExtra(builder.toString());
            return true;
        }
        return false;
    }
}