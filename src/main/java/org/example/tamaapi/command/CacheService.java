package org.example.tamaapi.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.tamaapi.common.cache.MyCacheType;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CacheService {

    private final CacheManager cacheManager;

    public void save(MyCacheType cacheName, String key, Object value) {
        Cache cache = getCache(cacheName);
        //덮어 씌움
        if (cache != null) cache.put(key, value);
    }

    public Object get(MyCacheType cacheName, String key) {
        Cache cache = getCache(cacheName);
        if (cache != null) return cache.get(key, Object.class);
        return null;
    }

    public void evict(MyCacheType cacheName, String key) {
        Cache cache = getCache(cacheName);
        if (cache != null) cache.evict(key);
    }

    private Cache getCache(MyCacheType cacheName){
        return cacheManager.getCache(cacheName.name());
    }

    //테스트 용도
    public Map<String, Map<Object, Object>> getAllCachesWithContents() {
        Map<String, Map<Object, Object>> allCaches = new HashMap<>();

        cacheManager.getCacheNames().forEach(cacheName -> {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache instanceof CaffeineCache caffeineCache) {
                Map<Object, Object> cacheContent = caffeineCache.getNativeCache().asMap();
                allCaches.put(cacheName, cacheContent);
            }
        });

        return allCaches;
    }

}
