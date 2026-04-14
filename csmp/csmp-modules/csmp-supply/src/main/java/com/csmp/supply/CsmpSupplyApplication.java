package com.csmp.supply;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.metrics.buffering.BufferingApplicationStartup;

/**
 * 供应链模块
 *
 * @author csmp
 */
@EnableDubbo
@MapperScan("com.csmp.supply.mapper")
@SpringBootApplication
public class CsmpSupplyApplication {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(CsmpSupplyApplication.class);
        application.setApplicationStartup(new BufferingApplicationStartup(2048));
        application.run(args);
        System.out.println("(♥◠‿◠)ﾉﾞ  供应链模块启动成功   ლ(´ڡ`ლ)ﾞ  ");
    }
}
