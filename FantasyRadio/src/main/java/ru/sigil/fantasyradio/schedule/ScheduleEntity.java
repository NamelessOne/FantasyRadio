package ru.sigil.fantasyradio.schedule;

import org.joda.time.DateTime;

import java.io.Serializable;

/**
 * Элемент расписания радиостанции.
 * Содержит дату начала и конца трансляции, название, ссылку на изображение (если есть), текст с подробностями.
 */
public class ScheduleEntity implements Serializable{
    private DateTime startDate;
    private DateTime endDate;
    private String title;
    private String imageURL;
    private String text;

    public DateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(DateTime startDate) {
        this.startDate = startDate;
    }

    public DateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(DateTime endDate) {
        this.endDate = endDate;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
