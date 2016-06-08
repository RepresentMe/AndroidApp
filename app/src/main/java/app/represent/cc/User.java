package app.represent.cc;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONObject;

/**
 * Created by Eduard Albu on 5/6/16, 05, 2016
 * for project represent
 * email eduard.albu@gmail.com
 */
public class User implements Parcelable {
    private String mUrl;
    private String mMessage;
    private String mJsonUserData;
    private String mName;
    private int mUserId;
    private String mUserName;

    private User() {

    }

    protected User(Parcel in) {
        mUrl = in.readString();
        mMessage = in.readString();
        mJsonUserData = in.readString();
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    public String getUrl() {
        return mUrl;
    }

    public String getMessage() {
        return mMessage;
    }

    public String getJsonUserData() {
        return mJsonUserData;
    }

    public String getName() {
        return mName;
    }

    public int getUserId() {
        return mUserId;
    }

    public String getUserName() {
        return mUserName;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mUrl);
        dest.writeString(mMessage);
        dest.writeString(mJsonUserData);
    }

    public static class Builder {
        private String mUrl;
        private String mMessage;
        private String mJsonUserData;
        private String mName;
        private int mUserId;
        private String mUserName;

        public Builder url(String url) {
            mUrl = url;
            return this;
        }

        public Builder message(String message) {
            mMessage = message;
            return this;
        }

        public Builder jsonUserData(String jsonUserData) {
            mJsonUserData = jsonUserData;
            try {
                JSONObject object = new JSONObject(jsonUserData);
                mName = object.getString("name");
                mUserId = object.getInt("id");
                mUserName = object.getString("username");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return this;
        }

        public User build() {
            User user = new User();
            user.mUrl = mUrl;
            user.mMessage = mMessage;
            user.mJsonUserData = mJsonUserData;
            user.mName = mName;
            user.mUserId = mUserId;
            user.mUserName = mUserName;
            return user;
        }
    }
}
