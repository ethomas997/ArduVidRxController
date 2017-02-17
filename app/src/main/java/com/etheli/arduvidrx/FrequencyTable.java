//FrequencyTable.java:  Video-frequencies table and utilities.
//
//  1/24/2017 -- [ET]
//

package com.etheli.arduvidrx;

/**
 * Class FrequencyTable defines the video-frequencies table and utilities.
 */
public class FrequencyTable
{
  private static final FrequencyBand[] frequencyTableBandsArray = new FrequencyBand[] {
          new FrequencyBand("F", new short[] {5740,5760,5780,5800,5820,5840,5860,5880}),
          new FrequencyBand("E", new short[] {5705,5685,5665,5645,5885,5905,5925,5945}),
          new FrequencyBand("A", new short[] {5865,5845,5825,5805,5785,5765,5745,5725}),
          new FrequencyBand("B", new short[] {5733,5752,5711,5790,5809,5828,5847,5866}),
          new FrequencyBand("R", new short[] {5658,5695,5732,5769,5806,5843,5880,5917}),
          new FrequencyBand("L", new short[] {5362,5399,5436,5473,5510,5547,5584,5621})
  };

  private final FreqChannelItem[] freqChannelItemsArray;

  /**
   * Creates the frequency-table object (and loads the frequency-channel-items array).
   */
  public FrequencyTable()
  {
    int numItems = 0;        //calculate total number of frequencies
    for(int bandNum = 0; bandNum< frequencyTableBandsArray.length; ++bandNum)
      numItems += frequencyTableBandsArray[bandNum].frequenciesArr.length;
    freqChannelItemsArray = new FreqChannelItem[numItems];
    int iArrPos = 0;         //load array:
    for(int bandNum = 0; bandNum< frequencyTableBandsArray.length; ++bandNum)
    {  //for each frequency band
      final String fBandPreStr = frequencyTableBandsArray[bandNum].bandPrefixStr;
      final short[] fBandValsArr = frequencyTableBandsArray[bandNum].frequenciesArr;
      for(int p=0; p<fBandValsArr.length; ++p)
      {  //for each frequency value in band
        freqChannelItemsArray[iArrPos] =
                 new FreqChannelItem((fBandPreStr+(p+1)), fBandValsArr[p], iArrPos);
        ++iArrPos;
      }
    }
  }

  /**
   * Returns the frequency-channel-items array containing items for all
   * frequencies in the table.
   * @return An array of FreqChannelItem objects.
   */
  public FreqChannelItem[] getFreqChannelItemsArray()
  {
    return freqChannelItemsArray;
  }

  /**
   * Returns the frequency-channel-item object for the given channel code
   * and frequency value.
   * @param channelCodeStr code value for channel (i.e., "F4").
   * @param frequencyVal frequency value for channel.
   * @return The matching frequency-channel-item object, or null if no match.
   */
  public FreqChannelItem getFreqChannelItemObj(String channelCodeStr, short frequencyVal)
  {
    for(int i=0; i<freqChannelItemsArray.length; ++i)
    {  //for each item in array; check for match
      if(freqChannelItemsArray[i].equals(channelCodeStr,frequencyVal))
        return freqChannelItemsArray[i];
    }
    return null;
  }

  /**
   * Returns the frequency-channel-item object for the given array index.
   * @param idx channel-array-index value.
   * @return The matching frequency-channel-item object, or null if no match.
   */
  public FreqChannelItem getFreqChanItemForIdx(int idx)
  {
    return (idx >= 0 && idx < freqChannelItemsArray.length) ? freqChannelItemsArray[idx] : null;
  }


  /**
   * Class FrequencyBand defines a band of frequencies values (channels).
   */
  public static class FrequencyBand
  {
    public final String bandPrefixStr;
    public final short[] frequenciesArr;

    /**
     * Creates a frequency-band object.
     * @param bandPrefixStr prefix string that identifies the band.
     * @param frequenciesArr array of frequency values for the band.
     */
    public FrequencyBand(String bandPrefixStr, short[] frequenciesArr)
    {
      this.bandPrefixStr = bandPrefixStr;
      this.frequenciesArr = frequenciesArr;
    }
  }


  /**
   * Class FreqChannelItem defines a frequency (channel) value.  Any array of
   * FreqChannelItem objects may be handled like an array of String objects.
   */
  public static class FreqChannelItem implements CharSequence
  {
    public final String channelCodeStr;
    public final short frequencyVal;
    public final int itemArrayIndex;
    public final String displayString;

    /**
     * Creates a frequency (channel) object.
     * @param channelCodeStr code value for channel (i.e., "F4").
     * @param frequencyVal frequency value for channel.
     * @param itemArrayIndex array-index value for item.
     */
    public FreqChannelItem(String channelCodeStr, short frequencyVal, int itemArrayIndex)
    {
      this.channelCodeStr = channelCodeStr;
      this.frequencyVal = frequencyVal;
      this.itemArrayIndex = itemArrayIndex;
      displayString = channelCodeStr + "    " + Short.toString(frequencyVal);
    }

    /**
     * Returns length of display string for item.
     * @return Length of display string for item.
     */
    @Override
    public int length()
    {
      return displayString.length();
    }

    /**
     * Returns character at position in display string for item.
     * @param i position.
     * @return Character at position.
     */
    @Override
    public char charAt(int i)
    {
      return displayString.charAt(i);
    }

    /**
     * Returns substring of display string for item.
     * @param start start position.
     * @param end end position.
     * @return New substring.
     */
    @Override
    public CharSequence subSequence(int start, int end)
    {
      return displayString.subSequence(start,end);
    }

    /**
     * Returns display string for item.
     * @return Display string for item.
     */
    @Override
    public String toString()
    {
      return displayString;
    }

    /**
     * Determines if equal to other object.
     * @return true if equal; false if not.
     */
    @Override
    public boolean equals(Object obj)
    {
      return displayString.equals(obj);
    }

    /**
     * Determines if the given channel code and frequency value are the same
     * as those held by this object.
     * @param channelCodeStr code value for channel (i.e., "F4").
     * @param frequencyVal frequency value for channel.
     * @return true if equal; false if not.
     */
    public boolean equals(String channelCodeStr, short frequencyVal)
    {
      if(channelCodeStr != null)
      {  //channel code given; check if equal
        if(!channelCodeStr.equals(this.channelCodeStr))
          return false;
      }
      else if(this.channelCodeStr != null)  //if no channel code given but this object
        return false;                       // has a channel code then not equal
      return (frequencyVal == this.frequencyVal);     //check freq value
    }
  }
}
