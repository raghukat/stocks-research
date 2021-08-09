//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.stocks.research.mkvapi_util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

public class TimerJobRunner {
    private static final Logger log = LoggerFactory.getLogger(TimerJobRunner.class);
    int timePreiodMs;
    int ratePerTimePeriod;
    TimerJob timerJob;

    public TimerJobRunner() {
    }

    public void start(int ratePerTimePeriod, int timePreiodMs, TimerJob timerListener) {
        this.timePreiodMs = timePreiodMs;
        this.ratePerTimePeriod = ratePerTimePeriod;
        this.timerJob = timerListener;
        this.runJob();
    }

    private void runJob() {
        long counter = 0L;
        long pauseMs = 0L;
        log.info("running job: timePreiodMs={} ratePerTimePeriod={}", this.timePreiodMs, this.ratePerTimePeriod);

        while(true) {
            long startTime = System.currentTimeMillis();

            for(int i = 0; i < this.ratePerTimePeriod; ++i) {
                try {
                    if (!this.timerJob.onNextTime(counter++)) {
                        log.info("timer job returned false, will stop, counter={}", counter);
                        return;
                    }

                    if (pauseMs > 0L) {
                        Thread.sleep(pauseMs);
                    }
                } catch (Exception var15) {
                    log.error("exception from job runner, will stop", var15);
                    return;
                }
            }

            long endTime = System.currentTimeMillis();
            long timeToSleep = (long)this.timePreiodMs - (endTime - startTime);

            try {
                long changePauseTime;
                if (timeToSleep < 0L) {
                    if (pauseMs == 0L) {
                        log.warn("adjust-pause-time: can't adjust, Job takes too long to run, can't run at required rate!");
                    } else {
                        changePauseTime = timeToSleep / (long)this.ratePerTimePeriod;
                        pauseMs += changePauseTime;
                        if (pauseMs < 0L) {
                            pauseMs = 0L;
                        }

                        log.debug("adjust-pause-time: reduced changePauseTime={} pauseMs={}", changePauseTime, pauseMs);
                    }
                } else {
                    log.debug("waiting for next time to run job, timeToSleep={}", timeToSleep);
                    Thread.sleep(timeToSleep);
                    if (pauseMs == 0L) {
                        pauseMs = timeToSleep / (long)this.ratePerTimePeriod;
                        pauseMs -= 10L;
                        log.debug("adjust-pause-time: set pause time pauseMs=[{}]", pauseMs);
                    } else if (timeToSleep > 50L) {
                        changePauseTime = timeToSleep / (long)this.ratePerTimePeriod;
                        pauseMs += changePauseTime;
                        log.debug("adjust-pause-time: increased  pauseMs=[{}], added extraPauseTime={} ", pauseMs, changePauseTime);
                    }
                }
            } catch (Exception var14) {
                log.error("exception from sleep, exit loop");
            }
        }
    }

    public static void main(String[] args) {
        log.info("### TEST STARTING {}", System.currentTimeMillis());
        TimerJob timerJob = (counter) -> {
            return testTimerJob(counter);
        };
        TimerJobRunner timerJobRunner = new TimerJobRunner();
        timerJobRunner.start(10, 1000, timerJob);
    }

    static boolean testTimerJob(long counter) {
        Date date = new Date();
        log.info("Running Job, counter={} date=[{}]", counter, date);
        if (counter > 1000L) {
            return false;
        } else {
            long n = (long)(Math.random() * 100.0D);

            try {
                log.info("...job variability - sleep {}", n);
                Thread.sleep(n);
                return true;
            } catch (Exception var6) {
                log.error("exception in JOB - sleep", var6);
                return false;
            }
        }
    }
}
