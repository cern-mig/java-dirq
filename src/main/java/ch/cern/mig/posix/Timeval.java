package ch.cern.mig.posix;

import com.sun.jna.Structure;

public final class Timeval extends Structure {
	public long tv_sec;
	public long tv_usec;

	public Timeval() {
	}

	public void setTime(long[] timeval) {
		assert timeval.length == 2;
		tv_sec = timeval[0];
		tv_usec = timeval[1];
	}
}