package ch.cern.mig.posix;

import com.sun.jna.LastErrorException;

public interface LinuxLibC extends LibC {
    public int __fxstat(int version, int fd, FileStat stat)
            throws LastErrorException;

    public int __lxstat(int version, String path, FileStat stat)
            throws LastErrorException;

    public int __xstat(int version, String path, FileStat stat)
            throws LastErrorException;

    public int __fxstat64(int version, int fd, FileStat stat)
            throws LastErrorException;

    public int __lxstat64(int version, String path, FileStat stat)
            throws LastErrorException;

    public int __xstat64(int version, String path, FileStat stat)
            throws LastErrorException;
}
