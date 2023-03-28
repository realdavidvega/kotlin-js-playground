import com.typesafe.config.ConfigFactory
import io.ktor.http.HttpMethod
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.compression.Compression
import io.ktor.server.plugins.compression.gzip
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing

fun main() {
    val config = ConfigFactory.load()
    val host = config.getString("ktor.deployment.host")
    val port = config.getInt("ktor.deployment.port")

    embeddedServer(
        factory = Netty,
        port = port,
        host = host,
        module = Application::module
    ).start(wait = true)
}

fun Application.module() {
    plugins()
    routing()
}

fun Application.plugins() {
    install(ContentNegotiation) {
        json()
    }
    install(CORS) {
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Delete)
        anyHost()
    }
    install(Compression) {
        gzip()
    }
}

fun Application.routing() {
    routing {
        get("/hello") {
            call.respondText("Hello, API!")
        }
    }
}
