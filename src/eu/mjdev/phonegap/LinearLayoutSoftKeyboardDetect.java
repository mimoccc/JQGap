package eu.mjdev.phonegap;

import eu.mjdev.phonegap.api.LOG;
import android.content.Context;
import android.widget.LinearLayout;

/**
 * This class is used to detect when the soft keyboard is shown and hidden in the web view.
 */
public class LinearLayoutSoftKeyboardDetect extends LinearLayout {

    private static final String TAG = "SoftKeyboardDetect";
    
    private int oldHeight = 0;  // Need to save the old height as not to send redundant events
    private int oldWidth = 0; // Need to save old width for orientation change          
    private int screenWidth = 0;
    private int screenHeight = 0;
    private DroidGap app = null;
                
    public LinearLayoutSoftKeyboardDetect(Context context, int width, int height) {
        super(context);     
        screenWidth = width;
        screenHeight = height;
        app = (DroidGap) context;
    }

    @Override
    /**
     * Start listening to new measurement events.  Fire events when the height 
     * gets smaller fire a show keyboard event and when height gets bigger fire 
     * a hide keyboard event.
     * 
     * Note: We are using app.postMessage so that this is more compatible with the API
     * 
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);       
        LOG.v(TAG, "We are in our onMeasure method");
        int width, height;
        height = MeasureSpec.getSize(heightMeasureSpec);
        width = MeasureSpec.getSize(widthMeasureSpec);
        LOG.v(TAG, "Old Height = %d", oldHeight);
        LOG.v(TAG, "Height = %d", height);             
        LOG.v(TAG, "Old Width = %d", oldWidth);
        LOG.v(TAG, "Width = %d", width);
        if (oldHeight == 0 || oldHeight == height) {
            LOG.d(TAG, "Ignore this event");
            if(app != null) app.sendJavascript("$(window).triggerHandler('resize');");
        }
        else if(screenHeight == width)
        {
            int tmp_var = screenHeight;
            screenHeight = screenWidth;
            screenWidth = tmp_var;
            if(app != null) app.sendJavascript("$(window).triggerHandler('resize');");
        }
        else if (height > oldHeight) {
            if(app != null) app.sendJavascript("$(window).triggerHandler('hide_keyboard');");
        } 
        else if (height < oldHeight) {
            if(app != null) app.sendJavascript("$(window).triggerHandler('show_keyboard');");
        }
        oldHeight = height;
        oldWidth = width;
    }

}
