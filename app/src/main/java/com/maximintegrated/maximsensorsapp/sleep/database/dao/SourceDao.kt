package com.maximintegrated.maximsensorsapp.sleep.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.maximintegrated.maximsensorsapp.sleep.database.SearchFile
import com.maximintegrated.maximsensorsapp.sleep.database.entity.Source

@Dao
interface SourceDao {
    @Query("SELECT * FROM Source")
    fun getAll(): LiveData<List<Source>>

    @Query("SELECT * FROM Source WHERE file_name=:fileName")
    fun findByFileName(fileName: String): LiveData<Source>

    @Query("SELECT COUNT(*) as exist, :fileName as fileName, :md5 as md5  FROM Source where file_name=:fileName and md5=:md5")
    fun getByNameAndMd5(fileName: String, md5: String): LiveData<SearchFile>

    @Query("SELECT 0 as exist, file_name as fileName, md5  FROM Source where file_name in(:nameList) and md5 in(:md5List)")
    fun getListByNameAndMd5(
        nameList: List<String>,
        md5List: List<String>
    ): LiveData<List<SearchFile>>

    @Insert
    fun insertAll(vararg sources: Source)

    @Insert
    fun insert(sources: Source): Long

    @Delete
    fun delete(source: Source)

    @Query("DELETE FROM Source")
    fun deleteAll()

    @Query("DELETE FROM Source where file_name=:fileName")
    fun deleteByFileName(fileName: String)

}