package io.brahmaos.wallet.brahmawallet;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

public class AppFrontBackHelper {
    private OnAppStatusListener mOnAppStatusListener;

    public AppFrontBackHelper() {

    }

    public void register(Application application, OnAppStatusListener listener){
        mOnAppStatusListener = listener;
        application.registerActivityLifecycleCallbacks(activityLifecycleCallbacks);
    }

    public void unRegister(Application application){
        application.unregisterActivityLifecycleCallbacks(activityLifecycleCallbacks);
    }

    private Application.ActivityLifecycleCallbacks activityLifecycleCallbacks = new Application.ActivityLifecycleCallbacks() {
        private int activityStartCount = 0;

        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

        }

        @Override
        public void onActivityStarted(Activity activity) {
            activityStartCount++;
            //change from 0 to 1: from background to front
            if (activityStartCount == 1){
                if(mOnAppStatusListener != null){
                    mOnAppStatusListener.onFront();
                }
            }
        }

        @Override
        public void onActivityResumed(Activity activity) {

        }

        @Override
        public void onActivityPaused(Activity activity) {

        }

        @Override
        public void onActivityStopped(Activity activity) {
            activityStartCount--;
            //change from 1 to 0: from front to background
            if (activityStartCount == 0){
                if(mOnAppStatusListener != null){
                    mOnAppStatusListener.onBack();
                }
            }
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

        }

        @Override
        public void onActivityDestroyed(Activity activity) {

        }
    };

    public interface OnAppStatusListener{
        void onFront();
        void onBack();
    }

}
