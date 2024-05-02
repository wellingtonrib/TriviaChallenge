package br.com.jwar.triviachallenge.presentation.screens.home

import br.com.jwar.triviachallenge.domain.model.Activity
import br.com.jwar.triviachallenge.domain.model.Unit
import br.com.jwar.triviachallenge.domain.repositories.ActivityRepository
import br.com.jwar.triviachallenge.domain.repositories.UnitRepository
import br.com.jwar.triviachallenge.presentation.model.ActivityModel
import br.com.jwar.triviachallenge.presentation.model.UnitModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

interface HomeViewReducer {

    val activityRepository: ActivityRepository
    val unitRepository: UnitRepository

    suspend fun reduce(state: HomeViewState, action: HomeViewState.Action): HomeViewState {
        return when (action) {
            is HomeViewState.Action.OnLoaded -> {
                onLoaded(action)
            }
            is HomeViewState.Action.OnUserXPUpdated -> {
                onUserXPUpdated(state, action.userXP)
            }
        }
    }

    suspend fun HomeViewReducer.onLoaded(
        action: HomeViewState.Action.OnLoaded
    ) = if (unlockUnitsOrActivitiesIfNeeded(action.units)) {
        HomeViewState.Loading
    } else {
        HomeViewState.Loaded(action.units)
    }

    fun onUserXPUpdated(state: HomeViewState, userXP: Int) = when (state) {
        is HomeViewState.Loaded -> state.copy(userXP = userXP)
        else -> state
    }

    private suspend fun unlockUnitsOrActivitiesIfNeeded(units: List<UnitModel>): Boolean {
        if (areAllUnitsLocked(units)) {
            unlockFirstUnit(units)
            return true
        }

        val lastUnlockedUnit = getLastUnlockedUnit(units)
        if (lastUnlockedUnit?.areAllActivitiesCompleted() == true) {
            unlockNextUnit(lastUnlockedUnit, units)
            return true
        }

        val lastUnlockedActivity = lastUnlockedUnit?.getLastUnlockedActivity()
        if (lastUnlockedActivity?.isCompleted == true) {
            unlockNextActivity(lastUnlockedUnit, lastUnlockedActivity)
            return true
        }

        return false
    }

    private suspend fun unlockFirstUnit(units: List<UnitModel>) {
        units.firstOrNull()?.let { firstUnit ->
            unitRepository.unlockUnit(firstUnit.id)
            unlockFirstActivity(firstUnit)
        }
    }

    private suspend fun unlockNextUnit(currentUnit: UnitModel, units: List<UnitModel>) {
        val nextLockedUnit = currentUnit.getNextLockedUnit(units)
        if (nextLockedUnit != null) {
            unitRepository.unlockUnit(nextLockedUnit.id)
            unlockFirstActivity(nextLockedUnit)
        }
    }

    private suspend fun unlockNextActivity(lastUnlockedUnit: UnitModel, lastUnlockedActivity: ActivityModel) {
        val nextLockedActivity = lastUnlockedUnit.getNextLockedActivity(lastUnlockedActivity)
        if (nextLockedActivity != null) {
            activityRepository.unlockActivity(nextLockedActivity.id)
        }
    }

    private suspend fun unlockFirstActivity(unit: UnitModel) =
        unit.activities.firstOrNull()?.let { activityRepository.unlockActivity(it.id) }

    private fun areAllUnitsLocked(units: List<UnitModel>) = units.all { !it.isUnlocked }

    private fun getLastUnlockedUnit(units: List<UnitModel>) = units.lastOrNull { it.isUnlocked }

    private fun UnitModel.areAllActivitiesCompleted() = this.activities.all { it.isCompleted }

    private fun UnitModel.getLastUnlockedActivity() =
        this.activities.lastOrNull { it.isUnlocked }

    private fun UnitModel.getNextLockedActivity(currentActivity: ActivityModel) =
        this.activities.zipWithNext().find { (current, next) ->
            current == currentActivity && next.isUnlocked.not()
        }?.second

    private fun UnitModel.getNextLockedUnit(units: List<UnitModel>) =
        units.zipWithNext().find { (current, next) ->
            current == this && next.isUnlocked.not()
        }?.second

    suspend fun List<Unit>.toUnitModels(refresh: Boolean) = combine(
        this.map { unit ->
            activityRepository.getActivities(unit.id, refresh).map { activities ->
                UnitModel.fromUnit(unit, activities.toActivityModels())
            }.distinctUntilChanged()
        }
    ) { unitModels -> unitModels.toList() }

    private fun List<Activity>.toActivityModels() = this.map { ActivityModel.fromActivity(it) }
}