package com.boki.codev.entity.worker

import com.boki.codev.entity.BaseEntity
import com.boki.codev.entity.project.Project
import com.boki.codev.entity.user.Role
import com.boki.codev.entity.user.User
import jakarta.persistence.*
import java.time.LocalDateTime

@Table(name = "workers")
@Entity
class Worker(
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = true)
    val role: Role = Role.WORKER,

    val joinedAt: LocalDateTime = LocalDateTime.now(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", foreignKey = ForeignKey(name = "fk_worker_user_id"), nullable = false)
    val user: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", foreignKey = ForeignKey(name = "fk_worker_project_id"), nullable = false)
    val project: Project,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
): BaseEntity()