package ru.pulsar.jenkins.library.configuration

import com.fasterxml.jackson.annotation.JsonProperty

enum InitMethod {
    @JsonProperty("fromSource")
    fromSource,

    @JsonProperty("fromInternet")
    fromInternet

}