# LibUI Compose

LibUI Compose is a Kotlin Compose UI library for the [LibUI](https://github.com/andlabs/libui) native GUI library. It allows you to build native GUI applications using the Compose declarative UI framework.

## Components

### Layout Components

#### VBox
A vertical box container that stacks its children vertically.

```kotlin
VBox {
    // Child components go here
}
```

#### HBox
A horizontal box container that stacks its children horizontally.

```kotlin
HBox {
    // Child components go here
}
```

#### Form
A form container that arranges its children in a label-control pattern. Typically used for settings or data entry forms.

```kotlin
Form {
    // Child components go here
}
```

#### TabPane
A tab container that allows switching between different pages of content. Each child component added to the TabPane will be placed in a separate tab.

```kotlin
TabPane {
    // Child components go here
}
```

#### Group
A group container with a title that can contain a single child.

```kotlin
Group("Group Title") {
    // Child component goes here
}
```

### Basic Controls

#### Label
A simple text label.

```kotlin
Label("Hello, World!")
```

#### Button
A clickable button.

```kotlin
Button("Click Me") {
    // onClick handler
}
```

#### Checkbox
A checkbox with a label.

```kotlin
val checked = remember { mutableStateOf(false) }
Checkbox("Enable feature", checked)
```

#### Slider
A slider widget that allows the user to select a value from a range.

```kotlin
val value = remember { mutableStateOf(50) }
Slider(value, 0, 100)
```

#### ProgressBar
A progress bar that shows progress as a percentage.

```kotlin
// Determinate progress bar (0-100)
ProgressBar(75)

// Indeterminate progress bar
ProgressBar()
```

#### ColorButton
A button that allows the user to select a color.

```kotlin
val color = remember { mutableStateOf(Color(0xFFFFFF)) }
ColorButton(color)
```

### Text Input Controls

#### TextField
A single-line text input field.

```kotlin
val text = remember { mutableStateOf("") }
TextField(text)
```

#### PasswordField
A single-line password input field that masks the input.

```kotlin
val password = remember { mutableStateOf("") }
PasswordField(password)
```

#### SearchField
A single-line text input field with search functionality.

```kotlin
val searchQuery = remember { mutableStateOf("") }
SearchField(searchQuery)
```

#### MultilineEntry
A multi-line text input field.

```kotlin
val text = remember { mutableStateOf("") }
MultilineEntry(text)
```

#### NonWrappingMultilineEntry
A multi-line text input field that doesn't wrap text.

```kotlin
val text = remember { mutableStateOf("") }
NonWrappingMultilineEntry(text)
```

### Separators

#### HorizontalSeparator
A horizontal line separator.

```kotlin
HorizontalSeparator()
```

#### VerticalSeparator
A vertical line separator.

```kotlin
VerticalSeparator()
```

### Window

#### Window
A window that can contain a single child.

```kotlin
Window(
    onCloseRequest = { /* Handle close request */ },
    state = windowState,
    title = "Window Title"
) {
    // Window content goes here
}
```

## Getting Started

To use LibUI Compose, you need to call the `runLibUI` function and create a window:

```kotlin
fun main() {
    runLibUI {
        val state = remember { WindowState(SizeInt(800, 600)) }
        
        Window(
            onCloseRequest = { /* Handle close request */ },
            state = state,
            title = "My LibUI Compose App"
        ) {
            // Your UI components here
            VBox {
                Label("Hello, LibUI Compose!")
                Button("Click Me") {
                    println("Button clicked!")
                }
            }
        }
    }
}
```

## State Management

LibUI Compose uses Compose's state management system. You can use `remember` and `mutableStateOf` to create and manage state:

```kotlin
val text = remember { mutableStateOf("Hello") }
TextField(text)
```

## Composition

LibUI Compose uses Compose's composition system to build UIs. You can use all the standard Compose features like `if` statements, loops, and functions to build your UI:

```kotlin
VBox {
    val count = remember { mutableStateOf(0) }
    
    Label("Count: ${count.value}")
    
    Button("Increment") {
        count.value++
    }
    
    if (count.value > 5) {
        Label("Count is greater than 5!")
    }
}
```