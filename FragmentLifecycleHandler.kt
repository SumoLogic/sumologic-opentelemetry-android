package com.sumologic.opentelemetry.rum

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager

class FragmentLifecycleHandler: FragmentManager.FragmentLifecycleCallbacks() {
    private val instrumentation = UiClassInstrumentation<Fragment>()

    override fun onFragmentPreAttached(fm: FragmentManager, f: Fragment, context: Context) {
        instrumentation.createSpan(f)
    }

    override fun onFragmentAttached(fm: FragmentManager, f: Fragment, context: Context) {
        instrumentation.getSpan(f)?.addEvent("fragmentAttached")
    }

    override fun onFragmentPreCreated(
        fm: FragmentManager,
        f: Fragment,
        savedInstanceState: Bundle?
    ) {
        instrumentation.getSpan(f)?.addEvent("fragmentPreCreated")
    }

    override fun onFragmentCreated(fm: FragmentManager, f: Fragment, savedInstanceState: Bundle?) {
        instrumentation.getSpan(f)?.addEvent("fragmentCreated")
    }

    override fun onFragmentActivityCreated(
        fm: FragmentManager,
        f: Fragment,
        savedInstanceState: Bundle?
    ) {
        instrumentation.getSpan(f)?.addEvent("fragmentActivityCreated")
    }

    override fun onFragmentViewCreated(
        fm: FragmentManager,
        f: Fragment,
        v: View,
        savedInstanceState: Bundle?
    ) {
        if (instrumentation.getSpan(f) == null) {
            instrumentation.createSpan(f) // in case when fragment is restored
        }
        instrumentation.getSpan(f)?.addEvent("fragmentViewCreated")
    }

    override fun onFragmentStarted(fm: FragmentManager, f: Fragment) {
        instrumentation.getSpan(f)?.addEvent("fragmentStarted")
    }

    override fun onFragmentResumed(fm: FragmentManager, f: Fragment) {
        instrumentation.getSpan(f)?.addEvent("fragmentResumed")
        instrumentation.endSpan(f)
    }

    override fun onFragmentPaused(fm: FragmentManager, f: Fragment) {
        instrumentation.getSpan(f)?.addEvent("fragmentPaused")
    }

    override fun onFragmentStopped(fm: FragmentManager, f: Fragment) {
        instrumentation.getSpan(f)?.addEvent("fragmentStopped")
    }

    override fun onFragmentSaveInstanceState(fm: FragmentManager, f: Fragment, outState: Bundle) {
        instrumentation.getSpan(f)?.addEvent("fragmentSaveInstanceState")
    }

    override fun onFragmentViewDestroyed(fm: FragmentManager, f: Fragment) {
        instrumentation.getSpan(f)?.addEvent("fragmentViewDestroyed")
    }

    override fun onFragmentDestroyed(fm: FragmentManager, f: Fragment) {
        instrumentation.getSpan(f)?.addEvent("fragmentDestroyed")
    }

    override fun onFragmentDetached(fm: FragmentManager, f: Fragment) {
        instrumentation.getSpan(f)?.addEvent("fragmentDetached")
    }
}