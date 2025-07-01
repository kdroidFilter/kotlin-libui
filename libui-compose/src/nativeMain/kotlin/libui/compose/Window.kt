package libui.compose

import androidx.compose.runtime.*
import cnames.structs.uiWindow
import kotlinx.cinterop.*
import libui.*
import libui.compose.setMainWindowPtr

/**
 * State holder for a window.
 * 
 * @param contentSize The initial size of the window content area.
 */
class WindowState(contentSize: SizeInt) {
    /**
     * The current size of the window content area.
     * This will be updated automatically when the window is resized.
     */
    var contentSize by mutableStateOf(contentSize)
}

/**
 * Scope for window-related composables.
 * This class is used internally by the [runLibUI] function.
 */
class WindowScope internal constructor() {
    /**
     * Creates a window with the specified parameters.
     *
     * @param onCloseRequest Callback that will be invoked when the user attempts to close the window.
     * @param state The state of the window, including its size.
     * @param title The title of the window.
     * @param hasMenubar Whether the window has a menubar.
     * @param borderless Whether the window is borderless.
     * @param margined Whether the window has margins.
     * @param fullscreen Whether the window is fullscreen.
     * @param isVisible Whether the window is visible.
     * @param enabled Whether the window is enabled.
     * @param content The content of the window.
     */
    @OptIn(ExperimentalForeignApi::class)
    @Composable
    fun Window(
        onCloseRequest: () -> Unit,
        state: WindowState,
        title: String,
        hasMenubar: Boolean = false,
        borderless: Boolean = false,
        margined: Boolean = false,
        fullscreen: Boolean = false,
        isVisible: Boolean = true,
        enabled: Boolean = true,
        content: @Composable () -> Unit,
    ) {
        val control = rememberControl {
            uiNewWindow(
                title,
                state.contentSize.width,
                state.contentSize.height,
                if (hasMenubar) 1 else 0
            )!!
        }

        handleChildren(content) {
            object : SingletonApplier<CPointer<uiControl>>() {
                override fun setItem(item: CPointer<uiControl>?) {
                    uiWindowSetChild(control.ptr, item)
                }
            }
        }

        val onCloseRef = rememberStableRef(onCloseRequest)
        val stateRef = rememberStableRef(state)

        // Set the main window pointer for dialogs
        setMainWindowPtr(control.ptr)

        ComposeNode<CPointer<uiWindow>, MutableListApplier<CPointer<uiWindow>>>(
            factory = { control.ptr },
            update = {
                update(state.contentSize) { (w, h) -> uiWindowSetContentSize(this, w, h) }
                update(title) { uiWindowSetTitle(this, it) }
                set(borderless) { uiWindowSetBorderless(this, if (it) 1 else 0) }
                set(margined) { uiWindowSetMargined(this, if (it) 1 else 0) }
                set(fullscreen) { uiWindowSetFullscreen(this, if (it) 1 else 0) }
                set(onCloseRef) {
                    uiWindowOnClosing(
                        this,
                        staticCFunction { _, senderData ->
                            val ref = senderData!!.asStableRef<() -> Unit>()
                            ref.get()()
                            0
                        },
                        it.asCPointer()
                    )
                }
                set(stateRef) {
                    uiWindowOnContentSizeChanged(
                        this,
                        staticCFunction { sender, senderData ->
                            val ref = senderData!!.asStableRef<WindowState>()

                            val array = IntArray(2)
                            array.usePinned { pin ->
                                uiWindowContentSize(sender, pin.addressOf(0), pin.addressOf(1))
                            }

                            ref.get().contentSize = SizeInt(array[0], array[1])
                        },
                        it.asCPointer()
                    )
                }
                setCommon(enabled, isVisible)
            }
        )
    }
}
