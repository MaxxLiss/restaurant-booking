package ru.misis.booking.domain.model

import ru.misis.booking.domain.exceptions.InvalidArgumentException
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "reviews")
class Review(
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "restaurant_id", nullable = false)
    val restaurant: Restaurant,
    @Column(nullable = false)
    val rating: Int,
    @Column(nullable = false, length = 2000)
    var comment: String,
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    @Column(nullable = false)
    var published: Boolean = false,
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    val reviewId: Long = 0
) {
    init {
        require(rating in 1..5) { "Рейтинг должен быть от 1 до 5" }
        require(comment.isNotBlank()) { "Комментарий не может быть пустым" }
    }

    fun publish() { published = true }

    fun edit(newComment: String) {
        if (newComment.isBlank()) throw InvalidArgumentException("Комментарий не может быть пустым")
        comment = newComment
    }
}
