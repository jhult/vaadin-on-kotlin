package eu.vaadinonkotlin.vaadin10

import com.vaadin.flow.component.UI
import com.vaadin.flow.server.VaadinRequest
import com.vaadin.flow.server.VaadinResponse
import com.vaadin.flow.server.VaadinService
import com.vaadin.flow.server.VaadinSession
import java.io.Serializable
import javax.servlet.http.Cookie
import kotlin.reflect.KClass

/**
 * A namespace object for attaching your session objects.
 *
 * WARNING: you can only read the property while holding the Vaadin UI lock (that is, there is current session available).
 */
object Session {

    /**
     * Returns the current [VaadinSession]; fails if there is no session, most probably since we are not in the UI thread.
     */
    val current: VaadinSession get() = VaadinSession.getCurrent() ?: throw IllegalStateException("Not in UI thread")

    /**
     * Returns the attribute stored in this session under given key.
     * @param key the key
     * @return the attribute value, may be null
     */
    operator fun get(key: String): Any? = current.getAttribute(key)

    /**
     * Returns the attribute stored in this session under given key.
     * @param key the key
     * @return the attribute value, may be null
     */
    operator fun <T: Any> get(key: KClass<T>): T? = current.getAttribute(key.java)

    /**
     * Stores given value under given key in a session. Removes the mapping if value is null
     * @param key the key
     * @param value the value to store, may be null if
     */
    operator fun set(key: String, value: Any?) = current.setAttribute(key, value)

    /**
     * Stores given value under given key in a session. Removes the mapping if value is null
     * @param key the key
     * @param value the value to store, may be null if
     */
    operator fun <T: Any> set(key: KClass<T>, value: T?) = current.setAttribute(key.java, value)

    /**
     * Retrieves the class stored under its class name from the session; if it's not yet there calls [defaultValue] block to create it.
     *
     * Use this function to create session-bound services such as:
     * ```
     * val Session.loggedInUser: LoggedInUser get() = getOrPut { LoggedInUser() }
     * ```
     * @return the session-bound instance
     */
    inline fun <reified T: Serializable> getOrPut(noinline defaultValue: () -> T): T = getOrPut(T::class, defaultValue)

    /**
     * Retrieves the class stored under its class name as the [key] from the session; if it's not yet there calls [defaultValue] block to create it.
     * @return the session-bound instance
     */
    fun <T: Serializable> getOrPut(key: KClass<T>, defaultValue: ()->T): T {
        val value = get(key)
        return if (value == null) {
            val answer = defaultValue()
            set(key, answer)
            answer
        } else {
            value
        }
    }
}

val currentRequest: VaadinRequest get() = VaadinService.getCurrentRequest() ?: throw IllegalStateException("No current request")
val currentResponse: VaadinResponse get() = VaadinService.getCurrentResponse() ?: throw IllegalStateException("No current response")

/**
 * You can use `Cookies["mycookie"]` to retrieve a cookie named "mycookie" (or null if no such cookie exists.
 * You can also use `Cookies += cookie` to add a pre-created cookie to a session.
 */
object Cookies {
    /**
     * Finds a cookie by name.
     * @param name cookie name
     * @return cookie or null if there is no such cookie.
     */
    operator fun get(name: String): Cookie? = currentRequest.cookies?.firstOrNull { it.name == name }

    /**
     * Overwrites given cookie, or deletes it.
     * @param name cookie name
     * @param cookie the cookie to overwrite. If null, the cookie is deleted.
     */
    operator fun set(name: String, cookie: Cookie?) {
        if (cookie == null) {
            val newCookie = Cookie(name, null)
            newCookie.maxAge = 0  // delete immediately
            newCookie.path = "/"
            currentResponse.addCookie(newCookie)
        } else {
            currentResponse.addCookie(cookie)
        }
    }

    /**
     * Deletes cookie with given [name]. Does nothing if there is no such cookie.
     */
    fun delete(name: String) = set(name, null)
}

infix operator fun Cookies.plusAssign(cookie: Cookie) = set(cookie.name, cookie)

infix operator fun Cookies.minusAssign(cookie: Cookie) = set(cookie.name, null)
