package com.viroge.newsletter.domain

interface SubscriberRepository {
    fun save(subscriber: Subscriber): Subscriber
    fun findByEmail(email: String): Subscriber?
    fun findById(id: String): Subscriber?
}
