package libui.compose

import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.Snapshot
import kotlinx.cinterop.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import libui.*

@OptIn(ExperimentalForeignApi::class)
inline fun withLibUI(block: () -> Unit) {
    platform.posix.srand(platform.posix.time(null).toUInt())

    val error = memScoped {
        val options = alloc<uiInitOptions>()
        uiInit(options.ptr)
    }
    if (error != null) {
        val errorString: String
        try {
            errorString = error.toKString()
        } finally {
            uiFreeInitError(error)
        }
        throw Error("error initializing ui: '$errorString'")
    }

    try {
        block()
    } finally {
        // Shutdown libui
        uiUninit()
    }
}

internal fun Snapshot.Companion.globalWrites(): Flow<Any> {
    return callbackFlow {
        val handle = registerGlobalWriteObserver { trySend(it) }
        awaitClose { handle.dispose() }
    }
}

@OptIn(ExperimentalForeignApi::class)
internal fun CPointer<ByteVar>.uiText(): String {
    try {
        return toKString()
    } finally {
        uiFreeText(this)
    }
}

/**
 * Remembers a control across recompositions.
 * 
 * @param key An optional key to force recreation of the control when it changes.
 * @param block A function that creates the control.
 * @return A [Control] instance that will be remembered across recompositions.
 */
@OptIn(ExperimentalForeignApi::class)
@Composable
internal fun <T: CPointed> rememberControl(vararg key: Any?, block: () -> CPointer<T>): Control<T> {
    return remember(*key) { Control(block()) }
}

@OptIn(ExperimentalForeignApi::class)
internal class Control<T: CPointed>(val ptr: CPointer<T>) : RememberObserver {
    override fun onAbandoned() {
        uiControlDestroy(ptr.reinterpret())
    }

    override fun onForgotten() {
        uiControlDestroy(ptr.reinterpret())
    }

    override fun onRemembered() {
    }
}

@Composable
internal fun handleChildren(
    content: @Composable () -> Unit,
    applier: () -> Applier<*>
) {
    val compContext = rememberCompositionContext()
    DisposableEffect(content) {
        val composition = Composition(applier(), compContext)
        composition.setContent(content)
        onDispose {
            composition.dispose()
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
internal fun <T: CPointed> Updater<CPointer<T>>.setCommon(enabled: Boolean, visible: Boolean) {
    set(visible) {
        if (it) {
            uiControlShow(this.reinterpret())
        } else {
            uiControlHide(this.reinterpret())
        }
    }
    set(enabled) {
        if (it) {
            uiControlEnable(this.reinterpret())
        } else {
            uiControlDisable(this.reinterpret())
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
@Composable
internal fun <T: Any> rememberStableRef(data: T): StableRef<T> {
    class Wrapper(val ref: StableRef<T>) : RememberObserver {
        override fun onAbandoned() {
            ref.dispose()
        }

        override fun onForgotten() {
            ref.dispose()
        }

        override fun onRemembered() {}
    }

    return remember(data) { Wrapper(StableRef.create(data)) }.ref
}

abstract class SingletonApplier<T> : AbstractApplier<T?>(null) {
    protected abstract fun setItem(item: T?)

    override fun insertTopDown(index: Int, instance: T?) {
        setItem(instance)
    }

    override fun insertBottomUp(index: Int, instance: T?) {
        // Ignore, we have a single value
    }

    override fun remove(index: Int, count: Int) {
        require(index == 0)
        require(count <= 1)
        if (count > 0) {
            setItem(null)
        }
    }

    override fun move(from: Int, to: Int, count: Int) {
        require(count == 0)
    }

    override fun onClear() {
        setItem(null)
    }
}

/**
 * Abstract applier for components that can have multiple children.
 * Provides default implementations for common operations, but requires subclasses to implement
 * [deleteItem] and [appendItem].
 */
@OptIn(ExperimentalForeignApi::class)
abstract class AppendDeleteApplier : Applier<CPointer<uiControl>?> {
    /**
     * Deletes the item at the specified index.
     * 
     * @param index The index of the item to delete.
     */
    abstract fun deleteItem(index: Int)

    /**
     * Appends an item to the end of the container.
     * 
     * @param instance The control to append.
     */
    abstract fun appendItem(instance: CPointer<uiControl>?)

    /**
     * Inserts an item at the specified index.
     * Subclasses should override this method to use the native insertAt method if available.
     * 
     * @param index The index at which to insert the item.
     * @param instance The control to insert.
     */
    @OptIn(ExperimentalForeignApi::class)
    open fun insertItemAt(index: Int, instance: CPointer<uiControl>?) {
        // Default implementation removes all controls after the insertion index and re-adds them
        // Subclasses should override this method if a more efficient implementation is available
        val tempControls = controls.drop(index).toList()

        // Remove all controls after the insertion index
        for (i in controls.lastIndex downTo index) {
            deleteItem(i)
        }

        // Add the new control
        appendItem(instance)

        // Re-add the removed controls
        for (control in tempControls) {
            appendItem(control)
        }
    }

    /**
     * Moves items from one position to another.
     * Subclasses can override this method to use the native move method if available.
     * 
     * @param from The starting index.
     * @param to The destination index.
     * @param count The number of items to move.
     */
    @OptIn(ExperimentalForeignApi::class)
    open fun moveItems(from: Int, to: Int, count: Int) {
        // Default implementation removes and re-adds all items
        // Subclasses should override this method if a more efficient implementation is available
        for (i in controls.lastIndex downTo 0) {
            deleteItem(i)
        }
        for (control in controls) {
            appendItem(control)
        }
    }

    val controls = mutableListOf<CPointer<uiControl>>()
    private val listApplier = MutableListApplier(controls)

    override fun clear() {
        for (i in controls.lastIndex downTo 0) {
            deleteItem(i)
        }
        listApplier.clear()
    }

    override fun remove(index: Int, count: Int) {
        listApplier.remove(index, count)
        repeat(count) {
            deleteItem(index)
        }
    }

    override fun move(from: Int, to: Int, count: Int) {
        listApplier.move(from, to, count)
        moveItems(from, to, count)
    }

    override fun insertTopDown(index: Int, instance: CPointer<uiControl>?) {
        insertItemAt(index, instance)
        listApplier.insertTopDown(index, instance)
    }

    override fun insertBottomUp(index: Int, instance: CPointer<uiControl>?) {
        listApplier.insertBottomUp(index, instance)
    }

    override val current: CPointer<uiControl>? get() = listApplier.current

    override fun up() {
        listApplier.up()
    }

    override fun down(node: CPointer<uiControl>?) {
        listApplier.down(node)
    }
}
