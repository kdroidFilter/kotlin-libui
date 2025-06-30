@file:OptIn(ExperimentalForeignApi::class)

package libui.compose

import androidx.compose.runtime.Applier
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposeNode
import androidx.compose.runtime.currentComposer
import androidx.compose.runtime.remember
import cnames.structs.uiTab
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import libui.*

/**
 * A tab container that allows switching between different pages of content.
 * Each child component added to the TabPane will be placed in a separate tab.
 *
 * @param enabled Whether the tab pane is enabled.
 * @param visible Whether the tab pane is visible.
 * @param content The content of the tab pane.
 */
@OptIn(ExperimentalForeignApi::class)
@Composable
fun TabPane(
    enabled: Boolean = true,
    visible: Boolean = true,
    content: @Composable () -> Unit
) {
    val control = rememberControl { uiNewTab()!! }

    handleChildren(content) { TabApplier(control.ptr) }

    ComposeNode<CPointer<uiTab>, Applier<CPointer<uiControl>>>(
        factory = { control.ptr },
        update = {
            setCommon(enabled, visible)
        }
    )
}

/**
 * A tab item that has a name and content. This function is provided for backward compatibility.
 *
 * @param name The name of the tab.
 * @param margined Whether the tab content should have margins.
 * @param content The content of the tab.
 */
@Composable
fun TabItem(
    name: String,
    margined: Boolean = true,
    content: @Composable () -> Unit
) {
    val tabName = remember { name } // Mémoriser le nom pour qu'il reste stable

    // Créer le contenu VBox en premier
    val vboxContent = @Composable {
        VBox(padded = true) {
            content()
        }
    }

    // Récupérer l'applier du TabPane parent
    val composer = currentComposer
    val applier = composer.applier

    if (applier is TabApplier) {
        // Enregistrer le nom et le flag margined pour cet onglet spécifique
        applier.registerNextTabName(tabName)
        applier.registerNextMargined(margined)

        // Ajouter le contenu
        vboxContent()
    } else {
        // Si on n'est pas dans un TabPane, on affiche juste le contenu
        vboxContent()
    }
}

/**
 * Scope for managing tabs within a TabPane.
 */
class TabPaneScope @OptIn(ExperimentalForeignApi::class) constructor(private val tab: CPointer<uiTab>) {
    /**
     * Adds a tab with the specified name and content.
     *
     * @param name The name of the tab.
     * @param margined Whether the tab content should have margins.
     * @param content The content of the tab.
     */
    @Composable
    fun page(name: String, margined: Boolean = true, content: @Composable () -> Unit) {
        // Récupérer l'applier du TabPane parent
        val tabApplier = currentComposer.applier as TabApplier

        // Enregistrer le nom et le flag margined pour cet onglet spécifique
        tabApplier.registerNextTabName(name)
        tabApplier.registerNextMargined(margined)

        // Ajouter le contenu dans un VBox
        VBox(padded = true) {
            content()
        }
    }
}

/**
 * An applier for tab containers that handles adding and removing children.
 *
 * @param tab The tab container to apply changes to.
 */
class TabApplier @OptIn(ExperimentalForeignApi::class) constructor(private val tab: CPointer<uiTab>) : AppendDeleteApplier() {
    /**
     * Liste des noms des onglets dans l'ordre d'enregistrement.
     */
    private val pendingTabNames = mutableListOf<String>()

    /**
     * Liste des flags margined pour les onglets dans l'ordre d'enregistrement.
     */
    private val pendingMargined = mutableListOf<Boolean>()

    /**
     * Enregistre le nom du prochain onglet à ajouter.
     * 
     * @param name Le nom de l'onglet.
     */
    fun registerNextTabName(name: String) {
        pendingTabNames.add(name)
    }

    /**
     * Enregistre si le prochain onglet doit avoir des marges.
     * 
     * @param margined Si l'onglet doit avoir des marges.
     */
    fun registerNextMargined(margined: Boolean) {
        pendingMargined.add(margined)
    }

    /**
     * Définit si l'onglet à l'index spécifié doit avoir des marges.
     * 
     * @param index L'index de l'onglet.
     * @param margined Si l'onglet doit avoir des marges.
     */
    @OptIn(ExperimentalForeignApi::class)
    fun setMargined(index: Int, margined: Boolean) {
        uiTabSetMargined(tab, index, if (margined) 1 else 0)
    }

    /**
     * Appends an item to the tab container.
     *
     * @param instance The control to append.
     */
    @OptIn(ExperimentalForeignApi::class)
    override fun appendItem(instance: CPointer<uiControl>?) {
        // Récupérer le nom pour l'onglet actuel ou utiliser un nom par défaut
        val name = if (pendingTabNames.isNotEmpty()) pendingTabNames.removeAt(0) else "Tab ${controls.size}"

        // Récupérer si l'onglet doit avoir des marges
        val margined = if (pendingMargined.isNotEmpty()) pendingMargined.removeAt(0) else true

        // Ajouter l'onglet avec son nom
        uiTabAppend(tab, name, instance)

        // Définir les marges
        setMargined(controls.size - 1, margined)
    }

    /**
     * Deletes an item from the tab container at the specified index.
     *
     * @param index The index of the item to delete.
     */
    @OptIn(ExperimentalForeignApi::class)
    override fun deleteItem(index: Int) {
        uiTabDelete(tab, index)
    }

    /**
     * Inserts an item into the tab container at the specified index.
     *
     * @param index The index at which to insert the item.
     * @param instance The control to insert.
     */
    override fun insertItemAt(index: Int, instance: CPointer<uiControl>?) {
        // Récupérer le nom pour l'onglet actuel ou utiliser un nom par défaut
        val name = if (pendingTabNames.isNotEmpty()) pendingTabNames.removeAt(0) else "Tab $index"

        // Récupérer si l'onglet doit avoir des marges
        val margined = if (pendingMargined.isNotEmpty()) pendingMargined.removeAt(0) else true

        // Insérer l'onglet avec son nom
        uiTabInsertAt(tab, name, index, instance)

        // Définir les marges
        setMargined(index, margined)
    }
}
