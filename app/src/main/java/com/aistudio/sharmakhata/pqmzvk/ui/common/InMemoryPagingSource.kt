package com.aistudio.sharmakhata.pqmzvk.ui.common

import androidx.paging.PagingSource
import androidx.paging.PagingState

/**
 * A [PagingSource] that wraps an in-memory list into pages.
 * Data is static once created — to react to data changes, the parent
 * should recreate the PagingSource via [flatMapLatest] or similar.
 */
class InMemoryPagingSource<T : Any>(
    private val data: List<T>
) : PagingSource<Int, T>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, T> {
        val position = params.key ?: 0
        val pageSize = params.loadSize
        if (position >= data.size) {
            return LoadResult.Page(data = emptyList(), prevKey = null, nextKey = null)
        }
        val endIndex = (position + pageSize).coerceAtMost(data.size)
        val page = data.subList(position, endIndex)
        return LoadResult.Page(
            data = page,
            prevKey = if (position == 0) null else position - pageSize,
            nextKey = if (endIndex >= data.size) null else endIndex
        )
    }

    override fun getRefreshKey(state: PagingState<Int, T>): Int? {
        return state.anchorPosition?.let { anchor ->
            val pageSize = state.config.pageSize
            (anchor / pageSize) * pageSize
        }
    }
}
