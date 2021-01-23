package pt.obvionaoe.broker.db.dao

import org.jooq.impl.DSL.inline
import pt.obvionaoe.broker.db.DatabaseInit.dsl
import pt.obvionaoe.broker.db.entity.MessagesRow
import pt.obvionaoe.broker.db.entity.convertToMessagesRow
import pt.obvionaoe.broker.db.model.MessagesModel
import pt.obvionaoe.broker.db.model.TagsModel
import java.sql.Timestamp
import java.util.*

// the object that handles query creation for the 'messages' database table
object MessagesDatabaseAccessObject {
    private val databaseDao by lazy {
        DatabaseAccessObject
    }

    fun insertMsg(publisher: UUID, tag: String, message: String, timestamp: Long): MessagesRow? {
        val uuid = UUID.randomUUID()

        val query1 = dsl
            .insertInto(MessagesModel.table)
            .columns(MessagesModel.id, MessagesModel.tag, MessagesModel.publisher, MessagesModel.message, MessagesModel.timestamp)
            .values(inline(uuid), inline(tag), inline(publisher), inline(message), inline(Timestamp(timestamp)))
            .sql

        val query2 = dsl
            .select()
            .from(MessagesModel.table)
            .where(MessagesModel.id.eq(inline(uuid)))
            .sql

        databaseDao.executeUpdate(query1)

        return databaseDao.executeQuery(query2) {
            if (it.next()) {
                it.convertToMessagesRow()
            } else null
        }
    }

    fun getAllMsg(tag: String): List<MessagesRow> {
        val query = dsl
            .select()
            .from(MessagesModel.table)
            .where(
                MessagesModel.tag.eq(
                    dsl
                        .select(TagsModel.tag)
                        .from(TagsModel.table)
                        .where(TagsModel.tag.eq(inline(tag)))
                )
            )
            .sql

        return databaseDao.executeQuery(query) {
            val list = mutableListOf<MessagesRow>()
            while (it.next()) {
                list.add(it.convertToMessagesRow())
            }
            list
        }
    }

    fun deleteOldMsgs(now: Long, interval: Long): Boolean {
        val query = dsl
            .deleteFrom(MessagesModel.table)
            .where(MessagesModel.timestamp.lessThan(inline(Timestamp(now - interval))))
            .sql

        return databaseDao.execute(query)
    }
}
