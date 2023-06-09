package com.springkotlindemo.theater.domain

import org.hibernate.Hibernate
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.ManyToOne

@Entity
data class Booking(
    @Id @GeneratedValue(strategy = GenerationType.AUTO) val id: Long,
    val customerName: String
) {
    @ManyToOne
    lateinit var seat: Seat

    @ManyToOne
    lateinit var performance: Performance
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
        other as Booking

        return id == other.id
    }

    override fun hashCode(): Int = javaClass.hashCode()

    @Override
    override fun toString(): String {
        return this::class.simpleName + "(id = $id )"
    }
}
