package com.higgsblock.global.chain.app.blockchain;

import com.higgsblock.global.chain.app.common.handler.BaseEntityHandler;
import com.higgsblock.global.chain.common.utils.ExecutorServices;
import lombok.extern.slf4j.Slf4j;

import java.util.Timer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @program: HiggsGlobal
 * @description:
 * @author: yezaiyong
 * @create: 2018-06-29 14:31
 **/
@Slf4j
public class CandidateMiner{
    private int preHeight =0;
    private int currHeight = 0;
    private int curSec;
    private boolean isDom = false;
    private ExecutorService executorService;
    private volatile boolean isRunning ;

    public void StartTimer(int preHeight,int currHeight) throws InterruptedException {
        //矿机重启，同步区块完成后，如果缓存高度等于当前高度 开始计时
        LOGGER.info("preHeight ="+preHeight+"currHeight"+currHeight);
        if (preHeight == currHeight){
            if(executorService == null){
                this.start(ExecutorServices.newScheduledThreadPool(getClass().getName(), 1));
            }else{
                if (curSec >10){
                    //判断是否已经打出区块，如果已经打出，再发送给见证
                    if (isDom){
                        LOGGER.info("休息十秒再把本地的区块发出去");
                        TimeUnit.SECONDS.sleep(10000);
                        sendBlock();
                    }else{
                        doMing();
                    }
                }
                System.out.println(curSec);
            }
        }
        //抛弃区块
        if (preHeight > currHeight){

        }
        //更新preHeight，清理计时，重新开始计时
        LOGGER.info("preHeight ="+preHeight+"currHeight"+currHeight);
        if (preHeight <currHeight){
            this.preHeight = currHeight;
            this.curSec =0;
            if (executorService !=null){
                executorService.shutdown();
                System.out.println("线程已经关闭");
            }
            this.start(ExecutorServices.newSingleThreadExecutor(getClass().getName(), 100));


        }
    }

    public void doMing(){
        System.out.println("时间到了 可以开始打区块了");
        //isDom =true;
    }

    public static void sendBlock(){
        System.out.println("再发送区块给见证这");
    }

    public final synchronized void start(ExecutorService executorService) {
        if (!isRunning){
            this.executorService = executorService;
            this.executorService.execute(() -> {
                while(isRunning){
                    try {
                        ++curSec;
                        TimeUnit.SECONDS.sleep(1);
                        System.out.println("------------"+curSec);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
            isRunning = true;
        }

    }
}