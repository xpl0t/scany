package com.xpl0t.scany.dependencyinjection

import com.xpl0t.scany.filter.BlurFilter
import com.xpl0t.scany.filter.FilterList
import com.xpl0t.scany.filter.GrayscaleFilter
import com.xpl0t.scany.filter.SharpenFilter
import com.xpl0t.scany.repository.Repository
import com.xpl0t.scany.repository.RepositoryImpl
import com.xpl0t.scany.settings.SettingsService
import com.xpl0t.scany.settings.SettingsServiceMockImpl
import com.xpl0t.scany.ui.scanlist.scannamegenerator.RandomScanNameGenerator
import com.xpl0t.scany.ui.scanlist.scannamegenerator.ScanNameGenerator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

@Module
@InstallIn(ActivityComponent::class)
class HiltModule {

    @Provides
    fun bindSettingsService(settingsService: SettingsServiceMockImpl): SettingsService {
        return settingsService
    }

    @Provides
    fun bindRepository(repository: RepositoryImpl): Repository {
        return repository
    }

    @Provides
    fun bindScanNameGenerator(generator: RandomScanNameGenerator): ScanNameGenerator {
        return generator
    }

    @Provides
    fun bindFilters(
        grayscaleFilter: GrayscaleFilter,
        sharpenFilter: SharpenFilter,
        blurFilter: BlurFilter
    ): FilterList {
        return listOf(
            grayscaleFilter,
            sharpenFilter,
            blurFilter
        )
    }
}