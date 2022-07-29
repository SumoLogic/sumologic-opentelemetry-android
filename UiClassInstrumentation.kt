package com.sumologic.opentelemetry.rum

import android.os.Handler
import android.os.Looper
import io.opentelemetry.api.GlobalOpenTelemetry
import io.opentelemetry.api.trace.Span
import io.opentelemetry.api.trace.Tracer
import io.opentelemetry.context.Scope
import java.util.*

class UiClassInstrumentation<T: Any> {
    private val tracer: Tracer = GlobalOpenTelemetry.getTracer("sumo-logic-rum")
    private val spans = WeakHashMap<T, Span>()
    private val spanScopes = WeakHashMap<Span, Scope>()

    fun createSpan(target: T) {
        val span = tracer.spanBuilder(target.javaClass.simpleName).startSpan()
        spans[target] = span
        spanScopes[span] = span.makeCurrent()
    }

    fun getSpan(target: T): Span? = spans[target]

    fun endSpan(target: T) {
        val span = spans[target] ?: return
        val spanScope = spanScopes[span]
        spans.remove(target)

        span.end()
        Handler(Looper.getMainLooper()).postDelayed({
            spanScope?.close()
        }, 100)
    }
}