package com.sumologic.opentelemetry.rum

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.fragment.app.FragmentActivity

class ActivityLifecycleHandler: Application.ActivityLifecycleCallbacks {
    private val instrumentation = UiClassInstrumentation<Activity>()

    override fun onActivityPreCreated(activity: Activity, savedInstanceState: Bundle?) {
        super.onActivityPreCreated(activity, savedInstanceState)
        if (activity is FragmentActivity) {
            activity.supportFragmentManager.registerFragmentLifecycleCallbacks(FragmentLifecycleHandler(), true)
        }
        instrumentation.createSpan(activity)
    }

    override fun onActivityCreated(p0: Activity, p1: Bundle?) {
        instrumentation.getSpan(p0)?.addEvent("activityCreated")
    }

    override fun onActivityPostCreated(activity: Activity, savedInstanceState: Bundle?) {
        instrumentation.getSpan(activity)?.addEvent("activityPostCreated")
    }

    override fun onActivityPreStarted(activity: Activity) {
        instrumentation.getSpan(activity)?.addEvent("activityPreStarted")
    }

    override fun onActivityStarted(p0: Activity) {
        instrumentation.getSpan(p0)?.addEvent("activityStarted")
    }

    override fun onActivityPostStarted(activity: Activity) {
        instrumentation.getSpan(activity)?.addEvent("activityPostStarted")
    }

    override fun onActivityPreResumed(activity: Activity) {
        instrumentation.getSpan(activity)?.addEvent("activityPreResumed")
    }

    override fun onActivityResumed(p0: Activity) {
        instrumentation.getSpan(p0)?.addEvent("activityResumed")
    }

    override fun onActivityPostResumed(activity: Activity) {
        instrumentation.getSpan(activity)?.addEvent("activityPostResumed")
        instrumentation.endSpan(activity)
    }

    override fun onActivityPrePaused(activity: Activity) {
    }

    override fun onActivityPaused(p0: Activity) {
    }

    override fun onActivityPostPaused(activity: Activity) {
    }

    override fun onActivityPreStopped(activity: Activity) {
    }

    override fun onActivityStopped(p0: Activity) {
    }

    override fun onActivityPostStopped(activity: Activity) {
    }

    override fun onActivityPreSaveInstanceState(activity: Activity, outState: Bundle) {
    }

    override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) {
    }

    override fun onActivityPostSaveInstanceState(activity: Activity, outState: Bundle) {
    }

    override fun onActivityPreDestroyed(activity: Activity) {
    }

    override fun onActivityDestroyed(p0: Activity) {
    }

    override fun onActivityPostDestroyed(activity: Activity) {
    }
}