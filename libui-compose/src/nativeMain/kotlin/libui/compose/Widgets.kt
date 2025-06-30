@file:Suppress("FunctionName")

package libui.compose

import androidx.compose.runtime.*
import cnames.structs.uiButton
import cnames.structs.uiCheckbox
import cnames.structs.uiColorButton
import cnames.structs.uiCombobox
import cnames.structs.uiDateTimePicker
import cnames.structs.uiEditableCombobox
import cnames.structs.uiEntry
import cnames.structs.uiFontButton
import cnames.structs.uiLabel
import cnames.structs.uiMultilineEntry
import cnames.structs.uiProgressBar
import cnames.structs.uiRadioButtons
import cnames.structs.uiSeparator
import cnames.structs.uiSlider
import cnames.structs.uiSpinbox
import kotlinx.cinterop.*
import libui.*
import kotlin.native.concurrent.Worker

@OptIn(ExperimentalForeignApi::class)
@Composable
fun Label(
    text: String,
    enabled: Boolean = true,
    visible: Boolean = true,
) {
    val control = rememberControl { uiNewLabel(text)!! }

    ComposeNode<CPointer<uiLabel>, Applier<CPointer<uiControl>>>(
        factory = { control.ptr },
        update = {
            setCommon(enabled, visible)
            update(text) { uiLabelSetText(this, it) }
        }
    )
}

// TODO: Only use in HBox
@OptIn(ExperimentalForeignApi::class)
@Composable
fun VerticalSeparator(
    enabled: Boolean = true,
    visible: Boolean = true,
) {
    val control = rememberControl { uiNewVerticalSeparator()!! }

    ComposeNode<CPointer<uiSeparator>, Applier<CPointer<uiControl>>>(
        factory = { control.ptr },
        update = {
            setCommon(enabled, visible)
        }
    )
}

// TODO: Only use in VBox
@OptIn(ExperimentalForeignApi::class)
@Composable
fun HorizontalSeparator(
    enabled: Boolean = true,
    visible: Boolean = true,
) {
    val control = rememberControl { uiNewHorizontalSeparator()!! }

    ComposeNode<CPointer<uiSeparator>, Applier<CPointer<uiControl>>>(
        factory = { control.ptr },
        update = {
            setCommon(enabled, visible)
        }
    )
}

@OptIn(ExperimentalForeignApi::class)
@Composable
fun ProgressBar(
    value: Int = -1,
    enabled: Boolean = true,
    visible: Boolean = true,
) {
    val control = rememberControl { uiNewProgressBar()!! }

    ComposeNode<CPointer<uiProgressBar>, Applier<CPointer<uiControl>>>(
        factory = { control.ptr },
        update = {
            set(value) { uiProgressBarSetValue(this, it) }
            setCommon(enabled, visible)
        }
    )
}

@OptIn(ExperimentalForeignApi::class)
@Composable
fun Button(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    visible: Boolean = true,
) {
    val control = rememberControl { uiNewButton(text)!! }

    val callback = rememberStableRef(onClick)

    ComposeNode<CPointer<uiButton>, Applier<CPointer<uiControl>>>(
        factory = { control.ptr },
        update = {
            setCommon(enabled, visible)
            update(text) { uiButtonSetText(this, it) }
            set(callback) {
                uiButtonOnClicked(
                    this,
                    staticCFunction { _, senderData ->
                        val ref = senderData!!.asStableRef<() -> Unit>()
                        ref.get()()
                    },
                    it.asCPointer()
                )
            }
        }
    )
}

@OptIn(ExperimentalForeignApi::class)
@Composable
fun ColorButton(
    color: MutableState<Color>,
    enabled: Boolean = true,
    visible: Boolean = true,
) {
    val control = rememberControl { uiNewColorButton()!! }

    val state = rememberStableRef(color)

    ComposeNode<CPointer<uiColorButton>, Applier<CPointer<uiControl>>>(
        factory = { control.ptr },
        update = {
            setCommon(enabled, visible)
            set(color.value) { uiColorButtonSetColor(this, it.r, it.g, it.b, it.a) }
            set(state) {
                uiColorButtonOnChanged(
                    this,
                    staticCFunction { ctrl, senderData ->
                        val ref = senderData!!.asStableRef<MutableState<Color>>()
                        val array = DoubleArray(4)
                        array.usePinned { pin ->
                            uiColorButtonColor(
                                ctrl,
                                pin.addressOf(0),
                                pin.addressOf(1),
                                pin.addressOf(2),
                                pin.addressOf(3)
                            )
                        }
                        ref.get().value = Color(array[0], array[1], array[2], array[3])
                    },
                    it.asCPointer()
                )
            }
        }
    )
}

