package pt.obvionaoe.broker.db.entity

import pt.obvionaoe.broker.db.model.TagsModel
import java.sql.ResultSet

// database row representation as an object
data class TagsRow(
    val tag: String
)

fun ResultSet.convertToTagsRow(): TagsRow {
    val tag = this.getString(TagsModel.tag.name)!!

    return TagsRow(tag)
}
