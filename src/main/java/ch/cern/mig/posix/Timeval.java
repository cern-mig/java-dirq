package ch.cern.mig.posix;

import java.util.Arrays;
import java.util.List;

import com.sun.jna.Structure;

public final class Timeval extends Structure {
	public long tv_sec;
	public long tv_usec;

	public Timeval() {
	}

	public List<String> getFieldOrder() {
		return Arrays.asList("tv_sec", "tv_usec");
	}

	public void setTime(long[] timeval) {
		assert timeval.length == 2;
		tv_sec = timeval[0];
		tv_usec = timeval[1];
	}
}