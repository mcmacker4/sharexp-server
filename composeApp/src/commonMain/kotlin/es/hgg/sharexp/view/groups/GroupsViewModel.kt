package es.hgg.sharexp.view.groups

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.collections.emptyList

class GroupsViewModel : ViewModel() {

    private val groups = MutableStateFlow<MutableList<String>>(mutableListOf())
    val state: StateFlow<List<String>> = groups.asStateFlow()

    fun updateGroups() {
        groups.update { it.apply { add("Hello") } }
    }

}