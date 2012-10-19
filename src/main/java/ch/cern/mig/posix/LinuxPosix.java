package ch.cern.mig.posix;

import com.sun.jna.LastErrorException;
import com.sun.jna.Platform;

public class LinuxPosix extends BasePosix {

	public LinuxPosix(LibC libc) {
		super(libc);
	}

	public static int statVersion = Platform.is64Bit() ? 0 : 3;

	@Override
	public FileStat stat(String path) throws LastErrorException {
		FileStat stat = newFileStat();
		((LinuxLibC) libc).__xstat64(statVersion, path, stat);
		return stat;
	}

	@Override
	public FileStat lstat(String path) throws LastErrorException {
		FileStat stat = newFileStat();
		((LinuxLibC) libc).__lxstat64(statVersion, path, stat);
		return stat;
	}
}
