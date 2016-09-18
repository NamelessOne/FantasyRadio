package ru.sigil.fantasyradio.dagger;

import ru.sigil.fantasyradio.MyApp;

/**
 * Created by NamelessOne
 * on 17.09.2016.
 */
public enum Bootstrap {
    INSTANCE;

    private PlayerComponent bootstrap;

    public void setup(MyApp app) {
        PlayerModule module = new PlayerModule(app);
        bootstrap =  DaggerPlayerComponent.builder().playerModule(module).build();
    }

    public PlayerComponent getBootstrap() {
        return bootstrap;
    }
}