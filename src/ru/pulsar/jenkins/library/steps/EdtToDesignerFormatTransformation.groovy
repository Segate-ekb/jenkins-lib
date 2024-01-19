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
        def workspaceDir = FileUtils.getFilePath("$env.WORKSPACE/$WORKSPACE")

        def projectWorkspaceDir = FileUtils.getFilePath("$workspaceDir/cf")
        def projectDir = FileUtils.getFilePath("$env.WORKSPACE/$srcDir")
        def configurationRoot = FileUtils.getFilePath("$env.WORKSPACE/$CONFIGURATION_DIR")


        def extensionRoot = FileUtils.getFilePath("$env.WORKSPACE/$EXTENSION_DIR")
        def edtVersionForRing = EDT.ringModule(config)

        steps.deleteDir(workspaceDir)

        transformConfiguration(steps, projectDir, projectWorkspaceDir, configurationRoot, edtVersionForRing)
        transformExtensions(steps, workspaceDir, extensionRoot, edtVersionForRing)
    }

    private void transformConfiguration(IStepExecutor steps, def projectDir, def projectWorkspaceDir, def configurationRoot, String edtVersionForRing) {
        steps.deleteDir(configurationRoot)

        Logger.println("Конвертация исходников из формата EDT в формат Конфигуратора")

        def ringCommand = "ring $edtVersionForRing workspace export --workspace-location \"$projectWorkspaceDir\" --project \"$projectDir\" --configuration-files \"$configurationRoot\""

        def ringOpts = [Constants.DEFAULT_RING_OPTS]
        steps.withEnv(ringOpts) {
            steps.cmd(ringCommand)
        }

        steps.zip(CONFIGURATION_DIR, CONFIGURATION_ZIP)
        steps.stash(CONFIGURATION_ZIP_STASH, CONFIGURATION_ZIP)
    }

    private void transformExtensions(IStepExecutor steps, def workspaceDir, def extensionRoot, String edtVersionForRing) {
        steps.deleteDir(extensionRoot)

        config.initInfoBaseOptions.extensions.each {
            Logger.println("Конвертация исходников расширения ${it.name} из формата EDT в формат Конфигуратора")

            def env = steps.env();
            def projectDir = FileUtils.getFilePath("$env.WORKSPACE/${it.path}")
            def currentExtensionWorkspaceDir = FileUtils.getFilePath("$workspaceDir/cfe/${it.name}")

            def ringCommand = "ring $edtVersionForRing workspace export --workspace-location \"$currentExtensionWorkspaceDir\" --project \"$projectDir\" --configuration-files \"$extensionRoot/${it.name}\""

            def ringOpts = [Constants.DEFAULT_RING_OPTS]
            steps.withEnv(ringOpts) {
                steps.cmd(ringCommand)
            }
        }
        steps.zip(EXTENSION_DIR, EXTENSION_ZIP)
        steps.stash(EXTENSION_ZIP_STASH, EXTENSION_ZIP)
    }

}
