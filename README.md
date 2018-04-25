# 缓存框架V1.0.3
对缓冲层的抽象，简化缓存使用；可接入本地缓存和第三方缓存<br/>
未指定localCache和remoteCache或者需缓存返回对象的方法的参数为空，则不做任何操作<br/>
CacheManager为主要操作类，单例实现，可单独使用，如：<br/>
CacheManager cm=CacheManager.getInstance();<br/>
cm.putToRemote(key,value);<br/>
cm.removeRemote(shardName,key);<br/>

###版本变更记录
<table>
	<tr align='center'>
		<th>版本</th>
		<th>日期</th>
		<th>描述</th>
	</tr>
	<tr align='center'>
		<td>V1.0.0</td>
		<td>2017-06-06</td>
		<td align="left">完成预期功能,可投入使用</td>
	</tr>
	<tr align='center'>
		<td>V1.0.1</td>
		<td>2017-08-01</td>
		<td align="left">支持分片</td>
	</tr>
	<tr align='center'>
		<td>V1.0.2</td>
		<td>2017-11-06</td>
		<td align="left">包结构重置;LOGGER分离</td>
	</tr>
	<tr align='center'>
		<td>V1.0.3</td>
		<td>2018-04-25</td>
		<td align="left">新增自定义Schema</td>
	</tr>
</table>

---

### e.g
Spring配置增加：

	...
	xmlns:xcache="http://www.xteam.org/xcache"
	xsi:schemaLocation="http://www.xteam.org/xcache http://www.xteam.org/xcache/xcache-1.0.xsd"
	...
	<bean id="localCache" class="xcache.bean.SingleMapCache"/>
	<bean id="remoteCache" class="xcache.redis.SingleRedisCache">
			<constructor-arg>
				<!-- 需配置org.springframework.data.redis.core.RedisTemplate -->
				<ref bean="redisTemplate"/>
			</constructor-arg>
	</bean>
	<xcache:config local="localCache" remote="remoteCache"/>
	<xcache:autoproxy/>
	
使用：
	
	@XCache(key="#user.id",remove="globalUpdate",expiring=5,timeUnit=TimeUnit.HOUR,prefix="CLASS_SCOPE_PREFIX",suffix="CLASS_SCOPE_SUFFIX")
	public class XService{
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
		@RCache(shardName="6",key="#userInfo['id'] + '-' + #userInfo['name']")
		public String getBankCard(Map userInfo){
		...
		}
		@LCache(key="myConfig",remove={"updateConfig","deleteConfig"},throwable=true,expiring=10,timeUnit=TimeUnit.MINUTE,prefix="PREFIX",suffix="SUFFIX")
		public Map MyConfig(){
		...
		}
	}

### 已接入的本地缓存：
<table>
	<tr align="left">
		<td>xcache.bean.SingleMapCache</td>
		<td>单Map实现，不自动清理，线程安全</td>
	</tr>
	<tr align="left">
		<td>xcache.bean.SingleMapAutoCleanCache</td>
		<td>单Map实现，可自动清理过期内容，线程安全</td>
	</tr>
	<tr align="left">
		<td>xcache.bean.MultiMapCache</td>
		<td>双Map实现，不自动清理，线程安全</td>
	</tr>
	<tr align="left">
		<td>xcache.bean.MultiMapAutoCleanCache</td>
		<td>双Map实现，可自动清理过期内容，线程安全</td>
	</tr>
	<tr align="left">
		<td>xcache.bean.LRUCache</td>
		<td>单LURMap实现，不自动清理，线程安全</td>
	</tr>
	<tr align="left">
		<td>xcache.bean.MultiLRUCache</td>
		<td>双LURMap实现，不自动清理，线程安全</td>
	</tr>
	<tr align="left">
		<td>xcache.bean.EHCache</td>
		<td>基于Ehcache实现的Cache</td>
	</tr>
</table>


### 已接入的第三方缓存
<table>
	<tr>
		<td>xcache.redis.SingleRedisCache</td>
		<td>基于Spring的RedisTemplate实现</td>
	</tr>
	<tr>
		<td>xcache.redis.SingleRedisShardCache</td>
		<td>基于Spring的RedisTemplate实现;可指定数据库ID</td>
	</tr>
	<tr>
		<td>xcache.redis.RedisClusterCache(未实现)</td>
		<td>基于Redis集群实现</td>
	</tr>
</table>

### 配置详解
@RCache:启用第三方缓存<br/>
@LCache:启用本地缓存<br/>
@XCache:配置Class全局默认参数；无throwable选项<br/>
*所有的参数都不是必填项<br/>

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
		<td>shardName</td>
		<td>String</td>
		<td>空</td>
		<td>数据库分库名;目前仅Redis使用，用于指定数据库下标</td>
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
