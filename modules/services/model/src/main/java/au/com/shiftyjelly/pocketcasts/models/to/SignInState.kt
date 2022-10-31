package au.com.shiftyjelly.pocketcasts.models.to

import au.com.shiftyjelly.pocketcasts.models.type.SubscriptionPlatform
import au.com.shiftyjelly.pocketcasts.utils.DateUtil
import java.util.Date

private val paidSubscriptionPlatforms = listOf(SubscriptionPlatform.ANDROID, SubscriptionPlatform.IOS, SubscriptionPlatform.WEB)

sealed class SignInState {
    data class SignedIn(val email: String, val subscriptionStatus: SubscriptionStatus) : SignInState()
    class SignedOut : SignInState()

    val isSignedIn: Boolean
        get() = this is SignedIn

    val isSignedInAsFree: Boolean
        get() = this is SignedIn && this.subscriptionStatus is SubscriptionStatus.Free

    val isSignedInAsPlus: Boolean
        get() = this is SignedIn && this.subscriptionStatus is SubscriptionStatus.Plus

    val isSignedInAsPlusPaid: Boolean
        get() = this is SignedIn && this.subscriptionStatus is SubscriptionStatus.Plus && paidSubscriptionPlatforms.contains(this.subscriptionStatus.platform)

    val isSignedInAsPlusGifted: Boolean
        get() = this is SignedIn && this.subscriptionStatus is SubscriptionStatus.Plus && this.subscriptionStatus.platform == SubscriptionPlatform.GIFT && this.subscriptionStatus.giftDays != 0

    val isLifetimePlus: Boolean
        get() = this is SignedIn && this.subscriptionStatus.isLifetimePlus

    val isExpiredTrial: Boolean
        get() = this is SignedIn && this.subscriptionStatus is SubscriptionStatus.Free && (getDaysRemaining() ?: 0) < 0 && this.subscriptionStatus.platform == SubscriptionPlatform.GIFT

    fun getDaysRemaining(): Int? {
        if (this is SignedIn) {
            return DateUtil.daysBetweenTwoDates(Date(), this.subscriptionStatus.expiryDate ?: Date())
        }
        return null
    }
}
