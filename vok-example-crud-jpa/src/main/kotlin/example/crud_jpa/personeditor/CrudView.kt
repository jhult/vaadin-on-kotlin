package example.crud_jpa.personeditor

import eu.vaadinonkotlin.vaadin8.jpa.db
import eu.vaadinonkotlin.vaadin8.jpa.deleteById
import eu.vaadinonkotlin.vaadin8.jpa.jpaDataProvider
import com.github.mvysny.karibudsl.v8.*
import com.github.mvysny.karibudsl.v8.ModifierKey.Alt
import com.github.mvysny.karibudsl.v8.ModifierKey.Ctrl
import eu.vaadinonkotlin.vaadin8.jpa.generateFilterComponents
import com.vaadin.event.ShortcutAction.KeyCode.C
import com.vaadin.navigator.View
import com.vaadin.navigator.ViewChangeListener
import com.vaadin.ui.*
import com.vaadin.ui.renderers.ButtonRenderer
import com.vaadin.ui.renderers.LocalDateRenderer
import com.vaadin.ui.renderers.TextRenderer
import java.util.*

/**
 * Demonstrates a CRUD over [Person]. Note how the autoViewProvider automatically discovers your view and assigns a name to it.
 */
@AutoView
class CrudView: Composite(), View {
    override fun enter(event: ViewChangeListener.ViewChangeEvent) {
        personGrid.refresh()
    }

    companion object {
        fun navigateTo() = navigateToView<CrudView>()
    }

    private lateinit var createButton: Button
    private lateinit var personGrid: Grid<Person>

    // you can restrict the values by writing the following expression:
//    private val personGridDS = jpaDataProvider<Person>().and { Person::age between 20..60 }

    private val root = verticalLayout {
        setSizeFull(); isMargin = false
        horizontalLayout {
            createButton = button("Create New Person (Ctrl+Alt+C)") {
                onLeftClick { createOrEditPerson(Person(created = Date())) }
                clickShortcut = Ctrl + Alt + C
            }
            button("Generate testing data") {
                onLeftClick { generateTestingData() }
            }
        }
        // the JPA list demo - shows all instances of a particular JPA entity, allow sorting and filtering
        personGrid = grid(dataProvider = jpaDataProvider()) {
            expandRatio = 1f; setSizeFull()

            // a sample of how to reconfigure a column
            addColumnFor(Person::id) { isSortable = false }
            addColumnFor(Person::name)
            addColumnFor(Person::age)
            addColumnFor(Person::dateOfBirth) {
                setRenderer(LocalDateRenderer())
            }
            addColumnFor(Person::maritalStatus)
            addColumnFor(Person::alive)
            addColumnFor(Person::created) {
                // example of a custom renderer which converts value to a displayable string.
                setRenderer({ it.toString() }, TextRenderer())
            }

            // add additional columns with buttons
            addColumn({ "Show" }, ButtonRenderer<Person>({ event -> PersonView.navigateTo(event.item) }))
            addColumn({ "Edit" }, ButtonRenderer<Person>({ event -> createOrEditPerson(event.item) })).id = "edit"
            addColumn({ "Delete" }, ButtonRenderer<Person>({ event -> deletePerson(event.item.id!!) }))

            // automatically create filters, based on the types of values present in particular columns.
            appendHeaderRow().generateFilterComponents(this, Person::class)
        }
    }

    private fun generateTestingData() {
        db {
            (0..85).forEach {
                em.persist(Person(name = "generated$it", age = it + 15, maritalStatus = MaritalStatus.Single,
                        alive = true, created = Date()))
            }
        }
        personGrid.refresh()
    }

    private fun deletePerson(id: Long) {
        db { em.deleteById<Person>(id) }
        personGrid.refresh()
    }

    private fun createOrEditPerson(person: Person) {
        CreateEditPerson(person).apply {
            addCloseListener { personGrid.refresh() }
            UI.getCurrent().addWindow(this)
        }
    }
}
