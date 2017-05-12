//FrequencyTable.java:  Video-frequencies table and utilities.
//
//   5/7/2017 -- [ET]
//

package com.etheli.arduvidrx;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Class FrequencyTable defines the video-frequencies table and utilities.
 */
public class FrequencyTable
{
  private static final FrequencyBand[] frequencyTableBandsArray = new FrequencyBand[] {
          new FrequencyBand("F", new short[] {5740,5760,5780,5800,5820,5840,5860,5880}),
          new FrequencyBand("E", new short[] {5705,5685,5665,5645,5885,5905,5925,5945}),
          new FrequencyBand("A", new short[] {5865,5845,5825,5805,5785,5765,5745,5725}),
          new FrequencyBand("B", new short[] {5733,5752,5771,5790,5809,5828,5847,5866}),
          new FrequencyBand("R", new short[] {5658,5695,5732,5769,5806,5843,5880,5917}),
          new FrequencyBand("L", new short[] {5362,5399,5436,5473,5510,5547,5584,5621})
  };
  private final FreqChannelItem[] freqChannelItemsArray;
    /** Tag string for logging. */
  public static final String LOG_TAG = "FreqTable";

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
   * @param channelCodeStr code value for channel (i.e., "F4"), or null for
   * none (match any).
   * @param frequencyVal frequency value for channel.
   * @return The matching frequency-channel-item object, or null if no match.
   */
  public FreqChannelItem getFreqChannelItemObj(String channelCodeStr, short frequencyVal)
  {
    for(int i=0; i<freqChannelItemsArray.length; ++i)
    {  //for each item in array; check for match
      if(freqChannelItemsArray[i].frequencyVal == frequencyVal)
      {  //frequency value matches; also check channel code (if given)
        if(channelCodeStr == null || freqChannelItemsArray[i].equals(channelCodeStr,frequencyVal))
          return freqChannelItemsArray[i];
      }
    }
    return null;
  }

