package pt.obvionaoe.publisher.poisson

import java.util.*
import kotlin.math.ln

// poisson distribution
object PoissonUtils {
    private val random by lazy { Random() }

    fun timeToNextEvent(lambda: Double) = (-ln(1.0 - random.nextDouble()) / lambda) * 3_600_000
}
