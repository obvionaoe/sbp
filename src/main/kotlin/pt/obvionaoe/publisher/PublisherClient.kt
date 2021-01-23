package pt.obvionaoe.publisher

import io.grpc.Channel
import io.grpc.ManagedChannelBuilder
import io.grpc.StatusRuntimeException
import io.grpc.stub.StreamObserver
import pt.obvionaoe.publisher.poisson.PoissonUtils.timeToNextEvent
import pt.obvionaoe.sbp.*
import pt.obvionaoe.utils.Utils.warn
import java.lang.Thread.sleep
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.logging.Logger

class PublisherClient(channel: Channel) {
    private val blockingStub: PublishBrokerGrpc.PublishBrokerBlockingStub =
        PublishBrokerGrpc.newBlockingStub(channel)
    private val asyncStub: PublishBrokerGrpc.PublishBrokerStub =
        PublishBrokerGrpc.newStub(channel)
    private lateinit var id: String

    // generate a new TaggedMessage
    private fun getTaggedMessage(tag: String, counter: Int) = TaggedMessage
        .newBuilder()
        .setPublisherId(id)
        .setMessage("example message #$counter")
        .setTag(tag)
        .setTimestamp(System.currentTimeMillis())
        .build()

    // register with the server
    private fun register(tag: String): Boolean {
        val request = RegisterRequest
            .newBuilder()
            .setTag(tag)
            .build()

        logger.info("registering...")

        val reply: RegisterResponse
        try {
            reply = blockingStub.register(request)
        } catch (e: StatusRuntimeException) {
            logger.warn("RPC failed: {0}", e.status)
            return false
        }

        logger.info("Status: ${reply.status}")
        id = reply.id

        return true
    }

    // send a TaggedMessage to the server
    private fun sendMsg(tag: String, lambda: Double) {
        val finishLatch = CountDownLatch(1)

        val responseObserver = object : StreamObserver<PublishResponse> {
            override fun onNext(value: PublishResponse) {
                logger.info("Status: ${value.status}")
            }

            override fun onError(t: Throwable) {
                logger.warn("RPC failed: ${t.message}")
                finishLatch.countDown()
            }

            override fun onCompleted() {
                logger.info("Finished sending messages!")
                finishLatch.countDown()
            }
        }

        val requestObserver = asyncStub.publish(responseObserver)

        var counter = 0
        while (true) {
            sleep(timeToNextEvent(lambda).toLong())

            getTaggedMessage(tag, counter++).let(requestObserver::onNext)
            logger.info("Sending message...")

            if (finishLatch.count == 0L) break
        }

        finishLatch.await(1, TimeUnit.MINUTES)
    }

    companion object {
        private val logger: Logger = Logger.getLogger(PublisherClient::class.java.name)

        /**
         * Start the client
         */
        @Throws(Exception::class)
        @JvmStatic
        fun main(args: Array<String>) {
            // default target given as host:port
            val target = "localhost:50051"

            if (args.size != 2) {
                System.err.println("ERR: Please provide a lambda and a tag")
                return
            }

            val lambda = args[0].toDouble()
            val tag = args[1]

            // Create a channel to communicate with the server
            val channel = ManagedChannelBuilder.forTarget(target)
                .usePlaintext()
                .keepAliveWithoutCalls(true)
                .build()

            try {
                val client = PublisherClient(channel)
                if (client.register(tag)) {
                    client.sendMsg(tag, lambda)
                } else {
                    System.err.println("ERR: That tag is not allowed")
                }
            } finally {
                channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS)
            }
        }
    }
}
