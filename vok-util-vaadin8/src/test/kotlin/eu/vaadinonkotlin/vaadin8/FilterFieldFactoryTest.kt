package eu.vaadinonkotlin.vaadin8

import com.github.mvysny.dynatest.DynaTest
import com.github.mvysny.karibudsl.v8.DateRangePopup
import com.github.mvysny.kaributesting.v8.MockVaadin
import com.github.mvysny.vokdataloader.Filter
import com.github.mvysny.vokdataloader.FullTextFilter
import com.vaadin.data.HasValue
import com.vaadin.ui.ComboBox
import com.vaadin.ui.TextField
import kotlin.test.expect

class FilterFieldFactoryTest : DynaTest({
    beforeEach { MockVaadin.setup() }
    afterEach { MockVaadin.tearDown() }

    test("smoke test") {
        val factory: FilterFieldFactory<Person, Filter<Person>> = vokDefaultFilterFieldFactory()
        expect<Class<*>>(ComboBox::class.java) { factory.createFieldFor(Person::alive)!!.javaClass }
        expect<Class<*>>(TextField::class.java) { factory.createFieldFor(Person::personName)!!.javaClass }
        expect<Class<*>>(NumberFilterPopup::class.java) { factory.createFieldFor(Person::age)!!.javaClass }
        expect<Class<*>>(DateFilterPopup::class.java) { factory.createFieldFor(Person::created)!!.javaClass }
        expect<Class<*>>(DateFilterPopup::class.java) { factory.createFieldFor(Person::dateOfBirth)!!.javaClass }
    }

    test("delegate pattern works") {
        var factory: FilterFieldFactory<Person, Filter<Person>> = vokDefaultFilterFieldFactory()
        factory = factory.withFullTextOn(Person::personName)
        val filterComponent: HasValue<String?> = factory.createFieldFor(Person::personName)!!
        val filter: FullTextFilter<Person> =
                factory.createFilter<String?>("foo bar baz", filterComponent, Person::personName.definition) as FullTextFilter<Person>
        expect(setOf("foo", "bar", "baz")) { filter.words }
        expect("personName") { filter.propertyName }
    }
})
