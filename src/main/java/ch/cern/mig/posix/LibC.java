package ch.cern.mig.posix;

import com.sun.jna.LastErrorException;
import com.sun.jna.Library;

public interface LibC extends Library {
    int getpid() throws LastErrorException;

    int link(String fromFile, String toFile) throws LastErrorException;

    int unlink(String path) throws LastErrorException;

    int lstat(String path, FileStat stat) throws LastErrorException;

    int mkdir(String name, int mode) throws LastErrorException;

    int rename(String from, String to) throws LastErrorException;

    int rmdir(String name) throws LastErrorException;

    int stat(String path, FileStat stat) throws LastErrorException;

    int umask() throws LastErrorException;

    int umask(int val) throws LastErrorException;

    int utimes(String path, Timeval[] timeval) throws LastErrorException;
}