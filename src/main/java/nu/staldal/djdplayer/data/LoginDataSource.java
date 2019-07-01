package nu.staldal.djdplayer.data;

import android.provider.Settings;

import nu.staldal.djdplayer.data.model.LoggedInUser;
import com.github.felixgail.gplaymusic.util.TokenProvider;
import svarzee.gps.gpsoauth.AuthToken;
import java.io.IOException;

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
public class LoginDataSource {

    public Result<LoggedInUser> login(String username, String password) {

        try {
            String androidId = Settings.Secure.getString(getContentResolver(),
                    Settings.Secure.ANDROID_ID);
            AuthToken authToken = TokenProvider.provideToken(username,
                    password, androidId);
            LoggedInUser fakeUser =
                    new LoggedInUser(
                            java.util.UUID.randomUUID().toString(),
                            "Jane Doe");
            return new Result.Success<>(fakeUser);
        } catch (Exception e) {
            return new Result.Error(new IOException("Error logging in", e));
        }
    }

    public void logout() {
        // TODO: revoke authentication
    }
}
