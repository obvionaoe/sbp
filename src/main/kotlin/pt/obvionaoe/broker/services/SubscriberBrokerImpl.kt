package pt.obvionaoe.broker.services

import io.grpc.stub.StreamObserver
import pt.obvionaoe.broker.db.dao.MessagesDatabaseAccessObject
import pt.obvionaoe.broker.db.dao.TagsDatabaseAccessObject
import pt.obvionaoe.broker.db.entity.toTaggedMessage
import pt.obvionaoe.broker.services.utils.ErrorResponses.NOT_FOUND_ERROR
import pt.obvionaoe.broker.services.utils.Utils.store
import pt.obvionaoe.sbp.*
import java.util.logging.Logger

object SubscriberBrokerImpl : SubscribeBrokerGrpc.SubscribeBrokerImplBase() {
    private val logger: Logger = Logger.getLogger(SubscriberBrokerImpl::class.java.name)
    private val messagesDao: MessagesDatabaseAccessObject by lazy {
        MessagesDatabaseAccessObject
    }
    private val tagsDao: TagsDatabaseAccessObject by lazy {
        TagsDatabaseAccessObject
    }

    // the subscriber gets a list of available tags to subscribe to
    override fun getTags(request: ListTagsRequest, responseObserver: StreamObserver<ListTagsResponse>) {
        val tagsList = tagsDao.getAllTags() ?: run {
            responseObserver.onError(NOT_FOUND_ERROR("No tags currently available"))
            return
        }

        logger.info("Getting tags...")

        ListTagsResponse
            .newBuilder()
            .addAllTagsList(tagsList.map { it.tag })
            .build()
            .let(responseObserver::onNext)

        responseObserver.onCompleted()
    }

    // the subscriber subscribes to a tag, all previous messages are sent to the subscriber
    // and the subscriber is added to the list of active subscribers for that tag
    override fun subscribe(request: SubscribeRequest, responseObserver: StreamObserver<TaggedMessage>) {
        val tag = tagsDao.getTag(request.tag) ?: run {
            responseObserver.onError(NOT_FOUND_ERROR("Tag does not exist"))
            return
        }

        logger.info("Subscribing to ${tag.tag}...")
        store(request.tag, responseObserver)

        val messages = messagesDao.getAllMsg(request.tag)

        if (messages.isNotEmpty()) {
            messages.map {
                it.toTaggedMessage()
            }.forEach(responseObserver::onNext)
        }
    }
}
