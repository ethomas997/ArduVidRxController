//GuiUtils.java:  GUI-helper classes and utilities.
//
//  2/6/2017 -- [ET]
//
package com.etheli.util;

import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * Class GuiUtils defines a set of GUI-helper classes and utilities.
 */
public class GuiUtils
{

  /**
   * Sets the on-click listener for all Button objects that are children of
   * the given View (ViewGroup) object.
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
      }
    }
  }

  /**
   * Sets the enabled state for all Button objects that are children of
   * the given View (ViewGroup) object.
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
      }
    }
  }
}
