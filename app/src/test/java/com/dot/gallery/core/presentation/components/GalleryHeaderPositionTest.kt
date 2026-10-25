package com.dot.gallery.core.presentation.components

import org.junit.Assert.assertEquals
import org.junit.Test

class GalleryHeaderPositionTest {
    @Test fun scrollbarJumpCollapsesWithoutNestedScroll() {
        assertEquals(-112f, galleryHeaderOffset(500, 0, -112f))
    }

    @Test fun reverseScrollRemainsCompactUntilContentReturnsToStart() {
        for (index in listOf(500, 100, 1)) {
            assertEquals(-112f, galleryHeaderOffset(index, 0, -112f))
        }
        assertEquals(-112f, galleryHeaderOffset(0, 180, -112f))
        assertEquals(-40f, galleryHeaderOffset(0, 40, -112f))
        assertEquals(0f, galleryHeaderOffset(0, 0, -112f), 0f)
    }

    @Test fun shortScrollMovesTitleWithPhotosWithoutSnappingOpen() {
        assertEquals(-12f, galleryHeaderOffset(0, 12, -112f))
        assertEquals(-40f, galleryHeaderOffset(0, 40, -40f))
    }
}
