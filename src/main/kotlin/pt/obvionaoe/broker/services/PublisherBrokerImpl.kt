package pt.obvionaoe.broker.services

import io.grpc.stub.StreamObserver
import pt.obvionaoe.broker.db.dao.MessagesDatabaseAccessObject
import pt.obvionaoe.broker.db.dao.PublishersDatabaseAccessObject
import pt.obvionaoe.broker.db.dao.TagsDatabaseAccessObject
import pt.obvionaoe.broker.services.utils.ErrorResponses.INTERNAL_ERROR
import pt.obvionaoe.broker.services.utils.Utils.sendMsg
import pt.obvionaoe.sbp.*
import pt.obvionaoe.utils.Utils.toUUID
import pt.obvionaoe.utils.Utils.warn
import java.util.logging.Logger

object PublisherBrokerImpl : PublishBrokerGrpc.PublishBrokerImplBase() {
    private val messagesDao: MessagesDatabaseAccessObject by lazy {
        MessagesDatabaseAccessObject
    }
    private val publisherDao: PublishersDatabaseAccessObject by lazy {
        PublishersDatabaseAccessObject
    }
    private val tagsDao: TagsDatabaseAccessObject by lazy {
        TagsDatabaseAccessObject
    }

    private val logger: Logger = Logger.getLogger(PublisherBrokerImpl::class.java.name)

    // publisher registers a tag with the broker
    override fun register(request: RegisterRequest, responseObserver: StreamObserver<RegisterResponse>) {
        logger.info(request.tag)
        val tag = tagsDao.createTagIfNotExists(request.tag) ?: run {
            responseObserver.onError(INTERNAL_ERROR("Error with tag"))
            return
        }

        val publisher = publisherDao.register(tag.tag) ?: run {
            responseObserver.onError(INTERNAL_ERROR("Couldn't register publisher"))
            return
        }

        RegisterResponse
            .newBuilder()
            .setStatus(PublishStatus.ACCEPTED)
            .setId(publisher.id.toString())
            .build()
            .let(responseObserver::onNext)

        responseObserver.onCompleted()
    }

    // publisher sends a message to the broker which is immediately sent to a subscriber (if available)
    // or stored in the database
    override fun publish(responseObserver: StreamObserver<PublishResponse>): StreamObserver<TaggedMessage> {
        return object : StreamObserver<TaggedMessage> {
            override fun onNext(message: TaggedMessage) {
                messagesDao.insertMsg(message.publisherId.toUUID(), message.tag, message.message, message.timestamp) ?: run {
                    responseObserver.onError(INTERNAL_ERROR("Couldn't insert message"))
                    return
                }

                sendMsg(message)
            }

            override fun onError(t: Throwable) {
                t.message?.let { logger.warn(it) }
            }

            override fun onCompleted() {
                PublishResponse
                    .newBuilder()
                    .setStatus(PublishStatus.ACCEPTED)
                    .build()
                    .let {
                    responseObserver.onNext(it)
                    responseObserver.onCompleted()
                }
            }


        }
    }
}
