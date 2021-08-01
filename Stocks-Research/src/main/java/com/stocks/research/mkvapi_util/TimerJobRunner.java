/*     */ package com.stocks.research.mkvapi_util;
/*     */ 
/*     */ import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/*     */
/*     */
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public class TimerJobRunner
/*     */ {
/*  13 */   private static final Logger log = LoggerFactory.getLogger(TimerJobRunner.class);
/*     */   
/*     */   int timePreiodMs;
/*     */   int ratePerTimePeriod;
/*     */   TimerJob timerJob;
/*     */   
/*     */   public void start(int ratePerTimePeriod, int timePreiodMs, TimerJob timerListener) {
/*  20 */     this.timePreiodMs = timePreiodMs;
/*  21 */     this.ratePerTimePeriod = ratePerTimePeriod;
/*  22 */     this.timerJob = timerListener;
/*  23 */     runJob();
/*     */   }
/*     */ 
/*     */   
/*     */   private void runJob() {
/*  28 */     long counter = 0L;
/*  29 */     long pauseMs = 0L;
/*     */     
/*  31 */     log.info("running job: timePreiodMs={} ratePerTimePeriod={}", Integer.valueOf(this.timePreiodMs), Integer.valueOf(this.ratePerTimePeriod));
/*     */ 
/*     */     
/*     */     while (true) {
/*  35 */       long startTime = System.currentTimeMillis();
/*  36 */       for (int i = 0; i < this.ratePerTimePeriod; i++) {
/*     */         try {
/*  38 */           if (!this.timerJob.onNextTime(counter++)) {
/*  39 */             log.info("timer job returned false, will stop, counter={}", Long.valueOf(counter));
/*     */             return;
/*     */           } 
/*  42 */           if (pauseMs > 0L) {
/*  43 */             Thread.sleep(pauseMs);
/*     */           }
/*     */         }
/*  46 */         catch (Exception e) {
/*  47 */           log.error("exception from job runner, will stop", e);
/*     */           
/*     */           return;
/*     */         } 
/*     */       } 
/*     */       
/*  53 */       long endTime = System.currentTimeMillis();
/*  54 */       long timeToSleep = this.timePreiodMs - endTime - startTime;
/*     */       
/*     */       try {
/*  57 */         if (timeToSleep < 0L) {
/*  58 */           if (pauseMs == 0L) {
/*  59 */             log.warn("adjust-pause-time: can't adjust, Job takes too long to run, can't run at required rate!");
/*     */             continue;
/*     */           } 
/*  62 */           long changePauseTime = timeToSleep / this.ratePerTimePeriod;
/*  63 */           pauseMs += changePauseTime;
/*  64 */           if (pauseMs < 0L) {
/*  65 */             pauseMs = 0L;
/*     */           }
/*  67 */           log.debug("adjust-pause-time: reduced changePauseTime={} pauseMs={}", Long.valueOf(changePauseTime), Long.valueOf(pauseMs));
/*     */           
/*     */           continue;
/*     */         } 
/*  71 */         log.debug("waiting for next time to run job, timeToSleep={}", Long.valueOf(timeToSleep));
/*  72 */         Thread.sleep(timeToSleep);
/*  73 */         if (pauseMs == 0L) {
/*  74 */           pauseMs = timeToSleep / this.ratePerTimePeriod;
/*  75 */           pauseMs -= 10L;
/*  76 */           log.debug("adjust-pause-time: set pause time pauseMs=[{}]", Long.valueOf(pauseMs)); continue;
/*     */         } 
/*  78 */         if (timeToSleep > 50L) {
/*  79 */           long extraPauseTime = timeToSleep / this.ratePerTimePeriod;
/*  80 */           pauseMs += extraPauseTime;
/*  81 */           log.debug("adjust-pause-time: increased  pauseMs=[{}], added extraPauseTime={} ", Long.valueOf(pauseMs), Long.valueOf(extraPauseTime));
/*     */         }
/*     */       
/*     */       }
/*  85 */       catch (Exception e) {
/*  86 */         log.error("exception from sleep, exit loop");
/*     */       } 
/*     */     } 
/*     */   }
/*     */   
/*     */   public static void main(String[] args) {
/*  92 */     log.info("### TEST STARTING {}", Long.valueOf(System.currentTimeMillis()));
/*  93 */     TimerJob timerJob = counter -> testTimerJob(counter);
/*  94 */     TimerJobRunner timerJobRunner = new TimerJobRunner();
/*  95 */     timerJobRunner.start(10, 1000, timerJob);
/*     */   }
/*     */   
/*     */   static boolean testTimerJob(long counter) {
/*  99 */     Date date = new Date();
/* 100 */     log.info("Running Job, counter={} date=[{}]", Long.valueOf(counter), date);
/* 101 */     if (counter > 1000L) {
/* 102 */       return false;
/*     */     }
/* 104 */     long n = (long)(Math.random() * 100.0D);
/*     */     try {
/* 106 */       log.info("...job variability - sleep {}", Long.valueOf(n));
/* 107 */       Thread.sleep(n);
/*     */     }
/* 109 */     catch (Exception e) {
/* 110 */       log.error("exception in JOB - sleep", e);
/* 111 */       return false;
/*     */     } 
/* 113 */     return true;
/*     */   }
/*     */ }

