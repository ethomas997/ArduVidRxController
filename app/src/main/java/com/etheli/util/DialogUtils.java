//DialogUtils.java:  Dialog-helper classes and utilities.
//
//  5/6/2017 -- [ET]
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
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
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
   * Displays a simple single-choice dialog containing the given array of items.
   * Selecting an item will close the dialog and invoke the listener.
   * @param activityObj parent activity for dialog.
   * @param titleId resource ID of title for dialog, or 0 for none.
   * @param charSeqArr array of items to be displayed.
   * @param initIdx index of initial item to be selected, or -1 for none.
   * @param closeButtonId resource ID of text for 'close' button, or 0 for none.
   * @param listenerObj listener to be invoked when an item is selected, or null for none.
   * @return A new DialogFragment object.
   */
  public static DialogFragment showSingleChoiceDialogFragment(Activity activityObj, int titleId,
                                      CharSequence [] charSeqArr, int initIdx, int closeButtonId,
                                                          DialogItemSelectedListener listenerObj)
  {
    return showSingleChoiceDialogFragment(activityObj,
                          ((titleId != 0) ? activityObj.getString(titleId) : null),
                                                               charSeqArr, initIdx,
              ((closeButtonId != 0) ? activityObj.getString(closeButtonId) : null),
                                                                      listenerObj);
  }

  /**
   * Displays a simple single-choice dialog containing the given array of items.
   * Selecting an item will close the dialog and invoke the listener.
   * @param activityObj parent activity for dialog.
   * @param titleStr title string for dialog, or null for none.
   * @param charSeqArr array of items to be displayed.
   * @param initIdx index of initial item to be selected, or -1 for none.
   * @param closeButtonStr text for 'close' button, or null for none.
   * @param listenerObj listener to be invoked when an item is selected, or null for none.
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
   * Displays a single-choice dialog containing the given array of items.
   * @param activityObj parent activity for dialog.
   * @param titleId resource ID of title for dialog, or 0 for none.
   * @param charSeqArr array of items to be displayed.
   * @param initIdx index of initial item to be selected, or -1 for none.
   * @param negativeButtonId resource ID of text for "negative" button, or 0 for none.
   * @param positiveButtonId resource ID of text for "positive" button, or 0 for none.
   * @param neutralButtonId resource ID of text for "neutral" button, or 0 for none.
   * @param itemSelListenerObj listener invoked when an item is selected, or null for none.
   * @param dismissOnSelectedFlag true to close dialog after item selected;
   * false to invoke listener but not close dialog.
   * @param dismissListenerObj listener invoked when the dialog is closed, or null for none.
   * @return A new DialogFragment object.
   */
  public static DialogFragment showSingleChoiceDialogFragment(Activity activityObj,
                                           int titleId, CharSequence [] charSeqArr,
                                                 int initIdx, int negativeButtonId,
                                         int positiveButtonId, int neutralButtonId,
                                     DialogItemSelectedListener itemSelListenerObj,
                                                     boolean dismissOnSelectedFlag,
                                DialogInterface.OnClickListener dismissListenerObj)
  {
    return showSingleChoiceDialogFragment(activityObj,
                          ((titleId != 0) ? activityObj.getString(titleId) : null),
                                                               charSeqArr, initIdx,
        ((negativeButtonId != 0) ? activityObj.getString(negativeButtonId) : null),
        ((positiveButtonId != 0) ? activityObj.getString(positiveButtonId) : null),
          ((neutralButtonId != 0) ? activityObj.getString(neutralButtonId) : null),
                    itemSelListenerObj, dismissOnSelectedFlag, dismissListenerObj);
  }

  /**
   * Displays a single-choice dialog containing the given array of items.
   * @param activityObj parent activity for dialog.
   * @param titleStr title string for dialog, or null for none.
   * @param charSeqArr array of items to be displayed.
   * @param initIdx index of initial item to be selected, or -1 for none.
   * @param negativeButtonStr text for "negative" button, or null for none.
   * @param positiveButtonStr text for "positive" button, or null for none.
   * @param neutralButtonStr text for "neutral" button, or null for none.
   * @param itemSelListenerObj listener invoked when an item is selected, or null for none.
   * @param dismissOnSelectedFlag true to close dialog after item selected;
   * false to invoke listener but not close dialog.
   * @param dismissListenerObj listener invoked when the dialog is closed, or null for none.
   * @return A new DialogFragment object.
   */
  public static DialogFragment showSingleChoiceDialogFragment(Activity activityObj,
                                       String titleStr, CharSequence [] charSeqArr,
                                             int initIdx, String negativeButtonStr,
                                 String positiveButtonStr, String neutralButtonStr,
                                     DialogItemSelectedListener itemSelListenerObj,
                                                     boolean dismissOnSelectedFlag,
                                DialogInterface.OnClickListener dismissListenerObj)
  {
    final DialogFragment fragObj = createSingleChoiceDialogFragment(
                                     titleStr,charSeqArr,initIdx,negativeButtonStr,
                             positiveButtonStr,neutralButtonStr,itemSelListenerObj,
                                         dismissOnSelectedFlag,dismissListenerObj);
    fragObj.show(activityObj.getFragmentManager(),"SingleChoiceDialogFragment");
    return fragObj;
  }

  /**
   * Creates a simple single-choice dialog containing the given array of items.
   * Selecting an item will close the dialog and invoke the listener.
   * @param titleStr title string for dialog, or null for none.
   * @param charSeqArr array of items to be displayed.
   * @param initIdx index of initial item to be selected, or -1 for none.
   * @param closeButtonStr text for 'close' button, or null for none.
   * @param listenerObj listener to be invoked when an item is selected, or null for none.
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
   * Creates a single-choice dialog containing the given array of items.
   * @param titleStr title string for dialog, or null for none.
   * @param charSeqArr array of items to be displayed.
   * @param initIdx index of initial item to be selected, or -1 for none.
   * @param negativeButtonStr text for "negative" button, or null for none.
   * @param positiveButtonStr text for "positive" button, or null for none.
   * @param neutralButtonStr text for "neutral" button, or null for none.
   * @param itemSelListenerObj listener invoked when an item is selected,
   * or null for none.
   * @param dismissOnSelectedFlag true to close dialog after item
   * selected; false to invoke listener but not close dialog.
   * @param dismissListenerObj listener invoked when the dialog is closed,
   * or null for none.
   * @return A new DialogFragment object.
   */
  public static DialogFragment createSingleChoiceDialogFragment(
                                                     String titleStr, CharSequence [] charSeqArr,
                                                           int initIdx, String negativeButtonStr,
                                               String positiveButtonStr, String neutralButtonStr,
                                                   DialogItemSelectedListener itemSelListenerObj,
                                                                   boolean dismissOnSelectedFlag,
                                              DialogInterface.OnClickListener dismissListenerObj)
  {
    final SingleChoiceDialogFragment fragObj = new SingleChoiceDialogFragment();
    fragObj.setInitialData(titleStr,charSeqArr,initIdx,negativeButtonStr,
                                           positiveButtonStr,neutralButtonStr,itemSelListenerObj,
                                                       dismissOnSelectedFlag,dismissListenerObj);
    return fragObj;
  }
  /**
   * Displays a simple multi-choice dialog containing the given array of items.
   * Selecting an item will close the dialog and invoke the listener.
   * @param activityObj parent activity for dialog.
   * @param titleId resource ID of title for dialog, or 0 for none.
   * @param charSeqArr array of items to be displayed.
   * @param selFlagsArr array indicating initial items to be selected.
   * @param closeButtonId resource ID of text for 'close' button, or 0 for none.
   * @param multiChoiceListenerObj listener to be invoked when an item is clicked on,
   * or null for none.
   * @return A new DialogFragment object.
   */
  public static DialogFragment showMultiChoiceDialogFragment(Activity activityObj, int titleId,
                           CharSequence [] charSeqArr, boolean [] selFlagsArr, int closeButtonId,
                               DialogInterface.OnMultiChoiceClickListener multiChoiceListenerObj)
  {
    return showMultiChoiceDialogFragment(activityObj,
                                        ((titleId != 0) ? activityObj.getString(titleId) : null),
                                                                         charSeqArr, selFlagsArr,
                            ((closeButtonId != 0) ? activityObj.getString(closeButtonId) : null),
                                                                         multiChoiceListenerObj);
  }

  /**
   * Displays a simple multi-choice dialog containing the given array of items.
   * Selecting an item will close the dialog and invoke the listener.
   * @param activityObj parent activity for dialog.
   * @param titleStr title string for dialog, or null for none.
   * @param charSeqArr array of items to be displayed.
   * @param selFlagsArr array indicating initial items to be selected.
   * @param closeButtonStr text for 'close' button, or null for none.
   * @param multiChoiceListenerObj listener to be invoked when an item is clicked on,
   * or null for none.
   * @return A new DialogFragment object.
   */
  public static DialogFragment showMultiChoiceDialogFragment(Activity activityObj, String titleStr,
                       CharSequence [] charSeqArr, boolean [] selFlagsArr, String closeButtonStr,
                               DialogInterface.OnMultiChoiceClickListener multiChoiceListenerObj)
  {
    final DialogFragment fragObj = createMultiChoiceDialogFragment(
                        titleStr,charSeqArr,selFlagsArr,closeButtonStr,multiChoiceListenerObj);
    fragObj.show(activityObj.getFragmentManager(),"MultiChoiceDialogFragment");
    return fragObj;
  }

  /**
   * Displays a multi-choice dialog containing the given array of items.
   * @param activityObj parent activity for dialog.
   * @param titleId resource ID of title for dialog, or 0 for none.
   * @param charSeqArr array of items to be displayed.
   * @param selFlagsArr array indicating initial items to be selected.
   * @param negativeButtonId resource ID of text for "negative" button, or 0 for none.
   * @param positiveButtonId resource ID of text for "positive" button, or 0 for none.
   * @param neutralButtonId resource ID of text for "neutral" button, or 0 for none.
   * @param clearButtonIdVal specifies which button will be the "clear" button
   * (Dialog.BUTTON_NEUTRAL, Dialog.BUTTON_NEGATIVE or Dialog.BUTTON_POSITIVE),
   * or 0 for none.
   * @param multiChoiceListenerObj listener invoked when an item is clicked on,
   * or null for none.
   * @param dismissListenerObj listener invoked when the dialog is closed,
   * or null for none.
   * @return A new DialogFragment object.
   */
  public static DialogFragment showMultiChoiceDialogFragment(Activity activityObj,
                                                         int titleId, CharSequence [] charSeqArr,
                                                    boolean [] selFlagsArr, int negativeButtonId,
                                 int positiveButtonId, int neutralButtonId, int clearButtonIdVal,
                               DialogInterface.OnMultiChoiceClickListener multiChoiceListenerObj,
                                              DialogInterface.OnClickListener dismissListenerObj)
  {
    return showMultiChoiceDialogFragment(activityObj,
               ((titleId != 0) ? activityObj.getString(titleId) : null), charSeqArr, selFlagsArr,
                      ((negativeButtonId != 0) ? activityObj.getString(negativeButtonId) : null),
                      ((positiveButtonId != 0) ? activityObj.getString(positiveButtonId) : null),
                        ((neutralButtonId != 0) ? activityObj.getString(neutralButtonId) : null),
                                  clearButtonIdVal,  multiChoiceListenerObj, dismissListenerObj);
  }

  /**
   * Displays a multi-choice dialog containing the given array of items.
   * @param activityObj parent activity for dialog.
   * @param titleStr title string for dialog, or null for none.
   * @param charSeqArr array of items to be displayed.
   * @param selFlagsArr array indicating initial items to be selected.
   * @param negativeButtonStr text for "negative" button, or null for none.
   * @param positiveButtonStr text for "positive" button, or null for none.
   * @param neutralButtonStr text for "neutral" button, or null for none.
   * @param clearButtonIdVal specifies which button will be the "clear" button
   * (Dialog.BUTTON_NEUTRAL, Dialog.BUTTON_NEGATIVE or Dialog.BUTTON_POSITIVE),
   * or 0 for none.
   * @param multiChoiceListenerObj listener invoked when an item is clicked on,
   * or null for none.
   * @param dismissListenerObj listener invoked when the dialog is closed,
   * or null for none.
   * @return A new DialogFragment object.
   */
  public static DialogFragment showMultiChoiceDialogFragment(Activity activityObj,
                                                     String titleStr, CharSequence [] charSeqArr,
                                                boolean [] selFlagsArr, String negativeButtonStr,
                         String positiveButtonStr, String neutralButtonStr, int clearButtonIdVal,
                               DialogInterface.OnMultiChoiceClickListener multiChoiceListenerObj,
                                              DialogInterface.OnClickListener dismissListenerObj)
  {
    final DialogFragment fragObj = createMultiChoiceDialogFragment(
                             titleStr,charSeqArr,selFlagsArr,negativeButtonStr,positiveButtonStr,
                    neutralButtonStr,clearButtonIdVal,multiChoiceListenerObj,dismissListenerObj);
    fragObj.show(activityObj.getFragmentManager(),"MultiChoiceDialogFragment");
    return fragObj;
  }

  /**
   * Creates a simple multi-choice dialog containing the given array of items.
   * Selecting an item will close the dialog and invoke the listener.
   * @param titleStr title string for dialog, or null for none.
   * @param charSeqArr array of items to be displayed.
   * @param selFlagsArr array indicating initial items to be selected.
   * @param closeButtonStr text for 'close' button, or null for none.
   * @param multiChoiceListenerObj listener to be invoked when an item is clicked on,
   * or null for none.
   * @return A new DialogFragment object.
   */
  public static DialogFragment createMultiChoiceDialogFragment(String titleStr,
                       CharSequence [] charSeqArr, boolean [] selFlagsArr, String closeButtonStr,
                               DialogInterface.OnMultiChoiceClickListener multiChoiceListenerObj)
  {
    final MultiChoiceDialogFragment fragObj = new MultiChoiceDialogFragment();
    fragObj.setInitialData(titleStr,charSeqArr,selFlagsArr,closeButtonStr,multiChoiceListenerObj);
    return fragObj;
  }

  /**
   * Creates a multi-choice dialog containing the given array of items.
   * @param titleStr title string for dialog, or null for none.
   * @param charSeqArr array of items to be displayed.
   * @param selFlagsArr array indicating initial items to be selected.
   * @param negativeButtonStr text for "negative" button, or null for none.
   * @param positiveButtonStr text for "positive" button, or null for none.
   * @param neutralButtonStr text for "neutral" button, or null for none.
   * @param clearButtonIdVal specifies which button will be the "clear" button
   * (Dialog.BUTTON_NEUTRAL, Dialog.BUTTON_NEGATIVE or Dialog.BUTTON_POSITIVE),
   * or 0 for none.
   * @param multiChoiceListenerObj listener invoked when an item is clicked on,
   * or null for none.
   * @param dismissListenerObj listener invoked when the dialog is closed,
   * or null for none.
   * @return A new DialogFragment object.
   */
  public static DialogFragment createMultiChoiceDialogFragment(
                                                     String titleStr, CharSequence [] charSeqArr,
                                                boolean [] selFlagsArr, String negativeButtonStr,
                         String positiveButtonStr, String neutralButtonStr, int clearButtonIdVal,
                               DialogInterface.OnMultiChoiceClickListener multiChoiceListenerObj,
                                              DialogInterface.OnClickListener dismissListenerObj)
  {
    final MultiChoiceDialogFragment fragObj = new MultiChoiceDialogFragment();
    fragObj.setInitialData(titleStr,charSeqArr,selFlagsArr,negativeButtonStr,
                                             positiveButtonStr,neutralButtonStr,clearButtonIdVal,
                                                      multiChoiceListenerObj,dismissListenerObj);
    return fragObj;
  }

  /**
   * Displays a dialog to edit a numeric value.
   * @param activityObj parent activity for dialog.
   * @param titleId resource ID of title for dialog.
   * @param initVal initial value for editor in dialog.
   * @param maxLen maximum length (number of digits) for editor in dialog,
   * or 0 for no limit.
   * @param listenerObj listener to be invoked after a value is entered, or null for none.
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
   * @param listenerObj listener to be invoked after a value is entered, or null for none.
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
   * @param listenerObj listener to be invoked after a value is entered, or null for none.
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
   * an array of items.
   */
  public static class SingleChoiceDialogFragment extends DialogFragment
  {
    private String dialogTitleString = null;
    private CharSequence [] dialogItemsArray = null;
    private int selectedItemIndex = 0;
    private String dialogNegativeButtonString = null;
    private String dialogPositiveButtonString = null;
    private String dialogNeutralButtonString = null;
    private DialogItemSelectedListener dialogItemSelectedListenerObj = null;
    private boolean dismissOnItemSelectedFlag = true;
    private DialogInterface.OnClickListener dialogOnDismissClickListenerObj = null;
    private int buttonClickWhichValue = 0;

    /**
     * Sets initial data items for a simple select-item dialog.  Selecting
     * an item will close the dialog and invoke the listener.  This method
     * should be used before the dialog is displayed.
     * @param titleStr title string for dialog, or null for none.
     * @param charSeqArr array of items to be displayed.
     * @param initIdx index of initial item to be selected, or -1 for none.
     * @param closeButtonStr text for 'close' button, or null for none.
     * @param itemSelListenerObj listener to be invoked when an item is selected,
     * or null for none.
     */
    public void setInitialData(String titleStr, CharSequence [] charSeqArr,
                                         int initIdx, String closeButtonStr,
                              DialogItemSelectedListener itemSelListenerObj)
    {
      dialogTitleString = titleStr;
      dialogItemsArray = charSeqArr;
      selectedItemIndex = initIdx;
      dialogNegativeButtonString = closeButtonStr;
      dialogItemSelectedListenerObj = itemSelListenerObj;
    }

    /**
     * Sets initial data items for a single-choice dialog.  This method
     * should be used before the dialog is displayed.
     * @param titleStr title string for dialog, or null for none.
     * @param charSeqArr array of items to be displayed.
     * @param initIdx index of initial item to be selected, or -1 for none.
     * @param negativeButtonStr text for "negative" button, or null for none.
     * @param positiveButtonStr text for "positive" button, or null for none.
     * @param neutralButtonStr text for "neutral" button, or null for none.
     * @param itemSelListenerObj listener invoked when an item is selected,
     * or null for none.
     * @param dismissOnSelectedFlag true to close dialog after item
     * selected; false to invoke listener but not close dialog.
     * @param dismissListenerObj listener invoked when the dialog is closed,
     * or null for none.
     */
    public void setInitialData(String titleStr, CharSequence [] charSeqArr,
                                      int initIdx, String negativeButtonStr,
                          String positiveButtonStr, String neutralButtonStr,
                              DialogItemSelectedListener itemSelListenerObj,
                                              boolean dismissOnSelectedFlag,
                         DialogInterface.OnClickListener dismissListenerObj)
    {
      dialogTitleString = titleStr;
      dialogItemsArray = charSeqArr;
      selectedItemIndex = initIdx;
      dialogNegativeButtonString = negativeButtonStr;
      dialogPositiveButtonString = positiveButtonStr;
      dialogNeutralButtonString = neutralButtonStr;
      dialogItemSelectedListenerObj = itemSelListenerObj;
      dismissOnItemSelectedFlag = dismissOnSelectedFlag;
      dialogOnDismissClickListenerObj = dismissListenerObj;
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
                  if(dismissOnItemSelectedFlag)
                    dismiss();
                  if(dialogItemSelectedListenerObj != null)
                    dialogItemSelectedListenerObj.itemSelected(idx);
                }
              });
              //create button-click listener to be invoked when button clicked or dialog canceled:
      final DialogInterface.OnClickListener bClickListenerObj;
      if(dialogOnDismissClickListenerObj != null)
      {  //dialog-dismissed listener was specified
        bClickListenerObj = new DialogInterface.OnClickListener()
            {           //make button-click listener that saves button-ID value
              @Override
              public void onClick(DialogInterface dialogObj, int which)
              {
                buttonClickWhichValue = which;
              }
            };
      }
      else
        bClickListenerObj = null;
              //setup buttons and listeners (for those that are specified):
      if(dialogNegativeButtonString != null)
        builderObj.setNegativeButton(dialogNegativeButtonString,bClickListenerObj);
      if(dialogPositiveButtonString != null)
        builderObj.setPositiveButton(dialogPositiveButtonString,bClickListenerObj);
      if(dialogNeutralButtonString != null)
        builderObj.setNeutralButton(dialogNeutralButtonString,bClickListenerObj);

      return builderObj.create();
    }

    /**
     * Called when the dialog is dismissed and the fragment is disposed.
     * Overridden to invoke the dialog-dismissed listener (if setup).
     */
    @Override
    public void onDestroy()
    {
      super.onDestroy();
      if(dialogOnDismissClickListenerObj != null)
      {  //dialog-dismissed listener was specified
        try
        {               //invoke listener with button-ID value:
          dialogOnDismissClickListenerObj.onClick(getDialog(),buttonClickWhichValue);
        }
        catch(Exception ex)
        {
          ex.printStackTrace();
        }
      }
    }
  }


  /**
   * Class MultiChoiceDialogFragment defines a multi-choice dialog containing
   * an array of items.  The dialog list is scrolled to the first selected item
   * when the dialog is shown.
   */
  public static class MultiChoiceDialogFragment extends DialogFragment
  {
    private String dialogTitleString = null;
    private CharSequence [] dialogItemsArray = null;
    private boolean [] dialogSelFlagsArray = null;
    private String dialogNegativeButtonString = null;
    private String dialogPositiveButtonString = null;
    private String dialogNeutralButtonString = null;
    private int dialogClearButtonIdVal = 0;
    private DialogInterface.OnMultiChoiceClickListener dialogMultiChoiceClickListenerObj = null;
    private DialogInterface.OnClickListener dialogOnDismissClickListenerObj = null;
    private int buttonClickWhichValue = 0;

    /**
     * Sets initial data items for a simple multi-select-item dialog.
     * This method should be used before the dialog is displayed.
     * @param titleStr title string for dialog, or null for none.
     * @param charSeqArr array of items to be displayed.
     * @param selFlagsArr array indicating initial items to be selected.
     * @param closeButtonStr text for 'close' button, or null for none.
     * @param multiChoiceListenerObj listener to be invoked when an item is clicked on,
     * or null for none.
     */
    public void setInitialData(String titleStr, CharSequence [] charSeqArr,
                                                   boolean [] selFlagsArr, String closeButtonStr,
                               DialogInterface.OnMultiChoiceClickListener multiChoiceListenerObj)
    {
      dialogTitleString = titleStr;
      dialogItemsArray = charSeqArr;
      dialogSelFlagsArray = selFlagsArr;
      dialogNegativeButtonString = closeButtonStr;
      dialogMultiChoiceClickListenerObj = multiChoiceListenerObj;
    }

    /**
     * Sets initial data items for a single-choice dialog.  This method
     * should be used before the dialog is displayed.
     * @param titleStr title string for dialog, or null for none.
     * @param charSeqArr array of items to be displayed.
     * @param selFlagsArr array indicating initial items to be selected.
     * @param negativeButtonStr text for "negative" button, or null for none.
     * @param positiveButtonStr text for "positive" button, or null for none.
     * @param neutralButtonStr text for "neutral" button, or null for none.
     * @param clearButtonIdVal specifies which button will be the "clear" button
     * (Dialog.BUTTON_NEUTRAL, Dialog.BUTTON_NEGATIVE or Dialog.BUTTON_POSITIVE),
     * or 0 for none.
     * @param multiChoiceListenerObj listener invoked when an item is clicked on,
     * or null for none.
     * @param dismissListenerObj listener invoked when the dialog is closed,
     * or null for none.
     */
    public void setInitialData(String titleStr, CharSequence [] charSeqArr,
                                                boolean [] selFlagsArr, String negativeButtonStr,
                         String positiveButtonStr, String neutralButtonStr, int clearButtonIdVal,
                               DialogInterface.OnMultiChoiceClickListener multiChoiceListenerObj,
                                              DialogInterface.OnClickListener dismissListenerObj)
    {
      dialogTitleString = titleStr;
      dialogItemsArray = charSeqArr;
      dialogSelFlagsArray = selFlagsArr;
      dialogNegativeButtonString = negativeButtonStr;
      dialogPositiveButtonString = positiveButtonStr;
      dialogNeutralButtonString = neutralButtonStr;
      dialogClearButtonIdVal = clearButtonIdVal;
      dialogMultiChoiceClickListenerObj = multiChoiceListenerObj;
      dialogOnDismissClickListenerObj = dismissListenerObj;
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
      builderObj.setMultiChoiceItems(dialogItemsArray, dialogSelFlagsArray,
                                                dialogMultiChoiceClickListenerObj);
              //create button-click listener to be invoked when button clicked or dialog canceled:
      final DialogInterface.OnClickListener bClickListenerObj;
      if(dialogOnDismissClickListenerObj != null)
      {  //dialog-dismissed listener was specified
        bClickListenerObj = new DialogInterface.OnClickListener()
            {           //make button-click listener that saves button-ID value
              @Override
              public void onClick(DialogInterface dialogObj, int which)
              {
                buttonClickWhichValue = which;
              }
            };
      }
      else
        bClickListenerObj = null;
              //setup buttons and listeners (for those that are specified):
      if(dialogNegativeButtonString != null)
        builderObj.setNegativeButton(dialogNegativeButtonString,bClickListenerObj);
      if(dialogPositiveButtonString != null)
        builderObj.setPositiveButton(dialogPositiveButtonString,bClickListenerObj);
      if(dialogNeutralButtonString != null)
        builderObj.setNeutralButton(dialogNeutralButtonString,bClickListenerObj);

      return setupScrollToFirstAndClearButton(builderObj.create());
    }

    /**
     * Sets up action to scroll the given dialog to the first selected entry
     * and action for "clear" button.
     * @param dialogObj given dialog object.
     * @return given dialog object.
     */
    private AlertDialog setupScrollToFirstAndClearButton(final AlertDialog dialogObj)
    {
      if(dialogObj != null)
      {  //given dialog object OK
        dialogObj.setOnShowListener(new DialogInterface.OnShowListener()
            {           //perform actions right after dialog shown
              @Override
              public void onShow(DialogInterface dialogInterface)
              {
                try
                {
                  final Button buttonObj;
                  if(dialogClearButtonIdVal != 0 &&
                                 (buttonObj=dialogObj.getButton(dialogClearButtonIdVal)) != null)
                  {  //"clear" button specified and found OK
                    buttonObj.setOnClickListener(new View.OnClickListener()
                      {      //set button action (and don't close dialog) on click
                          @Override
                          public void onClick(View v)
                          {
                            try
                            {
                              deselectAllItems();          //clear all selections
                            }
                            catch(Exception ex)
                            {
                              ex.printStackTrace();
                            }
                          }
                        });
                  }

                  final int idx;  //if selected entry found (past first one) then scroll to it
                  if((idx=getFirstSelectedIndex()) > 0 && dialogObj.getListView() != null)
                  {
                    dialogObj.getListView().smoothScrollToPositionFromTop(idx,
                                               GuiUtils.getDisplayHeightValue(getActivity())/20);
                  }

                  dialogObj.setOnShowListener(null);  //clean listener (only need to do once)
                }
                catch(Exception ex)
                {     //ignore any exceptions (non-critical task)
                }
              }
            });
      }
      return dialogObj;
    }

    /**
     * Returns the index of the first selected item.
     * @return The index of the first selected item, or 0 if none selected.
     */
    public int getFirstSelectedIndex()
    {
      int idx = 0;
      if(dialogSelFlagsArray != null)
      {  //boolean array is available
        while(true)
        {  //for each entry
          if(idx >= dialogSelFlagsArray.length)
          {  //reached end of array
            idx = 0;         //return 0 for no selection
            break;
          }
          if(dialogSelFlagsArray[idx])      //if selected entry found
            break;                          // then return index
          ++idx;
        }
      }
      return idx;
    }

    /**
     * Deselects all items in the dialog.
     */
    public void deselectAllItems()
    {
      final ListView listViewObj;
      if(getDialog() instanceof AlertDialog &&
                                  (listViewObj=((AlertDialog)getDialog()).getListView()) != null)
      {  //ListView object for dialog OK
        listViewObj.clearChoices();     //clear checkboxes on dialog
        clearDialogSelFlagsArray();     //clear array of select flags
                                        //indicate list data changed:
        if(listViewObj.getAdapter() instanceof BaseAdapter)
          ((BaseAdapter)listViewObj.getAdapter()).notifyDataSetChanged();
        listViewObj.invalidate();       //also request view update
      }
    }

    /**
     * Clears the array of flags indicating which items are selected.
     */
    private void clearDialogSelFlagsArray()
    {
      if(dialogSelFlagsArray != null)
      {  //boolean array is available
        for(int i=0; i<dialogSelFlagsArray.length; ++i)
          dialogSelFlagsArray[i] = false;
      }
    }

    /**
     * Called when the dialog is dismissed and the fragment is disposed.
     * Overridden to invoke the dialog-dismissed listener (if setup).
     */
    @Override
    public void onDestroy()
    {
      super.onDestroy();
      if(dialogOnDismissClickListenerObj != null)
      {  //dialog-dismissed listener was specified
        try
        {               //invoke listener with button-ID value:
          dialogOnDismissClickListenerObj.onClick(getDialog(),buttonClickWhichValue);
        }
        catch(Exception ex)
        {
          ex.printStackTrace();
        }
      }
    }
  }


  /**
   * Class MultiChoiceClickUpdater defines a listener that can be attached
   * to a multi-choice dialog, which will update an array of booleans in
   * response to select/deselect clicks on the dialog.
   */
  public static class MultiChoiceClickUpdater implements DialogInterface.OnMultiChoiceClickListener
  {
    private boolean [] updSelectFlagsArray;

    /**
     * Creates an updater.
     * @param flagsArr array of boolean to be updated.
     */
    public MultiChoiceClickUpdater(boolean [] flagsArr)
    {
      if(flagsArr == null)
        throw new NullPointerException();
      updSelectFlagsArray = flagsArr;
    }

    /**
     * Method invoked via select/deselect clicks on the dialog.
     * @param dialogObj source dialog object.
     * @param idx position of item that was clicked.
     * @param flagVal true if the click checked the item, else false.
     */
    @Override
    public void onClick(DialogInterface dialogObj, int idx, boolean flagVal)
    {
      if(idx >= 0 && idx < updSelectFlagsArray.length)
        updSelectFlagsArray[idx] = flagVal;
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
