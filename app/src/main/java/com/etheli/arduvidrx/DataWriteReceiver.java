//DataWriteReceiver.java:  Receives data via 'write()' method.
//
// 10/22/2016 -- [ET]
//

package com.etheli.arduvidrx;

/**
 * Receives data via 'write()' method.
 */
public interface DataWriteReceiver
{

  /**
   * Receives data bytes.
   * @param buffer data bytes.
   * @param length number of bytes.
   */
  public void write(byte[] buffer, int length);
}
