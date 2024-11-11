package com.ejebring.cf

import com.ejebring.cf.plugins.*
import io.ktor.resources.*
import io.ktor.server.application.*
import io.ktor.server.engine.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureSecurity()
    configureHTTP()
    configureSerialization()
    configureDatabases()
    configureRouting()
}