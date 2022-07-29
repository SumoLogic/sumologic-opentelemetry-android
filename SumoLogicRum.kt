package com.sumologic.opentelemetry.rum

import android.app.Application
import android.content.Context
import android.os.Build
import android.provider.Settings
import com.github.anrwatchdog.ANRWatchDog
import com.sumologic.thecoffeebar.BuildConfig
import io.opentelemetry.api.GlobalOpenTelemetry
import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.api.common.Attributes
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator
import io.opentelemetry.context.propagation.ContextPropagators
import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporter
import io.opentelemetry.sdk.OpenTelemetrySdk
import io.opentelemetry.sdk.common.Clock
import io.opentelemetry.sdk.common.CompletableResultCode
import io.opentelemetry.sdk.resources.Resource
import io.opentelemetry.sdk.trace.SdkTracerProvider
import io.opentelemetry.sdk.trace.data.DelegatingSpanData
import io.opentelemetry.sdk.trace.data.SpanData
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor
import io.opentelemetry.sdk.trace.export.SpanExporter
import java.util.*
import java.util.concurrent.TimeUnit

private enum class ResourceAttributes(val value: String) {
    SERVICE_NAME("service.name"),
    SERVICE_VERSION("service.version"),
    APPLICATION("application"),
    DEVICE_MODEL("device.model"),
    DEVICE_ID("device.id"),
    OS_NAME("os.name"),
    OS_VERSION("os.version")
}

private fun getDeviceName(): String =
    if (Build.MODEL.startsWith(Build.MANUFACTURER, ignoreCase = true)) {
        Build.MODEL
    } else {
        "${Build.MANUFACTURER} ${Build.MODEL}"
    }.replaceFirstChar { it.uppercase() }

private fun getDeviceId(context: Context): String =
    // TODO: use advertising id instead
    Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)

class SumoLogicRum {
    companion object {
        fun initialize(
            application: Application,
            collectionSourceUrl: String,
            serviceName: String = application.applicationContext.applicationInfo.loadLabel(application.applicationContext.packageManager).toString(),
            applicationName: String?
        ) {
            val attributesBuilder = Attributes.builder()
            fun putAttribute(key: ResourceAttributes, value: String) {
                attributesBuilder.put(key.value, value)
            }
            putAttribute(ResourceAttributes.SERVICE_NAME, serviceName)
            putAttribute(ResourceAttributes.SERVICE_VERSION, BuildConfig.VERSION_NAME)
            applicationName?.let { putAttribute(ResourceAttributes.APPLICATION, it) }
            putAttribute(ResourceAttributes.DEVICE_MODEL, getDeviceName())
            putAttribute(ResourceAttributes.DEVICE_ID, getDeviceId(application.applicationContext))
            putAttribute(ResourceAttributes.OS_NAME, "Android")
            putAttribute(ResourceAttributes.OS_VERSION, Build.VERSION.RELEASE)

            val spanExporter = SumoLogicOtlpTraceExporter(OtlpHttpSpanExporter.builder()
                .setEndpoint(collectionSourceUrl)
                .build())
            val spanProcessor = BatchSpanProcessor.builder(spanExporter).build()
            val tracerProvider = SdkTracerProvider.builder()
                .addSpanProcessor(spanProcessor)
                .setResource(Resource.create(attributesBuilder.build()))
                .build()
            OpenTelemetrySdk.builder()
                .setTracerProvider(tracerProvider)
                .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
                .buildAndRegisterGlobal()
            Runtime.getRuntime().addShutdownHook(Thread(tracerProvider::shutdown))

            application.registerActivityLifecycleCallbacks(ActivityLifecycleHandler())

            // ANR spans
            ANRWatchDog().setANRListener {
                println("ANR LISTENER !!!! ")
                val tracer = GlobalOpenTelemetry.getTracer("sumo-logic-rum")
                val span = tracer.spanBuilder("ANR").setStartTimestamp(System.currentTimeMillis() - it.duration, TimeUnit.MILLISECONDS).startSpan()
                span.recordException(it)
                span.end()
            }
        }
    }
}