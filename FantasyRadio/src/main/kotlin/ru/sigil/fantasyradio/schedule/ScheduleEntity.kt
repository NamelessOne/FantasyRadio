package ru.sigil.fantasyradio.schedule

import org.joda.time.DateTime
import java.io.Serializable

/**
 * Created by namelessone
 * on 02.12.18.
 * Элемент расписания радиостанции.
 * Содержит дату начала и конца трансляции, название, ссылку на изображение (если есть), текст с подробностями.
 */
data class ScheduleEntity(var startDate: DateTime?, var endDate: DateTime?, var title: String?,
                          var imageURL: String?, var text: String?): Serializable