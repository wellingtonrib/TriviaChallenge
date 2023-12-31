package br.com.jwar.triviachallenge.data.mappers

import br.com.jwar.triviachallenge.domain.model.Challenge
import br.com.jwar.triviachallenge.data.services.responses.ChallengeResponse

interface ChallengeResponseToChallengeMapper {
    suspend fun mapFrom(challengeResponse: ChallengeResponse): Challenge
}