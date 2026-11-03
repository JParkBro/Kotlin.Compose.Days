package jpark.bro.days.feature.settings.impl.account

import jpark.bro.days.core.model.AuthUser

internal data class AccountState(
    val user: AuthUser? = null,
    val isSigningOut: Boolean = false,
)
