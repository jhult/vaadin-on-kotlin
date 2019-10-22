package eu.vaadinonkotlin.vaadin10.sql2o

import com.github.vokorm.Entity
import com.github.vokorm.db
import com.github.vokorm.findById
import com.vaadin.flow.data.binder.Binder
import com.vaadin.flow.data.binder.Result
import com.vaadin.flow.data.binder.ValueContext
import com.vaadin.flow.data.converter.Converter

/**
 * Converts an entity to its ID and back. Useful for combo boxes which shows a list of entities as their options while being bound to a
 * field containing ID of that entity.
 * @param T the type of the entity
 * @param ID the type of the ID field of the entity
 */
class EntityToIdConverter2<ID: Any, T: Entity<ID>>(val clazz: Class<T>) : Converter<T?, ID?> {
    override fun convertToModel(value: T?, context: ValueContext?): Result<ID?> =
        Result.ok(value?.id)

    override fun convertToPresentation(value: ID?, context: ValueContext?): T? {
        if (value == null) return null
        return db { con.findById(clazz, value) }
    }
}

/**
 * Converts an entity to its ID and back. Useful for combo boxes which shows a list of entities as their options while being bound to a
 * field containing ID of that entity:
 * ```kotlin
 * data class Category(override var id: Long? = null, var name: String = "") : Entity<Long>
 * data class Review(override var id: Long? = null, var category: Long? = null) : Entity<Long>
 *
 * // editing the Review, we want the user to be able to choose the Review's category
 * val binder = BeanValidationBinder(Review::class.java)
 * categoryBox = comboBox("Choose a category") {
 *     setItemLabelGenerator { it.name }
 *     isAllowCustomValue = false
 *     dataProvider = Category.dataProvider
 *     bind(binder).toId().bind(Review::category)
 * }
 * ```
 */
inline fun <BEAN, ID: Any, reified ENTITY: Entity<ID>> Binder.BindingBuilder<BEAN, ENTITY?>.toId(): Binder.BindingBuilder<BEAN, ID?> =
    withConverter(EntityToIdConverter2(ENTITY::class.java))
