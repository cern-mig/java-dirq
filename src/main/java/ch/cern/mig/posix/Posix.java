package ch.cern.mig.posix;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import com.sun.jna.LastErrorException;
import com.sun.jna.Native;
import com.sun.jna.Platform;

public class Posix {
	public static final String LIBC = Platform.isLinux() ? "libc.so.6" : "c";
	public static final LibC libc = (LibC) Native.loadLibrary(LIBC,
			Platform.isLinux() ? LinuxLibC.class : LibC.class);
	public static final BasePosix posix = Platform.isLinux() ? new LinuxPosix(
			libc) : new BasePosix(libc);

	public static int getErrorCode(LastErrorException error) {
		String aMethod = "getErrorCode";
		// get the method
		Object result = null;
		try {
			Method thisMethod = error.getClass().getDeclaredMethod(aMethod);
			result = thisMethod.invoke(error);
		} catch (Exception e) {
			try {
				Field field = error.getClass().getField("errorCode");
				result = field.get(error);
			} catch (Exception e1) {
				throw new IllegalStateException("Could not get the error code");
			}
		}
		return (Integer) result;
	}
}
