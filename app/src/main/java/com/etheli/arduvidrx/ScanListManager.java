//ScanListManager.java:  Manages a list of frequency (channel) values where
//                       each value may be selected or not-selected.
//
//   5/7/2017 -- [ET]
//

package com.etheli.arduvidrx;

import java.util.ArrayList;
import java.util.List;

/**
 * Class ScanListManager manages a list of frequency (channel) values where
 * each value may be selected or not-selected.
 */
public class ScanListManager
{
  private final ArrayList<FreqChannelSelectItem> baseFreqChannelSelItemsList =
                                                          new ArrayList<FreqChannelSelectItem>();
  private final ArrayList<FreqChannelSelectItem> presetsFreqChannelSelItemsList =
                                                          new ArrayList<FreqChannelSelectItem>();

  /**
   * Creates a scan-list manager.
   * @param itemsArr array of frequency (channel) objects for the list.
   */
  public ScanListManager(FrequencyTable.FreqChannelItem [] itemsArr)
  {
         //convert given array to array of 'FreqChannelSelectItem' objects,
         // removing items with duplicate frequency values:
    FreqChannelSelectItem newSelItemObj;
    for(FrequencyTable.FreqChannelItem itemObj : itemsArr)
    {  //for each frequency (channel) object in given list; add if frequency not duplicate
      newSelItemObj = new FreqChannelSelectItem(itemObj);
      if(!baseFreqChannelSelItemsList.contains(newSelItemObj))
        baseFreqChannelSelItemsList.add(newSelItemObj);
    }
         //enter "preset" items into presets list:
    enterPresetFreqChanSelItems(presetsFreqChannelSelItemsList);
  }

  /**
   * Returns the array of frequency (channel) objects for this manager.
   * @return An array of 'FreqChannelSelectItem' objects.
   */
  public FreqChannelSelectItem [] getFreqChannelSelItemsArr()
  {
    final FreqChannelSelectItem [] retArr = new FreqChannelSelectItem[
                       baseFreqChannelSelItemsList.size()+presetsFreqChannelSelItemsList.size()];
    int idx = 0;             //returned array contains "base" + "preset" items
    for(FreqChannelSelectItem itemObj : baseFreqChannelSelItemsList)
      retArr[idx++] = itemObj;
    for(FreqChannelSelectItem itemObj : presetsFreqChannelSelItemsList)
      retArr[idx++] = itemObj;
    return retArr;
  }

  /**
   * An array of booleans, one for each frequency (channel) object
   * in the scan list.
   * @param clearAllFlag true to set all array entries to 'false'; or
   * false to set array entries to booleans indicating which frequency
   * (channel) objects are selected.
   * @return A new array of booleans.
   */
  public boolean [] getItemsSelectedArr(boolean clearAllFlag)
  {
    final boolean [] retFlagsArr = new boolean [
                       baseFreqChannelSelItemsList.size()+presetsFreqChannelSelItemsList.size()];
    if(clearAllFlag)
    {  //return array with all flags clear
      for(int i=0; i<retFlagsArr.length; ++i)
        retFlagsArr[i] = false;
    }
    else
    {  //return array with flags set as per 'selectedFlag' in "base" entries
      int i = 0;
      for(FreqChannelSelectItem itemObj : baseFreqChannelSelItemsList)
        retFlagsArr[i++] = itemObj.selectedFlag;
      while(i < retFlagsArr.length)
        retFlagsArr[i++] = false;           //clear flags for all "preset" entries
    }
    return retFlagsArr;
  }

  /**
   * An array of booleans indicating which frequency (channel) objects
   * are selected.
   * @return A new array of booleans.
   */
  public boolean [] getItemsSelectedArr()
  {
    return getItemsSelectedArr(false);
  }

  /**
   * Sets which frequency (channel) objects in the list are selected
   * using the given array of booleans.
   * @param selFlagsArr An array of booleans.
   */
  public void setItemsSelectedArr(boolean [] selFlagsArr)
  {
    if(selFlagsArr.length <=
                      baseFreqChannelSelItemsList.size() + presetsFreqChannelSelItemsList.size())
    {  //given array is large enough
      int i = 0;
      for(FreqChannelSelectItem itemObj : baseFreqChannelSelItemsList)
        itemObj.selectedFlag = selFlagsArr[i++];
      for(FreqChannelSelectItem itemObj : presetsFreqChannelSelItemsList)
        itemObj.selectedFlag = selFlagsArr[i++];
    }
    else
    {  //given array not large enough; check bounds
      int i = 0;
      for(FreqChannelSelectItem itemObj : baseFreqChannelSelItemsList)
        itemObj.selectedFlag = (i < selFlagsArr.length && selFlagsArr[i++]);
      for(FreqChannelSelectItem itemObj : presetsFreqChannelSelItemsList)
        itemObj.selectedFlag = (i < selFlagsArr.length && selFlagsArr[i++]);
    }
  }

