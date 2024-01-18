package ru.pulsar.jenkins.library.steps

import ru.pulsar.jenkins.library.IStepExecutor
import ru.pulsar.jenkins.library.configuration.JobConfiguration
import ru.pulsar.jenkins.library.configuration.SourceFormat
import ru.pulsar.jenkins.library.ioc.ContextRegistry
import ru.pulsar.jenkins.library.utils.Logger
import ru.pulsar.jenkins.library.utils.VRunner

class LoadExtensions implements Serializable {

    private final JobConfiguration config;

    LoadExtensions(JobConfiguration config) {
        this.config = config
    }

    def run() {
        IStepExecutor steps = ContextRegistry.getContext().getStepExecutor()

        Logger.printLocation()

        List<String> logosConfig = ["LOGOS_CONFIG=$config.logosConfig"]
        steps.withEnv(logosConfig) {

            String vrunnerPath = VRunner.getVRunnerPath();

            steps.catchError {
               
                config.initInfoBaseOptions.extensions.each {
                    Logger.println("Установим расширение ${it.name}")

                    // if (config.sourceFormat == SourceFormat.EDT) {
                    //     def env = steps.env();
                    //     srcDir = "$env.WORKSPACE/$EdtToDesignerFormatTransformation.CONFIGURATION_DIR"

                    //     steps.unstash(EdtToDesignerFormatTransformation.CONFIGURATION_ZIP_STASH)
                    //     steps.unzip(srcDir, EdtToDesignerFormatTransformation.CONFIGURATION_ZIP)
                    // } else {
                    //     srcDir = config.srcDir;
                    // }

                    // VRunner.exec("$vrunnerPath ${it} --ibconnection \"/F./build/ib\"")
                }
            }
        }
    }
}
