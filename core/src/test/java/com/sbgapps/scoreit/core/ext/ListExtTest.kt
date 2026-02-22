package com.sbgapps.scoreit.core.ext

import junit.framework.Assert.assertEquals
import org.junit.Test

class ListExtTest {

    @Test
    fun `remplace un élément au milieu de la liste`() {
        val list = listOf("a", "b", "c")
        assertEquals(listOf("a", "X", "c"), list.replace(1, "X"))
    }

    @Test
    fun `remplace le premier élément`() {
        val list = listOf(1, 2, 3)
        assertEquals(listOf(99, 2, 3), list.replace(0, 99))
    }

    @Test
    fun `remplace le dernier élément`() {
        val list = listOf(1, 2, 3)
        assertEquals(listOf(1, 2, 99), list.replace(2, 99))
    }

    @Test
    fun `ne modifie pas la liste originale`() {
        val list = listOf("a", "b", "c")
        list.replace(1, "X")
        assertEquals(listOf("a", "b", "c"), list)
    }
}
