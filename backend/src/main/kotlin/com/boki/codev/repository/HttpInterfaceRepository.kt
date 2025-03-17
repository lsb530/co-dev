package com.boki.codev.repository

import com.boki.codev.entity.httpinterface.HttpInterface
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface HttpInterfaceRepository: CrudRepository<HttpInterface, Long>