package pt.obvionaoe.broker.services.utils

import io.grpc.Status
import io.grpc.StatusRuntimeException

object ErrorResponses {
    fun NOT_FOUND_ERROR(msg: String) = StatusRuntimeException(Status.NOT_FOUND.withDescription(msg))

    fun INTERNAL_ERROR(msg: String) = StatusRuntimeException(Status.INTERNAL.withDescription(msg))
}
