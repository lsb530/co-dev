package com.boki.codev.entity.user

import com.boki.codev.converter.PasswordConverter
import com.boki.codev.entity.BaseEntity
import com.boki.codev.entity.assigntask.AssignTask
import com.boki.codev.entity.comment.Comment
import com.boki.codev.entity.project.Project
import com.boki.codev.entity.worker.Worker
import jakarta.persistence.*
import org.hibernate.annotations.SQLRestriction

@SQLRestriction("deleted_at IS NULL")
@Table(name = "users")
@Entity
class User(
    @Column(unique = true, nullable = false, updatable = false)
    val email: String,

    @Column(unique = false, nullable = false)
    @Convert(converter = PasswordConverter::class)
    var password: String,

    @Column(length = 20, unique = false, nullable = false)
    var username: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    val role: Role,

    @OneToMany(mappedBy = "owner")
    val projects: List<Project> = mutableListOf(),

    @OneToMany(mappedBy = "user")
    val comments: List<Comment> = mutableListOf(),

    @OneToMany(mappedBy = "user")
    val workers: List<Worker> = mutableListOf(),

    @OneToMany(mappedBy = "user")
    val assignedTasks: List<AssignTask> = mutableListOf(),

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
): BaseEntity() {
    override fun toString(): String {
        return "User(username='$username', email='$email', password='$password', role=$role, id=$id)"
    }

    val isManager: Boolean
        get() = this.role == Role.MANAGER
}