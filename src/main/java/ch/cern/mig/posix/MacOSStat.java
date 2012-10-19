package ch.cern.mig.posix;

public class MacOSStat extends FileStat {
	public int st_dev;
	public int st_ino;
	public short st_mode;
	public short st_nlink;
	public int st_uid;
	public int st_gid;
	public int st_rdev;
	public long st_atime;
	public long st_atimensec;
	public long st_mtime;
	public long st_mtimensec;
	public long st_ctime;
	public long st_ctimensec;
	public long st_size;
	public long st_blocks;
	public int st_blksize;
	public int st_flags;
	public int st_gen;
	public int st_lspare;
	public long st_qspare0;
	public long st_qspare1;

	@Override
	public long atime() {
		return st_atime;
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
		return st_mode & 0xffff;
	}

	@Override
	public long mtime() {
		return st_mtime;
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
