package ru.misis.booking.domain

import ru.misis.booking.domain.enums.TableStatus
import ru.misis.booking.domain.model.RestaurantTable
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class RestaurantTableTest {

    @Test
    fun `status can be changed`() {
        val table = RestaurantTable(4, 0f, 0f)
        assertEquals(TableStatus.FREE, table.status)
        table.status = TableStatus.BUSY
        assertEquals(TableStatus.BUSY, table.status)
    }
}
