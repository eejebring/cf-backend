package com.ejebring.cf

import com.ejebring.cf.plugins.configureHTTP
import com.ejebring.cf.plugins.configureRouting
import com.ejebring.cf.plugins.configureSecurity
import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureSecurity()
    configureHTTP()
    configureRouting()
}