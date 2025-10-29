package es.hgg.sharexp.server.util

/**
 * Determines the requested page and the size of the pages for a paginated resource.
 * Page numbers start at 1
 */
data class PageRequest<S : Enum<S>>(
    val page: Int,
    val size: Int,
    val sort: S,
    val asc: Boolean,
) {
    val offset: Long
        get() = (page.toLong() - 1) * size
}