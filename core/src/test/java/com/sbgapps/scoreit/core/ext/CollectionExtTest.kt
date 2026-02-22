package com.sbgapps.scoreit.core.ext

import junit.framework.Assert.assertFalse
import junit.framework.Assert.assertTrue
import org.junit.Test

class CollectionExtTest {

    @Test
    fun `même contenu dans le même ordre`() {
        assertTrue(listOf(1, 2, 3) sameContentWith listOf(1, 2, 3))
    }

    @Test
    fun `même contenu dans un ordre différent`() {
        assertTrue(listOf(1, 2, 3) sameContentWith listOf(3, 1, 2))
    }

    @Test
    fun `contenu différent`() {
        assertFalse(listOf(1, 2, 3) sameContentWith listOf(1, 2, 4))
    }

    @Test
    fun `tailles différentes`() {
        assertFalse(listOf(1, 2) sameContentWith listOf(1, 2, 3))
    }

    @Test
    fun `collections vides`() {
        assertTrue(emptyList<Int>() sameContentWith emptyList())
    }
}
