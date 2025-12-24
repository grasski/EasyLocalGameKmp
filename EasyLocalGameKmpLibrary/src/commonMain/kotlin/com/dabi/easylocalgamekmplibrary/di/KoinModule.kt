package com.dabi.easylocalgamekmplibrary.di

import org.koin.core.module.Module

/**
 * Platform-specific Koin module definition.
 * Each platform provides its own implementation of the Nearby Connections manager.
 */
expect val easyLocalGameModule: Module
