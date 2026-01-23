package com.viroge.newsletter.repository

import com.viroge.newsletter.domain.Subscriber

interface SubscriberRepository {
    fun save(subscriber: Subscriber): Subscriber
    fun findByEmail(email: String): Subscriber?
    fun findById(id: String): Subscriber?
    fun findAll(): List<Subscriber>
}
