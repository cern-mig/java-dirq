package ch.cern.mig.posix;

public class Linux64Stat extends FileStat {
	public long st_dev;
	public long st_ino;
	public long st_nlink;
	public int st_mode;
	public int st_uid;
	public int st_gid;
	public long st_rdev;
	public long st_size;
	public long st_blksize;
	public long st_blocks;
	public long st_atime;
	public long st_atimensec;
	public long st_mtime;
	public long st_mtimensec;
	public long st_ctime;
	public long st_ctimensec;
	public long __unused4;
	public long __unused5;
	public long __unused6;

	@Override
	public long atime() {
		return st_atime;
	}

	@Override
	public long blockSize() {
		return st_blksize;
	}

	@Override
	public long blocks() {
		return st_blocks;
	}

	@Override
	public long ctime() {
		return st_ctime;
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
		return st_mode;
	}

	@Override
	public long mtime() {
		return st_mtime;
	}

	@Override
	public int nlink() {
		return (int) st_nlink;
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
