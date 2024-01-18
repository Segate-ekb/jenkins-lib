package ru.pulsar.jenkins.library.steps

import ru.pulsar.jenkins.library.IStepExecutor
import ru.pulsar.jenkins.library.configuration.JobConfiguration
import ru.pulsar.jenkins.library.configuration.SourceFormat
import ru.pulsar.jenkins.library.configuration.InitMethod
import ru.pulsar.jenkins.library.configuration.InitInfoBaseOptions.Extension
import ru.pulsar.jenkins.library.ioc.ContextRegistry
import ru.pulsar.jenkins.library.utils.Logger
import ru.pulsar.jenkins.library.utils.VRunner

class GetExtensions implements Serializable {

    public static final String EXTENSIONS_OUT_DIR = 'build/out/cfe'

    private final JobConfiguration config;
    private final String vrunnerPath = VRunner.getVRunnerPath();
    private final String srcDir = getSrcDir();

    GetExtensions(JobConfiguration config) {
        this.config = config
    }

    def run() {
        IStepExecutor steps = ContextRegistry.getContext().getStepExecutor()

        Logger.printLocation()

        steps.installLocalDependencies();

        Logger.println("Сборка расширений")

        config.initInfoBaseOptions.extensions.each {
            if (it.initMethod == InitMethod.SOURCE) {
                Logger.println("Сборка расширения ${it.name} из исходников")
                buildExtension(it)
            } else {
                Logger.println("Загрузка расширения ${it.name} из интернета по ссылке ${it.path}")
                loadExtension(it)
            }
        }
    }

    private void buildExtension(Extension extension) {

        String name = extension.name;

        def compileExtCommand = "$vrunnerPath compileexttocfe --src $srcDir/${name} --out $EXTENSIONS_OUT_DIR/${name}.cfe"
        List<String> logosConfig = ["LOGOS_CONFIG=$config.logosConfig"]
        steps.withEnv(logosConfig) {
            VRunner.exec(compileExtCommand)
        }
    }

    private void loadExtension(Extension extension) {
        String pathToExtension = "$env.WORKSPACE/${EXTENSIONS_OUT_DIR}/${extension.name}.cfe"
        FilePath localPathToExtension = FileUtils.getFilePath(pathToExtension)
        localPathToExtension.copyFrom(new URL(extension.path))
    }

    private String getSrcDir() {
        if (config.sourceFormat == SourceFormat.EDT) {
            def env = steps.env();

            sourceDirName = "$env.WORKSPACE/$EdtToDesignerFormatTransformation.EXTENSION_DIR"
            // распакуем расширения
            steps.unstash(EdtToDesignerFormatTransformation.EXTENSION_ZIP_STASH)
            steps.unzip(sourceDirName, EdtToDesignerFormatTransformation.EXTENSION_ZIP)

            return sourceDirName;
        } else {
            return config.srcDir;
        }
    }
}
