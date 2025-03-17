package com.boki.codev.entity.comment

import com.boki.codev.entity.BaseEntity
import com.boki.codev.entity.task.Task
import com.boki.codev.entity.user.User
import jakarta.persistence.*

@Table(name = "comments")
@Entity
class Comment(
    @Lob
    @Column(length = 256) // TINYTEXT to TEXT
    var content: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", foreignKey = ForeignKey(name = "fk_comment_task_id"), nullable = false)
    val task: Task,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", foreignKey = ForeignKey(name = "fk_comment_user_id"), nullable = false)
    val user: User,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
): BaseEntity()