//DialogUtils.java:  Dialog-helper classes and utilities.
//
//  3/8/2017 -- [ET]
//

package com.etheli.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;

import static android.view.WindowManager.LayoutParams;

/**
 * Class DialogUtils defines a set of dialog-helper classes and utilities.
 */
public class DialogUtils
{
  //Private constructor for static-only access.
  private DialogUtils()
  {
  }

  /**
   * Displays a single-choice dialog containing the given array of items.
   * Selecting an item will dismiss the dialog and invoke the listener.
   * @param activityObj parent activity for dialog.
   * @param titleId resource ID of title for dialog.
   * @param charSeqArr array of items to be displayed.
   * @param initIdx index of initial item to be selected, or -1 for none.
   * @param closeButtonId resource ID of text for 'close' button.
   * @param listenerObj listener to be invoked when an item is selected.
   * @return A new DialogFragment object.
   */
  public static DialogFragment showSingleChoiceDialogFragment(Activity activityObj, int titleId,
                                      CharSequence [] charSeqArr, int initIdx, int closeButtonId,
                                                          DialogItemSelectedListener listenerObj)
  {
    return showSingleChoiceDialogFragment(activityObj,activityObj.getString(titleId),charSeqArr,
                                       initIdx,activityObj.getString(closeButtonId),listenerObj);
  }

  /**
   * Displays a single-choice dialog containing the given array of items.
   * Selecting an item will dismiss the dialog and invoke the listener.
   * @param activityObj parent activity for dialog.
   * @param titleStr title string for dialog, or null for none.
   * @param charSeqArr array of items to be displayed.
   * @param initIdx index of initial item to be selected, or -1 for none.
   * @param closeButtonStr text for 'close' button, or null for none.
   * @param listenerObj listener to be invoked when an item is selected.
   * @return A new DialogFragment object.
   */
  public static DialogFragment showSingleChoiceDialogFragment(Activity activityObj, String titleStr,
                                  CharSequence [] charSeqArr, int initIdx, String closeButtonStr,
                                                          DialogItemSelectedListener listenerObj)
  {
    final DialogFragment fragObj = createSingleChoiceDialogFragment(
                        titleStr,charSeqArr,initIdx,closeButtonStr,listenerObj);
    fragObj.show(activityObj.getFragmentManager(),"SingleChoiceDialogFragment");
    return fragObj;
  }

  /**
   * Creates a single-choice dialog containing the given array of items.
   * Selecting an item will dismiss the dialog and invoke the listener.
   * @param titleStr title string for dialog, or null for none.
   * @param charSeqArr array of items to be displayed.
   * @param initIdx index of initial item to be selected, or -1 for none.
   * @param closeButtonStr text for 'close' button, or null for none.
   * @param listenerObj listener to be invoked when an item is selected.
   * @return A new DialogFragment object.
   */
  public static DialogFragment createSingleChoiceDialogFragment(String titleStr,
                                  CharSequence [] charSeqArr, int initIdx, String closeButtonStr,
                                                          DialogItemSelectedListener listenerObj)
  {
    final SingleChoiceDialogFragment fragObj = new SingleChoiceDialogFragment();
    fragObj.setInitialData(titleStr,charSeqArr,initIdx,closeButtonStr,listenerObj);
    return fragObj;
  }

  /**
   * Displays a dialog to edit a numeric value.
   * @param activityObj parent activity for dialog.
   * @param titleId resource ID of title for dialog.
   * @param initVal initial value for editor in dialog.
   * @param maxLen maximum length (number of digits) for editor in dialog,
   * or 0 for no limit.
   * @param listenerObj listener to be invoked after a value is entered.
   * @return A new DialogFragment object.
   */
  public static DialogFragment showEditNumberDialogFragment(Activity activityObj, int titleId,
                                 int initVal, int maxLen, DialogItemSelectedListener listenerObj)
  {
    return showEditNumberDialogFragment(activityObj,activityObj.getString(titleId),
                                                                     initVal,maxLen,listenerObj);
  }

  /**
   * Displays a dialog to edit a numeric value.
   * @param activityObj parent activity for dialog.
   * @param titleStr title string for dialog, or null for none.
   * @param initVal initial value for editor in dialog.
   * @param maxLen maximum length (number of digits) for editor in dialog,
   * or 0 for no limit.
   * @param listenerObj listener to be invoked after a value is entered.
   * @return A new DialogFragment object.
   */
  public static DialogFragment showEditNumberDialogFragment(Activity activityObj, String titleStr,
                                 int initVal, int maxLen, DialogItemSelectedListener listenerObj)
  {
    final DialogFragment fragObj =
                             createEditNumberDialogFragment(titleStr,initVal,maxLen,listenerObj);
    fragObj.show(activityObj.getFragmentManager(),"EditNumberDialogFragment");
    return fragObj;
  }

  /**
   * Creates a dialog to edit a numeric value.
   * @param titleStr title string for dialog, or null for none.
   * @param initVal initial value for editor in dialog.
   * @param maxLen maximum length (number of digits) for editor in dialog,
   * or 0 for no limit.
   * @param listenerObj listener to be invoked after a value is entered.
   * @return A new DialogFragment object.
   */
  public static DialogFragment createEditNumberDialogFragment(String titleStr,
                                int initVal, int maxLen, DialogItemSelectedListener listenerObj)
  {
    final EditNumberDialogFragment fragObj = new EditNumberDialogFragment();
    fragObj.setInitialData(titleStr,initVal,maxLen,listenerObj);
    return fragObj;
  }

