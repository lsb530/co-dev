package com.boki.codev.fixture

import com.boki.codev.entity.user.Role
import com.boki.codev.entity.user.User

val adminUser = User(
    id = 1L,
    email = "admin@co-dev.com",
    password = "admin",
    username = "admin",
    role = Role.ADMIN,
)

val managerUser = User(
    id = 2L,
    email = "manager@co-dev.com",
    password = "manager",
    username = "manager",
    role = Role.MANAGER,
)

val workerUser = User(
    id = 3L,
    email = "worker@co-dev.com",
    password = "worker",
    username = "worker",
    role = Role.WORKER,
)