package com.xpl0t.scany.dependencyinjection

import com.xpl0t.scany.filter.*
import com.xpl0t.scany.repository.Repository
import com.xpl0t.scany.repository.RepositoryImpl
import com.xpl0t.scany.services.pdf.scalecalculator.CenterInsideScaleCalculator
import com.xpl0t.scany.services.pdf.scalecalculator.FitScaleCalculator
import com.xpl0t.scany.services.pdf.scalecalculator.ScaleCalculatorList
import com.xpl0t.scany.settings.SettingsService
import com.xpl0t.scany.settings.SettingsServiceMockImpl
import com.xpl0t.scany.ui.documentlist.documentnamegenerator.DocumentNameGenerator
import com.xpl0t.scany.ui.documentlist.documentnamegenerator.SequentialDocumentNameGenerator
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
    fun bindDocumentNameGenerator(generator: SequentialDocumentNameGenerator): DocumentNameGenerator {
        return generator
    }

    @Provides
    fun bindFilters(
        grayscaleFilter: GrayscaleFilter,
        sharpenFilter: SharpenFilter,
        blurFilter: BlurFilter,
        binarizeFilter: BinarizeFilter
    ): FilterList {
        return listOf(
            grayscaleFilter,
            sharpenFilter,
            blurFilter,
            binarizeFilter
        )
    }

    @Provides
    fun bindScaleCalculators(
        fitScaleCalculator: FitScaleCalculator,
        centerInsideScaleCalculator: CenterInsideScaleCalculator
    ): ScaleCalculatorList {
        return listOf(
            fitScaleCalculator,
            centerInsideScaleCalculator
        )
    }

}