
import androidx.compose.runtime.*
import libui.compose.*
import libui.uiQuit

fun main() = runLibUI {
    val state = remember { WindowState(SizeInt(320, 240)) }

    Window(
        onCloseRequest = { uiQuit() },
        state = state,
        title = "Hello",
        hasMenubar = false,
        margined = true
    ) {
        VBox {
            val text = remember { mutableStateOf("") }

            Button(
                text = "libui говорит: click me!",
                onClick = {
                    text.value += """
                    |Hello, World!  Ciao, mondo!
                    |Привет, мир!  你好，世界！
                    |
                    """.trimMargin()
                }
            )

            MultilineEntry(
                text = text,
                readOnly = true
            )
        }
    }
}
