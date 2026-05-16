package com.zong.agent.runtime.task;

@FunctionalInterface
public interface TaskRunner {
    String run(String prompt) throws Exception;
}
