package com.sumologic.opentelemetry.rum

import io.opentelemetry.api.common.Attributes
import io.opentelemetry.sdk.common.Clock
import io.opentelemetry.sdk.common.CompletableResultCode
import io.opentelemetry.sdk.resources.Resource
import io.opentelemetry.sdk.trace.data.DelegatingSpanData
import io.opentelemetry.sdk.trace.data.SpanData
import io.opentelemetry.sdk.trace.export.SpanExporter
import java.util.concurrent.TimeUnit

private const val TELEMETRY_SDK_EXPORT_TIMESTAMP = "sumologic.telemetry.sdk.export_timestamp"

internal class SumoLogicOtlpTraceExporter(private val delegate: SpanExporter): SpanExporter {
    override fun export(spans: MutableCollection<SpanData>): CompletableResultCode {
        val exportTimestampMs = TimeUnit.MILLISECONDS.convert(Clock.getDefault().now(), TimeUnit.NANOSECONDS)
        val extendedResources = mutableMapOf<Resource, Resource>()
        val extraAttributes = Attributes.builder()
            .put(TELEMETRY_SDK_EXPORT_TIMESTAMP, exportTimestampMs)
            .build()
        val extraResource = Resource.create(extraAttributes)

        class CustomSpanData(delegate: SpanData): DelegatingSpanData(delegate) {
            override fun getResource(): Resource {
                val resource = super.getResource()
                val extendedResource = extendedResources[resource]
                if (extendedResource != null) {
                    return extendedResource
                }
                val newResource = resource.merge(extraResource)
                extendedResources[resource] = newResource
                return newResource
            }
        }

        val extendedSpans = spans.map { CustomSpanData(it) }
        return delegate.export(extendedSpans)
    }

    override fun flush(): CompletableResultCode {
        return delegate.flush()
    }

    override fun shutdown(): CompletableResultCode {
        return delegate.shutdown()
    }
}