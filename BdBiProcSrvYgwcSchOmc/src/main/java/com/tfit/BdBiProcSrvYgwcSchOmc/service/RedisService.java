package com.tfit.BdBiProcSrvYgwcSchOmc.service;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.tfit.BdBiProcSrvYgwcSchOmc.client.RedisRWClient;
import com.tfit.BdBiProcSrvYgwcSchOmc.config.AppModConfig;

//redis服务
@Service
public class RedisService {
	
    //以键值和域名获取hash表域值
    public String getHashByKeyField(int jedisIdx, int dbIndex, String key, String field) {
    	RedisRWClient jedisUtil = RedisRWClient.getInstance();
    	if(!existHashKeyFied(jedisIdx, dbIndex, key, field))
    		return null;
        return jedisUtil.getHashByKeyField(jedisIdx, dbIndex, key, field);
    }
    
    //以键值获取hash表所有映射值
    public Map<String, String> getHashByKey(int jedisIdx, int dbIndex, String key) {
    	RedisRWClient jedisUtil = RedisRWClient.getInstance();
    	if(!existHashKey(jedisIdx, dbIndex, key))
    		return AppModConfig.getHdfsHashKey(key);
        return jedisUtil.getHashByKey(jedisIdx, dbIndex, key);
    }
    
    //判断hash表键值、域是否存在
    public boolean existHashKeyFied(int jedisIdx, int dbIndex, String key, String field) {
    	RedisRWClient jedisUtil = RedisRWClient.getInstance();
    	return jedisUtil.existHashKeyFied(jedisIdx, dbIndex, key, field);
    }
    
    //判断hash表键值是否存在
    public boolean existHashKey(int jedisIdx, int dbIndex, String key) {
    	RedisRWClient jedisUtil = RedisRWClient.getInstance();
    	return jedisUtil.existHashKey(jedisIdx, dbIndex, key);
    }
    
    //删除键key
    public boolean delHaskKey(int jedisIdx, int dbIndex, String key) {
    	RedisRWClient jedisUtil = RedisRWClient.getInstance();
    	return jedisUtil.delHaskKey(jedisIdx, dbIndex, key);
    }
    
    //删除键key及field
    public boolean delHaskKey(int jedisIdx, int dbIndex, String key, String field) {
    	RedisRWClient jedisUtil = RedisRWClient.getInstance();
    	return jedisUtil.delHaskKey(jedisIdx, dbIndex, key, field);
    }
    
    //设置key及域值
    public boolean setHashKeyFieldVal(int jedisIdx, int dbIndex, String key, String field, String value) {
    	RedisRWClient jedisUtil = RedisRWClient.getInstance();
    	return jedisUtil.setHashKeyFieldVal(jedisIdx, dbIndex, key, field, value);
    }
}
