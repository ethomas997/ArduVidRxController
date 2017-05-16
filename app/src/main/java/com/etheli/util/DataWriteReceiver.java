//DataWriteReceiver.java:  Receives data via 'write()' method.
//
//  5/15/2017 -- [ET]
//

package com.etheli.util;

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
