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

val managerUser1 = User(
    id = 2L,
    email = "manager1@co-dev.com",
    password = "manager",
    username = "manager1",
    role = Role.MANAGER,
)

val managerUser2 = User(
    id = 3L,
    email = "manager2@co-dev.com",
    password = "manager",
    username = "manager2",
    role = Role.MANAGER,
)

val workerUser = User(
    id = 4L,
    email = "worker@co-dev.com",
    password = "worker",
    username = "worker",
    role = Role.WORKER,
)