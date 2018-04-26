package xcache.bean;

import java.util.concurrent.TimeUnit;

import commons.variable.ActionTimer;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import xcache.LocalCache;
import xcache.XcacheLoggerHolder;

/**
 * 可自动清理过期对象的本地缓存
 * 
 * @author bailey.fu
 * @date Dec 14, 2010
 * @update 2017-06-20 15:01
 * @version 2.1
 * @description 自定义缓存,由ActionTimer实现计时
 */
public abstract class AutoCleanAbleCache<K,V> implements XcacheLoggerHolder,LocalCache<K,V> {
	private static final int DEFAULT_CLEAR_INTERVAL = 1;

	/** 清理间隔(单位：分钟；最小为1分钟,默认1分钟) */
	private int clearInterval;
	private ActionTimer actionTimer;
	// 检查间隔1分钟
	private int checkInterval = 1;
	private boolean emittingHasStoped = false;
	private Observable<Long> obva;
	private Disposable emitterDis;

	public AutoCleanAbleCache(){
		this.clearInterval=DEFAULT_CLEAR_INTERVAL;
		startEmit();
	}
	
	public AutoCleanAbleCache(int clearInterval) {
		this.clearInterval = clearInterval < 1 ? 1 : clearInterval;
		startEmit();
	}

	private void init() {
		this.actionTimer = new ActionTimer(true);
		this.obva = Observable.create((ObservableOnSubscribe<Long>) e -> {
			Disposable d = Observable.interval(checkInterval, TimeUnit.MINUTES).subscribe((t) -> {
				emit(e, t);
			});
			setEmitterDis(d);
		});
		this.obva.subscribeOn(Schedulers.newThread()).observeOn(Schedulers.io()).subscribe(this::clear, (e) -> {
			LOGGER.error("AutoCleanAbleCache emit error", e);
			stopEmit();
		}, () -> {
			stopEmit();
		});
	}

	private void setEmitterDis(Disposable dis) {
		emitterDis = dis;
	}

	private void startEmit() {
		emittingHasStoped = false;
		init();
	}

	private void stopEmit() {
		emittingHasStoped = true;
		actionTimer = null;
		obva = null;
		emitterDis.dispose();
	}

	private void clear(long t) {
		if (actionTimer.onTime(clearInterval * 60 * 1000)) {
			clearExpiring();
		}
	}

	private void emit(ObservableEmitter<Long> emitter, long t) {
		if (emittingHasStoped)
			emitter.onComplete();
		emitter.onNext(t);
	}

	abstract protected void clearExpiring();

	class Entity {
		V element;
		long expiring;
		long createTime;

		public Entity(V element) {
			this.element = element;
			expiring=0l;
			this.createTime = System.currentTimeMillis();
		}

		public Entity(V element, long expiring) {
			this.element = element;
			this.expiring = expiring;
			this.createTime = System.currentTimeMillis();
		}

		public boolean unAble() {
			return expiring > 0 && (createTime + expiring ) <= System.currentTimeMillis();
		}

		public V getElement() {
			if (unAble()) {
				return null;
			}
			return element;
		}
	}
}
