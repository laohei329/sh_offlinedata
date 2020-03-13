package com.tfit.BdBiProcSrvYgwcSchOmc;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.tfit.BdBiProcSrvYgwcSchOmc.config.SpringConfig;

@ComponentScan("com.tfit.BdBiProcSrvYgwcSchOmc")
@MapperScan("com.tfit.BdBiProcSrvYgwcSchOmc.dao")
@SpringBootApplication
@EnableScheduling
public class BdBiProcSrvYgwcSchOmcApp 
{
	public static void main( String[] args )
    {
        SpringApplication.run(BdBiProcSrvYgwcSchOmcApp.class, args);
        //运行配置
        SpringConfig.RunConfig();
    }
}
