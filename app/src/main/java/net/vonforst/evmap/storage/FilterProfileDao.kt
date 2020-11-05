package net.vonforst.evmap.storage

import androidx.lifecycle.LiveData
import androidx.room.*

@Entity(indices = [Index("name", unique = true)])
data class FilterProfile(@PrimaryKey(autoGenerate = true) var id: Long, val name: String) {
    constructor(name: String) : this(0, name)
}

@Dao
interface FilterProfileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vararg profiles: FilterProfile)

    @Delete
    suspend fun delete(vararg profiles: FilterProfile)

    @Query("SELECT * FROM filterProfile")
    fun getProfiles(): LiveData<List<FilterProfile>>

    @Query("SELECT * FROM filterProfile WHERE name = :name")
    suspend fun getProfileByName(name: String): FilterProfile?

    @Transaction
    suspend fun getOrCreateProfileByName(name: String): FilterProfile {
        val profile = getProfileByName(name)
        if (profile == null) {
            insert(FilterProfile(name))
            return getProfileByName(name)!!
        } else {
            return profile
        }
    }
}