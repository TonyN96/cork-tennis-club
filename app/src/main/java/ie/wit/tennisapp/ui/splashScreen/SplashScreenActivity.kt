package ie.wit.tennisapp.ui.splashScreen

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import ie.wit.tennisapp.R
import ie.wit.tennisapp.ui.auth.WelcomeActivity

//Implemented with reference to the following article
//https://www.geeksforgeeks.org/how-to-create-a-splash-screen-in-android-using-kotlin/

class SplashScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        Handler().postDelayed({
            val intent = Intent(this, WelcomeActivity::class.java)
            startActivity(intent)
            finish()
        }, 3000)
    }
}
