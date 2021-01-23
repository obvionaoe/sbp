package pt.obvionaoe.broker.db.model

import org.jooq.impl.DSL.*
import org.jooq.impl.SQLDataType
import org.jooq.impl.SQLDataType.VARCHAR
import pt.obvionaoe.broker.db.DatabaseInit.dsl
import java.util.UUID

// a model for the data in the 'publishers' table
object PublishersModel {
    val table = table("publishers")
    val id = field("id", UUID::class.java)
    val tag = field("tag", String::class.java)

    val creationQuery = dsl
        .createTableIfNotExists(table.name)
        .column(id.name, SQLDataType.UUID.nullable(false))
        .column(tag.name, VARCHAR.length(10).nullable(false))
        .constraints(
            constraint("PK_PUBLISHERS").primaryKey(id.name),
            constraint("FK_TAGS").foreignKey(tag.name).references(TagsModel.table)
        ).sql
}
