package com.andriosol.andriodocpro.common.entity

import io.micronaut.core.annotation.Introspected
import io.micronaut.data.annotation.*
import java.time.Instant
import java.util.UUID

@MappedEntity
@Introspected
abstract class BaseEntity(

    @field:Id
    @field:AutoPopulated
    open var id: UUID? = null,

    @DateCreated
    open var createdAt: Instant? = null,

    @DateUpdated
    open var updatedAt: Instant? = null
)