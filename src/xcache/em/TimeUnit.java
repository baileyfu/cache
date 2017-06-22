package xcache.em;

public enum TimeUnit {
	SECOND, MINUTE, HOUR, DAY;
	public int toSeconds(long value) {
		if (this == SECOND) {
			return Long.valueOf(value).intValue();
		} else if (this == MINUTE) {
			return Long.valueOf(value * 60).intValue();
		} else if (this == HOUR) {
			return Long.valueOf(value * 60 * 60).intValue();
		} else if (this == DAY) {
			return Long.valueOf(value * 24 * 60 * 60).intValue();
		}
		return 0;
	}

	public long toMilliseconds(long value) {
		if (this == SECOND) {
			return value * 1000;
		} else if (this == MINUTE) {
			return value * 60 * 1000;
		} else if (this == HOUR) {
			return value * 60 * 60 * 1000;
		} else if (this == DAY) {
			return value * 24 * 60 * 60 * 1000;
		}
		return 0;
	}
}
