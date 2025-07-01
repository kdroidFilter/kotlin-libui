
import androidx.compose.runtime.*
import libui.compose.*
import libui.uiQuit

fun main() = runLibUI {
    val state = remember { WindowState(SizeInt(320, 200)) }

    Window(
        onCloseRequest = { uiQuit() },
        state = state,
        title = "Authentication required",
        hasMenubar = false,
        margined = true
    ) {
        VBox {
            val username = remember { mutableStateOf("") }
            val password = remember { mutableStateOf("") }

            Form {
                FormItem(label = "Username") {
                    TextField(
                        text = username,
                        readOnly = false
                    )
                }

                FormItem(label = "Password") {
                    PasswordField(
                        text = password,
                        readOnly = false
                    )
                }
            }

            Button(
                text = "Login",
                onClick = {
                    // In a real application, you would handle the login here
                    // For this sample, we'll just print the values
                    println("Username: ${username.value}, Password: ${password.value}")
                }
            )
        }
    }
}
