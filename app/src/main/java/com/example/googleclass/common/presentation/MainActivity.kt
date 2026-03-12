package com.example.googleclass.common.presentation

import android.Manifest
import android.content.ClipData
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import com.example.googleclass.common.navigation.AppNavGraph
import com.example.googleclass.common.navigation.ScreenRoute
import com.example.googleclass.common.presentation.theme.GoogleClassTheme
import com.example.googleclass.feature.authorization.data.TokenStorage
import com.example.googleclass.feature.authorization.domain.SessionExpiredNotifier
import org.koin.android.ext.android.inject
import org.koin.core.component.KoinComponent

class MainActivity : ComponentActivity(), KoinComponent {

    private val tokenStorage: TokenStorage by inject()
    private val sessionExpiredNotifier: SessionExpiredNotifier by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestNotificationPermissionIfNeeded()
        handleOpenFileFromNotification(intent)
        val startDestination = if (tokenStorage.getTokens() != null) {
            ScreenRoute.Courses.route
        } else {
            ScreenRoute.Authorization.route
        }
        setContent {
            GoogleClassTheme {
                val navController = rememberNavController()
                LaunchedEffect(sessionExpiredNotifier) {
                    sessionExpiredNotifier.sessionExpiredEvents.collect {
                        navController.navigate(ScreenRoute.Authorization.route) {
                            popUpTo(ScreenRoute.Courses.route) { inclusive = true }
                        }
                    }
                }
                AppNavGraph(
                    navController = navController,
                    startDestination = startDestination,
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleOpenFileFromNotification(intent)
    }

    private fun handleOpenFileFromNotification(intent: Intent?) {
        val uriString = intent?.getStringExtra(EXTRA_OPEN_FILE_URI) ?: return
        val mimeType = intent?.getStringExtra(EXTRA_OPEN_FILE_MIME) ?: "application/octet-stream"
        intent.removeExtra(EXTRA_OPEN_FILE_URI)
        intent.removeExtra(EXTRA_OPEN_FILE_MIME)
        val uri = Uri.parse(uriString)
        try {
            val viewIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    clipData = ClipData.newRawUri("", uri)
                }
            }
            val chooser = Intent.createChooser(viewIntent, null).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(this, getString(com.example.googleclass.R.string.download_notification_error_text), Toast.LENGTH_SHORT).show()
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { }
                .launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    companion object {
        const val ACTION_OPEN_DOWNLOADED_FILE = "com.example.googleclass.ACTION_OPEN_DOWNLOADED_FILE"
        const val EXTRA_OPEN_FILE_URI = "extra_open_file_uri"
        const val EXTRA_OPEN_FILE_MIME = "extra_open_file_mime"
    }
}