  /**
   * Returns the channel code associated with the given frequency value.
   * @param frequencyVal frequency value.
   * @return Code-value string (i.e., "F4"), or null if no match.
   */
  public String getChannelCodeForFreqVal(short frequencyVal)
  {
    final FreqChannelItem itemObj;
    return ((itemObj=getFreqChannelItemObj(null,frequencyVal)) != null) ?
                                              itemObj.channelCodeStr : null;
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
   * Returns a frequency-channel-items array containing items matching the
   * frequencies (and RSSI values) in the given list string.
   * @param scanStr list string of space-delimited "freq=RSSI" entries.
   * @return A new array of FreqChannelItem objects (empty array if no parsable data).
   */
  public FreqChannelItem[] getFChanItemsArrForScanStr(String scanStr)
  {
    final ArrayList<FreqChannelItem> itemsList = new ArrayList<FreqChannelItem>();
    try
    {
      scanStr = scanStr.trim();                  //remove any leading spaces
      final int scanStrLen = scanStr.length();
      if(scanStrLen > 2 && Character.isDigit(scanStr.charAt(0)))
      {  //given string starts with a numeric (not an error message)
        int ePos, sPos = 0;
        FreqChannelItem itemObj;
        while(sPos < scanStrLen)
        {  //for each "freq=RSSI" entry
          if((ePos=scanStr.indexOf(' ',sPos+1)) < 0)
            ePos = scanStrLen;
          if(ePos > sPos)
          {  //non-empty entry; parse into new 'FreqChannelItem' object
            if((itemObj=createItemForScanEntryStr(scanStr.substring(sPos,ePos))) != null)
              itemsList.add(itemObj);
            else
            {  //error parsing
              Log.e(LOG_TAG, "Unable to parse entry in getFChanItemsArrForScanStr():  " +
                                                                   scanStr.substring(sPos,ePos));
            }
          }
          sPos = ePos + 1;
        }
      }
    }
    catch(Exception ex)
    {  //some kind of exception error; log it and move on
      Log.e(LOG_TAG, "Exception in getFChanItemsArrForScanStr()", ex);
    }
         //convert list to array and return it:
    final FreqChannelItem[] retArr = new FreqChannelItem[itemsList.size()];
    return itemsList.toArray(retArr);
  }

  /**
   * Creates a frequency-channel-item object for the given scan-entry string.
   * @param entryStr a string containing a "freq=RSSI" entry.
   * @return A new frequency-channel-item object containing the specified
   * frequency and RSSI values, or null if a parsing error occurred.
   */
  protected FreqChannelItem createItemForScanEntryStr(String entryStr)
  {
    try
    {
      final int p;
      if((p=entryStr.indexOf('=')) > 0 && p < entryStr.length()-1)
      {  //found equals-sign character; parse numbers on either side
        final short freqVal = Short.parseShort(entryStr.substring(0,p).trim());
        final short rssiVal = Short.parseShort(entryStr.substring(p+1).trim());
        FreqChannelItem itemObj;  //get item with matching frequency from table:
        if((itemObj=getFreqChannelItemObj(null,freqVal)) != null)
          itemObj = new FreqChannelItem(itemObj);     //create copy of item from table
        else
          itemObj = new FreqChannelItem("",freqVal,-1);    //create new item for freq value
        itemObj.setDisplayRssiValue(rssiVal);
        return itemObj;
      }
    }
    catch(Exception ex)
    {  //some kind of exception error; return null
    }
    return null;
  }

  /**
   * Converts given string of space or comma-separated numeric values to
   * a list of 'Short' objects.
   * @param str string containing space or comma-separated numeric values,
   * or null or an empty string for none.
   * @return A list of 'Short' objects, or null if a parsing error occurred.
   */
  public static List<Short> convStringToShortsList(String str)
  {
    try
    {
      final ArrayList<Short> retList = new ArrayList<Short>();
      final int strLen = (str != null) ? str.length() : 0;
      int ePos, sPos = 0;
      while(sPos < strLen)
      {
        while((str.charAt(sPos) == ' ' || str.charAt(sPos) == ',') && ++sPos < strLen);
        ePos = sPos;
        while(++ePos < strLen && str.charAt(ePos) != ' ');
        if(sPos < ePos)
          retList.add(Short.parseShort(str.substring(sPos,ePos)));
        sPos = ePos + 1;
      }
      return retList;
    }
    catch(NumberFormatException ex)
    {
      return null;
    }
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
   * Class FreqChannelItem defines a frequency (channel) value.  An array of
   * FreqChannelItem objects may be handled like an array of String objects.
   */
  public static class FreqChannelItem implements CharSequence
  {
    public final String channelCodeStr;
    public final short frequencyVal;
    public final int itemArrayIndex;
    protected String displayString;
    protected short displayRssiValue = -1;       //-1 == don't show

    /**
     * Creates a frequency (channel) object.
     * @param channelCodeStr code value for channel (i.e., "F4"), or null for none.
     * @param frequencyVal frequency value for channel.
     * @param itemArrayIndex array-index value for item.
     */
    public FreqChannelItem(String channelCodeStr, short frequencyVal, int itemArrayIndex)
    {
      this.channelCodeStr = channelCodeStr;
      this.frequencyVal = frequencyVal;
      this.itemArrayIndex = itemArrayIndex;
      updateDisplayString();
    }

    /**
     * Creates a copy of a frequency (channel) object.
     * @param itemObj source object for copy.
     */
    public FreqChannelItem(FreqChannelItem itemObj)
    {
      this.channelCodeStr = itemObj.channelCodeStr;
      this.frequencyVal = itemObj.frequencyVal;
      this.itemArrayIndex = itemObj.itemArrayIndex;
      this.displayRssiValue = itemObj.displayRssiValue;
      updateDisplayString();
    }

    /**
     * Creates a copy of a frequency (channel) object, but with the given
     * array-index value.
     * @param itemObj source object for copy.
     * @param idxVal array-index value for item.
     */
    public FreqChannelItem(FreqChannelItem itemObj, int idxVal)
    {
      this.channelCodeStr = itemObj.channelCodeStr;
      this.frequencyVal = itemObj.frequencyVal;
      this.itemArrayIndex = idxVal;
      this.displayRssiValue = itemObj.displayRssiValue;
      updateDisplayString();
    }

    /**
     * Updates the display string for item using its current values.
     */
    protected final void updateDisplayString()
    {
      displayString = ((channelCodeStr != null) ? channelCodeStr : "") +
                                     ((frequencyVal > (short)0) ? ("    " + frequencyVal) : "") +
                     ((displayRssiValue >= (short)0) ? ("            " + displayRssiValue) : "");
    }

    /**
     * Sets the RSSI value to be shown in the display string.
     * @param val RSSI value to be shown, or -1 for none (don't show any value).
     */
    public void setDisplayRssiValue(short val)
    {
      displayRssiValue = val;
      updateDisplayString();
    }

    /**
     * Returns the RSSI value to be shown in the display string.
     * @return The RSSI value to be shown, or -1 for none (don't show any value).
     */
    public short getDisplayRssiValue()
    {
      return displayRssiValue;
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
