package com.mrchk.pocketdeutsch.data.mapper

import com.mrchk.pocketdeutsch.data.local.CourseNodeEntity
import com.mrchk.pocketdeutsch.domain.model.CourseNode

fun CourseNodeEntity.toDomain(): CourseNode {
    return CourseNode(
        id = this.id,
        lessonId = this.lessonId,
        title = this.title,
        type = this.type,
        orderIndex = this.orderIndex,
        isCompleted = this.isCompleted
    )
}

fun CourseNode.toEntity(): CourseNodeEntity {
    return CourseNodeEntity(
        id = this.id,
        lessonId = this.lessonId,
        title = this.title,
        type = this.type,
        orderIndex = this.orderIndex,
        isCompleted = this.isCompleted
    )
}