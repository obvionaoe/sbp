package pt.obvionaoe.broker.db.dao

import org.jooq.impl.DSL.inline
import pt.obvionaoe.broker.db.DatabaseInit.dsl
import pt.obvionaoe.broker.db.entity.PublishersRow
import pt.obvionaoe.broker.db.entity.convertToPublishersRow
import pt.obvionaoe.broker.db.model.PublishersModel
import java.util.*

// the object that handles query creation for the 'publishers' database table
object PublishersDatabaseAccessObject {

    private val databaseDao by lazy {
        DatabaseAccessObject
    }

    fun register(tag: String): PublishersRow? {
        val uuid = UUID.randomUUID()

        val query1 = dsl
            .insertInto(PublishersModel.table)
            .columns(PublishersModel.id, PublishersModel.tag)
            .values(inline(uuid), inline(tag))
            .sql

        val query2 = dsl
            .select()
            .from(PublishersModel.table)
            .where(PublishersModel.id.eq(inline(uuid)))
            .sql

        databaseDao.executeUpdate(query1)

        return databaseDao.executeQuery(query2) {
            if (it.next()) {
                it.convertToPublishersRow()
            } else null
        }
    }
}
