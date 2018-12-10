package ru.sigil.fantasyradio.exceptions

import java.lang.Exception

/**
 * Неправильный логин/пароль.
 *
 * @see ru.sigil.fantasyradio.archive.ArchiveListAdapter#getView(int, android.view.View, android.view.ViewGroup)
 */
class WrongLoginOrPasswordException: Exception()