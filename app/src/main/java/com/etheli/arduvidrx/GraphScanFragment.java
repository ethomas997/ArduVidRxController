//GraphScanFragment.java:  Fragment for graph showing scan data.
//
//  4/17/2017 -- [ET]
//

package com.etheli.arduvidrx;

import android.app.Fragment;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import com.etheli.util.DialogUtils;
import com.etheli.util.GuiUtils;
import com.etheli.util.PausableThread;
import com.github.mikephil.charting.buffer.BarBuffer;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.renderer.BarChartRenderer;
import com.github.mikephil.charting.utils.Utils;
import com.github.mikephil.charting.utils.ViewPortHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

/**
 * Class GraphScanFragment defines a fragment for graph showing scan data.
 */
public class GraphScanFragment extends Fragment
{
  private static final int MIN_PEAKLABELS_RSSI = 5;        //min RSSI for peak labels on graph
  private static final int MAX_ENTRIESLIST_SIZE = 200;     //max size for 'graphScanEntriesList'
  private static final float LABELS_TEXT_SIZE = 14.0f;     //size for graph text labels
  private View graphScanFragmentViewObj = null;
  private BarChart graphScanBarChartObj = null;
  private List<BarEntry> graphScanEntriesList = null;
  private int graphScanEntriesIndex = 0;
  private int graphScanLastHighlightFreqVal = 0;
  private int graphScanLastFoundPeakFreqVal = 0;
  private int graphScanMinPeakLabelsSpacing = 10;
  private TextView graphScanStatusTextViewObj = null;
  private Button graphScanValuesButtonObj = null;
  private Button graphScanPauseButtonObj = null;
  private boolean graphScanPausedFlag = false;
  private final ReceiverScanDataThread receiverScanDataThreadObj = new ReceiverScanDataThread();
  private VidReceiverManager vidReceiverManagerObj = null;
    //message codes for 'widgetUpdateHandlerObj':
  private static final int UPD_HIGHLIGHT_MSG = 1;     //update highlighted bar on graph
  private static final int UPD_STATUSTEXT_MSG = 2;    //update status text view
    /** Tag string for logging. */
  public static final String LOG_TAG = "GraphScanFragment";

