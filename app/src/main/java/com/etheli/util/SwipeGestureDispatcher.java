//SwipeGestureDispatcher.java:  Monitors 'touch' events and dispatches those
//                              detected as 'swipe' events to a listener.
//
//  2/23/207 -- [ET]
//

package com.etheli.util;

import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * Class SwipeGestureDispatcher monitors 'touch' events and dispatches those
 * detected as 'swipe' events to a listener.
 */
public class SwipeGestureDispatcher implements View.OnTouchListener
{
  private final GestureDetector swipeGesDetObj =
                          new GestureDetector(new SwipeGestureListener());
  private SwipeGestureEventInterface swipeGestureEventIntfObj = null;

  /**
   * Interface SwipeGestureEventInterface defines the method for receiving
   * 'swipe' events.
   */
  public interface SwipeGestureEventInterface
  {
    /**
     * Invoked when a 'swipe' event is detected.
     * @param rightFlag true if the swipe went left to right; false if the
     * swipe went right to left.
     */
    public void onSwipeGesture(boolean rightFlag);
  }

  /**
   * Sets the listener object to be invoked when a 'swipe' event is detected.
   * @param intfObj listener object, or null for none (no dispatch).
   */
  public void setSwipeGestureEventIntfObj(SwipeGestureEventInterface intfObj)
  {
    swipeGestureEventIntfObj = intfObj;
  }

  /**
   * Returns the listener object that will be invoked when a 'swipe' event is detected.
   * @return the listener object, or null for none set.
   */
  public SwipeGestureEventInterface getSwipeGestureEventIntfObj()
  {
    return swipeGestureEventIntfObj;
  }

  /**
   * Called when a touch event has been received.
   * @param view The View associated with the touch event.
   * @param motionEvent The MotionEvent object containing information about the event.
   * @return true if the listener has consumed the event; false otherwise.
   */
  @Override
  public boolean onTouch(View view, MotionEvent motionEvent)
  {
    return !(swipeGesDetObj.onTouchEvent(motionEvent));
  }


  /**
   * Class SwipeGestureListener implements listener support for monitoring
   * 'touch' events and dispatching those detected as 'swipe' events to a
   * listener.  Based on code from:
   * http://smartandroidians.blogspot.in/2010/04/swipe-action-and-viewflipper-in-android.html
   */
  public class SwipeGestureListener extends GestureDetector.SimpleOnGestureListener
  {
    private static final int SWIPE_MIN_DISTANCE = 50;           //min delta-X for swipe
    private static final int SWIPE_MAX_OFF_PATH = 100;          //max delta-Y for swipe
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;    //min speed for swipe

    /**
     * Notified of a fling event when it occurs with the initial on down
     * MotionEvent and the matching up MotionEvent. The calculated velocity
     * is supplied along the x and y axis in pixels per second.
     * @param e1 MotionEvent: The first down motion event that started the
     * fling.
     * @param e2 MotionEvent: The move motion event that triggered the
     * current onFling.
     * @param velocityX float: The velocity of this fling measured in pixels
     * per second along the x axis.
     * @param velocityY  float: The velocity of this fling measured in pixels
     * per second along the y axis.
     * @return true if the event is consumed, else false.
     */
    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY)
    {
      if(swipeGestureEventIntfObj != null)
      {  //callback object is set
        if(Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
          return false;      //if gesture has too much vertical then ignore
        if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE &&
                               Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY)
        {  //swipe left
          swipeGestureEventIntfObj.onSwipeGesture(false);
        }
        else if(e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE &&
                               Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY)
        {  //swipe right
          swipeGestureEventIntfObj.onSwipeGesture(true);
        }
      }
      return super.onFling(e1,e2,velocityX,velocityY);
    }
  }
}
