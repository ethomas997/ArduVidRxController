//GuiUtils.java:  GUI-helper classes and utilities.
//
//  4/20/2017 -- [ET]
//
package com.etheli.util;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Paint;
import android.util.TypedValue;
import android.view.Display;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TabHost;
import android.widget.TextView;

import static android.R.style.Widget;

/**
 * Class GuiUtils defines a set of GUI-helper classes and utilities.
 */
public class GuiUtils
{
  private static Paint guiUtilsPaintObj = null;
  private static float guiUtilsSpaceWidthValue = 2.0f;

  /**
   * Sets the on-click listener for all Button objects that are children of
   * the given View (ViewGroup) object or its descendants.
   * @param viewObj parent View (ViewGroup) object.
   * @param listenerObj on-click listener object.
   */
  public static void setViewButtonsClickListener(View viewObj, View.OnClickListener listenerObj)
  {
    if(viewObj instanceof ViewGroup)
    {  //given view is a ViewGroup
      final ViewGroup vGroupObj = (ViewGroup)viewObj;
      View vObj;
      for(int i=0; i<vGroupObj.getChildCount(); ++i)
      {  //for each child; if button then set on-click listener
        if((vObj=vGroupObj.getChildAt(i)) instanceof Button)
          ((Button)vObj).setOnClickListener(listenerObj);
        else if(vObj instanceof ViewGroup)
          setViewButtonsClickListener(vObj,listenerObj);
      }
    }
  }

  /**
   * Sets the enabled state for all Button objects that are children of
   * the given View (ViewGroup) object or its descendants.
   * @param viewObj parent View (ViewGroup) object.
   * @param enabledFlag true for enabled; false for disabled.
   */
  public static void setViewButtonsEnabledState(View viewObj, boolean enabledFlag)
  {
    if(viewObj instanceof ViewGroup)
    {  //given view is a ViewGroup
      final ViewGroup vGroupObj = (ViewGroup)viewObj;
      View vObj;
      for(int i=0; i<vGroupObj.getChildCount(); ++i)
      {  //for each child; if button then set on-click listener
        if((vObj=vGroupObj.getChildAt(i)) instanceof Button)
          ((Button)vObj).setEnabled(enabledFlag);
        else if(vObj instanceof ViewGroup)
          setViewButtonsEnabledState(vObj,enabledFlag);
      }
    }
  }

  /**
   * Scales the text size on all Button objects that are children of
   * the given View (ViewGroup) object or its descendants.
   * @param viewObj parent View (ViewGroup) object.
   * @param scaleVal scale-factor value.
   */
  public static void scaleButtonTextSizes(View viewObj, float scaleVal)
  {
    if(viewObj instanceof ViewGroup)
    {  //given view is a ViewGroup
      final ViewGroup vGroupObj = (ViewGroup)viewObj;
      View vObj;
      for(int i=0; i<vGroupObj.getChildCount(); ++i)
      {  //for each child; if button then set on-click listener
        if((vObj=vGroupObj.getChildAt(i)) instanceof Button && !(vObj instanceof CheckBox))
        {  //object is Button (but not CheckBox)
          ((Button)vObj).setTextSize(TypedValue.COMPLEX_UNIT_PX,
                                                  ((Button)vObj).getTextSize() * scaleVal);
        }
        else if(vObj instanceof ViewGroup)
          scaleButtonTextSizes(vObj,scaleVal);
      }
    }
  }

  /**
   * Determines and returns the current orientation of the screen.  Based on:
   * http://stackoverflow.com/questions/2795833/check-orientation-on-android-phone
   * @param activityObj Activity object to use for 'getWindowManager()' call.
   * @return One of:  Configuration.ORIENTATION_PORTRAIT,
   * Configuration.ORIENTATION_LANDSCAPE or ORIENTATION_UNDEFINED.
   */
  public static int getScreenOrientation(Activity activityObj)
  {
    int orientation = Configuration.ORIENTATION_UNDEFINED;
    try
    {
      final Display displayObj = activityObj.getWindowManager().getDefaultDisplay();
      if(displayObj.getWidth() <= displayObj.getHeight())
        orientation = Configuration.ORIENTATION_PORTRAIT;
      else
        orientation = Configuration.ORIENTATION_LANDSCAPE;
    }
    catch(Exception ex)
    {
    }
    return orientation;
  }

