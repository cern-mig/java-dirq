/**
 * 
 */
package ch.cern.dirq;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import com.sun.jna.LastErrorException;

/**
 * @author Massimo Paladin - massimo.paladin@gmail.com
 * <br />Copyright CERN 2010-2013
 *
 */
public class LEE {
	
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
		int errorCode = (Integer) result;
		return errorCode;
	}
}
