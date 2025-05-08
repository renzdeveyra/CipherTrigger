package com.cite012a_cs32s1.ciphertrigger.di

import android.content.Context
import com.cite012a_cs32s1.ciphertrigger.data.repositories.AlertRepository
import com.cite012a_cs32s1.ciphertrigger.data.repositories.ContactRepository
import com.cite012a_cs32s1.ciphertrigger.data.repositories.LocationRepository
import com.cite012a_cs32s1.ciphertrigger.data.repositories.PreferencesRepository

/**
 * Simple dependency injection provider for the app
 */
object AppModule {
    
    private var preferencesRepository: PreferencesRepository? = null
    private var locationRepository: LocationRepository? = null
    private var contactRepository: ContactRepository? = null
    private var alertRepository: AlertRepository? = null
    
    /**
     * Initialize the repositories
     */
    fun initialize(applicationContext: Context) {
        preferencesRepository = PreferencesRepository(applicationContext)
        locationRepository = LocationRepository(applicationContext)
        contactRepository = ContactRepository(applicationContext)
        alertRepository = AlertRepository(applicationContext)
    }
    
    /**
     * Get the preferences repository
     */
    fun providePreferencesRepository(context: Context): PreferencesRepository {
        return preferencesRepository ?: PreferencesRepository(context).also {
            preferencesRepository = it
        }
    }
    
    /**
     * Get the location repository
     */
    fun provideLocationRepository(context: Context): LocationRepository {
        return locationRepository ?: LocationRepository(context).also {
            locationRepository = it
        }
    }
    
    /**
     * Get the contact repository
     */
    fun provideContactRepository(context: Context): ContactRepository {
        return contactRepository ?: ContactRepository(context).also {
            contactRepository = it
        }
    }
    
    /**
     * Get the alert repository
     */
    fun provideAlertRepository(context: Context): AlertRepository {
        return alertRepository ?: AlertRepository(context).also {
            alertRepository = it
        }
    }
}
