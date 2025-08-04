package com.example.gemmahackathon.data.diary

import androidx.room.*

//Each tag row belongs to one diary entry
@Entity(
    tableName = "tags",
    foreignKeys = [ForeignKey(
        entity = DiaryEntry::class,
        parentColumns = ["id"], //Primary key of diary entry
        childColumns = ["entryId"],//Foreign key of tag
        onDelete = ForeignKey.CASCADE //When diary entry is deleted, delete all associated tags
    )],
    indices = [Index("entryId")]
)

data class Tag(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val entryId: Long, //Links the tag back to the diary entry it belongs to  (Foreign Key)
    val name: String //The actual tag content
)