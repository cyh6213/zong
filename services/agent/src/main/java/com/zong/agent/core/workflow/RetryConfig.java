package com.zong.agent.core.workflow;

/**
 * 重试配置
 */
public class RetryConfig {

    private int maxAttempts = 3;
    private long delayMillis = 1000;
    private double multiplier = 2.0;
    private String[] retryableExceptions = {"java.lang.Exception"};

    public RetryConfig() {
    }

    public RetryConfig(int maxAttempts, long delayMillis) {
        this.maxAttempts = maxAttempts;
        this.delayMillis = delayMillis;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public void setMaxAttempts(int maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    public long getDelayMillis() {
        return delayMillis;
    }

    public void setDelayMillis(long delayMillis) {
        this.delayMillis = delayMillis;
    }

    public double getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(double multiplier) {
        this.multiplier = multiplier;
    }

    public String[] getRetryableExceptions() {
        return retryableExceptions;
    }

    public void setRetryableExceptions(String[] retryableExceptions) {
        this.retryableExceptions = retryableExceptions;
    }

    /**
     * 计算下一次重试的延迟时间
     */
    public long getNextDelay(int attempt) {
        return (long) (delayMillis * Math.pow(multiplier, attempt - 1));
    }

    public static RetryConfig defaultConfig() {
        return new RetryConfig(3, 1000);
    }

    public static RetryConfig noRetry() {
        return new RetryConfig(1, 0);
    }
}
