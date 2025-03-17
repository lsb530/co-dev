package com.boki.codev.entity.user

enum class Role {
    ADMIN, // 어드민계정. User의 권한을 변경할 수 있고, 백오피스 접근 가능
    MANAGER, // 프로젝트를 생성하거나 상태를 바꿀 수 있고, Task 생성 & 상태 변경 가능, Task 지정 가능
    WORKER, // Task에 Comment를 추가할 수 있음, Task의 상태변경을 요청할 수 있음
}