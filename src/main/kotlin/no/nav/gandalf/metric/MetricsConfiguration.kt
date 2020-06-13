package no.nav.gandalf.metric

import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics
import io.micrometer.core.instrument.binder.logging.LogbackMetrics
import io.micrometer.core.instrument.binder.system.FileDescriptorMetrics
import io.micrometer.core.instrument.binder.system.ProcessorMetrics
import io.micrometer.core.instrument.binder.system.UptimeMetrics
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class MetricsConfiguration {

    @Bean
    fun threadMetrics(): JvmThreadMetrics? {
        return JvmThreadMetrics()
    }

    @Bean
    fun classLoaderMetrics(): ClassLoaderMetrics? {
        return ClassLoaderMetrics()
    }

    @Bean
    fun jvmGcMetrics(): JvmGcMetrics? {
        return JvmGcMetrics()
    }

    @Bean
    fun processorMetrics(): ProcessorMetrics? {
        return ProcessorMetrics()
    }

    @Bean
    fun jvmMemoryMetrics(): JvmMemoryMetrics? {
        return JvmMemoryMetrics()
    }

    @Bean
    fun logbackMetrics(): LogbackMetrics? {
        return LogbackMetrics()
    }

    @Bean
    fun uptimeMetrics(): UptimeMetrics? {
        return UptimeMetrics()
    }

    @Bean
    fun fileDescriptorMetrics(): FileDescriptorMetrics? {
        return FileDescriptorMetrics()
    }
}
