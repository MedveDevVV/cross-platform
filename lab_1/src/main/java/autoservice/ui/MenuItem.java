package autoservice.ui;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public record MenuItem(String title, IAction action) {


    @Contract("_, _, _ -> new")
    public static @NotNull MenuItem createNaviItem(String title, Navigator navigator, Menu nextMenu) {
        return new MenuItem(title, () -> navigator.setCurrentMenu(nextMenu));
    }

    @Contract("_, _ -> new")
    public static @NotNull MenuItem createItem(String title, IAction action) {
        return new MenuItem(title, action);
    }

    // Фабричный метод для пунктов меню с проверкой доступности
    @Contract("_, _, _ -> new")
    public static @NotNull MenuItem createConditionalItem(String title, IAction action, boolean isAvailable) {
        return new MenuItem(title, isAvailable ? action : () ->
                System.out.println("Этот пункт меню недоступен"));
    }
}