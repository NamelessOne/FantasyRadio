package ru.sigil.fantasyradio.dagger

import ru.sigil.fantasyradio.MyApp



/**
 * Created by namelessone
 * on 01.12.18.
 */
enum class Bootstrap {
    INSTANCE;

    private var bootstrap: PlayerComponent? = null

    fun setup(app: MyApp) {
        val module = PlayerModule(app)
        bootstrap = DaggerPlayerComponent.builder().playerModule(module).build()
    }

    fun getBootstrap(): PlayerComponent {
        return bootstrap!!
    }
}