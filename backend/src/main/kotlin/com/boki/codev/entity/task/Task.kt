package com.boki.codev.entity.task

import com.boki.codev.entity.BaseEntity
import com.boki.codev.entity.assigntask.AssignTask
import com.boki.codev.entity.comment.Comment
import com.boki.codev.entity.project.Project
import jakarta.persistence.*

@Table(name = "tasks")
@Entity
class Task(
    var title: String,

    @Lob
    @Column(length = 256) // TINYTEXT to TEXT
    var description: String,

    @Enumerated(EnumType.STRING)
    var status: TaskStatus,

    @ElementCollection
    @CollectionTable(name = "task_tags")
    val tags: Set<String> = mutableSetOf(),

    @OneToMany(mappedBy = "task")
    val comments: List<Comment> = mutableListOf(),

    @OneToMany(mappedBy = "task")
    val assignedTasks: List<AssignTask> = mutableListOf(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", foreignKey = ForeignKey(name = "fk_task_project_id"), nullable = false)
    val project: Project,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
): BaseEntity()