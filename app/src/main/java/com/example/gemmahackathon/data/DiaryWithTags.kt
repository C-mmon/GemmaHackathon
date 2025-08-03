package com.example.gemmahackathon.data
import androidx.room.Embedded
import androidx.room.Relation

//This class performs a relation mapping that let you fetch a diary entry together with all
// of its associated tags
data class DiaryWithTags(
    @Embedded val diaryEntry: DiaryEntry, //Tells Room to embed the DiaryEntry into this class
    @Relation( //Set up a one-to-many relationship between DiaryEntry and Tag
        parentColumn = "id", //Primary Key of the DiaryEntry
        entityColumn = "entryId" //Foreign Key of the Tag
    )
    val tags: List<Tag>
)

//Now query with diary entry row, for each row, look up all Tags rows where entryId == DiaryEntry.di
// Bundle them together into the tags: ListTags