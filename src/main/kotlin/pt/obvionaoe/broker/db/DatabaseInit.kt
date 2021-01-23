package pt.obvionaoe.broker.db

import org.jooq.SQLDialect
import org.jooq.impl.DSL
import pt.obvionaoe.broker.db.dao.DatabaseAccessObject
import pt.obvionaoe.broker.db.model.MessagesModel
import pt.obvionaoe.broker.db.model.PublishersModel
import pt.obvionaoe.broker.db.model.TagsModel
import java.sql.Connection
import java.sql.DriverManager

// initializes the database and executes (through DatabaseAccessObject) the creation queries for the tables
// these queries are ignored by the database if the tables already exist
object DatabaseInit {
    private const val user = "admin"
    private const val pass = "unsafe_password"
    private const val db = "broker"

    private const val url = "jdbc:sqlite:$db.db"

    private val databaseDao by lazy {
        DatabaseAccessObject
    }

    val dsl by lazy {
        DSL.using(SQLDialect.SQLITE)
    }

    fun conn(): Connection = DriverManager.getConnection(url, user, pass)

    fun setUpDatabase() {
        System.getProperties().setProperty("org.jooq.no-logo", "true")
        databaseDao.execute(MessagesModel.creationQuery)
        databaseDao.execute(TagsModel.creationQuery)
        databaseDao.execute(PublishersModel.creationQuery)
    }
}
