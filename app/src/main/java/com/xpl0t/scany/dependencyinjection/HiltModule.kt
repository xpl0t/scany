package com.xpl0t.scany.dependencyinjection

import com.xpl0t.scany.repository.Repository
import com.xpl0t.scany.repository.RepositoryMockImpl
import com.xpl0t.scany.settings.SettingsService
import com.xpl0t.scany.settings.SettingsServiceMockImpl
import com.xpl0t.scany.ui.scanlist.scannamegenerator.RandomScanNameGenerator
import com.xpl0t.scany.ui.scanlist.scannamegenerator.ScanNameGenerator
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

@Module
@InstallIn(ActivityComponent::class)
abstract class HiltModule {

    @Binds
    abstract fun bindSettingsService(settingsService: SettingsServiceMockImpl): SettingsService

    @Binds
    abstract fun bindRepository(repository: RepositoryMockImpl): Repository

    @Binds
    abstract fun bindScanNameGenerator(generator: RandomScanNameGenerator): ScanNameGenerator

}