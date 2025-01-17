package ru.pulsar.jenkins.library.steps

import ru.pulsar.jenkins.library.IStepExecutor
import ru.pulsar.jenkins.library.configuration.JobConfiguration
import ru.pulsar.jenkins.library.configuration.SourceFormat
import ru.pulsar.jenkins.library.ioc.ContextRegistry
import ru.pulsar.jenkins.library.utils.Logger
import ru.pulsar.jenkins.library.utils.VRunner
import ru.pulsar.jenkins.library.steps.CreateInfobase

class InitFromFiles implements Serializable {

    private final JobConfiguration config;

    InitFromFiles(JobConfiguration config) {
        this.config = config
    }

    def run() {
        IStepExecutor steps = ContextRegistry.getContext().getStepExecutor()

        Logger.printLocation()

        if (!config.infoBaseFromFiles()) {
            Logger.println("init infoBase from files is disabled")
            return
        }

        steps.installLocalDependencies();

        Logger.println("Распаковка файлов")

        String srcDir;

        if (config.sourceFormat == SourceFormat.EDT) {
            def env = steps.env();
            srcDir = "$env.WORKSPACE/$EdtToDesignerFormatTransformation.CONFIGURATION_DIR"

            steps.unstash(EdtToDesignerFormatTransformation.CONFIGURATION_ZIP_STASH)
            steps.unzip(srcDir, EdtToDesignerFormatTransformation.CONFIGURATION_ZIP)
        } else {
            srcDir = config.srcDir;
        }

        def createInfobase = new CreateInfobase(config)
        createInfobase.run()

        Logger.println("Выполнение загрузки конфигурации из файлов")
        String vrunnerPath = VRunner.getVRunnerPath();
        def command = "$vrunnerPath update-dev --src $srcDir --ibconnection \"/F./build/ib\""

        def options = config.initInfoBaseOptions

        String vrunnerSettings = options.vrunnerSettings
        if (vrunnerSettings && steps.fileExists(vrunnerSettings)) {
            command += " --settings $vrunnerSettings"
        }

        VRunner.exec(command)
    }
}
