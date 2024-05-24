package br.com.jwar.triviachallenge.data.repositories

import br.com.jwar.triviachallenge.data.datasources.local.preferences.Preferences
import br.com.jwar.triviachallenge.domain.repositories.UserRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class UserRepositoryImpl @Inject constructor(
    private val preferences: Preferences,
) : UserRepository {
    override suspend fun getXP(): Flow<Int> = preferences.getXp()

    override suspend fun addXP(xp: Int) = preferences.addXp(xp)
}