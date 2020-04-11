package ph.esrconstruction.esrsys.esrsysmobile.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import android.util.Patterns
import com.orhanobut.logger.Logger
import io.realm.Realm
import ph.esrconstruction.esrsys.esrsysmobile.ESRSys
import ph.esrconstruction.esrsys.esrsysmobile.data.LoginRepository
import ph.esrconstruction.esrsys.esrsysmobile.data.Result

import ph.esrconstruction.esrsys.esrsysmobile.R
import ph.esrconstruction.esrsys.esrsysmobile.data.model.LoggedInUser

class LoginViewModel(val loginRepository: LoginRepository) : ViewModel() {
    internal lateinit var realm: Realm
    private val _loginForm = MutableLiveData<LoginFormState>()
    val loginFormState: LiveData<LoginFormState> = _loginForm

    private val _loginResult = MutableLiveData<LoginResult>()
    val loginResult: LiveData<LoginResult> = _loginResult


    fun asyncLogin(username: String, password: String) {
        // handle login

        ////////////////*
        val result = loginRepository.asyncLogin(username, password, myCallback = {

            if (it is Result.Success) {
                _loginResult.value= LoginResult(success = LoggedInUserView(displayName = it.data.displayName))
            } else {
                _loginResult.value = LoginResult(error = R.string.login_failed)
                Logger.d(it.toString())
            }

        })



    }



    fun loginDataChanged(username: String, password: String) {
        if (!isUserNameValid(username)) {
            _loginForm.value = LoginFormState(usernameError = R.string.invalid_username)
        } else if (!isPasswordValid(password)) {
            _loginForm.value = LoginFormState(passwordError = R.string.invalid_password)
        } else {
            _loginForm.value = LoginFormState(isDataValid = true)
        }
    }

    // A placeholder username validation check
    private fun isUserNameValid(username: String): Boolean {
        return if (username.contains('@')) {
            Patterns.EMAIL_ADDRESS.matcher(username).matches()
        } else {
            username.isNotBlank()
        }
    }

    // A placeholder password validation check
    private fun isPasswordValid(password: String): Boolean {
        return password.length > 5;
    }
}
