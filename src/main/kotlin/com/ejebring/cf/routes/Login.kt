import com.ejebring.cf.Login
import com.ejebring.cf.UserService
import com.ejebring.cf.plugins.newToken
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*


suspend fun login(call: RoutingCall, userService: UserService) {
    val login = call.receive<Login>()
    try {
        login.validate()
    } catch (e: IllegalArgumentException) {
        call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid user")
        return
    }

    val user = userService.findByUsername(login.username)

    if (user == null || user.passcode != login.passcode) {
        call.respond(HttpStatusCode.Unauthorized, "Incorrect username or password")
        return
    }

    val token = newToken(user.name)
    call.respond(HttpStatusCode.OK, token)
}