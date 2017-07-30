//ULog.java:  Utility-logging class that dispatches to the system-specific
//            logging class.
//
//  5/16/2017 -- [ET]
//

package com.etheli.util;

import android.util.Log;

/**
 * Class ULog is a utility-logging class that dispatches to the system-specific
 * logging class.
 */
public class ULog
{

  public static int v(String tag, String msg)
  {
    return Log.v(tag,msg);
  }

  public static int v(String tag, String msg, Throwable tr)
  {
    return Log.v(tag,msg,tr);
  }

  public static int d(String tag, String msg)
  {
    return Log.d(tag,msg);
  }

  public static int d(String tag, String msg, Throwable tr)
  {
    return Log.d(tag,msg,tr);
  }

  public static int i(String tag, String msg)
  {
    return Log.i(tag,msg);
  }

  public static int i(String tag, String msg, Throwable tr)
  {
    return Log.i(tag,msg,tr);
  }

  public static int w(String tag, String msg)
  {
    return Log.w(tag,msg);
  }

  public static int w(String tag, String msg, Throwable tr)
  {
    return Log.w(tag,msg,tr);
  }

  public static int w(String tag, Throwable tr)
  {
    return Log.w(tag,tr);
  }

  public static int e(String tag, String msg)
  {
    return Log.e(tag,msg);
  }

  public static int e(String tag, String msg, Throwable tr)
  {
    return Log.e(tag,msg,tr);
  }

  public static int wtf(String tag, String msg)
  {
    return Log.wtf(tag,msg);
  }

  public static int wtf(String tag, Throwable tr)
  {
    return Log.wtf(tag,tr);
  }

  public static int wtf(String tag, String msg, Throwable tr)
  {
    return Log.wtf(tag,msg,tr);
  }

  public static String getStackTraceString(Throwable tr)
  {
    return Log.getStackTraceString(tr);
  }

  public static int println(int priority, String tag, String msg)
  {
    return Log.println(priority,tag,msg);
  }
}
