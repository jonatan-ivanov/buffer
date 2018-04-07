import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.scheduling.annotation.SchedulingConfigurer
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
import org.springframework.scheduling.config.ScheduledTaskRegistrar
import org.springframework.web.bind.annotation.*

import java.util.concurrent.ConcurrentHashMap

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE

@Grab('spring-boot-starter-actuator')

/**
 * @author Jonatan Ivanov
 */
@RestController
@EnableScheduling
class Main {
    private BufferFactory bufferFactory
    private Map<String, Buffer> buffers

    Main(BufferProperties bufferProperties, BufferFactory bufferFactory) {
        this.bufferFactory = bufferFactory
        this.buffers = bufferProperties.bufferList
                .collect { bufferFactory.createBuffer(it) }
                .collectEntries(new ConcurrentHashMap(), { [(it.name): it] })
    }

    @GetMapping(path = '/buffers', produces = APPLICATION_JSON_VALUE)
    def buffers() {
        return buffers.collectEntries { [(it.key): it.value.stats()] }
    }

    @PostMapping(path = '/buffers', consumes= APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    def create(@RequestBody BufferProperty bufferProperty) {
        Buffer buffer = bufferFactory.createBuffer(bufferProperty)
        buffers.put(buffer.name, buffer)

        return buffer.stats()
    }

    // TODO: This will not stop the tasks which are in progress, buffer need to keep track of Futures and cancel them
    @DeleteMapping(path = '/buffers', produces = APPLICATION_JSON_VALUE)
    void deleteAll() {
        buffers.clear()
    }

    @GetMapping(path = '/buffers/clear', produces = APPLICATION_JSON_VALUE)
    void clearAll() {
        buffers.each { it.value.clear() }
    }

    @GetMapping(path = '/buffers/{name}', produces = APPLICATION_JSON_VALUE)
    def buffer(@PathVariable name) {
        return buffers.get(name).stats()
    }

    @DeleteMapping(path = '/buffers/{name}', produces = APPLICATION_JSON_VALUE)
    void delete(@PathVariable name) {
        buffers.remove(name)
    }

    @GetMapping(path = '/buffers/{name}/head', produces = APPLICATION_JSON_VALUE)
    def poll(@PathVariable name) {
        return buffers.get(name).poll()
    }

    @GetMapping(path = '/buffers/{name}/clear', produces = APPLICATION_JSON_VALUE)
    void clear(@PathVariable name) {
        buffers.get(name).clear()
    }

    @Scheduled(fixedRate = 1_000L)
    void maintain() {
        buffers.each { it.value.fill() }
    }

    @Configuration
    class SchedulerConfig implements SchedulingConfigurer {
        @Override
        void configureTasks(ScheduledTaskRegistrar scheduledTaskRegistrar) {
            ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler()
            threadPoolTaskScheduler.setPoolSize(1)
            threadPoolTaskScheduler.initialize()

            scheduledTaskRegistrar.setTaskScheduler(threadPoolTaskScheduler)
        }
    }
}
