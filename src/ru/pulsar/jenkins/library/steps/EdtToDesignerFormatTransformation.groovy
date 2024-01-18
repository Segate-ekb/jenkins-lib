package ru.pulsar.jenkins.library.steps


import ru.pulsar.jenkins.library.IStepExecutor
import ru.pulsar.jenkins.library.configuration.JobConfiguration
import ru.pulsar.jenkins.library.configuration.SourceFormat
import ru.pulsar.jenkins.library.ioc.ContextRegistry
import ru.pulsar.jenkins.library.utils.Constants
import ru.pulsar.jenkins.library.utils.EDT
import ru.pulsar.jenkins.library.utils.FileUtils
import ru.pulsar.jenkins.library.utils.Logger

class EdtToDesignerFormatTransformation implements Serializable {

    public static final String WORKSPACE = 'build/edt-workspace'
    public static final String CONFIGURATION_DIR = 'build/cfg'
    public static final String CONFIGURATION_ZIP = 'build/cfg.zip'
    public static final String CONFIGURATION_ZIP_STASH = 'cfg-zip'
    public static final String EXTENSION_DIR = 'build/cfe_src'
    public static final String EXTENSION_ZIP = 'build/cfe_src.zip'
    public static final String EXTENSION_ZIP_STASH = 'cfe_src-zip'

    private final JobConfiguration config;

    EdtToDesignerFormatTransformation(JobConfiguration config) {
        this.config = config
    }

    def run() {
        IStepExecutor steps = ContextRegistry.getContext().getStepExecutor()

        Logger.printLocation()

        if (config.sourceFormat != SourceFormat.EDT) {
            Logger.println("SRC is not in EDT format. No transform is needed.")
            return
        }

        def env = steps.env();

        def srcDir = config.srcDir
        def projectDir = FileUtils.getFilePath("$env.WORKSPACE/$srcDir")
        def workspaceDir = FileUtils.getFilePath("$env.WORKSPACE/$WORKSPACE")
        def configurationRoot = FileUtils.getFilePath("$env.WORKSPACE/$CONFIGURATION_DIR")
        def extensionRoot = FileUtils.getFilePath("$env.WORKSPACE/$EXTENSION_DIR")
        def edtVersionForRing = EDT.ringModule(config)

        steps.deleteDir(workspaceDir)

        transformConfiguration(steps, projectDir, workspaceDir, configurationRoot, edtVersionForRing)
        // TODO: Переделать когда пойму как выглядит расширение в EDT
       // transformExtensions(steps, projectDir, workspaceDir, extensionRoot, edtVersionForRing)
    }

    private void transformConfiguration(IStepExecutor steps, String projectDir, String workspaceDir, String configurationRoot, String edtVersionForRing) {
        steps.deleteDir(configurationRoot)

        Logger.println("Конвертация исходников из формата EDT в формат Конфигуратора")

        def ringCommand = "ring $edtVersionForRing workspace export --workspace-location \"$workspaceDir\" --project \"$projectDir\" --configuration-files \"$configurationRoot\""

        def ringOpts = [Constants.DEFAULT_RING_OPTS]
        steps.withEnv(ringOpts) {
            steps.cmd(ringCommand)
        }

        steps.zip(CONFIGURATION_DIR, CONFIGURATION_ZIP)
        steps.stash(CONFIGURATION_ZIP_STASH, CONFIGURATION_ZIP)
    }

    // TODO: Переделать когда пойму как выглядит расширение в EDT
    private void transformExtensions(IStepExecutor steps, String projectDir, String workspaceDir, String extensionRoot, String edtVersionForRing) {
        steps.deleteDir(extensionRoot)

        Logger.println("Конвертация исходников расширений из формата EDT в формат Конфигуратора")

        def ringCommand = "ring $edtVersionForRing workspace export --workspace-location \"$workspaceDir\" --project \"$projectDir\" --configuration-files \"$extensionRoot\""

        def ringOpts = [Constants.DEFAULT_RING_OPTS]
        steps.withEnv(ringOpts) {
            steps.cmd(ringCommand)
        }

        steps.zip(EXTENSION_DIR, EXTENSION_ZIP)
        steps.stash(EXTENSION_ZIP_STASH, EXTENSION_ZIP)
    }

}
