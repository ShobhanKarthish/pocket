package com.shobhankarthish.pocket

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.shobhankarthish.pocket.shelf.ItemIngestor
import com.shobhankarthish.pocket.shelf.ShelfFileStore
import com.shobhankarthish.pocket.shelf.ShelfRepository
import com.shobhankarthish.pocket.shelf.db.PocketDatabase
import com.shobhankarthish.pocket.shelf.prefs.HowToAddPrefs

private val Context.howToAddStore by preferencesDataStore(name = "how_to_add")

class AppContainer(context: Context) {
    private val app = context.applicationContext
    private val database: PocketDatabase = Room.databaseBuilder(
        app,
        PocketDatabase::class.java,
        "pocket.db",
    ).build()

    val fileStore = ShelfFileStore(app.filesDir)
    val repository = ShelfRepository(database.shelfItemDao(), fileStore)
    val ingestor = ItemIngestor(fileStore, repository)
    val howToAddPrefs = HowToAddPrefs(app.howToAddStore)
}
