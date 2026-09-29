package ru.matule.app.data.repository

import ru.matule.app.data.local.PinLocalDataSource
import ru.matule.app.domain.repository.PinRepository

/**
 * PIN-код хранится только на устройстве.
 *
 * @param local хранилище PIN.
 */
class PinRepositoryImpl(private val local: PinLocalDataSource) : PinRepository {
    override suspend fun hasPin(): Boolean = local.hasPin()

    override suspend fun savePin(pin: String) = local.savePin(pin)

    override suspend fun verifyPin(pin: String): Boolean = local.verify(pin)
}
