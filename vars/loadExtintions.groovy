import ru.pulsar.jenkins.library.configuration.JobConfiguration
import ru.pulsar.jenkins.library.ioc.ContextRegistry
import ru.pulsar.jenkins.library.steps.loadExtintions

def call(JobConfiguration config) {
    ContextRegistry.registerDefaultContext(this)

    def loadExtintions = new loadExtintions(config)
    loadExtintions.run()
}