# Kotlin LibUI

[![Kotlin](https://img.shields.io/badge/kotlin-2.2.0-blue.svg)](https://kotlinlang.org)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

A fork of [kotlin-libui](https://github.com/msink/kotlin-libui) updated to Kotlin 2.2.0, providing [Kotlin/Native](https://github.com/JetBrains/kotlin-native) bindings to the [libui](https://github.com/andlabs/libui.git) C library.

## Overview

LibUI is a lightweight multi-platform UI library that uses native widgets on Linux (Gtk3), macOS, and Windows. With these Kotlin bindings, you can develop cross-platform, native-looking GUI applications written in Kotlin and compiled to small native executable files.

## Platform Compatibility

- **Windows**: Working properly
- **Linux**: Working properly
- **macOS**: Currently experiencing issues:
  - Does not execute via Gradle
  - Requires manual compilation of the executable before launching
  - Window appears in the bottom-left corner of the screen

## Features

### Traditional API

The library provides a Kotlin DSL for building UIs in a concise and readable way:

```kotlin
fun main() = appWindow(
    title = "Hello",
    width = 320,
    height = 240
) {
    vbox {
        lateinit var scroll: TextArea

        button("Click me!") {
            action {
                scroll.append("Hello, World!\n")
            }
        }
        scroll = textarea {
            readonly = true
            stretchy = true
        }
    }
}
```

### Experimental Compose Implementation

This fork aims to provide a Compose UI implementation for LibUI, although this remains a very challenging task. The current Compose implementation is **extremely experimental** and should be considered only as a technical proof of concept:

```kotlin
fun main() = runLibUI {
    val state = remember { WindowState(SizeInt(640, 480)) }

    Window(
        onCloseRequest = { uiQuit() },
        state = state,
        title = "LibUI Compose Example"
    ) {
        VBox {
            val text = remember { mutableStateOf("") }

            Button(
                text = "Click Me",
                onClick = { text.value += "Hello, Compose UI!\n" }
            )

            MultilineEntry(
                text = text,
                readOnly = true
            )
        }
    }
}
```

## Getting Started

### Installation

To use this library in your project, clone this repository:

```bash
git clone https://github.com/yourusername/kotlin-libui.git
```

### Building

For local builds:

```bash
# On Linux/macOS
./gradlew build

# On Windows
gradlew build
```

To build and run a sample:

```bash
# Build the library
cd kotlin-libui/libui
../gradlew build

# Run a sample
cd ../samples/hello-ktx/
../../gradlew run
```

### macOS Special Instructions

On macOS, due to current issues:

1. Compile the executable manually
2. Launch the executable directly (not through Gradle)
3. Note that the window will appear in the bottom-left corner of the screen

## Samples

- [Hello World](samples/hello-ktx)
- [Form](samples/form)
- [Controls Gallery](samples/controlgallery)
- [Histogram](samples/histogram)
- [Draw Text](samples/drawtext)
- [Date & Time](samples/datetime)
- [Timer](samples/timer)
- [Logo](samples/logo)
- [Table](samples/table)

## Documentation

- [API Documentation](docs/index.md)
- [Compose UI Documentation](libui-compose/README.md)

## Lifecycle Management

Kotlin memory management differs from the native C model. All libui objects are wrapped in Kotlin objects inherited from [Disposable](docs/libui.ktx/-disposable/index.md), and direct use of libui functions is not recommended in most cases.

Disposable objects must be disposed by calling the [dispose](docs/libui.ktx/-disposable/dispose.md)() method before the program ends. Most objects are attached as children to other objects, in which case the parent is responsible for disposing of all its children recursively.

## Status

This fork is under active development with a focus on:

1. Maintaining compatibility with Kotlin 2.2.0
2. Fixing platform-specific issues, especially on macOS
3. Exploring the possibility of a Compose UI implementation, despite the significant challenges involved

## Contributing

Contributions are welcome! Feel free to submit issues and pull requests.

## License

This project is licensed under the MIT License - see the LICENSE file for details.
