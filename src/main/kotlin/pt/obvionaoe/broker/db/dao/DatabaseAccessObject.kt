package pt.obvionaoe.broker.db.dao

import pt.obvionaoe.broker.db.DatabaseInit.conn
import java.sql.ResultSet
import java.util.logging.Level
import java.util.logging.Logger

// the object that executes the queries in the database
object DatabaseAccessObject {
    private val logger = Logger.getLogger(DatabaseAccessObject::class.java.name)

    fun <T> executeQuery(query: String, process: (ResultSet) -> T): T {
        logger.log(Level.INFO, "Executing query '$query'")

        return conn().use {
            val resultSet = it.createStatement().executeQuery(query)

            process(resultSet)
        }
    }

    fun executeUpdate(query: String): Boolean = runCatching {
        logger.log(Level.INFO, "Executing query '$query'")

        return conn().use {
            it.createStatement().executeUpdate(query) > 0
        }
    }.isSuccess

    fun execute(query: String): Boolean = runCatching {
        logger.log(Level.INFO, "Executing query '$query'")

        return conn().use {
            it.createStatement().execute(query)
        }
    }.isSuccess
}
