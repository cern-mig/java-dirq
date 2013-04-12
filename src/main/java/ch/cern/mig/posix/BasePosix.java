package ch.cern.mig.posix;

import java.io.File;
import java.io.IOException;

import com.sun.jna.LastErrorException;
import com.sun.jna.Platform;

public class BasePosix {
    public static final int EPERM   =  1;
    public static final int ENOENT  =  2;
    public static final int ESRCH   =  3;
    public static final int EINTR   =  4;
    public static final int EIO     =  5;
    public static final int ENXIO   =  6;
    public static final int E2BIG   =  7;
    public static final int ENOEXEC =  8;
    public static final int EBADF   =  9;
    public static final int ECHILD  = 10;
    public static final int EAGAIN  = 11;
    public static final int ENOMEM  = 12;
    public static final int EACCES  = 13;
    public static final int EFAULT  = 14;
    public static final int ENOTBLK = 15;
    public static final int EBUSY   = 16;
    public static final int EEXIST  = 17;
    public static final int EXDEV   = 18;
    public static final int ENODEV  = 19;
    public static final int ENOTDIR = 20;
    public static final int EISDIR  = 21;
    public static final int EINVAL  = 22;

    public LibC libc;

    public BasePosix(LibC libc) {
        this.libc = libc;
    }

    public int umask() {
        int val = libc.umask(022);
        libc.umask(val);
        return val;
    }

    public int umask(int val) {
        return libc.umask(val);
    }

    public int utimes(String path, Timeval[] timeval) {
        return libc.utimes(path, timeval);
    }

    public int getpid() {
        return libc.getpid();
    }

    public void mkdir(String name) throws LastErrorException {
        libc.mkdir(name, 0777 - umask());
    }

    public void mkdir(String name, int mode) throws LastErrorException {
        libc.mkdir(name, mode);
    }

    public void link(String from, String to) throws LastErrorException {
        libc.link(from, to);
    }

    public File opendir(String path) throws LastErrorException {
        File dir = new File(path);
        if (!dir.exists())
            throw new LastErrorException(ENOENT);
        return dir;
    }

    public File open(String path) throws LastErrorException {
        File file = new File(path);
        boolean result = false;
        try {
            result = file.createNewFile();
        } catch (IOException e) {
            if (e.getMessage().equals("No such file or directory"))
                throw new LastErrorException(ENOENT);
            throw new LastErrorException(e.getMessage());
        }
        if (result)
            if (file.canWrite())
                return file;
            else
                throw new LastErrorException(EACCES);
        else
            throw new LastErrorException(EEXIST);
    }

    public void rename(String from, String to) throws LastErrorException {
        libc.rename(from, to);
    }

    public void rmdir(String path) throws LastErrorException {
        libc.rmdir(path);
    }

    public void unlink(String path) throws LastErrorException {
        libc.unlink(path);
    }

    public FileStat newFileStat() {
        if (Platform.isMac())
            return new MacOSStat();
        else if (Platform.isLinux())
            if (Platform.is64Bit())
                return new Linux64Stat();
            else
                return new LinuxStat();
        else
            throw new RuntimeException("Platform not supported");
    }

    public FileStat stat(String path) throws LastErrorException {
        FileStat stat = newFileStat();
        libc.stat(path, stat);
        return stat;
    }

    public FileStat lstat(String path) throws LastErrorException {
        FileStat stat = newFileStat();
        libc.lstat(path, stat);
        return stat;
    }

}
