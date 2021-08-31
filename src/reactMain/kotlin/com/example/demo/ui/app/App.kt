package com.example.demo.ui.app

import com.example.demo.client.Client
import com.example.demo.model.EventType
import com.example.demo.model.Todo
import com.example.demo.model.TodoEvent
import com.example.demo.model.TodoFilter
import com.example.demo.service.TodoService
import com.example.demo.ui.components.headerInput
import com.example.demo.ui.components.info
import com.example.demo.ui.components.todoBar
import com.example.demo.ui.components.todoList
import io.rsocket.kotlin.ExperimentalMetadataApi
import kotlinx.browser.document
import kotlinx.html.InputType
import kotlinx.html.id
import kotlinx.html.js.onChangeFunction
import kotlinx.html.title
import org.w3c.dom.HTMLInputElement
import react.*
import react.dom.attrs
import react.dom.input
import react.dom.label
import react.dom.section
import com.example.demo.utils.translate

object AppOptions {
    var language = "no-language"
    var localStorageKey = "todos-koltin-react"
}

@ExperimentalMetadataApi
class App(props: AppProps) : RComponent<AppProps, AppState>(props) {

    override fun AppState.init(props: AppProps) {
        console.log("componentWillReceiveProps $props")

        props.client.handleTodos {
            props.service.handleEvent(it)

            setState {
                todos = props.service.listTodos()
            }
        }
    }

    override fun componentWillMount() {
        console.log("component will mount com.example.demo.ui.app")

        val listTodos = props.service.listTodos()

        props.client.exchange(listTodos)

        setState {
            todos = listTodos
        }
    }

    override fun RBuilder.render() {

        val currentFilter = when (props.route) {
            "pending" -> TodoFilter.PENDING
            "completed" -> TodoFilter.COMPLETED
            else -> TodoFilter.ANY
        }

        section("todoapp") {
            headerInput(::createTodo)


            if (state.todos.isNotEmpty()) {

                val allChecked = isAllCompleted()

                section("main") {
                    input(InputType.checkBox, classes = "toggle-all") {
                        attrs {
                            id = "toggle-all"
                            checked = allChecked

                            onChangeFunction = { event ->
                                val isChecked = (event.currentTarget as HTMLInputElement).checked

                                setAllStatus(isChecked)
                            }
                        }
                    }
                    label {
                        attrs["htmlFor"] = "toggle-all"
                        attrs.title = "Mark all as complete".translate()
                    }

                    todoList(::removeTodo, ::updateTodo, state.todos, currentFilter)
                }

                todoBar(
                    pendingCount = countPending(),
                    anyCompleted = state.todos.any { todo -> todo.completed },
                    clearCompleted = ::clearCompleted,
                    currentFilter = currentFilter,
                    updateFilter = ::updateFilter
                )
            }

        }
        info()
    }

    private fun updateFilter(newFilter: TodoFilter) {
        document.location!!.href = "#?route=${newFilter.name.lowercase()}"
    }

    private fun countPending() = pendingTodos().size

    private fun removeTodo(todo: Todo) {
        console.log("removeTodo [${todo.id}] ${todo.title}")
        props.client.removeTodo(todo)

        props.service.handleEvent(TodoEvent(EventType.REMOVE, todo))

        setState {
            todos = props.service.listTodos()
        }
    }

    private fun createTodo(todo: Todo) {
        console.log("createTodo [${todo.id}] ${todo.title}")

        props.client.addTodo(todo)

        props.service.handleEvent(TodoEvent(EventType.ADD, todo))

        setState {
            todos = props.service.listTodos()
        }
    }

    private fun updateTodo(todo: Todo) {
        console.log("updateTodo [${todo.id}] ${todo.title}")

        props.client.updateTodo(todo)

        props.service.handleEvent(TodoEvent(EventType.UPDATE, todo))

        setState {
            todos = props.service.listTodos()
        }
    }

    private fun setAllStatus(newStatus: Boolean) {
        state.todos.forEach { todo -> updateTodo(todo.copy(completed = newStatus)) }
    }

    private fun clearCompleted() {
        state.todos.filter { todo -> todo.completed }
            .forEach { todo -> removeTodo(todo.copy(removed = true)) }
    }

    private fun isAllCompleted(): Boolean {
        return state.todos.fold(true) { allCompleted, todo ->
            allCompleted && todo.completed
        }
    }

    private fun pendingTodos(): List<Todo> {
        return state.todos.filter { todo -> !todo.completed }
    }
}


external interface AppState : State {
    var todos: List<Todo>
}

external interface AppProps : Props {
    var route: String
    var client: Client
    var service: TodoService
}

fun RBuilder.app(route: String, client: Client, service: TodoService) = child(App::class) {
    attrs.route = route
    attrs.client = client
    attrs.service = service
}
