package au.com.shiftyjelly.pocketcasts.account

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import au.com.shiftyjelly.pocketcasts.account.databinding.AccountActivityBinding
import au.com.shiftyjelly.pocketcasts.account.viewmodel.CreateAccountViewModel
import au.com.shiftyjelly.pocketcasts.account.viewmodel.SubscriptionType
import au.com.shiftyjelly.pocketcasts.ui.theme.Theme
import au.com.shiftyjelly.pocketcasts.utils.Util
import au.com.shiftyjelly.pocketcasts.views.helper.UiUtil
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import au.com.shiftyjelly.pocketcasts.images.R as IR

@AndroidEntryPoint
class AccountActivity : AppCompatActivity() {

    @Inject lateinit var theme: Theme

    private lateinit var binding: AccountActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(theme.activeTheme.resourceId)

        binding = AccountActivityBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        if (savedInstanceState == null) {
            val navController = findNavController(R.id.nav_host_fragment)
            val navInflater = navController.navInflater
            val graph = navInflater.inflate(R.navigation.account_nav_graph)
            val arguments = Bundle()

            val viewModel: CreateAccountViewModel by viewModels()
            if (isNewUpgradeInstance(intent)) {
                viewModel.clearReadyForUpgrade()
                graph.setStartDestination(R.id.createFrequencyFragment)
            } else if (isPromoCodeInstance(intent)) {
                graph.setStartDestination(R.id.promoCodeFragment)
                arguments.putString(PromoCodeFragment.ARG_PROMO_CODE, intent.getStringExtra(PROMO_CODE_VALUE))
            } else if (isSignInInstance(intent)) {
                graph.setStartDestination(R.id.signInFragment)
                arguments.putParcelable(SignInFragment.EXTRA_SUCCESS_INTENT, intent.getParcelableExtra(SUCCESS_INTENT))
            } else {
                if (isNewAutoSelectPlusInstance(intent)) {
                    viewModel.defaultSubscriptionType = SubscriptionType.PLUS
                }
                viewModel.clearValues()
                graph.setStartDestination(R.id.accountFragment)
            }

            viewModel.supporterInstance = intent.getBooleanExtra(SUPPORTER_INTENT, false)

            navController.setGraph(graph, arguments)

            val navConfiguration = AppBarConfiguration(navController.graph)
            binding.toolbar?.setupWithNavController(navController, navConfiguration)
            binding.carHeader?.btnClose?.setOnClickListener { onBackPressed() }

            navController.addOnDestinationChangedListener { _, destination, _ ->
                if (!Util.isCarUiMode(this)) {
                    when (destination.id) {
                        R.id.createDoneFragment, R.id.accountFragment, R.id.promoCodeFragment -> {
                            binding.toolbar?.isVisible = false
                        }
                        else -> {
                            binding.toolbar?.isVisible = true
                        }
                    }
                } else {
                    val resource = when (destination.id) {
                        R.id.createDoneFragment, R.id.accountFragment, R.id.promoCodeFragment -> {
                            IR.drawable.ic_close
                        }
                        else -> {
                            IR.drawable.ic_arrow_back
                        }
                    }
                    binding.carHeader?.btnClose?.setImageResource(resource)
                }
            }
        }
    }

    override fun onBackPressed() {
        val currentFragment = findNavController(R.id.nav_host_fragment).currentDestination
        if (currentFragment?.id == R.id.createPayNowFragment || currentFragment?.id == R.id.createDoneFragment) {
            finish()
            return
        }

        UiUtil.hideKeyboard(binding.root)
        super.onBackPressed()
    }

    companion object {
        const val SKIP_FIRST = "account_activity.skip_first"
        fun newUpgradeInstance(context: Context?): Intent {
            val intent = Intent(context, AccountActivity::class.java)
            intent.putExtra(SKIP_FIRST, true)
            return intent
        }
        fun isNewUpgradeInstance(intent: Intent): Boolean {
            return intent.getBooleanExtra(SKIP_FIRST, false)
        }

        const val AUTO_SELECT_PLUS = "account_activity.autoSelectPlus"
        fun newAutoSelectPlusInstance(context: Context?): Intent {
            val intent = Intent(context, AccountActivity::class.java)
            intent.putExtra(AUTO_SELECT_PLUS, true)
            return intent
        }
        fun isNewAutoSelectPlusInstance(intent: Intent): Boolean {
            return intent.getBooleanExtra(AUTO_SELECT_PLUS, false)
        }

        const val IS_PROMO_CODE = "account_activity.is_promo_code"
        const val PROMO_CODE_VALUE = "account_activity.promo_code"
        const val PROMO_CODE_RETURN_DESCRIPTION = "account_activity.promo_code_return_description"
        const val SIGN_IN_ONLY = "account_activity.sign_in_only"
        const val SUCCESS_INTENT = "account_activity.success_intent"
        const val SUPPORTER_INTENT = "account_activity.supporter"

        fun promoCodeInstance(context: Context?, code: String): Intent {
            val intent = Intent(context, AccountActivity::class.java)
            intent.putExtra(IS_PROMO_CODE, true)
            intent.putExtra(PROMO_CODE_VALUE, code)
            return intent
        }

        fun isPromoCodeInstance(intent: Intent): Boolean {
            return intent.getBooleanExtra(IS_PROMO_CODE, false)
        }

        fun isSignInInstance(intent: Intent): Boolean {
            return intent.getBooleanExtra(SIGN_IN_ONLY, false)
        }

        fun signInInstance(context: Context, intentOnSuccess: PendingIntent?): Intent {
            val intent = Intent(context, AccountActivity::class.java)
            intent.putExtra(SIGN_IN_ONLY, true)
            intent.putExtra(SUCCESS_INTENT, intentOnSuccess)
            return intent
        }

        fun supporterInstance(context: Context): Intent {
            val intent = Intent(context, AccountActivity::class.java)
            intent.putExtra(SUPPORTER_INTENT, true)
            return intent
        }
    }
}