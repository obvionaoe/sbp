package pt.obvionaoe.broker

import io.grpc.Server
import io.grpc.ServerBuilder
import io.grpc.stub.StreamObserver
import pt.obvionaoe.broker.db.DatabaseInit.setUpDatabase
import pt.obvionaoe.broker.db.dao.MessagesDatabaseAccessObject
import pt.obvionaoe.broker.services.PublisherBrokerImpl
import pt.obvionaoe.broker.services.SubscriberBrokerImpl
import pt.obvionaoe.sbp.TaggedMessage
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.logging.Logger
import kotlin.collections.HashMap
import kotlin.concurrent.fixedRateTimer

object BrokerServer {
    private const val INTERVAL: Long = 3_600_000
    private lateinit var server: Server
    private lateinit var fixedRateTimer: Timer
    private val logger: Logger = Logger.getLogger(BrokerServer::class.java.name)

    private val messagesDao by lazy {
        MessagesDatabaseAccessObject
    }
    val tagToSubscribers: HashMap<String, MutableList<StreamObserver<TaggedMessage>>> by lazy {
        HashMap()
    }

    private fun start() {
        // the port the server will listen on
        val port = 50051

        server = ServerBuilder.forPort(port)
            .addService(PublisherBrokerImpl)
            .addService(SubscriberBrokerImpl)
            .build()
            .start()

        logger.info("Server started, listening on port $port")

        // creates a timer to delete messages older than 1 hour in the database
        fixedRateTimer = fixedRateTimer(name = "db-clean-up", period = INTERVAL) {
            messagesDao.deleteOldMsgs(System.currentTimeMillis(), INTERVAL)
            logger.info("Cleaning messages older than 1 hour...")
        }

        // makes the server wait for the JVM shutdown to quit, in most cases (Ctrl+C)
        Runtime.getRuntime().addShutdownHook(
            object : Thread() {
                override fun run() {
                    System.err.println("*** shutting down gRPC server since JVM is shutting down")
                    try {
                        this@BrokerServer.stop()
                    } catch (e: InterruptedException) {
                        e.printStackTrace(System.err)
                    }
                    System.err.println("*** server shut down")
                }
            }
        )
    }

    private fun stop() {
        tagToSubscribers.forEach { tagEntry ->
            tagEntry.value.forEach { observer ->
                observer.onCompleted()
            }
        }
        fixedRateTimer.cancel()
        server.shutdown().awaitTermination(30, TimeUnit.SECONDS)
    }

    // await termination on the main thread since the gRPC library uses daemon threads.
    private fun blockUntilShutdown() {
        server.awaitTermination()
    }

    // the main function launches the server from the command line
    @JvmStatic
    fun main(args: Array<String>) {
        setUpDatabase()
        val server = BrokerServer
        server.start()
        server.blockUntilShutdown()
    }
}
