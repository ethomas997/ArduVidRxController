//Averager.java:  Implements a "windowed" average.
//
//  2/12/2017 -- [ET]
//

package com.etheli.util;

/**
 * Class Averager implements a "windowed" average, where the average of
 * the last 'n' values entered is calculated.
 */
public class Averager
{
  private final int [] valuesArray;         //array of values in "window"
  private final int windowSize;             //size of array "window"
  private long valuesTotal;                 //total of values in "window"
  private int valuesCount;                  //number of values in "window"
  private int valuesPos;                    //current position in "window"

  /**
   * Creates a "windowed" averaging object.
   * @param windowSize the number of values to track and use when
   * computing the average.  If the value is not greater than zero
   * then a runtime exception is thrown.
   */
  public Averager(int windowSize)
  {
    if(windowSize <= 0)                     //if bad "window" size then
      throw new RuntimeException(           //throw runtime exception
                        "Parameter 'windowSize' must be greater than zero");
    this.windowSize = windowSize;           //set "window" size
    valuesArray = new int[windowSize];      //allocate array for values
    clear();                                //set all values to zero
  }

  /**
   * Clears the averaging "window" back to its initial state.  All
   * values are set to zero.
   */
  public void clear()
  {
    for(int p=0; p<windowSize; ++p)         //clear values array
      valuesArray[p] = 0;
    valuesTotal = 0L;                       //init total
    valuesCount = valuesPos = 0;            //init count and position
  }

  /**
   * Enters the new value into the averaging "window".
   * @param val the value to be entered.
   * @return The new calculated average.
   */
  public int enter(int val)
  {
    valuesTotal -= valuesArray[valuesPos];            //sub previous value
    valuesTotal += (valuesArray[valuesPos] = val);    //add & save new value
    if(++valuesPos >= windowSize)      //increment position in "window"
      valuesPos = 0;                   //if past end then wrap-around to beg
    if(valuesCount < windowSize)       //if "window" not yet full then
      ++valuesCount;                   //increment current count
    return getAverage();               //return new average
  }

  /**
   * Returns the current average of values in the "window".
   * @return  the current average of values in the "window".
   */
  public int getAverage()
  {
    return (valuesCount > 0) ? (int)(valuesTotal/valuesCount) : 0;
  }
}
