package pt.obvionaoe.broker.db.dao

import org.jooq.impl.DSL.inline
import pt.obvionaoe.broker.db.DatabaseInit.dsl
import pt.obvionaoe.broker.db.entity.TagsRow
import pt.obvionaoe.broker.db.entity.convertToTagsRow
import pt.obvionaoe.broker.db.model.TagsModel

// the object that handles query creation for the 'tags' database table
object TagsDatabaseAccessObject {

    private val databaseDao by lazy {
        DatabaseAccessObject
    }

    fun createTagIfNotExists(tag: String): TagsRow? {
        val query1 = dsl
            .insertInto(TagsModel.table)
            .columns(TagsModel.tag)
            .values(inline(tag))
            .sql

        val query2 = dsl
            .select()
            .from(TagsModel.table)
            .where(TagsModel.tag.eq(inline(tag)))
            .sql

        databaseDao.executeUpdate(query1)

        return databaseDao.executeQuery(query2) {
            if (it.next()) {
                it.convertToTagsRow()
            } else null
        }
    }

    fun getTag(tag: String): TagsRow? {
        val query = dsl
            .select()
            .from(TagsModel.table)
            .where(TagsModel.tag.eq(inline(tag)))
            .sql

        return databaseDao.executeQuery(query) {
            if (it.next()) {
                it.convertToTagsRow()
            } else null
        }
    }

    fun getAllTags(): List<TagsRow>? {
        val query = dsl
            .select()
            .from(TagsModel.table)
            .sql

        return databaseDao.executeQuery(query) {
            val list = mutableListOf<TagsRow>()
            while (it.next()) {
                list.add(it.convertToTagsRow())
            }
            if (list.isEmpty()) {
                null
            } else {
                list
            }
        }
    }
}