  /**
   * Returns a string of space-separated numeric values corresponding to
   * the frequency (channel) objects in the list that are selected.
   * @return A string of space-separated numeric values, or "0" if none
   * are selected.
   */
  public String getSelectedFreqsListStr()
  {
    final ArrayList<FreqChannelSelectItem> selList = new ArrayList<FreqChannelSelectItem>();
    FreqChannelSelectItem tmpItemObj;
    for(FreqChannelSelectItem presetItemObj : presetsFreqChannelSelItemsList)
    {  //for each "preset" entry item
      if(presetItemObj.selectedFlag && presetItemObj.frequenciesArray != null)
      {  //"preset" entry item was selected and has a set of frequency values
        for(short freqVal : presetItemObj.frequenciesArray)
        {  //for each frequency value for preset; add to list (if not already in list)
          tmpItemObj = new FreqChannelSelectItem(null,freqVal,0);
          if(!selList.contains(tmpItemObj))
            selList.add(tmpItemObj);
        }
      }
    }
    for(FreqChannelSelectItem baseItemObj : baseFreqChannelSelItemsList)
    {  //for each "base" entry item; if selected then add to list (if not already in list)
      if(baseItemObj.selectedFlag && !selList.contains(baseItemObj))
        selList.add(baseItemObj);
    }
         //convert items in local "selected" list to string of numeric frequency values:
    final StringBuilder buff = new StringBuilder();
    for(FreqChannelSelectItem itemObj : selList)
    {  //for each frequency item in local "selected" list; add to string buffer
      buff.append(' ');
      buff.append(itemObj.frequencyVal);
    }
    return (buff.length() > 0) ? buff.toString() : "0";
  }

  /**
   * Sets which frequency (channel) objects in the list are selected
   * using the given string of space-separated numeric values.  For any
   * values not matching a frequency (channel) object in the list, a
   * new frequency (channel) object is created and added to the list.
   * @param listStr string containing space or comma-separated numeric values;
   * or "0", null or an empty list for no objects selected.
   * @return true if successful; false if a parsing error occurred.
   */
  public boolean setSelectedFreqsListStr(String listStr)
  {
    final List<Short> parsedList;      //parse string numerics to list of 'Short' objects:
    if((parsedList=FrequencyTable.convStringToShortsList(listStr)) != null)
    {  //parse succeeded
      for(FreqChannelSelectItem itemObj : baseFreqChannelSelItemsList)
        itemObj.selectedFlag = false;       //initialize all items to not-selected
      for(FreqChannelSelectItem itemObj : presetsFreqChannelSelItemsList)
        itemObj.selectedFlag = false;
              //if list empty or just "0" then return with all objects deselected:
      if(!isScanListEmpty(parsedList))
      {  //list not empty (and not just "0")
        FreqChannelSelectItem itemObj;
        for(short freqVal : parsedList)
        {  //for each frequency value specified
          if((itemObj=getSelItemForFreqVal(freqVal)) != null)
            itemObj.selectedFlag = true;         //if matches list item then select item
          else if(freqVal > (short)0)
          {  //item frequency value not in list (and is valid); add new item to base list
            itemObj = new FreqChannelSelectItem(null,freqVal,baseFreqChannelSelItemsList.size());
            itemObj.selectedFlag = true;                   //new item is selected
            baseFreqChannelSelItemsList.add(itemObj);      //enter at end of list
          }
        }
      }
      return true;
    }
    return false;
  }

  /**
   * Determines if the given monitor/scan list is empty (contains no
   * values or only contains a single '0' value).
   * @param listObj list of frequency values.
   * @return true if the list is empty; false if not.
   */
  public static boolean isScanListEmpty(List<Short> listObj)
  {
    return (listObj == null || listObj.size() <= 0 ||
                                            (listObj.size() == 1 && listObj.get(0) == (short)0));
  }

  /**
   * Determines if the given monitor/scan list is empty (contains no
   * values or only contains a single '0' value).
   * @param listStr string containing space or comma-separated numeric values;
   * or "0", null or an empty list for no objects selected.
   * @return true if the list is empty; false if not.
   */
  public static boolean isScanListEmpty(String listStr)
  {
    return isScanListEmpty(FrequencyTable.convStringToShortsList(listStr));
  }

