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
 * @param content The content of the tab.
 */
@Composable
fun TabItem(
    name: String,
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
        // Enregistrer le nom pour cet onglet spécifique
        applier.registerNextTabName(tabName)

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
        // On stocke l'index de l'onglet pour pouvoir définir ses marges après l'ajout
        val tabApplier = currentComposer.applier as TabApplier
        val index = tabApplier.controls.size

        // On mémorise le nom et on ajoute le contenu
        tabApplier.registerNextTabName(name)

        // Ajouter le contenu dans un VBox
        VBox(padded = true) {
            content()
        }

        // Définir les marges après l'ajout de l'onglet
        if (margined) {
            uiTabSetMargined(tab, index, 1)
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
     * Enregistre le nom du prochain onglet à ajouter.
     * 
     * @param name Le nom de l'onglet.
     */
    fun registerNextTabName(name: String) {
        pendingTabNames.add(name)
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

        // Ajouter l'onglet avec son nom
        uiTabAppend(tab, name, instance)
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

        // Insérer l'onglet avec son nom
        uiTabInsertAt(tab, name, index, instance)
    }
}