/**
 * A font button widget that allows the user to select a font.
 *
 * @param enabled Whether the font button is enabled.
 * @param visible Whether the font button is visible.
 */
@OptIn(ExperimentalForeignApi::class)
@Composable
fun FontButton(
    enabled: Boolean = true,
    visible: Boolean = true,
) {
    val control = rememberControl { uiNewFontButton()!! }

    ComposeNode<CPointer<uiFontButton>, Applier<CPointer<uiControl>>>(
        factory = { control.ptr },
        update = {
            setCommon(enabled, visible)
        }
    )
}



@OptIn(ExperimentalForeignApi::class)
@Composable
private fun Entry(
    control: Control<uiEntry>,
    text: MutableState<String>,
    readOnly: Boolean,
    enabled: Boolean,
    visible: Boolean,
) {
    val state = rememberStableRef(text)

    ComposeNode<CPointer<uiEntry>, Applier<CPointer<uiControl>>>(
        factory = { control.ptr },
        update = {
            setCommon(enabled, visible)
            set(text.value) { uiEntrySetText(this, it) }
            set(readOnly) { uiEntrySetReadOnly(this, if (it) 1 else 0) }
            set(state) {
                uiEntryOnChanged(
                    this,
                    staticCFunction { entry, senderData ->
                        val ref = senderData!!.asStableRef<MutableState<String>>()
                        val data = uiEntryText(entry)!!.uiText()
                        ref.get().value = data
                    },
                    it.asCPointer()
                )
            }
        }
    )
}

@OptIn(ExperimentalForeignApi::class)
@Composable
fun TextField(
    text: MutableState<String>,
    readOnly: Boolean = false,
    enabled: Boolean = true,
    visible: Boolean = true,
) {
    val control = rememberControl { uiNewEntry()!! }
    Entry(control, text, readOnly, enabled, visible)
}

@OptIn(ExperimentalForeignApi::class)
@Composable
fun PasswordField(
    text: MutableState<String>,
    readOnly: Boolean = false,
    enabled: Boolean = true,
    visible: Boolean = true,
) {
    val control = rememberControl { uiNewPasswordEntry()!! }
    Entry(control, text, readOnly, enabled, visible)
}

@OptIn(ExperimentalForeignApi::class)
@Composable
fun SearchField(
    text: MutableState<String>,
    readOnly: Boolean = false,
    enabled: Boolean = true,
    visible: Boolean = true,
) {
    val control = rememberControl { uiNewSearchEntry()!! }
    Entry(control, text, readOnly, enabled, visible)
}


@OptIn(ExperimentalForeignApi::class)
@Composable
private fun MultilineEntryBase(
    control: Control<uiMultilineEntry>,
    text: MutableState<String>,
    readOnly: Boolean = true,
    enabled: Boolean = true,
    visible: Boolean = true,
) {
    val state = rememberStableRef(text)

    ComposeNode<CPointer<uiMultilineEntry>, Applier<CPointer<uiControl>>>(
        factory = { control.ptr },
        update = {
            setCommon(enabled, visible)
            set(text.value) { uiMultilineEntrySetText(this, it) }
            set(readOnly) { uiMultilineEntrySetReadOnly(this, if (it) 1 else 0) }
            set(state) {
                uiMultilineEntryOnChanged(
                    this,
                    staticCFunction { entry, senderData ->
                        val ref = senderData!!.asStableRef<MutableState<String>>()
                        val data = uiMultilineEntryText(entry)!!.uiText()
                        ref.get().value = data
                    },
                    it.asCPointer()
                )
            }
        }
    )
}

@OptIn(ExperimentalForeignApi::class)
@Composable
fun MultilineEntry(
    text: MutableState<String>,
    readOnly: Boolean = true,
    enabled: Boolean = true,
    visible: Boolean = true,
) {
    val control = rememberControl { uiNewMultilineEntry()!! }
    MultilineEntryBase(control, text, readOnly, enabled, visible)
}

@OptIn(ExperimentalForeignApi::class)
@Composable
fun NonWrappingMultilineEntry(
    text: MutableState<String>,
    readOnly: Boolean = true,
    enabled: Boolean = true,
    visible: Boolean = true,
) {
    val control = rememberControl { uiNewNonWrappingMultilineEntry()!! }
    MultilineEntryBase(control, text, readOnly, enabled, visible)
}

