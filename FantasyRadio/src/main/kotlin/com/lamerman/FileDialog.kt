package com.lamerman

import android.app.Activity
import android.app.AlertDialog
import android.app.ListActivity
import android.content.Context
import android.os.Bundle
import android.os.Environment
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import ru.sigil.fantasyradio.R
import java.io.File
import java.io.File.separator
import java.util.*

private const val ITEM_KEY = "key"
private const val ITEM_IMAGE = "image"

/**
 * Created by namelessone
 * on 24.11.18.
 */
class FileDialog: ListActivity() {
    private val PATH_ROOT = Environment.getExternalStorageDirectory().absolutePath

    // @see https://code.google.com/p/android-file-dialog/issues/detail?id=3
    // @see
    // http://twigstechtips.blogspot.com.au/2011/11/for-my-app-moustachify-everything-i-was.html
    // This is purely a data storage class for saving information between
    // rotations
    private inner class LastConfiguration(var m_strCurrentPath: String)

    private val _pathSDCARD = Environment.getExternalStorageDirectory().absolutePath

    private var _options: FileDialogOptions? = null

    private var _path: ArrayList<String>? = null
    private var _myPath: TextView? = null
    private var _mFileName: EditText? = null

    private var _selectButton: Button? = null

    private var _layoutSelect: LinearLayout? = null
    private var _layoutCreate: LinearLayout? = null
    private var _inputManager: InputMethodManager? = null
    private var _parentPath: String? = null
    private var _currentPath = PATH_ROOT

    private val _lastPositions = HashMap<String, Int>()

    public override fun onStart() {
        super.onStart()
    }

    public override fun onStop() {
        super.onStop()
    }

    /**
     * Called when the activity is first created.
     */
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setResult(RESULT_CANCELED, intent)

        setContentView(R.layout.file_dialog_main)
        _myPath = findViewById(R.id.path)
        _mFileName = findViewById(R.id.fdEditTextFile)

        // Read options
        _options = FileDialogOptions(intent)

        // Hide the titlebar if needed
        if (_options?.titleBarForCurrentPath == true) {
            _myPath?.visibility = View.GONE
        }

        _inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        _selectButton = findViewById(R.id.fdButtonSelect)
        // selectButton.setEnabled(false);
        _selectButton?.setOnClickListener({
            returnFilename()
        })

        val newButton = findViewById<Button>(R.id.fdButtonNew)
        newButton.setOnClickListener { v ->
            setCreateVisible(v)
            _mFileName?.setText("")
            _mFileName?.requestFocus()
        }

        if (_options?.allowCreate == false) {
            newButton.isEnabled = false
        }

        _layoutSelect = findViewById(R.id.fdLinearLayoutSelect)
        _layoutCreate = findViewById(R.id.fdLinearLayoutCreate)
        _layoutCreate?.visibility = View.GONE

        // If the New button is disabled and it's one click select, hide the
        // selection layout.
        if (_options?.allowCreate == false && _options?.oneClickSelect == true) {
            _layoutSelect?.visibility = View.GONE
        }

        val cancelButton = findViewById<Button>(R.id.fdButtonCancel)
        cancelButton.setOnClickListener { v -> setSelectVisible(v) }
        val createButton = findViewById<Button>(R.id.fdButtonCreate)
        createButton.setOnClickListener {
            // Тут написать создание папки
            if (!_mFileName?.text.isNullOrEmpty()) {
                val f = File(_currentPath + separator + _mFileName?.text)
                f.mkdirs()
                // Тут надо обновить список папок
                getDir(_currentPath)
                cancelButton.performClick()
            }
        }

        // Try to restore current path after screen rotation
        val lastConfiguration = lastNonConfigurationInstance

