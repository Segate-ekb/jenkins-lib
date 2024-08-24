package ru.pulsar.jenkins.library.steps

import ru.pulsar.jenkins.library.IStepExecutor
import ru.pulsar.jenkins.library.configuration.JobConfiguration
import ru.pulsar.jenkins.library.ioc.ContextRegistry
import ru.pulsar.jenkins.library.utils.Logger
import ru.pulsar.jenkins.library.utils.VRunner
import hudson.FilePath
import ru.pulsar.jenkins.library.utils.FileUtils

class CreateInfobase implements Serializable {

    private final JobConfiguration config;

    CreateInfobase(JobConfiguration config) {
        this.config = config
    }

    def run() {
        IStepExecutor steps = ContextRegistry.getContext().getStepExecutor()

        Logger.printLocation()

        def env = steps.env()

        steps.installLocalDependencies();

        String baseDBPath = config.initInfoBaseOptions.baseDBPath
        if (baseDBPath == '') {
            // Не указан путь к базе данных, создадим пустую базу данных.
            createBase()
        } else if (baseDBPath.endsWith('.1CD')) {
            // Это файл базы данных 1С, просто скопируем его.
            String pathToInfobase = "$env.WORKSPACE/build/ib/1Cv8.1CD"
            FileUtils.loadFile(baseDBPath, env, pathToInfobase)
        } else if (baseDBPath.endsWith('.dt')) {
            // Это файл дампа БД, скопируем его и создадим БД.
            String pathToDt = "$env.WORKSPACE/build/tmp/dump.dt"
            FileUtils.loadFile(baseDBPath, env, pathToDt)
            createBase('build/tmp/dump.dt')
        } else {
            Logger.println("Неизвестный формат базы данных. Поддерживаются только .1CD и .dt")
        }

    }

    private void createBase(String dtPath = '') {
        Logger.println("Создание информационной базы")
        String vrunnerPath = VRunner.getVRunnerPath();
        def initCommand = "$vrunnerPath init-dev  --ibconnection \"/F./build/ib\""
        if (dtPath) {
            initCommand += " --dt $dtPath"
        }

        def options = config.initInfoBaseOptions

        String vrunnerSettings = options.vrunnerSettings
        if (vrunnerSettings && steps.fileExists(vrunnerSettings)) {
            command += " --settings $vrunnerSettings"
        }
        
        VRunner.exec(initCommand)
    }
}
