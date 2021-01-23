package pt.obvionaoe.broker.db.model

import org.jooq.impl.DSL.*
import org.jooq.impl.SQLDataType.VARCHAR
import pt.obvionaoe.broker.db.DatabaseInit.dsl

// a model for the data in the 'tags' table
object TagsModel {
    val table = table("tags")
    val tag = field("tag", String::class.java)

    val creationQuery = dsl
        .createTableIfNotExists(table.name)
        .column(tag.name, VARCHAR.length(10).nullable(false))
        .constraints(
            constraint("PK_TAGS").primaryKey(tag.name),
        ).sql
}