  /**
   * Determines the screen width & height and returns the shorter value.
   * The returned value is rounded to the nearest/next 100 (to try to account
   * for the size value being lowered because of screen decorations).
   * @param activityObj Activity object to use for 'getWindowManager()' call.
   * @return The shorter of the screen width & height values, or 0 if error.
   */
  public static int getShorterScreenSizeValue(Activity activityObj)
  {
    try
    {
      final Display displayObj = activityObj.getWindowManager().getDefaultDisplay();
      final int szVal = (displayObj.getWidth() <= displayObj.getHeight()) ?
                                     displayObj.getWidth() : displayObj.getHeight();
      return ((szVal + 99) / 100) * 100;
    }
    catch(Exception ex)
    {
    }
    return 0;
  }

  /**
   * Determines the screen width & height and returns the longer value.
   * The returned value is rounded to the nearest/next 100 (to try to account
   * for the size value being lowered because of screen decorations).
   * @param activityObj Activity object to use for 'getWindowManager()' call.
   * @return The longer of the screen width & height values, or 0 if error.
   */
  public static int getLongerScreenSizeValue(Activity activityObj)
  {
    try
    {
      final Display displayObj = activityObj.getWindowManager().getDefaultDisplay();
      final int szVal = (displayObj.getWidth() <= displayObj.getHeight()) ?
                                                            displayObj.getHeight() :
                                                              displayObj.getWidth();
      return ((szVal + 99) / 100) * 100;
    }
    catch(Exception ex)
    {
    }
    return 0;
  }

  /**
   * Returns the current width of the display.
   * @param activityObj Activity object to use for 'getWindowManager()' call.
   * @return The current width of the display, or 0 if error.
   */
  public static int getDisplayWidthValue(Activity activityObj)
  {
    try
    {
      return activityObj.getWindowManager().getDefaultDisplay().getWidth();
    }
    catch(Exception ex)
    {
    }
    return 0;
  }

  /**
   * Determines if the screen size is SMALL or NORMAL.
   * @param activityObj Activity object to use for 'getApplicationContext()' call.
   * @return true if the screen size is SMALL or NORMAL (or if an error occurs).
   */
  public static boolean isSmallOrNormalScreenSize(Activity activityObj)
  {
    try
    {
      final int screenLayout = activityObj.getApplicationContext().getResources().
                          getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
      return (screenLayout == Configuration.SCREENLAYOUT_SIZE_SMALL ||
                                         screenLayout == Configuration.SCREENLAYOUT_SIZE_NORMAL);
    }
    catch(Exception ex)
    {
    }
    return true;
  }

  /**
   * Adjusts the top and bottom margins on the given view item.
   * @param itemObj view item object.
   * @param topScale scaling factor for top margin.
   * @param bottomScale scaling factor for bottom margin.
   */
  public static void adjustItemLayoutMargins(View itemObj, float topScale, float bottomScale)
  {
    try
    {
      final ViewGroup.LayoutParams lParamsObj = itemObj.getLayoutParams();
      final ViewGroup.MarginLayoutParams mLParamsObj;
      if(lParamsObj instanceof ViewGroup.MarginLayoutParams)
      {  //layout params object is MarginLayoutParams type
        mLParamsObj = (ViewGroup.MarginLayoutParams)lParamsObj;
        mLParamsObj.topMargin = Math.round(mLParamsObj.topMargin * topScale);
        mLParamsObj.bottomMargin = Math.round(mLParamsObj.bottomMargin * bottomScale);
      }
    }
    catch(Exception ex)
    {  //some kind of exception error
      ex.printStackTrace();
    }
  }

  /**
   * Sets the utility Paint object used by this class.
   * @param paintObj Paint object to use.
   */
  public static void setUtilPaintObj(Paint paintObj)
  {
    guiUtilsPaintObj = paintObj;
              //set space-width value used by 'getFillerStr()':
    if((guiUtilsSpaceWidthValue=paintObj.measureText(" ")) < 0.001f)
      guiUtilsSpaceWidthValue = 1.0f;
  }

