package ch.cern.mig.posix;

public class LinuxStat extends FileStat {
	public long st_dev;
	public short __pad1;
	public int st_ino;
	public int st_mode;
	public int st_nlink;
	public int st_uid;
	public int st_gid;
	public long st_rdev;
	public short __pad2;
	public long st_size;
	public int st_blksize;
	public int st_blocks;
	public int __unused4;
	public int st_atim_sec;
	public int st_atim_nsec;
	public int st_mtim_sec;
	public int st_mtim_nsec;
	public int st_ctim_sec;
	public int st_ctim_nsec;
	public long __unused5;

	@Override
	public long atime() {
		return st_atim_sec;
	}

	@Override
	public long blocks() {
		return st_blocks;
	}

	@Override
	public long blockSize() {
		return st_blksize;
	}

	@Override
	public long ctime() {
		return st_ctim_sec;
	}

	@Override
	public long dev() {
		return st_dev;
	}

	@Override
	public int gid() {
		return st_gid;
	}

	@Override
	public long ino() {
		return st_ino;
	}

	@Override
	public int mode() {
		return st_mode & 0xffff;
	}

	@Override
	public long mtime() {
		return st_mtim_sec;
	}

	@Override
	public int nlink() {
		return st_nlink;
	}

	@Override
	public long rdev() {
		return st_rdev;
	}

	@Override
	public long st_size() {
		return st_size;
	}

	@Override
	public int uid() {
		return st_uid;
	}
}