  /**
   * Interface for listener invoked by single-choice dialog.
   */
  public interface DialogItemSelectedListener
  {
    /**
     * Method invoked when an item is selected.
     * @param val value or index of selected item.
     */
    public void itemSelected(int val);
  }

  /**
   * Class SingleChoiceDialogFragment defines a single-choice dialog containing
   * an array of items.  Selecting an item will dismiss the dialog and invoke
   * the listener.
   */
  public static class SingleChoiceDialogFragment extends DialogFragment
  {
    private String dialogTitleString = null;
    private CharSequence [] dialogItemsArray = null;
    private int selectedItemIndex = 0;
    private String dialogCloseButtonString = null;
    private DialogItemSelectedListener dialogItemSelectedListenerObj = null;

    /**
     * Sets initial data items for dialog.  This method should be invoked
     * before the dialog is displayed.
     * @param titleStr title string for dialog, or null for none.
     * @param charSeqArr array of items to be displayed.
     * @param initIdx index of initial item to be selected, or -1 for none.
     * @param closeButtonStr text for 'close' button, or null for none.
     * @param listenerObj listener to be invoked when an item is selected.
     */
    public void setInitialData(String titleStr, CharSequence [] charSeqArr,
                                         int initIdx, String closeButtonStr,
                                     DialogItemSelectedListener listenerObj)
    {
      dialogTitleString = titleStr;
      dialogItemsArray = charSeqArr;
      selectedItemIndex = initIdx;
      dialogCloseButtonString = closeButtonStr;
      dialogItemSelectedListenerObj = listenerObj;
    }

    /**
     * Builds the dialog container.
     * @param savedInstanceState Bundle: The last saved instance state of
     * the Fragment, or null if this is a freshly created Fragment.
     * @return A new dialog object to be displayed by the fragment.
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
      AlertDialog.Builder builderObj = new AlertDialog.Builder(getActivity());
      if(dialogTitleString != null)
        builderObj.setTitle(dialogTitleString);
      builderObj.setSingleChoiceItems(dialogItemsArray, selectedItemIndex,
          new DialogInterface.OnClickListener()
              {         //invoked when item is selected
                @Override
                public void onClick(DialogInterface dialogObj, int idx)
                {
                  dismiss();
                  if(dialogItemSelectedListenerObj != null)
                    dialogItemSelectedListenerObj.itemSelected(idx);
                }
              });
      if(dialogCloseButtonString != null)
        builderObj.setNegativeButton(dialogCloseButtonString,null);
      return builderObj.create();      //create and return AlertDialog object
    }
  }


  /**
   * Class EditNumberDialogFragment defines a dialog to edit a numeric value.
   */
  public static class EditNumberDialogFragment extends DialogFragment
  {
    private String dialogTitleString = null;
    private int initialNumberValue = 0;
    private int maximumLengthValue = 0;
    private DialogItemSelectedListener dialogItemSelectedListenerObj = null;

    /**
     * Sets initial data values for dialog.  This method should be invoked
     * before the dialog is displayed.
     * @param titleStr title string for dialog, or null for none.
     * @param initVal initial value for editor in dialog.
     * @param maxLen maximum length (number of digits) for editor in dialog,
     * or 0 for no limit.
     * @param listenerObj listener to be invoked after.
     */
    public void setInitialData(String titleStr, int initVal, int maxLen,
                                     DialogItemSelectedListener listenerObj)
    {
      dialogTitleString = titleStr;
      initialNumberValue = initVal;
      maximumLengthValue = maxLen;
      dialogItemSelectedListenerObj = listenerObj;
    }

    /**
     * Builds the dialog container.
     * @param savedInstanceState Bundle: The last saved instance state of
     * the Fragment, or null if this is a freshly created Fragment.
     * @return A new dialog object to be displayed by the fragment.
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
      AlertDialog.Builder builderObj = new AlertDialog.Builder(getActivity());
      if(dialogTitleString != null)
        builderObj.setTitle(dialogTitleString);
      final EditText editTextObj = new EditText(getActivity());
      editTextObj.setInputType(InputType.TYPE_CLASS_NUMBER);         //set numeric input
      editTextObj.setText(Integer.toString(initialNumberValue));     //set initial value
      if(maximumLengthValue > 0)
      {  //positive maximum-length value was given; configure it
        editTextObj.setFilters(
                         new InputFilter[] { new InputFilter.LengthFilter(maximumLengthValue) });
      }
      editTextObj.setSelectAllOnFocus(true);
      editTextObj.setOnEditorActionListener(
          new TextView.OnEditorActionListener()
            {           //listener for EditText actions
              @Override
              public boolean onEditorAction(TextView vObj, int actionId, KeyEvent evtObj)
              {
                if(actionId == EditorInfo.IME_ACTION_DONE)
                {  //action is via soft keyboard 'Done' button
                    if(dialogItemSelectedListenerObj != null)
                    {
                      try
                      {
                        dismiss();
                        dialogItemSelectedListenerObj.itemSelected(
                                        Integer.parseInt(String.valueOf(editTextObj.getText())));
                        return true;
                      }
                      catch(NumberFormatException ex)
                      {  //error parsing value; just return false
                      }
                    }
                }
                return false;
              }
            });
      builderObj.setView(editTextObj);
      final AlertDialog dialogObj = builderObj.create();
      try
      {                 //always show soft keyboard:
        dialogObj.getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
      }
      catch(Exception ex)
      {  //some kind of exception error; ignore and more on
      }
      return dialogObj;
    }
  }
}
