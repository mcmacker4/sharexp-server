package es.hgg.sharexp.server.persistence

import es.hgg.sharexp.server.persistence.tables.*
import io.ktor.server.application.*
import io.r2dbc.spi.ConnectionFactoryOptions
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabaseConfig
import org.jetbrains.exposed.v1.r2dbc.SchemaUtils
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction


suspend fun Application.configureDatabase() {
    val url = environment.config.property("database.url").getString()
    val user = environment.config.propertyOrNull("database.user")?.getString()
    val password = environment.config.propertyOrNull("database.password")?.getString()

    R2dbcDatabase.connect(
        url = url,
        databaseConfig = R2dbcDatabaseConfig {
            connectionFactoryOptions {
                user?.let { option(ConnectionFactoryOptions.USER, it) }
                password?.let { option(ConnectionFactoryOptions.PASSWORD, it) }
            }
        }
    )

    suspendTransaction {
        SchemaUtils.create(Users, Groups, GroupMembers, Expenses, ExpenseParticipants)
    }
}