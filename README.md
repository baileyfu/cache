# 缓存框架
可接入本地缓存和第三方缓存；注解方式使用，切面中调用缓存实现<br/>

### e.g
@RCache(key="#user.id")
public String getBankCard(User user){
...
}

### 已接入的本地缓存：
SingleMapCache            单Map（ConcurrentHashMap）实现，不自动清理<br/>
SingleMapAutoCleanCache   单Map（ConcurrentHashMap）实现，可自动清理过期内容<br/>
MultiMapCache             双Map（ConcurrentHashMap）实现，不自动清理<br/>
MultiMapAutoCleanCache    双Map（ConcurrentHashMap）实现，可自动清理过期内容<br/>
EHCache                   基于Ehcache实现的Cache<br/>

### 已接入的第三方缓存
RedisCache                基于Spring的RedisTemplate实现<br/>
RedisClusterCache(待实现)  基于Redis集群实现<br/>
