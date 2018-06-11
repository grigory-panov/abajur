package online.abajur.config;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableCaching
public class CacheConfig extends CachingConfigurerSupport {

    @Bean
    @Override
    public CacheManager cacheManager() {
        SimpleCacheManager simpleCacheManager = new SimpleCacheManager();
        List<Cache> caches = new ArrayList<>();
        caches.add(new ConcurrentMapCache("landing"));
        caches.add(new ConcurrentMapCache("statistic"));
        caches.add(new ConcurrentMapCache("games"));
        caches.add(new ConcurrentMapCache("players"));
        simpleCacheManager.setCaches(caches);
        return simpleCacheManager;
    }

    @Bean
    @Override
    public KeyGenerator keyGenerator() {
        return (target, method, params) -> {
            StringBuilder sb = new StringBuilder();
            sb.append(method.getName());
            for (Object obj : params) {
                sb.append('#').append(String.valueOf(obj));
            }
            return sb.toString();
        };
    }
}
