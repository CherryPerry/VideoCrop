package ru.cherryperry.instavideo.presentation.navigation

import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentTransaction
import com.google.android.exoplayer2.util.MimeTypes
import ru.cherryperry.instavideo.presentation.navigation.cicerone.SupportAppNavigator
import ru.terrakok.cicerone.commands.Command
import ru.terrakok.cicerone.commands.Forward

class CustomSupportAppNavigator(
    private val activity: FragmentActivity,
    containerId: Int
) : SupportAppNavigator(activity, containerId) {

    override fun applyCommand(command: Command) {
        if (command is Forward) {
            val screen = command.screen
            when (screen) {
                is CloseScreen -> {
                    activity.finish()
                    return
                }
                is OpenVideoScreen -> {
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.setDataAndType(screen.uri, MimeTypes.VIDEO_H264)
                    if (intent.resolveActivity(activity.packageManager) != null) {
                        activity.startActivity(intent)
                    }
                    return
                }
            }
        }
        super.applyCommand(command)
    }

    override fun setupFragmentTransaction(
        command: Command,
        currentFragment: Fragment?,
        nextFragment: Fragment,
        fragmentTransaction: FragmentTransaction
    ) {
        super.setupFragmentTransaction(command, currentFragment, nextFragment, fragmentTransaction)
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
    }
}
