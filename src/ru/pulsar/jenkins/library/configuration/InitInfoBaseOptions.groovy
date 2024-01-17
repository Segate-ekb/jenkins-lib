package ru.pulsar.jenkins.library.configuration

import com.cloudbees.groovy.cps.NonCPS
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonPropertyDescription

@JsonIgnoreProperties(ignoreUnknown = true)
class InitInfoBaseOptions implements Serializable {

    @JsonPropertyDescription("""
    Способ инициализации информационной базы.
    Поддерживается три варианта:
        * fromStorage - инициализация информационной базы из хранилища конфигурации;
        * fromSource - инициализация информационной базы из исходников конфигурации;
        * defaultBranchFromStorage - инициализация основной ветки из хранилища конфигурации, остальных - из исходников конфигурации.
    По умолчанию содержит значение "fromStorage".""")
    InitInfoBaseMethod initMethod = InitInfoBaseMethod.FROM_STORAGE;

    @JsonPropertyDescription("Запустить миграцию ИБ")
    Boolean runMigration = true

    @JsonPropertyDescription("""Дополнительные шаги, запускаемые через vrunner.
    В каждой строке передается отдельная команда 
    vrunner и ее аргументы (например, "vanessa --settings ./tools/vrunner.first.json")
    """)
    String[] additionalInitializationSteps


    @JsonPropertyDescription("Массив расширений для загрузки в конфигурацию.")
    Extintion[] extintions;

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class Extintion implements Serializable {
        @JsonPropertyDescription("Имя расширения с которым оно грузится в конфигурацию")
        String name;

        @JsonPropertyDescription("""
        Способ инициализации расширения.
        Поддерживается два варианта:
            * fromSource - инициализация расширения из исходников;
            * fromInternet - скачивание скомпилированного cfe по ссылке.
        """)
        String initMethod;

        @JsonPropertyDescription("""
        Хранит в себе путь к расширению.
            * В случае если выбран initMethod <fromSource> - указывается путь к исходникам расширения.
            * В случае если выбран initMethod <fromInternet> - указывается ссылка на cfe-файл
        """)
        String path;

        @JsonPropertyDescription("Формат исходников конфигурации")
        String sourceFormat;
    }

    @Override
    @NonCPS
    String toString() {
        return "InitInfoBaseOptions{" +
            "initMethod=" + initMethod +
            ", runMigration=" + runMigration +
            ", additionalInitializationSteps=" + additionalInitializationSteps +
            ", extintions=" + extintions +
            '}';
    }
}
