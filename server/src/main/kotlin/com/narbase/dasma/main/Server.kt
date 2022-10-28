package com.narbase.dasma.main


import com.narbase.dasma.common.auth.setupAuthenticators
import com.narbase.dasma.common.db.DatabaseConnector
import com.narbase.dasma.common.db.migrations.Migrations
import com.narbase.dasma.common.db.migrations.initializeUserMigrations
import com.narbase.dasma.common.exceptions.handleExceptions
import com.narbase.dasma.deployment.appConf
import com.narbase.dasma.domain.admin.setupAdminRoutes
import com.narbase.dasma.domain.client.setupClientRoutes
import com.narbase.dasma.domain.user.setupUserRoutes
import com.narbase.dasma.main.files.filesWithThumbnailsGenerator
import com.narbase.dasma.main.properties.VersionProperties
import com.narbase.dasma.main.provisioning.registerFirstAdmin
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.serialization.gson.*
import io.ktor.server.application.*
import io.ktor.server.config.*
import io.ktor.server.engine.*
import io.ktor.server.http.content.*
import io.ktor.server.jetty.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.doublereceive.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.event.Level
import java.io.File
import java.text.DateFormat
import java.time.Duration
import io.ktor.server.plugins.cors.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.callid.*
import io.ktor.server.plugins.forwardedheaders.*
import io.ktor.server.request.*
import io.ktor.server.plugins.compression.*
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.partialcontent.*

/*
 * Copyright 2017-2020 Narbase technologies and contributors. Use of this source code is governed by the MIT License.
 */

object Server {
    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    fun run() {
        logger.info("Launching server. App version: ${VersionProperties.versionName}, ${VersionProperties.versionNumber}")
        val appConfig = appConf
        printHeader(appConfig)
        DatabaseConnector.connect()
        initializeUserMigrations()
        Migrations.migrate()
        registerFirstAdmin()

        val serverPort = appConfig.propertyOrNull("ktor.deployment.port")?.getString()?.toInt()
            ?: throw RuntimeException("Port not found")

        val server = embeddedServer(Jetty,
            applicationEngineEnvironment {
                watchPaths = listOf("classes")
                config = appConfig
                module {
                    appModule()
                }
                connector {
                    port = serverPort
                    host = "0.0.0.0"
                }
            }
        )

        server.start(wait = true)
    }


    private fun printHeader(config: HoconApplicationConfig) {
        config.propertyOrNull("header")?.getString()?.let {
            println(it)
        }
    }

    private fun Application.appModule() {
        val config = appConf
        val jwtIssuer = config.property("jwt.domain").getString()
        val jwtAudience = config.property("jwt.audience").getString()
        val jwtRealm = config.property("jwt.realm").getString()

        enableCors()
        install(Compression)
        install(PartialContent) {
            maxRangeCount = 10
        }
        install(DoubleReceive)
        install(CallId)
        install(CallLogging) {
            level = Level.TRACE
            format { call ->
                val userAgent = call.request.headers["User-Agent"]
                val status = call.response.status()
                val httpMethod = call.request.httpMethod.value
                val uri = call.request.uri
                val ip = call.request.origin.remoteHost
                call.application.log.info("$status: $httpMethod - $uri")
                runBlocking {
                    var body = call.receiveNullable<String>()
                    if (body?.contains("password") == true || body?.contains("token") == true) {
                        body = "***"
                    }

                    "$status: $httpMethod - $uri - IP: $ip - User agent: $userAgent ${
                        if (body.isNullOrBlank().not()) "- Body: $body" else ""
                    }"
                }
            }
            filter { call -> call.request.path().startsWith("/") }
            callIdMdc("call-id")
        }

        install(ContentNegotiation) {
            gson {
                setDateFormat(DateFormat.LONG)
                setPrettyPrinting()
            }
        }
        install(WebSockets)
        install(ForwardedHeaders)
        install(XForwardedHeaders)

        setupAuthenticators(jwtRealm, jwtIssuer, jwtAudience)

        handleExceptions()
        routing {
            setupClientRoutes(jwtIssuer, jwtAudience)
            setupAdminRoutes()
            setupUserRoutes()
            createDirectoriesIfMissing("files", "web")
            static("files") {
                filesWithThumbnailsGenerator("files")
            }
            static("voiceNotes") {
                files("files/voiceNotes")
            }
            static("public") {
                files("web/public")
            }
            static("js") {
                files("web/js")
            }
            static("fonts") {
                files("web/fonts")
            }
            static("/") {
                file("web.js", "web/web.js")
            }

            get("/{path...}") {
                call.respondFile(File("./web/index.html"))
            }
        }
    }

    private fun createDirectoriesIfMissing(vararg fileNames: String) {
        fileNames.forEach { name ->
            val directory = File(name)
            if (directory.exists().not()) {
                directory.mkdir()
            }
        }

    }

    private fun Application.enableCors() {
        install(CORS) {
            allowMethod(HttpMethod.Options)
            allowMethod(HttpMethod.Put)
            anyHost()
            allowHeader("Authorization")
            allowHeader("DNT")
            allowHeader("X-CustomHeader")
            allowHeader("Keep-Alive")
            allowHeader("User-Agent")
            allowHeader("X-Requested-With")
            allowHeader("If-Modified-Since")
            allowHeader("Cache-Control")
            allowHeader("Content-Type")
            allowHeader("Content-Range")
            allowHeader("Accept-Ranges")
            allowHeader("Range")
            allowHeader("Client-Language")

            allowCredentials = true
            maxAgeInSeconds = Duration.ofDays(1).seconds
        }
    }

}
