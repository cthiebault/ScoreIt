/*
 * Copyright 2020 Stéphane Baiget
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sbgapps.scoreit.app.ui.widget

import android.graphics.Color
import androidx.annotation.ColorInt

data class LinePoint @JvmOverloads constructor(var x: Float = 0f, var y: Float = 0f) {

    constructor(x: Double, y: Double) : this(x.toFloat(), y.toFloat())

    constructor(x: Int, y: Int) : this(x.toFloat(), y.toFloat())
}

data class LineSet(
    val points: List<LinePoint>,
    @ColorInt val color: Int = Color.BLACK,
    val arePointsDisplayed: Boolean = true
) {

    fun getMinX(): Float = points.minOfOrNull { it.x } ?: 0f

    fun getMinY(): Float = points.minOfOrNull { it.y } ?: 0f

    fun getMaxX(): Float = points.maxOfOrNull { it.x } ?: 0f

    fun getMaxY(): Float = points.maxOfOrNull { it.y } ?: 0f
}
