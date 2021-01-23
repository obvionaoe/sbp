package pt.obvionaoe.broker.db.model

import org.jooq.impl.DSL.*
import org.jooq.impl.SQLDataType
import org.jooq.impl.SQLDataType.TIMESTAMP
import org.jooq.impl.SQLDataType.VARCHAR
import pt.obvionaoe.broker.db.DatabaseInit.dsl
import java.sql.Timestamp
import java.util.UUID

// a model for the data in the 'messages' table
object MessagesModel {
    val table = table("messages")
    val id = field("id", UUID::class.java)
    val tag = field("tag", String::class.java)
    val message = field("message", String::class.java)
    val publisher = field("publisher", UUID::class.java)
    val timestamp = field("timestamp", Timestamp::class.java)

    val creationQuery = dsl
        .createTableIfNotExists(table.name)
        .column(id.name, SQLDataType.UUID.nullable(false))
        .column(tag.name, VARCHAR.length(10).nullable(false))
        .column(message.name, VARCHAR.length(50).nullable(false))
        .column(publisher.name, SQLDataType.UUID.nullable(false))
        .column(timestamp.name, TIMESTAMP)
        .constraints(
            constraint("PK_MESSAGES").primaryKey(id.name),
            constraint("FK_TAG").foreignKey(tag.name).references(TagsModel.table),
            constraint("FK_PUBLISHER").foreignKey(publisher.name).references(PublishersModel.table)
        ).sql
}
