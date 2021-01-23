package pt.obvionaoe.broker.db.entity

import pt.obvionaoe.broker.db.model.PublishersModel
import java.sql.ResultSet
import java.util.*

// database row representation as an object
data class PublishersRow(
    val id: UUID,
    val tag: String
)

fun ResultSet.convertToPublishersRow(): PublishersRow {
    val id = UUID.fromString(this.getString(PublishersModel.id.name)!!)
    val tag = this.getString(PublishersModel.tag.name)!!

    return PublishersRow(id, tag)
}
