package nu.staldal.djdplayer.ui.login;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import android.os.AsyncTask;
import android.util.Patterns;

import nu.staldal.djdplayer.data.LoginRepository;
import nu.staldal.djdplayer.data.Result;
import nu.staldal.djdplayer.data.model.LoggedInUser;
import nu.staldal.djdplayer.R;

public class LoginViewModel extends ViewModel {

    private MutableLiveData<LoginFormState> loginFormState = new MutableLiveData<>();
    private MutableLiveData<LoginResult> loginResult = new MutableLiveData<>();
    private LoginRepository loginRepository;

    LoginViewModel(LoginRepository loginRepository) {
        this.loginRepository = loginRepository;
    }

    LiveData<LoginFormState> getLoginFormState() {
        return loginFormState;
    }

    LiveData<LoginResult> getLoginResult() {
        return loginResult;
    }

    public void login(String username, String password) {
        new LoginTask().execute(username, password);
    }

    public void loginDataChanged(String username, String password) {
        if (!isUserNameValid(username)) {
            loginFormState.setValue(new LoginFormState(R.string.invalid_username, null));
        } else if (!isPasswordValid(password)) {
            loginFormState.setValue(new LoginFormState(null, R.string.invalid_password));
        } else {
            loginFormState.setValue(new LoginFormState(true));
        }
    }

    // A placeholder username validation check
    private boolean isUserNameValid(String username) {
        if (username == null) {
            return false;
        }
        if (username.contains("@")) {
            return Patterns.EMAIL_ADDRESS.matcher(username).matches();
        } else {
            return !username.trim().isEmpty();
        }
    }

    // A placeholder password validation check
    private boolean isPasswordValid(String password) {
        return password != null && password.trim().length() > 5;
    }

    private class LoginTask extends AsyncTask<String, Void, Result<LoggedInUser>> {
        protected Result<LoggedInUser> doInBackground(String... strings) {
            String username = strings[0], password = strings[1];
            Result<LoggedInUser> result = loginRepository.login(username, password);
            return result;
        }

        protected void onPostExecute(Result<LoggedInUser> result) {
            if (result instanceof Result.Success) {
                LoggedInUser data = ((Result.Success<LoggedInUser>) result).getData();
                loginResult.setValue(new LoginResult(new LoggedInUserView(data.getDisplayName())));
            } else {
                loginResult.setValue(new LoginResult(R.string.login_failed));
            }
        }
    }

}
