package eu.vaadinonkotlin.restclient

import com.github.mvysny.dynatest.DynaTest
import com.github.mvysny.dynatest.expectList
import com.github.mvysny.dynatest.expectThrows
import io.javalin.Javalin
import okhttp3.OkHttpClient
import okhttp3.Request
import org.intellij.lang.annotations.Language
import java.io.FileNotFoundException
import java.io.IOException
import kotlin.test.expect

data class Person(var name: String, var surname: String)

class OkHttpClientUtilsTest : DynaTest({
    fun String.get(): Request = Request.Builder().url(this).get().build()
    lateinit var request: Request
    beforeEach {
        OkHttpClientVokPlugin().init()
        request = "http://localhost:54444/foo".get()
    }
    lateinit var content: String
    fun client(): OkHttpClient = OkHttpClientVokPlugin.okHttpClient!!
    afterEach { OkHttpClientVokPlugin().destroy() }
    lateinit var javalin: Javalin
    beforeEach {
        javalin = Javalin.create()
                .disableStartupBanner()
                .get("foo") { ctx -> ctx.result(content) }
                .get("fail") { ctx -> throw RuntimeException() }
                .start(54444)
    }
    afterEach { javalin.stop() }

    test("json") {
        content = """{"name":"John", "surname":"Doe"}"""
        expect(Person("John", "Doe")) { client().exec(request) { it.json(Person::class.java) } }
    }

    test("404") {
        expectThrows(FileNotFoundException::class, "404: Not found (http://localhost:54444/bar)") {
            client().exec("http://localhost:54444/bar".get()) {}
        }
    }

    test("500") {
        expectThrows(IOException::class, "500: Internal server error (http://localhost:54444/fail)") {
            client().exec("http://localhost:54444/fail".get()) {}
        }
    }

    group("jsonArray") {
        test("empty") {
            content = """[]"""
            expectList() { client().exec(request) { it.jsonArray(Person::class.java) } }
        }
        test("simple") {
            content = """[{"name":"John", "surname":"Doe"}]"""
            expectList(Person("John", "Doe")) { client().exec(request) { it.jsonArray(Person::class.java) } }
        }
    }

    group("jsonMap") {
        test("empty") {
            content = """{}"""
            expect(mapOf()) { client().exec(request) { it.jsonMap(Person::class.java) } }
        }
        test("simple") {
            content = """{"director": {"name":"John", "surname":"Doe"}}"""
            expect(mapOf("director" to Person("John", "Doe"))) { client().exec(request) { it.jsonMap(Person::class.java) } }
        }
    }
})
