package com.boki.codev.entity.project

import jakarta.persistence.Embeddable
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated

@Embeddable
class ProjectStatusWrapper(
    var value: Int,

    @Enumerated(EnumType.STRING)
    var projectStatus: ProjectStatus,
) {
    constructor(projectStatus: ProjectStatus) : this(
        projectStatus = projectStatus,
        value = projectStatus.ordinal
    )

    fun updateStatus(projectStatus: ProjectStatus) {
        this.projectStatus = projectStatus
        this.value = projectStatus.ordinal
    }

}
