package kritor.service

import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.kritor.event.EventServiceGrpcKt
import io.kritor.event.EventStructure
import io.kritor.event.EventType
import io.kritor.event.RequestPushEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import moe.fuqiuluo.shamrock.internals.GlobalEventTransmitter

internal object EventService : EventServiceGrpcKt.EventServiceCoroutineImplBase() {
    override fun registerActiveListener(request: RequestPushEvent): Flow<EventStructure> {
        return channelFlow {
            when (request.type!!) {
                EventType.CORE_EVENT -> {}
                EventType.MESSAGE -> GlobalEventTransmitter.onMessageEvent {
                    send(EventStructure.newBuilder().apply {
                        this.type = EventType.MESSAGE
                        this.message = it.second
                    }.build())
                }

                EventType.NOTICE -> GlobalEventTransmitter.onNoticeEvent {
                    send(EventStructure.newBuilder().apply {
                        this.type = EventType.NOTICE
                        this.notice = it
                    }.build())
                }

                EventType.REQUEST -> GlobalEventTransmitter.onRequestEvent {
                    send(EventStructure.newBuilder().apply {
                        this.type = EventType.REQUEST
                        this.request = it
                    }.build())
                }

                EventType.UNRECOGNIZED -> throw StatusRuntimeException(Status.INVALID_ARGUMENT)
            }
        }
    }
}
