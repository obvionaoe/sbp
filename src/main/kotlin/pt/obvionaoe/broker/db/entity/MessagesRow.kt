package pt.obvionaoe.broker.db.entity

import pt.obvionaoe.broker.db.model.MessagesModel
import pt.obvionaoe.sbp.TaggedMessage
import java.sql.ResultSet
import java.text.SimpleDateFormat
import java.util.*

// database row representation as an object
data class MessagesRow(
    val id: UUID,
    val tag: String,
    val message: String,
    val publisher: UUID,
    val timestamp: Long
)

fun ResultSet.convertToMessagesRow(): MessagesRow {
    val id = UUID.fromString(this.getString(MessagesModel.id.name)!!)
    val tag = this.getString(MessagesModel.tag.name)!!
    val message = this.getString(MessagesModel.message.name)!!
    val publisher = UUID.fromString(this.getString(MessagesModel.publisher.name)!!)
    val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(this.getString(MessagesModel.timestamp.name)).time

    return MessagesRow(id, tag, message, publisher, timestamp)
}

fun MessagesRow.toTaggedMessage(): TaggedMessage = TaggedMessage.newBuilder().setMessage(this.message).setTag(this.tag).setTimestamp(this.timestamp).build()
