package com.example.demo.client

import com.example.demo.model.Todo
import com.example.demo.model.TodoEvent
import io.ktor.utils.io.core.*
import io.rsocket.kotlin.ExperimentalMetadataApi
import io.rsocket.kotlin.RSocket
import io.rsocket.kotlin.metadata.CompositeMetadata
import io.rsocket.kotlin.metadata.RoutingMetadata
import io.rsocket.kotlin.metadata.metadata
import io.rsocket.kotlin.payload.buildPayload
import io.rsocket.kotlin.payload.data
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@OptIn(ExperimentalMetadataApi::class)
class RSocketClient(val rsocket: RSocket) : Client {

    val scope = MainScope()
    
    override fun handleTodos(handler: (TodoEvent) -> Unit) {
        rsocket
            .requestStream(buildPayload {
                data(ByteReadPacket.Empty)
                metadata(CompositeMetadata(RoutingMetadata("todos")))
            })
            .map {
                val string = it.data.readText()
                Json.decodeFromString<TodoEvent>(string)
            }
            .onEach {
                handler(it)
            }
            .launchIn(scope)
    }

    override fun exchange(todo: List<Todo>) {
        scope.launch {
            todo.forEach {
                rsocket
                    .fireAndForget(buildPayload {
                        data(Json.encodeToString<Todo>(it))
                        metadata(CompositeMetadata(RoutingMetadata("todos.upsert")))
                    })
            }
        }
    }

    override fun addTodo(todo: Todo) {
        scope.launch {
            rsocket
                .fireAndForget(buildPayload {
                    data(Json.encodeToString<Todo>(todo))
                    metadata(CompositeMetadata(RoutingMetadata("todos.add")))
                })
        }
    }

    override fun updateTodo(todo: Todo) {
        scope.launch {
            rsocket
                .fireAndForget(buildPayload {
                    data(Json.encodeToString<Todo>(todo))
                    metadata(CompositeMetadata(RoutingMetadata("todos.update")))
                })
        }
    }

    override fun removeTodo(todo: Todo) {
        scope.launch {
            rsocket
                .fireAndForget(buildPayload {
                    data(Json.encodeToString<Todo>(todo))
                    metadata(CompositeMetadata(RoutingMetadata("todos.remove")))
                })
        }
    }

    companion object
}

expect suspend fun RSocketClient.Companion.create(): Client