@OptIn(ExperimentalForeignApi::class)
@Composable
fun Checkbox(
    label: String,
    checked: MutableState<Boolean>,
    enabled: Boolean = true,
    visible: Boolean = true,
) {
    val control = rememberControl { uiNewCheckbox(label)!! }

    val state = rememberStableRef(checked)

    ComposeNode<CPointer<uiCheckbox>, Applier<CPointer<uiControl>>>(
        factory = { control.ptr },
        update = {
            setCommon(enabled, visible)
            update(label) { uiCheckboxSetText(this, it) }
            set(checked.value) { uiCheckboxSetChecked(this, if (it) 1 else 0) }
            set(state) {
                uiCheckboxOnToggled(
                    this,
                    staticCFunction { entry, senderData ->
                        val ref = senderData!!.asStableRef<MutableState<Boolean>>()
                        val data = uiCheckboxChecked(entry) != 0
                        ref.get().value = data
                    },
                    it.asCPointer()
                )
            }
        }
    )
}

// Combobox

/**
 * A combobox widget that allows the user to select an item from a dropdown list.
 *
 * @param selected The index of the currently selected item.
 * @param items The list of items to display in the combobox.
 * @param enabled Whether the combobox is enabled.
 * @param visible Whether the combobox is visible.
 */
@OptIn(ExperimentalForeignApi::class)
@Composable
fun Combobox(
    selected: MutableState<Int>,
    items: List<String>,
    enabled: Boolean = true,
    visible: Boolean = true,
) {
    val state = rememberStableRef(selected)

    // We need to recreate the control when the items change
    val control = rememberControl(items.hashCode()) { 
        val combobox = uiNewCombobox()!!

        // Add all items to the combobox
        items.forEach { item ->
            uiComboboxAppend(combobox, item)
        }

        combobox
    }

    ComposeNode<CPointer<uiCombobox>, Applier<CPointer<uiControl>>>(
        factory = { control.ptr },
        update = {
            setCommon(enabled, visible)
            set(selected.value) { uiComboboxSetSelected(this, it) }
            set(state) {
                uiComboboxOnSelected(
                    this,
                    staticCFunction { entry, senderData ->
                        val ref = senderData!!.asStableRef<MutableState<Int>>()
                        val data = uiComboboxSelected(entry)
                        ref.get().value = data
                    },
                    it.asCPointer()
                )
            }
        }
    )
}

/**
 * An editable combobox widget that allows the user to select an item from a dropdown list or enter a custom value.
 *
 * @param text The current text value of the combobox.
 * @param items The list of items to display in the combobox.
 * @param enabled Whether the combobox is enabled.
 * @param visible Whether the combobox is visible.
 */
@OptIn(ExperimentalForeignApi::class)
@Composable
fun EditableCombobox(
    text: MutableState<String>,
    items: List<String>,
    enabled: Boolean = true,
    visible: Boolean = true,
) {
    val state = rememberStableRef(text)

    // We need to recreate the control when the items change
    val control = rememberControl(items.hashCode()) { 
        val combobox = uiNewEditableCombobox()!!

        // Add all items to the combobox
        items.forEach { item ->
            uiEditableComboboxAppend(combobox, item)
        }

        combobox
    }

    ComposeNode<CPointer<uiEditableCombobox>, Applier<CPointer<uiControl>>>(
        factory = { control.ptr },
        update = {
            setCommon(enabled, visible)
            set(text.value) { uiEditableComboboxSetText(this, it) }
            set(state) {
                uiEditableComboboxOnChanged(
                    this,
                    staticCFunction { entry, senderData ->
                        val ref = senderData!!.asStableRef<MutableState<String>>()
                        val data = uiEditableComboboxText(entry)!!.uiText()
                        ref.get().value = data
                    },
                    it.asCPointer()
                )
            }
        }
    )
}

// Spinbox

/**
 * A spinbox widget that allows the user to select a value from a range.
 *
 * @param value The current value of the spinbox.
 * @param min The minimum value of the spinbox.
 * @param max The maximum value of the spinbox.
 * @param enabled Whether the spinbox is enabled.
 * @param visible Whether the spinbox is visible.
 */
@OptIn(ExperimentalForeignApi::class)
@Composable
fun Spinbox(
    value: MutableState<Int>,
    min: Int,
    max: Int,
    enabled: Boolean = true,
    visible: Boolean = true,
) {
    val state = rememberStableRef(value)

    // Since min and max cannot be changed after creation, we need to recreate the control
    // when they change. We use min and max as keys to force recomposition.
    val control = rememberControl(min, max) { uiNewSpinbox(min, max)!! }

    ComposeNode<CPointer<uiSpinbox>, Applier<CPointer<uiControl>>>(
        factory = { control.ptr },
        update = {
            setCommon(enabled, visible)
            set(value.value) { uiSpinboxSetValue(this, it) }
            set(state) {
                uiSpinboxOnChanged(
                    this,
                    staticCFunction { entry, senderData ->
                        val ref = senderData!!.asStableRef<MutableState<Int>>()
                        val data = uiSpinboxValue(entry)
                        ref.get().value = data
                    },
                    it.asCPointer()
                )
            }
        }
    )
}

