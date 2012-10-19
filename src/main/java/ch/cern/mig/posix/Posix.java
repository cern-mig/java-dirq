package ch.cern.mig.posix;

import com.sun.jna.Native;
import com.sun.jna.Platform;

public class Posix {
	public static final String LIBC = Platform.isLinux() ? "libc.so.6" : "c";
	public static final LibC libc = (LibC) Native.loadLibrary(LIBC,
			Platform.isLinux() ? LinuxLibC.class : LibC.class);
	public static final BasePosix posix = Platform.isLinux() ? new LinuxPosix(
			libc) : new BasePosix(libc);
}
