package com.taskforce.superinvention.app.domain
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime
import javax.persistence.*

@EntityListeners(AuditingEntityListener::class)
@MappedSuperclass
open class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var seq: Long? = null

    @CreatedDate
    protected  val createdAt: LocalDateTime? = LocalDateTime.now()

    @LastModifiedDate
    protected  val updatedAt: LocalDateTime? = LocalDateTime.now()

}