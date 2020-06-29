// Copyright (C) 2020 - UMons
// 
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
// 
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
// 
// You should have received a copy of the GNU General Public License
// along with this program.  If not, see <https://www.gnu.org/licenses/>.

package com.numediart.reviewapp.models;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import com.numediart.reviewapp.database.DataConverter;

import java.util.Date;

@Entity
public class Review {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @SerializedName("Title")
    @Expose
    private String title;

    @SerializedName("Content")
    @Expose
    private String content;

    @SerializedName("Author")
    @Expose
    private String author;

    @SerializedName("Session")
    @Expose
    private String sessionKey;

    @TypeConverters(DataConverter.class)
    @SerializedName("Date")
    @Expose
    private Date date;

    @SerializedName("Emotion")
    @Expose
    @Embedded
    private Emotion emotion;

    @SerializedName("Location")
    @Expose
    @Embedded
    private Location location;

    @TypeConverters(DataConverter.class)
    @SerializedName("Image")
    @Expose
    private Uri picture;

    private int sent;
    private boolean isComplete;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Emotion getEmotion() {
        return emotion;
    }

    public void setEmotion(Emotion emotion) {
        this.emotion = emotion;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Uri getPicture() {
        return picture;
    }

    public void setPicture(Uri picture) {
        this.picture = picture;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getSessionKey() {
        return sessionKey;
    }

    public void setSessionKey(String sessionKey) {
        this.sessionKey = sessionKey;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int getSent() {
        return sent;
    }

    public void setSent(int sent) {
        this.sent = sent;
    }

    public boolean isComplete() {
        return isComplete;
    }

    public void setComplete(boolean complete) {
        isComplete = complete;
    }

    @NonNull
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("Id: ").append(id);
        sb.append("\nTitle: ").append(title);
        sb.append("\nContent: ").append(content);
        sb.append("\nLocation: ").append(location != null ? location.toString() : "/");
        sb.append("\nEmotion Score: ").append(emotion != null ? emotion.toString() : "/");
        sb.append("\nUri Picture: ").append(picture != null ? picture.toString() : "/");
        sb.append("\nDate: ").append(date);
        sb.append("\nAuthor: ").append(author);
        sb.append("\nSession Key: ").append(sessionKey);
        sb.append("\nSent: ").append(sent);
        sb.append("\nComplete: ").append(isComplete);

        return sb.toString();
    }
}
