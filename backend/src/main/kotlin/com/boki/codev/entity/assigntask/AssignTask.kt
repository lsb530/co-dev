package com.boki.codev.entity.assigntask

import com.boki.codev.entity.task.Task
import com.boki.codev.entity.user.User
import jakarta.persistence.*

@Table(name = "assign_tasks")
@Entity
class AssignTask(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", foreignKey = ForeignKey(name = "fk_assign_user_id"), nullable = false)
    val user: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", foreignKey = ForeignKey(name = "fk_assign_task_id"), nullable = false)
    val task: Task,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null
)