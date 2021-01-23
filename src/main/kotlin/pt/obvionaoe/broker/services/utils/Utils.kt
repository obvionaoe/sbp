package pt.obvionaoe.broker.services.utils

import io.grpc.StatusRuntimeException
import io.grpc.stub.StreamObserver
import pt.obvionaoe.broker.BrokerServer.tagToSubscribers
import pt.obvionaoe.sbp.TaggedMessage

// some utilitarian and extension functions
object Utils {
    fun store(tag: String, observer: StreamObserver<TaggedMessage>) {
        if (tagToSubscribers.containsKey(tag)) {
            if (!tagToSubscribers[tag]!!.contains(observer)) tagToSubscribers[tag]!!.add(observer)
        } else {
            tagToSubscribers[tag] = mutableListOf(observer)
        }
    }

    fun sendMsg(message: TaggedMessage) {
        val toDelete = mutableListOf<StreamObserver<TaggedMessage>>()
        tagToSubscribers[message.tag]?.forEach {
            try {
                it.onNext(message)
            } catch (e: StatusRuntimeException) {
                toDelete.add(it)
            }
        }

        tagToSubscribers[message.tag]?.removeAll(toDelete)
    }
}
