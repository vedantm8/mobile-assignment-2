package edu.stevens.cs522.chatserver.entities;

import android.os.Parcel;
import android.os.Parcelable;

import java.time.Instant;

/**
 * Created by dduggan.
 */

public class Peer implements Parcelable {

    // Will be database key
    public long id;

    public String name;

    // Last time we heard from this peer.
    public Instant timestamp;

    // Where we heard from them
    public Double latitude;

    public Double longitude;

    public Peer() {
    }

    @Override
    public String toString() {
        // TODO
        return null;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public Peer(Parcel in) {
        // TODO

    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        // TODO

    }

    public static final Creator<Peer> CREATOR = new Creator<Peer>() {

        @Override
        public Peer createFromParcel(Parcel source) {
            // TODO
            return null;
        }

        @Override
        public Peer[] newArray(int size) {
            // TODO
            return null;
        }

    };
}
