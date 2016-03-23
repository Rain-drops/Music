package com.sgj.john.mytimber.helpers;

import android.os.Parcel;
import android.os.Parcelable;

import com.sgj.john.mytimber.utils.TimberUtils;

/**
 * Created by John on 2016/3/11.
 *
 */
public class MusicPlaybackTrack implements Parcelable {

    public long mId;
    public long mSourceId;
    public TimberUtils.IdType mSourceType;
    public int mSourcePosition;

    public MusicPlaybackTrack(long mId, long mSourceId, TimberUtils.IdType mSourceType, int mSourcePosition) {
        this.mId = mId;
        this.mSourceId = mSourceId;
        this.mSourceType = mSourceType;
        this.mSourcePosition = mSourcePosition;
    }

    public MusicPlaybackTrack(Parcel in) {
        mId = in.readLong();
        mSourceId = in.readLong();
        mSourceType = TimberUtils.IdType.getTypeById(in.readInt());
        mSourcePosition = in.readInt();
    }

    public static final Creator<MusicPlaybackTrack> CREATOR = new Creator<MusicPlaybackTrack>() {
        @Override
        public MusicPlaybackTrack createFromParcel(Parcel in) {
            return new MusicPlaybackTrack(in);
        }

        @Override
        public MusicPlaybackTrack[] newArray(int size) {
            return new MusicPlaybackTrack[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof MusicPlaybackTrack) {
            MusicPlaybackTrack other = (MusicPlaybackTrack) o;
            if (other != null) {
                return mId == other.mId
                        && mSourceId == other.mSourceId
                        && mSourceType == other.mSourceType
                        && mSourcePosition == other.mSourcePosition;

            }
        }

        return super.equals(o);

    }

    @Override
    public int hashCode() {
        int result = (int) (mId ^ (mId >>> 32));
        result = 31 * result + (int) (mSourceId ^ (mSourceId >>> 32));
        result = 31 * result + (mSourceType != null ? mSourceType.hashCode() : 0);
        result = 31 * result + mSourcePosition;
        return result;
    }
}
