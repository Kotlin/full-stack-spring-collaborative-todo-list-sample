package com.example.demo.ui.components

import com.example.demo.model.Todo
import com.example.demo.utils.Keys
import com.example.demo.utils.value
import kotlinx.html.InputType
import kotlinx.html.Tag
import kotlinx.html.js.onBlurFunction
import kotlinx.html.js.onChangeFunction
import kotlinx.html.js.onClickFunction
import kotlinx.html.js.onKeyUpFunction
import org.w3c.dom.events.Event
import react.*
import react.dom.*

private val TodoItem: FC<TodoItemProps> = functionComponent { props ->
    val (editText, setEditText) = useState(props.todo.title)

    fun finishEditing(title: String) {
        if (title.isNotBlank()) {
            props.updateTodo(title, props.todo.completed)
        } else {
            props.removeTodo()
        }

        props.endEditing()
    }

    fun handleKeyUp(keyEvent: Event) {
        val key = Keys.fromString(keyEvent.asDynamic().key as String)
        when (key) {
            Keys.Enter -> {
                finishEditing(editText)
            }
            Keys.Escape -> {
                props.endEditing()
            }

            else -> {
                //TODO?
            }
        }
    }

    div(classes = "view") {

        input(classes = "toggle", type = InputType.checkBox) {

            attrs.onChangeFunction = { event ->
                val c = event.currentTarget.asDynamic().checked as Boolean
                props.updateTodo(props.todo.title, c)
            }

            ref { it?.checked = props.todo.completed }
        }
        label {
            +props.todo.title
        }
        button(classes = "destroy") {
            attrs.onClickFunction = {
                props.removeTodo()
            }
        }
    }
    input(classes = "edit", type = InputType.text) {
        attrs {
            value = editText
            onChangeFunction = { event ->
                val text = event.value
                setEditText(text)
            }
            onBlurFunction = { finishEditing(editText) }
            onKeyUpFunction = ::handleKeyUp

        }

        if (props.editing) {
            ref { it?.focus() }
        }
    }
}

external interface TodoItemProps : Props {
    var todo: Todo
    var editing: Boolean
    var removeTodo: () -> Unit
    var updateTodo: (String, Boolean) -> Unit
    var endEditing: () -> Unit
}

fun RBuilder.todoItem(
    todo: Todo,
    editing: Boolean,
    removeTodo: () -> Unit,
    updateTodo: (String, Boolean) -> Unit,
    endEditing: () -> Unit,
) = child(TodoItem) {
    attrs.todo = todo
    attrs.editing = editing
    attrs.removeTodo = removeTodo
    attrs.updateTodo = updateTodo
    attrs.endEditing = endEditing
}

fun <T : Tag> RDOMBuilder<T>.ref(handler: (dynamic) -> Unit) {
    domProps.ref(handler)
}

fun <T> Props.ref(ref: (T?) -> Unit) {
    asDynamic().ref = ref
}