  /**
   * Finds the frequency (channel) object in the "base" list matching the
   * given frequency value.
   * @param freqVal frequency value (in MHz).
   * @return The matching frequency (channel) object, or null if no match.
   */
  private FreqChannelSelectItem getSelItemForFreqVal(short freqVal)
  {
    for(FreqChannelSelectItem itemObj : baseFreqChannelSelItemsList)
    {
      if(itemObj.frequencyVal == freqVal)
        return itemObj;
    }
    return null;
  }

  /**
   * Adds preset-entry items to the given list.
   * @param itemsList list of 'FreqChannelSelectItem' objects.
   */
  public static void enterPresetFreqChanSelItems(List<FreqChannelSelectItem> itemsList)
  {
    itemsList.add(new FreqChannelSelectItem("F",
                                         new short[] {5740,5760,5780,5800,5820,5840,5860,5880}));
    itemsList.add(new FreqChannelSelectItem("E",
                                         new short[] {5705,5685,5665,5645,5885,5905,5925,5945}));
    itemsList.add(new FreqChannelSelectItem("A",
                                         new short[] {5865,5845,5825,5805,5785,5765,5745,5725}));
    itemsList.add(new FreqChannelSelectItem("R",
                                         new short[] {5658,5695,5732,5769,5806,5843,5880,5917}));
    itemsList.add(new FreqChannelSelectItem("B",
                                         new short[] {5733,5752,5771,5790,5809,5828,5847,5866}));
    itemsList.add(new FreqChannelSelectItem("L",
                                         new short[] {5362,5399,5436,5473,5510,5547,5584,5621}));
    itemsList.add(new FreqChannelSelectItem("IMD5", new short[] {5685,5760,5800,5860,5905}));
    itemsList.add(new FreqChannelSelectItem("IMD6", new short[] {5645,5685,5760,5800,5860,5905}));
    itemsList.add(new FreqChannelSelectItem("ET5",  new short[] {5665,5725,5820,5860,5945}));
    itemsList.add(new FreqChannelSelectItem("ET5A", new short[] {5665,5752,5800,5866,5905}));
    itemsList.add(new FreqChannelSelectItem("ET5B", new short[] {5665,5752,5800,5865,5905}));
    itemsList.add(new FreqChannelSelectItem("ET5C", new short[] {5665,5760,5800,5865,5905}));
    itemsList.add(new FreqChannelSelectItem("ETBest6",
                                                   new short[] {5645,5685,5760,5805,5905,5945}));
    itemsList.add(new FreqChannelSelectItem("ET6minus1", new short[] {5645,5685,5760,5905,5945}));
  }


  /**
   * Class FreqChannelSelectItem defines a frequency (channel) value with a
   * 'selectedFlag' value.  An array of FreqChannelSelectItem objects may be
   * handled like an array of String objects.
   */
  public static class FreqChannelSelectItem extends FrequencyTable.FreqChannelItem
  {
    public boolean selectedFlag = false;
    public final short [] frequenciesArray;      //used for "preset" entries

    /**
     * Creates a frequency (channel) object.
     * @param channelCodeStr code value for channel (i.e., "F4"), or null for none.
     * @param frequencyVal frequency value for channel.
     * @param itemArrayIndex array-index value for item.
     */
    public FreqChannelSelectItem(String channelCodeStr, short frequencyVal, int itemArrayIndex)
    {
      super(channelCodeStr,frequencyVal,itemArrayIndex);
      frequenciesArray = null;                   //not a "preset" entry
    }

    /**
     * Creates a frequency (channel) object for a "preset" entry (containing
     * a set of frequency values).
     * @param presetNameStr name for preset.
     * @param freqsArr array of frequency values for preset.
     */
    public FreqChannelSelectItem(String presetNameStr, short [] freqsArr)
    {
      super((" [preset]    " + presetNameStr), (short)0, 0);
      frequenciesArray = freqsArr;
    }

    /**
     * Creates a copy of a frequency (channel) object.
     * @param itemObj source object for copy.
     */
    public FreqChannelSelectItem(FrequencyTable.FreqChannelItem itemObj)
    {
      super(itemObj);
      frequenciesArray = null;                   //not a "preset" entry
    }

    /**
     * Determines if equal to other object, based on the frequency value.
     * @return true if equal; false if not.
     */
    @Override
    public boolean equals(Object obj)
    {
      if((obj instanceof FrequencyTable.FreqChannelItem) &&
                            ((FrequencyTable.FreqChannelItem)obj).frequencyVal == frequencyVal &&
                                ((FrequencyTable.FreqChannelItem)obj).frequencyVal != (short)0 &&
                                                                        frequencyVal != (short)0)
      {  //frequency values are equal and neither is zero (for "preset" entry)
        return true;
      }
      return super.equals(obj);
    }
  }
}
