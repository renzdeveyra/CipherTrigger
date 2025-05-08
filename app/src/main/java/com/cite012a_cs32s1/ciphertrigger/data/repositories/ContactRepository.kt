package com.cite012a_cs32s1.ciphertrigger.data.repositories

import android.Manifest
import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.provider.ContactsContract
import androidx.core.app.ActivityCompat
import com.cite012a_cs32s1.ciphertrigger.data.models.EmergencyContact
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

/**
 * Repository for managing contacts
 */
class ContactRepository(private val context: Context) {
    
    /**
     * Check if contacts permission is granted
     */
    fun hasContactsPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    /**
     * Get all device contacts
     */
    suspend fun getDeviceContacts(): List<DeviceContact> = withContext(Dispatchers.IO) {
        if (!hasContactsPermission()) {
            return@withContext emptyList<DeviceContact>()
        }
        
        val contacts = mutableListOf<DeviceContact>()
        val contentResolver: ContentResolver = context.contentResolver
        
        val projection = arrayOf(
            ContactsContract.Contacts._ID,
            ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,
            ContactsContract.Contacts.HAS_PHONE_NUMBER,
            ContactsContract.Contacts.PHOTO_URI
        )
        
        val cursor: Cursor? = contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            projection,
            null,
            null,
            ContactsContract.Contacts.DISPLAY_NAME_PRIMARY
        )
        
        cursor?.use {
            val idIndex = it.getColumnIndex(ContactsContract.Contacts._ID)
            val nameIndex = it.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY)
            val hasPhoneIndex = it.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)
            val photoUriIndex = it.getColumnIndex(ContactsContract.Contacts.PHOTO_URI)
            
            while (it.moveToNext()) {
                val id = it.getString(idIndex)
                val name = it.getString(nameIndex) ?: "Unknown"
                val hasPhone = it.getInt(hasPhoneIndex) > 0
                val photoUri = it.getString(photoUriIndex)
                
                if (hasPhone) {
                    val phoneNumbers = getPhoneNumbers(contentResolver, id)
                    if (phoneNumbers.isNotEmpty()) {
                        contacts.add(
                            DeviceContact(
                                id = id,
                                name = name,
                                phoneNumbers = phoneNumbers,
                                photoUri = photoUri
                            )
                        )
                    }
                }
            }
        }
        
        contacts
    }
    
    /**
     * Get phone numbers for a contact
     */
    private fun getPhoneNumbers(contentResolver: ContentResolver, contactId: String): List<String> {
        val phoneNumbers = mutableListOf<String>()
        
        val phoneCursor: Cursor? = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
            "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
            arrayOf(contactId),
            null
        )
        
        phoneCursor?.use {
            val phoneNumberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            while (it.moveToNext()) {
                val phoneNumber = it.getString(phoneNumberIndex)
                if (phoneNumber != null) {
                    phoneNumbers.add(phoneNumber)
                }
            }
        }
        
        return phoneNumbers
    }
    
    /**
     * Convert a device contact to an emergency contact
     */
    fun convertToEmergencyContact(
        deviceContact: DeviceContact,
        phoneNumber: String,
        priority: Int = 0,
        sendSms: Boolean = true,
        makeCall: Boolean = false
    ): EmergencyContact {
        return EmergencyContact(
            id = UUID.randomUUID().toString(),
            name = deviceContact.name,
            phoneNumber = phoneNumber,
            photoUri = deviceContact.photoUri,
            priority = priority,
            sendSms = sendSms,
            makeCall = makeCall
        )
    }
    
    /**
     * Data class representing a device contact
     */
    data class DeviceContact(
        val id: String,
        val name: String,
        val phoneNumbers: List<String>,
        val photoUri: String? = null
    )
}
