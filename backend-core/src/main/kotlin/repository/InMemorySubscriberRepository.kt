package com.viroge.newsletter.repository

import com.viroge.newsletter.domain.Subscriber
import com.viroge.newsletter.repository.SubscriberRepository
import java.util.concurrent.ConcurrentHashMap

class InMemorySubscriberRepository : SubscriberRepository {

    private val storage = ConcurrentHashMap<String, Subscriber>()

    override fun save(subscriber: Subscriber): Subscriber {
        storage[subscriber.id] = subscriber
        return subscriber
    }

    override fun findByEmail(email: String): Subscriber? =
        storage.values.firstOrNull { it.email == email }

    override fun findById(id: String): Subscriber? =
        storage[id]

    override fun findAll(): List<Subscriber> =
        storage.values.toList()
}