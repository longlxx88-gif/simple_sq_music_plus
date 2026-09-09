package com.sqmusicplus.v3.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 总线程池配置与下载线程池 - JDK 21 虚拟线程版本
 *
 * @author SQ
 **/
@Configuration
public class ThreadPoolConfig
{
    @Bean(name = "threadPoolTaskExecutor")
    public ExecutorService threadPoolTaskExecutor()
    {
        // 使用虚拟线程执行器
        return Executors.newVirtualThreadPerTaskExecutor();
    }
}
