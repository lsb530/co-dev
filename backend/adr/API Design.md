# Architectural Decision Record

## REST API Design

* api는 versioning을 명시한다(v1, v2)
* 2음절 이상일 경우, url: kebab-case / path variable: camelCase로 명시한다
* 파일 확장자(file extension)은 포함하지 않는다
* ?로 시작하거나 :로 시작하는 부분은 query string 또는 path variable
* 목록, 단건은 목록(조회), 단건(조회)로 지칭하며 목록조회는 묵시적으로 List 또는 Pagination 중 하나로 처리
* 회원가입, 코드인증, 로그인을 제외한 모든 API는 인증(Authentication)이 요구됨
* 권한/인가(Authorization)는 최소 권한만 명시(ex: Admin/Manager/Worker 中, Woker만 명시되어 있으면 Admin/Manager 둘 다 가능)
* Response HTTP Status Code
    * 2xx: 200, 201(location), 204
    * 3xx: x(no redirect/no forward)
    * 4xx: 400, 401, 403, 404, 409
    * 5xx: 500, 501, 502(nginx)

### 인증(/api/v1/auth)

| **_Method_** | **_Path_**    | **_Code_** | **_Permission_** | **_Action_** |
|--------------|---------------|------------|------------------|--------------|
| POST         | /register     | 201        | x                | 회원가입         |
| POST         | /verify/:code | 200        | x                | 코드인증         |
| POST         | /login        | 200        | x                | 로그인          |
| DELETE       | /logout       | 204        | o                | 로그아웃         |
| GET          | /me           | 200        | o                | 내정보          |

### 유저(/api/v1/users)

| **_Method_** | **_Path_** | **_Code_** | **_Permission_** | **_Action_** |
|--------------|------------|------------|------------------|--------------|
| GET          | /          | 200        | o(admin)         | 목록           |
| GET          | /:userId   | 200        | o(admin)         | 단건           |
| PATCH        | /:userId   | 200        | o                | 회원정보 수정      |
| DELETE       | /:userId   | 204        | o                | 탈퇴           |

### 프로젝트(/api/v1/projects)

| **_Method_** | **_Path_**  | **_Code_** | **_Permission_** | **_Action_** |
|--------------|-------------|------------|------------------|--------------|
| POST         | /           | 201        | o(manager)       | 생성           |
| PATCH        | /:projectId | 200        | o(manager)       | 수정           |
| DELETE       | /:projectId | 204        | o(manager)       | 삭제           |
| GET          | /           | 200        | o(manager)       | 목록           |
| GET          | /:projectId | 200        | o(worker)        | 단건           |

### 작업(/api/v1/tasks)

| **_Method_** | **_Path_** | **_Code_** | **_Permission_** | **_Action_** |
|--------------|------------|------------|------------------|--------------|
| POST         | /          | 201        | o(manager)       | 생성           |
| PATCH        | /:taskId   | 200        | o(manager)       | 수정           |
| DELETE       | /:taskId   | 204        | o(manager)       | 삭제           |
| GET          | /          | 200        | o(worker)        | 목록           |
| GET          | /:taskId   | 200        | o(worker)        | 단건           |

### 댓글(/api/v1/comments)

| **_Method_** | **_Path_**  | **_Code_** | **_Permission_** | **_Action_** |
|--------------|-------------|------------|------------------|--------------|
| POST         | /           | 201        | o(worker)        | 생성           |
| PATCH        | /:commentId | 200        | o(worker)        | 수정           |
| DELETE       | /:commentId | 204        | o(worker)        | 삭제           |
| GET          | /           | 200        | o(worker)        | 목록           |

### 태그(/api/v1/tags)

| **_Method_** | **_Path_** | **_Code_** | **_Permission_** | **_Action_** |
|--------------|------------|------------|------------------|--------------|
| POST         | /          | 201        | o(manager)       | 생성           |
| DELETE       | /:tagId    | 204        | o(manager)       | 삭제           |
| GET          | /          | 200        | o(worker)        | 목록           |