/**
 * A slider widget that allows the user to select a value from a range.
 *
 * @param value The current value of the slider.
 * @param min The minimum value of the slider.
 * @param max The maximum value of the slider.
 * @param enabled Whether the slider is enabled.
 * @param visible Whether the slider is visible.
 */
@OptIn(ExperimentalForeignApi::class)
@Composable
fun Slider(
    value: MutableState<Int>,
    min: Int,
    max: Int,
    enabled: Boolean = true,
    visible: Boolean = true,
) {
    val state = rememberStableRef(value)

    // Since min and max cannot be changed after creation, we need to recreate the control
    // when they change. We use min and max as keys to force recomposition.
    val control = rememberControl(min, max) { uiNewSlider(min, max)!! }

    ComposeNode<CPointer<uiSlider>, Applier<CPointer<uiControl>>>(
        factory = { control.ptr },
        update = {
            setCommon(enabled, visible)
            set(value.value) { uiSliderSetValue(this, it) }
            set(state) {
                uiSliderOnChanged(
                    this,
                    staticCFunction { entry, senderData ->
                        val ref = senderData!!.asStableRef<MutableState<Int>>()
                        val data = uiSliderValue(entry)
                        ref.get().value = data
                    },
                    it.asCPointer()
                )
            }
        }
    )
}

/**
 * A radio buttons widget that allows the user to select one option from a group of options.
 *
 * @param selected The index of the currently selected option.
 * @param options The list of options to display.
 * @param enabled Whether the radio buttons are enabled.
 * @param visible Whether the radio buttons are visible.
 */
@OptIn(ExperimentalForeignApi::class)
@Composable
fun RadioButtons(
    selected: MutableState<Int>,
    options: List<String>,
    enabled: Boolean = true,
    visible: Boolean = true,
) {
    val state = rememberStableRef(selected)

    // We need to recreate the control when the options change
    val control = rememberControl(options.hashCode()) { 
        val radioButtons = uiNewRadioButtons()!!

        // Add all options to the radio buttons
        options.forEach { option ->
            uiRadioButtonsAppend(radioButtons, option)
        }

        radioButtons
    }

    ComposeNode<CPointer<uiRadioButtons>, Applier<CPointer<uiControl>>>(
        factory = { control.ptr },
        update = {
            setCommon(enabled, visible)
            set(selected.value) { uiRadioButtonsSetSelected(this, it) }
            set(state) {
                uiRadioButtonsOnSelected(
                    this,
                    staticCFunction { entry, senderData ->
                        val ref = senderData!!.asStableRef<MutableState<Int>>()
                        val data = uiRadioButtonsSelected(entry)
                        ref.get().value = data
                    },
                    it.asCPointer()
                )
            }
        }
    )
}

/**
 * A date and time picker widget that allows the user to select a date and time.
 *
 * @param enabled Whether the date time picker is enabled.
 * @param visible Whether the date time picker is visible.
 */
@OptIn(ExperimentalForeignApi::class)
@Composable
fun DateTimePicker(
    enabled: Boolean = true,
    visible: Boolean = true,
) {
    val control = rememberControl { uiNewDateTimePicker()!! }

    ComposeNode<CPointer<uiDateTimePicker>, Applier<CPointer<uiControl>>>(
        factory = { control.ptr },
        update = {
            setCommon(enabled, visible)
        }
    )
}

/**
 * A date picker widget that allows the user to select a date.
 *
 * @param enabled Whether the date picker is enabled.
 * @param visible Whether the date picker is visible.
 */
@OptIn(ExperimentalForeignApi::class)
@Composable
fun DatePicker(
    enabled: Boolean = true,
    visible: Boolean = true,
) {
    val control = rememberControl { uiNewDatePicker()!! }

    ComposeNode<CPointer<uiDateTimePicker>, Applier<CPointer<uiControl>>>(
        factory = { control.ptr },
        update = {
            setCommon(enabled, visible)
        }
    )
}

/**
 * A time picker widget that allows the user to select a time.
 *
 * @param enabled Whether the time picker is enabled.
 * @param visible Whether the time picker is visible.
 */
@OptIn(ExperimentalForeignApi::class)
@Composable
fun TimePicker(
    enabled: Boolean = true,
    visible: Boolean = true,
) {
    val control = rememberControl { uiNewTimePicker()!! }

    ComposeNode<CPointer<uiDateTimePicker>, Applier<CPointer<uiControl>>>(
        factory = { control.ptr },
        update = {
            setCommon(enabled, visible)
        }
    )
}
