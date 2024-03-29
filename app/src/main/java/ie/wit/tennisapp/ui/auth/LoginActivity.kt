package ie.wit.tennisapp.ui.auth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import ie.wit.tennisapp.R
import ie.wit.tennisapp.databinding.ActivityLoginBinding
import ie.wit.tennisapp.main.MainApp
import ie.wit.tennisapp.models.MemberModel
import ie.wit.tennisapp.ui.home.Home
import timber.log.Timber

class LoginActivity() : AppCompatActivity(), View.OnClickListener {

    private lateinit var authenticationViewModel : AuthenticationViewModel
    private lateinit var binding: ActivityLoginBinding
    var member = MemberModel()
    lateinit var app: MainApp
    private lateinit var auth: FirebaseAuth
    private lateinit var togglePasswordVisButton: ImageButton
    private lateinit var startForResult : ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbarAdd.title = title
        setSupportActionBar(binding.toolbarAdd)

        app = application as MainApp

        binding.loginButton.setOnClickListener(this)
        binding.togglePasswordVisButton.setOnClickListener(this)
        binding.googleSignInButton.setOnClickListener(this)

        togglePasswordVisButton = findViewById(R.id.togglePasswordVisButton)
        togglePasswordVisButton.setImageResource(R.drawable.ic_eye)

        binding.googleSignInButton.setSize(SignInButton.SIZE_WIDE)
        binding.googleSignInButton.setColorScheme(0)

        auth = FirebaseAuth.getInstance()
    }

    private fun logIn(email: String, password: String) {
        Timber.d( "signIn:$email")
        if (!validateForm()) {
            return
        }

        authenticationViewModel.login(email, password)
    }

    public override fun onStart() {
        super.onStart()

        authenticationViewModel = ViewModelProvider(this).get(AuthenticationViewModel::class.java)
        authenticationViewModel.liveFirebaseUser.observe(this, Observer {
            firebaseUser -> if (firebaseUser != null)
                startActivity(Intent(this, Home::class.java))
        })

        authenticationViewModel.firebaseAuthManager.errorStatus.observe(this, Observer
        { status -> checkStatus(status) })

        setupGoogleSignInCallback()
    }

    private fun checkStatus(error:Boolean) {
        if (error)
            Toast.makeText(this,
                getString(R.string.auth_failed),
                Toast.LENGTH_LONG).show()
    }

    private fun validateForm(): Boolean {
        var valid = true

        val email = binding.email.text.toString()
        if (TextUtils.isEmpty(email)) {
            binding.email.error = "Required."
            valid = false
        } else {
            binding.email.error = null
        }

        val password = binding.password.text.toString()
        if (TextUtils.isEmpty(password)) {
            binding.password.error = "Required."
            valid = false
        } else {
            binding.password.error = null
        }

        return valid
    }

    private fun googleSignIn() {
        val signInIntent = authenticationViewModel.firebaseAuthManager
            .googleSignInClient.value!!.signInIntent

        startForResult.launch(signInIntent)
    }

    private fun setupGoogleSignInCallback() {
        startForResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                when(result.resultCode){
                    RESULT_OK -> {
                        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                        try {
                            val account = task.getResult(ApiException::class.java)
                            authenticationViewModel.authWithGoogle(account!!)
                        } catch (e: ApiException) {
                            Timber.i( "Google sign in failed $e")
                            Toast.makeText(this,
                                getString(R.string.auth_failed),
                                Toast.LENGTH_LONG).show()
                        }
                        Timber.i("Cork Tennis Club - Google Result $result.data")
                    }
                    RESULT_CANCELED -> {

                    } else -> { }
                }
            }
    }

    private fun togglePasswordVisibility() {
        val passwordEntry = findViewById<EditText>(R.id.password)
        if(togglePasswordVisButton.drawable.constantState == ContextCompat.getDrawable(this, R.drawable.ic_eye)?.constantState) {
            togglePasswordVisButton.setImageResource(R.drawable.ic_eye_slash)
            passwordEntry.transformationMethod = HideReturnsTransformationMethod.getInstance()
        } else {
            togglePasswordVisButton.setImageResource(R.drawable.ic_eye)
            passwordEntry.transformationMethod = PasswordTransformationMethod.getInstance()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_cancel, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.item_cancel -> {
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.loginButton -> logIn(binding.email.text.toString(), binding.password.text.toString())
            R.id.togglePasswordVisButton -> togglePasswordVisibility()
            R.id.googleSignInButton -> googleSignIn()
        }
    }
}