  /**
   * Sets the utility Paint object used by this class.
   * @param viewObj View object to use to create a "generic" Paint object.
   */
  public static void setUtilPaintObj(View viewObj)
  {
    try
    {
      setUtilPaintObj((new TextView(viewObj.getContext())).getPaint());
    }
    catch(Exception ex)
    {
      ex.printStackTrace();
    }
  }

  /**
   * Returns the utility Paint object used by this class.
   * @return Paint object in use, or null if none set.
   */
  public static Paint getUtilPaintObj()
  {
    return guiUtilsPaintObj;
  }

  /**
   * Returns the width of the given text, using the utility Paint object
   * set via 'setUtilPaintObj()'.
   * @param str string to measure.
   * @return The width of the text, or 0.0 if error.
   */
  public static float measureTextViaUtilPaint(String str)
  {
    try
    {
      if(guiUtilsPaintObj == null)
        return str.length();
      return guiUtilsPaintObj.measureText(str);
    }
    catch(Exception ex)
    {
    }
    return 0.0f;
  }

  /**
   * Returns the font-metrics height (ascent to descent) using the utility
   * Paint object set via 'setUtilPaintObj()'.
   * @return The font-metrics height, or 0.0 if error.
   */
  public static float getFontMetricsHeightViaUtilPaint()
  {
    try
    {
      if(guiUtilsPaintObj == null)
        return 0.0f;
      return guiUtilsPaintObj.getFontMetrics().descent - guiUtilsPaintObj.getFontMetrics().ascent;
    }
    catch(Exception ex)
    {
    }
    return 0.0f;
  }

  /**
   * Calculates the number of spaces needed to fill the remaining width of
   * a field and returns a string containing that many spaces.
   * @param fieldWidth target field width.
   * @param diffStr text that is taking up part of the space in the field,
   * or null for none.
   * @return A string containing zero or more spaces.
   */
  public static String getFillerStr(float fieldWidth, Object diffStr)
  {
    final float diffLen = (diffStr instanceof String) ?
                                   measureTextViaUtilPaint((String)diffStr) : 0.0f;
    return getSpacesStr(Math.round((fieldWidth-diffLen)/guiUtilsSpaceWidthValue));
  }

  /**
   * Returns a string containing the given number of spaces (up to 50)
   * @param numSpaces Number of spaces (up to 50).
   * @return A string containing the given number of spaces.
   */
  public static String getSpacesStr(int numSpaces)
  {
    if(numSpaces <= 0)
      return "";
    switch(numSpaces)
    {
      case 1:  return " ";
      case 2:  return "  ";
      case 3:  return "   ";
      case 4:  return "    ";
      case 5:  return "     ";
      case 6:  return "      ";
      case 7:  return "       ";
      case 8:  return "        ";
      case 9:  return "         ";
      case 10:  return "          ";
      case 11:  return "           ";
      case 12:  return "            ";
      case 13:  return "             ";
      case 14:  return "              ";
      case 15:  return "               ";
      case 16:  return "                ";
      case 17:  return "                 ";
      case 18:  return "                  ";
      case 19:  return "                   ";
      case 20:  return "                    ";
      case 21:  return "                     ";
      case 22:  return "                      ";
      case 23:  return "                       ";
      case 24:  return "                        ";
      case 25:  return "                         ";
      case 26:  return "                          ";
      case 27:  return "                           ";
      case 28:  return "                            ";
      case 29:  return "                             ";
      case 30:  return "                              ";
      case 31:  return "                               ";
      case 32:  return "                                ";
      case 33:  return "                                 ";
      case 34:  return "                                  ";
      case 35:  return "                                   ";
      case 36:  return "                                    ";
      case 37:  return "                                     ";
      case 38:  return "                                      ";
      case 39:  return "                                       ";
      case 40:  return "                                        ";
      case 41:  return "                                         ";
      case 42:  return "                                          ";
      case 43:  return "                                           ";
      case 44:  return "                                            ";
      case 45:  return "                                             ";
      case 46:  return "                                              ";
      case 47:  return "                                               ";
      case 48:  return "                                                ";
      case 49:  return "                                                 ";
      default:  return "                                                  ";
    }
  }
}
