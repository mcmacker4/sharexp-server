package es.hgg.sharexp.view.groups

import androidx.lifecycle.ViewModel
import androidx.paging.Pager
import androidx.paging.PagingConfig
import es.hgg.sharexp.service.GroupsPagingSource
import es.hgg.sharexp.service.GroupsService

class GroupsViewModel(val service: GroupsService) : ViewModel() {

    val groupsPager = Pager(PagingConfig(pageSize = 20)) { GroupsPagingSource(service) }

}