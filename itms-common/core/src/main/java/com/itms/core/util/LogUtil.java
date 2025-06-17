package com.itms.core.util;

public class LogUtil {

	public static String errorLog(Throwable e) {
		StackTraceElement error = e.getStackTrace()[0];
		return System.getProperty("line.separator") + e + " occured, Please check line number " + error.getLineNumber()
				+ " in file " + error.getClassName() + " inside " + error.getMethodName() + " method";
	}

	public static String startLog(String className, String transactionId) {
		final int CURRENT_METHOD = 2;
		return "Entering Class " + className + " into method "
				+ Thread.currentThread().getStackTrace()[CURRENT_METHOD].getMethodName() + " with transactionId :"
				+ transactionId;
	}

	public static String exitLog(String className, String transactionId) {
		final int CURRENT_METHOD = 2;
		return "Exiting Class " + className + " from method  "
				+ Thread.currentThread().getStackTrace()[CURRENT_METHOD].getMethodName() + " with transactionId :"
				+ transactionId;
	}

	public static String presentationLogger(String transactionId, String hanlderName) {
		return "Reached  url : " + hanlderName + " with transactionId :" + transactionId;
	}

	public static String startLog(String className) {
		final int CURRENT_METHOD = 2;
		return "Entering Class " + className + " into method "
				+ Thread.currentThread().getStackTrace()[CURRENT_METHOD].getMethodName();
	}

	public static String exitLog(String className) {
		final int CURRENT_METHOD = 2;
		return "Exiting Class " + className + " from method  "
				+ Thread.currentThread().getStackTrace()[CURRENT_METHOD].getMethodName();
	}

	public static String presentationLogger(String hanlderName) {
		return "Reached  url : " + hanlderName;
	}
}
