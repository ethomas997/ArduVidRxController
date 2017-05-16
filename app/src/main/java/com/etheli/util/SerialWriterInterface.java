//SerialWriterInterface.java:  Interface for writing to a serial connection.
//
//  5/15/2017 -- [ET]
//

package com.etheli.util;

/**
 * Interface SerialWriterInterface defines methods for writing to a serial connection.
 */
public interface SerialWriterInterface
{
  /**
   * Writes the given data to the serial connection.
   * @param out array of bytes to be written.
   */
  public void write(byte[] out);

  /**
   * Determines if the current state is 'connected'.
   * @return true if the current state is 'connected'.
   */
  public boolean isConnected();
}