  /**
   * Creates the view for the main operations screen.
   * @param inflater LayoutInflater: The LayoutInflater object that can be used to inflate
   * any views in the fragment.
   * @param container ViewGroup: If non-null, this is the parent view that the fragment's UI
   * should be attached to.
   * @param savedInstanceState Bundle: If non-null, this fragment is being re-constructed
   * from a previous saved state as given here.
   * @return Return the View for the fragment's UI, or null.
   */
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState)
  {
    if(graphScanFragmentViewObj == null)
    {  //view object not created by previous iteration; inflate fragment view from XML
      graphScanFragmentViewObj = inflater.inflate(R.layout.graphscan_fragment,container,false);
    }
    return graphScanFragmentViewObj;
  }

  /**
   * Called when the fragment is started.
   */
  @Override
  public void onStart()
  {
    super.onStart();
    if(graphScanBarChartObj == null)
    {  //this is the first time through
      graphScanBarChartObj = (BarChart)getActivity().findViewById(R.id.graphScanChart);
      setGraphScanChartHeight();                 //adjust chart height for good fit
      addPeakLabelsSpacingLayoutListener();      //setup listener for peak-labels spacing
      adjustMinPeakLabelsSpacing();              //do initial call to listener function
      graphScanBarChartObj.setDrawGridBackground(false);
      graphScanBarChartObj.setDrawBarShadow(false);
      graphScanBarChartObj.getLegend().setEnabled(false);
      graphScanBarChartObj.getAxisRight().setEnabled(false);
      graphScanBarChartObj.getDescription().setEnabled(false);
      graphScanBarChartObj.setPinchZoom(true);
         //set custom renderer that can show X values above bars:
      graphScanBarChartObj.setRenderer(new GraphScanChartRenderer());

//      final Typeface tf = Typeface.defaultFromStyle(Typeface.NORMAL);
      final YAxis leftAxis = graphScanBarChartObj.getAxisLeft();
//      leftAxis.setTypeface(tf);
      leftAxis.setAxisMinimum(0f);
      leftAxis.setAxisMaximum(110f);
//      leftAxis.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
//      leftAxis.setLabelCount(8, false);
//      leftAxis.setLabelCount(8);
      leftAxis.setValueFormatter(new SimpleIntAxisValueFormatter());
//      leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
//      leftAxis.setSpaceTop(15f);
      leftAxis.setTextSize(LABELS_TEXT_SIZE);

      final XAxis xAxis = graphScanBarChartObj.getXAxis();
      xAxis.setEnabled(true);
      xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
      xAxis.setDrawGridLines(false);
      xAxis.setValueFormatter(new SimpleIntAxisValueFormatter());
      xAxis.setTextSize(LABELS_TEXT_SIZE);
      xAxis.setSpaceMin(20.0f);        //configure some margin space left/right for bars
      xAxis.setSpaceMax(20.0f);
      xAxis.setYOffset(0.0f);          //tweak so Y-axis labels aren't too low

              //get initial set of entries for frequency values:
      graphScanEntriesList = getInitialEntriesList();
              //setup listener to handle selection of bars on chart:
      graphScanBarChartObj.setOnChartValueSelectedListener(
          new OnChartValueSelectedListener()
            {
              @Override
              public void onValueSelected(Entry entryObj, Highlight hlObj)
              {
                if(graphScanPausedFlag && entryObj instanceof ScanItemBarEntry)
                {  //data-scan thread is paused and item-entry object OK
                  final ScanItemBarEntry itemObj = (ScanItemBarEntry)entryObj;
                  graphScanLastHighlightFreqVal = itemObj.getFreqVal();   //track highlight
                  showChannelInStatusTextView(itemObj,false);   //update status text view
                  tuneToScanItemChannel(itemObj,true);          //tune chan; update highlight
                }
              }
              @Override
              public void onNothingSelected()
              {              //if unselect then reselect so bar stays highlighted
                if(graphScanLastHighlightFreqVal > 0)
                  setHighlightBarAndUpdateGraph(graphScanLastHighlightFreqVal);
              }
            });

//      System.out.print("DEBUG: ");
//      for(BarEntry entryObj : graphScanEntriesList)
//        System.out.print(" " + Math.round(entryObj.getX()));
//      System.out.println();
//      System.out.println("DEBUG:  entriesListSize = " + graphScanEntriesList.size());

      setupBarChartEntriesListData();            //setup data objects for chart
      graphScanBarChartObj.invalidate();         //do initial render
      graphScanEntriesIndex = 0;                 //initialize update index
      graphScanPausedFlag = false;               //data-scan thread will be running

//      for(BarEntry entryObj : graphScanEntriesList)
//        entryObj.setY(((int)entryObj.getX())%100 / 2 + 10);
//      graphScanBarChartObj.invalidate();

      graphScanStatusTextViewObj = (TextView)getActivity().findViewById(R.id.statusGraphTextView);
              //if not large screen then reduce margins to leave more room for chart:
      if(GuiUtils.isSmallOrNormalScreenSize(getActivity()))
        GuiUtils.adjustItemLayoutMargins(graphScanStatusTextViewObj,0.1f,0.1f);

      if((graphScanValuesButtonObj=
                             (Button)getActivity().findViewById(R.id.valuesGraphButton)) != null)
      {  //button found OK; setup click action
        graphScanValuesButtonObj.setOnClickListener(
            new View.OnClickListener()
              {
                @Override
                public void onClick(View vObj)
                {
                  doValuesButtonAction();
                }
              });
      }
      if((graphScanPauseButtonObj=
                              (Button)getActivity().findViewById(R.id.pauseGraphButton)) != null)
      {  //button found OK; setup click action
        graphScanPauseButtonObj.setOnClickListener(
            new View.OnClickListener()
              {
                @Override
                public void onClick(View vObj)
                {
                  doPauseButtonAction();
                }
              });
      }
      final Button buttonObj;
      if((buttonObj=(Button)getActivity().findViewById(R.id.closeGraphButton)) != null)
      {  //button found OK; setup click action
        buttonObj.setOnClickListener(
            new View.OnClickListener()
              {
                @Override
                public void onClick(View vObj)
                {
                  doCloseButtonAction();
                }
              });
      }
      receiverScanDataThreadObj.start();         //start data-scanning thread
    }
    else
    {  //this is not the first time through; resume data-scanning thread (if not paused via button)
      if((!graphScanPausedFlag) && receiverScanDataThreadObj.isThreadPaused())
        receiverScanDataThreadObj.resumeThread();
    }
  }

  /**
   * Called when the fragment is stopped.
   */
  @Override
  public void onStop()
  {
    if(receiverScanDataThreadObj.isAlive())
      receiverScanDataThreadObj.pauseThread(0);       //pause data-scanning thread
    super.onStop();
  }

  /**
   * Called when the fragment is no longer in use.
   */
  @Override
  public void onDestroy()
  {
    receiverScanDataThreadObj.terminate(500);    //stop data-scanning thread (wait for terminate)
    super.onDestroy();
  }

  /**
   * Sets the height of the graph-scan chart so as to fit the chart, status text view,
   * and the buttons all the screen when in landscape orientation.
   */
  private void setGraphScanChartHeight()
  {
    try
    {
      final ViewGroup.LayoutParams lParamsObj = graphScanBarChartObj.getLayoutParams();
              //adjust height-scaling factor to make chart larger when screen not large:
      final float factVal = (GuiUtils.isSmallOrNormalScreenSize(getActivity())) ? 10 : 15;
              //estimate the height of the status text view and the buttons
              // and subtract that value to get the height value for the chart:
      lParamsObj.height = GuiUtils.getShorterScreenSizeValue(getActivity()) -
                          Math.round(GuiUtils.getFontMetricsHeightViaUtilPaint() * factVal);
    }
    catch(Exception ex)
    {  //some kind of exception error; log it
      Log.e(LOG_TAG, "Exception in 'setGraphScanChartHeight()'", ex);
    }
  }

  /**
   * Sets up the data objects for rendering the scan-entries list values on the bar chart.
   */
  private void setupBarChartEntriesListData()
  {
         //setup data objects for bar chart:
    final BarDataSet barDataSetObj = new BarDataSet(graphScanEntriesList,"Scan Data");
    barDataSetObj.setColor(Color.BLACK,255);
    barDataSetObj.setDrawValues(true);
    barDataSetObj.setValueFormatter(new SimpleIntValueFormatter());
    barDataSetObj.setValueTextSize(LABELS_TEXT_SIZE);
         //setup color for highlighted bar:
    barDataSetObj.setHighLightColor(Color.GREEN);
    barDataSetObj.setHighLightAlpha(255);
    barDataSetObj.setHighlightEnabled(true);
         //craate data set and enter into chart:
    final BarData barDataObj = new BarData(barDataSetObj);
    barDataObj.setBarWidth(10.0f);
    graphScanBarChartObj.setData(barDataObj);
  }

  /**
   * Adds a layout listener to the bar chart to adjust the spacing between
   * peak labels using the display width.  The value will change as the
   * screen orientation changes between portrait and landscape.
   */
  private void addPeakLabelsSpacingLayoutListener()
  {
      graphScanBarChartObj.addOnLayoutChangeListener(new View.OnLayoutChangeListener()
          {
            @Override
            public void onLayoutChange(View view, int i, int i1, int i2, int i3,
                                                 int i4, int i5, int i6, int i7)
            {
              adjustMinPeakLabelsSpacing();
            }
          });
  }

  /**
   * Adjusts the spacing between peak labels using the display width.
   */
  private void adjustMinPeakLabelsSpacing()
  {
    try
    {
      final int dispWidth;
      if((dispWidth=GuiUtils.getDisplayWidthValue(getActivity())) > 0)
      {  //display-width value fetched OK
        final int newSpacingVal;
        if((newSpacingVal=5400/dispWidth) != graphScanMinPeakLabelsSpacing)
        {  //spacing value is changing
          graphScanMinPeakLabelsSpacing = newSpacingVal;
          if(graphScanPausedFlag)           //if data-scan thread is paused then
            doMarkPeaksAndUpdateGraph();    //find new peaks and update graph
        }
      }
    }
    catch(Exception ex)
    {
    }
  }

  /**
   * Returns an initial list of entries for the graph.
   * @return A new list of 'ScanItemBarEntry' objects.
   */
  private List<BarEntry> getInitialEntriesList()
  {
    final ArrayList<BarEntry> entriesList = new ArrayList<BarEntry>();
    try
    {              //get full list of frequency-channel items:
      final FrequencyTable freqTableObj =
                                ProgramResources.getProgramResourcesObj().getFrequencyTableObj();
      final FrequencyTable.FreqChannelItem[] itemsArr = freqTableObj.getFreqChannelItemsArray();

                   //create sorted set of frequency values:
      final TreeSet<Integer> valsSet = new TreeSet<Integer>();
      for(FrequencyTable.FreqChannelItem itemObj : itemsArr)
        valsSet.add((int)itemObj.frequencyVal);
                   //add values to beginning and end of set:
      valsSet.add(valsSet.first()-37);
      valsSet.add(valsSet.last()+37);
                   //convert set to array:
      final Integer[] valsArr = valsSet.toArray(new Integer[valsSet.size()]);
         //enter frequency values into list of 'ScanItemBarEntry' objects
         // filling in gaps between frequencies as needed:
      int val, diff, i = 0;
      while(true)
      {  //for each frequency value in array
        val = valsArr[i];
        entriesList.add(new ScanItemBarEntry(
                                       val,0,freqTableObj.getChannelCodeForFreqVal((short)val)));
        if(++i >= valsArr.length)
          break;
        if((diff=valsArr[i] - val) > 35)
        {  //large gap (fill in three values)
          diff /= 4;
          val += diff;
          entriesList.add(new ScanItemBarEntry(val,0,null));
          val += diff+1;
          entriesList.add(new ScanItemBarEntry(val,0,null));
          val += diff;
          entriesList.add(new ScanItemBarEntry(val,0,null));
        }
        else if(diff > 22)
        {  //medium gap (fill in two values)
          diff /= 3;
          val += diff;
          entriesList.add(new ScanItemBarEntry(val,0,null));
          val += diff;
          entriesList.add(new ScanItemBarEntry(val,0,null));
        }
        else if(diff > 9)
        {  //small gap (fill in one value)
          val += diff / 2;
          entriesList.add(new ScanItemBarEntry(val,0,null));
        }
      }
    }
    catch(Exception ex)
    {  //some kind of exception error; just return (possibly-empty) list
    }
    return entriesList;
  }

  /**
   * Displays a dialog containing the scan items (with frequency and
   * RSSI values).
   * @param initIdx index of initial item to be selected in dialog.
   * @param scanThreadWasRunningFlag true if data-scan thread was
   * running (and should be resumed if dialog canceled).
   */
  public void showScanValuesDialog(int initIdx, final boolean scanThreadWasRunningFlag)
  {
    try
    {    //create array of frequency-scan items for dialog:
      final ScanItemBarEntry [] scanItemEntriesArr =
                                               new ScanItemBarEntry[graphScanEntriesList.size()];
      graphScanEntriesList.toArray((BarEntry[])scanItemEntriesArr);
              //create listener to be invoked when an item is selected:
      final DialogUtils.DialogItemSelectedListener itemSelListenerObj =
          new DialogUtils.DialogItemSelectedListener()
            {        //invoked when item is selected
              @Override
              public void itemSelected(int selIdx)
              {           //translate array index to channel and tune to it:
                if(selIdx >= 0 && selIdx < scanItemEntriesArr.length)
                {
                  final ScanItemBarEntry itemObj = scanItemEntriesArr[selIdx];
                  tuneToScanItemChannel(itemObj,true);          //tune chan; update highlight
                  showChannelInStatusTextView(itemObj,false);   //update status text view
                }
              }
            };
              //create listener to be invoked when the dialog is closed:
      final DialogInterface.OnClickListener dismissListenerObj =
          new DialogInterface.OnClickListener()
            {
              @Override
              public void onClick(DialogInterface dialogObj, int which)
              {
                final boolean runningFlag;
                if(which == DialogInterface.BUTTON_NEGATIVE)         //"Pause" button
                  runningFlag = false;
                else if(which == DialogInterface.BUTTON_POSITIVE)    //"Resume" button
                  runningFlag = true;
                else                        //neither button pressed (dialog canceled)
                  runningFlag = scanThreadWasRunningFlag;      //restore previous state
                        //pause/resume data-scan thread (don't tune highest-RSSI channel):
                setGraphScanDataThreadState(runningFlag,false);
              }
            };
              //build and display the dialog:
      DialogUtils.showSingleChoiceDialogFragment(getActivity(), R.string.chansel_dialog_title,
                                    scanItemEntriesArr, initIdx, R.string.pausegraph_button_name,
                                                             R.string.resumegraph_button_name, 0,
                                                  itemSelListenerObj, false, dismissListenerObj);
    }
    catch(Exception ex)
    {  //some kind of exception error; log it
      Log.e(LOG_TAG, "Exception in 'showScanValuesDialog()'", ex);
    }
  }

  /**
   * Updates the next entry (frequency-value slot) on the graph.
   * @param freqVal frequency to be updated, or 0 to reset to first slot.
   * @param rssiVal RSSI value for entry.
   * @param chanCodeStr channel-code value for entry (i.e., "F4"), or null for none.
   */
  private void updateNextGraphEntry(int freqVal, int rssiVal, String chanCodeStr)
  {
    if(freqVal > 0)
    {  //valid frequency value
      final ScanItemBarEntry barEntryObj;
      if(graphScanEntriesIndex < graphScanEntriesList.size())
      {  //slot-position exists in list; update values in entry
        barEntryObj = (ScanItemBarEntry)graphScanEntriesList.get(graphScanEntriesIndex);
        barEntryObj.setFreqVal(freqVal);
        barEntryObj.setRssiVal(rssiVal);
        barEntryObj.setChanCodeStr(chanCodeStr);
        ++graphScanEntriesIndex;
      }       //slot-position does not exist in list; create new entry with values (if room)
      else if(graphScanEntriesList.size() < MAX_ENTRIESLIST_SIZE)
      {  //slot-position does not exist and not too many in list; create new entry with values
        barEntryObj = new ScanItemBarEntry(freqVal,rssiVal,chanCodeStr);
        graphScanEntriesList.add(barEntryObj);
        ++graphScanEntriesIndex;
        setupBarChartEntriesListData();          //redo setup to handle size change
      }
      else
      {  //too many entries
        freqVal = 0;
        barEntryObj = null;
      }
      markPeaksInScanEntriesList();              //find and mark peak entries
              //set highlighted bar and update displayed graph:
      setHighlightBarAndUpdateGraph(freqVal);
              //if just updated displayed peak entry then update status text view now:
      if(freqVal == graphScanLastFoundPeakFreqVal && freqVal > 0 && rssiVal >= MIN_PEAKLABELS_RSSI)
        showChannelInStatusTextView(barEntryObj,true);
    }
    else
    {  //given frequency value is zero
      graphScanEntriesIndex = 0;            //reset index to first slot
      setHighlightBarAndUpdateGraph(-1);    //clear highlight
                                            //update peak freq in status text view:
      graphScanLastFoundPeakFreqVal = showPeakChanInStatusTextView();
    }
  }

  /**
   * Scans 'graphScanEntriesList' and marks peak entries (by setting
   * 'showFreqAboveBarFlag' on entries).
   */
  private void markPeaksInScanEntriesList()
  {
    try
    {
      final int listSize = graphScanEntriesList.size();
      int prevMaxEntIdx = -1, curMaxEntIdx = -1;
      int j, yVal, lastYVal = 0, curMaxYVal = MIN_PEAKLABELS_RSSI;
      ScanItemBarEntry entryObj;
      for(int idx=0; idx<listSize; ++idx)
      {  //for each entry in list
        entryObj = (ScanItemBarEntry)graphScanEntriesList.get(idx);
        entryObj.setShowFreqAboveBarFlag(false);      //initialize entries while scanning
        yVal = entryObj.getRssiVal();
        if(yVal > lastYVal && yVal > curMaxYVal)
        {  //Y-value increasing and larger than currently-tracked maximum
          curMaxYVal = yVal;
          curMaxEntIdx = idx;
        }
        else if(curMaxEntIdx >= 0 && (prevMaxEntIdx < 0 ||
                                curMaxEntIdx - prevMaxEntIdx >= graphScanMinPeakLabelsSpacing) &&
                                             idx - curMaxEntIdx >= graphScanMinPeakLabelsSpacing)
        {  //current peak has enough space from previous peak (if any) and scan pos; mark peak
          j = curMaxEntIdx;       //check if 3+ bars at max level
          while(j < listSize-1 &&
                    ((ScanItemBarEntry)graphScanEntriesList.get(j+1)).getRssiVal() == curMaxYVal)
          {
            ++j;
          }
          if((j=j-curMaxEntIdx) >= 3)       //if 3+ bars at max level then
            curMaxEntIdx += j / 2;          //move index up to center bar
          ((ScanItemBarEntry)graphScanEntriesList.get(curMaxEntIdx)).setShowFreqAboveBarFlag(true);
          prevMaxEntIdx = curMaxEntIdx;          //save as new "previous" peak
          curMaxEntIdx = -1;                     //reset "current" peak index
          curMaxYVal = MIN_PEAKLABELS_RSSI;      //reset "current" peak Y-value
        }
        lastYVal = yVal;
      }
      if(curMaxEntIdx >= 0 &&
            (prevMaxEntIdx < 0 || curMaxEntIdx - prevMaxEntIdx >= graphScanMinPeakLabelsSpacing))
      {  //last "current" peak has enough space from previous peak (if any); mark peak
        ((ScanItemBarEntry)graphScanEntriesList.get(curMaxEntIdx)).setShowFreqAboveBarFlag(true);
      }
    }
    catch(Exception ex)
    {  //some kind of exception error; log it and move on
      Log.e(LOG_TAG, "Exception in 'markPeaksInScanEntriesList()'", ex);
    }
  }

  /**
   * Invokes 'markPeaksInScanEntriesList()' and updates the graph, using a
   * worker thread.
   */
  private void doMarkPeaksAndUpdateGraph()
  {
    (new Thread(new Runnable()
        {
          @Override
          public void run()
          {
            try
            {
              markPeaksInScanEntriesList();
              graphScanBarChartObj.postInvalidate();
            }
            catch(Exception ex)
            {  //some kind of exception error; log it
              Log.e(LOG_TAG, "Exception in 'doMarkPeaksAndUpdateGraph()' worker", ex);
            }
          }
        }, "doMarkPeaksAndUpdateGraph")).start();
  }

  /**
   * Performs 'Close' button action.
   */
  private void doCloseButtonAction()
  {                          //do same action as 'Back' button:
    getActivity().getFragmentManager().popBackStack();
  }

  /**
   * Performs 'Values' button action.
   */
  private void doValuesButtonAction()
  {
    graphScanValuesButtonObj.setEnabled(false);  //disable button while preparing/showing dialog
    (new Thread("valuesButtonAction")
        {          //use separate thread (because 'pauseThread()' can take time)
          public void run()
          {
            try
            {
              final boolean wasRunningFlag;
              if(!graphScanPausedFlag)
              {  //data-scanning thread is running; pause it
                wasRunningFlag = graphScanPausedFlag = true;
                receiverScanDataThreadObj.pauseThread(1000);
              }
              else  //data-scanning thread not running
                wasRunningFlag = false;
                        //if data-scanning thread was running then tune to and get index for
                        // highest-RSSI channel; otherwise get index for highlighted bar:
              final int initIdx = wasRunningFlag ? tuneToHighestRssiScanItem() :
                                                                   getIdxForLastHighlightedBar();
              if(getView() != null)
              {  //verified that view object is OK
                getView().post(new Runnable()
                    {        //launch dialog via UI thread
                      public void run()
                      {
                        try
                        {
                          showScanValuesDialog(initIdx,wasRunningFlag);
                          graphScanValuesButtonObj.setEnabled(true);
                        }
                        catch(Exception ex)
                        {  //some kind of exception error; log it
                          Log.e(LOG_TAG, "Exception in 'doValuesButtonAction()' post", ex);
                        }
                      }
                    });
              }
            }
            catch(Exception ex)
            {  //some kind of exception error; log it
              Log.e(LOG_TAG, "Exception in 'doValuesButtonAction()'", ex);
            }
          }
        }).start();
  }

  /**
   * Performs pause (or resume) button action for graph.
   */
  private void doPauseButtonAction()
  {
    setGraphScanDataThreadState(graphScanPausedFlag,true);      //toggle state
  }

  /**
   * Sets the state of the graph-data-scan thread.  The method should be
   * called via the main-UI thread.
   * @param runningFlag true for thread running; false for paused.
   * @param tuneHighestFlag if true and 'runningFlag'==true then tune to
   * highest-RSSI channel and select its bar on the graph.
   */
  private void setGraphScanDataThreadState(boolean runningFlag, final boolean tuneHighestFlag)
  {
    if(runningFlag)
    {  //set data-scan thread to running
      if(graphScanPausedFlag)
      {  //data-scan thread is currently paused
        graphScanPausedFlag = false;
        graphScanLastFoundPeakFreqVal = 0;       //reset tracking of peak frequency
        receiverScanDataThreadObj.resumeThread();
      }
      if(graphScanPauseButtonObj != null)   //set button name to "Pause"
        graphScanPauseButtonObj.setText(R.string.pausegraph_button_name);
    }
    else
    {  //set data-scan thread to paused
      if(!graphScanPausedFlag)
      {  //data-scan thread is currently running
        graphScanPausedFlag = true;
        (new Thread("pauseButtonAction")
            {           //use separate thread (because 'pauseThread()' can take time)
              public void run()
              {
                try
                {
                  receiverScanDataThreadObj.pauseThread(1000);
                  if(tuneHighestFlag)  //if flag then tune to highest-RSSI channel
                    tuneToHighestRssiScanItem();
                }
                catch(Exception ex)
                {  //some kind of exception error; log it
                  Log.e(LOG_TAG, "Exception in 'setGraphScanDataThreadState()' post", ex);
                }
              }
            }).start();
      }
      if(graphScanPauseButtonObj != null)   //set button name to "Resume"
        graphScanPauseButtonObj.setText(R.string.resumegraph_button_name);
    }
  }

  /**
   * Tunes the receiver the channel specified by the item from the
   * 'graphScanEntriesList' with the highest RSSI value and updates
   * the highlighted bar on the graph.  This method should only be
   * used while the graph-data-scan thread is paused.
   * @return The index of the ScanItemBarEntry object for the tuned
   * channel, or -1 if error.
   */
  private int tuneToHighestRssiScanItem()
  {
    final int idx = findHighestRssiScanItem();
    final ScanItemBarEntry itemObj;
    if((itemObj=entriesListIdxToScanItem(idx)) != null)
    {
      tuneToScanItemChannel(itemObj,true);            //tune channel and update highlight
      showChannelInStatusTextView(itemObj,true);      //update status text view
      return idx;
    }
    return -1;
  }

  /**
   * Displays information for the given channel in the status text view.
   * @param itemObj item entry object containing channel information.
   * @param peakFlag true if "peak" channel; false if "selected" channel.
   */
  private void showChannelInStatusTextView(ScanItemBarEntry itemObj, boolean peakFlag)
  {
    if(itemObj == null)
      return;
    final int rssiVal = itemObj.getRssiVal();
    final String preStr;
    if(peakFlag)
    {
      if(rssiVal < MIN_PEAKLABELS_RSSI)
      {  //peak and RSSI not high enough for label
        updateStatusTextView("");           //clear text view
        return;
      }
      preStr = "Peak";
    }
    else
      preStr = "Selected";
    final String chanStr = itemObj.getChanCodeStr();
    updateStatusTextView(preStr + ":   " + ((chanStr != null) ? chanStr : "") + "  " +
                                                 itemObj.getFreqVal() + " MHz  RSSI=" + rssiVal);
  }

  /**
   * Finds the item from the 'graphScanEntriesList' with the highest
   * RSSI value and displays it in the status text view.  This method
   * should only be used while the graph-data-scan thread is paused or
   * between updates.
   * @return The frequency value for the found/displayed item, or 0
   * if none found.
   */
  private int showPeakChanInStatusTextView()
  {
    final int idx = findHighestRssiScanItem();
    final ScanItemBarEntry itemObj;
    if((itemObj=entriesListIdxToScanItem(idx)) != null)
    {
      showChannelInStatusTextView(itemObj,true);
      return itemObj.getFreqVal();
    }
    return 0;
  }

  /**
   * Finds the item from the 'graphScanEntriesList' with the highest
   * RSSI value.  This method should only be used while the graph-data-scan
   * thread is paused or between updates.
   * @return The index of the found ScanItemBarEntry object, or -1 if error.
   */
  private int findHighestRssiScanItem()
  {
    try
    {
      final int listSize;
      if((listSize=graphScanEntriesList.size()) > 0)
      {  //list not empty
        int maxItemIdx = 0;
        int maxItemRssiVal = ((ScanItemBarEntry)graphScanEntriesList.get(0)).getRssiVal();
        for(int idx=1; idx<listSize; ++idx)
        {  //for each item in list
          if(((ScanItemBarEntry)graphScanEntriesList.get(idx)).getRssiVal() > maxItemRssiVal)
          {  //new maximum
            maxItemIdx = idx;
            maxItemRssiVal = ((ScanItemBarEntry)graphScanEntriesList.get(idx)).getRssiVal();
          }
        }
        int j = maxItemIdx;       //check if 3+ bars at max level
        while(j < listSize-1 &&
                ((ScanItemBarEntry)graphScanEntriesList.get(j+1)).getRssiVal() == maxItemRssiVal)
        {
          ++j;
        }
        if((j=j-maxItemIdx) >= 3)      //if 3+ bars at max level then
          maxItemIdx += j / 2;         //select center bar
        return maxItemIdx;
      }
    }
    catch(Exception ex)
    {  //some kind of exception error
      Log.e(LOG_TAG, "Exception in 'findHighestRssiScanItem()'", ex);
    }
    return -1;
  }

  /**
   * Fetches the specified scan-item object from the 'graphScanEntriesList'.
   * @param idx index into 'graphScanEntriesList' for item.
   * @return The ScanItemBarEntry item, or null if index out of bounds.
   */
  private ScanItemBarEntry entriesListIdxToScanItem(int idx)
  {
    return (idx >= 0 && idx < graphScanEntriesList.size()) ?
                     (ScanItemBarEntry)graphScanEntriesList.get(idx) : null;
  }

  /**
   * Fetches the frequency value for the specified scan-item object from
   * the 'graphScanEntriesList'.
   * @param idx index into 'graphScanEntriesList' for item.
   * @return The frequency value, or -1 if index out of bounds.
   */
  private int entriesListIdxToFreqVal(int idx)
  {
    final ScanItemBarEntry itemObj;
    return ((itemObj=entriesListIdxToScanItem(idx)) != null) ? itemObj.getFreqVal() : -1;
  }

  /**
   * Determines the index into 'graphScanEntriesList' for the given frequency.
   * This method should only be used while the graph-data-scan thread is
   * paused or between updates.
   * @param freqVal frequency value.
   * @return Index into 'graphScanEntriesList' for frequency, or -1 if no match.
   */
  private int freqValToEntriesListIdx(int freqVal)
  {
    try
    {
      final int listSize = graphScanEntriesList.size();
      for(int idx=0; idx<listSize; ++idx)
      {  //for each entry in list; check if frequency matches
        if(((ScanItemBarEntry)graphScanEntriesList.get(idx)).getFreqVal() == freqVal)
          return idx;
      }
    }
    catch(Exception ex)
    {  //some kind of exception error; return -1
    }
    return -1;
  }

  /**
   * Returns the index into 'graphScanEntriesList' corresponding to the
   * last highlighted bar on the graph.  This method should only be used
   * while the graph-data-scan thread is paused or between updates.
   * @return Index into 'graphScanEntriesList', or -1 if none.
   */
  private int getIdxForLastHighlightedBar()
  {
    return freqValToEntriesListIdx(graphScanLastHighlightFreqVal);
  }

  /**
   * Tunes the receiver to the channel specified by the given scan-item object.
   * @param itemObj scan-item object to use.
   * @param updateHighlightFlag true to update highlighted bar on graph.
   */
  private void tuneToScanItemChannel(ScanItemBarEntry itemObj, boolean updateHighlightFlag)
  {
    if(itemObj != null && graphScanPausedFlag)
    {  //item OK and data-scan thread is paused
      if(vidReceiverManagerObj != null)
      {  //receiver manager OK
        final String chCodeStr;
        if((chCodeStr=itemObj.getChanCodeStr()) != null &&
                                                chCodeStr.trim().length() > 0)
        {  //channel code is OK; tune to it
          vidReceiverManagerObj.tuneReceiverToChannelCode(chCodeStr);
        }
        else     //if no channel code then tune to frequency value
          vidReceiverManagerObj.tuneReceiverToFrequency(itemObj.getFreqVal());
      }
      if(updateHighlightFlag)          //if flag then update highlighted bar on graph
        setHighlightBarAndUpdateGraph(itemObj.getFreqVal());
    }
  }

  /**
   * Sets the highlighted bar and posts an update to the graph.
   * @param xVal X-axis (frequency) value for bar entry to be highlighted, or -1 for none.
   */
  private void setHighlightBarAndUpdateGraph(int xVal)
  {
    graphScanLastHighlightFreqVal = xVal;        //track highlighted freq / bar
    widgetUpdateHandlerObj.obtainMessage(UPD_HIGHLIGHT_MSG,xVal,0).sendToTarget();
  }

  /**
   * Updates the contents of the status text view.
   * @param str contents for text view.
   */
  private void updateStatusTextView(String str)
  {
    widgetUpdateHandlerObj.obtainMessage(UPD_STATUSTEXT_MSG,str).sendToTarget();
  }

  //Handler that manages updates to GUI items in the fragment.
  private final Handler widgetUpdateHandlerObj =
        new Handler(Looper.getMainLooper())           //run on UI thread
          {
            @Override
            public void handleMessage(Message msgObj)
            {
              try
              {
                switch(msgObj.what)
                {
                  case UPD_HIGHLIGHT_MSG:       //update highlighted bar on graph
                    if(msgObj.arg1 > 0)     //value is valid
                      graphScanBarChartObj.highlightValue(msgObj.arg1,0,false);
                    else                    //value not valid; clear highlight
                      graphScanBarChartObj.highlightValue(0,-1,false);
                             // call to 'highlightValue()' will also invoke 'invalidate()'
                    break;
                  case UPD_STATUSTEXT_MSG:       //update status text view
                    if(msgObj.obj instanceof String)
                      graphScanStatusTextViewObj.setText((String)msgObj.obj);
                    break;
                }
              }
              catch(Exception ex)
              {  //some kind of exception error; log it
                Log.e(LOG_TAG, "Exception in 'widgetUpdateHandler'", ex);
              }
            }
          };


  /**
   * Class SimpleIntAxisValueFormatter defines an axis-value formatter for
   * integer values.
   */
  public static class SimpleIntAxisValueFormatter implements IAxisValueFormatter
  {
    @Override
    public String getFormattedValue(float value, AxisBase axis)
    {
      return Integer.toString(Math.round(value));
    }
  }


  /**
   * Class SimpleIntValueFormatter defines a value formatter for integer values.
   */
  public static class SimpleIntValueFormatter implements IValueFormatter
  {
    @Override
    public String getFormattedValue(float value, Entry entry, int dataSetIndex,
                                                                 ViewPortHandler viewPortHandler)
    {
      return Integer.toString(Math.round(value));
    }
  }

  /**
   * Class GraphScanChartRenderer extends BarChartRenderer to add support
   * for showing X-axis values above the bars on flagged entries.
   */
  private class GraphScanChartRenderer extends BarChartRenderer
  {
    /**
     * Creates a renderer using 'graphScanBarChartObj'.
     */
    public GraphScanChartRenderer()
    {
      super(graphScanBarChartObj,
                  graphScanBarChartObj.getAnimator(), graphScanBarChartObj.getViewPortHandler());
    }

    /**
     * Code from BarChartRenderer, modified to show X-axis values above
     * the bars on flagged entries.
     * @param canvasObj target canvas to draw on.
     */
    @Override
    public void drawValues(Canvas canvasObj)
    {
      List<IBarDataSet> dataSets = mChart.getBarData().getDataSets();
      final float valueOffsetPlus = Utils.convertDpToPixel(4.5f);
      float posOffset = 0f;
      float negOffset = 0f;
      boolean drawValueAboveBar = mChart.isDrawValueAboveBarEnabled();
      for(int i = 0; i < mChart.getBarData().getDataSetCount(); i++)
      {
        IBarDataSet dataSet = dataSets.get(i);
        if(!shouldDrawValues(dataSet))
          continue;
        // apply the text-styling defined by the DataSet
        applyValueTextStyle(dataSet);
        boolean isInverted = mChart.isInverted(dataSet.getAxisDependency());
        // calculate the correct offset depending on the draw position of the value
        float valueTextHeight = Utils.calcTextHeight(mValuePaint, "8");
        posOffset = (drawValueAboveBar ? -valueOffsetPlus : valueTextHeight + valueOffsetPlus);
        negOffset = (drawValueAboveBar ? valueTextHeight + valueOffsetPlus : -valueOffsetPlus);
        if(isInverted)
        {
          posOffset = -posOffset - valueTextHeight;
          negOffset = -negOffset - valueTextHeight;
        }
        // get the buffer
        BarBuffer buffer = mBarBuffers[i];
        final float phaseY = mAnimator.getPhaseY();
        // if only single values are drawn (sum)
        if(!dataSet.isStacked())
        {
          for(int j = 0; j < buffer.buffer.length * mAnimator.getPhaseX(); j += 4)
          {
            float x = (buffer.buffer[j] + buffer.buffer[j + 2]) / 2f;
            if(!mViewPortHandler.isInBoundsRight(x))
              break;
            if(!mViewPortHandler.isInBoundsY(buffer.buffer[j + 1])
                       || !mViewPortHandler.isInBoundsLeft(x))
            {
              continue;
            }
            BarEntry entry = dataSet.getEntryForIndex(j / 4);
            if(entry instanceof ScanItemBarEntry &&
                                             ((ScanItemBarEntry)entry).getShowFreqAboveBarFlag())
            {  //show X (frequency) value above bar
              float val = entry.getX();
              drawValue(canvasObj, dataSet.getValueFormatter(), val, entry, i, x,
                    val >= 0 ? (buffer.buffer[j + 1] + posOffset) : (buffer.buffer[j + 3] + negOffset),
                    dataSet.getValueTextColor(j / 4));
            }
          }
        }
      }
    }
  }

  /**
   * Class ReceiverScanDataThread defines a background-worker thread for
   * reading data from the receiver and sending it to the graphing handler.
   */
  private class ReceiverScanDataThread extends PausableThread
  {
    private boolean threadPauseOrTerminateFlag = false;

    /**
     * Creates a background-worker thread.
     */
    public ReceiverScanDataThread()
    {
      super("graphReceiverScanData");
    }

    /**
     * Executing method for thread.
     */
    public void run()
    {
      VidReceiverManager vidRecvrMgrObj = null;
      try
      {
        vidRecvrMgrObj = ProgramResources.getProgramResourcesObj().getVidReceiverManagerObj();
        if(vidRecvrMgrObj == null || !vidRecvrMgrObj.isReceiverSerialConnected())
        {  //video receiver not connected (test mode)
          doTestModeGraphOutput();
          return;
        }
        vidRecvrMgrObj.pauseReceiverUpdateWorker();   //make sure manager worker is paused
        vidReceiverManagerObj = vidRecvrMgrObj;       //make mgr available to other methods
        waitForNotify(100);
        String respStr;
        boolean errFlag = false;
        int freqVal;
        while(!isTerminated())
        {  //for each iteration of "XF" command and response
//          System.out.println("DEBUG ReceiverScanDataThread sending XF");
          vidRecvrMgrObj.outputFullBandScanCommand();      //send "XF" command
          while(true)
          {  //for each "freqCC=rssi" line received
            if((respStr=vidRecvrMgrObj.getNextReceivedLine(
                                                   VidReceiverManager.RESP_WAIT_TIMEMS)) == null)
            {  //error fetching line
              errFlag = true;
              break;
            }
//            System.out.println("DEBUG ReceiverScanDataThread " + respStr);
            if((freqVal=parseFreqRssiAndDoUpdate(respStr)) < 0)
            {  //error parsing line
              errFlag = true;
              break;
            }
            if(freqVal == 0)      //if "0=0" received then
              break;              //exit inner loop (end of set)
            if(threadPauseOrTerminateFlag)
            {  //thread is pausing or terminating; send CR to abort rest of scan
              vidRecvrMgrObj.transmitCarriageReturn();
            }
          }
          if(errFlag)
          {  //error was flagged
            Log.e(LOG_TAG, "Processing error in ReceiverScanDataThread");
            errFlag = false;
            if(!isTerminated())
            {  //thread is not terminating
              vidRecvrMgrObj.transmitCarriageReturn();     //send CR to abort rest of scan
              waitForNotify(250);                          //pause to flush incoming data
            }
          }
          if(threadPauseOrTerminateFlag)
          {  //thread is pausing or terminating; restore to channel that was active before scan
            vidRecvrMgrObj.setReceiverViaChannelTracker();
            threadPauseOrTerminateFlag = false;
          }
          if(isTerminated())
            break;
          waitForNotify(10);           //handle thread pause and terminate
        }
      }
      catch(Exception ex)
      {  //some kind of exception error; log it
        Log.e(LOG_TAG, "Exception in ReceiverScanDataThread", ex);
      }
    }

    /**
     * Pauses the thread.
     * @param waitTimeMs the maximum number of milliseconds to wait for
     * the thread to pause, or 0 to return immediately.
     * @return false if a timeout occurred before the thread-pause could
     * be confirmed; true otherwise.
     */
    public boolean pauseThread(long waitTimeMs)
    {
      threadPauseOrTerminateFlag = true;        //trigger sending of CR to abort scan
      return super.pauseThread(waitTimeMs);
    }

    /**
     * Performs a thread-notify and terminates this thread if the thread
     * is not terminated and alive.
     * @param waitTimeMs the maximum number of milliseconds to wait for
     * the thread to terminate, or 0 to wait indefinitely.
     */
    public void terminate(long waitTimeMs)
    {
      threadPauseOrTerminateFlag = true;        //trigger sending of CR to abort scan
      super.terminate(waitTimeMs);
    }

    /**
     * Parses frequency and RSSI values from the given string and sends them
     * as a graph update.
     * @param respStr response string, "freqCC=rssi".
     * @return Parsed frequency value if successful; -1 if parsing error.
     */
    private int parseFreqRssiAndDoUpdate(String respStr)
    {
      try
      {       //parse "freqCC=rssi" response
        if(respStr.startsWith(">"))         //ignore any leading '>' prompt
          respStr = respStr.substring(1);
        respStr = respStr.trim();           //remove leading space
        final int p;
        if((p=respStr.indexOf('=')) > 0)
        {  //length of response OK; parse digits as frequency (max of 4)
          final int freqVal = Integer.parseInt(respStr.substring(0, ((p<4) ? p : 4)));
          final String chCodeStr = (p >= 6) ? respStr.substring(4,6) : null;
          int q;             //check for trailing space
          if((q=respStr.indexOf(' ',p+1)) < 0)
            q = respStr.length();      //if no trailing space then end of string
          final int rssiVal = Integer.parseInt(respStr.substring(p+1,q));
                             //send values as graph update:
          updateNextGraphEntry(freqVal,rssiVal,chCodeStr);
          return freqVal;    //indicate success
        }
      }
      catch(NumberFormatException ex)
      {  //error parsing; return -1 for error
      }
      return -1;
    }

    /**
     * Generates a simulated-signal output on the graph (test mode).
     */
    private void doTestModeGraphOutput()
    {
      final int numTestFreqs = graphScanEntriesList.size();
         //create array of frequency-scan items for test output:
      final ScanItemBarEntry [] testEntriesArr = new ScanItemBarEntry[numTestFreqs];
      graphScanEntriesList.toArray((BarEntry[])testEntriesArr);
      int rssiVal, curSignalIdx = 0;
      waitForNotify(100);         //do initial delay (in case graph needs to setup)
      while(!isTerminated())
      {  //for each simulated "signal" frequency (while thread not terminated)
        for(int updIdx=0; updIdx<numTestFreqs; ++updIdx)
        {  //for each frequency-scan item in array
                   //generate test value using index of frequency vs "signal" frequency
          rssiVal = Math.abs(50 - Math.abs(updIdx-curSignalIdx)*2);
          updateNextGraphEntry(testEntriesArr[updIdx].getFreqVal(),rssiVal,
                                                        testEntriesArr[updIdx].getChanCodeStr());
          waitForNotify(5);
          if(isTerminated())
            return;
        }
        updateNextGraphEntry(0,0,null);          //indicate end of set
        if(++curSignalIdx >= numTestFreqs)       //increment "signal" frequency
          curSignalIdx = 0;                      // (with wrap-around)
        waitForNotify(100);
      }
    }
  }


  /**
   * Defines a bar-graph entry item for frequency/RSSI-scan values.  An array of
   * ScanItemBarEntry objects may be handled like an array of String objects.
   */
  public static class ScanItemBarEntry extends BarEntry implements CharSequence
  {
    protected String displayString;
    private boolean showFreqAboveBarFlag = false;
    protected boolean dataModifiedFlag = true;
    private float codeFreqFieldWidth = 0.0f, rssiValFieldWidth = 0.0f;

    /**
     * Creates a bar-graph entry item for frequency/RSSI-scan values.
     * @param freqVal frequency value for item.
     * @param rssiVal scanned RSSI value for item.
     * @param chanCodeStr channel-code value for item (i.e., "F4"), or null for none.
     */
    public ScanItemBarEntry(int freqVal, int rssiVal, String chanCodeStr)
    {
      super((float)freqVal,(float)rssiVal,chanCodeStr);
    }

    /**
     * Updates the display string for item using its current values.
     */
    protected void updateDisplayString()
    {
         //calculate field widths and padding so columns of item values will line up:
      if(codeFreqFieldWidth <= 0.0f)
        codeFreqFieldWidth = GuiUtils.measureTextViaUtilPaint("mm ");
      if(rssiValFieldWidth <= 0.0f)
        rssiValFieldWidth = GuiUtils.measureTextViaUtilPaint("100");
      final Object codeStr = (getData()!=null)?(getData()+" "):"";     //pad string for chan-code column:
      final String codeFreqPadStr = GuiUtils.getFillerStr(codeFreqFieldWidth,codeStr);
      final int rssiVal = getRssiVal();     //pad string for RSSI-value column:
      final String rssiValStr = Integer.toString(rssiVal);
      final String rssiPadStr = GuiUtils.getFillerStr(rssiValFieldWidth,rssiValStr);
      final int numBars;
      if(rssiVal >= 0)       //show number of "bars" based on RSSI value
        numBars = (rssiVal <= 100) ? rssiVal/10 : 10;
      else
        numBars = 0;
      final String barsStr = "||||||||||".substring(0,numBars);
      displayString = ((codeStr != null) ? codeStr : "") + codeFreqPadStr + "\t\t" +
                 getFreqVal() + "\t\t" + rssiPadStr + rssiValStr + "\t\t" + barsStr;
      dataModifiedFlag = false;        //clear updated-needed flag
    }

    /**
     * Sets the frequency value for this entry item.
     * @param freqVal frequency value for item.
     */
    public void setFreqVal(int freqVal)
    {
      setX(freqVal);
    }

    /**
     * Sets the X (frequency) value for this entry item.
     * @param val X value for item.
     */
    @Override
    public void setX(float val)
    {
      super.setX(val);
      dataModifiedFlag = true;         //indicate need to update 'displayString'
    }

    /**
     * Sets the Y (RSSI) value for this entry item.
     * @param val Y value for item.
     */
    @Override
    public void setY(float val)
    {
      super.setY(val);
      dataModifiedFlag = true;         //indicate need to update 'displayString'
    }

    /**
     * Sets the data obj (channel-code string) for this entry item.
     * @param obj data object for item.
     */
    @Override
    public void setData(Object obj)
    {
      super.setData(obj);
      dataModifiedFlag = true;         //indicate need to update 'displayString'
    }

    /**
     * Returns the frequency value for this entry item.
     * @return The integer frequency value for this entry item.
     */
    public int getFreqVal()
    {
      return Math.round(getX());
    }

    /**
     * Sets the RSSI value for this entry item.
     * @param rssiVal scanned RSSI value for item.
     */
    public void setRssiVal(int rssiVal)
    {
      setY(rssiVal);
    }

    /**
     * Returns the RSSI value for this entry item.
     * @return The integer RSSI value for this entry item.
     */
    public int getRssiVal()
    {
      return Math.round(getY());
    }

    /**
     * Sets the channel-code string for this entry item.
     * @param chanCodeStr channel-code value for item (i.e., "F4"), or null for none.
     */
    public void setChanCodeStr(String chanCodeStr)
    {
      setData(chanCodeStr);
    }

    /**
     * Returns the channel-code string for this entry item.
     * @return The channel-code string for this entry item, or null if none.
     */
    public String getChanCodeStr()
    {
      return (getData() instanceof String) ? (String)getData() : null;
    }

    /**
     * Sets the value for the flag determining whether or not the frequency
     * value should be shown above the bar on the graph.
     * @param flagVal true to show the value; false to not show the value.
     */
    public void setShowFreqAboveBarFlag(boolean flagVal)
    {
      showFreqAboveBarFlag = flagVal;
    }

    /**
     * Returns the value for the flag determining whether or not the frequency
     * value should be shown above the bar on the graph.
     * @return true to show the value; false to not show the value.
     */
    public boolean getShowFreqAboveBarFlag()
    {
      return showFreqAboveBarFlag;
    }

    /**
     * Returns length of display string for item.
     * @return Length of display string for item.
     */
    @Override
    public int length()
    {
      if(dataModifiedFlag)             //if data modified then
        updateDisplayString();         //do update first
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
      if(dataModifiedFlag)             //if data modified then
        updateDisplayString();         //do update first
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
      if(dataModifiedFlag)             //if data modified then
        updateDisplayString();         //do update first
      return displayString.subSequence(start,end);
    }

    /**
     * Returns display string for item.
     * @return Display string for item.
     */
    @Override
    public String toString()
    {
      if(dataModifiedFlag)             //if data modified then
        updateDisplayString();         //do update first
      return displayString;
    }
  }
}
