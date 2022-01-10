package com.xpl0t.scany.dependencyinjection

import com.xpl0t.scany.settings.SettingsService
import com.xpl0t.scany.settings.SettingsServiceMockImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

@Module
@InstallIn(ActivityComponent::class)
abstract class HiltModule {

    @Binds
    abstract fun bindSettingsService(settingsService: SettingsServiceMockImpl): SettingsService

}