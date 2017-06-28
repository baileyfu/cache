# 缓存框架
对缓冲层的抽象，可接入本地缓存和第三方缓存<br/>
未指定localCache和remoteCache或者需缓存返回对象的方法的参数为空，则不做任何操作<br/>
CacheManager为主要操作类，单例实现，可单独使用，如：<br/>
CacheManager cm=CacheManager.getInstance();<br/>
cm.putToRemote(key,value);<br/>
...
### e.g
Spring配置增加：

	<bean class="xcache.spring.CacheBeanPostProcessor"/>
	<bean class="xcache.spring.CacheConfiguration">
			<constructor-arg index="0">
				<!-- LocalCache;可为Null -->
				<bean class="xcache.bean.SingleMapCache"/>
			</constructor-arg>
			<constructor-arg index="1">
				<!-- RemoteCache;可为Null -->
				<bean id="remoteCache" class="xcache.redis.RedisCache">
						<constructor-arg>
							<!-- 需配置org.springframework.data.redis.core.RedisTemplate -->
							<ref bean="redisTemplate"/>
						</constructor-arg>
				</bean>
			</constructor-arg>
	</bean>
	
使用：
	
	@RCache
	public String getBankCard(String userId){
	...
	}
	@RCache(key="#user.id")
	public String getBankCard(User user){
	...
	}
	@RCache(key="#userInfo[0]")
	public String getBankCard(String[] userInfo){
	...
	}
	@RCache(key="#userInfo['id'] + '-' + #userInfo['name']")
	public String getBankCard(Map userInfo){
	...
	}
	@LCache(key="myConfig",remove={"updateConfig","deleteConfig"},throwable=true,expiring=10,timeUnit=TimeUnit.MINUTE,prefix="PREFIX",suffix="SUFFIX")
	public Map MyConfig(){
	...
	}

### 已接入的本地缓存：
<table>
	<tr align="left">
		<td>SingleMapCache</td>
		<td>单Map实现，不自动清理，线程安全</td>
	</tr>
	<tr align="left">
		<td>SingleMapAutoCleanCache</td>
		<td>单Map实现，可自动清理过期内容，线程安全</td>
	</tr>
	<tr align="left">
		<td>MultiMapCache</td>
		<td>双Map实现，不自动清理，线程安全</td>
	</tr>
	<tr align="left">
		<td>MultiMapAutoCleanCache</td>
		<td>双Map实现，可自动清理过期内容，线程安全</td>
	</tr>
	<tr align="left">
		<td>LRUCache</td>
		<td>单LURMap实现，不自动清理，线程安全</td>
	</tr>
	<tr align="left">
		<td>MultiLRUCache</td>
		<td>双LURMap实现，不自动清理，线程安全</td>
	</tr>
	<tr align="left">
		<td>EHCache</td>
		<td>基于Ehcache实现的Cache</td>
	</tr>
</table>


### 已接入的第三方缓存
<table>
	<tr>
		<td>RedisCache</td>
		<td>基于Spring的RedisTemplate实现</td>
	</tr>
	<tr>
		<td>RedisClusterCache(待实现)</td>
		<td>基于Redis集群实现</td>
	</tr>
</table>

### 配置详解
@RCache和@LCache参数相同；所有的参数都不是必填项<br/>
<table>
	<tr>
		<td>参数名</td>
		<td>类型</td>
		<td>默认值</td>
		<td>描述</td>
	</tr>
	<tr>
		<td>key</td>
		<td>String</td>
		<td>空</td>
		<td>
			缓存主键生成方式，三种方式：<br/>
			1.不指定；以方法的第一个参数dump成字符串作为key<br/>
			2.SpEL；表达式引用参数名称正确则解析，否则将表达式以字符串形式作为key；若多个参数名称相同，则取第一个参数参与表达式运算<br/>
			3.字符串形式，直接作为key<br/>
		</td>
	</tr>
	<tr>
		<td>remove</td>
		<td>String数组</td>
		<td>空</td>
		<td>触发缓存清理的方法名；匹配所有重载的方法</td>
	</tr>
	<tr>
		<td>throwable</td>
		<td>Boolean</td>
		<td>false</td>
		<td>是否抛出缓存操作时的异常</td>
	</tr>
	<tr>
		<td>expiring</td>
		<td>Integer</td>
		<td>0</td>
		<td>有效期</td>
	</tr>
	<tr>
		<td>timeUnit</td>
		<td>xcache.em.TimeUnit</td>
		<td>MINUTE</td>
		<td>有效期单位</td>
	</tr>
	<tr>
		<td>prefix</td>
		<td>String</td>
		<td>空</td>
		<td>key前缀</td>
	</tr>
	<tr>
		<td>suffix</td>
		<td>String</td>
		<td>空</td>
		<td>key后缀</td>
	</tr>
</table>
