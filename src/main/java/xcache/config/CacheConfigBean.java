package com.lz.components.cache.config;

/**
 * 缓存配置定义
 * 
 * @author fuli
 * @date 2018年9月10日
 * @version 1.0.0
 */
public class CacheConfigBean {
	private String name;
	private boolean enable;
	private ConfigDetail local;
	private ConfigDetail remote;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public boolean isEnable() {
		return enable;
	}
	public void setEnable(boolean enable) {
		this.enable = enable;
	}
	public ConfigDetail getLocal() {
		return local;
	}
	public void setLocal(ConfigDetail local) {
		this.local = local;
	}
	public ConfigDetail getRemote() {
		return remote;
	}
	public void setRemote(ConfigDetail remote) {
		this.remote = remote;
	}
}
