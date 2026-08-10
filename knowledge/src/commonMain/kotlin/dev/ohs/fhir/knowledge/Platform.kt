/*
 * Copyright 2026 Open Health Stack Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.ohs.fhir.knowledge

import androidx.room3.RoomDatabase
import dev.ohs.fhir.knowledge.db.KnowledgeDatabase
import kotlinx.coroutines.CoroutineDispatcher
import okio.FileSystem
import okio.Path

/**
 * Returns a builder for the metadata database, backed by the bundled SQLite driver.
 *
 * @param platformContext the application `Context` on Android; ignored on the other platforms.
 * @param inMemory whether the database lives only for as long as the process does.
 */
internal expect fun knowledgeDatabaseBuilder(
  platformContext: Any,
  inMemory: Boolean,
): RoomDatabase.Builder<KnowledgeDatabase>

/**
 * Returns the directory the knowledge manager stores downloaded NPM packages under, unless the
 * caller names one of its own.
 *
 * @param platformContext the application `Context` on Android; ignored on the other platforms.
 */
internal expect fun defaultNpmDirectory(platformContext: Any): Path

/**
 * The dispatcher the library reads and writes the package cache on. `Dispatchers.IO` exists on
 * every target this library builds for, but not in the common source set.
 */
internal expect val ioDispatcher: CoroutineDispatcher

/**
 * The file system the package cache lives on. As with [ioDispatcher], okio publishes
 * `FileSystem.SYSTEM` for every target this library builds for, but not for the common one.
 */
internal expect val systemFileSystem: FileSystem
