package com.defano.spring;

import javax.annotation.PostConstruct;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import redis.clients.jedis.Jedis;

@RestController
public class RedisCacheController {

	private Jedis jedis;
	
	@PostConstruct
	public void initJedis () {
		// 'redishost' is the host name of the Redis server; we will use Docker linking to define this host in /etc/hosts
		this.jedis = new Jedis("redishost");
	}
	
    @RequestMapping("/")
    public String index() {
        return "Hello world!\n";
    }
    
    @RequestMapping(value="/set", method=RequestMethod.GET)
    public String set (@RequestParam String key, @RequestParam String value) {
    	jedis.set(key, value);
    	return "Set: " + key + "=" + value + "\n";
    }
    
    @RequestMapping(value="/get", method=RequestMethod.GET)
    public String get (@RequestParam String key) {
    	String value = jedis.get(key);
    	return "Get: " + key + "=" + value + "\n";
    }
}
