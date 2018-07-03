package com.higgsblock.global.chain.app;


import com.alibaba.fastjson.JSON;
import com.higgsblock.global.chain.app.common.SocketRequest;
import com.higgsblock.global.chain.common.utils.ExecutorServices;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @program: HiggsGlobal
 * @description:
 * @author: yezaiyong
 * @create: 2018-06-27 17:13
 **/
public class TimerUtil {

    private static int preHeight = 0;
    private static int currHeight = 0;
    private static int limitSec=0;
    private static int curSec=0;
    static Timer timer;
    private ExecutorService executorService;
    private static boolean isDom = false;

    public void StartTimer(int limitSec,int preHeight,int currHeight) throws InterruptedException {
        this.limitSec = limitSec;
        this.curSec = limitSec;
        //矿机重启，同步区块完成后，如果缓存高度等于当前高度 开始计时
        if (preHeight == currHeight){
            if(timer == null){
                StartTimer2(preHeight,currHeight);
            }else{
                if (curSec * -1 >10){
                    //判断是否已经打出区块，如果已经打出，再发送给见证
                    if (isDom){
                        System.out.println("休息十秒再把本地的区块发出去");
                        TimeUnit.SECONDS.sleep(10000);
                        TimerUtil.sendBlock();
                    }else{
                        TimerUtil.doMing();
                    }
                }
                System.out.println(curSec);
            }
        }
        //抛弃区块
        if (preHeight > currHeight){

        }
        //更新preHeight，清理计时，重新开始计时
        if (preHeight <currHeight){
            preHeight =currHeight;
            if (timer !=null){
                timer.cancel();
            }
            StartTimer2(preHeight,currHeight);
        }
    }

    public void StartTimer2(int preHeight,int currHeight) throws InterruptedException {
        //矿机重启，同步区块完成后，如果缓存高度等于当前高度 开始计时
        System.out.println("preHeight ="+preHeight+"currHeight"+currHeight);
        if (preHeight == currHeight){
            if(executorService == null){
                this.start(ExecutorServices.newSingleThreadExecutor(getClass().getName(), 100));
            }else{
                if (curSec >10){
                    //判断是否已经打出区块，如果已经打出，再发送给见证
                    if (isDom){
                        System.out.println("休息十秒再把本地的区块发出去");
                        TimeUnit.SECONDS.sleep(10000);
                        TimerUtil.sendBlock();
                    }else{
                        TimerUtil.doMing();
                    }
                }
                System.out.println(curSec);
            }
        }
        //抛弃区块
        if (preHeight > currHeight){

        }
        //更新preHeight，清理计时，重新开始计时
        System.out.println("preHeight ="+preHeight+"currHeight"+currHeight);
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

    public final synchronized void start(ExecutorService executorService) {
        this.executorService = executorService;
        this.executorService.execute(() -> {
            while(true){
                try {
                    ++curSec;
                    TimeUnit.SECONDS.sleep(1);
                    System.out.println("------------"+curSec);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

    }
    public static void CanltTimer(){
        timer.cancel();
    }

    public static void doMing(){
        System.out.println("时间到了 可以开始打区块了");
        isDom =true;
    }

    public static void sendBlock(){
        System.out.println("再发送区块给见证这");
    }


    public static void starCountTime(){
        timer=new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                int i = --curSec;
                System.out.println("Time remians " + i + " s");
                if (i * -1 == 10){
                    TimerUtil.doMing();
                }
            }
        }, 0, 1000);
    }

    public static void main(String[] args) throws InterruptedException {
        //获取当前高度
        preHeight = 100;
        currHeight =100;



        TimerUtil timeUnit = new TimerUtil();
        timeUnit.StartTimer2(preHeight,currHeight);
        TimeUnit.SECONDS.sleep(50);
        currHeight =101;
        timeUnit.StartTimer2(preHeight,currHeight);
        TimeUnit.SECONDS.sleep(50);
        preHeight =101;
//        System.out.println("当前高度"+currHeight+"pre高度"+preHeight);
        timeUnit.StartTimer2(preHeight,currHeight);
        //System.out.println("结束");
    }
}