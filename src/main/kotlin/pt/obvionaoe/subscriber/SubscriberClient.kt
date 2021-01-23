package pt.obvionaoe.subscriber

import io.grpc.Channel
import io.grpc.ManagedChannelBuilder
import io.grpc.StatusRuntimeException
import pt.obvionaoe.sbp.*
import pt.obvionaoe.utils.Utils.warn
import java.sql.Timestamp
import java.time.Instant
import java.util.logging.Logger
import kotlin.system.exitProcess

class SubscriberClient(channel: Channel) {
    private val blockingStub: SubscribeBrokerGrpc.SubscribeBrokerBlockingStub =
        SubscribeBrokerGrpc.newBlockingStub(channel)

    // get a list of tags available for subscription
    fun listAvailableTags(): List<String> {
        val request: ListTagsRequest = ListTagsRequest
            .newBuilder()
            .build()

        logger.info("Asking for a list of tags")

        val reply: ListTagsResponse
        try {
            reply = blockingStub.getTags(request)
        } catch (e: StatusRuntimeException) {
            logger.warn("RPC failed: {0}", e.status)
            exitProcess(1)
        }

        logger.info("list received: " + reply.tagsListList)

        return reply.tagsListList
    }

    // subscribe to a tag
    fun subscribe(tag: String) {
        val request: SubscribeRequest = SubscribeRequest
            .newBuilder()
            .setTag(tag)
            .build()

        logger.info("Subscribing to $tag...")

        try {
            blockingStub.subscribe(request).forEach {
                logger.info("message received: [${it.message}, ${it.tag}, ${Instant.ofEpochMilli(it.timestamp)}]")
            }
        } catch (e: StatusRuntimeException) {
            logger.warn("RPC failed: {0}", e.status)
            exitProcess(1)
        }
    }

    companion object {
        private val logger: Logger = Logger.getLogger(SubscriberClient::class.java.name)

        /**
         * Start the client
         */
        @Throws(Exception::class)
        @JvmStatic
        fun main(args: Array<String>) {
            // default target given as host:port
            val target = "localhost:50051"

            if (args.size != 1) {
                System.err.println("ERR: Please provide a tag")
                return
            }

            Timestamp(0L).toInstant()

            val tag = args[0]

            // Create a channel to communicate with the server
            val channel = ManagedChannelBuilder.forTarget(target)
                .usePlaintext()
                .keepAliveWithoutCalls(true)
                .build()

            val client = SubscriberClient(channel)
            val tagList = client.listAvailableTags()
            if (tagList.contains(tag)) {
                client.subscribe(tag)
            } else {
                System.err.println("Tag '$tag' does not exist!")
                System.err.println("Exiting...")
                exitProcess(1)
            }
        }
    }
}
