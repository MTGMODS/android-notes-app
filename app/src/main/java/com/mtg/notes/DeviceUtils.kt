package com.mtg.notes

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.Location
import android.net.Uri
import android.provider.Settings
import androidx.core.content.FileProvider
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.tasks.await
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DeviceUtils {

    private const val CHNU_LAT = 48.2970
    private const val CHNU_LON = 25.9228

//    @SuppressLint("MissingPermission")
//    suspend fun getCurrentLocation(context: Context): Location? {
//        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
//        return try {
//            fusedLocationClient.lastLocation.await()
//        } catch (e: Exception) {
//            null
//        }
//    }

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(context: Context): Location? {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        return try {
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                CancellationTokenSource().token
            ).await()
        } catch (e: Exception) {
            null
        }
    }

    fun calculateDistanceToCHNU(lat: Double, lon: Double): Float {
        val results = FloatArray(1)
        Location.distanceBetween(lat, lon, CHNU_LAT, CHNU_LON, results)
        return results[0]
    }


    fun createImageFile(context: Context): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale("uk", "UA")).format(Date())
        val imageFileName = "JPEG_${timeStamp}_"
        return File.createTempFile(imageFileName, ".jpg", context.filesDir)
    }

    fun getUriForFile(context: Context, file: File): Uri {
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    fun openAppSettings(context: Context) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
        }
        context.startActivity(intent)
    }
}