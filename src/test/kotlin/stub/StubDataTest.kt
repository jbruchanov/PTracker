package stub

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class StubDataTest {

    @Test
    fun pieChartData() {
        val stub = StubData.onlineStubHoldings()
        StubData.pieChartData()
    }
}