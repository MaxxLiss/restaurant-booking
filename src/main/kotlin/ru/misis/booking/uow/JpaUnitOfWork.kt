package ru.misis.booking.uow

import org.springframework.stereotype.Component
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import ru.misis.booking.repository.BookingRepository
import ru.misis.booking.repository.RestaurantRepository
import ru.misis.booking.repository.UserRepository

@Component
class JpaUnitOfWork(
    override val restaurants: RestaurantRepository,
    override val users: UserRepository,
    override val bookings: BookingRepository,
    transactionManager: PlatformTransactionManager
) : IUnitOfWork {

    private val txTemplate = TransactionTemplate(transactionManager)
    private val readOnlyTxTemplate = TransactionTemplate(transactionManager).also { it.isReadOnly = true }

    override fun <T> execute(block: IUnitOfWork.() -> T): T =
        txTemplate.execute { block() }!!

    override fun <T> executeReadOnly(block: IUnitOfWork.() -> T): T =
        readOnlyTxTemplate.execute { block() }!!
}
