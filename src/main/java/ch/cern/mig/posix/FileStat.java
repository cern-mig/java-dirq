package ch.cern.mig.posix;

import com.sun.jna.Structure;

public abstract class FileStat extends Structure {

	public abstract String systemCommand();

	public abstract String customRepr();

	public abstract long atime();

	public abstract long blockSize();

	public abstract long blocks();

	public abstract long ctime();

	public abstract long dev();

	public abstract int gid();

	public abstract long ino();

	public abstract int mode();

	public abstract long mtime();

	public abstract int nlink();

	public abstract long rdev();

	public abstract long st_size();

	public abstract int uid();
}
