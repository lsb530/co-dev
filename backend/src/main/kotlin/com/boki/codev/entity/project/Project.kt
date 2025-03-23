package com.boki.codev.entity.project

import com.boki.codev.entity.BaseEntity
import com.boki.codev.entity.task.Task
import com.boki.codev.entity.user.User
import com.boki.codev.entity.worker.Worker
import jakarta.persistence.*
import java.time.LocalDateTime

@Table(name = "projects")
@Entity
class Project(
    @Column
    var name: String,

    @Lob
    @Column(length = 256) // TINYTEXT to TEXT
    var description: String,

    @Embedded
    val projectStatusWrapper: ProjectStatusWrapper,

    @Column(nullable = true)
    val startDt: LocalDateTime? = LocalDateTime.now(),

    @Column(nullable = true)
    var endDt: LocalDateTime? = null,

    // 프로젝트 담당자가 퇴사할 수도 있기 때문에 외래키 nullable처리
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", foreignKey = ForeignKey(name = "fk_project_user_id"), nullable = true)
    var owner: User?,

    @OneToMany(mappedBy = "project")
    val workers: List<Worker> = mutableListOf(),

    @OneToMany(mappedBy = "project")
    val tasks: List<Task> = mutableListOf(),

    @ElementCollection
    @CollectionTable(name = "project_tags")
    val tags: Set<String> = mutableSetOf(),

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
): BaseEntity() {
    override fun toString(): String {
        return "Project(id=$id, name='$name', description='$description', projectStatus=$projectStatusWrapper)"
    }
}