        if (lastConfiguration != null) {
            getDir((lastConfiguration as LastConfiguration).m_strCurrentPath)
        } else {
            val file = File(_options?.currentPath)

            if (file.isDirectory && file.exists()) {
                getDir(_options?.currentPath.orEmpty())
            } else {
                getDir(PATH_ROOT)
            }
        }// New instance of FileDialog
    }

    private fun getDir(dirPath: String) {

        val useAutoSelection = dirPath.length < _currentPath.length

        val position = _lastPositions[_parentPath]

        getDirImpl(dirPath)

        if (position != null && useAutoSelection) {
            listView.setSelection(position)
        }
    }

    private fun getDirImpl(dirPath: String) {
        _currentPath = dirPath

        _path = ArrayList()
        val mList = ArrayList<HashMap<String, Any>>()

        var f = File(_currentPath)
        var files = f.listFiles()

        // Null if file is not a directory
        if (files == null) {
            _currentPath = PATH_ROOT
            f = File(_currentPath)
            files = f.listFiles()
        }

        // Sort files by alphabet and ignore casing
        if (files != null) {
            Arrays.sort(files)
        }

        if (_options?.titleBarForCurrentPath == true) {
            this.title = _currentPath
        } else {
            _myPath?.text = String.format("%s: %s", getText(R.string.location).toString(), _currentPath)
        }

        /*
		 * http://stackoverflow.com/questions/5090915/show-songs-from-sdcard
		 * http://developer.android.com/reference/android/os/Environment.html
		 * http
		 * ://stackoverflow.com/questions/5453708/android-how-to-use-environment
		 * -getexternalstoragedirectory
		 */
        if (_currentPath.equals(PATH_ROOT)) {
            val mounted = Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED

            if (mounted) {
                addItem(mList, "$_pathSDCARD(SD Card)",
                        _options?.iconSDCard?:0)
                _path?.add(_pathSDCARD)
            }
        }

        if (_currentPath != PATH_ROOT) {
            addItem(mList, "/ (Root folder)", _options?.iconUp?:0)
            _path?.add(PATH_ROOT)

            addItem(mList, "../ (Parent folder)", _options?.iconUp?:0)
            _path?.add(f.parent)
            _parentPath = f.parent
        }

        val dirsMap = TreeMap<String, String>()
        val dirsPathMap = TreeMap<String, String>()
        val filesMap = TreeMap<String, String>()
        val filesPathMap = TreeMap<String, String>()

        if (files == null) {
            Toast.makeText(baseContext, "Ошибка открытия SD-карты", Toast.LENGTH_SHORT).show()
            setResult(Activity.RESULT_CANCELED, intent)
            finish()
            return
        }
        for (file in files) {
            if (file.isDirectory) {
                val dirName = file.name
                dirsMap[dirName] = dirName
                dirsPathMap[dirName] = file.path
            } else {
                filesMap[file.name] = file.name
                filesPathMap[file.name] = file.path
            }
        }

        _path?.addAll(dirsPathMap.tailMap("").values)
        _path?.addAll(filesPathMap.tailMap("").values)

        for (dir in dirsMap.tailMap("").values) {
            addItem(mList, dir, _options?.iconFolder?:0)
        }

        val fileList = SimpleAdapter(this, mList,
                R.layout.file_dialog_row,
                arrayOf(ITEM_KEY, ITEM_IMAGE), intArrayOf(R.id.fdrowtext, R.id.fdrowimage))

        fileList.notifyDataSetChanged()

        listAdapter = fileList
    }

    private fun addItem(mList: ArrayList<HashMap<String, Any>>, fileName: String, imageId: Int) {
        val item = HashMap<String, Any>()
        item[ITEM_KEY] = fileName
        item[ITEM_IMAGE] = imageId
        mList.add(item)
    }

    override fun onListItemClick(l: ListView, v: View, position: Int, id: Long) {
        val file = File(_path?.get(position))
        setSelectVisible(v)

        if (!file.exists()) {
            AlertDialog.Builder(this)
                    .setIcon(R.drawable.notification_icon)
                    .setTitle("Does not exist.")
                    .setMessage(file.name)
                    .setPositiveButton("OK",
                            { _, _ -> }).show()
            return
        }

        if (file.isDirectory) {
            // selectButton.setEnabled(false);
            if (file.canRead()) {
                // Save the scroll position so users don't get confused when
                // they come back
                _lastPositions[_currentPath] = listView.firstVisiblePosition
                getDir(_path?.get(position).orEmpty())
            } else {
                AlertDialog.Builder(this)
                        .setIcon(R.drawable.notification_icon)
                        .setTitle(
                                "[" + file.name + "] "
                                        + getText(R.string.cant_read_folder))
                        .setPositiveButton("OK", { _, _ -> }).show()
            }
        } else {
            v.isSelected = true
            _selectButton?.isEnabled = true

            if (_options?.oneClickSelect == true) {
                _selectButton?.performClick()
            }
        }
    }

    override fun  onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            // selectButton.setEnabled(false);

            if (_layoutCreate?.visibility == View.VISIBLE) {
                _layoutCreate?.visibility = View.GONE
                _layoutSelect?.visibility = View.VISIBLE
            } else {
                if (_currentPath != PATH_ROOT) {
                    getDir(_parentPath.orEmpty())
                } else {
                    return super.onKeyDown(keyCode, event)
                }
            }

            return true
        } else {
            return super.onKeyDown(keyCode, event)
        }
    }

    private fun setCreateVisible(v: View) {
        _layoutCreate?.visibility = View.VISIBLE
        _layoutSelect?.visibility = View.GONE

        _inputManager?.hideSoftInputFromWindow(v.windowToken, 0)
        // selectButton.setEnabled(false);
    }

    private fun setSelectVisible(v: View) {
        if (_options?.oneClickSelect == true) {
            return
        }

        _layoutCreate?.visibility = View.GONE
        _layoutSelect?.visibility = View.VISIBLE

        _inputManager?.hideSoftInputFromWindow(v.windowToken, 0)
        // selectButton.setEnabled(false);
    }

    private fun returnFilename() {
        _options?.currentPath = _currentPath
        // this.options.selectedFile = filepath;

        setResult(Activity.RESULT_OK, _options?.createResultIntent())
        finish()
    }

    // Remember the information when the screen is just about to be rotated.
    // This information can be retrieved by using
    // getLastNonConfigurationInstance()
    override fun onRetainNonConfigurationInstance(): Any {
        return LastConfiguration(_currentPath)
    }
}