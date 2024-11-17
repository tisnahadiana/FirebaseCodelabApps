package com.deeromptech.firebasecodelab.di

import com.deeromptech.firebasecodelab.feature.auth.login.LoginViewModel
import com.deeromptech.firebasecodelab.feature.auth.signup.RegisterViewModel
import com.deeromptech.firebasecodelab.feature.profile.ProfileViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val presentationModule = module {
    includes(viewModelModule)
}

val viewModelModule = module {
    viewModel {
        LoginViewModel()
    }

    viewModel {
        RegisterViewModel()
    }

    viewModel {
        ProfileViewModel()
    }
}