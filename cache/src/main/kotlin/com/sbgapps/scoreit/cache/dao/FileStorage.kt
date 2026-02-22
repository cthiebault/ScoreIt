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

package com.sbgapps.scoreit.cache.dao

import android.content.Context
import java.io.File

interface FileStorage {
    fun loadFile(directoryName: String, fileName: String): String
    fun saveFile(directoryName: String, fileName: String, data: String)
    fun createDirectory(directoryName: String)
    fun getSavedFiles(directoryName: String): List<Pair<String, Long>>
    fun removeFile(directoryName: String, fileName: String)
    fun renameFile(directoryName: String, oldFileName: String, newFileName: String)
}

class ScoreItFileStorage(context: Context) : FileStorage {

    private val internalDirectory = context.filesDir

    override fun loadFile(directoryName: String, fileName: String): String =
        File(internalDirectory, "$directoryName/$fileName").readText()

    override fun saveFile(directoryName: String, fileName: String, data: String) {
        File(internalDirectory, "$directoryName/$fileName").writeText(data)
    }

    override fun createDirectory(directoryName: String) {
        File(internalDirectory, directoryName).mkdirs()
    }

    override fun getSavedFiles(directoryName: String): List<Pair<String, Long>> =
        File(internalDirectory, directoryName).listFiles()?.map {
            it.name to it.lastModified()
        } ?: emptyList()

    override fun removeFile(directoryName: String, fileName: String) {
        File(internalDirectory, "$directoryName/$fileName").delete()
    }

    override fun renameFile(directoryName: String, oldFileName: String, newFileName: String) {
        val dir = File(internalDirectory, directoryName)
        File(dir, oldFileName).renameTo(File(dir, newFileName))
    }
}
