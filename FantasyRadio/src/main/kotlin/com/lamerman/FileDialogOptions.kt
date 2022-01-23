package com.lamerman

import android.os.Environment
import ru.sigil.fantasyradio.R
import android.content.Intent
import android.util.Log
import android.app.Activity

private const val SELECTION_MODE_CREATE = 0
private const val SELECTION_MODE_OPEN = 1

// This is used to configure the initial folder when it opens.
private const val START_PATH = "START_PATH"
// Deprecated: Set to SelectionMode.MODE_OPEN to disable the "New" button.
private const val SELECTION_MODE = "SELECTION_MODE"

private const val OPTION_ALLOW_CREATE = "OPTION_ALLOW_CREATE"
// Set to hide the "myPath" TextView.
private const val OPTION_CURRENT_PATH_IN_TITLEBAR = "OPTION_CURRENT_PATH_IN_TITLEBAR"
// Option for one-click select
private const val OPTION_ONE_CLICK_SELECT = "OPTION_ONE_CLICK_SELECT"
// Option for file icon.
private const val OPTION_ICON_FILE = "OPTION_ICON_FILE"
// Option for folder icon.
private const val OPTION_ICON_FOLDER = "OPTION_ICON_FOLDER"
// Option for up icon.
private const val OPTION_ICON_UP = "OPTION_ICON_UP"

// // Used to retrieve the absolute filename of the result file.
// public static final String RESULT_PATH = "RESULT_PATH";
private const val RESULT_FILE = "RESULT_FILE"
// Used to retrieve the full folder of the result file.
private const val RESULT_FOLDER = "RESULT_FOLDER"

/**
 * Created by namelessone
 * on 25.11.18.
 * Default constructor used by activities which need the FileDialog.
 */
class FileDialogOptions() {
    // Legacy

    // This is used to configure the start folder when it opens and the folder
    // of the result file.
    //public String currentPath = FileDialog.PATH_ROOT;
    var currentPath = Environment.getExternalStorageDirectory().absolutePath
    // Used to retrieve the absolute filename of the result file.
    private val selectedFile: String? = null

    // Set to enable the "New" file button.
    var allowCreate = true
    // Set to show current folder in activity titlebar and hide the "myPath"
    // TextView.
    var titleBarForCurrentPath: Boolean = false
    // Option for one-click select
    var oneClickSelect: Boolean = false

    // Option for file icon.
    private var iconFile = R.drawable.document
    // Option for folder icon.
    var iconFolder = R.drawable.folder_horizontal
    // Option for up/root icon.
    var iconUp = R.drawable.shortcut_overlay
    // Option for SDCard icon
    var iconSDCard = R.drawable.floppy

    /**
     * Constructor (used by FileDialog) which automatically reads all the intent
     * option values.
     *
     * @param intent
     * The intent passed to FileDialog.
     */
     constructor(intent: Intent) : this() {
        // Configure the initial folder when it opens.
        if (intent.hasExtra(START_PATH)) {
            currentPath = intent.getStringExtra(START_PATH)
        }

        // Allow creation of new files
        // Check the old intent for compatibility
        allowCreate = if (intent.hasExtra(SELECTION_MODE)) {
            Log.w("FileDialogOptions", "SELECTION_MODE intent value is deprecated. Use FileDialogOptions.allowCreate")
            intent.getIntExtra(SELECTION_MODE,
                    SELECTION_MODE_CREATE) == SELECTION_MODE_OPEN
        } else {
            intent.getBooleanExtra(OPTION_ALLOW_CREATE,
                    allowCreate)
        }

        // Hide the titlebar if needed
        titleBarForCurrentPath = intent.getBooleanExtra(
                OPTION_CURRENT_PATH_IN_TITLEBAR, titleBarForCurrentPath)

        // One click select
        oneClickSelect = intent.getBooleanExtra(OPTION_ONE_CLICK_SELECT,
                oneClickSelect)

        // Icons
        iconFile = intent.getIntExtra(OPTION_ICON_FILE, iconFile)
        iconFolder = intent.getIntExtra(OPTION_ICON_FOLDER,
                iconFolder)
        iconUp = intent.getIntExtra(OPTION_ICON_UP, iconUp)
    }

    /**
     * Once the options are all configured, return an intent with everything
     * set.
     *
     * @param activity
     * The activity wishing to call FileDialog.
     * @return Intent An intent which is ready to be used with
     * startActivityForResult()
     */
    fun createFileDialogIntent(activity: Activity): Intent {
        val intent = Intent(activity.baseContext, FileDialog::class.java)

        intent.putExtra(START_PATH, this.currentPath)
        intent.putExtra(OPTION_ALLOW_CREATE, this.allowCreate)
        intent.putExtra(OPTION_CURRENT_PATH_IN_TITLEBAR,
                this.titleBarForCurrentPath)
        intent.putExtra(OPTION_ONE_CLICK_SELECT, this.oneClickSelect)
        intent.putExtra(OPTION_ICON_FILE, this.iconFile)
        intent.putExtra(OPTION_ICON_FOLDER, this.iconFolder)
        intent.putExtra(OPTION_ICON_UP, this.iconUp)

        return intent
    }

    fun createResultIntent(): Intent {
        val intent = Intent()

        intent.putExtra(RESULT_FILE, this.selectedFile)
        intent.putExtra(RESULT_FOLDER, this.currentPath)

        return intent
    }

    /**
     * Returns the selected filename from the intent.
     */
    fun readResultFile(intent: Intent): String? {
        return intent.getStringExtra(RESULT_FILE)
    }

    /**
     * Returns the selected folder from the intent.
     */
    fun readResultFolder(intent: Intent): String? {
        return intent.getStringExtra(RESULT_FOLDER)
    }
}