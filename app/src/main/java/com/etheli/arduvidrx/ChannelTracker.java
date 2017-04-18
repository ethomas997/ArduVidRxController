//ChannelTracker.java:  Tracks the currently-tuned video-receiver channel.
//
//  3/31/2017 -- [ET]
//

package com.etheli.arduvidrx;

/**
 * Class ChannelTracker tracks the currently-tuned video-receiver channel.
 */
public class ChannelTracker
{
  private final FrequencyTable frequencyTableObj;
  private FrequencyTable.FreqChannelItem curFreqChannelItemObj = null;
  private short curFrequencyInMHzVal = 0;

  /**
   * Creates a channel-tracker object.
   * @param frequencyTableObj frequency-table util object.
   */
  public ChannelTracker(FrequencyTable frequencyTableObj)
  {
    this.frequencyTableObj = frequencyTableObj;
  }

  /**
   * Sets the current channel.
   * @param chanCodeStr code value for channel (i.e., "F4"), or null for none.
   * @param freqVal frequency value for channel.
   * @return The matching frequency-channel-item object, or null if no match.
   */
  public synchronized FrequencyTable.FreqChannelItem setFreqChannel(
                                                               String chanCodeStr, short freqVal)
  {
    curFreqChannelItemObj = frequencyTableObj.getFreqChannelItemObj(chanCodeStr,freqVal);
              //if match then use freq value from item; else use given freq value:
    curFrequencyInMHzVal = (curFreqChannelItemObj != null) ?
                                                    curFreqChannelItemObj.frequencyVal : freqVal;
    return curFreqChannelItemObj;
  }

  /**
   * Returns the frequency-channel-item object for current channel.
   * @return The frequency-channel-item object for current channel, or null
   * if no item matches the current frequency.
   */
  public synchronized FrequencyTable.FreqChannelItem getCurFreqChannelItemObj()
  {
    return curFreqChannelItemObj;
  }

  /**
   * Returns array index for frequency-channel-item object for current channel.
   * @return Array index for frequency-channel-item object for current channel,
   * or -1 if no item matches the current frequency.
   */
  public synchronized int getCurFreqChannelItemIdx()
  {
    return (curFreqChannelItemObj != null) ? curFreqChannelItemObj.itemArrayIndex : -1;
  }

  /**
   * Returns current frequency in MHz.
   * @return Current frequency in MHz.
   */
  public synchronized int getCurFrequencyInMHz()
  {
    return curFrequencyInMHzVal;
  }

  /**
   * Returns code value for current channel (i.e., "F4"), or null if none.
   * @return Code value for current channel (i.e., "F4"), or null if none.
   */
  public synchronized String getCurChannelCode()
  {
    return (curFreqChannelItemObj != null) ? curFreqChannelItemObj.channelCodeStr : null;
  }
}
